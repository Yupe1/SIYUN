package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.AuditPayload;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.JsGoods;
import com.yupe.siyun.entity.JsGoodsCategory;
import com.yupe.siyun.entity.JsGoodsVO;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.JsGoodsCategoryMapper;
import com.yupe.siyun.mapper.JsGoodsMapper;
import com.yupe.siyun.mapper.JsGoodsVOMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
public class AdminGoodsController extends AdminControllerSupport {

    @Autowired
    private JsGoodsMapper jsGoodsMapper;
    @Autowired
    private JsGoodsVOMapper jsGoodsVOMapper;
    @Autowired
    private JsGoodsCategoryMapper jsGoodsCategoryMapper;

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
    public Object addGoods(@RequestBody JsGoods goods, HttpSession session) {
        goods.setCreateBy(currentUser(session).getId());
        if (goods.getStatus() == null) goods.setStatus(1);
        if (goods.getRecommendStatus() == null) goods.setRecommendStatus(0);
        jsGoodsMapper.insert(goods);
        return ResultData.success("goods", goods, "商品已添加");
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
}
