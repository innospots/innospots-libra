package io.innospots.connector.ai.aliyun.operator;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.request.BaseRequest;

import java.util.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
public abstract class AliyunAiOperator<Prm,Msg,Res> implements IExecutionOperator {

    protected String apiKey;
    protected Map<String, Object> options;


    public AliyunAiOperator(String apiKey, Map<String, Object> options) {
        this.apiKey = apiKey;
        this.options = options;
    }

    protected abstract Res invoke(BaseRequest request);

    protected abstract Prm buildParam(BaseRequest<?> request);

    protected abstract Msg buildMessage(BaseRequest<?> request);

    protected void fillOptions(Prm param, BaseRequest<?> request){
        if (options != null) {
            BeanUtil.fillBeanWithMap(options, param, CopyOptions.create().ignoreError().ignoreNullValue());
        }

        if (request.getQuery() != null) {
            BeanUtil.fillBeanWithMap(request.getQuery(), param, CopyOptions.create().ignoreError().ignoreNullValue());
        }
        if(request.getBody() instanceof Map){
            BeanUtil.fillBeanWithMap((Map<String, Object>) request.getBody(), param, CopyOptions.create().ignoreError().ignoreNullValue());
        }
    }

}
