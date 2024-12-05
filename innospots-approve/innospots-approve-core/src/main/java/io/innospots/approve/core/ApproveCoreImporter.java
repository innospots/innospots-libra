package io.innospots.approve.core;

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
@Import({ApproveCoreImporter.ApproveInnerCoreConfiguration.class})
public @interface ApproveCoreImporter {


    @MapperScan(basePackages = {"io.innospots.approve.core.dao"})
    @EntityScan(basePackages = {"io.innospots.approve.core.entity"})
    @ComponentScan(basePackages = {"io.innospots.approve.core"})
    @Configuration
    class ApproveInnerCoreConfiguration{
    }
}
