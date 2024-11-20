package io.innospots.approve.console;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
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


    @MapperScan(basePackages = {"io.innospots.approve.console.dao"})
    @EntityScan(basePackages = {"io.innospots.approve.console.entity"})
    @ComponentScan(basePackages = {"io.innospots.approve.console"})
    @Configuration
    class ApproveConsoleConfiguration{

    }
}
