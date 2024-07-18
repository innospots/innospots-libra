package io.innospots.workflow.core.node.definition.meta;

import io.innospots.base.json.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/11
 */
@Slf4j
public class NodeMetaInfoLoader {

    private static final String NODE_META_PATH = "classpath*:NODE-META/**/*.json";

    private static final String NODE_TEMPLATE_PATH = "classpath*:NODE-TEMPLATE/*.json";

    private static Map<String, NodeMetaInfo> nodeMetaInfoMap = new HashMap<>();

    private static Map<String, NodeMetaInfo> tplMetaInfoMap = new HashMap<>();

    public static void load() {
        Map<String,NodeMetaInfo> tmpMetaInfoMap = new HashMap<>();
        Map<String,NodeMetaInfo> tmpTplMetaInfoMap = new HashMap<>();
        try {

            Resource[] nodeMetaResources = new PathMatchingResourcePatternResolver()
                    .getResources(NODE_META_PATH);

            Resource[] nodeTplResources = new PathMatchingResourcePatternResolver()
                    .getResources(NODE_TEMPLATE_PATH);

            for (Resource nodeTplResource : nodeTplResources) {
                InputStream inputStream = nodeTplResource.getInputStream();
                log.debug("loading node meta template: {}", nodeTplResource.getFilename());
                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                NodeMetaInfo tplMeta = JSONUtils.parseObject(content, NodeMetaInfo.class);
                if (tplMeta != null) {
                    tplMeta.setMetaFile(nodeTplResource.getFilename());
                    tmpTplMetaInfoMap.put(tplMeta.getCode(), tplMeta);
                    log.debug("loaded node meta template: {}", tplMeta);
                }
            }//end for

            for (Resource nodeMetaResource : nodeMetaResources) {
                log.debug("loading node meta: {}", nodeMetaResource.getFilename());
                InputStream inputStream = nodeMetaResource.getInputStream();
                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                NodeMetaInfo nodeMeta = JSONUtils.parseObject(content, NodeMetaInfo.class);
                if (nodeMeta != null) {
                    nodeMeta.setMetaFile(nodeMetaResource.getFilename());
                    tmpMetaInfoMap.put(nodeMeta.getCode(), nodeMeta);
                    log.debug("loaded node meta: {}", nodeMeta);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        log.info("node meta loaded, size:{}",tmpMetaInfoMap.size());
        log.info("node template meta loaded, size:{}",tmpTplMetaInfoMap.size());
        nodeMetaInfoMap = tmpMetaInfoMap;
        tplMetaInfoMap = tmpTplMetaInfoMap;
    }

    public static NodeMetaInfo getNodeMetaInfo(String code) {
        return nodeMetaInfoMap.get(code);
    }

    public static NodeMetaInfo getNodeMetaInfoTemplate(String code) {
        return tplMetaInfoMap.get(code);
    }

    public static Collection<NodeMetaInfo> nodeMetaInfos(){
        return nodeMetaInfoMap.values();
    }

    public static NodeMetaInfo getDefaultNodeMetaTemplate() {
        return tplMetaInfoMap.get("default");
    }

    public static NodeMetaInfo getNormalNodeMetaTemplate() {
        return tplMetaInfoMap.get("normal");
    }

    public static NodeMetaInfo getNodeMetaTemplate(String primitive){
        return tplMetaInfoMap.get(primitive);
    }
}
