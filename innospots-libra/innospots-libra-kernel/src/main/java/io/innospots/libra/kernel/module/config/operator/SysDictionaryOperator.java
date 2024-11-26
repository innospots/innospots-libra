package io.innospots.libra.kernel.module.config.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.libra.kernel.module.config.converter.SysDictionaryConverter;
import io.innospots.libra.kernel.module.config.dao.SysDictionaryDao;
import io.innospots.libra.kernel.module.config.entity.SysDictionaryEntity;
import io.innospots.libra.kernel.module.config.model.SysDictionary;
import io.innospots.libra.kernel.module.config.model.SysDictionaryType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/26
 */
@Component
public class SysDictionaryOperator extends ServiceImpl<SysDictionaryDao, SysDictionaryEntity> {


    public List<SysDictionary> list(String type){
        QueryWrapper<SysDictionaryEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(SysDictionaryEntity::getType, type);
        List<SysDictionaryEntity> entities = this.list(qw);
        return SysDictionaryConverter.INSTANCE.entitiesToModels(entities);
    }

    public List<SysDictionaryType> listTypes(){
        QueryWrapper<SysDictionaryEntity> qw = new QueryWrapper<>();
        qw.select("DISTINCT `type`","typeName");
        List<SysDictionaryEntity> entities = this.list(qw);
        return SysDictionaryConverter.INSTANCE.entitiesToTypes(entities);
    }

    @Transactional
    public boolean save(List<SysDictionary> sysDictionaries){
        List<SysDictionaryEntity> entities = SysDictionaryConverter.INSTANCE.modelsToEntities(sysDictionaries);
        return this.saveOrUpdateBatch(entities);
    }

    public boolean save(SysDictionary sysDictionary){
        return this.saveOrUpdate(SysDictionaryConverter.INSTANCE.modelToEntity(sysDictionary));
    }

    public boolean delete(String type){
        QueryWrapper<SysDictionaryEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(SysDictionaryEntity::getType, type);
        return this.remove(qw);
    }

    public boolean delete(String type, String code){
        QueryWrapper<SysDictionaryEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(SysDictionaryEntity::getType, type).eq(SysDictionaryEntity::getCode, code);
        return this.remove(qw);
    }

}
