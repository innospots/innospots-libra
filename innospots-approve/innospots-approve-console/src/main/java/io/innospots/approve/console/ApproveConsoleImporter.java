package io.innospots.approve.console;

import io.innospots.approve.core.ApproveCoreImporter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
    class ApproveConsoleConfiguration{

    }
}
