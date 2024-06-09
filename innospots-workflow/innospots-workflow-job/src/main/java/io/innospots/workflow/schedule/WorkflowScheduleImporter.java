package io.innospots.workflow.schedule;

import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.schedule.operator.JobExecutionOperator;
import io.innospots.workflow.core.config.WorkflowCoreConfiguration;
import io.innospots.workflow.core.flow.loader.IWorkflowLoader;
import io.innospots.workflow.schedule.flow.FlowJobExecutor;
import io.innospots.workflow.schedule.listener.FlowJobScheduleListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/8
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({WorkflowScheduleImporter.WorkflowScheduleConfiguration.class, WorkflowCoreConfiguration.class})
public @interface WorkflowScheduleImporter {

    @Configuration
    class WorkflowScheduleConfiguration {

        @Bean
        public FlowJobExecutor flowJobExecutor(JobExecutionOperator jobExecutionOperator,
                                               ReadJobDispatcher readJobDispatcher,
                                               IWorkflowLoader workflowLoader){
            return new FlowJobExecutor(jobExecutionOperator,readJobDispatcher,workflowLoader);
        }

        @Bean
        public FlowJobScheduleListener flowJobScheduleListener(FlowJobExecutor flowJobExecutor){
            return new FlowJobScheduleListener(flowJobExecutor);
        }
    }
}
