package io.innospots.workflow.runtime.endpoint;

import io.innospots.base.constant.PathConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalTime;
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
@Tag(name = "workflow node execution")
public class SseEventEndpoint {

    private ExecutorService sseMvcExecutor = Executors.newCachedThreadPool();

    @GetMapping(path = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Flux - " + LocalTime.now().toString());
    }

    @GetMapping("/stream-sse")
    public Flux<ServerSentEvent<String>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> {
                    if(sequence.intValue() < 5){
                        ServerSentEvent sse =
                                ServerSentEvent.<String> builder()
                                        .id(String.valueOf(sequence))
                                        .event("periodic-event")
                                        .data("SSE - " + LocalTime.now().toString())
                                        .build();
                        return sse;
                    }else {
                        return ServerSentEvent.<String>builder().data("").build();
                    }
                }
                );
    }

    @GetMapping("/stream-sse-mvc")
    public SseEmitter sseEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sseMvcExecutor.execute(()->{
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
            }
            log.info("quit emmitter.");
        });

        emitter.onCompletion(() -> System.out.println("completed"));
        emitter.onTimeout(() -> System.out.println("timeout"));
        emitter.onError((e) -> System.out.println("error"));
        return emitter;
    }
}
