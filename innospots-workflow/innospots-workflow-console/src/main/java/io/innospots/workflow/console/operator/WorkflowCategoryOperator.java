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

package io.innospots.workflow.console.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.innospots.base.enums.DataStatus;
import io.innospots.libra.base.category.CategoryType;
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.base.category.BaseCategoryOperator;
import io.innospots.workflow.console.entity.instance.WorkflowInstanceEntity;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenc
 * @date 2022/2/19
 */
public class WorkflowCategoryOperator extends BaseCategoryOperator {

    private final WorkflowInstanceOperator workflowInstanceOperator;

    public WorkflowCategoryOperator(WorkflowInstanceOperator workflowInstanceOperator) {
        this.workflowInstanceOperator = workflowInstanceOperator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCategory(Integer categoryId) {
        // update workflowInstance category is default
        workflowInstanceOperator.update(new UpdateWrapper<WorkflowInstanceEntity>().lambda().eq(WorkflowInstanceEntity::getCategoryId, categoryId).set(WorkflowInstanceEntity::getCategoryId, 0));
        return super.deleteCategory(categoryId);
    }

    public List<BaseCategory> list(Boolean hasNumber) {
        List<BaseCategory> list = super.listCategories(CategoryType.WORKFLOW);
        if (hasNumber == null) {
            hasNumber = Boolean.TRUE;
        }
        if (!hasNumber) {
            list.remove(0);
        }
        if (hasNumber) {
            List<Map<String, Object>> groupList = workflowInstanceOperator.listMaps(
                    new QueryWrapper<WorkflowInstanceEntity>()
                            .select("CASE WHEN CATEGORY_ID IS NULL THEN 0 ELSE CATEGORY_ID END AS CATEGORY_ID, COUNT(1) CNT ")
                            .ne("STATUS", DataStatus.REMOVED.name())
                            .groupBy("CATEGORY_ID"));

            Map<Integer, Integer> groupMap = new HashMap<>();
            for (Map<String, Object> cateMap : groupList) {
                Integer cateId = Integer.valueOf(cateMap.get("CATEGORY_ID").toString());
                Integer cnt = Integer.valueOf(cateMap.get("CNT").toString());
                if (groupMap.containsKey(cateId)) {
                    groupMap.put(cateId, groupMap.get(cateId) + cnt);
                } else {
                    groupMap.put(cateId, cnt);
                }
            }

            // fill subsetTotal
            for (BaseCategory category : list) {
                Integer count = groupMap.get(category.getCategoryId());
                category.setTotalCount(count == null ? 0 : count);
            }

            BaseCategory recycle = getRecycleBinCategory();
            long count = workflowInstanceOperator.count(new QueryWrapper<WorkflowInstanceEntity>().lambda().eq(WorkflowInstanceEntity::getStatus, DataStatus.REMOVED));
            recycle.setTotalCount((int) count);
            list.add(recycle);
        }
        return list;
    }


}