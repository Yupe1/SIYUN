package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.yupe.siyun.controller.dto.CourseCreatePayload;
import com.yupe.siyun.entity.*;
import com.yupe.siyun.mapper.*;
import com.yupe.siyun.service.CourseVOService;
import com.yupe.siyun.service.FileService;
import com.yupe.siyun.service.FrontCouponService;
import com.yupe.siyun.service.FrontUserService;
import com.yupe.siyun.service.FrontWalletService;
import com.yupe.siyun.service.MomentArticleService;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/siyun")
public class FrontFunctionController {
    @Autowired
    private CourseVOService courseVOService;
    @Autowired
    private OpCouponVOMapper opCouponVOMapper;
    @Autowired
    private ApxCoursePlayLogMapper apxCoursePlayLogMapper;
    @Autowired
    private ApxCourseLikeLogMapper apxCourseLikeLogMapper;
    @Autowired
    private ApxCourseCollectLogMapper apxCourseCollectLogMapper;
    @Autowired
    private ApxCourseShareLogMapper apxCourseShareLogMapper;
    @Autowired
    private CoCommentMapper coCommentMapper;
    @Autowired
    private MomentArticleService momentArticleService;
    @Autowired
    private FrontUserService frontUserService;
    @Autowired
    private QfPositionApplyMapper qfPositionApplyMapper;
    @Autowired
    private JsCourseContentMapper jsCourseContentMapper;
    @Autowired
    private JsCourseMapper jsCourseMapper;
    @Autowired
    private JsCourseCategoryMapper jsCourseCategoryMapper;
    @Autowired
    private JsGoodsMapper jsGoodsMapper;
    @Autowired
    private JsGoodsVOMapper jsGoodsVOMapper;
    @Autowired
    private JsGoodsCategoryMapper jsGoodsCategoryMapper;
    @Autowired
    private OpCircleAdMapper opCircleAdMapper;
    @Autowired
    private OpOrderMapper opOrderMapper;
    @Autowired
    private OpCouponUserMapper opCouponUserMapper;
    @Autowired
    private OpFrontUserWalletMapper opFrontUserWalletMapper;
    @Autowired
    private CoUserFeedbackMapper coUserFeedbackMapper;
    @Autowired
    private CoImMessageMapper coImMessageMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private FrontCouponService frontCouponService;
    @Autowired
    private FrontWalletService frontWalletService;

    @Value("${upload.profile.course.video.path}")
    private String courseVideoPath;
    @Value("${upload.profile.course.cover.path}")
    private String courseCoverPath;
    @Value("${upload.profile.article.cover.path}")
    private String momentImagePath;

    private static final long MAX_CREATOR_VIDEO_SIZE = 500L * 1024 * 1024;
    private static final Set<String> CREATOR_VIDEO_SUFFIXES = Set.of(
            ".mp4", ".mov", ".mkv", ".avi", ".flv", ".wmv", ".webm"
    );
    private static final Set<String> MOMENT_IMAGE_SUFFIXES = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    //课程
    //关键词搜索课程
    @GetMapping("/course")
    public Object searchCourse(@RequestParam(required = false) String keywords, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        List<JsCourseVO> courses = courseVOService.search(keywords);
        courses.forEach(c->c.setVideoUrl(null));//不确定是否购买，对核心资源脱敏
        return ResultData.success("courses",courses,"查询成功");
    }
    @GetMapping("/course/{id}")
    public Object courseDetail(@PathVariable Integer id) {
        JsCourseVO course = courseVOService.getById(id);
        if (course == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程不存在或已下架");
        }
        course.setVideoUrl(null);
        return ResultData.success("course", course, "课程详情查询成功");
    }
    //搜索某一课程的子分集内容
    @GetMapping("/course/content")
    public Object searchContent(@RequestParam Integer courseId, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        JsCourse sourceCourse = jsCourseMapper.selectById(courseId);
        boolean isCreatorOwner = sourceCourse != null
                && Objects.equals(sourceCourse.getFrontCreatorId(), frontUser.getId());
        if (!isCreatorOwner && !courseVOService.hasPurchased(frontUser, courseId)) {
            throw new MyException(ErrorType.UNAUTHORIZED,"请先购买");
        }
        LambdaQueryWrapper<JsCourseContent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JsCourseContent::getCourseId,courseId)
                .orderByAsc(JsCourseContent::getEpNo);
        List<JsCourseContent> ep = jsCourseContentMapper.selectList(queryWrapper);
        return ResultData.success("ep",ep,"content loaded");
    }
    //购买课程添加订单
    @PostMapping("/order")
    public Object purchace(@RequestBody Map<String, Object> payload, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        Integer courseId = bodyInt(payload, "id");
        if (courseId == null) {
            courseId = bodyInt(payload, "courseId");
        }
        JsCourse jsCourse = new JsCourse();
        jsCourse.setId(courseId);
        courseVOService.purchase(frontUser, jsCourse, bodyInt(payload, "couponUserId"));
        return ResultData.success("购买成功");
    }
    // 查询课程是否已购买
    @GetMapping("/orderStatus")
    public Object orderStatus(@RequestParam Integer courseId, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        boolean purchased = courseVOService.hasPurchased(frontUser, courseId);
        return ResultData.success("purchased", purchased, "查询成功");
    }
    // 查询课程点赞状态
    @GetMapping("/likeStatus")
    public Object likeStatus(@RequestParam Integer courseId, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<ApxCourseLikeLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApxCourseLikeLog::getUserId, frontUser.getId())
                .eq(ApxCourseLikeLog::getCourseId, courseId);
        boolean liked = apxCourseLikeLogMapper.selectCount(queryWrapper) > 0;
        return ResultData.success("liked", liked, "查询成功");
    }
    // 查询课程收藏状态
    @GetMapping("/collectStatus")
    public Object collectStatus(@RequestParam Integer courseId, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<ApxCourseCollectLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApxCourseCollectLog::getUserId, frontUser.getId())
                .eq(ApxCourseCollectLog::getCourseId, courseId);
        boolean collected = apxCourseCollectLogMapper.selectCount(queryWrapper) > 0;
        return ResultData.success("collected", collected, "查询成功");
    }
    //查询优惠卷 用户-商品
    @GetMapping("/coupons")
    public Object getMyCoupon(HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        return ResultData.success(
                "couponForOccassion",
                frontCouponService.myCoupons(frontUser.getId()),
                "优惠券刷新成功"
        );
    }

    @GetMapping("/coupons/available")
    public Object availableCoupons(@RequestParam Integer targetType,
                                   @RequestParam Integer targetId,
                                   @RequestParam(required = false, defaultValue = "1") Integer quantity,
                                   HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        int safeQuantity = quantity == null ? 1 : quantity;
        if (safeQuantity < 1 || safeQuantity > 99) {
            throw new MyException(ErrorType.WRONG_INFO, "购买数量必须在1到99之间");
        }
        BigDecimal total;
        if (Integer.valueOf(FrontCouponService.TARGET_GOODS).equals(targetType)) {
            JsGoods goods = jsGoodsMapper.selectById(targetId);
            if (goods == null || !Integer.valueOf(2).equals(goods.getStatus()) || goods.getPriceOriginal() == null) {
                throw new MyException(ErrorType.WRONG_INFO, "商品不存在、已下架或价格未配置");
            }
            total = goods.getPriceOriginal().multiply(BigDecimal.valueOf(safeQuantity));
        } else if (Integer.valueOf(FrontCouponService.TARGET_COURSE).equals(targetType)) {
            JsCourseVO course = courseVOService.getById(targetId);
            if (course == null || course.getPriceOriginal() == null) {
                throw new MyException(ErrorType.WRONG_INFO, "课程不存在、已下架或价格未配置");
            }
            total = course.getPriceOriginal();
        } else {
            throw new MyException(ErrorType.WRONG_INFO, "优惠券适用对象参数错误");
        }
        List<Map<String, Object>> coupons = frontCouponService.availableCoupons(
                frontUser.getId(), targetType, targetId, total
        );
        return ResultData.success("coupons", coupons, "可用优惠券查询成功");
    }
    //某次学习开始 //log存pinia 结束时查ID改log
    @PostMapping("/startplay")
    public Object startPlay(@RequestBody JsCourse jsCourse, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        ApxCoursePlayLog log = courseVOService.startPlay(frontUser,jsCourse);
        return ResultData.success("playLog",log,"startplay");
    }
    //某次学习结束
    @PatchMapping("/stopplay")
    @Transactional
    public Object stopPlay(@RequestBody ApxCoursePlayLog log, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        if (log == null || log.getId() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "缺少播放记录");
        }
        ApxCoursePlayLog currentLog = apxCoursePlayLogMapper.selectById(log.getId());
        if (currentLog == null || !Objects.equals(currentLog.getUserId(), frontUser.getId())) {
            throw new MyException(ErrorType.WRONG_INFO, "播放记录不存在");
        }
        LocalDateTime endTime = LocalDateTime.now();
        long elapsedSeconds = currentLog.getStartTime() == null
                ? 0
                : Duration.between(currentLog.getStartTime(), endTime).getSeconds();
        elapsedSeconds = Math.max(0, Math.min(elapsedSeconds, 12L * 60 * 60));
        int studyMinutes = elapsedSeconds <= 0 ? 0 : Math.max(1, (int) Math.ceil(elapsedSeconds / 60D));

        int settled = apxCoursePlayLogMapper.update(
                null,
                new LambdaUpdateWrapper<ApxCoursePlayLog>()
                        .eq(ApxCoursePlayLog::getId, currentLog.getId())
                        .eq(ApxCoursePlayLog::getUserId, frontUser.getId())
                        .isNull(ApxCoursePlayLog::getEndTime)
                        .set(ApxCoursePlayLog::getEndTime, endTime)
        );
        if (settled != 1) {
            return ResultData.success("studyMinutes", 0, "本次学习已结算");
        }

        if (studyMinutes > 0) {
            frontUserService.update(
                    new LambdaUpdateWrapper<ObjFrontUser>()
                            .eq(ObjFrontUser::getId, frontUser.getId())
                            .setSql("study_duration = COALESCE(study_duration, 0) + " + studyMinutes)
            );
            frontUser.setStudyDuration((frontUser.getStudyDuration() == null ? 0 : frontUser.getStudyDuration()) + studyMinutes);
            session.setAttribute("student", frontUser);
        }
        return ResultData.success("studyMinutes", studyMinutes, "本次学习已结算");
    }
    //课程点赞+-
    @PostMapping("/like")
    public Object like(@RequestBody JsCourse jsCourse, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<ApxCourseLikeLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApxCourseLikeLog::getUserId,frontUser.getId())
                .eq(ApxCourseLikeLog::getCourseId,jsCourse.getId());
        Long count = apxCourseLikeLogMapper.selectCount(queryWrapper);
        if (count == 0) {
            ApxCourseLikeLog log = new ApxCourseLikeLog();
            log.setCourseId(jsCourse.getId());
            log.setUserId(frontUser.getId());
            apxCourseLikeLogMapper.insert(log);
        }else{
            apxCourseLikeLogMapper.delete(queryWrapper);
        }
        return ResultData.success("like +-op success");
    }
    //课程收藏+-
    @PostMapping("/collect")
    public Object collect(@RequestBody JsCourse jsCourse, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        if (jsCourse == null || jsCourse.getId() == null || courseVOService.getById(jsCourse.getId()) == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程不存在");
        }
        LambdaQueryWrapper<ApxCourseCollectLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApxCourseCollectLog::getUserId,frontUser.getId())
                .eq(ApxCourseCollectLog::getCourseId,jsCourse.getId());
        Long count = apxCourseCollectLogMapper.selectCount(queryWrapper);
        boolean collected;
        if (count == 0) {
            ApxCourseCollectLog log = new ApxCourseCollectLog();
            log.setCourseId(jsCourse.getId());
            log.setUserId(frontUser.getId());
            apxCourseCollectLogMapper.insert(log);
            collected = true;
        }else{
            apxCourseCollectLogMapper.delete(queryWrapper);
            collected = false;
        }
        long countCollect = apxCourseCollectLogMapper.selectCount(
                new LambdaQueryWrapper<ApxCourseCollectLog>()
                        .eq(ApxCourseCollectLog::getCourseId, jsCourse.getId())
        );
        return ResultData.success(
                new String[]{"collected", "countCollect"},
                new Object[]{collected, countCollect},
                collected ? "收藏成功" : "已取消收藏"
        );
    }
    //课程搜索我的收藏
    @GetMapping("/collect")
    public Object myCollect(HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<ApxCourseCollectLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApxCourseCollectLog::getUserId,frontUser.getId())
                .select(ApxCourseCollectLog::getCourseId);
        List<ApxCourseCollectLog> logs = apxCourseCollectLogMapper.selectList(queryWrapper);
        // 收藏log提取 course_id 集合
        List<Integer> courseIds = logs.stream()
                .map(ApxCourseCollectLog::getCourseId)
                .collect(Collectors.toList());
        // 没有收藏记录就直接返回空
        if (CollectionUtils.isEmpty(courseIds)) {
            return Collections.emptyList();
        }
        // 用 course_id 列表去查 JsCourseVO
        LambdaQueryWrapper<JsCourseVO> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.in(JsCourseVO::getId, courseIds);
        List<JsCourseVO> courses = courseVOService.list(courseWrapper);
        return ResultData.success("myCollect",courses,"get myCollect");
    }
    //课程分享
    @PostMapping({"/courseShare", "/share"})
    public Object share(@RequestBody JsCourse jsCourse, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        if (jsCourse == null || jsCourse.getId() == null || courseVOService.getById(jsCourse.getId()) == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程不存在");
        }
        ApxCourseShareLog log = new ApxCourseShareLog();
        log.setCourseId(jsCourse.getId());
        log.setUserId(frontUser.getId());
        apxCourseShareLogMapper.insert(log);
        return ResultData.success("share success");
    }
    //课程评论
    @PostMapping("/comment")
    @Transactional
    public Object addComment(@RequestBody CoComment comment, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        validateComment(comment);
        int parentId = comment.getParentId() == null ? 0 : comment.getParentId();
        if (parentId != 0) {
            CoComment parent = coCommentMapper.selectById(parentId);
            if (parent == null
                    || !Objects.equals(parent.getEntityId(), comment.getEntityId())
                    || !Objects.equals(parent.getEntityType(), comment.getEntityType())) {
                throw new MyException(ErrorType.COMMENT_NOT_EXIST, "回复的评论不存在");
            }
            parent.setCountReply((parent.getCountReply() == null ? 0 : parent.getCountReply()) + 1);
            coCommentMapper.updateById(parent);
        }
        comment.setId(null);
        comment.setUserId(frontUser.getId());
        comment.setParentId(parentId);
        comment.setContent(comment.getContent().trim());
        comment.setStatusShow(1);
        comment.setCountLike(0);
        comment.setCountReply(0);
        coCommentMapper.insert(comment);
        adjustMomentCommentCount(comment.getEntityType(), comment.getEntityId(), 1);
        return ResultData.success("comment", comment, "评论成功");
    }
    //加载评论区0
    @GetMapping("/comment")
    public Object comment(@RequestParam Integer id,
                          @RequestParam(required = false) Integer entityType) {
        LambdaQueryWrapper<CoComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoComment::getEntityId, id)
                .eq(entityType != null, CoComment::getEntityType, entityType)
                .eq(CoComment::getParentId, 0)
                .orderByDesc(CoComment::getCreateTime)
                .orderByDesc(CoComment::getId);
        List<CoComment> comments = coCommentMapper.selectList(queryWrapper);
        return ResultData.success("commentList",comments,"comment loaded");
    }
    //加载子评论
    @GetMapping("/subComment")
    public Object subComment(@RequestParam Integer id) {
        LambdaQueryWrapper<CoComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoComment::getParentId, id)
                .orderByAsc(CoComment::getCreateTime)
                .orderByAsc(CoComment::getId);
        List<CoComment> comments = coCommentMapper.selectList(queryWrapper);
        return ResultData.success("commentList",comments,"sub-comment loaded");
    }
    //删除自己评论
    @DeleteMapping("/comment")
    @Transactional
    public Object deleteMy(@RequestBody CoComment comment, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        if (comment == null || comment.getId() == null) {
            throw new MyException(ErrorType.COMMENT_NOT_EXIST, "评论不存在");
        }
        CoComment stored = coCommentMapper.selectById(comment.getId());
        if (stored == null) {
            throw new MyException(ErrorType.COMMENT_NOT_EXIST, "评论不存在");
        }
        if (!Objects.equals(stored.getUserId(), frontUser.getId())) {
            throw new MyException(ErrorType.UNAUTHORIZED,"只能删除自己的评论");
        }
        List<CoComment> children = coCommentMapper.selectList(
                new LambdaQueryWrapper<CoComment>().eq(CoComment::getParentId, stored.getId())
        );
        for (CoComment child : children) {
            coCommentMapper.deleteById(child.getId());
        }
        coCommentMapper.deleteById(stored.getId());
        if (stored.getParentId() != null && stored.getParentId() != 0) {
            CoComment parent = coCommentMapper.selectById(stored.getParentId());
            if (parent != null) {
                parent.setCountReply(Math.max(0, (parent.getCountReply() == null ? 0 : parent.getCountReply()) - 1));
                coCommentMapper.updateById(parent);
            }
        }
        adjustMomentCommentCount(stored.getEntityType(), stored.getEntityId(), -(children.size() + 1));
        return ResultData.success("delete comment success");
    }


//微圈
    //查看单个微圈
    @GetMapping("/moment/{id}")
    public Object getMoment(@PathVariable Integer id) {
        JsMomentsArticle moment = momentArticleService.getById(id);
        if (moment != null) {
            Long commentCount = coCommentMapper.selectCount(
                    new LambdaQueryWrapper<CoComment>()
                            .eq(CoComment::getEntityId, id)
                            .eq(CoComment::getEntityType, 2)
            );
            moment.setCountComment(commentCount.intValue());
        }
        return ResultData.success("moment",moment,"one moments loaded");
    }
    //聚合搜索微圈
    @GetMapping("/moments")
    public Object searchMoments(@RequestParam(required = false) String keywords) {
        List<JsMomentsArticle> moments = momentArticleService.search(keywords);
        return ResultData.success("moments",moments,"moments loaded");
    }
    //搜索自己的微圈
    @GetMapping("/myMoments")
    public Object searchMyMoments(HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<JsMomentsArticle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JsMomentsArticle::getAuthorId, frontUser.getId());
        List<JsMomentsArticle> moments = momentArticleService.list(queryWrapper);
        return ResultData.success("myMoments",moments,"my moments loaded");
    }
    //登录用户均可发布微圈
    @PostMapping("/moment")
    public Object addMoment(@RequestBody JsMomentsArticle moment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        moment.setAuthorId(frontUser.getId());
        momentArticleService.save(moment);
        return ResultData.success("add moments success");
    }
    //删除自己微圈
    @DeleteMapping("/moment")
    public Object deleteMoment(@RequestBody JsMomentsArticle moment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        if (moment == null || moment.getId() == null) {
            throw new MyException(ErrorType.WRONG_INFO,"微圈不存在");
        }
        JsMomentsArticle storedMoment = momentArticleService.getById(moment.getId());
        if (storedMoment == null) {
            throw new MyException(ErrorType.WRONG_INFO,"微圈不存在");
        }
        if (!Objects.equals(storedMoment.getAuthorId(), frontUser.getId())) {
            throw new MyException(ErrorType.UNAUTHORIZED,"您无权操作");
        }
        momentArticleService.removeById(storedMoment.getId());
        return ResultData.success("delete moments success");
    }

    @GetMapping("/moment/collectStatus")
    public Object momentCollectStatus(@RequestParam Integer momentId, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        if (momentId == null || momentArticleService.getById(momentId) == null) {
            throw new MyException(ErrorType.WRONG_INFO, "微圈不存在");
        }
        boolean collected = apxCourseCollectLogMapper.selectCount(
                new LambdaQueryWrapper<ApxCourseCollectLog>()
                        .eq(ApxCourseCollectLog::getUserId, frontUser.getId())
                        .eq(ApxCourseCollectLog::getCourseId, momentId)
        ) > 0;
        return ResultData.success("collected", collected, "查询成功");
    }

    @GetMapping("/moment/likeStatus")
    public Object momentLikeStatus(@RequestParam Integer momentId, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        if (momentId == null || momentArticleService.getById(momentId) == null) {
            throw new MyException(ErrorType.WRONG_INFO, "微圈不存在");
        }
        boolean liked = apxCourseLikeLogMapper.selectCount(
                new LambdaQueryWrapper<ApxCourseLikeLog>()
                        .eq(ApxCourseLikeLog::getUserId, frontUser.getId())
                        .eq(ApxCourseLikeLog::getCourseId, momentId)
        ) > 0;
        return ResultData.success("liked", liked, "查询成功");
    }

    //收藏+-
    @PostMapping("/collectMoment")
    public Object collectMoment(@RequestBody JsMomentsArticle moment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        if (moment == null || moment.getId() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "微圈不存在");
        }
        JsMomentsArticle storedMoment = momentArticleService.getById(moment.getId());
        if (storedMoment == null) {
            throw new MyException(ErrorType.WRONG_INFO, "微圈不存在");
        }
        LambdaQueryWrapper<ApxCourseCollectLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApxCourseCollectLog::getUserId,frontUser.getId())
                .eq(ApxCourseCollectLog::getCourseId,moment.getId());
        Long count = apxCourseCollectLogMapper.selectCount(queryWrapper);
        boolean collected;
        if (count == 0) {
            ApxCourseCollectLog log = new ApxCourseCollectLog();
            log.setCourseId(moment.getId());
            log.setUserId(frontUser.getId());
            apxCourseCollectLogMapper.insert(log);
            collected = true;
        }else{
            apxCourseCollectLogMapper.delete(queryWrapper);
            collected = false;
        }
        momentArticleService.updateCollect(storedMoment);
        return ResultData.success(
                new String[]{"collected", "countCollect"},
                new Object[]{collected, storedMoment.getCountCollect()},
                collected ? "收藏成功" : "已取消收藏"
        );
    }
    //点赞+-
    @PostMapping("/likeMoment")
    public Object likeMoment(@RequestBody JsMomentsArticle moment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<ApxCourseLikeLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApxCourseLikeLog::getUserId,frontUser.getId())
                .eq(ApxCourseLikeLog::getCourseId,moment.getId());
        Long count = apxCourseLikeLogMapper.selectCount(queryWrapper);
        if (count == 0) {
            ApxCourseLikeLog log = new ApxCourseLikeLog();
            log.setCourseId(moment.getId());
            log.setUserId(frontUser.getId());
            apxCourseLikeLogMapper.insert(log);
        }else{
            apxCourseLikeLogMapper.delete(queryWrapper);
        }
        momentArticleService.updateLike(moment);
        return ResultData.success("like +-op success");
    }
    //转发
    @PostMapping("/shareMoment")
    public Object shareMoment(@RequestBody JsMomentsArticle moment, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        if (moment == null || moment.getId() == null || momentArticleService.getById(moment.getId()) == null) {
            throw new MyException(ErrorType.WRONG_INFO, "微圈不存在");
        }
        ApxCourseShareLog log = new ApxCourseShareLog();
        log.setCourseId(moment.getId());
        log.setUserId(frontUser.getId());
        apxCourseShareLogMapper.insert(log);
        return ResultData.success("share success");
    }
    //实名认证
    @PutMapping("/identify")
    public Object identify(@RequestBody ObjFrontUser user, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        frontUser.setChinaId(user.getChinaId());
        frontUserService.updateById(frontUser);
        frontUser.setPassword(null);
        session.setAttribute("student", frontUser);
        return ResultData.success("frontUser", frontUser, "identify success");
    }
    //申请成为创作者 qf_position_apply
    @PostMapping("/beingCreator")
    public Object beingCreator(@RequestBody QfPositionApply apply, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        if(frontUser.getChinaId() == null){
            throw new MyException(ErrorType.NO_AUTH_ID,"请完成实名认证");
        }
        apply.setUserId(frontUser.getId());
        apply.setTargetPosition("creator");
        apply.setChinaId(frontUser.getChinaId());
        apply.setTel(frontUser.getStuTel());
        apply.setEmail(frontUser.getEmail());
        apply.setStatus(0);
        qfPositionApplyMapper.insert(apply);
        return ResultData.success("请求提交成功，等待审核");
    }

    // ==================== 个人中心 ====================

    @GetMapping("/mine/overview")
    public Object mineOverview(HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        ObjFrontUser latestUser = frontUserService.getById(frontUser.getId());
        int studyDuration = latestUser == null || latestUser.getStudyDuration() == null
                ? 0
                : latestUser.getStudyDuration();
        frontUser.setStudyDuration(studyDuration);
        session.setAttribute("student", frontUser);
        OpFrontUserWallet wallet = opFrontUserWalletMapper.selectById(frontUser.getId());
        long orderCount = opOrderMapper.selectCount(
                new LambdaQueryWrapper<OpOrder>().eq(OpOrder::getUserId, frontUser.getId())
        );
        long couponCount = frontCouponService.availableCouponCount(frontUser.getId());
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("studyDuration", studyDuration);
        overview.put("wallet", wallet == null || wallet.getWallet() == null ? 0D : wallet.getWallet());
        overview.put("orderCount", orderCount);
        overview.put("couponCount", couponCount);
        return ResultData.success("overview", overview, "个人中心数据查询成功");
    }

    @GetMapping("/wallet")
    public Object wallet(HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        return ResultData.success("balance", frontWalletService.balance(frontUser.getId()), "钱包查询成功");
    }

    @PostMapping("/wallet/recharge")
    @Transactional
    public Object rechargeWallet(@RequestBody Map<String, Object> payload, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        BigDecimal balance = frontWalletService.recharge(frontUser.getId(), bodyMoney(payload, "amount"));
        return ResultData.success("balance", balance, "充值成功");
    }

    @GetMapping("/study-history")
    public Object studyHistory(HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        List<ApxCoursePlayLog> logs = apxCoursePlayLogMapper.selectList(
                new LambdaQueryWrapper<ApxCoursePlayLog>()
                        .eq(ApxCoursePlayLog::getUserId, frontUser.getId())
                        .orderByDesc(ApxCoursePlayLog::getStartTime)
        );
        List<Map<String, Object>> history = new ArrayList<>();
        Set<Integer> seenCourseIds = new HashSet<>();
        for (ApxCoursePlayLog log : logs) {
            if (log.getCourseId() == null || !seenCourseIds.add(log.getCourseId())) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", log.getId());
            item.put("courseId", log.getCourseId());
            item.put("startTime", log.getStartTime());
            item.put("endTime", log.getEndTime());
            item.put("videoUrl", log.getVideoUrl());
            JsCourseVO course = courseVOService.getById(log.getCourseId());
            item.put("courseTitle", course == null ? "课程已下架" : course.getTitle());
            item.put("coverUrl", course == null ? null : course.getCoverUrl());
            history.add(item);
        }
        return ResultData.success("history", history, "学习记录查询成功");
    }

    // ==================== 商城 ====================

    @GetMapping("/goods")
    public Object goods(@RequestParam(required = false) String keywords,
                        @RequestParam(required = false) Integer cateId) {
        LambdaQueryWrapper<JsGoods> wrapper = new LambdaQueryWrapper<JsGoods>()
                .eq(JsGoods::getStatus, 2);
        if (hasText(keywords)) {
            wrapper.and(query -> query.like(JsGoods::getGoodsName, keywords.trim())
                    .or().like(JsGoods::getKeywords, keywords.trim()));
        }
        if (cateId != null) {
            wrapper.eq(JsGoods::getCateId, cateId);
        }
        wrapper.orderByDesc(JsGoods::getRecommendStatus)
                .orderByDesc(JsGoods::getCreateTime);
        return ResultData.success("goods", jsGoodsMapper.selectList(wrapper), "商品查询成功");
    }

    @GetMapping("/goods/{id}")
    public Object goodsDetail(@PathVariable Integer id) {
        JsGoods goods = jsGoodsMapper.selectById(id);
        if (goods == null || !Integer.valueOf(2).equals(goods.getStatus())) {
            throw new MyException(ErrorType.WRONG_INFO, "商品不存在或已下架");
        }
        JsGoodsVO view = jsGoodsVOMapper.selectById(id);
        return ResultData.success(
                new String[]{"goods", "view"},
                new Object[]{goods, view},
                "商品详情查询成功"
        );
    }

    @GetMapping("/goods-categories")
    public Object goodsCategories() {
        List<JsGoodsCategory> categories = jsGoodsCategoryMapper.selectList(
                new LambdaQueryWrapper<JsGoodsCategory>()
                        .eq(JsGoodsCategory::getStatus, 1)
                        .orderByAsc(JsGoodsCategory::getParentId)
                        .orderByAsc(JsGoodsCategory::getSortNum)
        );
        return ResultData.success("categories", categories, "商品分类查询成功");
    }

    @PostMapping("/goods/order")
    @Transactional
    public Object purchaseGoods(@RequestBody Map<String, Object> payload, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        Integer goodsId = bodyInt(payload, "goodsId");
        int quantity = bodyInt(payload, "quantity") == null ? 1 : bodyInt(payload, "quantity");
        Integer couponUserId = bodyInt(payload, "couponUserId");
        String fullAddress = bodyText(payload, "fullAddress");
        String userRemark = bodyText(payload, "userRemark");
        if (goodsId == null) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择商品");
        }
        if (quantity < 1 || quantity > 99) {
            throw new MyException(ErrorType.WRONG_INFO, "商品数量必须在1到99之间");
        }
        if (!hasText(fullAddress)) {
            throw new MyException(ErrorType.WRONG_INFO, "请填写收货地址");
        }
        JsGoods goods = jsGoodsMapper.selectById(goodsId);
        if (goods == null || !Integer.valueOf(2).equals(goods.getStatus())) {
            throw new MyException(ErrorType.WRONG_INFO, "商品不存在或已下架");
        }

        BigDecimal unitPrice = goods.getPriceOriginal();
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new MyException(ErrorType.WRONG_INFO, "商品价格未配置");
        }
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        FrontCouponService.CouponDiscount coupon = frontCouponService.resolveCoupon(
                frontUser.getId(),
                FrontCouponService.TARGET_GOODS,
                goodsId,
                total,
                couponUserId
        );
        BigDecimal pricePay = coupon == null ? total : total.subtract(coupon.amount());
        frontWalletService.debit(frontUser.getId(), pricePay);

        OpOrder order = new OpOrder();
        order.setUserId(frontUser.getId());
        order.setOrderSn(createOrderSn(frontUser.getId()));
        order.setEntityId(goodsId);
        order.setEntityType(2);
        order.setStatus(2);
        order.setTotalQuantity(quantity);
        order.setPriceTotal(total);
        order.setPricePay(pricePay);
        order.setPriceFreight(BigDecimal.ZERO);
        order.setPayChannel(1);
        order.setPayTime(LocalDateTime.now());
        order.setFullAddress(fullAddress.trim());
        order.setSourceType(1);
        order.setCommentStatus(0);
        order.setUserRemark(hasText(userRemark) ? userRemark.trim() : null);
        order.setCreateTime(LocalDateTime.now());
        opOrderMapper.insert(order);
        frontCouponService.consumeCoupon(coupon, frontUser.getId(), order.getId());
        return ResultData.success("order", order, "下单成功，等待商家发货");
    }

    @GetMapping("/orders")
    public Object myOrders(HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        List<OpOrder> orders = opOrderMapper.selectList(
                new LambdaQueryWrapper<OpOrder>()
                        .eq(OpOrder::getUserId, frontUser.getId())
                        .orderByDesc(OpOrder::getCreateTime)
                        .orderByDesc(OpOrder::getId)
        );
        List<Map<String, Object>> result = orders.stream().map(this::toFrontOrder).toList();
        return ResultData.success("orders", result, "我的订单查询成功");
    }

    // ==================== 前台创作者课程 ====================

    @GetMapping("/creator/course-categories")
    public Object creatorCourseCategories(HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        ensureCreator(frontUser);
        List<JsCourseCategory> categories = jsCourseCategoryMapper.selectList(
                new LambdaQueryWrapper<JsCourseCategory>()
                        .orderByAsc(JsCourseCategory::getParentId)
                        .orderByAsc(JsCourseCategory::getSortNum)
                        .orderByAsc(JsCourseCategory::getId)
        );
        return ResultData.success("categories", categories, "课程分类查询成功");
    }

    @GetMapping("/creator/courses")
    public Object creatorCourses(HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        ensureCreator(frontUser);
        List<Map<String, Object>> courses = jsCourseMapper.selectList(
                new LambdaQueryWrapper<JsCourse>()
                        .eq(JsCourse::getFrontCreatorId, frontUser.getId())
                        .orderByDesc(JsCourse::getCreateTime)
                        .orderByDesc(JsCourse::getId)
        ).stream().map(this::toCreatorCourseItem).toList();
        return ResultData.success("courses", courses, "我的视频课程查询成功");
    }

    @GetMapping("/creator/courses/{id}")
    public Object creatorCourseDetail(@PathVariable Integer id, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        ensureCreator(frontUser);
        JsCourse course = creatorOwnedCourse(id, frontUser.getId());
        List<JsCourseContent> contents = creatorCourseContents(id);
        long salesCount = creatorCourseSales(id);
        return ResultData.success(
                new String[]{"course", "contents", "salesCount", "incomeTotal"},
                new Object[]{
                        course,
                        contents,
                        salesCount,
                        course.getPriceOriginal().multiply(BigDecimal.valueOf(salesCount))
                },
                "创作者课程详情查询成功"
        );
    }

    @PostMapping("/creator/courses")
    @Transactional
    public Object addCreatorCourse(@RequestBody CourseCreatePayload payload, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        ensureCreator(frontUser);
        JsCourse course = normalizeCreatorCourse(payload, frontUser, null);
        jsCourseMapper.insert(course);
        List<JsCourseContent> contents = insertCreatorCourseContents(course.getId(), payload.getContents());
        return ResultData.success(
                new String[]{"course", "contents"},
                new Object[]{course, contents},
                "视频课程已提交审核"
        );
    }

    @PutMapping("/creator/courses/{id}")
    @Transactional
    public Object updateCreatorCourse(@PathVariable Integer id,
                                      @RequestBody CourseCreatePayload payload,
                                      HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        ensureCreator(frontUser);
        JsCourse oldCourse = creatorOwnedCourse(id, frontUser.getId());
        List<JsCourseContent> oldContents = creatorCourseContents(id);

        JsCourse course = normalizeCreatorCourse(payload, frontUser, id);
        jsCourseMapper.updateById(course);
        jsCourseContentMapper.delete(
                new LambdaQueryWrapper<JsCourseContent>().eq(JsCourseContent::getCourseId, id)
        );
        List<JsCourseContent> contents = insertCreatorCourseContents(id, payload.getContents());

        Set<String> removedFiles = creatorCourseFiles(oldCourse, oldContents);
        removedFiles.removeAll(creatorCourseFiles(course, contents));
        deleteCreatorCourseFilesAfterCommit(removedFiles);
        return ResultData.success(
                new String[]{"course", "contents"},
                new Object[]{course, contents},
                "视频课程已更新并重新提交审核"
        );
    }

    @DeleteMapping("/creator/courses/{id}")
    @Transactional
    public Object deleteCreatorCourse(@PathVariable Integer id, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        ensureCreator(frontUser);
        JsCourse course = creatorOwnedCourse(id, frontUser.getId());
        Long orderCount = opOrderMapper.selectCount(
                new LambdaQueryWrapper<OpOrder>()
                        .eq(OpOrder::getEntityType, 1)
                        .eq(OpOrder::getEntityId, id)
                        .ge(OpOrder::getStatus, 1)
                        .lt(OpOrder::getStatus, 7)
        );
        if (orderCount != null && orderCount > 0) {
            throw new MyException(ErrorType.OPERATION_FAILED, "课程已有购买记录，不能删除");
        }
        List<JsCourseContent> contents = creatorCourseContents(id);
        jsCourseContentMapper.delete(
                new LambdaQueryWrapper<JsCourseContent>().eq(JsCourseContent::getCourseId, id)
        );
        opCircleAdMapper.delete(
                new LambdaQueryWrapper<OpCircleAd>().eq(OpCircleAd::getCourseId, id)
        );
        jsCourseMapper.deleteById(id);
        deleteCreatorCourseFilesAfterCommit(creatorCourseFiles(course, contents));
        return ResultData.success("视频课程已删除");
    }

    // ==================== 创作者视频 ====================

    @PostMapping("/upload/course-cover")
    public Object uploadCreatorCourseCover(@RequestParam("file") MultipartFile file, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        ensureCreator(frontUser);
        validateMomentImage(file);
        try {
            String coverUrl = fileService.uploadFile(file, courseCoverPath);
            return ResultData.success("coverUrl", coverUrl, "课程封面上传成功");
        } catch (IOException error) {
            throw new MyException(ErrorType.OPERATION_FAILED, "课程封面上传失败，请检查上传目录权限");
        }
    }

    @PostMapping("/upload/video")
    public Object uploadCreatorVideo(@RequestParam("file") MultipartFile file, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        ensureCreator(frontUser);
        validateCreatorVideo(file);
        try {
            String videoUrl = fileService.uploadFile(file, courseVideoPath);
            return ResultData.success("videoUrl", videoUrl, "视频上传成功");
        } catch (IOException error) {
            throw new MyException(ErrorType.OPERATION_FAILED, "视频上传失败，请检查上传目录权限");
        }
    }

    @PostMapping("/upload/moment-image")
    public Object uploadMomentImage(@RequestParam("file") MultipartFile file, HttpSession session) {
        currentFrontUser(session);
        validateMomentImage(file);
        try {
            String imageUrl = fileService.uploadFile(file, momentImagePath);
            return ResultData.success(
                    new String[]{"imageUrl", "coverUrl"},
                    new Object[]{imageUrl, imageUrl},
                    "图片上传成功"
            );
        } catch (IOException error) {
            throw new MyException(ErrorType.OPERATION_FAILED, "图片上传失败，请检查上传目录权限");
        }
    }

    // ==================== 反馈与客服 ====================

    @GetMapping("/feedback")
    public Object myFeedback(HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        List<CoUserFeedback> feedback = coUserFeedbackMapper.selectList(
                new LambdaQueryWrapper<CoUserFeedback>()
                        .eq(CoUserFeedback::getUserId, frontUser.getId())
                        .orderByDesc(CoUserFeedback::getCreateTime)
                        .orderByDesc(CoUserFeedback::getId)
        );
        return ResultData.success("feedback", feedback, "反馈记录查询成功");
    }

    @PostMapping("/feedback")
    public Object addFeedback(@RequestBody CoUserFeedback feedback, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        if (feedback == null || !hasText(feedback.getContent())) {
            throw new MyException(ErrorType.WRONG_INFO, "请填写反馈内容");
        }
        if (feedback.getContent().trim().length() > 1000) {
            throw new MyException(ErrorType.WRONG_INFO, "反馈内容不能超过1000字");
        }
        int starLevel = feedback.getStarLevel() == null ? 5 : feedback.getStarLevel();
        if (starLevel < 1 || starLevel > 5) {
            throw new MyException(ErrorType.WRONG_INFO, "评分必须在1到5之间");
        }
        feedback.setId(null);
        feedback.setUserId(frontUser.getId());
        feedback.setContent(feedback.getContent().trim());
        feedback.setFeedbackType(feedback.getFeedbackType() == null ? 3 : feedback.getFeedbackType());
        feedback.setStatus(1);
        feedback.setStarLevel(starLevel);
        feedback.setCreateTime(LocalDateTime.now());
        coUserFeedbackMapper.insert(feedback);
        return ResultData.success("feedback", feedback, "反馈提交成功");
    }

    @GetMapping("/service/messages")
    public Object serviceMessages(HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        List<CoImMessage> messages = coImMessageMapper.selectList(
                new LambdaQueryWrapper<CoImMessage>()
                        .and(query -> query
                                .and(sent -> sent.eq(CoImMessage::getSenderId, frontUser.getId())
                                        .eq(CoImMessage::getReceiverId, 0))
                                .or(received -> received.eq(CoImMessage::getSenderId, 0)
                                        .eq(CoImMessage::getReceiverId, frontUser.getId())))
                        .orderByAsc(CoImMessage::getSendTime)
                        .orderByAsc(CoImMessage::getId)
        );
        return ResultData.success("messages", messages, "客服消息查询成功");
    }

    @PostMapping("/service/messages")
    public Object sendServiceMessage(@RequestBody CoImMessage message, HttpSession session) {
        ObjFrontUser frontUser = currentFrontUser(session);
        if (message == null || !hasText(message.getContent())) {
            throw new MyException(ErrorType.WRONG_INFO, "请输入消息内容");
        }
        if (message.getContent().trim().length() > 500) {
            throw new MyException(ErrorType.WRONG_INFO, "消息不能超过500字");
        }
        message.setId(null);
        message.setSenderId(frontUser.getId());
        message.setSenderName(frontUser.getNickName() == null ? frontUser.getStuTel() : frontUser.getNickName());
        message.setReceiverId(0);
        message.setReceiverName("在线客服");
        message.setContent(message.getContent().trim());
        message.setSendTime(LocalDateTime.now());
        message.setIsRead(0);
        coImMessageMapper.insert(message);
        return ResultData.success("message", message, "消息发送成功");
    }

    private JsCourse normalizeCreatorCourse(CourseCreatePayload payload,
                                            ObjFrontUser frontUser,
                                            Integer courseId) {
        if (payload == null || payload.getCourse() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程信息不能为空");
        }
        List<JsCourseContent> contents = payload.getContents();
        if (contents == null || contents.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请至少添加一集课程内容");
        }
        JsCourse source = payload.getCourse();
        String title = trimToNull(source.getTitle());
        if (title == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程名称不能为空");
        }
        if (source.getCateId() == null || jsCourseCategoryMapper.selectById(source.getCateId()) == null) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择有效的课程分类");
        }
        if (source.getPriceOriginal() == null || source.getPriceOriginal().signum() < 0) {
            throw new MyException(ErrorType.WRONG_INFO, "课程售价不能小于0");
        }
        if (!hasText(source.getCoverUrl())) {
            throw new MyException(ErrorType.WRONG_INFO, "请上传课程封面");
        }

        int totalDuration = 0;
        for (int index = 0; index < contents.size(); index++) {
            JsCourseContent content = contents.get(index);
            if (content == null || !hasText(content.getEpName())) {
                throw new MyException(ErrorType.WRONG_INFO, "第 " + (index + 1) + " 集名称不能为空");
            }
            if (!hasText(content.getVideoUrl())) {
                throw new MyException(ErrorType.WRONG_INFO, "请上传第 " + (index + 1) + " 集视频");
            }
            if (content.getDuration() != null && content.getDuration() < 0) {
                throw new MyException(ErrorType.WRONG_INFO, "第 " + (index + 1) + " 集时长不能为负数");
            }
            totalDuration += content.getDuration() == null ? 0 : content.getDuration();
        }

        JsCourse course = new JsCourse();
        course.setId(courseId);
        course.setTeacherId(null);
        course.setFrontCreatorId(frontUser.getId());
        course.setCateId(source.getCateId());
        course.setRecommendType(0);
        course.setTitle(title);
        course.setIntro(trimToNull(source.getIntro()));
        course.setPriceOriginal(source.getPriceOriginal().setScale(2, RoundingMode.HALF_UP));
        course.setStatusShelf(0);
        course.setStatusAudit(1);
        course.setKeywords(hasText(source.getKeywords()) ? source.getKeywords().trim() : title);
        course.setCoverUrl(source.getCoverUrl().trim());
        course.setVideoUrl(contents.get(0).getVideoUrl().trim());
        course.setDetailDesc(trimToNull(source.getDetailDesc()));
        course.setEpisodeNum(contents.size());
        course.setDuration(totalDuration);
        if (courseId == null) {
            course.setCreateBy(frontUser.getId());
        } else {
            course.setUpdateBy(frontUser.getId());
        }
        return course;
    }

    private List<JsCourseContent> insertCreatorCourseContents(Integer courseId,
                                                               List<JsCourseContent> submitted) {
        List<JsCourseContent> contents = new ArrayList<>();
        for (int index = 0; index < submitted.size(); index++) {
            JsCourseContent source = submitted.get(index);
            JsCourseContent content = new JsCourseContent();
            content.setCourseId(courseId);
            content.setEpName(source.getEpName().trim());
            content.setEpNo(index + 1);
            content.setVideoUrl(source.getVideoUrl().trim());
            content.setDuration(source.getDuration() == null ? 0 : source.getDuration());
            jsCourseContentMapper.insert(content);
            contents.add(content);
        }
        return contents;
    }

    private JsCourse creatorOwnedCourse(Integer courseId, Integer frontUserId) {
        if (courseId == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程不存在");
        }
        JsCourse course = jsCourseMapper.selectOne(
                new LambdaQueryWrapper<JsCourse>()
                        .eq(JsCourse::getId, courseId)
                        .eq(JsCourse::getFrontCreatorId, frontUserId)
        );
        if (course == null) {
            throw new MyException(ErrorType.PERMISSION_DENIED, "课程不存在或不属于当前账号");
        }
        return course;
    }

    private List<JsCourseContent> creatorCourseContents(Integer courseId) {
        return jsCourseContentMapper.selectList(
                new LambdaQueryWrapper<JsCourseContent>()
                        .eq(JsCourseContent::getCourseId, courseId)
                        .orderByAsc(JsCourseContent::getEpNo)
                        .orderByAsc(JsCourseContent::getId)
        );
    }

    private long creatorCourseSales(Integer courseId) {
        Long count = opOrderMapper.selectCount(
                new LambdaQueryWrapper<OpOrder>()
                        .eq(OpOrder::getEntityType, 1)
                        .eq(OpOrder::getEntityId, courseId)
                        .ge(OpOrder::getStatus, 1)
                        .lt(OpOrder::getStatus, 7)
        );
        return count == null ? 0L : count;
    }

    private Map<String, Object> toCreatorCourseItem(JsCourse course) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", course.getId());
        item.put("title", course.getTitle());
        item.put("intro", course.getIntro());
        item.put("coverUrl", course.getCoverUrl());
        item.put("cateId", course.getCateId());
        JsCourseCategory category = jsCourseCategoryMapper.selectById(course.getCateId());
        item.put("cateName", category == null ? "未分类" : category.getCateName());
        item.put("priceOriginal", course.getPriceOriginal());
        item.put("statusShelf", course.getStatusShelf());
        item.put("statusAudit", course.getStatusAudit());
        item.put("episodeNum", course.getEpisodeNum());
        item.put("duration", course.getDuration());
        item.put("createTime", course.getCreateTime());
        item.put("updateTime", course.getUpdateTime());
        long salesCount = creatorCourseSales(course.getId());
        item.put("salesCount", salesCount);
        item.put(
                "incomeTotal",
                (course.getPriceOriginal() == null ? BigDecimal.ZERO : course.getPriceOriginal())
                        .multiply(BigDecimal.valueOf(salesCount))
        );
        return item;
    }

    private Set<String> creatorCourseFiles(JsCourse course, List<JsCourseContent> contents) {
        Set<String> files = new HashSet<>();
        addCreatorCourseFile(files, course == null ? null : course.getCoverUrl());
        addCreatorCourseFile(files, course == null ? null : course.getVideoUrl());
        if (contents != null) {
            contents.forEach(content -> addCreatorCourseFile(files, content.getVideoUrl()));
        }
        return files;
    }

    private void addCreatorCourseFile(Set<String> files, String fileUrl) {
        if (hasText(fileUrl) && fileUrl.startsWith("/uploaded/courses/")) {
            files.add(fileUrl);
        }
    }

    private void deleteCreatorCourseFilesAfterCommit(Set<String> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        files.removeIf(this::isCreatorCourseFileReferenced);
        if (files.isEmpty()) {
            return;
        }
        Runnable deleteFiles = () -> files.forEach(fileService::deletePhysicalFile);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteFiles.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteFiles.run();
            }
        });
    }

    private boolean isCreatorCourseFileReferenced(String fileUrl) {
        Long courseReferences = jsCourseMapper.selectCount(
                new LambdaQueryWrapper<JsCourse>()
                        .eq(JsCourse::getCoverUrl, fileUrl)
                        .or()
                        .eq(JsCourse::getVideoUrl, fileUrl)
        );
        if (courseReferences != null && courseReferences > 0) {
            return true;
        }
        Long contentReferences = jsCourseContentMapper.selectCount(
                new LambdaQueryWrapper<JsCourseContent>().eq(JsCourseContent::getVideoUrl, fileUrl)
        );
        if (contentReferences != null && contentReferences > 0) {
            return true;
        }
        Long adReferences = opCircleAdMapper.selectCount(
                new LambdaQueryWrapper<OpCircleAd>().eq(OpCircleAd::getPicUrl, fileUrl)
        );
        return adReferences != null && adReferences > 0;
    }

    private ObjFrontUser currentFrontUser(HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN, "请先登录");
        }
        return frontUser;
    }

    private void ensureCreator(ObjFrontUser frontUser) {
        if (!hasText(frontUser.getChinaId())) {
            throw new MyException(ErrorType.NO_AUTH_ID, "请先完成实名认证");
        }
        if (!Integer.valueOf(1).equals(frontUser.getCreaterVerified())) {
            throw new MyException(ErrorType.UNAUTHORIZED, "请先完成创作者认证");
        }
    }

    private void validateCreatorVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择要上传的视频");
        }
        if (file.getSize() > MAX_CREATOR_VIDEO_SIZE) {
            throw new MyException(ErrorType.FORMATE_ERROR, "视频不能超过500MB");
        }
        String filename = file.getOriginalFilename();
        String suffix = filename == null || !filename.contains(".")
                ? ""
                : filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (!CREATOR_VIDEO_SUFFIXES.contains(suffix)) {
            throw new MyException(ErrorType.FORMATE_ERROR, "仅支持mp4、mov、mkv、avi、flv、wmv、webm视频");
        }
    }

    private void validateMomentImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择要上传的图片");
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new MyException(ErrorType.FORMATE_ERROR, "图片不能超过5MB");
        }
        String filename = file.getOriginalFilename();
        String suffix = filename == null || !filename.contains(".")
                ? ""
                : filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (!MOMENT_IMAGE_SUFFIXES.contains(suffix)) {
            throw new MyException(ErrorType.FORMATE_ERROR, "仅支持jpg、jpeg、png、gif、webp图片");
        }
    }

    private void validateComment(CoComment comment) {
        if (comment == null || comment.getEntityId() == null || !hasText(comment.getContent())) {
            throw new MyException(ErrorType.WRONG_INFO, "评论内容不能为空");
        }
        if (comment.getContent().trim().length() > 1000) {
            throw new MyException(ErrorType.WRONG_INFO, "评论不能超过1000字");
        }
        int entityType = comment.getEntityType() == null ? 0 : comment.getEntityType();
        if (entityType != 0 && entityType != 2) {
            throw new MyException(ErrorType.WRONG_INFO, "评论对象类型错误");
        }
        if (entityType == 0 && courseVOService.getById(comment.getEntityId()) == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程不存在");
        }
        if (entityType == 2 && momentArticleService.getById(comment.getEntityId()) == null) {
            throw new MyException(ErrorType.WRONG_INFO, "微圈不存在");
        }
        comment.setEntityType(entityType);
    }

    private void adjustMomentCommentCount(Integer entityType, Integer entityId, int delta) {
        if (!Integer.valueOf(2).equals(entityType) || entityId == null || delta == 0) {
            return;
        }
        JsMomentsArticle moment = momentArticleService.getById(entityId);
        if (moment == null) {
            return;
        }
        moment.setCountComment(Math.max(0, (moment.getCountComment() == null ? 0 : moment.getCountComment()) + delta));
        momentArticleService.updateById(moment);
    }

    private Map<String, Object> toFrontOrder(OpOrder order) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", order.getId());
        item.put("orderSn", order.getOrderSn());
        item.put("entityId", order.getEntityId());
        item.put("entityType", order.getEntityType());
        item.put("status", order.getStatus());
        item.put("totalQuantity", order.getTotalQuantity());
        item.put("priceTotal", order.getPriceTotal());
        item.put("pricePay", order.getPricePay());
        item.put("fullAddress", order.getFullAddress());
        item.put("deliverySn", order.getDeliverySn());
        item.put("createTime", order.getCreateTime());
        if (Integer.valueOf(2).equals(order.getEntityType())) {
            JsGoods goods = jsGoodsMapper.selectById(order.getEntityId());
            item.put("itemName", goods == null ? "商品已下架" : goods.getGoodsName());
            item.put("imageUrl", goods == null ? null : goods.getMainPicUrl());
        } else {
            JsCourseVO course = courseVOService.getById(order.getEntityId());
            item.put("itemName", course == null ? "课程已下架" : course.getTitle());
            item.put("imageUrl", course == null ? null : course.getCoverUrl());
        }
        return item;
    }

    private String createOrderSn(Integer userId) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
        return "SY" + time + userId + random;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private Integer bodyInt(Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null || String.valueOf(payload.get(key)).isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(payload.get(key)));
        } catch (NumberFormatException error) {
            throw new MyException(ErrorType.WRONG_INFO, "参数格式错误：" + key);
        }
    }

    private BigDecimal bodyMoney(Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null || String.valueOf(payload.get(key)).isBlank()) {
            throw new MyException(ErrorType.WRONG_INFO, "请输入充值金额");
        }
        try {
            return new BigDecimal(String.valueOf(payload.get(key)));
        } catch (NumberFormatException error) {
            throw new MyException(ErrorType.WRONG_INFO, "充值金额格式错误");
        }
    }

    private String bodyText(Map<String, Object> payload, String key) {
        return payload == null || payload.get(key) == null ? null : String.valueOf(payload.get(key));
    }


}
