package io.innospots.schedule;

import io.innospots.schedule.config.InnospotsScheduleProperties;
import io.innospots.schedule.controller.JobExecutionController;
import io.innospots.schedule.controller.JobExecutorController;
import io.innospots.schedule.controller.ScheduleJobInfoController;
import io.innospots.schedule.dao.ReadyJobDao;
import io.innospots.schedule.dao.ScheduleJobInfoDao;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.schedule.explore.ScheduleJobInfoExplorer;
import io.innospots.schedule.operator.JobExecutionOperator;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import io.innospots.schedule.queue.IReadyJobQueue;
import io.innospots.schedule.queue.ReadyJobDbQueue;
import org.mybatis.spring.annotation.MapperScan;
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
        public ReadJobDispatcher readJobDispatcher(IReadyJobQueue readyJobDbQueue){
            return new ReadJobDispatcher(readyJobDbQueue);
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

    }

}
