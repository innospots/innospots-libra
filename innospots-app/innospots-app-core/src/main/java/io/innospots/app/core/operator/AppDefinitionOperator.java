package io.innospots.app.core.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.app.core.converter.AppDefinitionConverter;
import io.innospots.app.core.dao.AppDefinitionDao;
import io.innospots.app.core.entity.AppDefinitionEntity;
import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.core.model.form.AppQueryRequest;
import io.innospots.app.core.model.form.CreateAppFrom;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.utils.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/17
 */
public class AppDefinitionOperator extends ServiceImpl<AppDefinitionDao, AppDefinitionEntity> {


    public AppDefinition getAppDefinition(String appKey) {
        AppDefinitionEntity entity = this.getById(appKey);
        if (entity == null) {
            return null;
        }
        return AppDefinitionConverter.INSTANCE.entityToModel(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppDefinition createAppDefinition(CreateAppFrom createAppFrom) {

        if (this.checkExist(createAppFrom.getName(), null)) {
            throw ResourceException.buildExistException(this.getClass(), createAppFrom.getName());
        }
        long codeCount = 0;
        AppDefinition appDefinition = new AppDefinition();
        appDefinition.setName(createAppFrom.getName());
        appDefinition.setCategoryId(createAppFrom.getCategoryId());
        appDefinition.setIcon(createAppFrom.getIcon());
        appDefinition.setAppType(createAppFrom.getAppType());

        do {
            String key = StringConverter.randomKey(8);
            appDefinition.setAppKey(key);
            QueryWrapper<AppDefinitionEntity> qw = new QueryWrapper<>();
            qw.lambda().eq(AppDefinitionEntity::getAppKey, key);
            codeCount = this.count(qw);
        } while (codeCount > 0);
        AppDefinitionEntity entity = AppDefinitionConverter.INSTANCE.modelToEntity(appDefinition);
        super.save(entity);
        return this.getAppDefinition(entity.getAppKey());
    }

    @Transactional(rollbackFor = Exception.class)
    public AppDefinition updateAppDefinition(AppDefinition appDefinition) {
        if (this.checkExist(appDefinition.getName(), appDefinition.getAppKey())) {
            throw ResourceException.buildExistException(this.getClass(), appDefinition.getName());
        }
        AppDefinitionEntity entity = AppDefinitionConverter.INSTANCE.modelToEntity(appDefinition);
        super.updateById(entity);
        return this.getAppDefinition(entity.getAppKey());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(String appKey, DataStatus status) {
        AppDefinitionEntity entity = super.getById(appKey);
        if (entity == null) {
            throw ResourceException.buildNotExistException(this.getClass(), appKey);
        }
        entity.setStatus(status.name());
        super.updateById(entity);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAppDefinition(String appKey) {
        return super.removeById(appKey);
    }

    public PageBody<AppDefinition> pageAppInfos(AppQueryRequest queryRequest) {
        QueryWrapper<AppDefinitionEntity> query = new QueryWrapper<>();
        query.lambda().orderByDesc(AppDefinitionEntity::getCreatedTime);

        if (queryRequest.getCategoryId() != null) {
            query.lambda().eq(AppDefinitionEntity::getCategoryId,queryRequest.getCategoryId());
        }

        if (queryRequest.getDataStatus() != null) {
            query.lambda().eq(AppDefinitionEntity::getStatus,queryRequest.getDataStatus());
        }

        if (StringUtils.isNotBlank(queryRequest.getQueryInput())) {
            query.lambda().eq(AppDefinitionEntity::getName,queryRequest.getQueryInput());
        }

        IPage<AppDefinitionEntity> entityPage = super.page(PageDTO.of(queryRequest.getPage(), queryRequest.getSize()), query);

        PageBody<AppDefinition> pageBody = new PageBody<>();
        pageBody.setList(AppDefinitionConverter.INSTANCE.entitiesToModels(entityPage.getRecords()));
        pageBody.setPageSize(entityPage.getSize());
        pageBody.setCurrent(entityPage.getCurrent());
        pageBody.setTotal(entityPage.getTotal());
        return pageBody;

    }

    public boolean checkExist(String name, String appKey) {
        QueryWrapper<AppDefinitionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(AppDefinitionEntity::getName, name);
        if (appKey != null) {
            qw.lambda().ne(AppDefinitionEntity::getAppKey, appKey);
        }
        return super.count(qw) > 0;
    }
}
