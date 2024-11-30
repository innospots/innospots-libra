package io.innospots.libra.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/30
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "innospots.security.oauth-provider")
public class OauthProviderProperties {

    private boolean enabled;

    @NestedConfigurationProperty
    private List<OauthProvider> providers;

}
