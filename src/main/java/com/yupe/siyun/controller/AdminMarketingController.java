package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.CouponGrantPayload;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.OpCircleAd;
import com.yupe.siyun.entity.OpCoupon;
import com.yupe.siyun.entity.OpCouponGoods;
import com.yupe.siyun.entity.OpCouponUser;
import com.yupe.siyun.entity.OpCouponVO;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.OpCircleAdMapper;
import com.yupe.siyun.mapper.OpCouponGoodsMapper;
import com.yupe.siyun.mapper.OpCouponMapper;
import com.yupe.siyun.mapper.OpCouponUserMapper;
import com.yupe.siyun.mapper.OpCouponVOMapper;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminMarketingController extends AdminControllerSupport {

    @Autowired
    private OpCouponMapper opCouponMapper;
    @Autowired
    private OpCouponVOMapper opCouponVOMapper;
    @Autowired
    private OpCouponUserMapper opCouponUserMapper;
    @Autowired
    private OpCouponGoodsMapper opCouponGoodsMapper;
    @Autowired
    private OpCircleAdMapper opCircleAdMapper;

    @GetMapping("/coupons")
    @RequiresPermission("admin:coupon:list")
    public Object coupons(@RequestParam(defaultValue = "1") Long page,
                          @RequestParam(defaultValue = "10") Long size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer statusShelf,
                          @RequestParam(required = false) Integer applyType) {
        LambdaQueryWrapper<OpCoupon> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) wrapper.and(w -> w.like(OpCoupon::getCouponName, keyword).or().like(OpCoupon::getCouponSn, keyword));
        if (statusShelf != null) wrapper.eq(OpCoupon::getStatusShelf, statusShelf);
        if (applyType != null) wrapper.eq(OpCoupon::getApplyType, applyType);
        wrapper.orderByDesc(OpCoupon::getCreateTime);
        Page<OpCoupon> data = new Page<>(page, size);
        opCouponMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "优惠券列表");
    }

    @PostMapping("/coupons")
    @RequiresPermission("admin:coupon:add")
    public Object addCoupon(@RequestBody OpCoupon coupon, HttpSession session) {
        coupon.setCreateBy(currentUser(session).getId());
        if (coupon.getStatusShelf() == null) coupon.setStatusShelf(0);
        if (coupon.getIssueType() == null) coupon.setIssueType(1);
        if (coupon.getApplyType() == null) coupon.setApplyType(0);
        opCouponMapper.insert(coupon);
        return ResultData.success("coupon", coupon, "优惠券已添加");
    }

    @PutMapping("/coupons/{id}")
    @RequiresPermission("admin:coupon:update")
    public Object updateCoupon(@PathVariable Integer id, @RequestBody OpCoupon coupon, HttpSession session) {
        coupon.setId(id);
        coupon.setUpdateBy(currentUser(session).getId());
        opCouponMapper.updateById(coupon);
        return ResultData.success("优惠券已更新");
    }

    @DeleteMapping("/coupons/{id}")
    @RequiresPermission("admin:coupon:delete")
    public Object deleteCoupon(@PathVariable Integer id) {
        opCouponMapper.deleteById(id);
        return ResultData.success("优惠券已删除");
    }

    @PutMapping("/coupons/{id}/status")
    @RequiresPermission("admin:coupon:status")
    public Object updateCouponStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body, HttpSession session) {
        OpCoupon coupon = new OpCoupon();
        coupon.setId(id);
        coupon.setStatusShelf(bodyInt(body, "statusShelf"));
        coupon.setUpdateBy(currentUser(session).getId());
        opCouponMapper.updateById(coupon);
        return ResultData.success("优惠券上下线状态已更新");
    }

    @GetMapping("/coupon-users")
    @RequiresPermission("admin:coupon:user")
    public Object couponUsers(@RequestParam(defaultValue = "1") Long page,
                              @RequestParam(defaultValue = "10") Long size,
                              @RequestParam(required = false) Integer userId,
                              @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<OpCouponVO> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(OpCouponVO::getUserId, userId);
        if (status != null) {
            wrapper.inSql(OpCouponVO::getId, "select coupon_id from op_coupon_user where status = " + status);
        }
        Page<OpCouponVO> data = new Page<>(page, size);
        opCouponVOMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "用户优惠券查询");
    }

    @PostMapping("/coupon-users/grant")
    @RequiresPermission("admin:coupon:grant")
    public Object grantCoupon(@RequestBody CouponGrantPayload payload) {
        OpCouponUser couponUser = new OpCouponUser();
        couponUser.setCouponId(payload.getCouponId());
        couponUser.setUserId(payload.getUserId());
        couponUser.setGetTime(LocalDateTime.now());
        couponUser.setStatus(0);
        opCouponUserMapper.insert(couponUser);
        return ResultData.success("couponUser", couponUser, "优惠券已赠送");
    }

    @GetMapping("/coupon-goods")
    @RequiresPermission("admin:coupon:list")
    public Object couponGoods(@RequestParam(required = false) Integer couponId) {
        LambdaQueryWrapper<OpCouponGoods> wrapper = new LambdaQueryWrapper<>();
        if (couponId != null) wrapper.eq(OpCouponGoods::getCouponId, couponId);
        return ResultData.success("items", opCouponGoodsMapper.selectList(wrapper), "优惠券适用商品/课程");
    }

    @GetMapping("/ads")
    @RequiresPermission("admin:ad:list")
    public Object ads(@RequestParam(defaultValue = "1") Long page,
                      @RequestParam(defaultValue = "10") Long size,
                      @RequestParam(required = false) Integer positionType,
                      @RequestParam(required = false) Integer statusShow) {
        LambdaQueryWrapper<OpCircleAd> wrapper = new LambdaQueryWrapper<>();
        if (positionType != null) wrapper.eq(OpCircleAd::getPositionType, positionType);
        if (statusShow != null) wrapper.eq(OpCircleAd::getStatusShow, statusShow);
        wrapper.orderByDesc(OpCircleAd::getSortNum).orderByDesc(OpCircleAd::getId);
        Page<OpCircleAd> data = new Page<>(page, size);
        opCircleAdMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "轮播图列表");
    }

    @PostMapping("/ads")
    @RequiresPermission("admin:ad:add")
    public Object addAd(@RequestBody OpCircleAd ad, HttpSession session) {
        ad.setCreateBy(currentUser(session).getId());
        if (ad.getStatusShow() == null) ad.setStatusShow(1);
        if (ad.getSortNum() == null) ad.setSortNum(0);
        opCircleAdMapper.insert(ad);
        return ResultData.success("ad", ad, "轮播图已添加");
    }

    @PutMapping("/ads/{id}")
    @RequiresPermission("admin:ad:update")
    public Object updateAd(@PathVariable Integer id, @RequestBody OpCircleAd ad) {
        ad.setId(id);
        opCircleAdMapper.updateById(ad);
        return ResultData.success("轮播图已更新");
    }

    @DeleteMapping("/ads/{id}")
    @RequiresPermission("admin:ad:delete")
    public Object deleteAd(@PathVariable Integer id) {
        opCircleAdMapper.deleteById(id);
        return ResultData.success("轮播图已删除");
    }

    @PutMapping("/ads/{id}/status")
    @RequiresPermission("admin:ad:status")
    public Object updateAdStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        OpCircleAd ad = new OpCircleAd();
        ad.setId(id);
        ad.setStatusShow(bodyInt(body, "statusShow"));
        opCircleAdMapper.updateById(ad);
        return ResultData.success("轮播图状态已更新");
    }

    @PutMapping("/ads/{id}/top")
    @RequiresPermission("admin:ad:top")
    public Object topAd(@PathVariable Integer id) {
        OpCircleAd ad = new OpCircleAd();
        ad.setId(id);
        ad.setSortNum(9999);
        opCircleAdMapper.updateById(ad);
        return ResultData.success("轮播图已置顶");
    }
}
