package io.innospots.app.visitor.operator;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author Smars
 * @date 2024/8/29
 */
public class AppStreamDataOperator {



    public SseEmitter stream(String appKey, String registryId, Map<String,Object> params) {
        return null;
    }
}
