package io.innospots.workflow.core.node.definition.meta;

import io.innospots.base.condition.Factor;
import io.innospots.base.enums.DataStatus;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import io.innospots.workflow.core.node.definition.model.NodePage;
import io.innospots.workflow.core.node.definition.model.NodeResource;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/11
 */
public class NodeDefinitionBuilder {


    public static NodeDefinition build(String code) {
        NodeDefinition nodeDefinition = new NodeDefinition();
        NodeMetaInfo nodeMetaInfo = NodeMetaInfoLoader.getNodeMetaInfo(code);
        if (nodeMetaInfo == null) {
            return null;
        }
        Map<String, Object> settings = defaultSettings();
        if (nodeMetaInfo.getSettings() != null) {
            settings.putAll(nodeMetaInfo.getSettings());
        }
        nodeDefinition.setSettings(settings);

        fillInfo(nodeMetaInfo, nodeDefinition);
        rebuildComp(nodeMetaInfo, nodeDefinition);
        return nodeDefinition;
    }

    private static void rebuildComp(NodeMetaInfo nodeMetaInfo, NodeDefinition nodeDefinition) {
        NodeResource nodeResource = new NodeResource();
        fillPorts(nodeResource, nodeMetaInfo);
        List<NodePage> nodePages = new ArrayList<>();
        NodePage nodePage = new NodePage();
        nodePage.setPageKey("pg_" + RandomStringUtils.randomAlphabetic(6));
        nodePages.add(nodePage);
        nodeResource.setPages(nodePages);
        Map<String, NodeComponent> componentMap = new LinkedHashMap<>();
        Map<String, Map<String, Object>> tartgetIdMap = new LinkedHashMap<>();
        Map<String, String> nameToIdMap = new LinkedHashMap<>();
        for (int i = 0; i < nodeMetaInfo.getComponents().size(); i++) {
            NodeComponent nodeComponent = nodeMetaInfo.getComponents().get(i);
            componentMap.put(nodeComponent.getName(), nodeComponent);
            Map<String, Object> compMap = nodeComponent.toMap(nodeMetaInfo.getCode(), i + 1);
            String id = (String) compMap.get("id");
            nameToIdMap.put(nodeComponent.getName(), id);
            if (!compMap.containsKey("layout")) {
                Map<String, Object> defaultLayout = defaultLayout(id,i);
                compMap.put("layout", defaultLayout);
            }
            tartgetIdMap.put(id, compMap);
        }

        nameToIdMap.forEach((name, id) -> {
            NodeComponent nodeComponent = componentMap.get(name);
            Map<String, Object> compMap = tartgetIdMap.get(id);
            fillConditions(compMap, nodeComponent, nameToIdMap);
        });
        nodePage.setComponents(tartgetIdMap);
        nodeDefinition.setResources(nodeResource);
    }

    private static void fillConditions(Map<String, Object> compMap, NodeComponent nodeComponent, Map<String, String> nameToIdMap) {
        if (nodeComponent.getConditions() != null) {
            Map<String, List<Map<String,Object>>> conditionListMap = new LinkedHashMap<>();
            for (CompCondition condition : nodeComponent.getConditions()) {
                Map<String, Object> conditionMap = new LinkedHashMap<>();
                conditionMap.put("result", condition.getResult());
                conditionMap.put("relation", condition.getRelation());
                if (condition.getChildren() != null) {
                    List<Map<String, Object>> children = new ArrayList<>();
                    for (Factor factor : condition.getChildren()) {
                        Map<String, Object> factorMap = new LinkedHashMap<>();
                        Map<String,Object> source = new LinkedHashMap<>();
                        source.put("value",nameToIdMap.get(factor.getName()));
                        source.put("label",nodeComponent.getName() + "-" + nodeComponent.getSettings().get("label"));
                        factorMap.put("source", source);
                        Map<String,Object> opt = new LinkedHashMap<>();
                        opt.put("label",factor.getOpt().descZh());
                        opt.put("value",factor.getOpt());
                        factorMap.put("opt", opt);
                        if (factor.getValue() != null) {
                            factorMap.put("value", factor.getValue());
                        }
                        children.add(factorMap);
                    }//end for factor
                    conditionMap.put("children", children);
                }//end if children
                List<Map<String,Object>> condList = conditionListMap.computeIfAbsent(condition.getResult(), k -> new ArrayList<>());
                condList.add(conditionMap);
            }//end for condition
            compMap.put("conditions", conditionListMap);
        }
    }


    private static void fillPorts(NodeResource nodeResource, NodeMetaInfo metaInfo) {
        if (metaInfo.getInPorts() instanceof List) {
            nodeResource.setInPorts((List<Map<String, Object>>) metaInfo.getInPorts());
        } else if (metaInfo.getInPorts() instanceof Number ||
                metaInfo.getInPorts() instanceof String) {
            List<Map<String, Object>> inPorts = new ArrayList<>();
            Integer inNum = Integer.parseInt(metaInfo.getInPorts().toString());
            for (int i = 0; i < inNum; i++) {
                Map<String, Object> port = new LinkedHashMap<>();
                port.put("label", String.join("_", metaInfo.getCode().toLowerCase(), "in", String.valueOf(i + 1)));
                port.put("count", 1);
                inPorts.add(port);
            }
            nodeResource.setInPorts(inPorts);
        }

        if (metaInfo.getOutPorts() instanceof List) {
            nodeResource.setOutPorts((List<Map<String, Object>>) metaInfo.getOutPorts());
        } else if (metaInfo.getOutPorts() instanceof Number ||
                metaInfo.getOutPorts() instanceof String) {
            List<Map<String, Object>> outPorts = new ArrayList<>();
            Integer outNum = Integer.parseInt(metaInfo.getOutPorts().toString());
            for (int i = 0; i < outNum; i++) {
                Map<String, Object> port = new LinkedHashMap<>();
                port.put("label", String.join("_", metaInfo.getCode().toLowerCase(), "out", String.valueOf(i + 1)));
                port.put("count", 1);
                outPorts.add(port);
            }
            nodeResource.setOutPorts(outPorts);
        }
    }

    private static void fillInfo(NodeMetaInfo metaInfo, NodeDefinition nodeDefinition) {
        if(metaInfo.getScripts()!=null){
            nodeDefinition.setScripts(metaInfo.getScripts());
        }else{
            nodeDefinition.setScripts(new LinkedHashMap<>());
        }
        nodeDefinition.setStatus(DataStatus.OFFLINE);
        nodeDefinition.setName(metaInfo.getName());
        nodeDefinition.setCode(metaInfo.getCode());
        nodeDefinition.setIcon(metaInfo.getIcon());
        nodeDefinition.setCredentialTypeCode(metaInfo.getCredentialTypeCode());
        nodeDefinition.setVendor(metaInfo.getVendor());
        nodeDefinition.setPrimitive(metaInfo.getPrimitive());
        nodeDefinition.setNodeType(metaInfo.getNodeType());
        nodeDefinition.setDescription(metaInfo.getDescription());
        nodeDefinition.setUsed(false);
        nodeDefinition.setDeletable(!"innospots".equals(metaInfo.getVendor()));
    }

    private static Map<String, Object> defaultSettings() {
        NodeMetaInfo defaultNodeMetaInfo = NodeMetaInfoLoader.getDefaultNodeMetaTemplate();
        return new LinkedHashMap<>(defaultNodeMetaInfo.getSettings());
    }

    private static Map<String, Object> defaultLayout(String id,int index_y) {
        NodeMetaInfo defaultNodeMetaInfo = NodeMetaInfoLoader.getDefaultNodeMetaTemplate();
        Map<String, Object> m = new LinkedHashMap<>(defaultNodeMetaInfo.getComponents().get(0).getLayout());
        m.put("i", id);
        m.put("y",index_y);
        return m;
    }
}
