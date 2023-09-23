package io.innospots.workflow.runtime.container.listener;

import io.innospots.base.events.EventBody;
import io.innospots.base.events.IEventListener;
import io.innospots.workflow.core.context.WorkflowRuntimeContext;
import io.innospots.workflow.core.runtime.FlowEventBody;
import io.innospots.workflow.core.webhook.WebhookPayload;
import io.innospots.workflow.runtime.container.DummyRuntimeContainer;
import io.innospots.workflow.runtime.container.WebhookRuntimeContainer;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/19
 */
public class WorkflowRuntimeEventListener implements IEventListener<FlowEventBody> {

    private DummyRuntimeContainer dummyRuntimeContainer;

    private WebhookRuntimeContainer webhookRuntimeContainer;

    private static final String DUMMY_NODE = "START";
    private static final String WEBHOOK_NODE = "WEBHOOK";

    public WorkflowRuntimeEventListener(DummyRuntimeContainer dummyRuntimeContainer, WebhookRuntimeContainer webhookRuntimeContainer) {
        this.dummyRuntimeContainer = dummyRuntimeContainer;
        this.webhookRuntimeContainer = webhookRuntimeContainer;
    }

    @Override
    public Object listen(FlowEventBody event) {
        if(DUMMY_NODE.equals(event.getNodeCode())){
            WorkflowRuntimeContext runtimeContext =  dummyRuntimeContainer.execute(event.getFlowKey(), (Map<String, Object>) event.getBody());
            return runtimeContext.getResponse();
        }else if(WEBHOOK_NODE.equals(event.getNodeCode())){
            WebhookPayload payload = new WebhookPayload();
            payload.setFlowKey(event.getFlowKey());
            payload.setBody((Map<String, Object>) event.getBody());
            return webhookRuntimeContainer.execute(payload);
        }
        return null;
    }

    @Override
    public Class<? extends EventBody> eventBodyClass() {
        return FlowEventBody.class;
    }
}
