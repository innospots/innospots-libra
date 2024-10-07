package io.innospots.workflow.node.app.response;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.response.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/5
 */
public class AppResponseNode extends BaseNodeExecutor {

    private static final String FIELD_CARD_LAYOUT = "card_layout";

    private static final String FIELD_RESPONSE_FIELDS = "response_fields";

    private static final String FIELD_CARD_ALIGN = "card_align";

    private static final String FIELD_LAYOUT_COLUMN = "layout_column";

    private CardLayout cardLayout;

    private CardAlign cardAlign;

    private Integer layoutColumn;

    private List<ResponseField> responseFields;


    @Override
    protected void initialize() {
        responseFields = buildResponseField(ni, FIELD_RESPONSE_FIELDS);
        this.cardLayout = CardLayout.valueOf(validString(FIELD_CARD_LAYOUT));
        this.cardAlign = CardAlign.valueOf(validString(FIELD_CARD_ALIGN));
        this.layoutColumn = validInteger(FIELD_LAYOUT_COLUMN);

    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput output = this.buildOutput(nodeExecution);
        RespPayload respPayload = RespPayload.builder()
                .layout(cardLayout)
                .align(cardAlign)
                .column(layoutColumn)
                .label(ni.getName())
                .icon(ni.getIcon())
                .rows(new ArrayList<>())
                .build();
        switch (cardLayout) {
            case TABLE:
                fillTableData(respPayload, nodeExecution);
                break;
            case PAGE:
                fillMarkdownData(respPayload, nodeExecution);
                break;
            case GRID:
                fillGrid(respPayload, nodeExecution);
                break;
            case CHAT:
                fillChatData(respPayload, nodeExecution);
                break;
            default:
        }
        output.addResult(BeanUtils.toMap(respPayload));
    }

    private void fillTableData(RespPayload respPayload, NodeExecution nodeExecution) {
        PageBody pageBody = new PageBody<>();
        pageBody.setSchemaFields(ni.getOutputFields());
        pageBody.setList(nodeExecution.flatInput());
        pageBody.setCurrent(1L);
        pageBody.setTotalPage(1L);
        pageBody.setTotal((long) pageBody.getList().size());
        ViewCard viewCard = ViewCard.builder()
                .viewId(nodeExecution.getNodeExecutionId())
                .viewType(CardViewType.table)
                .data(pageBody)
                .title(ni.getName())
                .icon(ni.getIcon())
                .build();
        respPayload.addRow(viewCard);
    }

    private void fillChatData(RespPayload respPayload, NodeExecution nodeExecution) {

    }

    private void fillMarkdownData(RespPayload respPayload, NodeExecution nodeExecution) {
        for (ExecutionInput input : nodeExecution.getInputs()) {
            for (Map<String, Object> item : input.getData()) {
                ViewCard vc = buildViewCard(item, CardViewType.markdown);
                respPayload.addRow(vc);
            }
        }
    }

    private void fillGrid(RespPayload respPayload, NodeExecution nodeExecution) {
        for (ExecutionInput input : nodeExecution.getInputs()) {
            if (CollectionUtils.isNotEmpty(input.getResources())) {
                for (ExecutionResource resource : input.getResources()) {
                    CardViewType viewType = CardViewType.CardMimeType.getType(resource.getMimeType());
                    if (viewType == null && resource.getResourceUri() != null) {
                        viewType = CardViewType.embed;
                    }
                    ViewCard viewCard = ViewCard.builder()
                            .viewId(resource.getResourceId())
                            .viewType(viewType)
                            .data(resource.toMetaInfo())
                            .title(resource.getResourceName())
                            .src(resource.getResourceUri())
                            .downloadUrl(resource.getResourceUri())
                            .previewUrl(resource.getResourceUri())
                            .data(resource.toMetaInfo())
                            .build();
                    respPayload.addRow(viewCard);
                }//end for resources
            } else {
                for (Map<String, Object> item : input.getData()) {
                    ViewCard vc = buildViewCard(item, null);
                    respPayload.addRow(vc);
                }
            }
        }
    }

    protected void end(NodeExecution nodeExecution, FlowExecution flowExecution) {
        for (ExecutionOutput nodeOutput : nodeExecution.getOutputs()) {
            flowExecution.addOutput(nodeOutput.getResults());
        }
    }

    private ViewCard buildViewCard(Map<String, Object> item, CardViewType viewType) {
        Map<String, Object> nItem = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(responseFields)) {
            for (ResponseField responseField : this.responseFields) {
                Object v = responseField.value(item);
                nItem.put(responseField.getFieldCode(), v);
            }
        } else {
            nItem = item;
        }
        ViewCard vc = BeanUtils.toBean(nItem, ViewCard.class);
        if (viewType == null && vc.getViewType() == null) {
            viewType = CardViewType.normal;
        }
        vc.setViewType(viewType);
        return vc;
    }

    private static List<ResponseField> buildResponseField(NodeInstance ni, String fieldName) {
        List<Map<String, Object>> values = ni.valueList(fieldName);
        List<ResponseField> fields = new ArrayList<>();
        if (values == null) {
            return fields;
        }
        for (Map<String, Object> value : values) {
            fields.add(JSONUtils.parseObject(value, ResponseField.class));
        }
        return fields;
    }
}
