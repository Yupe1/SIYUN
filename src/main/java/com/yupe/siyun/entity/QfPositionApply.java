package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资格职位申请表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("qf_position_apply")
public class QfPositionApply implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 发起时间
     */
    private LocalDateTime applyTime;

    /**
     * 发起人(前台用户id)
     */
    private Integer userId;

    /**
     * 申请职务
     */
    private String targetPosition;

    /**
     * 申请原因
     */
    private String applyReason;

    /**
     * 身份证号
     */
    private String chinaId;

    /**
     * 手机号
     */
    private String tel;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 作品/资料路径 (★需要上传)
     */
    private String fileUrl;

    /**
     * 执行人(人事id)
     */
    private Integer handlerId;

    /**
     * 执行时间
     */
    private LocalDateTime handleTime;

    /**
     * 0申请中 1审核中 2已通过 3已驳回
     */
    private Integer status;

    /**
     * 审批结果备注原因
     */
    private String handleRemark;
}

