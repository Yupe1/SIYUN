package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.AuditPayload;
import com.yupe.siyun.controller.dto.CourseCreatePayload;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.CoComment;
import com.yupe.siyun.entity.JsCourse;
import com.yupe.siyun.entity.JsCourseCategory;
import com.yupe.siyun.entity.JsCourseContent;
import com.yupe.siyun.entity.JsCourseVO;
import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.QfAuditLog;
import com.yupe.siyun.entity.QfUserRole;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.BackUserMapper;
import com.yupe.siyun.mapper.CoCommentMapper;
import com.yupe.siyun.mapper.CourseVOMapper;
import com.yupe.siyun.mapper.JsCourseCategoryMapper;
import com.yupe.siyun.mapper.JsCourseContentMapper;
import com.yupe.siyun.mapper.JsCourseMapper;
import com.yupe.siyun.mapper.QfUserRoleMapper;
import com.yupe.siyun.service.FileService;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminCourseController extends AdminControllerSupport {

    @Autowired
    private JsCourseMapper jsCourseMapper;
    @Autowired
    private CourseVOMapper courseVOMapper;
    @Autowired
    private JsCourseContentMapper jsCourseContentMapper;
    @Autowired
    private JsCourseCategoryMapper jsCourseCategoryMapper;
    @Autowired
    private CoCommentMapper coCommentMapper;
    @Autowired
    private BackUserMapper backUserMapper;
    @Autowired
    private QfUserRoleMapper qfUserRoleMapper;
    @Autowired
    private FileService fileService;

    @Value("${upload.profile.course.cover.path}")
    private String courseCoverPath;

    @Value("${upload.profile.course.video.path}")
    private String courseVideoPath;

    @GetMapping("/courses")
    @RequiresPermission("admin:course:list")
    public Object courses(@RequestParam(defaultValue = "1") Long page,
                          @RequestParam(defaultValue = "10") Long size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer cateId,
                          @RequestParam(required = false) Integer statusShelf,
                          @RequestParam(required = false) Integer statusAudit,
                          HttpSession session) {
        LambdaQueryWrapper<JsCourse> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(JsCourse::getTitle, keyword).or().like(JsCourse::getKeywords, keyword));
        }
        if (cateId != null) wrapper.eq(JsCourse::getCateId, cateId);
        if (statusShelf != null) wrapper.eq(JsCourse::getStatusShelf, statusShelf);
        if (statusAudit != null) wrapper.eq(JsCourse::getStatusAudit, statusAudit);
        if (teacherOnly(session)) wrapper.eq(JsCourse::getTeacherId, currentUser(session).getId());
        wrapper.orderByDesc(JsCourse::getCreateTime);
        Page<JsCourse> data = new Page<>(page, size);
        jsCourseMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "课程列表");
    }

    @GetMapping("/courses/export")
    @RequiresPermission("admin:course:export")
    public Object exportCourses(@RequestParam(required = false) String keyword, HttpSession session) {
        LambdaQueryWrapper<JsCourse> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(JsCourse::getTitle, keyword).or().like(JsCourse::getKeywords, keyword));
        }
        if (teacherOnly(session)) wrapper.eq(JsCourse::getTeacherId, currentUser(session).getId());
        return ResultData.success("courses", jsCourseMapper.selectList(wrapper), "课程导出数据");
    }

    @PostMapping("/courses/import")
    @RequiresPermission("admin:course:import")
    public Object importCourses(@RequestBody List<JsCourse> courses, HttpSession session) {
        for (JsCourse course : courses) {
            fillCourseDefaults(course, currentUser(session));
            jsCourseMapper.insert(course);
        }
        return ResultData.success("课程导入成功");
    }

    @GetMapping("/courses/{id}")
    @RequiresPermission("admin:course:detail")
    public Object courseDetail(@PathVariable Integer id, HttpSession session) {
        JsCourse course = jsCourseMapper.selectById(id);
        if (course == null) throw new MyException(ErrorType.WRONG_INFO, "课程不存在");
        ensureCourseOwnerIfTeacher(course, session);
        List<JsCourseContent> contents = jsCourseContentMapper.selectList(
                new LambdaQueryWrapper<JsCourseContent>()
                        .eq(JsCourseContent::getCourseId, id)
                        .orderByAsc(JsCourseContent::getEpNo)
        );
        List<CoComment> comments = coCommentMapper.selectList(
                new LambdaQueryWrapper<CoComment>()
                        .eq(CoComment::getEntityId, id)
                        .orderByDesc(CoComment::getCreateTime)
        );
        JsCourseVO view = courseVOMapper.selectById(id);
        return ResultData.success(
                new String[]{"course", "view", "contents", "comments"},
                new Object[]{course, view, contents, comments},
                "课程详情"
        );
    }

    @PostMapping("/courses")
    @RequiresPermission("admin:course:add")
    public Object addCourse(@RequestBody JsCourse course, HttpSession session) {
        fillCourseDefaults(course, currentUser(session));
        jsCourseMapper.insert(course);
        return ResultData.success("course", course, "课程已提交审核");
    }

    @PostMapping("/courses/with-contents")
    @RequiresPermission("admin:course:add")
    @Transactional
    public Object addCourseWithContents(@RequestBody CourseCreatePayload payload, HttpSession session) {
        if (payload.getCourse() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程信息不能为空");
        }
        List<JsCourseContent> contents = payload.getContents() == null ? Collections.emptyList() : payload.getContents();
        if (contents.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请至少添加一集课程内容");
        }
        ObjBackUser user = currentUser(session);
        JsCourse course = payload.getCourse();
        fillCourseDefaults(course, user);
        course.setStatusAudit(1);
        course.setEpisodeNum(contents.size());
        if (course.getDuration() == null) {
            course.setDuration(contents.stream()
                    .map(JsCourseContent::getDuration)
                    .filter(Objects::nonNull)
                    .reduce(0, Integer::sum));
        }
        if (!hasText(course.getVideoUrl())) {
            String firstVideoUrl = contents.stream()
                    .map(JsCourseContent::getVideoUrl)
                    .filter(this::hasText)
                    .findFirst()
                    .orElse(null);
            if (!hasText(firstVideoUrl)) {
                throw new MyException(ErrorType.WRONG_INFO, "请至少填写一集视频文件路径");
            }
            course.setVideoUrl(firstVideoUrl);
        }
        jsCourseMapper.insert(course);

        for (int i = 0; i < contents.size(); i++) {
            JsCourseContent content = contents.get(i);
            if (!hasText(content.getEpName())) {
                throw new MyException(ErrorType.WRONG_INFO, "第 " + (i + 1) + " 集名称不能为空");
            }
            if (!hasText(content.getVideoUrl())) {
                throw new MyException(ErrorType.WRONG_INFO, "第 " + (i + 1) + " 集视频文件路径不能为空");
            }
            content.setCourseId(course.getId());
            content.setEpNo(i + 1);
            if (content.getDuration() == null) {
                content.setDuration(0);
            }
            jsCourseContentMapper.insert(content);
        }
        return ResultData.success(new String[]{"course", "contents"}, new Object[]{course, contents}, "课程已提交审核");
    }

    @GetMapping("/upload/ping")
    public Object uploadPing() {
        return ResultData.success("后台上传接口已加载");
    }

    @PostMapping("/upload/course-cover")
    @RequiresPermission("admin:course:add")
    public Object uploadCourseCover(@RequestParam("file") MultipartFile file) {
        return uploadCourseFile(file, courseCoverPath, "coverUrl", "课程封面上传成功");
    }

    @PostMapping("/upload/course-video")
    @RequiresPermission("admin:course:add")
    public Object uploadCourseVideo(@RequestParam("file") MultipartFile file) {
        return uploadCourseFile(file, courseVideoPath, "videoUrl", "课程视频上传成功");
    }

    private Object uploadCourseFile(MultipartFile file, String subPath, String resultKey, String message) {
        if (file == null || file.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择要上传的文件");
        }
        try {
            String url = fileService.uploadFile(file, subPath);
            return ResultData.success(resultKey, url, message);
        } catch (IOException e) {
            throw new MyException(ErrorType.OPERATION_FAILED, "文件上传失败，请检查上传目录权限");
        }
    }

    @GetMapping("/course-teachers")
    @RequiresPermission("admin:course:add")
    public Object courseTeachers(HttpSession session) {
        if (teacherOnly(session)) {
            ObjBackUser teacher = currentUser(session);
            teacher.setPassword(null);
            return ResultData.success("teachers", Collections.singletonList(teacher), "可选教师");
        }
        List<QfUserRole> links = qfUserRoleMapper.selectList(
                new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getRoleId, 1)
        );
        List<Integer> teacherIds = links.stream()
                .map(QfUserRole::getBackUserId)
                .distinct()
                .collect(Collectors.toList());
        if (teacherIds.isEmpty()) {
            return ResultData.success("teachers", Collections.emptyList(), "可选教师");
        }
        List<ObjBackUser> teachers = backUserMapper.selectList(
                new LambdaQueryWrapper<ObjBackUser>()
                        .in(ObjBackUser::getId, teacherIds)
                        .eq(ObjBackUser::getStatus, 0)
        );
        teachers.forEach(teacher -> teacher.setPassword(null));
        return ResultData.success("teachers", teachers, "可选教师");
    }

    @PutMapping("/courses/{id}")
    @RequiresPermission("admin:course:update")
    public Object updateCourse(@PathVariable Integer id, @RequestBody JsCourse course, HttpSession session) {
        JsCourse old = jsCourseMapper.selectById(id);
        if (old == null) throw new MyException(ErrorType.WRONG_INFO, "课程不存在");
        ensureCourseOwnerIfTeacher(old, session);
        course.setId(id);
        course.setUpdateBy(currentUser(session).getId());
        if (teacherOnly(session)) {
            course.setStatusAudit(1);
            course.setStatusShelf(0);
        }
        jsCourseMapper.updateById(course);
        return ResultData.success("课程已更新");
    }

    @DeleteMapping("/courses/{id}")
    @RequiresPermission("admin:course:delete")
    public Object deleteCourse(@PathVariable Integer id, HttpSession session) {
        JsCourse course = jsCourseMapper.selectById(id);
        if (course == null) throw new MyException(ErrorType.WRONG_INFO, "课程不存在");
        ensureCourseOwnerIfTeacher(course, session);
        jsCourseMapper.deleteById(id);
        return ResultData.success("课程已删除");
    }

    @PostMapping("/courses/{id}/audit")
    @RequiresPermission("admin:course:audit")
    public Object auditCourse(@PathVariable Integer id, @RequestBody AuditPayload payload, HttpSession session) {
        JsCourse course = jsCourseMapper.selectById(id);
        if (course == null) throw new MyException(ErrorType.WRONG_INFO, "课程不存在");
        Integer result = payload.getAuditResult() == null ? 0 : payload.getAuditResult();
        course.setStatusAudit(result == 1 ? 3 : 2);
        if (result == 1 && payload.getStatusShelf() != null) {
            course.setStatusShelf(payload.getStatusShelf());
        }
        course.setUpdateBy(currentUser(session).getId());
        jsCourseMapper.updateById(course);
        addAuditLog(id, 1, course.getCreateBy(), result, payload, session);
        return ResultData.success("课程审核完成");
    }

    @GetMapping("/courses/{id}/logs")
    @RequiresPermission("admin:course:log")
    public Object courseLogs(@PathVariable Integer id, HttpSession session) {
        JsCourse course = jsCourseMapper.selectById(id);
        if (course != null) ensureCourseOwnerIfTeacher(course, session);
        List<QfAuditLog> logs = qfAuditLogMapper.selectList(
                new LambdaQueryWrapper<QfAuditLog>()
                        .eq(QfAuditLog::getEntityType, 1)
                        .eq(QfAuditLog::getEntityId, id)
                        .orderByDesc(QfAuditLog::getAuditTime)
        );
        return ResultData.success("logs", logs, "课程审核/操作日志");
    }

    @GetMapping("/courses/{id}/contents")
    @RequiresPermission("admin:course:content")
    public Object courseContents(@PathVariable Integer id, HttpSession session) {
        JsCourse course = jsCourseMapper.selectById(id);
        if (course != null) ensureCourseOwnerIfTeacher(course, session);
        List<JsCourseContent> contents = jsCourseContentMapper.selectList(
                new LambdaQueryWrapper<JsCourseContent>()
                        .eq(JsCourseContent::getCourseId, id)
                        .orderByAsc(JsCourseContent::getEpNo)
        );
        return ResultData.success("contents", contents, "课程分集");
    }

    @PostMapping("/courses/{id}/contents")
    @RequiresPermission("admin:course:content")
    public Object addCourseContent(@PathVariable Integer id, @RequestBody JsCourseContent content, HttpSession session) {
        JsCourse course = jsCourseMapper.selectById(id);
        if (course == null) throw new MyException(ErrorType.WRONG_INFO, "课程不存在");
        ensureCourseOwnerIfTeacher(course, session);
        content.setCourseId(id);
        jsCourseContentMapper.insert(content);
        return ResultData.success("content", content, "分集已添加");
    }

    @PutMapping("/course-contents/{id}")
    @RequiresPermission("admin:course:content")
    public Object updateCourseContent(@PathVariable Integer id, @RequestBody JsCourseContent content, HttpSession session) {
        JsCourseContent old = jsCourseContentMapper.selectById(id);
        if (old == null) throw new MyException(ErrorType.WRONG_INFO, "分集不存在");
        JsCourse course = jsCourseMapper.selectById(old.getCourseId());
        if (course != null) ensureCourseOwnerIfTeacher(course, session);
        content.setId(id);
        jsCourseContentMapper.updateById(content);
        return ResultData.success("分集已更新");
    }

    @DeleteMapping("/course-contents/{id}")
    @RequiresPermission("admin:course:content")
    public Object deleteCourseContent(@PathVariable Integer id, HttpSession session) {
        JsCourseContent old = jsCourseContentMapper.selectById(id);
        if (old != null) {
            JsCourse course = jsCourseMapper.selectById(old.getCourseId());
            if (course != null) ensureCourseOwnerIfTeacher(course, session);
        }
        jsCourseContentMapper.deleteById(id);
        return ResultData.success("分集已删除");
    }

    @GetMapping("/course-categories")
    @RequiresPermission("admin:course:category")
    public Object courseCategories(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<JsCourseCategory> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) wrapper.like(JsCourseCategory::getCateName, keyword);
        wrapper.orderByAsc(JsCourseCategory::getParentId).orderByAsc(JsCourseCategory::getSortNum);
        return ResultData.success("categories", jsCourseCategoryMapper.selectList(wrapper), "课程分类");
    }

    @PostMapping("/course-categories")
    @RequiresPermission("admin:course:category:add")
    public Object addCourseCategory(@RequestBody JsCourseCategory category, HttpSession session) {
        category.setCreateBy(currentUser(session).getId());
        jsCourseCategoryMapper.insert(category);
        return ResultData.success("category", category, "课程分类已添加");
    }

    @PutMapping("/course-categories/{id}")
    @RequiresPermission("admin:course:category:update")
    public Object updateCourseCategory(@PathVariable Integer id, @RequestBody JsCourseCategory category, HttpSession session) {
        category.setId(id);
        category.setUpdateBy(currentUser(session).getId());
        jsCourseCategoryMapper.updateById(category);
        return ResultData.success("课程分类已更新");
    }

    @DeleteMapping("/course-categories/{id}")
    @RequiresPermission("admin:course:category:delete")
    public Object deleteCourseCategory(@PathVariable Integer id) {
        jsCourseCategoryMapper.deleteById(id);
        return ResultData.success("课程分类已删除");
    }

    @GetMapping("/course-comments")
    @RequiresPermission("admin:course:comment")
    public Object courseComments(@RequestParam(defaultValue = "1") Long page,
                                 @RequestParam(defaultValue = "10") Long size,
                                 @RequestParam(required = false) Integer courseId,
                                 @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<CoComment> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) wrapper.eq(CoComment::getEntityId, courseId);
        if (hasText(keyword)) wrapper.like(CoComment::getContent, keyword);
        wrapper.orderByDesc(CoComment::getCreateTime);
        Page<CoComment> data = new Page<>(page, size);
        coCommentMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "课程评论列表");
    }
}
