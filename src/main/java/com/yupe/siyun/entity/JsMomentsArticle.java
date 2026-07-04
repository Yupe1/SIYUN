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
 * 微圈文章表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("js_moments_article")
public class JsMomentsArticle implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 作者前台用户id
     */
    private Integer authorId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章关键字
     */
    private String keywords;

    /**
     * 文章封面图片路径 (★需要上传)
     */
    private String coverUrl;

    /**
     * 文章内容(富文本) (★可能需要上传-富文本)
     */
    private String content;

    /**
     * 文章分类id
     */
    private Integer cateId;

    /**
     * 点击总量
     */
    private Integer countView;

    /**
     * 点赞总量
     */
    private Integer countLike;

    /**
     * 评论总量
     */
    private Integer countComment;

    /**
     * 分享总量
     */
    private Integer countShare;

    /**
     * 排序序号
     */
    private Integer sortNum;

    /**
     * 0隐藏 1显示
     */
    private Integer statusShow;

    /**
     * 0草稿 1未提交 2已提交 3已通过 4退回
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 更新人id
     */
    private Integer updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

