package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class JsCourse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 主讲教师(后台用户id)
     */
    private Integer teacherId;

    /**
     * 所属专业分类id
     */
    private Integer cateId;

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

    /**
     * 课程原价
     */
    private BigDecimal priceOriginal;

    /**
     * 课程促销价
     */
    private BigDecimal pricePromotion;

    /**
     * 0下架 1上架
     */
    private Integer statusShelf;

    /**
     * 0未审核 1审核中 2审核失败 3通过
     */
    private Integer statusAudit;

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

