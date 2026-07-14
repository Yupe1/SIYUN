package com.yupe.siyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupe.siyun.entity.*;
import com.yupe.siyun.mapper.ApxCoursePlayLogMapper;
import com.yupe.siyun.mapper.CourseVOMapper;
import com.yupe.siyun.mapper.OpOrderMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseVOServiceImpl extends ServiceImpl<CourseVOMapper, JsCourseVO> implements CourseVOService {
    @Autowired
    private OpOrderMapper opOrderMapper;
    @Autowired
    private CourseVOMapper courseVOMapper;
    @Autowired
    private ApxCoursePlayLogMapper apxCoursePlayLogMapper;

    @Override
    public List<JsCourseVO> search(String keywords) {
        List<JsCourseVO> courses = new ArrayList<>();
        //  teacherName // cateName // title
        LambdaQueryWrapper<JsCourseVO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(JsCourseVO::getTeacherName, keywords)
                .or()
                .like(JsCourseVO::getCateName, keywords)
                .or()
                .like(JsCourseVO::getTitle, keywords);
        List<JsCourseVO> course = this.list(wrapper);
        courses.addAll(course);
        return courses;
    }

    @Override
    public void purchase(ObjFrontUser frontUser, JsCourse jsCourse) {
        if (hasPurchased(frontUser, jsCourse.getId())) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "您已购买该课程，请直接学习");
        }
        OpOrder opOrder = new OpOrder();
        opOrder.setUserId(frontUser.getId());
        opOrder.setEntityId(jsCourse.getId());
        opOrder.setEntityType(1);
        opOrder.setStatus(1);
        opOrder.setTotalQuantity(1);
        opOrder.setPriceTotal(courseVOMapper.selectById(jsCourse.getId()).getPriceOriginal());
        opOrder.setPricePay(jsCourse.getPriceOriginal());//这里其实是前端选择优惠券计算后的优惠价
        opOrder.setCommentStatus(0);
        opOrderMapper.insert(opOrder);
    }

    @Override
    public ApxCoursePlayLog startPlay(ObjFrontUser frontUser, JsCourse jsCourse) {
        if (!hasPurchased(frontUser, jsCourse.getId())) {
            throw new RuntimeException("请先购买该课程");
        }
        JsCourseVO course = courseVOMapper.selectById(jsCourse.getId());
        ApxCoursePlayLog log = new ApxCoursePlayLog();
        String videoUrl = jsCourse.getVideoUrl();
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            videoUrl = course.getVideoUrl();
        }
        log.setVideoUrl(videoUrl);
        log.setCourseId(jsCourse.getId());
        log.setUserId(frontUser.getId());
        log.setStartTime(LocalDateTime.now());
        apxCoursePlayLogMapper.insert(log);
        return log;
    }

    @Override
    public boolean hasPurchased(ObjFrontUser frontUser, Integer courseId) {
        Long count = opOrderMapper.selectCount(
                new QueryWrapper<OpOrder>()
                        .eq("user_id", frontUser.getId())
                        .eq("entity_id", courseId)
                        .eq("entity_type", 1)
                        .ge("status", 1)
        );
        return count != null && count > 0;
    }
}
