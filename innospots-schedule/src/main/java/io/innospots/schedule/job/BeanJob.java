package io.innospots.schedule.job;

import cn.hutool.extra.spring.SpringUtil;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.base.utils.BeanUtils;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.model.JobExecution;

import java.lang.reflect.Method;
import java.util.HashMap;
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

    public BeanJob(JobExecution jobExecution) {
        super(jobExecution);
    }

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
    public InnospotResponse<Map<String, Object>> execute() {
        Object jobObject = SpringUtil.getBean(this.beanName);
        InnospotResponse<Map<String, Object>> resp = new InnospotResponse<>();
        try {
            Method method = jobObject.getClass().getMethod(this.methodName, Map.class);
            Object result = method.invoke(jobObject, this.params);
            if (result instanceof InnospotResponse) {
                resp = (InnospotResponse<Map<String, Object>>) result;
            } else if (result instanceof Map) {
                resp.fillBody((Map<String, Object>) result);
            } else if (result instanceof String) {
                Map<String,Object> out = new HashMap<>();
                out.put("job_output",result);
                resp.fillBody(out);
            } else if (result != null) {
                resp.fillBody(BeanUtils.toMap(result));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resp;
    }
}
