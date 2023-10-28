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

package io.innospots.libra.security.logger.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.utils.CCH;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.libra.base.enums.LoginStatus;
import io.innospots.libra.base.terminal.TerminalInfo;
import io.innospots.libra.base.terminal.TerminalRequestContextHolder;
import io.innospots.libra.security.logger.dao.LoginLogDao;
import io.innospots.libra.security.logger.entity.LoginLogEntity;
import io.innospots.libra.security.logger.mapper.LoginLogMapper;
import io.innospots.libra.security.logger.model.LogFormQuery;
import io.innospots.libra.security.logger.model.LoginLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/12/13
 */
@Slf4j
@Service
public class LoginLogOperator extends ServiceImpl<LoginLogDao, LoginLogEntity> {

    private static final Logger logger = LoggerFactory.getLogger(LoginLogOperator.class);

    /**
     * Filter log data
     *
     * @return
     */
    public PageBody<LoginLog> pageLogs(LogFormQuery request) {
        QueryWrapper<LoginLogEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<LoginLogEntity> lambda = queryWrapper.lambda();
        if (CollectionUtils.isNotEmpty(request.getUsernames())) {
            lambda.in(LoginLogEntity::getUserName, request.getUsernames());
        }
        if (CollectionUtils.isNotEmpty(request.getUserIds())) {
            lambda.in(LoginLogEntity::getUserId, request.getUserIds());
        }
        if (StringUtils.isNotEmpty(request.getFromTime())) {
            lambda.ge(LoginLogEntity::getLoginTime, request.getFromTime());
        }
        if (StringUtils.isNotEmpty(request.getEndTime())) {
            lambda.le(LoginLogEntity::getLoginTime, request.getEndTime());
        }
        if (StringUtils.isNotEmpty(request.getBrowser())) {
            lambda.likeRight(LoginLogEntity::getBrowser, request.getBrowser());
        }
        if (StringUtils.isNotEmpty(request.getOs())) {
            lambda.likeRight(LoginLogEntity::getOs, request.getOs());
        }
        if (StringUtils.isNotEmpty(request.getStatus())) {
            lambda.eq(LoginLogEntity::getStatus, request.getStatus());
        }
        if (StringUtils.isNotEmpty(request.getSort())) {
            queryWrapper.orderBy(true, request.getAsc(),
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getSort()));
        } else {
            queryWrapper.orderBy(true, false,
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "loginTime"));
        }

        PageBody<LoginLog> pageBody = new PageBody<>();
        IPage<LoginLogEntity> oPage = new Page<>(request.getPage(), request.getSize());
        IPage<LoginLogEntity> entityPage = super.page(oPage, queryWrapper);
        List<LoginLogEntity> entities = entityPage.getRecords();
        List<LoginLog> loginLogs = entities.stream().map(LoginLogMapper.INSTANCE::entity2Model).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
        pageBody.setCurrent(entityPage.getCurrent());
        pageBody.setPageSize(entityPage.getSize());
        pageBody.setTotal(entityPage.getTotal());
        pageBody.setTotalPage(entityPage.getPages());
        pageBody.setList(loginLogs);
        return pageBody;
    }

    public PageBody<LoginLog> pageLogs(Integer userId, Integer page, Integer size) {
        QueryWrapper<LoginLogEntity> query = new QueryWrapper<>();
        if (userId != null) {
            query.lambda().eq(LoginLogEntity::getUserId, userId);
        }
        query.lambda().orderByDesc(LoginLogEntity::getLoginTime);
        IPage<LoginLogEntity> oPage = new Page<>(page, size);
        IPage<LoginLogEntity> entityPage = super.page(oPage, query);
        PageBody<LoginLog> logPage = new PageBody<>();
        logPage.setCurrent(entityPage.getCurrent());
        logPage.setPageSize(entityPage.getSize());
        logPage.setTotalPage(entityPage.getPages());
        logPage.setTotal(entityPage.getTotal());
        logPage.setList(entityPage.getRecords().stream().map(LoginLogMapper.INSTANCE::entity2Model).collect(Collectors.toList()));
        return logPage;
    }

    /**
     * view log
     *
     * @param logId
     * @return
     */
    public LoginLog getLog(Integer logId) {
        LoginLogEntity log = this.getById(logId);
        if (log == null) {
            throw ResourceException.buildExistException(this.getClass(), "log does not exist");
        }
        return LoginLogMapper.INSTANCE.entity2Model(log);
    }

    public LoginLog getLatest() {
        QueryWrapper<LoginLogEntity> query = new QueryWrapper<>();
        query.lambda().eq(LoginLogEntity::getStatus, LoginStatus.SUCCESS);
        query.lambda().eq(LoginLogEntity::getUserId, CCH.userId());
        query.lambda().orderByDesc(LoginLogEntity::getLogId);
        query.last("limit 2");
        List<LoginLogEntity> entities = super.list(query);
        LoginLog loginLog = new LoginLog();
        if (CollectionUtils.isEmpty(entities)) {
            loginLog.setUserName(CCH.authUser());
            loginLog.setLoginTime(DateTimeUtils.formatDate(new Date(), null));
            TerminalInfo terminalInfo = TerminalRequestContextHolder.getTerminal();
            if (terminalInfo != null) {
                loginLog.setProvince(terminalInfo.getProvince());
                loginLog.setCity(terminalInfo.getCity());
            }
            return loginLog;
        }
        loginLog = LoginLogMapper.INSTANCE.entity2Model(entities.get(0));
        if (entities.size() == 2) {
            loginLog.setRecentProvince(entities.get(1).getProvince());
            loginLog.setRecentCity(entities.get(1).getCity());
        }
        return loginLog;
    }

    public List<LoginLog> listNewestLogs() {
        QueryWrapper<LoginLogEntity> query = new QueryWrapper<>();
        query.lambda().orderByDesc(LoginLogEntity::getLoginTime);
        query.last("limit 10");
        List<LoginLogEntity> entities = super.list(query);
        return entities.stream().map(LoginLogMapper.INSTANCE::entity2Model).collect(Collectors.toList());
    }


    /**
     * get browser
     *
     * @return
     */
    public List<String> listBrowsers() {
        List<Map<String, Object>> groupList = super.listMaps(
                new QueryWrapper<LoginLogEntity>()
                        .select("BROWSER, COUNT(1) CNT ")
                        .groupBy("BROWSER"));
        List<String> browsers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupList)) {
            for (Map<String, Object> map : groupList) {
                browsers.add(map.get("BROWSER").toString());
            }
        }
        return browsers;
    }

    /**
     * get os
     *
     * @return
     */
    public List<String> listOperationSystems() {
        List<Map<String, Object>> groupList = super.listMaps(
                new QueryWrapper<LoginLogEntity>()
                        .select("OS, COUNT(1) CNT ")
                        .groupBy("OS"));
        List<String> oses = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupList)) {
            for (Map<String, Object> map : groupList) {
                oses.add(map.get("OS").toString());
            }
        }
        return oses;
    }

    /**
     * get last LoginLogEntity by index
     *
     * @param index
     * @return
     */
    public LoginLogEntity getLastByIndex(int index) {
        QueryWrapper<LoginLogEntity> query = new QueryWrapper<>();
        query.lambda().orderByDesc(LoginLogEntity::getLogId);
        query.last("limit " + index + ", 1");
        List<LoginLogEntity> logEntities = this.list(query);
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
            log.info("LoginLogEntity deleteByLtLogId logId:{} times: {} total:{} time:{}", logId, times, total, LocalDateTime.now());
            if (times > maxTimes) {
                break;
            }
        } while (count > 0);
        log.info("LoginLogEntity deleteByLtLogId end logId:{} times: {} total:{} time:{}", logId, times, total, LocalDateTime.now());
        return total;
    }

}