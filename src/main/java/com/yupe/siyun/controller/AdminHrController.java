package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupe.siyun.controller.dto.RoleAssignPayload;
import com.yupe.siyun.controller.dto.StaffPayload;
import com.yupe.siyun.controller.dto.StaffUserVO;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.JsDept;
import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.ObjFrontUser;
import com.yupe.siyun.entity.QfPositionApply;
import com.yupe.siyun.entity.QfRole;
import com.yupe.siyun.entity.QfUserLockLog;
import com.yupe.siyun.entity.QfUserRole;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.BackUserMapper;
import com.yupe.siyun.mapper.FrontUserMapper;
import com.yupe.siyun.mapper.JsDeptMapper;
import com.yupe.siyun.mapper.QfPositionApplyMapper;
import com.yupe.siyun.mapper.QfRoleMapper;
import com.yupe.siyun.mapper.QfUserLockLogMapper;
import com.yupe.siyun.mapper.QfUserRoleMapper;
import com.yupe.siyun.service.BackUserService;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminHrController extends AdminControllerSupport {

    @Autowired
    private BackUserService backUserService;
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
    @Autowired
    private QfRoleMapper qfRoleMapper;

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
                             @RequestParam(required = false) Integer status,
                             @RequestParam(required = false) Integer roleId,
                             HttpSession session) {
        LambdaQueryWrapper<ObjBackUser> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) wrapper.and(w -> w.like(ObjBackUser::getName, keyword).or().like(ObjBackUser::getTel, keyword));
        if (deptId != null) wrapper.eq(ObjBackUser::getDeptId, deptId);
        if (status != null) wrapper.eq(ObjBackUser::getStatus, status);

        if (roleId != null) {
            List<Integer> roleUserIds = qfUserRoleMapper.selectList(
                            new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getRoleId, roleId)
                    ).stream()
                    .map(QfUserRole::getBackUserId)
                    .distinct()
                    .collect(Collectors.toList());
            if (roleUserIds.isEmpty()) {
                return ResultData.success("page", new Page<StaffUserVO>(page, size, 0), "后台人员列表");
            }
            wrapper.in(ObjBackUser::getId, roleUserIds);
        }

        if (!isAdmin(session)) {
            List<Integer> adminUserIds = userIdsWithRoleKey("ADMIN");
            if (!adminUserIds.isEmpty()) wrapper.notIn(ObjBackUser::getId, adminUserIds);
        }
        wrapper.orderByAsc(ObjBackUser::getId);
        Page<ObjBackUser> data = new Page<>(page, size);
        backUserMapper.selectPage(data, wrapper);
        return ResultData.success("page", buildStaffPage(data), "后台人员列表");
    }

    @GetMapping("/staff-options")
    @RequiresPermission("admin:staff:list")
    public Object staffOptions(HttpSession session) {
        List<JsDept> depts = jsDeptMapper.selectList(
                new LambdaQueryWrapper<JsDept>()
                        .orderByAsc(JsDept::getParentId)
                        .orderByAsc(JsDept::getId)
        );
        LambdaQueryWrapper<QfRole> roleWrapper = new LambdaQueryWrapper<QfRole>()
                .eq(QfRole::getStatus, 1)
                .orderByAsc(QfRole::getSortNum)
                .orderByAsc(QfRole::getId);
        if (!isAdmin(session)) roleWrapper.ne(QfRole::getRoleKey, "ADMIN");
        List<QfRole> roles = qfRoleMapper.selectList(roleWrapper);
        return ResultData.success(new String[]{"depts", "roles"}, new Object[]{depts, roles}, "人员表单选项");
    }

    @PostMapping("/staff-users")
    @RequiresPermission("admin:staff:add")
    @Transactional
    public Object addStaff(@RequestBody StaffPayload payload, HttpSession session) {
        validateStaffPayload(payload);
        validateAssignableRoles(payload.getRoleIds(), session);
        ObjBackUser user = payload.toUser();
        user.setPassword(initialPassword(user.getTel()));
        if (user.getRegisterIp() == null) user.setRegisterIp("0.0.0.0");
        backUserService.register(user);
        assignUserRoles(user.getId(), payload.getRoleIds());
        user.setPassword(null);
        return ResultData.success("staff", user, "后台人员已添加，初始密码为手机号后6位");
    }

    @PutMapping("/staff-users/{id}")
    @RequiresPermission("admin:staff:update")
    @Transactional
    public Object updateStaff(@PathVariable Integer id, @RequestBody StaffPayload payload, HttpSession session) {
        ensureCanManageStaff(id, session);
        validateStaffPayload(payload);
        validateAssignableRoles(payload.getRoleIds(), session);
        long sameTelCount = backUserMapper.selectCount(
                new LambdaQueryWrapper<ObjBackUser>()
                        .eq(ObjBackUser::getTel, payload.getTel())
                        .ne(ObjBackUser::getId, id)
        );
        if (sameTelCount > 0) throw new MyException(ErrorType.WRONG_INFO, "该手机号已被其他人员使用");
        ObjBackUser user = payload.toUser();
        user.setId(id);
        user.setPassword(null);
        backUserMapper.updateById(user);
        if (payload.getRoleIds() != null) {
            assignUserRoles(id, payload.getRoleIds());
        }
        return ResultData.success("后台人员已更新");
    }

    @DeleteMapping("/staff-users/{id}")
    @RequiresPermission("admin:staff:delete")
    @Transactional
    public Object deleteStaff(@PathVariable Integer id, HttpSession session) {
        ensureCanManageStaff(id, session);
        backUserMapper.deleteById(id);
        qfUserRoleMapper.delete(new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getBackUserId, id));
        return ResultData.success("后台人员已删除");
    }

    @PutMapping("/staff-users/{id}/roles")
    @RequiresPermission("admin:staff:assign")
    @Transactional
    public Object assignStaffRoles(@PathVariable Integer id, @RequestBody RoleAssignPayload payload, HttpSession session) {
        ensureCanManageStaff(id, session);
        validateAssignableRoles(payload.getRoleIds(), session);
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
        Page<QfPositionApply> source = new Page<>(page, size);
        qfPositionApplyMapper.selectPage(source, wrapper);

        Set<Integer> userIds = source.getRecords().stream()
                .map(QfPositionApply::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Integer> handlerIds = source.getRecords().stream()
                .map(QfPositionApply::getHandlerId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Integer, ObjFrontUser> users = userIds.isEmpty()
                ? Collections.emptyMap()
                : frontUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(ObjFrontUser::getId, user -> user));
        Map<Integer, ObjBackUser> handlers = handlerIds.isEmpty()
                ? Collections.emptyMap()
                : backUserMapper.selectBatchIds(handlerIds).stream()
                .collect(Collectors.toMap(ObjBackUser::getId, user -> user));

        List<Map<String, Object>> records = new ArrayList<>();
        for (QfPositionApply apply : source.getRecords()) {
            ObjFrontUser user = users.get(apply.getUserId());
            ObjBackUser handler = handlers.get(apply.getHandlerId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", apply.getId());
            item.put("userNickname", displayFrontUserName(user, apply.getUserId()));
            item.put("stuTel", user == null ? apply.getTel() : user.getStuTel());
            item.put("targetPosition", apply.getTargetPosition());
            item.put("targetPositionName", "creator".equalsIgnoreCase(apply.getTargetPosition())
                    ? "创作者认证"
                    : apply.getTargetPosition());
            item.put("applyReason", apply.getApplyReason());
            item.put("chinaId", apply.getChinaId());
            item.put("tel", apply.getTel());
            item.put("email", apply.getEmail());
            item.put("fileUrl", apply.getFileUrl());
            item.put("status", apply.getStatus());
            item.put("handlerName", displayBackUserName(handler, apply.getHandlerId()));
            item.put("applyTime", apply.getApplyTime());
            item.put("handleTime", apply.getHandleTime());
            item.put("handleRemark", apply.getHandleRemark());
            records.add(item);
        }
        Page<Map<String, Object>> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(records);
        return ResultData.success("page", result, "创作者申请列表");
    }

    @PostMapping("/position-applies/{id}/audit")
    @RequiresPermission("admin:apply:audit")
    @Transactional
    public Object auditPositionApply(@PathVariable Integer id, @RequestBody Map<String, Object> body, HttpSession session) {
        QfPositionApply apply = qfPositionApplyMapper.selectById(id);
        if (apply == null) throw new MyException(ErrorType.WRONG_INFO, "申请不存在");
        Integer status = bodyInt(body, "status");
        if (status == null || (status != 2 && status != 3)) {
            throw new MyException(ErrorType.WRONG_INFO, "审核结果只能是通过或驳回");
        }
        apply.setStatus(status);
        apply.setHandleRemark(bodyString(body, "handleRemark"));
        apply.setHandlerId(currentUser(session).getId());
        apply.setHandleTime(LocalDateTime.now());
        qfPositionApplyMapper.updateById(apply);
        ObjFrontUser user = new ObjFrontUser();
        user.setId(apply.getUserId());
        user.setCreaterVerified(status == 2 ? 1 : 0);
        frontUserMapper.updateById(user);
        return ResultData.success("申请已处理");
    }

    private String displayFrontUserName(ObjFrontUser user, Integer userId) {
        if (user == null) return "用户#" + (userId == null ? "-" : userId);
        if (hasText(user.getNickName())) return user.getNickName();
        if (hasText(user.getStuTel())) return user.getStuTel();
        return "用户#" + user.getId();
    }

    private String displayBackUserName(ObjBackUser user, Integer userId) {
        if (user == null) return userId == null ? "暂未处理" : "人员#" + userId;
        if (hasText(user.getName())) return user.getName();
        if (hasText(user.getTel())) return user.getTel();
        return "人员#" + user.getId();
    }

    private void assignUserRoles(Integer backUserId, List<Integer> roleIds) {
        qfUserRoleMapper.delete(new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getBackUserId, backUserId));
        if (roleIds == null) return;
        for (Integer roleId : new LinkedHashSet<>(roleIds)) {
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

    private Page<StaffUserVO> buildStaffPage(Page<ObjBackUser> source) {
        Page<StaffUserVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        if (source.getRecords().isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        List<Integer> userIds = source.getRecords().stream().map(ObjBackUser::getId).collect(Collectors.toList());
        List<QfUserRole> links = qfUserRoleMapper.selectList(
                new LambdaQueryWrapper<QfUserRole>().in(QfUserRole::getBackUserId, userIds)
        );
        Set<Integer> roleIds = links.stream().map(QfUserRole::getRoleId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Integer, String> roleNames = roleIds.isEmpty()
                ? Collections.emptyMap()
                : qfRoleMapper.selectBatchIds(roleIds).stream().collect(Collectors.toMap(QfRole::getId, QfRole::getRoleName));

        Set<Integer> deptIds = source.getRecords().stream()
                .map(ObjBackUser::getDeptId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Integer, String> deptNames = deptIds.isEmpty()
                ? Collections.emptyMap()
                : jsDeptMapper.selectBatchIds(deptIds).stream().collect(Collectors.toMap(JsDept::getId, JsDept::getDeptName));

        Map<Integer, List<Integer>> userRoleIds = new HashMap<>();
        for (QfUserRole link : links) {
            userRoleIds.computeIfAbsent(link.getBackUserId(), key -> new ArrayList<>()).add(link.getRoleId());
        }

        List<StaffUserVO> records = new ArrayList<>();
        for (ObjBackUser user : source.getRecords()) {
            StaffUserVO view = new StaffUserVO();
            view.setId(user.getId());
            view.setName(user.getName());
            view.setGender(user.getGender());
            view.setAvataUrl(user.getAvataUrl());
            view.setEmail(user.getEmail());
            view.setTel(user.getTel());
            view.setChinaId(user.getChinaId());
            view.setBirth(user.getBirth());
            view.setDeptId(user.getDeptId());
            view.setDeptName(deptNames.get(user.getDeptId()));
            view.setRegieterDate(user.getRegieterDate());
            view.setRegisterIp(user.getRegisterIp());
            view.setStatus(user.getStatus());
            view.setLevel(user.getLevel());
            view.setSalary(user.getSalary());
            view.setRemark(user.getRemark());
            List<Integer> ids = userRoleIds.getOrDefault(user.getId(), Collections.emptyList());
            view.setRoleIds(ids);
            view.setRoleNames(ids.stream().map(roleNames::get).filter(name -> name != null).collect(Collectors.toList()));
            records.add(view);
        }
        result.setRecords(records);
        return result;
    }

    private void validateStaffPayload(StaffPayload payload) {
        if (payload == null || !hasText(payload.getName())) {
            throw new MyException(ErrorType.WRONG_INFO, "人员姓名不能为空");
        }
        if (!hasText(payload.getTel()) || payload.getTel().trim().length() < 6) {
            throw new MyException(ErrorType.WRONG_INFO, "请填写正确的手机号");
        }
        if (payload.getDeptId() == null) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择所属部门");
        }
        if (payload.getRoleIds() == null || payload.getRoleIds().isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请至少选择一个角色");
        }
    }

    private void validateAssignableRoles(List<Integer> roleIds, HttpSession session) {
        if (roleIds == null || roleIds.isEmpty()) return;
        Set<Integer> distinctIds = new LinkedHashSet<>(roleIds);
        List<QfRole> roles = qfRoleMapper.selectBatchIds(distinctIds);
        if (roles.size() != distinctIds.size()) {
            throw new MyException(ErrorType.WRONG_INFO, "选择的角色不存在");
        }
        if (!isAdmin(session) && roles.stream().anyMatch(role -> "ADMIN".equals(role.getRoleKey()))) {
            throw new MyException(ErrorType.PERMISSION_DENIED, "只有超级管理员可以分配管理员角色");
        }
    }

    private void ensureCanManageStaff(Integer staffId, HttpSession session) {
        if (isAdmin(session)) return;
        List<QfUserRole> links = qfUserRoleMapper.selectList(
                new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getBackUserId, staffId)
        );
        if (links.isEmpty()) return;
        Set<Integer> roleIds = links.stream().map(QfUserRole::getRoleId).collect(Collectors.toSet());
        boolean targetIsAdmin = qfRoleMapper.selectBatchIds(roleIds).stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleKey()));
        if (targetIsAdmin) {
            throw new MyException(ErrorType.PERMISSION_DENIED, "只有超级管理员可以管理管理员账号");
        }
    }

    private List<Integer> userIdsWithRoleKey(String roleKey) {
        QfRole role = qfRoleMapper.selectOne(
                new LambdaQueryWrapper<QfRole>().eq(QfRole::getRoleKey, roleKey).last("LIMIT 1")
        );
        if (role == null) return Collections.emptyList();
        return qfUserRoleMapper.selectList(
                        new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getRoleId, role.getId())
                ).stream()
                .map(QfUserRole::getBackUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isAdmin(HttpSession session) {
        return currentRoles(session).contains("ADMIN");
    }

    private String initialPassword(String tel) {
        String normalizedTel = tel.trim();
        return normalizedTel.substring(normalizedTel.length() - 6);
    }
}
