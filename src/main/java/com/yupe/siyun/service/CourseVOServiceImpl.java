package com.yupe.siyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupe.siyun.entity.*;
import com.yupe.siyun.mapper.ApxCoursePlayLogMapper;
import com.yupe.siyun.mapper.CourseVOMapper;
import com.yupe.siyun.mapper.JsCourseMapper;
import com.yupe.siyun.mapper.OpOrderMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseVOServiceImpl extends ServiceImpl<CourseVOMapper, JsCourseVO> implements CourseVOService {
    @Autowired
    private OpOrderMapper opOrderMapper;
    @Autowired
    private CourseVOMapper courseVOMapper;
    @Autowired
    private JsCourseMapper jsCourseMapper;
    @Autowired
    private ApxCoursePlayLogMapper apxCoursePlayLogMapper;
    @Autowired
    private FrontCouponService frontCouponService;
    @Autowired
    private FrontWalletService frontWalletService;

    @Override
    public List<JsCourseVO> search(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return this.list();
        }

        String normalizedKeywords = keywords.trim();
        //  teacherName // cateName // title
        LambdaQueryWrapper<JsCourseVO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(JsCourseVO::getTeacherName, normalizedKeywords)
                .or()
                .like(JsCourseVO::getCateName, normalizedKeywords)
                .or()
                .like(JsCourseVO::getTitle, normalizedKeywords);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public void purchase(ObjFrontUser frontUser, JsCourse jsCourse, Integer couponUserId) {
        if (jsCourse == null || jsCourse.getId() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "缺少课程信息");
        }
        if (hasPurchased(frontUser, jsCourse.getId())) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "您已购买该课程，请直接学习");
        }
        JsCourseVO course = courseVOMapper.selectById(jsCourse.getId());
        if (course == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程不存在或已下架");
        }
        JsCourse sourceCourse = jsCourseMapper.selectById(jsCourse.getId());
        if (sourceCourse == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程不存在或已下架");
        }
        if (sourceCourse.getFrontCreatorId() != null
                && sourceCourse.getFrontCreatorId().equals(frontUser.getId())) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "不能购买自己发布的课程");
        }
        BigDecimal priceOriginal = course.getPriceOriginal();
        if (priceOriginal == null || priceOriginal.signum() < 0) {
            throw new MyException(ErrorType.WRONG_INFO, "课程价格配置异常");
        }
        FrontCouponService.CouponDiscount coupon = frontCouponService.resolveCoupon(
                frontUser.getId(),
                FrontCouponService.TARGET_COURSE,
                jsCourse.getId(),
                priceOriginal,
                couponUserId
        );
        BigDecimal pricePay = coupon == null
                ? priceOriginal
                : priceOriginal.subtract(coupon.amount());
        frontWalletService.debit(frontUser.getId(), pricePay);

        OpOrder opOrder = new OpOrder();
        opOrder.setUserId(frontUser.getId());
        opOrder.setOrderSn(createOrderSn(frontUser.getId()));
        opOrder.setEntityId(jsCourse.getId());
        opOrder.setEntityType(1);
        opOrder.setStatus(1);
        opOrder.setTotalQuantity(1);
        opOrder.setPriceTotal(priceOriginal);
        opOrder.setPricePay(pricePay);
        opOrder.setPriceFreight(BigDecimal.ZERO);
        opOrder.setPayChannel(1);
        opOrder.setPayTime(LocalDateTime.now());
        opOrder.setCommentStatus(0);
        opOrder.setCreateTime(LocalDateTime.now());
        opOrderMapper.insert(opOrder);
        frontCouponService.consumeCoupon(coupon, frontUser.getId(), opOrder.getId());
        if (sourceCourse.getFrontCreatorId() != null) {
            frontWalletService.creditCourseIncome(sourceCourse.getFrontCreatorId(), priceOriginal);
        }
    }

    @Override
    @Transactional
    public ApxCoursePlayLog startPlay(ObjFrontUser frontUser, JsCourse jsCourse) {
        if (jsCourse == null || jsCourse.getId() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "缺少课程信息");
        }
        if (!hasPurchased(frontUser, jsCourse.getId())) {
            throw new MyException(ErrorType.UNAUTHORIZED, "请先购买该课程");
        }
        JsCourseVO course = courseVOMapper.selectById(jsCourse.getId());
        if (course == null) {
            throw new MyException(ErrorType.WRONG_INFO, "课程不存在或已下架");
        }
        String videoUrl = jsCourse.getVideoUrl();
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            videoUrl = course.getVideoUrl();
        }
        ApxCoursePlayLog log = new ApxCoursePlayLog();
        log.setCourseId(jsCourse.getId());
        log.setUserId(frontUser.getId());
        log.setVideoUrl(videoUrl);
        log.setStartTime(LocalDateTime.now());
        apxCoursePlayLogMapper.insert(log);
        return log;
    }

    @Override
    public boolean hasPurchased(ObjFrontUser frontUser, Integer courseId) {
        Long count = opOrderMapper.selectCount(
                new QueryWrapper<OpOrder>()
                        .eq("user_id", frontUser.getId())
                        .eq("entity_id", courseId)
                        .eq("entity_type", 1)
                        .ge("status", 1)
                        .lt("status", 7)
        );
        return count != null && count > 0;
    }

    private String createOrderSn(Integer userId) {
        return "SY" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + userId
                + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
