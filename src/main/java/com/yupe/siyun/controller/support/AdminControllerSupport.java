package com.yupe.siyun.controller.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupe.siyun.controller.dto.AuditPayload;
import com.yupe.siyun.entity.JsCourse;
import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.QfAuditLog;
import com.yupe.siyun.mapper.QfAuditLogMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class AdminControllerSupport {

    @Autowired
    protected QfAuditLogMapper qfAuditLogMapper;

    protected ObjBackUser currentUser(HttpSession session) {
        ObjBackUser user = (ObjBackUser) session.getAttribute("backUser");
        if (user == null) {
            throw new MyException(ErrorType.NOT_LOGIN, "后台管理会话失效，请重新登录");
        }
        return user;
    }

    protected Set<String> currentRoles(HttpSession session) {
        Object raw = session.getAttribute("backRoles");
        Set<String> roles = new HashSet<>();
        if (raw instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item != null) roles.add(item.toString());
            }
        }
        Object primary = session.getAttribute("backRole");
        if (primary != null) roles.add(primary.toString());
        return roles;
    }

    protected boolean teacherOnly(HttpSession session) {
        Set<String> roles = currentRoles(session);
        return roles.size() == 1 && roles.contains("TEACHER");
    }

    protected void ensureCourseOwnerIfTeacher(JsCourse course, HttpSession session) {
        if (teacherOnly(session) && !Objects.equals(course.getTeacherId(), currentUser(session).getId())) {
            throw new MyException(ErrorType.PERMISSION_DENIED, "老师只能操作自己的课程");
        }
    }

    protected void fillCourseDefaults(JsCourse course, ObjBackUser user) {
        if (course.getTeacherId() == null) course.setTeacherId(user.getId());
        if (course.getCreateBy() == null) course.setCreateBy(user.getId());
        if (course.getRecommendType() == null) course.setRecommendType(0);
        if (course.getStatusShelf() == null) course.setStatusShelf(0);
        if (course.getStatusAudit() == null) course.setStatusAudit(1);
        if (course.getEpisodeNum() == null) course.setEpisodeNum(1);
        if (course.getKeywords() == null) course.setKeywords(course.getTitle());
    }

    protected void addAuditLog(Integer entityId, Integer entityType, Integer applicantId, Integer result,
                               AuditPayload payload, HttpSession session) {
        QfAuditLog log = new QfAuditLog();
        log.setEntityId(entityId);
        log.setEntityType(entityType);
        log.setApplyTime(LocalDateTime.now());
        log.setApplicantId(applicantId == null ? 0 : applicantId);
        log.setAuditorId(currentUser(session).getId());
        log.setAuditTime(LocalDateTime.now());
        log.setAuditResult(result == null ? 0 : result);
        log.setFeedbackDetail(payload.getFeedbackDetail());
        log.setRemark(payload.getRemark());
        qfAuditLogMapper.insert(log);
    }

    protected Integer bodyInt(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        if (!hasText(value.toString())) return null;
        return Integer.parseInt(value.toString());
    }

    protected String bodyString(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? null : value.toString();
    }

    protected boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    protected BigDecimal decimalResult(List<Object> values) {
        if (values == null || values.isEmpty() || values.get(0) == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(values.get(0).toString());
    }

    protected Integer nextFromMax(List<Object> max) {
        if (max == null || max.isEmpty() || max.get(0) == null) return 1;
        return Integer.parseInt(max.get(0).toString()) + 1;
    }

    protected <T> QueryWrapper<T> maxIdQuery() {
        return new QueryWrapper<T>().select("COALESCE(MAX(id),0)");
    }
}
