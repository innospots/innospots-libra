package io.innospots.workflow.console.controller.node;

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.core.node.NodeInfo;
import io.innospots.workflow.core.node.definition.converter.FlowNodeDefinitionConverter;
import io.innospots.workflow.core.node.definition.meta.NodeDefinitionBuilder;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfo;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfoLoader;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "flow-node/meta")
@ModuleMenu(menuKey = "node-management")
@Tag(name = "Node meta info")
public class FlowNodeMetaController {

    @GetMapping("list-meta")
    @Operation(summary = "list node meta")
    public InnospotsResponse<Collection<NodeMetaInfo>> listMetaInfo(){
        return InnospotsResponse.success(NodeMetaInfoLoader.nodeMetaInfos());
    }

    @GetMapping("list-info")
    @Operation(summary = "list node info")
    public InnospotsResponse<List<NodeInfo>> listNodeInfo(){
        List<NodeMetaInfo> metas = new ArrayList<>(NodeMetaInfoLoader.nodeMetaInfos());
        return InnospotsResponse.success(FlowNodeDefinitionConverter.INSTANCE.metaToInfoList(metas));
    }

    @GetMapping("list-definition")
    @Operation(summary = "list node definition according node metas")
    public InnospotsResponse<List<NodeDefinition>> listNodeDefinition(){
        List<NodeDefinition> definitions = new ArrayList<>();
        for (NodeMetaInfo nodeMetaInfo : NodeMetaInfoLoader.nodeMetaInfos()) {
            definitions.add(NodeDefinitionBuilder.build(nodeMetaInfo.getCode()));
        }
        return InnospotsResponse.success(definitions);
    }

    @GetMapping("definition/{code}")
    @Operation(summary = "node definition by code")
    public InnospotsResponse<NodeDefinition> getNodeDefinitionByCode(
            @PathVariable String code){
        return InnospotsResponse.success(NodeDefinitionBuilder.build(code));
    }

    @GetMapping("reload")
    @Operation(summary = "reload node meta info")
    public InnospotsResponse<List<NodeInfo>> reload(){
        NodeMetaInfoLoader.load();
        List<NodeMetaInfo> metas = new ArrayList<>(NodeMetaInfoLoader.nodeMetaInfos());
        return InnospotsResponse.success(FlowNodeDefinitionConverter.INSTANCE.metaToInfoList(metas));
    }
}
