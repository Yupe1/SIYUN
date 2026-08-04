package com.yupe.siyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yupe.siyun.entity.OpCoupon;
import com.yupe.siyun.entity.OpCouponGoods;
import com.yupe.siyun.entity.OpCouponUser;
import com.yupe.siyun.entity.JsCourse;
import com.yupe.siyun.entity.JsGoods;
import com.yupe.siyun.mapper.JsCourseMapper;
import com.yupe.siyun.mapper.JsGoodsMapper;
import com.yupe.siyun.mapper.OpCouponGoodsMapper;
import com.yupe.siyun.mapper.OpCouponMapper;
import com.yupe.siyun.mapper.OpCouponUserMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FrontCouponService {

    public static final int TARGET_GOODS = 1;
    public static final int TARGET_COURSE = 2;

    @Autowired
    private OpCouponMapper opCouponMapper;
    @Autowired
    private OpCouponUserMapper opCouponUserMapper;
    @Autowired
    private OpCouponGoodsMapper opCouponGoodsMapper;
    @Autowired
    private JsGoodsMapper jsGoodsMapper;
    @Autowired
    private JsCourseMapper jsCourseMapper;

    public List<Map<String, Object>> myCoupons(Integer userId) {
        List<OpCouponUser> owned = opCouponUserMapper.selectList(
                new LambdaQueryWrapper<OpCouponUser>()
                        .eq(OpCouponUser::getUserId, userId)
                        .eq(OpCouponUser::getStatus, 0)
                        .orderByDesc(OpCouponUser::getGetTime)
                        .orderByDesc(OpCouponUser::getId)
        );
        if (owned.isEmpty()) {
            return List.of();
        }

        List<Integer> couponIds = owned.stream().map(OpCouponUser::getCouponId).distinct().toList();
        Map<Integer, OpCoupon> couponMap = opCouponMapper.selectBatchIds(couponIds).stream()
                .collect(Collectors.toMap(OpCoupon::getId, Function.identity()));
        Map<Integer, List<OpCouponGoods>> bindingsByCoupon = opCouponGoodsMapper.selectList(
                new LambdaQueryWrapper<OpCouponGoods>()
                        .in(OpCouponGoods::getCouponId, couponIds)
                        .gt(OpCouponGoods::getQuota, 0)
                        .orderByAsc(OpCouponGoods::getId)
        ).stream().collect(Collectors.groupingBy(OpCouponGoods::getCouponId));

        List<Integer> targetIds = bindingsByCoupon.values().stream()
                .flatMap(List::stream)
                .map(OpCouponGoods::getGoodsId)
                .distinct()
                .toList();
        Map<Integer, JsGoods> goodsMap = targetIds.isEmpty()
                ? Collections.emptyMap()
                : jsGoodsMapper.selectBatchIds(targetIds).stream()
                .collect(Collectors.toMap(JsGoods::getId, Function.identity()));
        Map<Integer, JsCourse> courseMap = targetIds.isEmpty()
                ? Collections.emptyMap()
                : jsCourseMapper.selectBatchIds(targetIds).stream()
                .collect(Collectors.toMap(JsCourse::getId, Function.identity()));

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> result = new ArrayList<>();
        for (OpCouponUser couponUser : owned) {
            OpCoupon coupon = couponMap.get(couponUser.getCouponId());
            if (!isCouponActive(coupon, now)) {
                continue;
            }
            List<Map<String, Object>> targets = buildTargets(
                    coupon,
                    bindingsByCoupon.getOrDefault(coupon.getId(), List.of()),
                    goodsMap,
                    courseMap
            );
            if (targets.isEmpty()) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", couponUser.getId());
            item.put("couponUserId", couponUser.getId());
            item.put("couponId", coupon.getId());
            item.put("couponSn", coupon.getCouponSn());
            item.put("couponName", coupon.getCouponName());
            item.put("amount", couponAmount(coupon));
            item.put("imgUrl", coupon.getImgUrl());
            item.put("startTime", coupon.getStartTime());
            item.put("endTime", coupon.getEndTime());
            item.put("applyType", coupon.getApplyType() == null ? 0 : coupon.getApplyType());
            item.put("targets", targets);
            result.add(item);
        }
        return result;
    }

    public long availableCouponCount(Integer userId) {
        return myCoupons(userId).size();
    }

    public List<Map<String, Object>> availableCoupons(Integer userId,
                                                       Integer targetType,
                                                       Integer targetId,
                                                       BigDecimal priceTotal) {
        validateTarget(targetType, targetId);
        List<OpCouponUser> owned = opCouponUserMapper.selectList(
                new LambdaQueryWrapper<OpCouponUser>()
                        .eq(OpCouponUser::getUserId, userId)
                        .eq(OpCouponUser::getStatus, 0)
                        .orderByAsc(OpCouponUser::getId)
        );
        if (owned.isEmpty()) {
            return List.of();
        }

        List<Integer> couponIds = owned.stream().map(OpCouponUser::getCouponId).distinct().toList();
        Map<Integer, OpCoupon> couponMap = opCouponMapper.selectBatchIds(couponIds).stream()
                .collect(Collectors.toMap(OpCoupon::getId, Function.identity()));
        Map<Integer, OpCouponGoods> bindingMap = opCouponGoodsMapper.selectList(
                new LambdaQueryWrapper<OpCouponGoods>()
                        .in(OpCouponGoods::getCouponId, couponIds)
                        .eq(OpCouponGoods::getGoodsId, targetId)
                        .gt(OpCouponGoods::getQuota, 0)
        ).stream().collect(Collectors.toMap(
                OpCouponGoods::getCouponId,
                Function.identity(),
                (left, right) -> left
        ));

        LocalDateTime now = LocalDateTime.now();
        BigDecimal safeTotal = priceTotal == null || priceTotal.signum() < 0
                ? BigDecimal.ZERO
                : priceTotal;
        List<Map<String, Object>> result = new ArrayList<>();
        for (OpCouponUser couponUser : owned) {
            OpCoupon coupon = couponMap.get(couponUser.getCouponId());
            OpCouponGoods binding = bindingMap.get(couponUser.getCouponId());
            if (!isAvailable(coupon, binding, targetType, now)) {
                continue;
            }
            BigDecimal amount = couponAmount(coupon);
            BigDecimal discount = amount.min(safeTotal);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("couponUserId", couponUser.getId());
            item.put("couponId", coupon.getId());
            item.put("couponName", coupon.getCouponName());
            item.put("amount", amount);
            item.put("discount", discount);
            item.put("pricePay", safeTotal.subtract(discount));
            item.put("startTime", coupon.getStartTime());
            item.put("endTime", coupon.getEndTime());
            result.add(item);
        }
        result.sort((left, right) -> ((BigDecimal) right.get("discount"))
                .compareTo((BigDecimal) left.get("discount")));
        return result;
    }

    public CouponDiscount resolveCoupon(Integer userId,
                                        Integer targetType,
                                        Integer targetId,
                                        BigDecimal priceTotal,
                                        Integer couponUserId) {
        validateTarget(targetType, targetId);
        if (couponUserId == null) {
            return null;
        }
        OpCouponUser couponUser = opCouponUserMapper.selectById(couponUserId);
        if (couponUser == null || !userId.equals(couponUser.getUserId())) {
            throw new MyException(ErrorType.WRONG_INFO, "优惠券不存在或不属于当前用户");
        }
        if (!Integer.valueOf(0).equals(couponUser.getStatus())) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "该优惠券已使用或已失效");
        }
        OpCoupon coupon = opCouponMapper.selectById(couponUser.getCouponId());
        OpCouponGoods binding = opCouponGoodsMapper.selectOne(
                new LambdaQueryWrapper<OpCouponGoods>()
                        .eq(OpCouponGoods::getCouponId, couponUser.getCouponId())
                        .eq(OpCouponGoods::getGoodsId, targetId)
                        .last("limit 1")
        );
        if (!isAvailable(coupon, binding, targetType, LocalDateTime.now())) {
            throw new MyException(ErrorType.WRONG_INFO, "该优惠券不适用于当前商品或课程");
        }
        BigDecimal amount = couponAmount(coupon);
        BigDecimal safeTotal = priceTotal == null ? BigDecimal.ZERO : priceTotal.max(BigDecimal.ZERO);
        return new CouponDiscount(
                couponUser.getId(),
                coupon.getId(),
                binding.getId(),
                coupon.getCouponName(),
                amount.min(safeTotal)
        );
    }

    public void consumeCoupon(CouponDiscount coupon, Integer userId, Integer orderId) {
        if (coupon == null) {
            return;
        }
        int userUpdated = opCouponUserMapper.update(
                null,
                new LambdaUpdateWrapper<OpCouponUser>()
                        .eq(OpCouponUser::getId, coupon.couponUserId())
                        .eq(OpCouponUser::getUserId, userId)
                        .eq(OpCouponUser::getStatus, 0)
                        .set(OpCouponUser::getStatus, 1)
                        .set(OpCouponUser::getUseTime, LocalDateTime.now())
                        .set(OpCouponUser::getOrderId, orderId)
        );
        if (userUpdated != 1) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "优惠券已被使用，请重新下单");
        }
        int quotaUpdated = opCouponGoodsMapper.update(
                null,
                new LambdaUpdateWrapper<OpCouponGoods>()
                        .eq(OpCouponGoods::getId, coupon.bindingId())
                        .gt(OpCouponGoods::getQuota, 0)
                        .setSql("quota = quota - 1")
        );
        if (quotaUpdated != 1) {
            throw new MyException(ErrorType.WRONG_INFO, "优惠券库存不足，请重新选择");
        }
    }

    private boolean isAvailable(OpCoupon coupon,
                                OpCouponGoods binding,
                                Integer targetType,
                                LocalDateTime now) {
        if (coupon == null || binding == null || binding.getQuota() == null || binding.getQuota() <= 0) {
            return false;
        }
        if (!isCouponActive(coupon, now)) return false;
        Integer applyType = coupon.getApplyType() == null ? 0 : coupon.getApplyType();
        if (!Integer.valueOf(0).equals(applyType) && !targetType.equals(applyType)) {
            return false;
        }
        return true;
    }

    private boolean isCouponActive(OpCoupon coupon, LocalDateTime now) {
        return coupon != null
                && Integer.valueOf(1).equals(coupon.getStatusShelf())
                && coupon.getStartTime() != null
                && coupon.getEndTime() != null
                && !now.isBefore(coupon.getStartTime())
                && !now.isAfter(coupon.getEndTime())
                && coupon.getAmount() != null
                && coupon.getAmount() > 0;
    }

    private List<Map<String, Object>> buildTargets(OpCoupon coupon,
                                                   List<OpCouponGoods> bindings,
                                                   Map<Integer, JsGoods> goodsMap,
                                                   Map<Integer, JsCourse> courseMap) {
        List<Map<String, Object>> targets = new ArrayList<>();
        int applyType = coupon.getApplyType() == null ? 0 : coupon.getApplyType();
        for (OpCouponGoods binding : bindings) {
            Integer targetId = binding.getGoodsId();
            if (applyType == TARGET_GOODS || applyType == 0) {
                JsGoods goods = goodsMap.get(targetId);
                if (goods != null && Integer.valueOf(2).equals(goods.getStatus())) {
                    targets.add(target(
                            TARGET_GOODS,
                            goods.getId(),
                            goods.getGoodsName(),
                            goods.getMainPicUrl(),
                            binding.getQuota()
                    ));
                }
            }
            if (applyType == TARGET_COURSE || applyType == 0) {
                JsCourse course = courseMap.get(targetId);
                if (course != null
                        && Integer.valueOf(1).equals(course.getStatusShelf())
                        && Integer.valueOf(3).equals(course.getStatusAudit())) {
                    targets.add(target(
                            TARGET_COURSE,
                            course.getId(),
                            course.getTitle(),
                            course.getCoverUrl(),
                            binding.getQuota()
                    ));
                }
            }
        }
        return targets;
    }

    private Map<String, Object> target(Integer type,
                                       Integer id,
                                       String name,
                                       String imageUrl,
                                       Integer quota) {
        Map<String, Object> target = new HashMap<>();
        target.put("targetType", type);
        target.put("targetId", id);
        target.put("targetName", name);
        target.put("imageUrl", imageUrl);
        target.put("quota", quota);
        return target;
    }

    private BigDecimal couponAmount(OpCoupon coupon) {
        return BigDecimal.valueOf(coupon.getAmount());
    }

    private void validateTarget(Integer targetType, Integer targetId) {
        if ((!Integer.valueOf(TARGET_GOODS).equals(targetType)
                && !Integer.valueOf(TARGET_COURSE).equals(targetType)) || targetId == null) {
            throw new MyException(ErrorType.WRONG_INFO, "优惠券适用对象参数错误");
        }
    }

    public record CouponDiscount(Integer couponUserId,
                                 Integer couponId,
                                 Integer bindingId,
                                 String couponName,
                                 BigDecimal amount) {
    }
}
