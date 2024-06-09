package io.innospots.schedule.listener;

import io.innospots.base.events.IEventListener;
import io.innospots.base.quartz.ScheduleJobInfo;
import io.innospots.base.utils.BeanUtils;
import io.innospots.libra.base.events.ResourceActionEvent;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;

import java.util.Map;

import static io.innospots.base.constant.ModuleConstants.JOB_MODULE;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/3
 */
public class ScheduleJobInfoListener implements IEventListener<ResourceActionEvent> {

    private ScheduleJobInfoOperator scheduleJobInfoOperator;


    public ScheduleJobInfoListener(ScheduleJobInfoOperator scheduleJobInfoOperator) {
        this.scheduleJobInfoOperator = scheduleJobInfoOperator;
    }

    @Override
    public Object listen(ResourceActionEvent event) {
        ScheduleJobInfo scheduleJobInfo = null;
        if (JOB_MODULE.equals(event.getModule())) {
            switch (event.getResourceAction()){
                case SAVE:
                    scheduleJobInfo = getInfo(event);
                    scheduleJobInfo = scheduleJobInfoOperator.saveScheduleInfo(scheduleJobInfo);
                case CREATE:
                    scheduleJobInfo = getInfo(event);
                    scheduleJobInfo = scheduleJobInfoOperator.createScheduleJobInfo(scheduleJobInfo);
                    break;
                case UPDATE:
                    scheduleJobInfo = getInfo(event);
                    scheduleJobInfoOperator.updateScheduleJobInfo(scheduleJobInfo);
                    break;
                case DELETE:
                    String jobKey = String.valueOf(event.getPrimaryId());
                    scheduleJobInfoOperator.deleteScheduleJobInfo(jobKey);
                    break;
                case STATUS:
                    String jobKey2 = String.valueOf(event.getPrimaryId());
                    scheduleJobInfoOperator.updateScheduleJobStatus(jobKey2, event.getStatus());
                    break;
            }
        }
        return scheduleJobInfo;
    }

    private ScheduleJobInfo getInfo(ResourceActionEvent event){
        ScheduleJobInfo scheduleJobInfo = null;
        Object body = event.getBody();
        if (body instanceof Map) {
            Map paramMap = (Map) event.getBody();
            scheduleJobInfo = BeanUtils.toBean(paramMap, ScheduleJobInfo.class);
        } else if (body instanceof ScheduleJobInfo) {
            scheduleJobInfo = (ScheduleJobInfo) body;
        }
        return scheduleJobInfo;
    }
}
