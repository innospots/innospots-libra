package io.innospots.workflow.console.controller.node;

import io.innospots.base.model.response.R;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.console.service.NodeMetaService;
import io.innospots.workflow.core.node.NodeInfo;
import io.innospots.workflow.core.node.definition.converter.FlowNodeDefinitionConverter;
import io.innospots.workflow.core.node.definition.meta.NodeDefinitionBuilder;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfo;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfoLoader;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
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

    private final NodeMetaService nodeMetaService;

    public FlowNodeMetaController(NodeMetaService nodeMetaService) {
        this.nodeMetaService = nodeMetaService;
    }

    @GetMapping("list-meta")
    @Operation(description = "list node meta")
    public R<Collection<NodeMetaInfo>> listMetaInfo(){
        return R.success(NodeMetaInfoLoader.nodeMetaInfos());
    }

    @GetMapping("list-info")
    @Operation(description = "list node info")
    public R<List<NodeInfo>> listNodeInfo(){
        List<NodeMetaInfo> metas = new ArrayList<>(NodeMetaInfoLoader.nodeMetaInfos());
        return R.success(FlowNodeDefinitionConverter.INSTANCE.metaToInfoList(metas));
    }

    @GetMapping("list-definition")
    @Operation(description = "list node definition according node metas")
    public R<List<NodeDefinition>> listNodeDefinition(){
        List<NodeDefinition> definitions = new ArrayList<>();
        for (NodeMetaInfo nodeMetaInfo : NodeMetaInfoLoader.nodeMetaInfos()) {
            definitions.add(NodeDefinitionBuilder.build(nodeMetaInfo.getCode()));
        }
        return R.success(definitions);
    }

    @GetMapping("definition/{code}")
    @Operation(description = "node definition by code")
    public R<NodeDefinition> getNodeDefinitionByCode(
            @PathVariable String code){
        return R.success(NodeDefinitionBuilder.build(code));
    }

    @GetMapping("reload")
    @Operation(description = "reload node meta info")
    public R<List<NodeInfo>> reload(){
        NodeMetaInfoLoader.load();
        List<NodeMetaInfo> metas = new ArrayList<>(NodeMetaInfoLoader.nodeMetaInfos());
        return R.success(FlowNodeDefinitionConverter.INSTANCE.metaToInfoList(metas));
    }

    @PutMapping("synchronize/{force}")
    @Operation(description = "synchronize node meta info")
    public R<List<NodeDefinition>> synchronize(@PathVariable boolean force){
        return R.success(nodeMetaService.synchronizeNodeMeta(force));
    }
}
