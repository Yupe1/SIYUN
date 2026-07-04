package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.yupe.siyun.entity.*;
import com.yupe.siyun.mapper.*;
import com.yupe.siyun.service.CourseVOService;
import com.yupe.siyun.service.FrontUserService;
import com.yupe.siyun.service.MomentArticleService;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    //搜索某一课程的子分集内容
    @GetMapping("/course/content")
    public Object searchContent(@RequestParam Integer courseId, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        if(!courseVOService.hasPurchased(frontUser, courseId)) {
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
    public Object purchace(@RequestBody JsCourse jsCourse, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        courseVOService.purchase(frontUser,jsCourse);
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
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<OpCouponVO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OpCouponVO::getUserId,frontUser.getId());
        List<OpCouponVO> opCouponVOS = opCouponVOMapper.selectList(queryWrapper);
        return ResultData.success("couponForOccassion",opCouponVOS,"优惠券刷新成功");
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
    public Object stopPlay(@RequestBody ApxCoursePlayLog log) {
        ApxCoursePlayLog update = new ApxCoursePlayLog();
        update.setId(log.getId());
        update.setEndTime(LocalDateTime.now());
        apxCoursePlayLogMapper.updateById(update);
        return ResultData.success("endPlay");
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
        LambdaQueryWrapper<ApxCourseCollectLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApxCourseCollectLog::getUserId,frontUser.getId())
                .eq(ApxCourseCollectLog::getCourseId,jsCourse.getId());
        Long count = apxCourseCollectLogMapper.selectCount(queryWrapper);
        if (count == 0) {
            ApxCourseCollectLog log = new ApxCourseCollectLog();
            log.setCourseId(jsCourse.getId());
            log.setUserId(frontUser.getId());
            apxCourseCollectLogMapper.insert(log);
        }else{
            apxCourseCollectLogMapper.delete(queryWrapper);
        }
        return ResultData.success("collect +-op success");
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
        ApxCourseShareLog log = new ApxCourseShareLog();
        log.setCourseId(jsCourse.getId());
        log.setUserId(frontUser.getId());
        apxCourseShareLogMapper.insert(log);
        return ResultData.success("share success");
    }
    //课程评论
    @PostMapping("/comment")
    public Object addComment(@RequestBody CoComment comment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        if(comment.getParentId() != 0){
            CoComment parent = coCommentMapper.selectById(comment.getParentId());
            parent.setCountReply(parent.getCountReply()+1);
            coCommentMapper.updateById(parent);
        }
        comment.setUserId(frontUser.getId());
        coCommentMapper.insert(comment);
        return ResultData.success("one comment added");
    }
    //评论点赞
    @PostMapping("/commentLike")
    public Object commentLike(@RequestBody CoComment comment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        CoComment comment1 = coCommentMapper.selectById(comment.getId());
        if (comment1 == null) {
            throw new MyException(ErrorType.COMMENT_NOT_EXIST,"评论不存在");
        }
        comment1.setCountLike(comment1.getCountLike() + 1);
        coCommentMapper.updateById(comment1);
        return ResultData.success("commentLike +-op success");
    }
    //加载评论区0
    @GetMapping("/comment")
    public Object comment(@RequestParam Integer id, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<CoComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoComment::getEntityId, id)
                .eq(CoComment::getParentId, 0);
        List<CoComment> comments = coCommentMapper.selectList(queryWrapper);
        return ResultData.success("commentList",comments,"comment loaded");
    }
    //加载子评论
    @GetMapping("/subComment")
    public Object subComment(@RequestParam Integer id, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<CoComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoComment::getParentId, id);
        List<CoComment> comments = coCommentMapper.selectList(queryWrapper);
        return ResultData.success("commentList",comments,"sub-comment loaded");
    }
    //删除自己评论
    @DeleteMapping("/comment")
    public Object deleteMy(@RequestBody CoComment comment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        if(comment.getUserId() != frontUser.getId()){
            throw new MyException(ErrorType.NOT_LOGIN,"请登录正确账号");
        }
        coCommentMapper.deleteById(comment.getId());
        return ResultData.success("delete comment success");
    }


//微圈
    //查看单个微圈
    @GetMapping("/moment/{id}")
    public Object getMoment(@PathVariable Integer id) {
        JsMomentsArticle moment = momentArticleService.getById(id);
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
        if(frontUser.getChinaId() == null){
            throw new MyException(ErrorType.NO_AUTH_ID,"请完成实名认证");
        }
        LambdaQueryWrapper<JsMomentsArticle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JsMomentsArticle::getAuthorId, frontUser.getId());
        List<JsMomentsArticle> moments = momentArticleService.list(queryWrapper);
        return ResultData.success("myMoments",moments,"my moments loaded");
    }
    //实名可发布微圈
    @PostMapping("/moment")
    public Object addMoment(@RequestBody JsMomentsArticle moment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        if(frontUser.getChinaId() == null){
            throw new MyException(ErrorType.NO_AUTH_ID,"请完成实名认证");
        }
        if(frontUser.getCreaterVerified() == null || frontUser.getCreaterVerified() != 1){
            throw new MyException(ErrorType.UNAUTHORIZED,"请先完成创作者认证");
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
        if(frontUser.getChinaId() == null){
            throw new MyException(ErrorType.NO_AUTH_ID,"请完成实名认证");
        }
        if(moment.getAuthorId() != frontUser.getId()){
            throw new MyException(ErrorType.UNAUTHORIZED,"您无权操作");
        }
        momentArticleService.removeById(moment.getId());
        return ResultData.success("delete moments success");
    }

    //收藏+-
    @PostMapping("/collectMoment")
    public Object collectMoment(@RequestBody JsMomentsArticle moment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
        }
        LambdaQueryWrapper<ApxCourseCollectLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApxCourseCollectLog::getUserId,frontUser.getId())
                .eq(ApxCourseCollectLog::getCourseId,moment.getId());
        Long count = apxCourseCollectLogMapper.selectCount(queryWrapper);
        if (count == 0) {
            ApxCourseCollectLog log = new ApxCourseCollectLog();
            log.setCourseId(moment.getId());
            log.setUserId(frontUser.getId());
            apxCourseCollectLogMapper.insert(log);
        }else{
            apxCourseCollectLogMapper.delete(queryWrapper);
        }
        return ResultData.success("collect +-op success");
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
        return ResultData.success("like +-op success");
    }
    //转发
    @PostMapping("/shareMoment")
    public Object shareMoment(@RequestBody JsMomentsArticle moment, HttpSession session) {
        ObjFrontUser frontUser = (ObjFrontUser) session.getAttribute("student");
        if (frontUser == null) {
            throw new MyException(ErrorType.NOT_LOGIN,"请先登录");
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


//商城


}
