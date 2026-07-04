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
 * 首页轮播图表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("op_circle_ad")
public class OpCircleAd implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 图片网址路径 (★需要上传)
     */
    private String picUrl;

    /**
     * 图片标题
     */
    private String title;

    /**
     * 1首页顶部 2商城首页 3微圈顶部
     */
    private Integer positionType;

    /**
     * 展示开始时间
     */
    private LocalDateTime startTime;

    /**
     * 展示到期时间
     */
    private LocalDateTime endTime;

    /**
     * 0不显示 1显示
     */
    private Integer statusShow;

    /**
     * 排序序号(置顶权重)
     */
    private Integer sortNum;

    /**
     * 描述文本
     */
    private String intro;

    /**
     * 创建人id
     */
    private Integer createBy;

    /**
     * 备注
     */
    private String remark;
}

