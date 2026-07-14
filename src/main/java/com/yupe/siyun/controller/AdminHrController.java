package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.RoleAssignPayload;
import com.yupe.siyun.controller.dto.StaffPayload;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.JsDept;
import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.ObjFrontUser;
import com.yupe.siyun.entity.QfPositionApply;
import com.yupe.siyun.entity.QfUserLockLog;
import com.yupe.siyun.entity.QfUserRole;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.BackUserMapper;
import com.yupe.siyun.mapper.FrontUserMapper;
import com.yupe.siyun.mapper.JsDeptMapper;
import com.yupe.siyun.mapper.QfPositionApplyMapper;
import com.yupe.siyun.mapper.QfUserLockLogMapper;
import com.yupe.siyun.mapper.QfUserRoleMapper;
import com.yupe.siyun.service.BackUserService;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import com.yupe.siyun.util.SafeUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminHrController extends AdminControllerSupport {

    @Autowired
    private BackUserService backUserService;
    @Autowired
    private SafeUtil safeUtil;
    @Autowired
    private FrontUserMapper frontUserMapper;
    @Autowired
    private BackUserMapper backUserMapper;
    @Autowired
    private JsDeptMapper jsDeptMapper;
    @Autowired
    private QfPositionApplyMapper qfPositionApplyMapper;
    @Autowired
    private QfUserLockLogMapper qfUserLockLogMapper;
    @Autowired
    private QfUserRoleMapper qfUserRoleMapper;

    @GetMapping("/front-users")
    @RequiresPermission("admin:user:list")
    public Object frontUsers(@RequestParam(defaultValue = "1") Long page,
                             @RequestParam(defaultValue = "10") Long size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<ObjFrontUser> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) wrapper.and(w -> w.like(ObjFrontUser::getNickName, keyword).or().like(ObjFrontUser::getStuTel, keyword));
        if (status != null) wrapper.eq(ObjFrontUser::getStatus, status);
        Page<ObjFrontUser> data = new Page<>(page, size);
        frontUserMapper.selectPage(data, wrapper);
        data.getRecords().forEach(user -> user.setPassword(null));
        return ResultData.success("page", data, "前台用户列表");
    }

    @PutMapping("/front-users/{id}")
    @RequiresPermission("admin:user:update")
    public Object updateFrontUser(@PathVariable Integer id, @RequestBody ObjFrontUser user) {
        user.setId(id);
        user.setPassword(null);
        frontUserMapper.updateById(user);
        return ResultData.success("前台用户已更新");
    }

    @PutMapping("/front-users/{id}/status")
    @RequiresPermission("admin:user:lock")
    public Object updateFrontUserStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        ObjFrontUser user = new ObjFrontUser();
        user.setId(id);
        user.setStatus(bodyInt(body, "status"));
        frontUserMapper.updateById(user);
        return ResultData.success("用户状态已更新");
    }

    @PostMapping("/front-users/{id}/lock")
    @RequiresPermission("admin:user:lock")
    public Object lockFrontUser(@PathVariable Integer id, @RequestBody QfUserLockLog log, HttpSession session) {
        ObjFrontUser user = new ObjFrontUser();
        user.setId(id);
        user.setStatus(2);
        frontUserMapper.updateById(user);
        log.setUserId(id);
        log.setOperatorBy(currentUser(session).getId());
        if (log.getUnlockStatus() == null) log.setUnlockStatus(0);
        qfUserLockLogMapper.insert(log);
        return ResultData.success("用户已封停");
    }

    @PostMapping("/front-users/{id}/unlock")
    @RequiresPermission("admin:user:lock")
    public Object unlockFrontUser(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        ObjFrontUser user = new ObjFrontUser();
        user.setId(id);
        user.setStatus(0);
        frontUserMapper.updateById(user);
        List<QfUserLockLog> logs = qfUserLockLogMapper.selectList(
                new LambdaQueryWrapper<QfUserLockLog>()
                        .eq(QfUserLockLog::getUserId, id)
                        .eq(QfUserLockLog::getUnlockStatus, 0)
        );
        for (QfUserLockLog log : logs) {
            log.setUnlockStatus(1);
            log.setUnlockTime(LocalDateTime.now());
            log.setUnlockReason(bodyString(body, "unlockReason"));
            qfUserLockLogMapper.updateById(log);
        }
        return ResultData.success("用户已解封");
    }

    @GetMapping("/staff-users")
    @RequiresPermission("admin:staff:list")
    public Object staffUsers(@RequestParam(defaultValue = "1") Long page,
                             @RequestParam(defaultValue = "10") Long size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer deptId,
                             @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<ObjBackUser> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) wrapper.and(w -> w.like(ObjBackUser::getName, keyword).or().like(ObjBackUser::getTel, keyword));
        if (deptId != null) wrapper.eq(ObjBackUser::getDeptId, deptId);
        if (status != null) wrapper.eq(ObjBackUser::getStatus, status);
        Page<ObjBackUser> data = new Page<>(page, size);
        backUserMapper.selectPage(data, wrapper);
        data.getRecords().forEach(user -> user.setPassword(null));
        return ResultData.success("page", data, "后台人员列表");
    }

    @PostMapping("/staff-users")
    @RequiresPermission("admin:staff:add")
    public Object addStaff(@RequestBody StaffPayload payload) {
        ObjBackUser user = payload.toUser();
        if (user.getRegisterIp() == null) user.setRegisterIp("0.0.0.0");
        backUserService.register(user);
        assignUserRoles(user.getId(), payload.getRoleIds());
        user.setPassword(null);
        return ResultData.success("staff", user, "后台人员已添加");
    }

    @PutMapping("/staff-users/{id}")
    @RequiresPermission("admin:staff:update")
    public Object updateStaff(@PathVariable Integer id, @RequestBody StaffPayload payload) {
        ObjBackUser user = payload.toUser();
        user.setId(id);
        if (!hasText(user.getPassword())) {
            user.setPassword(null);
        } else if (!user.getPassword().contains("$")) {
            user.setPassword(safeUtil.transPassword(user.getPassword()));
        }
        backUserMapper.updateById(user);
        if (payload.getRoleIds() != null) {
            assignUserRoles(id, payload.getRoleIds());
        }
        return ResultData.success("后台人员已更新");
    }

    @DeleteMapping("/staff-users/{id}")
    @RequiresPermission("admin:staff:delete")
    public Object deleteStaff(@PathVariable Integer id) {
        backUserMapper.deleteById(id);
        qfUserRoleMapper.delete(new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getBackUserId, id));
        return ResultData.success("后台人员已删除");
    }

    @PutMapping("/staff-users/{id}/roles")
    @RequiresPermission("admin:staff:assign")
    public Object assignStaffRoles(@PathVariable Integer id, @RequestBody RoleAssignPayload payload) {
        assignUserRoles(id, payload.getRoleIds());
        return ResultData.success("角色已分配");
    }

    @GetMapping("/depts")
    @RequiresPermission("admin:dept:list")
    public Object depts(@RequestParam(required = false) Integer parentId) {
        LambdaQueryWrapper<JsDept> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) wrapper.eq(JsDept::getParentId, parentId);
        wrapper.orderByAsc(JsDept::getParentId).orderByAsc(JsDept::getId);
        return ResultData.success("depts", jsDeptMapper.selectList(wrapper), "部门列表");
    }

    @PostMapping("/depts")
    @RequiresPermission("admin:dept:add")
    public Object addDept(@RequestBody JsDept dept, HttpSession session) {
        dept.setCreateBy(currentUser(session).getId());
        if (dept.getStatus() == null) dept.setStatus(1);
        jsDeptMapper.insert(dept);
        return ResultData.success("dept", dept, "部门已添加");
    }

    @PutMapping("/depts/{id}")
    @RequiresPermission("admin:dept:update")
    public Object updateDept(@PathVariable Integer id, @RequestBody JsDept dept, HttpSession session) {
        dept.setId(id);
        dept.setUpdateBy(currentUser(session).getId());
        jsDeptMapper.updateById(dept);
        return ResultData.success("部门已更新");
    }

    @DeleteMapping("/depts/{id}")
    @RequiresPermission("admin:dept:delete")
    public Object deleteDept(@PathVariable Integer id) {
        jsDeptMapper.deleteById(id);
        return ResultData.success("部门已删除");
    }

    @GetMapping("/position-applies")
    @RequiresPermission("admin:apply:list")
    public Object positionApplies(@RequestParam(defaultValue = "1") Long page,
                                  @RequestParam(defaultValue = "10") Long size,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(required = false) String targetPosition) {
        LambdaQueryWrapper<QfPositionApply> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(QfPositionApply::getStatus, status);
        if (hasText(targetPosition)) wrapper.eq(QfPositionApply::getTargetPosition, targetPosition);
        wrapper.orderByDesc(QfPositionApply::getApplyTime);
        Page<QfPositionApply> data = new Page<>(page, size);
        qfPositionApplyMapper.selectPage(data, wrapper);
        return ResultData.success("page", data, "申请作品/职位列表");
    }

    @PostMapping("/position-applies/{id}/audit")
    @RequiresPermission("admin:apply:audit")
    public Object auditPositionApply(@PathVariable Integer id, @RequestBody Map<String, Object> body, HttpSession session) {
        QfPositionApply apply = qfPositionApplyMapper.selectById(id);
        if (apply == null) throw new MyException(ErrorType.WRONG_INFO, "申请不存在");
        Integer status = bodyInt(body, "status");
        apply.setStatus(status == null ? 1 : status);
        apply.setHandleRemark(bodyString(body, "handleRemark"));
        apply.setHandlerId(currentUser(session).getId());
        apply.setHandleTime(LocalDateTime.now());
        qfPositionApplyMapper.updateById(apply);
        if (apply.getStatus() != null && apply.getStatus() == 2) {
            ObjFrontUser user = new ObjFrontUser();
            user.setId(apply.getUserId());
            user.setCreaterVerified(1);
            frontUserMapper.updateById(user);
        }
        return ResultData.success("申请已处理");
    }

    private void assignUserRoles(Integer backUserId, List<Integer> roleIds) {
        qfUserRoleMapper.delete(new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getBackUserId, backUserId));
        if (roleIds == null) return;
        for (Integer roleId : roleIds) {
            QfUserRole link = new QfUserRole();
            link.setId(nextUserRoleId());
            link.setBackUserId(backUserId);
            link.setRoleId(roleId);
            qfUserRoleMapper.insert(link);
        }
    }

    private Integer nextUserRoleId() {
        return nextFromMax(qfUserRoleMapper.selectObjs(maxIdQuery()));
    }
}
