/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.libra.kernel.module.logger.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.ResourceType;
import io.innospots.libra.kernel.module.logger.dao.SysOperateLogDao;
import io.innospots.libra.kernel.module.logger.entity.SysOperateLogEntity;
import io.innospots.libra.kernel.module.logger.converter.SysOperateLogMapper;
import io.innospots.libra.kernel.module.logger.model.LogFormQuery;
import io.innospots.libra.kernel.module.logger.model.SysOperateLog;
import io.innospots.libra.kernel.module.logger.model.UserLogInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenc
 * @date 2021/2/7 17:27
 */
@Slf4j
@Service
public class SysLogOperator extends ServiceImpl<SysOperateLogDao, SysOperateLogEntity> {

    /**
     * filter operate log data
     *
     * @return
     */
    public PageBody<SysOperateLog> pageLogs(LogFormQuery request) {
        QueryWrapper<SysOperateLogEntity> query = new QueryWrapper<>();
        LambdaQueryWrapper<SysOperateLogEntity> lambda = query.lambda();
        if (CollectionUtils.isNotEmpty(request.getUsernames())) {
            lambda.in(SysOperateLogEntity::getUsername, request.getUsernames());
        }
        if (CollectionUtils.isNotEmpty(request.getUserIds())) {
            lambda.in(SysOperateLogEntity::getUserId, request.getUserIds());
        }
        if (request.getModule() != null) {
            lambda.eq(SysOperateLogEntity::getModule, request.getModule());
        }
        if (request.getOperateType() != null) {
            lambda.eq(SysOperateLogEntity::getOperateType, request.getOperateType());
        }
        if (request.getResourceType() != null) {
            lambda.eq(SysOperateLogEntity::getResourceType, request.getResourceType());
        }
        if (request.getResourceId() != null) {
            lambda.eq(SysOperateLogEntity::getResourceId, request.getResourceId());
        }
        if (request.getFromTime() != null) {
            lambda.ge(SysOperateLogEntity::getOperateTime, request.getFromTime());
        }
        if (request.getEndTime() != null) {
            lambda.le(SysOperateLogEntity::getOperateTime, request.getEndTime());
        }
        if (StringUtils.isNotBlank(request.getSort())) {
            query.orderBy(true, request.getAsc(), CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getSort()));
        } else {
            query.orderBy(true, false, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "operateTime"));
        }
        PageBody<SysOperateLog> pageBody = new PageBody<>();
        IPage<SysOperateLogEntity> iPage = new Page<>(request.getPage(), request.getSize());
        IPage<SysOperateLogEntity> page = super.page(iPage, query);
        List<SysOperateLogEntity> entities = page.getRecords();
        List<SysOperateLog> operateLogs = entities.stream().map(SysOperateLogMapper.INSTANCE::modelToEntity).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
        pageBody.setList(operateLogs);
        pageBody.setCurrent(page.getCurrent());
        pageBody.setPageSize(page.getSize());
        pageBody.setTotal(page.getTotal());
        pageBody.setTotalPage(page.getPages());
        return pageBody;
    }

    public List<UserLogInfo> listNewestLogs() {
        QueryWrapper<SysOperateLogEntity> query = new QueryWrapper<>();
        query.lambda().orderByDesc(SysOperateLogEntity::getOperateTime);
        query.last("limit 10");

        List<SysOperateLogEntity> logEntities = this.list(query);
        List<UserLogInfo> userLogInfos = new ArrayList<>();
        for (SysOperateLogEntity logEntity : logEntities) {
            UserLogInfo logInfo = new UserLogInfo();
            logInfo.setOperateType(logEntity.getOperateType());
            logInfo.setDetail(logEntity.getDetail());
            logInfo.setTime(DateTimeUtils.formatLocalDateTime(logEntity.getOperateTime(), DateTimeUtils.DEFAULT_DATETIME_PATTERN));
            logInfo.setUserId(logEntity.getUserId());
            logInfo.setRealName(logEntity.getUsername());
            logInfo.setAvatar(logEntity.getUserAvatar());
        }
        return userLogInfos;
    }

    /**
     * get last SysOperateLogEntity by index
     *
     * @param index
     * @return
     */
    public SysOperateLogEntity getLastByIndex(int index) {
        QueryWrapper<SysOperateLogEntity> query = new QueryWrapper<>();
        query.lambda().orderByDesc(SysOperateLogEntity::getLogId);
        query.last("limit " + index + ", 1");
        List<SysOperateLogEntity> logEntities = this.list(query);
        if (logEntities != null && !logEntities.isEmpty()) {
            return logEntities.get(0);
        }
        return null;
    }

    public int deleteLogHis(Long logId, LocalDateTime operateTime) {
        int maxTimes = 50;
        int total = 0;
        int count = 0;
        int times = 0;
        do {
            count = this.baseMapper.deleteByLtLogId(logId, operateTime);
            total += count;
            times++;
            log.info("deleteByLtLogId logId:{} times: {} total:{} time:{}", logId, times, total, LocalDateTime.now());
            if (times > maxTimes) {
                break;
            }
        } while (count > 0);
        log.info("deleteByLtLogId end logId:{} times: {} total:{} time:{}", logId, times, total, LocalDateTime.now());
        return total;
    }


    /**
     * view log
     *
     * @param logId
     * @return
     */
    public SysOperateLog getLog(Integer logId) {
        SysOperateLogEntity log = this.getById(logId);
        if (log == null) {
            throw ResourceException.buildExistException(this.getClass(), "log does not exist");
        }
        return SysOperateLogMapper.INSTANCE.modelToEntity(log);
    }

    /**
     * get module
     *
     * @return
     */
    public List<String> getModule() {
        List<String> modules = new ArrayList<>();
        for (ResourceType resourceType : ResourceType.values()) {
            if (CollectionUtils.isEmpty(modules) || !modules.contains(resourceType.getModule())) {
                modules.add(resourceType.getModule());
            }
        }
        return modules;
    }

    /**
     * get operate
     *
     * @return
     */
    public List<String> getOperate() {
        List<String> operates = new ArrayList<>();
        for (OperateType operateType : OperateType.values()) {
            operates.add(operateType.name());
        }
        return operates;
    }

    /**
     * get resource
     *
     * @return
     */
    public List<String> getResource() {
        List<String> resources = new ArrayList<>();
        for (ResourceType resourceType : ResourceType.values()) {
            resources.add(resourceType.name());
        }
        return resources;
    }
}