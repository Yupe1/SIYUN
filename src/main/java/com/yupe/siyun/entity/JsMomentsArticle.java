package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
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


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer authorId;
    private String title;
    private String keywords;
    private String coverUrl;
    private String content;
    private Integer countView;
    private Integer countLike;
    private Integer countComment;
    private Integer countCollect;
    private Integer sortNum;
    //0隐藏 1显示
    private Integer statusShow;
    //0草稿 1未提交 2已提交 3已通过 4退回
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer updateBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

