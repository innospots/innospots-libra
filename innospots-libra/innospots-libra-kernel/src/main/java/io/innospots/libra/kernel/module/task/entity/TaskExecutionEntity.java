package io.innospots.libra.kernel.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.BaseEntity;
import io.innospots.libra.base.task.TaskExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import static io.innospots.libra.kernel.module.task.entity.TaskExecutionEntity.TABLE_NAME;


/**
 * @author Smars
 * @date 2023/8/7
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class TaskExecutionEntity extends BaseEntity {

    public static final String TABLE_NAME = "sys_task_execution";

    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 64)
    private String taskExecutionId;

    @Column(length = 64)
    private String taskName;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private TaskExecutionStatus executionStatus;

    @Column(length = 64)
    private String extensionKey;

    @Column(length = 16)
    private String extensionType;

    @Column(length = 32)
    private String appName;

    @Column(length = 32)
    private String appKey;

    @Column
    private int percent;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String paramContext;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column
    private String detailUrl;
}
