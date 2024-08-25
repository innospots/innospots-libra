package io.innospots.schedule;

import io.innospots.base.entity.handler.EntityMetaObjectHandler;
import io.innospots.base.quartz.QuartzScheduleManager;
import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import io.innospots.libra.base.model.swagger.SwaggerOpenApiBuilder;
import io.innospots.schedule.config.InnospotsScheduleProperties;
import io.innospots.schedule.controller.ExecutorStateController;
import io.innospots.schedule.dao.JobExecutionDao;
import io.innospots.schedule.dao.ReadyJobDao;
import io.innospots.schedule.dao.ScheduleJobInfoDao;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.schedule.explore.JobExecutionExplorer;
import io.innospots.schedule.explore.ScheduleJobInfoExplorer;
import io.innospots.schedule.launcher.ReadyJobLauncher;
import io.innospots.schedule.listener.LineChainJobListener;
import io.innospots.schedule.queue.IReadyJobQueue;
import io.innospots.schedule.queue.ReadyJobDbQueue;
import io.innospots.schedule.queue.ReadyJobQueueListener;
import io.innospots.schedule.starter.ScheduleExecutorStarter;
import io.innospots.schedule.watcher.JobExecutionWatcher;
import io.innospots.schedule.watcher.ReadyQueueWatcher;
import io.innospots.schedule.watcher.RunningExecutionWatcher;
import io.innospots.schedule.watcher.ScheduleJobWatcher;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@Import({ScheduleExecutorImporter.InnerConfiguration.class})
public @interface ScheduleExecutorImporter {


    @Configuration
    @EnableConfigurationProperties(InnospotsScheduleProperties.class)
    @EntityScan(basePackages = "io.innospots.schedule.entity")
    @MapperScan(basePackages = "io.innospots.schedule.dao")
    class InnerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public EntityMetaObjectHandler metaObjectHandler() {
            return new EntityMetaObjectHandler();
        }

        @Bean
        @ConditionalOnMissingBean
        public IReadyJobQueue readyJobDbQueue(ScheduleJobInfoExplorer scheduleJobInfoExplorer, ReadyJobDao readyJobDao){
            return new ReadyJobDbQueue(scheduleJobInfoExplorer,readyJobDao);
        }


        @Bean
        public ReadyJobQueueListener readyJobQueueListener(IReadyJobQueue readyJobDbQueue){
            return new ReadyJobQueueListener(readyJobDbQueue);
        }

        @Bean
        @ConditionalOnMissingBean
        public ReadJobDispatcher readJobDispatcher(IReadyJobQueue readyJobDbQueue,JobExecutionExplorer jobExecutionExplorer,ReadyJobDao readyJobDao){
            return new ReadJobDispatcher(readyJobDbQueue, jobExecutionExplorer,readyJobDao);
        }

        @Bean
        @ConditionalOnMissingBean
        public JobExecutionExplorer jobExecutionExplorer(JobExecutionDao jobExecutionDao){
            return new JobExecutionExplorer(jobExecutionDao);
        }

        @Bean
        public ReadyJobLauncher readyJobLauncher(JobExecutionExplorer jobExecutionExplorer,
                                                 ScheduleJobInfoExplorer scheduleJobInfoExplorer,
                                                 IReadyJobQueue readyJobDbQueue, @Qualifier("executorThreadPool") ThreadTaskExecutor executorThreadPool){
            return new ReadyJobLauncher(scheduleJobInfoExplorer,jobExecutionExplorer,readyJobDbQueue,executorThreadPool);
        }

        @Bean
        public ThreadTaskExecutor executorThreadPool(InnospotsScheduleProperties scheduleProperties){
            return ThreadPoolBuilder.build(scheduleProperties.getExecutorSize(),scheduleProperties.getExecutorSize(),0,"job-executor");
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
            QuartzScheduleManager scheduleManager = new QuartzScheduleManager();
            scheduleManager.startup();
            return scheduleManager;
        }


        @Bean
        @ConditionalOnMissingBean
        public ScheduleJobInfoExplorer scheduleJobInfoExplorer(ScheduleJobInfoDao scheduleJobInfoDao){
            return new ScheduleJobInfoExplorer(scheduleJobInfoDao);
        }

        @Bean
        public ScheduleJobWatcher scheduleJobWatcher(QuartzScheduleManager scheduleManager,
                                                     ScheduleJobInfoExplorer scheduleJobInfoExplorer){
            return new ScheduleJobWatcher(scheduleManager,scheduleJobInfoExplorer);
        }

        @Bean
        public LineChainJobListener lineChainJobListener(ReadJobDispatcher readJobDispatcher,
                                                          JobExecutionExplorer jobExecutionExplorer){
            return new LineChainJobListener(readJobDispatcher,jobExecutionExplorer);
        }


        @Bean
        public RunningExecutionWatcher runningExecutionWatcher(JobExecutionExplorer jobExecutionExplorer,
                                                               ReadyJobLauncher readyJobLauncher){
            return new RunningExecutionWatcher(readyJobLauncher,jobExecutionExplorer);
        }

        @Bean
        public ScheduleExecutorStarter scheduleExecutorStarter(InnospotsScheduleProperties scheduleProperties){
            return new ScheduleExecutorStarter(scheduleProperties);
        }

        @Bean
        public ExecutorStateController executorStateController(QuartzScheduleManager quartzScheduleManager,
                                                               ReadyJobLauncher readyJobLauncher){
            return new ExecutorStateController(quartzScheduleManager,readyJobLauncher);
        }

        /*
        @Bean
        @ConditionalOnProperty(prefix = "innospots.config", name = "enable-swagger", havingValue = "true")
        public GroupedOpenApi scheduleExecutorGroupedOpenApi() {
            return GroupedOpenApi.builder().group("schedule-executor")
                    .packagesToScan("io.innospots.schedule")
                    .pathsToMatch("/**")
                    .build();
        }

         */
    }

}
