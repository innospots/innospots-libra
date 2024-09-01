package io.innospots.app.visitor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/1
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "innospots.application.visitor")
public class AppVisitorProperties {
    
    private String workflowAddress;

    private String workflowTestAddress;

}
