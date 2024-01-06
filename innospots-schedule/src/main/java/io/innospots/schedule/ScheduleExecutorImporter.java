package io.innospots.schedule;

import io.innospots.base.quartz.QuartzScheduleManager;
import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import io.innospots.schedule.config.InnospotsScheduleProperties;
import io.innospots.schedule.dao.JobExecutionDao;
import io.innospots.schedule.dao.ReadyJobDao;
import io.innospots.schedule.dao.ScheduleJobInfoDao;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.schedule.explore.JobExecutionExplorer;
import io.innospots.schedule.explore.ScheduleJobInfoExplorer;
import io.innospots.schedule.launcher.ReadyJobLauncher;
import io.innospots.schedule.operator.JobExecutionOperator;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import io.innospots.schedule.queue.IReadyJobQueue;
import io.innospots.schedule.queue.ReadyJobDbQueue;
import io.innospots.schedule.queue.ReadyJobQueueListener;
import io.innospots.schedule.watcher.JobExecutionWatcher;
import io.innospots.schedule.watcher.ReadyQueueWatcher;
import io.innospots.schedule.watcher.ScheduleJobWatcher;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
@Import({ScheduleExecutorImporter.InnerConfiguration.class})
public @interface ScheduleExecutorImporter {


    @Configuration
    @EnableConfigurationProperties(InnospotsScheduleProperties.class)
    @EntityScan(basePackages = "io.innospots.schedule.entity")
    @MapperScan(basePackages = "io.innospots.schedule.dao")
    @ComponentScan(basePackages = "io.innospots.schedule")
    class InnerConfiguration {


        @Bean
        public IReadyJobQueue readyJobDbQueue(ScheduleJobInfoExplorer scheduleJobInfoExplorer, ReadyJobDao readyJobDao){
            return new ReadyJobDbQueue(scheduleJobInfoExplorer,readyJobDao);
        }


        @Bean
        public ReadyJobQueueListener readyJobQueueListener(IReadyJobQueue readyJobDbQueue){
            return new ReadyJobQueueListener(readyJobDbQueue);
        }

        @Bean
        public ReadJobDispatcher readJobDispatcher(IReadyJobQueue readyJobDbQueue){
            return new ReadJobDispatcher(readyJobDbQueue);
        }

        @Bean
        public JobExecutionExplorer jobExecutionExplorer(JobExecutionDao jobExecutionDao){
            return new JobExecutionExplorer(jobExecutionDao);
        }

        @Bean
        public ReadyJobLauncher readyJobLauncher(JobExecutionExplorer jobExecutionExplorer,
                                                 ReadyJobDbQueue readyJobDbQueue, ThreadTaskExecutor executorThreadPool){
            return new ReadyJobLauncher(jobExecutionExplorer,readyJobDbQueue,executorThreadPool);
        }

        public ThreadTaskExecutor executorThreadPool(InnospotsScheduleProperties scheduleProperties){
            return ThreadPoolBuilder.build(scheduleProperties.getExecutorSize(),scheduleProperties.getExecutorSize(),0,"executorThreadPool");
        }

        @Bean
        public JobExecutionWatcher jobExecutionWatcher(JobExecutionExplorer jobExecutionExplorer){
            return new JobExecutionWatcher(jobExecutionExplorer);
        }

        @Bean
        public ReadyQueueWatcher readyQueueWatcher(IReadyJobQueue readyJobDbQueue,
                                                   ReadyJobLauncher readyJobLauncher,
                                                   InnospotsScheduleProperties scheduleProperties){
            return new ReadyQueueWatcher(readyJobDbQueue,readyJobLauncher,scheduleProperties);
        }

        @Bean
        @ConditionalOnMissingBean(QuartzScheduleManager.class)
        public QuartzScheduleManager quartzScheduleManager(){
            return new QuartzScheduleManager();
        }


        @Bean
        public ScheduleJobInfoExplorer scheduleJobInfoExplorer(ScheduleJobInfoDao scheduleJobInfoDao){
            return new ScheduleJobInfoExplorer(scheduleJobInfoDao);
        }

        @Bean
        public ScheduleJobWatcher scheduleJobWatcher(QuartzScheduleManager scheduleManager,
                                                     ScheduleJobInfoExplorer scheduleJobInfoExplorer){
            return new ScheduleJobWatcher(scheduleManager,scheduleJobInfoExplorer);
        }

    }

}
