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
 * 用户意见反馈表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("co_user_feedback")
public class CoUserFeedback implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 意见反馈内容
     */
    private String content;

    /**
     * 2视频反馈 3商品反馈 4其他反馈
     */
    private Integer feedbackType;

    /**
     * 2已提交 3待审核 4已审核 5未提交 6提交失败
     */
    private Integer status;

    /**
     * 上传图片路径 (★需要上传)
     */
    private String picUrl;

    /**
     * 满意度评分星级(1-5星)
     */
    private Integer starLevel;

    /**
     * 意见分类评价: 2很好 3好 4一般 5差
     */
    private Integer classification;

    /**
     * 反馈时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private Integer updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}

