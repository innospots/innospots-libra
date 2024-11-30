package io.innospots.libra.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/30
 */
@Getter
@Setter
public class OauthProvider {

    private String providerName;

    private boolean enabled=true;

    @NestedConfigurationProperty
    private UrlInfo authInfo;

    @NestedConfigurationProperty
    private UrlInfo tokenInfo;

    @NestedConfigurationProperty
    private UrlInfo refreshInfo;

    @NestedConfigurationProperty
    private UrlInfo userInfo;

    private String tokenPath = "$.access_token";

    private String expiresPath = "$.expires_in";

    private String refreshPath = "$.refresh_token";

    @Getter
    @Setter
    public static class UrlInfo {

        private String method;

        private String url;

        private Map<String,String> params;

        private Map<String,String> response;
    }

}
