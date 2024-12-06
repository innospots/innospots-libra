package io.innospots.approve.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.innospots.approve.core.entity.ApproveFlowInstanceEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/22
 */
public interface ApproveFlowInstanceDao extends BaseMapper<ApproveFlowInstanceEntity> {

    @Select({
            "<script>",
            "SELECT afi.* FROM approve_flow_instance afi",
            "JOIN approve_actor aa ON afi.approve_instance_key = aa.approve_instance_key",
            "WHERE",
            "((aa.actor_id IN",
            "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>",
            "#{roleId}",
            "</foreach>",
            "AND aa.actor_type = 'ROLE')",
            "OR (aa.actor_id = #{userId} AND aa.actor_type = 'USER')",
            "<if test='groupId != null'>",
            "OR (aa.actor_id = #{groupId} AND aa.actor_type = 'GROUP')",
            "</if>",
            ")",
            "AND aa.approve_action = 'PENDING'",
            "<if test='approveType != null'>",
            "AND aa.approve_type = #{approveType}",
            "</if>",
            "<if test='startTime != null'>",
            "AND afi.start_time &gt; #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "AND afi.start_time &lt; #{endTime}",
            "</if>",
            "ORDER BY afi.start_time ASC",
            "</script>"
    })
    IPage<ApproveFlowInstanceEntity> selectPendingApproveFlowInstances(
            Page<ApproveFlowInstanceEntity> page,
            @Param("roleIds") List<Integer> roleIds,
            @Param("userId") Integer userId,
            @Param("groupId") Integer groupId,
            @Param("approveType") String approveType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

}
