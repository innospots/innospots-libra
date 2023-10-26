package io.innospots.workflow.console;


import io.innospots.workflow.console.entity.instance.EdgeInstanceEntity;
import io.innospots.workflow.console.mapper.instance.EdgeInstanceBeanConverter;
import io.innospots.workflow.core.node.instance.Edge;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EdgeInstanceConvertMapperTest {

    @Test
    public void modelToEntityTest() {
        Edge edge = new Edge();
        edge.setEdgeId(1L);
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("k1", "v1");
        edge.setData(mapData);


        EdgeInstanceEntity entity = EdgeInstanceBeanConverter.INSTANCE.modelToEntity(edge);

        log.info("entity.data:{}", entity.getData());
    }


    @Test
    public void entityToModelTest() {
        EdgeInstanceEntity entity = new EdgeInstanceEntity();
        entity.setEdgeId(1L);
        entity.setData("{\"k1\":\"v1\"}");
        entity.setStartPoint("{\"k1\":\"v1\"}");

        Edge edge = EdgeInstanceBeanConverter.INSTANCE.entityToModel(entity);

        log.info("edge:{}", edge);
        log.info("edge.startPoint:{}", edge.getStartPoint());
    }
}


