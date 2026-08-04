package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.AuditPayload;
import com.yupe.siyun.controller.dto.MomentSavePayload;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.CoComment;
import com.yupe.siyun.entity.JsMomentsArticle;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.CoCommentMapper;
import com.yupe.siyun.mapper.JsMomentsArticleMapper;
import com.yupe.siyun.service.FileService;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
public class AdminMomentController extends AdminControllerSupport {

    @Autowired
    private JsMomentsArticleMapper jsMomentsArticleMapper;
    @Autowired
    private CoCommentMapper coCommentMapper;
    @Autowired
    private FileService fileService;

    @Value("${upload.profile.article.cover.path}")
    private String momentCoverPath;

    @GetMapping("/moments")
    @RequiresPermission("admin:moment:list")
    public Object moments(@RequestParam(defaultValue = "1") Long page,
                          @RequestParam(defaultValue = "10") Long size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) Integer statusShow,
                          HttpSession session) {
        LambdaQueryWrapper<JsMomentsArticle> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) wrapper.and(w -> w.like(JsMomentsArticle::getTitle, keyword).or().like(JsMomentsArticle::getKeywords, keyword));
        if (status != null) wrapper.eq(JsMomentsArticle::getStatus, status);
        if (statusShow != null) wrapper.eq(JsMomentsArticle::getStatusShow, statusShow);
        if (teacherOnly(session)) wrapper.eq(JsMomentsArticle::getAuthorId, currentUser(session).getId());
        wrapper.orderByDesc(JsMomentsArticle::getCreateTime);
        Page<JsMomentsArticle> data = new Page<>(page, size);
        jsMomentsArticleMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "微圈列表");
    }

    @GetMapping("/moments/{id}")
    @RequiresPermission("admin:moment:detail")
    public Object momentDetail(@PathVariable Integer id, HttpSession session) {
        JsMomentsArticle moment = requireMoment(id);
        ensureMomentOwnerIfTeacher(moment, session);
        List<CoComment> comments = coCommentMapper.selectList(
                new LambdaQueryWrapper<CoComment>()
                        .eq(CoComment::getEntityId, id)
                        .eq(CoComment::getEntityType, 2)
                        .orderByDesc(CoComment::getCreateTime)
        );
        return ResultData.success(new String[]{"moment", "comments"}, new Object[]{moment, comments}, "微圈详情");
    }

    @PostMapping("/moments")
    @RequiresPermission("admin:moment:add")
    public Object addMoment(@RequestBody MomentSavePayload payload, HttpSession session) {
        validateMomentPayload(payload);
        JsMomentsArticle moment = new JsMomentsArticle();
        copyEditableFields(payload, moment);
        moment.setAuthorId(currentUser(session).getId());
        moment.setCountView(0);
        moment.setCountLike(0);
        moment.setCountComment(0);
        moment.setCountCollect(0);
        moment.setSortNum(0);
        moment.setStatusShow(1);
        moment.setStatus(2);
        jsMomentsArticleMapper.insert(moment);
        return ResultData.success("moment", moment, "微圈已提交审核");
    }

    @PutMapping("/moments/{id}")
    @RequiresPermission("admin:moment:update")
    public Object updateMoment(@PathVariable Integer id, @RequestBody MomentSavePayload payload, HttpSession session) {
        validateMomentPayload(payload);
        JsMomentsArticle stored = requireMoment(id);
        ensureMomentOwnerIfTeacher(stored, session);
        copyEditableFields(payload, stored);
        stored.setUpdateBy(currentUser(session).getId());
        jsMomentsArticleMapper.updateById(stored);
        return ResultData.success("微圈已更新");
    }

    @DeleteMapping("/moments/{id}")
    @RequiresPermission("admin:moment:delete")
    public Object deleteMoment(@PathVariable Integer id, HttpSession session) {
        JsMomentsArticle moment = requireMoment(id);
        ensureMomentOwnerIfTeacher(moment, session);
        jsMomentsArticleMapper.deleteById(id);
        return ResultData.success("微圈已删除");
    }

    @PostMapping("/moments/{id}/audit")
    @RequiresPermission("admin:moment:audit")
    public Object auditMoment(@PathVariable Integer id, @RequestBody AuditPayload payload, HttpSession session) {
        JsMomentsArticle moment = jsMomentsArticleMapper.selectById(id);
        if (moment == null) throw new MyException(ErrorType.WRONG_INFO, "微圈不存在");
        moment.setStatus(Objects.equals(payload.getAuditResult(), 1) ? 3 : 4);
        if (payload.getStatusShelf() != null) {
            moment.setStatusShow(payload.getStatusShelf());
        }
        moment.setUpdateBy(currentUser(session).getId());
        jsMomentsArticleMapper.updateById(moment);
        return ResultData.success("微圈审核完成");
    }

    @PostMapping("/upload/moment-cover")
    @RequiresPermission("admin:moment:add")
    public Object uploadMomentCover(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择微圈封面图片");
        }
        try {
            String url = fileService.uploadFile(file, momentCoverPath);
            return ResultData.success("coverUrl", url, "微圈封面上传成功");
        } catch (IOException e) {
            throw new MyException(ErrorType.OPERATION_FAILED, "封面上传失败，请检查上传目录权限");
        }
    }

    private JsMomentsArticle requireMoment(Integer id) {
        JsMomentsArticle moment = jsMomentsArticleMapper.selectById(id);
        if (moment == null) {
            throw new MyException(ErrorType.WRONG_INFO, "微圈不存在");
        }
        return moment;
    }

    private void ensureMomentOwnerIfTeacher(JsMomentsArticle moment, HttpSession session) {
        if (teacherOnly(session) && !Objects.equals(moment.getAuthorId(), currentUser(session).getId())) {
            throw new MyException(ErrorType.PERMISSION_DENIED, "老师只能操作自己发布的微圈");
        }
    }

    private void validateMomentPayload(MomentSavePayload payload) {
        if (payload == null || !hasText(payload.getTitle())) {
            throw new MyException(ErrorType.WRONG_INFO, "微圈标题不能为空");
        }
        if (!hasText(payload.getCoverUrl())) {
            throw new MyException(ErrorType.WRONG_INFO, "请上传微圈封面图片");
        }
        if (!hasText(payload.getContent())) {
            throw new MyException(ErrorType.WRONG_INFO, "微圈内容不能为空");
        }
    }

    private void copyEditableFields(MomentSavePayload payload, JsMomentsArticle moment) {
        moment.setTitle(payload.getTitle().trim());
        moment.setKeywords(hasText(payload.getKeywords()) ? payload.getKeywords().trim() : payload.getTitle().trim());
        moment.setCoverUrl(payload.getCoverUrl().trim());
        moment.setContent(payload.getContent().trim());
    }
}
