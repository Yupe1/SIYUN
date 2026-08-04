package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.AuditPayload;
import com.yupe.siyun.controller.dto.GoodsCreatePayload;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.JsGoods;
import com.yupe.siyun.entity.JsGoodsCategory;
import com.yupe.siyun.entity.JsGoodsVO;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.JsGoodsCategoryMapper;
import com.yupe.siyun.mapper.JsGoodsMapper;
import com.yupe.siyun.mapper.JsGoodsVOMapper;
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
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminGoodsController extends AdminControllerSupport {
    private static final long MAX_GOODS_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_SERVICE_TAGS = Set.of("包邮", "退换无忧", "官方");

    @Autowired
    private JsGoodsMapper jsGoodsMapper;
    @Autowired
    private JsGoodsVOMapper jsGoodsVOMapper;
    @Autowired
    private JsGoodsCategoryMapper jsGoodsCategoryMapper;
    @Autowired
    private FileService fileService;

    @Value("${upload.profile.goods.image.path}")
    private String goodsImagePath;

    @GetMapping("/goods")
    @RequiresPermission("admin:goods:list")
    public Object goods(@RequestParam(defaultValue = "1") Long page,
                        @RequestParam(defaultValue = "10") Long size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer cateId,
                        @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<JsGoods> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) wrapper.and(w -> w.like(JsGoods::getGoodsName, keyword).or().like(JsGoods::getKeywords, keyword));
        if (cateId != null) wrapper.eq(JsGoods::getCateId, cateId);
        if (status != null) wrapper.eq(JsGoods::getStatus, status);
        wrapper.orderByDesc(JsGoods::getCreateTime);
        Page<JsGoods> data = new Page<>(page, size);
        jsGoodsMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "商品列表");
    }

    @GetMapping("/goods/{id}")
    @RequiresPermission("admin:goods:detail")
    public Object goodsDetail(@PathVariable Integer id) {
        JsGoods goods = jsGoodsMapper.selectById(id);
        JsGoodsVO view = jsGoodsVOMapper.selectById(id);
        return ResultData.success(new String[]{"goods", "view"}, new Object[]{goods, view}, "商品详情");
    }

    @PostMapping("/goods")
    @RequiresPermission("admin:goods:add")
    public Object addGoods(@RequestBody GoodsCreatePayload payload, HttpSession session) {
        validateGoodsPayload(payload);
        JsGoods goods = new JsGoods();
        goods.setCateId(payload.getCateId());
        goods.setGoodsName(payload.getGoodsName().trim());
        goods.setKeywords(hasText(payload.getKeywords()) ? payload.getKeywords().trim() : payload.getGoodsName().trim());
        goods.setMainPicUrl(payload.getImageUrls().stream().map(String::trim).collect(Collectors.joining(",")));
        goods.setPriceOriginal(payload.getPriceOriginal());
        goods.setIntro(hasText(payload.getIntro()) ? payload.getIntro().trim() : null);
        goods.setServiceTags(normalizeServiceTags(payload.getServiceTags()));
        goods.setCreateBy(currentUser(session).getId());
        goods.setStatus(1);
        goods.setRecommendStatus(0);
        jsGoodsMapper.insert(goods);
        return ResultData.success("goods", goods, "商品已添加并等待审核");
    }

    @PostMapping("/upload/goods-image")
    @RequiresPermission("admin:goods:add")
    public Object uploadGoodsImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择商品图片");
        }
        if (file.getSize() > MAX_GOODS_IMAGE_SIZE) {
            throw new MyException(ErrorType.FORMATE_ERROR, "商品图片不能超过5MB");
        }
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new MyException(ErrorType.FORMATE_ERROR, "仅支持jpg、png、webp图片");
        }
        try {
            String url = fileService.uploadFile(file, goodsImagePath);
            return ResultData.success("imageUrl", url, "商品图片上传成功");
        } catch (IOException e) {
            throw new MyException(ErrorType.OPERATION_FAILED, "商品图片上传失败，请检查上传目录权限");
        }
    }

    @PutMapping("/goods/{id}")
    @RequiresPermission("admin:goods:update")
    public Object updateGoods(@PathVariable Integer id, @RequestBody JsGoods goods, HttpSession session) {
        goods.setId(id);
        goods.setUpdateBy(currentUser(session).getId());
        jsGoodsMapper.updateById(goods);
        return ResultData.success("商品已更新");
    }

    @DeleteMapping("/goods/{id}")
    @RequiresPermission("admin:goods:delete")
    public Object deleteGoods(@PathVariable Integer id, HttpSession session) {
        JsGoods goods = new JsGoods();
        goods.setId(id);
        goods.setStatus(0);
        goods.setUpdateBy(currentUser(session).getId());
        jsGoodsMapper.updateById(goods);
        return ResultData.success("商品已删除");
    }

    @PostMapping("/goods/{id}/audit")
    @RequiresPermission("admin:goods:audit")
    public Object auditGoods(@PathVariable Integer id, @RequestBody AuditPayload payload, HttpSession session) {
        JsGoods goods = jsGoodsMapper.selectById(id);
        if (goods == null) throw new MyException(ErrorType.WRONG_INFO, "商品不存在");
        goods.setStatus(Objects.equals(payload.getAuditResult(), 1) ? 2 : 1);
        goods.setUpdateBy(currentUser(session).getId());
        jsGoodsMapper.updateById(goods);
        addAuditLog(id, 2, goods.getCreateBy(), payload.getAuditResult(), payload, session);
        return ResultData.success("商品审核完成");
    }

    @GetMapping("/goods-categories")
    @RequiresPermission("admin:goods:category")
    public Object goodsCategories(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<JsGoodsCategory> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) wrapper.like(JsGoodsCategory::getCateName, keyword);
        wrapper.orderByAsc(JsGoodsCategory::getParentId).orderByAsc(JsGoodsCategory::getSortNum);
        return ResultData.success("categories", jsGoodsCategoryMapper.selectList(wrapper), "商品分类");
    }

    @PostMapping("/goods-categories")
    @RequiresPermission("admin:goods:category:add")
    public Object addGoodsCategory(@RequestBody JsGoodsCategory category, HttpSession session) {
        category.setCreateBy(currentUser(session).getId());
        jsGoodsCategoryMapper.insert(category);
        return ResultData.success("category", category, "商品分类已添加");
    }

    @PutMapping("/goods-categories/{id}")
    @RequiresPermission("admin:goods:category:update")
    public Object updateGoodsCategory(@PathVariable Integer id, @RequestBody JsGoodsCategory category, HttpSession session) {
        category.setId(id);
        category.setUpdateBy(currentUser(session).getId());
        jsGoodsCategoryMapper.updateById(category);
        return ResultData.success("商品分类已更新");
    }

    @DeleteMapping("/goods-categories/{id}")
    @RequiresPermission("admin:goods:category:delete")
    public Object deleteGoodsCategory(@PathVariable Integer id) {
        jsGoodsCategoryMapper.deleteById(id);
        return ResultData.success("商品分类已删除");
    }

    private void validateGoodsPayload(GoodsCreatePayload payload) {
        if (payload == null || payload.getCateId() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择商品分类");
        }
        JsGoodsCategory category = jsGoodsCategoryMapper.selectById(payload.getCateId());
        if (category == null || !Objects.equals(category.getStatus(), 1)) {
            throw new MyException(ErrorType.WRONG_INFO, "选择的商品分类不存在或已禁用");
        }
        if (!hasText(payload.getGoodsName())) {
            throw new MyException(ErrorType.WRONG_INFO, "商品名称不能为空");
        }
        List<String> imageUrls = payload.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty() || imageUrls.size() > 9
                || imageUrls.stream().anyMatch(url -> !hasText(url) || url.contains(","))) {
            throw new MyException(ErrorType.WRONG_INFO, "请上传1至9张有效的商品图片");
        }
        BigDecimal price = payload.getPriceOriginal();
        if (price == null || price.scale() != 2 || price.signum() < 0 || price.precision() > 10) {
            throw new MyException(ErrorType.WRONG_INFO, "商品原价必须是两位小数且不能为负数");
        }
        if (payload.getServiceTags() != null
                && payload.getServiceTags().stream().anyMatch(tag -> !ALLOWED_SERVICE_TAGS.contains(tag))) {
            throw new MyException(ErrorType.WRONG_INFO, "包含不支持的服务标签");
        }
    }

    private String normalizeServiceTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return String.join(",", new LinkedHashSet<>(tags));
    }
}
