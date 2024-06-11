package io.innospots.workflow.core.node.definition.loader;

import io.innospots.base.json.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
        try {
            Resource[] nodeMetaResources = new PathMatchingResourcePatternResolver()
                    .getResources(NODE_META_PATH);

            Resource[] nodeTplResources = new PathMatchingResourcePatternResolver()
                    .getResources(NODE_TEMPLATE_PATH);

            for (Resource nodeTplResource : nodeTplResources) {
                InputStream inputStream = nodeTplResource.getInputStream();
                log.info("loading node meta template: {}", nodeTplResource.getFilename());
                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                NodeMetaInfo tplMeta = JSONUtils.parseObject(content, NodeMetaInfo.class);
                if (tplMeta != null) {
                    tplMetaInfoMap.put(tplMeta.getCode(), tplMeta);
                    log.info("loaded node meta template: {}", tplMeta);
                }
            }//end for

            for (Resource nodeMetaResource : nodeMetaResources) {
                log.info("loading node meta: {}", nodeMetaResource.getFilename());
                InputStream inputStream = nodeMetaResource.getInputStream();
                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                NodeMetaInfo nodeMeta = JSONUtils.parseObject(content, NodeMetaInfo.class);
                if (nodeMeta != null) {
                    nodeMetaInfoMap.put(nodeMeta.getCode(), nodeMeta);
                    log.info("loaded node meta: {}", nodeMeta);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    public static NodeMetaInfo getNodeMetaInfo(String code) {
        return nodeMetaInfoMap.get(code);
    }

    public static NodeMetaInfo getNodeMetaInfoTemplate(String code) {
        return tplMetaInfoMap.get(code);
    }

    public static NodeMetaInfo getDefaultNodeMetaTemplate() {
        return tplMetaInfoMap.get("default");
    }
}
