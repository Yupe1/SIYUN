package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程主表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("v_course")
public class JsCourseVO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 主讲教师(后台用户名字)
     */
    private String teacherName;

    /**
     * 前台创作者用户ID；后台教师课程为空。
     */
    private Integer frontCreatorId;

    /**
     * 所属专业分类名
     */
    private String cateName;

    /**
     * 0普通 1新品 2推荐
     */
    private Integer recommendType;

    /**
     * 课程名称
     */
    private String title;

    /**
     * 课程简介
     */
    private String intro;

    //课程原价
    private BigDecimal priceOriginal;
    //商品-coupon最大优惠价,具体看用户-商品 即v_coupon
    private BigDecimal pricePromotion;

    /**
     * 搜索关键词
     */
    private String keywords;

    /**
     * 封面图片路径 (★需要上传)
     */
    private String coverUrl;

    /**
     * 视频源文件路径 (★需要上传)
     */
    private String videoUrl;

    /**
     * 课程详情描述 (★可能需要上传-富文本)
     */
    private String detailDesc;

    /**
     * 集数/课程序号
     */
    private Integer episodeNum;

    /**
     * 视频时长(分钟)
     */
    private Integer duration;

    //==================================视图独占属性==========================================
    /**
     * 点赞总数
     */
    private Integer countLike;

    /**
     * 分享总数
     */
    private Integer countShare;

    /**
     * 收藏总数
     */
    private Integer countCollect;

    /**
     * 播放总数
     */
    private Integer countView;

    /**
     * 实际销量
     */
    private Integer salesVolume;
    //========================================================================================

    private Integer createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer updateBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
