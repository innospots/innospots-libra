package io.innospots.schedule;

import io.innospots.schedule.config.InnospotsScheduleProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Smars
 * @date 2023/12/12
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableConfigurationProperties(InnospotsScheduleProperties.class)
@Import({ScheduleImporter.InnerConfiguration.class})
public @interface ScheduleImporter {


    @Configuration
    @EntityScan(basePackages = "io.innospots.schedule.entity")
    @MapperScan(basePackages = "io.innospots.schedule.dao")
    @ComponentScan(basePackages = "io.innospots.schedule")
    class InnerConfiguration {

    }

}
