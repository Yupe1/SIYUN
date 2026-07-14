package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.ApxCourseCollectLog;
import com.yupe.siyun.entity.ApxCourseLikeLog;
import com.yupe.siyun.entity.CoComment;
import com.yupe.siyun.entity.JsCourse;
import com.yupe.siyun.entity.JsGoods;
import com.yupe.siyun.entity.OpOrder;
import com.yupe.siyun.entity.QfPositionApply;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.ApxCourseCollectLogMapper;
import com.yupe.siyun.mapper.ApxCourseLikeLogMapper;
import com.yupe.siyun.mapper.CoCommentMapper;
import com.yupe.siyun.mapper.FrontUserMapper;
import com.yupe.siyun.mapper.JsCourseMapper;
import com.yupe.siyun.mapper.JsGoodsMapper;
import com.yupe.siyun.mapper.OpOrderMapper;
import com.yupe.siyun.mapper.QfPositionApplyMapper;
import com.yupe.siyun.util.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminHomeController extends AdminControllerSupport {

    @Autowired
    private OpOrderMapper opOrderMapper;
    @Autowired
    private JsCourseMapper jsCourseMapper;
    @Autowired
    private JsGoodsMapper jsGoodsMapper;
    @Autowired
    private FrontUserMapper frontUserMapper;
    @Autowired
    private QfPositionApplyMapper qfPositionApplyMapper;
    @Autowired
    private CoCommentMapper coCommentMapper;
    @Autowired
    private ApxCourseLikeLogMapper apxCourseLikeLogMapper;
    @Autowired
    private ApxCourseCollectLogMapper apxCourseCollectLogMapper;

    @GetMapping("/home/index-data")
    public Object homeIndexData() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("todayOrderCount", countOrders(today, null));
        summary.put("todaySalesAmount", sumOrderPay(today, (Integer) null));
        summary.put("yesterdayOrderCount", countOrders(yesterday, null));
        summary.put("yesterdaySalesAmount", sumOrderPay(yesterday, (Integer) null));
        summary.put("last7DaysSalesAmount", sumOrderPay(today.minusDays(6), today.plusDays(1)));
        summary.put("courseCount", jsCourseMapper.selectCount(new LambdaQueryWrapper<>()));
        summary.put("goodsCount", jsGoodsMapper.selectCount(new LambdaQueryWrapper<JsGoods>().ne(JsGoods::getStatus, 0)));
        summary.put("userCount", frontUserMapper.selectCount(new LambdaQueryWrapper<>()));
        summary.put("pendingCourseAudit", jsCourseMapper.selectCount(new LambdaQueryWrapper<JsCourse>().in(JsCourse::getStatusAudit, 0, 1)));
        summary.put("pendingReturns", opOrderMapper.selectCount(new LambdaQueryWrapper<OpOrder>().in(OpOrder::getStatus, 5, 6)));
        summary.put("pendingCreatorApply", qfPositionApplyMapper.selectCount(new LambdaQueryWrapper<QfPositionApply>().in(QfPositionApply::getStatus, 0, 1)));

        Map<String, Object> todo = new LinkedHashMap<>();
        todo.put("goodsWaitPay", countOrderStatus(2, 0));
        todo.put("goodsWaitDeliver", countOrderStatus(2, 2));
        todo.put("goodsDelivered", countOrderStatus(2, 3));
        todo.put("goodsCompleted", countOrderStatus(2, 4));
        todo.put("courseWaitPay", countOrderStatus(1, 0));
        todo.put("courseCompleted", countOrderStatus(1, 4));
        todo.put("returns", opOrderMapper.selectCount(new LambdaQueryWrapper<OpOrder>().in(OpOrder::getStatus, 5, 6, 7)));
        todo.put("allOrders", opOrderMapper.selectCount(new LambdaQueryWrapper<>()));

        return ResultData.success(
                new String[]{"summary", "todo", "hourlyOrders"},
                new Object[]{summary, todo, hourlyOrders(today)},
                "后台首页数据"
        );
    }

    @GetMapping("/statistics")
    @RequiresPermission("admin:stats:view")
    public Object statistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("courseSales", opOrderMapper.selectMaps(new QueryWrapper<OpOrder>()
                .select("entity_id AS entityId", "SUM(total_quantity) AS total")
                .eq("entity_type", 1)
                .groupBy("entity_id")
                .orderByDesc("total")));
        stats.put("goodsSales", opOrderMapper.selectMaps(new QueryWrapper<OpOrder>()
                .select("entity_id AS entityId", "SUM(total_quantity) AS total")
                .eq("entity_type", 2)
                .groupBy("entity_id")
                .orderByDesc("total")));
        stats.put("courseLikes", apxCourseLikeLogMapper.selectMaps(new QueryWrapper<ApxCourseLikeLog>()
                .select("course_id AS courseId", "COUNT(1) AS total")
                .groupBy("course_id")
                .orderByDesc("total")));
        stats.put("courseCollects", apxCourseCollectLogMapper.selectMaps(new QueryWrapper<ApxCourseCollectLog>()
                .select("course_id AS courseId", "COUNT(1) AS total")
                .groupBy("course_id")
                .orderByDesc("total")));
        stats.put("commentCount", coCommentMapper.selectCount(new LambdaQueryWrapper<CoComment>()));
        return ResultData.success("statistics", stats, "统计数据");
    }

    private Long countOrderStatus(Integer entityType, Integer status) {
        return opOrderMapper.selectCount(
                new LambdaQueryWrapper<OpOrder>()
                        .eq(OpOrder::getEntityType, entityType)
                        .eq(OpOrder::getStatus, status)
        );
    }

    private Long countOrders(LocalDate day, Integer entityType) {
        LambdaQueryWrapper<OpOrder> wrapper = new LambdaQueryWrapper<OpOrder>()
                .ge(OpOrder::getCreateTime, day.atStartOfDay())
                .lt(OpOrder::getCreateTime, day.plusDays(1).atStartOfDay());
        if (entityType != null) wrapper.eq(OpOrder::getEntityType, entityType);
        return opOrderMapper.selectCount(wrapper);
    }

    private BigDecimal sumOrderPay(LocalDate day, Integer entityType) {
        QueryWrapper<OpOrder> wrapper = new QueryWrapper<OpOrder>()
                .select("COALESCE(SUM(price_pay),0)")
                .ge("create_time", day.atStartOfDay())
                .lt("create_time", day.plusDays(1).atStartOfDay());
        if (entityType != null) wrapper.eq("entity_type", entityType);
        return decimalResult(opOrderMapper.selectObjs(wrapper));
    }

    private BigDecimal sumOrderPay(LocalDate startDay, LocalDate endDay) {
        QueryWrapper<OpOrder> wrapper = new QueryWrapper<OpOrder>()
                .select("COALESCE(SUM(price_pay),0)")
                .ge("create_time", startDay.atStartOfDay())
                .lt("create_time", endDay.atStartOfDay());
        return decimalResult(opOrderMapper.selectObjs(wrapper));
    }

    private List<Map<String, Object>> hourlyOrders(LocalDate day) {
        return opOrderMapper.selectMaps(new QueryWrapper<OpOrder>()
                .select("HOUR(create_time) AS hour", "COUNT(1) AS count")
                .ge("create_time", day.atStartOfDay())
                .lt("create_time", day.plusDays(1).atStartOfDay())
                .groupBy("HOUR(create_time)")
                .orderByAsc("hour"));
    }
}
