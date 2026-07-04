package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服会话消息表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("co_im_message")
public class CoImMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private String receiverName;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime sendTime;
    private Integer isRead;
}

