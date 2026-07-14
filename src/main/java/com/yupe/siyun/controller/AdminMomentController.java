package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.AuditPayload;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.CoComment;
import com.yupe.siyun.entity.JsMomentsArticle;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.CoCommentMapper;
import com.yupe.siyun.mapper.JsMomentsArticleMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
public class AdminMomentController extends AdminControllerSupport {

    @Autowired
    private JsMomentsArticleMapper jsMomentsArticleMapper;
    @Autowired
    private CoCommentMapper coCommentMapper;

    @GetMapping("/moments")
    @RequiresPermission("admin:moment:list")
    public Object moments(@RequestParam(defaultValue = "1") Long page,
                          @RequestParam(defaultValue = "10") Long size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) Integer statusShow) {
        LambdaQueryWrapper<JsMomentsArticle> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) wrapper.and(w -> w.like(JsMomentsArticle::getTitle, keyword).or().like(JsMomentsArticle::getKeywords, keyword));
        if (status != null) wrapper.eq(JsMomentsArticle::getStatus, status);
        if (statusShow != null) wrapper.eq(JsMomentsArticle::getStatusShow, statusShow);
        wrapper.orderByDesc(JsMomentsArticle::getCreateTime);
        Page<JsMomentsArticle> data = new Page<>(page, size);
        jsMomentsArticleMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "微圈列表");
    }

    @GetMapping("/moments/{id}")
    @RequiresPermission("admin:moment:detail")
    public Object momentDetail(@PathVariable Integer id) {
        JsMomentsArticle moment = jsMomentsArticleMapper.selectById(id);
        List<CoComment> comments = coCommentMapper.selectList(
                new LambdaQueryWrapper<CoComment>()
                        .eq(CoComment::getEntityId, id)
                        .orderByDesc(CoComment::getCreateTime)
        );
        return ResultData.success(new String[]{"moment", "comments"}, new Object[]{moment, comments}, "微圈详情");
    }

    @PostMapping("/moments")
    @RequiresPermission("admin:moment:add")
    public Object addMoment(@RequestBody JsMomentsArticle moment, HttpSession session) {
        if (moment.getAuthorId() == null) moment.setAuthorId(currentUser(session).getId());
        if (moment.getCountView() == null) moment.setCountView(0);
        if (moment.getCountLike() == null) moment.setCountLike(0);
        if (moment.getCountComment() == null) moment.setCountComment(0);
        if (moment.getCountCollect() == null) moment.setCountCollect(0);
        if (moment.getSortNum() == null) moment.setSortNum(0);
        if (moment.getStatusShow() == null) moment.setStatusShow(1);
        if (moment.getStatus() == null) moment.setStatus(3);
        jsMomentsArticleMapper.insert(moment);
        return ResultData.success("moment", moment, "微圈已添加");
    }

    @PutMapping("/moments/{id}")
    @RequiresPermission("admin:moment:update")
    public Object updateMoment(@PathVariable Integer id, @RequestBody JsMomentsArticle moment, HttpSession session) {
        moment.setId(id);
        moment.setUpdateBy(currentUser(session).getId());
        jsMomentsArticleMapper.updateById(moment);
        return ResultData.success("微圈已更新");
    }

    @DeleteMapping("/moments/{id}")
    @RequiresPermission("admin:moment:delete")
    public Object deleteMoment(@PathVariable Integer id) {
        jsMomentsArticleMapper.deleteById(id);
        return ResultData.success("微圈已删除");
    }

    @PostMapping("/moments/{id}/audit")
    @RequiresPermission("admin:moment:audit")
    public Object auditMoment(@PathVariable Integer id, @RequestBody AuditPayload payload, HttpSession session) {
        JsMomentsArticle moment = jsMomentsArticleMapper.selectById(id);
        if (moment == null) throw new MyException(ErrorType.WRONG_INFO, "微圈不存在");
        moment.setStatus(Objects.equals(payload.getAuditResult(), 1) ? 3 : 4);
        moment.setUpdateBy(currentUser(session).getId());
        jsMomentsArticleMapper.updateById(moment);
        return ResultData.success("微圈审核完成");
    }
}
