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
 * 全站评论表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("co_comment")
public class CoComment implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 评论用户id(前台用户id)
     */
    private Integer userId;

    /**
     * 关联实体主键(课程ID/微圈ID/商品ID)
     */
    private Integer entityId;

    /**
     * 1视频 2微圈 3商品 4其他
     */
    private Integer entityType;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父级评论主键(第一级为0)
     */
    private Integer parentId;

    /**
     * 0不显示/被拦截 1显示
     */
    private Integer statusShow;

    /**
     * 点赞数量
     */
    private Integer countLike;

    /**
     * 下级回复数量
     */
    private Integer countReply;

    /**
     * 创建人id
     */
    private Integer createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Integer updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

