package io.innospots.workflow.runtime.endpoint;

import io.innospots.base.constant.PathConstant;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.utils.DataFakerUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2024/6/24
 */
@RestController
@RequestMapping(PathConstant.ROOT_PATH + "workflow/stream")
@Tag(name ="workflow stream")
public class WorkflowStreamEndpoint {

    private DataFakerUtils fakerUtils = DataFakerUtils.build();

    @GetMapping(value ="chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String,Object>>> chat(@RequestParam int delay,
                                         @RequestParam int times
    ){
        Map<String,Object>[] items = new Map[times];
        for (int i = 0; i < times; i++) {
            items[i] = fakerUtils.sample();
        }


        return Flux.just(items).map(item->{
            ServerSentEvent<Map<String,Object>> event =
                    ServerSentEvent.<Map<String,Object>>builder().data(item)
                    .event("chat").build();
            return event;
        }).delayElements(Duration.ofMillis(delay));
    }
}
