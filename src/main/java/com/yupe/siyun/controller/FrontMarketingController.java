package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yupe.siyun.entity.JsCourseVO;
import com.yupe.siyun.entity.OpCircleAd;
import com.yupe.siyun.mapper.CourseVOMapper;
import com.yupe.siyun.mapper.OpCircleAdMapper;
import com.yupe.siyun.util.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/siyun")
public class FrontMarketingController {

    @Autowired
    private OpCircleAdMapper opCircleAdMapper;
    @Autowired
    private CourseVOMapper courseVOMapper;

    /**
     * 前台首页顶部轮播图，只返回当前处于展示期的有效内容。
     */
    @GetMapping("/ads")
    public Object homeAds() {
        LocalDateTime now = LocalDateTime.now();
        List<OpCircleAd> ads = opCircleAdMapper.selectList(
                new LambdaQueryWrapper<OpCircleAd>()
                        .eq(OpCircleAd::getPositionType, 1)
                        .eq(OpCircleAd::getStatusShow, 1)
                        .and(wrapper -> wrapper.isNull(OpCircleAd::getStartTime)
                                .or().le(OpCircleAd::getStartTime, now))
                        .and(wrapper -> wrapper.isNull(OpCircleAd::getEndTime)
                                .or().ge(OpCircleAd::getEndTime, now))
                        .isNotNull(OpCircleAd::getPicUrl)
                        .ne(OpCircleAd::getPicUrl, "")
                        .orderByDesc(OpCircleAd::getSortNum)
                        .orderByDesc(OpCircleAd::getId)
        );
        List<Integer> courseIds = ads.stream()
                .map(OpCircleAd::getCourseId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        List<JsCourseVO> courses = courseIds.isEmpty()
                ? List.of()
                : courseVOMapper.selectList(
                        new LambdaQueryWrapper<JsCourseVO>()
                                .in(JsCourseVO::getId, courseIds)
                );
        courses.forEach(course -> course.setVideoUrl(null));
        return ResultData.success(
                new String[]{"ads", "courses"},
                new Object[]{ads, courses},
                "首页轮播图查询成功"
        );
    }
}
