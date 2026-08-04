package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.AdCourseCreatePayload;
import com.yupe.siyun.controller.dto.CouponGrantPayload;
import com.yupe.siyun.controller.dto.CouponGoodsBindPayload;
import com.yupe.siyun.controller.dto.CouponGoodsVO;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.JsCourse;
import com.yupe.siyun.entity.JsGoods;
import com.yupe.siyun.entity.OpCircleAd;
import com.yupe.siyun.entity.OpCoupon;
import com.yupe.siyun.entity.OpCouponGoods;
import com.yupe.siyun.entity.OpCouponUser;
import com.yupe.siyun.entity.OpCouponVO;
import com.yupe.siyun.entity.ObjFrontUser;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.FrontUserMapper;
import com.yupe.siyun.mapper.JsCourseMapper;
import com.yupe.siyun.mapper.JsGoodsMapper;
import com.yupe.siyun.mapper.OpCircleAdMapper;
import com.yupe.siyun.mapper.OpCouponGoodsMapper;
import com.yupe.siyun.mapper.OpCouponMapper;
import com.yupe.siyun.mapper.OpCouponUserMapper;
import com.yupe.siyun.mapper.OpCouponVOMapper;
import com.yupe.siyun.service.FileService;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminMarketingController extends AdminControllerSupport {

    private static final long MAX_AD_COUNT = 9;
    private static final long MAX_COUPON_IMAGE_SIZE = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_COUPON_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

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
    @Autowired
    private JsCourseMapper jsCourseMapper;
    @Autowired
    private JsGoodsMapper jsGoodsMapper;
    @Autowired
    private FrontUserMapper frontUserMapper;
    @Autowired
    private FileService fileService;

    @Value("${upload.profile.marketing.coupon.path}")
    private String couponImagePath;

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
        if (coupon.getStatusShelf() == null) coupon.setStatusShelf(0);
        if (coupon.getIssueType() == null) coupon.setIssueType(1);
        if (coupon.getApplyType() == null) coupon.setApplyType(0);
        validateCoupon(coupon, null);
        normalizeCoupon(coupon);
        coupon.setCreateBy(currentUser(session).getId());
        opCouponMapper.insert(coupon);
        return ResultData.success("coupon", coupon, "优惠券已添加");
    }

    @PostMapping("/upload/coupon-image")
    @RequiresPermission("admin:coupon:add")
    public Object uploadCouponImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择优惠券图片");
        }
        if (file.getSize() > MAX_COUPON_IMAGE_SIZE) {
            throw new MyException(ErrorType.FORMATE_ERROR, "优惠券图片不能超过2MB");
        }
        if (!ALLOWED_COUPON_IMAGE_TYPES.contains(file.getContentType())) {
            throw new MyException(ErrorType.FORMATE_ERROR, "仅支持jpg、png、webp图片");
        }
        try {
            String url = fileService.uploadFile(file, couponImagePath);
            return ResultData.success("imageUrl", url, "优惠券图片上传成功");
        } catch (IOException e) {
            throw new MyException(ErrorType.OPERATION_FAILED, "优惠券图片上传失败，请检查上传目录权限");
        }
    }

    @PutMapping("/coupons/{id}")
    @RequiresPermission("admin:coupon:update")
    public Object updateCoupon(@PathVariable Integer id, @RequestBody OpCoupon coupon, HttpSession session) {
        validateCoupon(coupon, id);
        normalizeCoupon(coupon);
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
    @Transactional
    public Object grantCoupon(@RequestBody CouponGrantPayload payload) {
        if (payload == null || payload.getCouponId() == null || payload.getUserId() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择用户和优惠券");
        }
        OpCoupon coupon = opCouponMapper.selectById(payload.getCouponId());
        LocalDateTime now = LocalDateTime.now();
        if (coupon == null || !Integer.valueOf(1).equals(coupon.getStatusShelf())
                || coupon.getStartTime() == null || coupon.getEndTime() == null
                || now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new MyException(ErrorType.WRONG_INFO, "选择的优惠券当前不可赠送");
        }
        Long bindingCount = opCouponGoodsMapper.selectCount(
                new LambdaQueryWrapper<OpCouponGoods>()
                        .eq(OpCouponGoods::getCouponId, coupon.getId())
                        .gt(OpCouponGoods::getQuota, 0)
        );
        if (bindingCount == 0) {
            throw new MyException(ErrorType.WRONG_INFO, "该优惠券尚未绑定商品或课程");
        }
        ObjFrontUser user = frontUserMapper.selectById(payload.getUserId());
        if (user == null || !Integer.valueOf(0).equals(user.getStatus())) {
            throw new MyException(ErrorType.WRONG_INFO, "选择的用户不存在或账号状态异常");
        }
        Long ownedCount = opCouponUserMapper.selectCount(
                new LambdaQueryWrapper<OpCouponUser>()
                        .eq(OpCouponUser::getCouponId, payload.getCouponId())
                        .eq(OpCouponUser::getUserId, payload.getUserId())
        );
        if (ownedCount > 0) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "该用户已经领取过这张优惠券，不能重复赠送");
        }
        OpCouponUser couponUser = new OpCouponUser();
        couponUser.setCouponId(payload.getCouponId());
        couponUser.setUserId(payload.getUserId());
        couponUser.setGetTime(now);
        couponUser.setStatus(0);
        try {
            opCouponUserMapper.insert(couponUser);
        } catch (DuplicateKeyException error) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "该用户已经领取过这张优惠券，不能重复赠送");
        }
        return ResultData.success("couponUser", couponUser, "优惠券已赠送");
    }

    @GetMapping("/coupon-grant-options")
    @RequiresPermission("admin:coupon:grant")
    public Object couponGrantOptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Integer> boundCouponIds = opCouponGoodsMapper.selectList(
                new LambdaQueryWrapper<OpCouponGoods>()
                        .gt(OpCouponGoods::getQuota, 0)
                        .select(OpCouponGoods::getCouponId)
        ).stream().map(OpCouponGoods::getCouponId).distinct().toList();
        List<OpCoupon> coupons = opCouponMapper.selectList(
                new LambdaQueryWrapper<OpCoupon>()
                        .eq(OpCoupon::getStatusShelf, 1)
                        .le(OpCoupon::getStartTime, now)
                        .ge(OpCoupon::getEndTime, now)
                        .orderByDesc(OpCoupon::getId)
        );
        List<ObjFrontUser> users = frontUserMapper.selectList(
                new LambdaQueryWrapper<ObjFrontUser>()
                        .eq(ObjFrontUser::getStatus, 0)
                        .select(ObjFrontUser::getId, ObjFrontUser::getNickName,
                                ObjFrontUser::getStuTel, ObjFrontUser::getStatus)
                        .orderByDesc(ObjFrontUser::getId)
        );
        List<OpCouponUser> grantedCouponUsers = opCouponUserMapper.selectList(
                new LambdaQueryWrapper<OpCouponUser>()
                        .select(OpCouponUser::getCouponId, OpCouponUser::getUserId)
        );
        return ResultData.success(
                new String[]{"coupons", "users", "boundCouponIds", "grantedCouponUsers"},
                new Object[]{coupons, users, boundCouponIds, grantedCouponUsers},
                "赠送优惠券选项"
        );
    }

    @GetMapping("/coupon-bind-options")
    @RequiresPermission("admin:coupon:goods:add")
    public Object couponBindOptions() {
        List<OpCoupon> coupons = opCouponMapper.selectList(
                new LambdaQueryWrapper<OpCoupon>()
                        .orderByDesc(OpCoupon::getId)
        );
        List<JsGoods> goods = jsGoodsMapper.selectList(
                new LambdaQueryWrapper<JsGoods>()
                        .ne(JsGoods::getStatus, 0)
                        .orderByDesc(JsGoods::getId)
        );
        List<JsCourse> courses = jsCourseMapper.selectList(
                new LambdaQueryWrapper<JsCourse>().orderByDesc(JsCourse::getId)
        );
        List<OpCouponGoods> bindings = opCouponGoodsMapper.selectList(
                new LambdaQueryWrapper<OpCouponGoods>()
                        .select(OpCouponGoods::getCouponId, OpCouponGoods::getGoodsId)
        );
        return ResultData.success(
                new String[]{"coupons", "goods", "courses", "bindings"},
                new Object[]{coupons, goods, courses, bindings},
                "商品课程优惠券选项"
        );
    }

    @PostMapping("/coupon-goods")
    @RequiresPermission("admin:coupon:goods:add")
    @Transactional
    public Object bindCouponGoods(@RequestBody CouponGoodsBindPayload payload, HttpSession session) {
        if (payload == null || payload.getCouponId() == null || payload.getGoodsId() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择优惠券和适用商品或课程");
        }
        if (!Integer.valueOf(1).equals(payload.getTargetType())
                && !Integer.valueOf(2).equals(payload.getTargetType())) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择实体商品或视频课程");
        }
        if (payload.getQuota() == null || payload.getQuota() < 1) {
            throw new MyException(ErrorType.WRONG_INFO, "优惠券数量必须为正整数");
        }
        OpCoupon coupon = opCouponMapper.selectById(payload.getCouponId());
        if (coupon == null) {
            throw new MyException(ErrorType.WRONG_INFO, "选择的优惠券不存在");
        }
        Integer applyType = coupon.getApplyType() == null ? 0 : coupon.getApplyType();
        if (!Integer.valueOf(0).equals(applyType) && !payload.getTargetType().equals(applyType)) {
            throw new MyException(ErrorType.WRONG_INFO, "优惠券适用类型与选择的商品或课程不一致");
        }
        if (Integer.valueOf(1).equals(payload.getTargetType())) {
            JsGoods goods = jsGoodsMapper.selectById(payload.getGoodsId());
            if (goods == null || Integer.valueOf(0).equals(goods.getStatus())) {
                throw new MyException(ErrorType.WRONG_INFO, "选择的商品不存在或已删除");
            }
        } else if (jsCourseMapper.selectById(payload.getGoodsId()) == null) {
            throw new MyException(ErrorType.WRONG_INFO, "选择的课程不存在");
        }
        Long duplicateCount = opCouponGoodsMapper.selectCount(
                new LambdaQueryWrapper<OpCouponGoods>()
                        .eq(OpCouponGoods::getCouponId, payload.getCouponId())
                        .eq(OpCouponGoods::getGoodsId, payload.getGoodsId())
        );
        if (duplicateCount > 0) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "该商品或课程已绑定这张优惠券");
        }
        if (Integer.valueOf(0).equals(applyType)) {
            OpCoupon update = new OpCoupon();
            update.setId(coupon.getId());
            update.setApplyType(payload.getTargetType());
            update.setUpdateBy(currentUser(session).getId());
            opCouponMapper.updateById(update);
        }
        OpCouponGoods binding = new OpCouponGoods();
        binding.setCouponId(payload.getCouponId());
        binding.setGoodsId(payload.getGoodsId());
        binding.setQuota(payload.getQuota());
        opCouponGoodsMapper.insert(binding);
        return ResultData.success("binding", binding, "优惠券已绑定");
    }

    @GetMapping("/coupon-goods")
    @RequiresPermission("admin:coupon:user")
    public Object couponGoods(@RequestParam(defaultValue = "1") Long page,
                              @RequestParam(defaultValue = "10") Long size,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer couponId,
                              @RequestParam(required = false) Integer applyType) {
        LambdaQueryWrapper<OpCouponGoods> wrapper = new LambdaQueryWrapper<>();
        if (couponId != null) wrapper.eq(OpCouponGoods::getCouponId, couponId);
        if (applyType != null) {
            List<Integer> couponIds = opCouponMapper.selectList(
                    new LambdaQueryWrapper<OpCoupon>().eq(OpCoupon::getApplyType, applyType)
                            .select(OpCoupon::getId)
            ).stream().map(OpCoupon::getId).toList();
            if (couponIds.isEmpty()) {
                return ResultData.success("page", new Page<CouponGoodsVO>(page, size), "优惠券适用商品查询");
            }
            wrapper.in(OpCouponGoods::getCouponId, couponIds);
        }
        if (hasText(keyword)) {
            Set<Integer> matchingItemIds = new HashSet<>();
            jsGoodsMapper.selectList(new LambdaQueryWrapper<JsGoods>()
                    .like(JsGoods::getGoodsName, keyword)
                    .select(JsGoods::getId))
                    .forEach(goods -> matchingItemIds.add(goods.getId()));
            jsCourseMapper.selectList(new LambdaQueryWrapper<JsCourse>()
                    .like(JsCourse::getTitle, keyword)
                    .select(JsCourse::getId))
                    .forEach(course -> matchingItemIds.add(course.getId()));
            if (matchingItemIds.isEmpty()) {
                return ResultData.success("page", new Page<CouponGoodsVO>(page, size), "优惠券适用商品查询");
            }
            wrapper.in(OpCouponGoods::getGoodsId, matchingItemIds);
        }
        wrapper.orderByDesc(OpCouponGoods::getId);

        Page<OpCouponGoods> relationPage = new Page<>(page, size);
        opCouponGoodsMapper.selectPage(relationPage, wrapper);
        List<OpCouponGoods> relations = relationPage.getRecords();
        Set<Integer> couponIds = relations.stream().map(OpCouponGoods::getCouponId).collect(Collectors.toSet());
        Set<Integer> itemIds = relations.stream().map(OpCouponGoods::getGoodsId).collect(Collectors.toSet());

        Map<Integer, OpCoupon> couponMap = couponIds.isEmpty() ? Collections.emptyMap() :
                opCouponMapper.selectBatchIds(couponIds).stream()
                        .collect(Collectors.toMap(OpCoupon::getId, Function.identity()));
        Map<Integer, JsGoods> goodsMap = itemIds.isEmpty() ? Collections.emptyMap() :
                jsGoodsMapper.selectBatchIds(itemIds).stream()
                        .collect(Collectors.toMap(JsGoods::getId, Function.identity()));
        Map<Integer, JsCourse> courseMap = itemIds.isEmpty() ? Collections.emptyMap() :
                jsCourseMapper.selectBatchIds(itemIds).stream()
                        .collect(Collectors.toMap(JsCourse::getId, Function.identity()));

        List<CouponGoodsVO> records = relations.stream()
                .map(relation -> toCouponGoodsVO(relation, couponMap, goodsMap, courseMap))
                .toList();
        Page<CouponGoodsVO> resultPage = new Page<>(page, size, relationPage.getTotal());
        resultPage.setRecords(records);
        return ResultData.success("page", resultPage, "优惠券适用商品查询");
    }

    @GetMapping("/ad-course-options")
    @RequiresPermission("admin:ad:add")
    public Object adCourseOptions() {
        List<JsCourse> courses = jsCourseMapper.selectList(
                new LambdaQueryWrapper<JsCourse>().orderByDesc(JsCourse::getId)
        );
        List<Integer> usedCourseIds = opCircleAdMapper.selectList(
                new LambdaQueryWrapper<OpCircleAd>()
                        .isNotNull(OpCircleAd::getCourseId)
                        .select(OpCircleAd::getCourseId)
        ).stream().map(OpCircleAd::getCourseId).distinct().toList();
        long currentCount = opCircleAdMapper.selectCount(null);
        return ResultData.success(
                new String[]{"courses", "usedCourseIds", "currentCount", "maxCount"},
                new Object[]{courses, usedCourseIds, currentCount, MAX_AD_COUNT},
                "轮播图课程选项"
        );
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
    @Transactional
    public Object addAd(@RequestBody AdCourseCreatePayload payload, HttpSession session) {
        if (payload == null || payload.getCourseId() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择课程");
        }
        if (opCircleAdMapper.selectCount(null) >= MAX_AD_COUNT) {
            throw new MyException(ErrorType.WRONG_INFO, "轮播图最多只能添加9个内容");
        }
        JsCourse course = jsCourseMapper.selectById(payload.getCourseId());
        if (course == null) {
            throw new MyException(ErrorType.WRONG_INFO, "选择的课程不存在");
        }
        if (!hasText(course.getCoverUrl())) {
            throw new MyException(ErrorType.WRONG_INFO, "该课程没有封面图片，不能加入轮播图");
        }
        Long duplicateCount = opCircleAdMapper.selectCount(
                new LambdaQueryWrapper<OpCircleAd>().eq(OpCircleAd::getCourseId, course.getId())
        );
        if (duplicateCount > 0) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "该课程已在轮播图中");
        }

        LocalDateTime now = LocalDateTime.now();
        OpCircleAd ad = new OpCircleAd();
        ad.setCourseId(course.getId());
        ad.setPicUrl(course.getCoverUrl());
        ad.setTitle(course.getTitle());
        ad.setIntro(course.getIntro());
        ad.setPositionType(1);
        ad.setStartTime(now);
        ad.setEndTime(now.plusYears(10));
        ad.setStatusShow(1);
        ad.setSortNum(0);
        ad.setCreateBy(currentUser(session).getId());
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

    private CouponGoodsVO toCouponGoodsVO(OpCouponGoods relation,
                                          Map<Integer, OpCoupon> couponMap,
                                          Map<Integer, JsGoods> goodsMap,
                                          Map<Integer, JsCourse> courseMap) {
        CouponGoodsVO vo = new CouponGoodsVO();
        vo.setId(relation.getId());
        vo.setGoodsId(relation.getGoodsId());
        vo.setCouponId(relation.getCouponId());
        vo.setQuota(relation.getQuota());

        OpCoupon coupon = couponMap.get(relation.getCouponId());
        if (coupon != null) {
            vo.setCouponSn(coupon.getCouponSn());
            vo.setCouponName(coupon.getCouponName());
            vo.setAmount(coupon.getAmount());
            vo.setApplyType(coupon.getApplyType());
            vo.setStatusShelf(coupon.getStatusShelf());
            vo.setStartTime(coupon.getStartTime());
            vo.setEndTime(coupon.getEndTime());
        }

        JsGoods goods = goodsMap.get(relation.getGoodsId());
        JsCourse course = courseMap.get(relation.getGoodsId());
        if (coupon != null && Integer.valueOf(2).equals(coupon.getApplyType())) {
            vo.setItemName(course == null ? "课程已删除" : course.getTitle());
        } else if (coupon != null && Integer.valueOf(1).equals(coupon.getApplyType())) {
            vo.setItemName(goods == null ? "商品已删除" : goods.getGoodsName());
        } else if (goods != null && course != null) {
            vo.setItemName(goods.getGoodsName() + " / " + course.getTitle());
        } else if (goods != null) {
            vo.setItemName(goods.getGoodsName());
        } else if (course != null) {
            vo.setItemName(course.getTitle());
        } else {
            vo.setItemName("商品/课程已删除");
        }
        return vo;
    }

    private void validateCoupon(OpCoupon coupon, Integer excludeId) {
        if (coupon == null || !hasText(coupon.getCouponSn())) {
            throw new MyException(ErrorType.WRONG_INFO, "优惠券编号不能为空");
        }
        if (!hasText(coupon.getCouponName())) {
            throw new MyException(ErrorType.WRONG_INFO, "优惠券名称不能为空");
        }
        if (coupon.getAmount() == null || !Double.isFinite(coupon.getAmount())
                || coupon.getAmount() <= 0 || coupon.getAmount() > 99999999.99
                || BigDecimal.valueOf(coupon.getAmount()).stripTrailingZeros().scale() > 2) {
            throw new MyException(ErrorType.WRONG_INFO, "优惠券金额必须为正数且最多保留两位小数");
        }
        if (coupon.getStartTime() == null || coupon.getEndTime() == null
                || !coupon.getEndTime().isAfter(coupon.getStartTime())) {
            throw new MyException(ErrorType.WRONG_INFO, "结束时间必须晚于开始时间");
        }
        if (!Set.of(0, 1).contains(coupon.getStatusShelf())) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择正确的上下线状态");
        }
        if (!Set.of(1, 2, 3).contains(coupon.getIssueType())) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择正确的发放类型");
        }
        if (!Set.of(0, 1, 2).contains(coupon.getApplyType())) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择正确的适用类型");
        }
        LambdaQueryWrapper<OpCoupon> duplicateWrapper = new LambdaQueryWrapper<OpCoupon>()
                .eq(OpCoupon::getCouponSn, coupon.getCouponSn().trim());
        if (excludeId != null) duplicateWrapper.ne(OpCoupon::getId, excludeId);
        if (opCouponMapper.selectCount(duplicateWrapper) > 0) {
            throw new MyException(ErrorType.REPEAT_OPERATION, "优惠券编号已存在");
        }
    }

    private void normalizeCoupon(OpCoupon coupon) {
        coupon.setCouponSn(coupon.getCouponSn().trim());
        coupon.setCouponName(coupon.getCouponName().trim());
        if (!hasText(coupon.getImgUrl())) coupon.setImgUrl(null);
    }
}
