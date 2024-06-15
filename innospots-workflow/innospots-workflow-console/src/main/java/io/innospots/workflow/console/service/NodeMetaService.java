package io.innospots.workflow.console.service;

import io.innospots.workflow.console.operator.node.FlowNodeDefinitionOperator;
import io.innospots.workflow.core.node.definition.meta.NodeDefinitionBuilder;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfo;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfoLoader;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/15
 */
@Slf4j
@Service
public class NodeMetaService {

    private final FlowNodeDefinitionOperator flowNodeDefinitionOperator;

    public NodeMetaService(FlowNodeDefinitionOperator flowNodeDefinitionOperator) {
        this.flowNodeDefinitionOperator = flowNodeDefinitionOperator;
    }

    public List<NodeDefinition> synchronizeNodeMeta(boolean force) {
        List<NodeDefinition> nd = new ArrayList<>();
        Collection<NodeMetaInfo> nodeMetaInfos = NodeMetaInfoLoader.nodeMetaInfos();
        List<String> codes = nodeMetaInfos.stream().map(NodeMetaInfo::getCode).collect(Collectors.toList());
        List<NodeDefinition> nodeDefinitions = flowNodeDefinitionOperator.listByCodes(codes);
        Map<String, NodeMetaInfo> metaInfoMap = nodeMetaInfos.stream().collect(Collectors.toMap(NodeMetaInfo::getCode, v -> v));
        if (CollectionUtils.isNotEmpty(nodeDefinitions)) {
            for (NodeDefinition oldNodeDefinition : nodeDefinitions) {
                if (force) {
                    NodeMetaInfo nodeMetaInfo = metaInfoMap.get(oldNodeDefinition.getCode());
                    NodeDefinition nodeDefinition = NodeDefinitionBuilder.build(nodeMetaInfo);
                    nodeDefinition.setStatus(oldNodeDefinition.getStatus());
                    nodeDefinition.setUsed(oldNodeDefinition.getUsed());
                    nodeDefinition.setCredentialTypeCode(oldNodeDefinition.getCredentialTypeCode());
                    nodeDefinition.setNodeGroupId(oldNodeDefinition.getNodeGroupId());
                    nodeDefinition.setNodeId(oldNodeDefinition.getNodeId());
                    log.info("update node definition from node meta: {}", nodeDefinition);
                    nodeDefinition = flowNodeDefinitionOperator.updateAllNodeDefinition(nodeDefinition);
                    nd.add(nodeDefinition);
                }
                metaInfoMap.remove(oldNodeDefinition.getCode());
            }//end for
        }//end if

        for (NodeMetaInfo value : metaInfoMap.values()) {
            NodeDefinition nodeDefinition = NodeDefinitionBuilder.build(value);
            log.info("create node definition from node meta: {}", nodeDefinition);
            nodeDefinition = flowNodeDefinitionOperator.createNodeDefinition(nodeDefinition);
            nd.add(nodeDefinition);
        }
        log.info("synchronize node definition: {}", nd.size());
        return nd;
    }

}
