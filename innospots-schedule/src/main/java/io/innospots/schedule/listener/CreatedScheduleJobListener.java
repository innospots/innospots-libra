package io.innospots.schedule.listener;

import io.innospots.base.events.IEventListener;
import io.innospots.base.utils.BeanUtils;
import io.innospots.libra.base.events.CreateEvent;
import io.innospots.schedule.model.ScheduleJobInfo;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/3
 */
public class CreatedScheduleJobListener implements IEventListener<CreateEvent> {

    private static final String JOB_MODULE = "job";

    private ScheduleJobInfoOperator scheduleJobInfoOperator;


    public CreatedScheduleJobListener(ScheduleJobInfoOperator scheduleJobInfoOperator) {
        this.scheduleJobInfoOperator = scheduleJobInfoOperator;
    }

    @Override
    public Object listen(CreateEvent event) {
        ScheduleJobInfo scheduleJobInfo = null;
        if(JOB_MODULE.equals(event.getModule())){
            Map paramMap = (Map) event.getBody();
            scheduleJobInfo = BeanUtils.toBean(paramMap, ScheduleJobInfo.class);
            scheduleJobInfo = scheduleJobInfoOperator.createScheduleJobInfo(scheduleJobInfo);
        }
        return scheduleJobInfo;
    }
}
