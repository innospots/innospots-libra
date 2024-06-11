package io.innospots.workflow.core.definition.loader;

import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.node.definition.loader.NodeDefinitionBuilder;
import io.innospots.workflow.core.node.definition.loader.NodeMetaInfoLoader;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import org.junit.jupiter.api.Test;

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

}