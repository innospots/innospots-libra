package io.innospots.workflow.runtime.endpoint;

import io.innospots.base.constant.PathConstant;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.utils.DataFakerUtils;
import io.innospots.base.utils.FakerExpression;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.sse.BaseEventEmitter;
import io.innospots.workflow.core.sse.NodeExecutionEmitter;
import io.innospots.workflow.core.sse.SseEmitterNodeExecutionListener;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/24
 */
@Slf4j
@RequestMapping(PathConstant.ROOT_PATH + "workflow/sse")
@RestController
@Tag(name = "server send event")
public class SseEventEndpoint {

    private ExecutorService sseMvcExecutor = Executors.newCachedThreadPool();

    private NodeExecutionEmitter nodeExecutionEmitter = new NodeExecutionEmitter();

    @GetMapping(path = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Flux - " + LocalTime.now().toString());
    }

    @GetMapping("/stream-sse")
    public Flux<ServerSentEvent<String>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> {
                            if (sequence.intValue() < 5) {
                                ServerSentEvent sse =
                                        ServerSentEvent.<String>builder()
                                                .id(String.valueOf(sequence))
                                                .event("periodic-event")
                                                .data("SSE - " + LocalTime.now().toString())
                                                .build();
                                return sse;
                            } else {
                                return ServerSentEvent.<String>builder().data("").build();
                            }
                        }
                );
    }

    @GetMapping("/stream-sse-mvc")
    public SseEmitter sseEmitter() {
        SseEmitter emitter = new SseEmitter(1000 * 60L * 2);

        sseMvcExecutor.execute(() -> {
            try {
                for (int i = 0; true; i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data("SSE MVC - " + LocalTime.now().toString())
                            .id(String.valueOf(i))
                            .name("sse event - mvc");
                    emitter.send(event);
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
                //log.error(ex.getMessage(),ex);
            }
            log.info("quit emmitter.");
        });

        emitter.onCompletion(() -> System.out.println("completed"));
        emitter.onTimeout(() -> System.out.println("timeout"));
        emitter.onError((e) -> System.out.println("error "));
        return emitter;
    }

    @GetMapping("/stream-node-execution-output")
    public SseEmitter nodeExecutionOutput(
            @RequestParam String nodeExecutionId,
            @RequestParam String streamId,
            @RequestParam(required = false,defaultValue = "25") Integer size,
            @RequestParam(required = false, defaultValue = "10") Integer rsize
            ) {
        if (BaseEventEmitter.hasExist(nodeExecutionId, streamId)) {
            return BaseEventEmitter.getEmitter(nodeExecutionId, streamId);
        }

        SseEmitter emitter = BaseEventEmitter.createEmitter(nodeExecutionId,"node-execution", streamId);
        sentNodeExecution(nodeExecutionId, emitter, size, rsize);
        log.info("create emmitter node execution:{}",nodeExecutionId);
        return emitter;
    }

    private void sentNodeExecution(String executionId,SseEmitter emitter, int size, int rsize) {
        sseMvcExecutor.execute(() -> {
            NodeExecution ne = sample(executionId, size, rsize);
            SseEmitterNodeExecutionListener listener = new SseEmitterNodeExecutionListener();
            try {
                log.info("send log");
                //listener.log(ne,null);
                for (NodeOutput output : ne.getOutputs()) {
                    log.info("send item");
                    for (Map<String, Object> result : output.getResults()) {
                        listener.item(ne, result);
                        Thread.sleep(1000);
                    }
                    log.info("send resource");
                    for (List<ExecutionResource> value : output.getResources().values()) {
                        for (ExecutionResource executionResource : value) {
                            listener.item(ne,executionResource);
                            Thread.sleep(1500);
                        }
                    }
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
                //log.error(ex.getMessage(),ex);
            }
            log.info("quit SseEmitter.");
            emitter.complete();
        });
    }

    private NodeExecution sample(String nodeExecutionId, int size, int resSize) {
        NodeExecution ne = NodeExecution.buildNewNodeExecution("abc", 1L, 1, "flow_executionId_abc", true);
        ne.addLog("create", "创建上下文");
        ne.setNodeExecutionId(nodeExecutionId);
        DataFakerUtils df = DataFakerUtils.build();
        NodeOutput output = new NodeOutput();
        for (int i = 0; i < size; i++) {
            output.addResult(df.sample());
        }
        ne.addLog("item", "item size:" + size);
        for (int i = 0; i < resSize; i++) {
            ExecutionResource es = new ExecutionResource();
            es.setFileSize(df.genNumbers(2) + "kb");
            es.setResourceId(df.gen(FakerExpression.RANDOM_HEX_32));
            es.setMimeType(df.gen("pdf", "txt", "png", "jpeg", "zip"));
            es.setResourceName(df.faker().book().title());
            output.addResource(i, es);
        }//end for
        ne.addLog("res", "res size:" + resSize);
        ne.addLog("end_time", df.gen(FakerExpression.DATE_FUTURE_15_HOURS));
        ne.addOutput(output);
        ne.end("sample msg");
        return ne;
    }
}
