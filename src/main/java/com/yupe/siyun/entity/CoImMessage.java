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
 * 客服会话消息表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("co_im_message")
public class CoImMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 发送人前台用户id
     */
    private Integer senderId;

    /**
     * 发送人姓名
     */
    private String senderName;

    /**
     * 接收人用户id(0代表智能客服/机器人)
     */
    private Integer receiverId;

    /**
     * 接收人姓名
     */
    private String receiverName;

    /**
     * 具体消息内容(文本)
     */
    private String content;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 0未读 1已读
     */
    private Integer isRead;
}

