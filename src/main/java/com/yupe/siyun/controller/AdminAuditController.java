package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.CoComment;
import com.yupe.siyun.entity.CoImMessage;
import com.yupe.siyun.entity.CoUserFeedback;
import com.yupe.siyun.entity.JsCourse;
import com.yupe.siyun.entity.JsGoods;
import com.yupe.siyun.entity.JsMomentsArticle;
import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.ObjFrontUser;
import com.yupe.siyun.entity.QfAuditLog;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.BackUserMapper;
import com.yupe.siyun.mapper.CoCommentMapper;
import com.yupe.siyun.mapper.CoImMessageMapper;
import com.yupe.siyun.mapper.CoUserFeedbackMapper;
import com.yupe.siyun.mapper.FrontUserMapper;
import com.yupe.siyun.mapper.JsCourseMapper;
import com.yupe.siyun.mapper.JsGoodsMapper;
import com.yupe.siyun.mapper.JsMomentsArticleMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminAuditController extends AdminControllerSupport {

    @Autowired
    private CoCommentMapper coCommentMapper;
    @Autowired
    private CoUserFeedbackMapper coUserFeedbackMapper;
    @Autowired
    private CoImMessageMapper coImMessageMapper;
    @Autowired
    private FrontUserMapper frontUserMapper;
    @Autowired
    private BackUserMapper backUserMapper;
    @Autowired
    private JsCourseMapper jsCourseMapper;
    @Autowired
    private JsGoodsMapper jsGoodsMapper;
    @Autowired
    private JsMomentsArticleMapper jsMomentsArticleMapper;

    @GetMapping("/comments")
    @RequiresPermission("admin:comment:list")
    public Object comments(@RequestParam(defaultValue = "1") Long page,
                           @RequestParam(defaultValue = "10") Long size,
                           @RequestParam(required = false) Integer entityId,
                           @RequestParam(required = false) Integer entityType,
                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<CoComment> wrapper = new LambdaQueryWrapper<>();
        if (entityId != null) wrapper.eq(CoComment::getEntityId, entityId);
        if (entityType != null) wrapper.eq(CoComment::getEntityType, entityType);
        if (hasText(keyword)) wrapper.like(CoComment::getContent, keyword.trim());
        wrapper.orderByDesc(CoComment::getCreateTime).orderByDesc(CoComment::getId);
        Page<CoComment> source = new Page<>(page, size);
        coCommentMapper.selectPage(source, wrapper);

        Set<Integer> userIds = source.getRecords().stream()
                .map(CoComment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Integer> courseIds = source.getRecords().stream()
                .filter(item -> Objects.equals(item.getEntityType(), 0))
                .map(CoComment::getEntityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Integer> momentIds = source.getRecords().stream()
                .filter(item -> Objects.equals(item.getEntityType(), 2))
                .map(CoComment::getEntityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, ObjFrontUser> users = frontUsers(userIds);
        Map<Integer, JsCourse> courses = courseIds.isEmpty()
                ? Collections.emptyMap()
                : jsCourseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(JsCourse::getId, Function.identity()));
        Map<Integer, JsMomentsArticle> moments = momentIds.isEmpty()
                ? Collections.emptyMap()
                : jsMomentsArticleMapper.selectBatchIds(momentIds).stream()
                .collect(Collectors.toMap(JsMomentsArticle::getId, Function.identity()));

        List<Map<String, Object>> records = new ArrayList<>();
        for (CoComment comment : source.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", comment.getId());
            item.put("entityTitle", commentTitle(comment, courses, moments));
            item.put("userNickname", frontUserName(users.get(comment.getUserId()), comment.getUserId()));
            item.put("content", comment.getContent());
            item.put("countLike", comment.getCountLike() == null ? 0 : comment.getCountLike());
            records.add(item);
        }
        return ResultData.success("page", pageView(source, records), "评论列表");
    }

    @DeleteMapping("/comments/{id}")
    @RequiresPermission("admin:comment:delete")
    public Object deleteComment(@PathVariable Integer id) {
        coCommentMapper.deleteById(id);
        return ResultData.success("评论已删除/拦截");
    }

    @GetMapping("/audit-logs")
    @RequiresPermission("admin:audit:log")
    public Object auditLogs(@RequestParam(defaultValue = "1") Long page,
                            @RequestParam(defaultValue = "10") Long size,
                            @RequestParam(required = false) Integer entityType,
                            @RequestParam(required = false) Integer entityId) {
        LambdaQueryWrapper<QfAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (entityType != null) wrapper.eq(QfAuditLog::getEntityType, entityType);
        if (entityId != null) wrapper.eq(QfAuditLog::getEntityId, entityId);
        wrapper.orderByDesc(QfAuditLog::getAuditTime).orderByDesc(QfAuditLog::getId);
        Page<QfAuditLog> source = new Page<>(page, size);
        qfAuditLogMapper.selectPage(source, wrapper);

        Set<Integer> courseIds = source.getRecords().stream()
                .filter(item -> Objects.equals(item.getEntityType(), 1))
                .map(QfAuditLog::getEntityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Integer> goodsIds = source.getRecords().stream()
                .filter(item -> Objects.equals(item.getEntityType(), 2))
                .map(QfAuditLog::getEntityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, JsCourse> courses = courseIds.isEmpty()
                ? Collections.emptyMap()
                : jsCourseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(JsCourse::getId, Function.identity()));
        Map<Integer, JsGoods> goods = goodsIds.isEmpty()
                ? Collections.emptyMap()
                : jsGoodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(JsGoods::getId, Function.identity()));

        Set<Integer> backUserIds = new LinkedHashSet<>();
        Set<Integer> frontUserIds = new LinkedHashSet<>();
        for (QfAuditLog log : source.getRecords()) {
            if (log.getAuditorId() != null) backUserIds.add(log.getAuditorId());
            JsCourse course = courses.get(log.getEntityId());
            if (Objects.equals(log.getEntityType(), 1) && course != null && course.getFrontCreatorId() != null) {
                frontUserIds.add(course.getFrontCreatorId());
            } else if (log.getApplicantId() != null) {
                backUserIds.add(log.getApplicantId());
            }
        }
        Map<Integer, ObjBackUser> backUsers = backUsers(backUserIds);
        Map<Integer, ObjFrontUser> frontUsers = frontUsers(frontUserIds);

        List<Map<String, Object>> records = new ArrayList<>();
        for (QfAuditLog log : source.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", log.getId());
            item.put("entityName", auditEntityName(log, courses, goods));
            item.put("entityTypeName", auditEntityTypeName(log.getEntityType()));
            item.put("applicantName", auditApplicantName(log, courses, frontUsers, backUsers));
            item.put("auditorName", backUserName(backUsers.get(log.getAuditorId()), log.getAuditorId()));
            item.put("auditResult", log.getAuditResult());
            item.put("feedbackDetail", log.getFeedbackDetail());
            item.put("auditTime", log.getAuditTime());
            records.add(item);
        }
        return ResultData.success("page", pageView(source, records), "审核日志");
    }

    @GetMapping("/feedback")
    @RequiresPermission("admin:feedback:list")
    public Object feedback(@RequestParam(defaultValue = "1") Long page,
                           @RequestParam(defaultValue = "10") Long size,
                           @RequestParam(required = false) Integer status,
                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<CoUserFeedback> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(CoUserFeedback::getStatus, status);
        if (hasText(keyword)) wrapper.like(CoUserFeedback::getContent, keyword.trim());
        wrapper.orderByDesc(CoUserFeedback::getCreateTime).orderByDesc(CoUserFeedback::getId);
        Page<CoUserFeedback> source = new Page<>(page, size);
        coUserFeedbackMapper.selectPage(source, wrapper);

        Set<Integer> userIds = source.getRecords().stream()
                .map(CoUserFeedback::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, ObjFrontUser> users = frontUsers(userIds);
        List<Map<String, Object>> records = new ArrayList<>();
        for (CoUserFeedback feedback : source.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", feedback.getId());
            item.put("userNickname", frontUserName(users.get(feedback.getUserId()), feedback.getUserId()));
            item.put("feedbackType", feedback.getFeedbackType());
            item.put("content", feedback.getContent());
            item.put("status", feedback.getStatus());
            item.put("remark", feedback.getRemark());
            item.put("createTime", feedback.getCreateTime());
            records.add(item);
        }
        return ResultData.success("page", pageView(source, records), "问题反馈列表");
    }

    @PutMapping("/feedback/{id}")
    @RequiresPermission("admin:feedback:reply")
    public Object updateFeedback(@PathVariable Integer id,
                                 @RequestBody Map<String, Object> body,
                                 HttpSession session) {
        CoUserFeedback feedback = coUserFeedbackMapper.selectById(id);
        if (feedback == null) throw new MyException(ErrorType.WRONG_INFO, "反馈记录不存在");
        Integer status = bodyInt(body, "status");
        if (status == null || status < 1 || status > 4) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择正确的处理状态");
        }
        feedback.setStatus(status);
        feedback.setRemark(bodyString(body, "remark"));
        feedback.setUpdateBy(currentUser(session).getId());
        feedback.setUpdateTime(LocalDateTime.now());
        coUserFeedbackMapper.updateById(feedback);
        return ResultData.success("反馈处理状态已更新");
    }

    @GetMapping("/service/conversations")
    @RequiresPermission("admin:service:list")
    public Object serviceConversations() {
        List<CoImMessage> messages = coImMessageMapper.selectList(
                new LambdaQueryWrapper<CoImMessage>()
                        .and(query -> query.eq(CoImMessage::getSenderId, 0)
                                .or()
                                .eq(CoImMessage::getReceiverId, 0))
                        .orderByDesc(CoImMessage::getSendTime)
                        .orderByDesc(CoImMessage::getId)
        );

        Set<Integer> userIds = messages.stream()
                .map(this::serviceUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Integer, ObjFrontUser> users = frontUsers(userIds);
        Map<Integer, Map<String, Object>> conversations = new LinkedHashMap<>();
        for (CoImMessage message : messages) {
            Integer userId = serviceUserId(message);
            if (userId == null || userId <= 0) continue;
            Map<String, Object> item = conversations.computeIfAbsent(userId, key -> {
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("userId", key);
                value.put("userNickname", frontUserName(users.get(key), key));
                value.put("lastMessage", message.getContent());
                value.put("lastTime", message.getSendTime());
                value.put("unreadCount", 0);
                return value;
            });
            if (Objects.equals(message.getSenderId(), userId) && Objects.equals(message.getReceiverId(), 0)
                    && !Objects.equals(message.getIsRead(), 1)) {
                item.put("unreadCount", ((Integer) item.get("unreadCount")) + 1);
            }
        }
        return ResultData.success("conversations", new ArrayList<>(conversations.values()), "客服会话列表");
    }

    @GetMapping("/service/conversations/{userId}/messages")
    @RequiresPermission("admin:service:list")
    @Transactional
    public Object serviceMessages(@PathVariable Integer userId) {
        ObjFrontUser user = frontUserMapper.selectById(userId);
        if (user == null) throw new MyException(ErrorType.WRONG_INFO, "用户不存在");
        List<CoImMessage> messages = coImMessageMapper.selectList(
                new LambdaQueryWrapper<CoImMessage>()
                        .and(query -> query
                                .and(sent -> sent.eq(CoImMessage::getSenderId, userId)
                                        .eq(CoImMessage::getReceiverId, 0))
                                .or(received -> received.eq(CoImMessage::getSenderId, 0)
                                        .eq(CoImMessage::getReceiverId, userId)))
                        .orderByAsc(CoImMessage::getSendTime)
                        .orderByAsc(CoImMessage::getId)
        );
        for (CoImMessage message : messages) {
            if (Objects.equals(message.getSenderId(), userId)
                    && Objects.equals(message.getReceiverId(), 0)
                    && !Objects.equals(message.getIsRead(), 1)) {
                CoImMessage readState = new CoImMessage();
                readState.setId(message.getId());
                readState.setIsRead(1);
                coImMessageMapper.updateById(readState);
                message.setIsRead(1);
            }
        }
        Map<String, Object> userView = new LinkedHashMap<>();
        userView.put("id", user.getId());
        userView.put("nickname", frontUserName(user, userId));
        userView.put("tel", user.getStuTel());
        return ResultData.success(
                new String[]{"messages", "user"},
                new Object[]{messages, userView},
                "客服消息查询成功"
        );
    }

    @PostMapping("/service/conversations/{userId}/messages")
    @RequiresPermission("admin:service:reply")
    public Object replyServiceMessage(@PathVariable Integer userId,
                                      @RequestBody Map<String, Object> body,
                                      HttpSession session) {
        ObjFrontUser receiver = frontUserMapper.selectById(userId);
        if (receiver == null) throw new MyException(ErrorType.WRONG_INFO, "用户不存在");
        String content = bodyString(body, "content");
        if (!hasText(content)) throw new MyException(ErrorType.WRONG_INFO, "请输入回复内容");
        content = content.trim();
        if (content.length() > 500) throw new MyException(ErrorType.WRONG_INFO, "消息不能超过500字");

        ObjBackUser operator = currentUser(session);
        CoImMessage message = new CoImMessage();
        message.setSenderId(0);
        message.setSenderName(hasText(operator.getName()) ? operator.getName() : "在线客服");
        message.setReceiverId(userId);
        message.setReceiverName(frontUserName(receiver, userId));
        message.setContent(content);
        message.setSendTime(LocalDateTime.now());
        message.setIsRead(0);
        coImMessageMapper.insert(message);
        return ResultData.success("message", message, "回复发送成功");
    }

    private String commentTitle(CoComment comment,
                                Map<Integer, JsCourse> courses,
                                Map<Integer, JsMomentsArticle> moments) {
        if (Objects.equals(comment.getEntityType(), 0)) {
            JsCourse course = courses.get(comment.getEntityId());
            return course == null ? "课程已删除" : course.getTitle();
        }
        if (Objects.equals(comment.getEntityType(), 2)) {
            JsMomentsArticle moment = moments.get(comment.getEntityId());
            return moment == null ? "微圈已删除" : moment.getTitle();
        }
        return "未知内容";
    }

    private String auditEntityName(QfAuditLog log,
                                   Map<Integer, JsCourse> courses,
                                   Map<Integer, JsGoods> goods) {
        if (Objects.equals(log.getEntityType(), 1)) {
            JsCourse course = courses.get(log.getEntityId());
            return course == null ? "课程已删除" : course.getTitle();
        }
        if (Objects.equals(log.getEntityType(), 2)) {
            JsGoods item = goods.get(log.getEntityId());
            return item == null ? "商品已删除" : item.getGoodsName();
        }
        return "未知内容";
    }

    private String auditEntityTypeName(Integer entityType) {
        if (Objects.equals(entityType, 1)) return "视频课程";
        if (Objects.equals(entityType, 2)) return "商品";
        return "未知";
    }

    private String auditApplicantName(QfAuditLog log,
                                      Map<Integer, JsCourse> courses,
                                      Map<Integer, ObjFrontUser> frontUsers,
                                      Map<Integer, ObjBackUser> backUsers) {
        JsCourse course = courses.get(log.getEntityId());
        if (Objects.equals(log.getEntityType(), 1) && course != null && course.getFrontCreatorId() != null) {
            return frontUserName(frontUsers.get(course.getFrontCreatorId()), course.getFrontCreatorId());
        }
        return backUserName(backUsers.get(log.getApplicantId()), log.getApplicantId());
    }

    private Integer serviceUserId(CoImMessage message) {
        return Objects.equals(message.getSenderId(), 0) ? message.getReceiverId() : message.getSenderId();
    }

    private Map<Integer, ObjFrontUser> frontUsers(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return frontUserMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(ObjFrontUser::getId, Function.identity()));
    }

    private Map<Integer, ObjBackUser> backUsers(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return backUserMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(ObjBackUser::getId, Function.identity()));
    }

    private String frontUserName(ObjFrontUser user, Integer userId) {
        if (user == null) return "用户#" + (userId == null ? "-" : userId);
        if (hasText(user.getNickName())) return user.getNickName();
        if (hasText(user.getStuTel())) return user.getStuTel();
        return "用户#" + user.getId();
    }

    private String backUserName(ObjBackUser user, Integer userId) {
        if (user == null) return "人员#" + (userId == null ? "-" : userId);
        if (hasText(user.getName())) return user.getName();
        if (hasText(user.getTel())) return user.getTel();
        return "人员#" + user.getId();
    }

    private <T> Page<Map<String, Object>> pageView(Page<T> source, List<Map<String, Object>> records) {
        Page<Map<String, Object>> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(records);
        return result;
    }
}
