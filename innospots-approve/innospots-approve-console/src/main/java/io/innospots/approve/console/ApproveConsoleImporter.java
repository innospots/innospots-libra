package io.innospots.approve.console;

import io.innospots.approve.core.ApproveCoreImporter;
import io.innospots.approve.console.interceptor.ApproveUserHandlerInterceptor;
import io.innospots.base.constant.PathConstant;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.annotation.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ApproveConsoleImporter.ApproveConsoleConfiguration.class})
public @interface ApproveConsoleImporter {

    @ApproveCoreImporter
    @ComponentScan(basePackages = {"io.innospots.approve.console"})
    @Configuration
    class ApproveConsoleConfiguration implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            ApproveUserHandlerInterceptor interceptor = new ApproveUserHandlerInterceptor();
            registry.addInterceptor(interceptor).addPathPatterns(PathConstant.ROOT_PATH+"approve/**");
        }
    }
}
