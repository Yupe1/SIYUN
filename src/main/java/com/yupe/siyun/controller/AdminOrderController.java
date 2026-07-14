package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.AuditPayload;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.OpOrder;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.OpOrderMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
public class AdminOrderController extends AdminControllerSupport {

    @Autowired
    private OpOrderMapper opOrderMapper;

    @GetMapping("/orders")
    @RequiresPermission("admin:order:list")
    public Object orders(@RequestParam(defaultValue = "1") Long page,
                         @RequestParam(defaultValue = "10") Long size,
                         @RequestParam(required = false) Integer entityType,
                         @RequestParam(required = false) Integer status,
                         @RequestParam(required = false) Integer userId) {
        LambdaQueryWrapper<OpOrder> wrapper = new LambdaQueryWrapper<>();
        if (entityType != null) wrapper.eq(OpOrder::getEntityType, entityType);
        if (status != null) wrapper.eq(OpOrder::getStatus, status);
        if (userId != null) wrapper.eq(OpOrder::getUserId, userId);
        wrapper.orderByDesc(OpOrder::getCreateTime);
        Page<OpOrder> data = new Page<>(page, size);
        opOrderMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "订单列表");
    }

    @GetMapping("/orders/returns")
    @RequiresPermission("admin:order:return")
    public Object returnOrders(@RequestParam(defaultValue = "1") Long page,
                               @RequestParam(defaultValue = "10") Long size,
                               @RequestParam(required = false) Integer entityType) {
        LambdaQueryWrapper<OpOrder> wrapper = new LambdaQueryWrapper<OpOrder>().in(OpOrder::getStatus, 5, 6, 7);
        if (entityType != null) wrapper.eq(OpOrder::getEntityType, entityType);
        wrapper.orderByDesc(OpOrder::getUpdateTime);
        Page<OpOrder> data = new Page<>(page, size);
        opOrderMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "退货订单列表");
    }

    @PutMapping("/orders/{id}/status")
    @RequiresPermission("admin:order:update")
    public Object updateOrderStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body, HttpSession session) {
        OpOrder order = new OpOrder();
        order.setId(id);
        order.setStatus(bodyInt(body, "status"));
        order.setDeliverySn(bodyString(body, "deliverySn"));
        order.setUpdateBy(currentUser(session).getId());
        if (Objects.equals(order.getStatus(), 3)) {
            order.setDeliveryTime(LocalDateTime.now());
        }
        opOrderMapper.updateById(order);
        return ResultData.success("订单状态已更新");
    }

    @PostMapping("/orders/{id}/return-audit")
    @RequiresPermission("admin:order:return:audit")
    public Object auditReturnOrder(@PathVariable Integer id, @RequestBody AuditPayload payload, HttpSession session) {
        OpOrder old = opOrderMapper.selectById(id);
        if (old == null) throw new MyException(ErrorType.WRONG_INFO, "订单不存在");
        OpOrder order = new OpOrder();
        order.setId(id);
        order.setStatus(Objects.equals(payload.getAuditResult(), 1) ? 6 : 4);
        order.setUpdateBy(currentUser(session).getId());
        opOrderMapper.updateById(order);
        return ResultData.success("退货审核完成");
    }
}
