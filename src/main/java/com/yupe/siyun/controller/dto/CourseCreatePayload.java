package com.yupe.siyun.controller.dto;

import com.yupe.siyun.entity.JsCourse;
import com.yupe.siyun.entity.JsCourseContent;
import lombok.Data;

import java.util.List;

@Data
public class CourseCreatePayload {
    private JsCourse course;
    private List<JsCourseContent> contents;
}
