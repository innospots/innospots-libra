package io.innospots.app.core.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/11
 */
@Setter
@Getter
public class AppDefinition extends AppTemplate {

    private String appKey;

    private String appPath;
}
