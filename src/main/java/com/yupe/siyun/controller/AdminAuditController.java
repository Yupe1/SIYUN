package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.CoComment;
import com.yupe.siyun.entity.QfAuditLog;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.CoCommentMapper;
import com.yupe.siyun.util.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminAuditController extends AdminControllerSupport {

    @Autowired
    private CoCommentMapper coCommentMapper;

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
        if (hasText(keyword)) wrapper.like(CoComment::getContent, keyword);
        wrapper.orderByDesc(CoComment::getCreateTime);
        Page<CoComment> data = new Page<>(page, size);
        coCommentMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "评论列表");
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
        wrapper.orderByDesc(QfAuditLog::getAuditTime);
        Page<QfAuditLog> data = new Page<>(page, size);
        qfAuditLogMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "审核日志");
    }
}
