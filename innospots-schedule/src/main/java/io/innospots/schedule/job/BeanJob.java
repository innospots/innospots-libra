package io.innospots.schedule.job;

import cn.hutool.extra.spring.SpringUtil;
import io.innospots.schedule.enums.JobType;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * using spring bean to execute job
 *
 * @author Smars
 * @vesion 2.0
 * @date 2024/5/19
 */
public class BeanJob extends BaseJob {

    public static String PARAM_BEAN_NAME = "job.bean.name";

    public static String PARAM_BEAN_METHOD = "job.bean.method";

    public static final String PARAM_EXECUTE_JOB_PARAMS = "job.execute.job_params";

    private String beanName;
    private String methodName;
    private Map<String, Object> params;

    @Override
    public void prepare() {
        this.beanName = validParamString(PARAM_BEAN_NAME);
        this.methodName = validParamString(PARAM_BEAN_METHOD);
        this.params = getParamMap(PARAM_EXECUTE_JOB_PARAMS);
    }

    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }


    @Override
    public void execute() {
        Object jobObject = SpringUtil.getBean(this.beanName);
        try {
            Method method = jobObject.getClass().getMethod(this.methodName, Map.class);
            Object res = method.invoke(jobObject, this.params);
            this.jobExecution.setMessage(String.valueOf(res));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
