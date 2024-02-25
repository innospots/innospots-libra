package io.innospots.schedule;

import io.innospots.libra.base.model.swagger.SwaggerOpenApiBuilder;
import io.innospots.schedule.config.InnospotsScheduleProperties;
import io.innospots.schedule.controller.JobExecutionController;
import io.innospots.schedule.controller.JobExecutorController;
import io.innospots.schedule.controller.ScheduleJobInfoController;
import io.innospots.schedule.dao.JobExecutionDao;
import io.innospots.schedule.dao.ReadyJobDao;
import io.innospots.schedule.dao.ScheduleJobInfoDao;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.schedule.explore.JobExecutionExplorer;
import io.innospots.schedule.explore.ScheduleJobInfoExplorer;
import io.innospots.schedule.operator.JobExecutionOperator;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import io.innospots.schedule.queue.IReadyJobQueue;
import io.innospots.schedule.queue.ReadyJobDbQueue;
import org.mybatis.spring.annotation.MapperScan;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
@Import({ScheduleConsoleImporter.InnerConfiguration.class})
public @interface ScheduleConsoleImporter {


    @Configuration
//    @ComponentScan(value = {"io.innospots.schedule.controller"})
    @EnableConfigurationProperties(InnospotsScheduleProperties.class)
    @EntityScan(basePackages = "io.innospots.schedule.entity")
    @MapperScan(basePackages = "io.innospots.schedule.dao")
    class InnerConfiguration {

        @Bean
        public JobExecutionOperator jobExecutionOperator(){
            return new JobExecutionOperator();
        }

        @Bean
        public ScheduleJobInfoOperator scheduleJobInfoOperator(){
            return new ScheduleJobInfoOperator();
        }

        @Bean
        public IReadyJobQueue readyJobDbQueue(ScheduleJobInfoExplorer scheduleJobInfoExplorer, ReadyJobDao readyJobDao){
            return new ReadyJobDbQueue(scheduleJobInfoExplorer,readyJobDao);
        }

        @Bean
        public JobExecutionExplorer jobExecutionExplorer(JobExecutionDao jobExecutionDao){
            return new JobExecutionExplorer(jobExecutionDao);
        }

        @Bean
        public ReadJobDispatcher readJobDispatcher(IReadyJobQueue readyJobDbQueue, JobExecutionExplorer jobExecutionExplorer){
            return new ReadJobDispatcher(readyJobDbQueue, jobExecutionExplorer);
        }

        @Bean
        public ScheduleJobInfoExplorer scheduleJobInfoExplorer(ScheduleJobInfoDao scheduleJobInfoDao){
            return new ScheduleJobInfoExplorer(scheduleJobInfoDao);
        }

        @Bean
        public JobExecutionController jobExecutionController(JobExecutionOperator jobExecutionOperator){
            return new JobExecutionController(jobExecutionOperator);
        }

        @Bean
        public JobExecutorController jobExecutorController(ReadJobDispatcher readJobDispatcher){
            return new JobExecutorController(readJobDispatcher);
        }

        @Bean
        public ScheduleJobInfoController scheduleJobInfoController(ScheduleJobInfoOperator scheduleJobInfoOperator){
            return new ScheduleJobInfoController(scheduleJobInfoOperator);
        }

        @Bean
        @ConditionalOnProperty(prefix = "innospots.config", name = "enable-swagger", havingValue = "true")
        public GroupedOpenApi scheduleConsoleGroupedOpenApi() {
            return SwaggerOpenApiBuilder.buildGroupedOpenApi("schedule-console", "io.innospots.schedule");
        }

    }

}
