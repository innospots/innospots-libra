package io.innospots.app.visitor.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @date 2024/8/29
 */
@Getter
@Setter
public class RequestAccess {

    private String accessKey;
    private String sign;

}
