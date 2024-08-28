package io.innospots.app.visitor.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/28
 */
@Getter
@Setter
public class AppToken {

    private String token;

    private String appKey;

    private long ts;
}
