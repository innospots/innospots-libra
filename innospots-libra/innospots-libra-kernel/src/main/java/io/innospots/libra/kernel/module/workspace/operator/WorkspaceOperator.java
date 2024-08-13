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

package io.innospots.libra.kernel.module.workspace.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.CCH;
import io.innospots.libra.base.utils.ServerTools;
import io.innospots.libra.kernel.module.page.entity.PageEntity;
import io.innospots.libra.kernel.module.page.enums.PageOperationType;
import io.innospots.libra.kernel.module.page.model.PageDetail;
import io.innospots.libra.kernel.module.page.operator.PageOperator;
import io.innospots.libra.kernel.module.workspace.dao.WorkspaceDao;
import io.innospots.libra.kernel.module.workspace.entity.WorkspaceEntity;
import io.innospots.libra.kernel.module.workspace.converter.WorkspaceConverter;
import io.innospots.libra.kernel.module.workspace.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Alfred
 * @date 2022/1/30
 */
@Service
public class WorkspaceOperator extends ServiceImpl<WorkspaceDao, WorkspaceEntity> {

    @Autowired
    private PageOperator pageOperator;

    @Transactional(rollbackFor = Exception.class)
    public Workspace updateWorkspace(Workspace workspace) {
        WorkspaceEntity entity = WorkspaceConverter.INSTANCE.modelToEntity(workspace);
        super.updateById(entity);

        PageDetail pageDetail = WorkspaceConverter.INSTANCE.workspaceToPageDetail(workspace);
        PageDetail pageDetailUpdate = pageOperator.createOrUpdate(pageDetail, PageOperationType.SAVE);

        Workspace update = WorkspaceConverter.INSTANCE.pageDetailToWorkspace(pageDetailUpdate);
        update.setUserId(workspace.getUserId());
        return update;
    }

    @Transactional(rollbackFor = Exception.class)
    public Workspace getWorkspaceByCurrentUser() {
        WorkspaceEntity entity = super.getOne(new QueryWrapper<WorkspaceEntity>().lambda().eq(WorkspaceEntity::getUserId, CCH.userId()));
        if (entity == null) {
            this.initialize(CCH.userId());
            entity = super.getOne(new QueryWrapper<WorkspaceEntity>().lambda().eq(WorkspaceEntity::getUserId, CCH.userId()));
        }

        PageDetail pageDetail = pageOperator.getPageDetail(entity.getPageId());
        Workspace workspace = WorkspaceConverter.INSTANCE.pageDetailToWorkspace(pageDetail);
        workspace.setUserId(entity.getUserId());
        return workspace;
    }

    private void initialize(Integer userId) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setPageType("workspace");
        pageEntity.setStatus(DataStatus.ONLINE.name());
        pageOperator.save(pageEntity);

        WorkspaceEntity workspaceEntity = new WorkspaceEntity();
        workspaceEntity.setUserId(userId);
        workspaceEntity.setPageId(pageEntity.getPageId());
        super.save(workspaceEntity);
    }

    public SystemInfo getSystemInfo() {
        try {
            return new SystemInfo(getBasicInfo(), getRunInfo(), getEnvInfo());
        } catch (Exception e) {
            log.error("get system info error: {}", e);
        }
        return null;
    }


    /**
     * get system env info
     *
     * @return
     */
    public SystemEnvInfo getEnvInfo() {
        SystemEnvInfo envInfo = new SystemEnvInfo();
        Properties props = System.getProperties();
        String jdkVersion = props.getProperty("java.version");
        String jdkHome = props.getProperty("java.home");
        envInfo.setJdkVersion(jdkVersion);
        envInfo.setJavaHome(jdkHome);
        List<String> results = this.executeCommand("jps");
        if (CollectionUtils.isNotEmpty(results)) {
            for (String result : results) {
                if (result.contains("flink")) {
                    envInfo.setInstallFlink(true);
                } else if (result.contains("mysql")) {
                    envInfo.setInstallMysql(true);
                } else if (result.contains("kafka")) {
                    envInfo.setInstallKafka(true);
                } else if (result.contains("redis")) {
                    envInfo.setInstallRedis(true);
                }
            }
        }
        if (!envInfo.isInstallMysql()) {
            results = this.executeCommands(new String[]{"ps", "-eaf"});
            for (String result : results) {
                if (result.contains("mysql")) {
                    envInfo.setInstallMysql(true);
                    break;
                }
            }
        }
        return envInfo;
    }

    /**
     * execute command
     *
     * @param command
     * @return
     */
    public List<String> executeCommand(String command) {
        List<String> rspList = new ArrayList<>();
        Runtime run = Runtime.getRuntime();
        try {
            Process proc = run.exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            // This command must be executed, or the in stream will not end
            out.println("exit");
            String rspLine;
            while ((rspLine = in.readLine()) != null) {
                rspList.add(rspLine.toLowerCase());
            }
            log.debug(JSONUtils.toJsonString(rspList));
            proc.waitFor();
            in.close();
            out.close();
            proc.destroy();

        } catch (Exception e) {
            log.error("execute command error: {}", e);
        }
        return rspList;
    }

    /**
     * execute commands
     *
     * @param commands
     * @return
     */
    public List<String> executeCommands(String[] commands) {
        List<String> rspList = new ArrayList<>();
        Runtime run = Runtime.getRuntime();
        try {
            Process proc = run.exec(commands);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String rspLine;
            while ((rspLine = in.readLine()) != null) {
                rspList.add(rspLine.toLowerCase());
            }
            in.close();
            proc.destroy();

        } catch (Exception e) {
            log.error("execute commands error: {}", e);
        }
        return rspList;
    }

    /**
     * get system run info
     *
     * @return
     */
    public SystemRunInfo getRunInfo() throws InterruptedException {
        SystemRunInfo runInfo = new SystemRunInfo();
        runInfo.setProcessCount(ServerTools.getOs().getProcessCount() + "");
        runInfo.setMemoryUse(ServerTools.getMemoryUse());
        runInfo.setAvailableDisk(ServerTools.getAvailableDisk());
        runInfo.setCpuUse(ServerTools.getCpuUse());
        return runInfo;
    }

    /**
     * get system basic info
     *
     * @return
     */
    private SystemBasicInfo getBasicInfo() {
        SystemBasicInfo basicInfo = new SystemBasicInfo();
        basicInfo.setOs(ServerTools.getOs().getFamily());
        basicInfo.setKernelVersion(ServerTools.getOsVersion().getBuildNumber());
        basicInfo.setMemory(ServerTools.getMemoryTotal());
        basicInfo.setCpuModel(ServerTools.getCpu().getProcessorIdentifier().getName());
        basicInfo.setCpuCores(ServerTools.getCpu().getPhysicalProcessorCount() + "核");
        basicInfo.setHostName(ServerTools.getNetworkParams().getHostName());
        basicInfo.setDiskSpace(ServerTools.getTotalDisk());
        basicInfo.setDomainName(ServerTools.getNetworkParams().getDomainName());
        basicInfo.setDnsServers(Arrays.asList(ServerTools.getNetworkParams().getDnsServers()));
        basicInfo.setIpv4(ServerTools.getNetworkParams().getIpv4DefaultGateway());
        basicInfo.setIpv6(ServerTools.getNetworkParams().getIpv6DefaultGateway());
        basicInfo.setBootTime(String.valueOf(ServerTools.getOs().getSystemBootTime()));
        basicInfo.setOsManufacturer(ServerTools.getOs().getManufacturer());
        basicInfo.setBaseboardManufacturer(ServerTools.getComputer().getBaseboard().getManufacturer());
        basicInfo.setSystemManufacturer(ServerTools.getComputer().getManufacturer());
        return basicInfo;
    }
}
