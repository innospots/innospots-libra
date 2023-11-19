package io.innospots.workflow.console;


import io.innospots.workflow.core.instance.entity.EdgeInstanceEntity;
import io.innospots.workflow.core.instance.converter.EdgeInstanceConverter;
import io.innospots.workflow.core.instance.model.Edge;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


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


        EdgeInstanceEntity entity = EdgeInstanceConverter.INSTANCE.modelToEntity(edge);

        log.info("entity.data:{}", entity.getData());
    }


    @Test
    public void entityToModelTest() {
        EdgeInstanceEntity entity = new EdgeInstanceEntity();
        entity.setEdgeId(1L);
        entity.setData("{\"k1\":\"v1\"}");
        entity.setStartPoint("{\"k1\":\"v1\"}");

        Edge edge = EdgeInstanceConverter.INSTANCE.entityToModel(entity);

        log.info("edge:{}", edge);
        log.info("edge.startPoint:{}", edge.getStartPoint());
    }
}


