package com.yupe.siyun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupe.siyun.entity.ApxCoursePlayLog;
import com.yupe.siyun.entity.JsCourse;
import com.yupe.siyun.entity.JsCourseVO;
import com.yupe.siyun.entity.ObjFrontUser;

import java.util.List;

public interface CourseVOService extends IService<JsCourseVO> {
    List<JsCourseVO> search(String keywords);

    void purchase(ObjFrontUser frontUser, JsCourse jsCourse);

    ApxCoursePlayLog startPlay(ObjFrontUser frontUser, JsCourse jsCourse);

    boolean hasPurchased(ObjFrontUser frontUser, Integer courseId);
}
