package io.innospots.approve.console.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.innospots.approve.console.enums.ApproveStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Id;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/19
 */
public class ApproveFormInstance {

    protected String appInstanceKey;

    protected String flowKey;

    protected String appKey;

    protected String belongTo;

    protected String approveType;

    protected ApproveStatus approveStatus;

    protected Integer originatorId;

    protected String originator;

    protected String message;
}
