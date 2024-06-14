package io.innospots.workflow.core.definition.loader;

import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.node.definition.meta.NodeDefinitionBuilder;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfo;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfoLoader;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import org.junit.jupiter.api.Test;

import java.util.Collection;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/11
 */
class NodeMetaInfoLoaderTest {


    @Test
    void testLoad(){
        NodeMetaInfoLoader.load();
    }

    @Test
    void testBuild(){
        NodeMetaInfoLoader.load();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.build("SQL");
        String json = JSONUtils.toJsonString(nodeDefinition);
        System.out.println(json);
    }

    @Test
    void testList(){
        NodeMetaInfoLoader.load();
        Collection<NodeMetaInfo> infos = NodeMetaInfoLoader.nodeMetaInfos();
        for (NodeMetaInfo info : infos) {
            try{
                NodeDefinition nd = NodeDefinitionBuilder.build(info.getCode());
                System.out.println(nd.toString());
            }catch (Exception e){
                System.err.println(info);
                throw new RuntimeException(e);
            }

            //System.out.println(JSONUtils.toJsonString(nd));
        }
    }

}