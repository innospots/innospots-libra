package io.innospots.libra.kernel.module.config.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.libra.kernel.module.config.converter.SysDictionaryConverter;
import io.innospots.libra.kernel.module.config.dao.SysDictionaryDao;
import io.innospots.libra.kernel.module.config.entity.SysDictionaryEntity;
import io.innospots.libra.kernel.module.config.model.SysDictTypeGroup;
import io.innospots.libra.kernel.module.config.model.SysDictionary;
import io.innospots.libra.kernel.module.config.model.SysDictionaryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/26
 */
@Slf4j
@Component
public class SysDictionaryOperator extends ServiceImpl<SysDictionaryDao, SysDictionaryEntity> {


    public List<SysDictionary> list(String type) {
        QueryWrapper<SysDictionaryEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(SysDictionaryEntity::getType, type);
        List<SysDictionaryEntity> entities = this.list(qw);
        return SysDictionaryConverter.INSTANCE.entitiesToModels(entities);
    }

    public List<SysDictionaryType> listTypes() {
        QueryWrapper<SysDictionaryEntity> qw = new QueryWrapper<>();
        qw.select("DISTINCT `type`", "type_name");
        List<SysDictionaryEntity> entities = this.list(qw);
        return SysDictionaryConverter.INSTANCE.entitiesToTypes(entities);
    }

    @Transactional
    public List<SysDictionary> save(SysDictTypeGroup sysDictTypeGroup) {
        List<SysDictionaryEntity> entities = SysDictionaryConverter.INSTANCE.modelsToEntities(sysDictTypeGroup.getDictionaries());
        List<SysDictionary> dbList = list(sysDictTypeGroup.getType());
        Map<String, SysDictionary> dbMap = dbList.stream().collect(Collectors.toMap(SysDictionary::getValue, item -> item));
        List<String> removeList = new ArrayList<>();
        for (SysDictionaryEntity entity : entities) {
            entity.setType(sysDictTypeGroup.getType());
            entity.setTypeName(sysDictTypeGroup.getTypeName());
            entity.setStatus(true);
            SysDictionary dict = dbMap.remove(entity.getValue());
            if (dict != null) {
                entity.setDictId(dict.getDictId());
            }
        }
        for (Map.Entry<String, SysDictionary> entry : dbMap.entrySet()) {
            removeList.add(entry.getValue().getDictId());
        }
        if (!removeList.isEmpty()) {
            log.info("remove dictionaryIds:{}",removeList);
            this.removeBatchByIds(removeList);
        }
        this.saveOrUpdateBatch(entities);
        return this.list(sysDictTypeGroup.getType());
    }


    public boolean delete(String type) {
        QueryWrapper<SysDictionaryEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(SysDictionaryEntity::getType, type);
        return this.remove(qw);
    }

    public boolean delete(String type, String value) {
        QueryWrapper<SysDictionaryEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(SysDictionaryEntity::getType, type).eq(SysDictionaryEntity::getValue, value);
        return this.remove(qw);
    }

}
