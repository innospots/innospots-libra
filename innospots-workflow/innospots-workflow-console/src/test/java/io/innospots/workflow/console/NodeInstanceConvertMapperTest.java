package io.innospots.workflow.console;


import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.instance.converter.NodeInstanceConverter;
import io.innospots.workflow.core.instance.entity.NodeInstanceEntity;
import io.innospots.workflow.core.instance.model.NodeInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class NodeInstanceConvertMapperTest {

    @Test
    public void modelToEntityTest() {
        NodeInstance instance = new NodeInstance();
        instance.setNodeInstanceId(1L);
        instance.setNodeDefinitionId(1);
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("k1", "v1");
        instance.setData(mapData);
        ParamField paramField = new ParamField();
        paramField.setCode("code");
        List<ParamField> list = new ArrayList<>();
        list.add(paramField);
//        instance.setInputFields(list);

        List<Map<String, Object>> ports = new ArrayList<>();
        Map<String, Object> port = new HashMap<>();
        port.put("id", "1");
        port.put("group", "in");
        Map<String, Object> portData = new HashMap<>();
        portData.put("a", "a");
        port.put("data", portData);
        ports.add(port);
        instance.setPorts(ports);

        NodeInstanceEntity entity = NodeInstanceConverter.INSTANCE.modelToEntity(instance);

        log.info("entity.data:{}", entity.getData());
        log.info("entity.inputFields:{}", entity.getInputFields());

        log.info("instance:{}", instance.getPorts());
        NodeInstance old = NodeInstanceConverter.INSTANCE.entityToModel(entity);
        log.info("old instance:{}", old.getPorts());
    }


}


