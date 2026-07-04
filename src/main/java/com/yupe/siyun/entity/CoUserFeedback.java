package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
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

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private String content;

    //1视频反馈 2商品反馈 3其他反馈
    private Integer feedbackType;

    //1已提交未审核 2已审核未回复 3已回复未解决 4已解决
    private Integer status;

    private String picUrl;
    private Integer starLevel;//1-5
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer updateBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private String remark;
}

