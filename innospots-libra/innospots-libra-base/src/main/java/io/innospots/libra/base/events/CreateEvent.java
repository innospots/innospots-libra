package io.innospots.libra.base.events;

import io.innospots.base.events.EventBody;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/3
 */
public class CreateEvent extends EventBody {

    private String module;

    public CreateEvent(String module, Map<String, Object> paramBody) {
        super(paramBody);
        this.module = module;
    }

    public String getModule() {
        return module;
    }
}
