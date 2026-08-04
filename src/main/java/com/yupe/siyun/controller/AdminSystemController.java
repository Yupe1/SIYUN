package com.yupe.siyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yupe.siyun.controller.dto.PermissionAssignPayload;
import com.yupe.siyun.controller.support.AdminControllerSupport;
import com.yupe.siyun.entity.QfPermission;
import com.yupe.siyun.entity.QfRole;
import com.yupe.siyun.entity.QfRolePermission;
import com.yupe.siyun.entity.QfUserRole;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.mapper.QfPermissionMapper;
import com.yupe.siyun.mapper.QfRoleMapper;
import com.yupe.siyun.mapper.QfRolePermissionMapper;
import com.yupe.siyun.mapper.QfUserRoleMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminSystemController extends AdminControllerSupport {

    @Autowired
    private QfRoleMapper qfRoleMapper;
    @Autowired
    private QfPermissionMapper qfPermissionMapper;
    @Autowired
    private QfRolePermissionMapper qfRolePermissionMapper;
    @Autowired
    private QfUserRoleMapper qfUserRoleMapper;

    @GetMapping("/roles")
    @RequiresPermission("admin:role:list")
    public Object roles() {
        return ResultData.success("roles", qfRoleMapper.selectList(new LambdaQueryWrapper<QfRole>().orderByAsc(QfRole::getSortNum)), "角色列表");
    }

    @PostMapping("/roles")
    @RequiresPermission("admin:role:add")
    public Object addRole(@RequestBody QfRole role, HttpSession session) {
        ensureAdmin(session);
        role.setCreateBy(currentUser(session).getId());
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getSortNum() == null) role.setSortNum(0);
        qfRoleMapper.insert(role);
        return ResultData.success("role", role, "角色已添加");
    }

    @PutMapping("/roles/{id}")
    @RequiresPermission("admin:role:update")
    public Object updateRole(@PathVariable Integer id, @RequestBody QfRole role, HttpSession session) {
        ensureAdmin(session);
        role.setId(id);
        qfRoleMapper.updateById(role);
        return ResultData.success("角色已更新");
    }

    @DeleteMapping("/roles/{id}")
    @RequiresPermission("admin:role:delete")
    @Transactional
    public Object deleteRole(@PathVariable Integer id, HttpSession session) {
        ensureAdmin(session);
        QfRole stored = requireRole(id);
        if ("ADMIN".equals(stored.getRoleKey())) {
            throw new MyException(ErrorType.PERMISSION_DENIED, "超级管理员角色不能删除");
        }
        qfRoleMapper.deleteById(id);
        qfRolePermissionMapper.delete(new LambdaQueryWrapper<QfRolePermission>().eq(QfRolePermission::getRoleId, id));
        qfUserRoleMapper.delete(new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getRoleId, id));
        return ResultData.success("角色已删除");
    }

    @GetMapping("/roles/{id}/permissions")
    @RequiresPermission("admin:role:permission")
    public Object rolePermissions(@PathVariable Integer id) {
        QfRole role = requireRole(id);
        if ("ADMIN".equals(role.getRoleKey())) {
            List<Integer> allPermissionIds = qfPermissionMapper.selectList(
                            new LambdaQueryWrapper<QfPermission>().eq(QfPermission::getStatus, 1)
                    ).stream()
                    .map(QfPermission::getId)
                    .collect(Collectors.toList());
            return ResultData.success("permissionIds", allPermissionIds, "超级管理员拥有全部权限");
        }
        List<QfRolePermission> links = qfRolePermissionMapper.selectList(
                new LambdaQueryWrapper<QfRolePermission>().eq(QfRolePermission::getRoleId, id)
        );
        return ResultData.success("permissionIds", links.stream().map(QfRolePermission::getPermissionId).collect(Collectors.toList()), "角色权限");
    }

    @GetMapping("/roles/self")
    public Object selfRoles(HttpSession session) {
        List<QfUserRole> links = qfUserRoleMapper.selectList(
                new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getBackUserId, currentUser(session).getId())
        );
        if (links.isEmpty()) {
            return ResultData.success("roles", Collections.emptyList(), "当前账号角色");
        }
        Set<Integer> roleIds = links.stream().map(QfUserRole::getRoleId).collect(Collectors.toSet());
        List<QfRole> roles = qfRoleMapper.selectList(
                new LambdaQueryWrapper<QfRole>()
                        .in(QfRole::getId, roleIds)
                        .eq(QfRole::getStatus, 1)
                        .orderByAsc(QfRole::getSortNum)
                        .orderByAsc(QfRole::getId)
        );
        return ResultData.success("roles", roles, "当前账号角色");
    }

    @GetMapping("/roles/self/{id}/permissions")
    public Object selfRolePermissions(@PathVariable Integer id, HttpSession session) {
        ensureOwnRole(id, session);
        List<Integer> permissionIds = qfRolePermissionMapper.selectList(
                        new LambdaQueryWrapper<QfRolePermission>().eq(QfRolePermission::getRoleId, id)
                ).stream()
                .map(QfRolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());
        List<QfPermission> permissions = permissionIds.isEmpty()
                ? Collections.emptyList()
                : qfPermissionMapper.selectList(
                        new LambdaQueryWrapper<QfPermission>()
                                .in(QfPermission::getId, permissionIds)
                                .eq(QfPermission::getStatus, 1)
                                .orderByAsc(QfPermission::getParentId)
                                .orderByAsc(QfPermission::getId)
                );
        return ResultData.success(
                new String[]{"permissionIds", "permissions"},
                new Object[]{permissionIds, permissions},
                "当前角色权限"
        );
    }

    @PutMapping("/roles/{id}/permissions")
    @RequiresPermission("admin:role:permission")
    @Transactional
    public Object assignRolePermissions(@PathVariable Integer id, @RequestBody PermissionAssignPayload payload,
                                        HttpSession session) {
        ensureAdmin(session);
        QfRole role = requireRole(id);
        if ("ADMIN".equals(role.getRoleKey())) {
            return ResultData.success("超级管理员固定拥有全部权限，无需修改");
        }
        qfRolePermissionMapper.delete(new LambdaQueryWrapper<QfRolePermission>().eq(QfRolePermission::getRoleId, id));
        if (payload.getPermissionIds() != null) {
            for (Integer permissionId : payload.getPermissionIds()) {
                QfRolePermission link = new QfRolePermission();
                link.setId(nextRolePermissionId());
                link.setRoleId(id);
                link.setPermissionId(permissionId);
                qfRolePermissionMapper.insert(link);
            }
        }
        return ResultData.success("角色权限已保存");
    }

    @GetMapping("/permissions")
    @RequiresPermission("admin:permission:list")
    public Object permissions() {
        List<QfPermission> permissions = qfPermissionMapper.selectList(
                new LambdaQueryWrapper<QfPermission>()
                        .orderByAsc(QfPermission::getParentId)
                        .orderByAsc(QfPermission::getId)
        );
        return ResultData.success("permissions", permissions, "权限列表");
    }

    @PostMapping("/permissions")
    @RequiresPermission("admin:permission:add")
    public Object addPermission(@RequestBody QfPermission permission, HttpSession session) {
        if (permission.getStatus() == null) permission.setStatus(1);
        permission.setUpdateBy(currentUser(session).getId());
        qfPermissionMapper.insert(permission);
        return ResultData.success("permission", permission, "权限已添加");
    }

    @PutMapping("/permissions/{id}")
    @RequiresPermission("admin:permission:update")
    public Object updatePermission(@PathVariable Integer id, @RequestBody QfPermission permission, HttpSession session) {
        permission.setId(id);
        permission.setUpdateBy(currentUser(session).getId());
        qfPermissionMapper.updateById(permission);
        return ResultData.success("权限已更新");
    }

    @DeleteMapping("/permissions/{id}")
    @RequiresPermission("admin:permission:delete")
    public Object deletePermission(@PathVariable Integer id) {
        qfPermissionMapper.deleteById(id);
        qfRolePermissionMapper.delete(new LambdaQueryWrapper<QfRolePermission>().eq(QfRolePermission::getPermissionId, id));
        return ResultData.success("权限已删除");
    }

    private Integer nextRolePermissionId() {
        return nextFromMax(qfRolePermissionMapper.selectObjs(maxIdQuery()));
    }

    private QfRole requireRole(Integer id) {
        QfRole role = qfRoleMapper.selectById(id);
        if (role == null) {
            throw new MyException(ErrorType.WRONG_INFO, "角色不存在");
        }
        return role;
    }

    private void ensureOwnRole(Integer roleId, HttpSession session) {
        long count = qfUserRoleMapper.selectCount(
                new LambdaQueryWrapper<QfUserRole>()
                        .eq(QfUserRole::getBackUserId, currentUser(session).getId())
                        .eq(QfUserRole::getRoleId, roleId)
        );
        if (count == 0) {
            throw new MyException(ErrorType.PERMISSION_DENIED, "只能查看自己的角色权限");
        }
    }

    private void ensureAdmin(HttpSession session) {
        if (!currentRoles(session).contains("ADMIN")) {
            throw new MyException(ErrorType.PERMISSION_DENIED, "只有超级管理员可以管理角色和授权");
        }
    }
}
