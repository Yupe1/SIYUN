package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("js_course")
public class JsCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer teacherId;
    /**
     * 前台创作者用户ID。后台教师课程使用 teacherId，前台上传课程使用此字段。
     */
    private Integer frontCreatorId;
    private Integer cateId;
    //0普通 1新品 2推荐
    private Integer recommendType;
    private String title;
    private String intro;
    private BigDecimal priceOriginal;
    //0下架 1上架
    private Integer statusShelf;
    //0未审核 1审核中 2审核失败 3通过
    private Integer statusAudit;
    private String keywords;
    private String coverUrl;
    private String videoUrl;
    private String detailDesc;
    private Integer episodeNum;
    private Integer duration;

    private Integer createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer updateBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
