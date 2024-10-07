package io.innospots.workflow.node.app.response;

import io.innospots.base.model.field.ParamField;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/5
 */
@Getter
@Setter
public class ResponseField {

    private String fieldCode;

    private ParamField sourceField;

    public Object value(Map<String, Object> item) {
        return item.get(sourceField.getCode());
    }

}
