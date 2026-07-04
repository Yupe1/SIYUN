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


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String picUrl;
    private String title;
    //1首页顶部 2商城首页 3微圈顶部
    private Integer positionType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    //0不显示 1显示
    private Integer statusShow;
    private Integer sortNum;
    private String intro;
    private Integer createBy;
    private String remark;
}

