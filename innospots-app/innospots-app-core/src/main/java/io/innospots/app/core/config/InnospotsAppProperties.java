package io.innospots.app.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/7
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "innospots.application")
public class InnospotsAppProperties {
}
