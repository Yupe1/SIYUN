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
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        role.setCreateBy(currentUser(session).getId());
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getSortNum() == null) role.setSortNum(0);
        qfRoleMapper.insert(role);
        return ResultData.success("role", role, "角色已添加");
    }

    @PutMapping("/roles/{id}")
    @RequiresPermission("admin:role:update")
    public Object updateRole(@PathVariable Integer id, @RequestBody QfRole role) {
        role.setId(id);
        qfRoleMapper.updateById(role);
        return ResultData.success("角色已更新");
    }

    @DeleteMapping("/roles/{id}")
    @RequiresPermission("admin:role:delete")
    public Object deleteRole(@PathVariable Integer id) {
        qfRoleMapper.deleteById(id);
        qfRolePermissionMapper.delete(new LambdaQueryWrapper<QfRolePermission>().eq(QfRolePermission::getRoleId, id));
        qfUserRoleMapper.delete(new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getRoleId, id));
        return ResultData.success("角色已删除");
    }

    @GetMapping("/roles/{id}/permissions")
    @RequiresPermission("admin:role:permission")
    public Object rolePermissions(@PathVariable Integer id) {
        List<QfRolePermission> links = qfRolePermissionMapper.selectList(
                new LambdaQueryWrapper<QfRolePermission>().eq(QfRolePermission::getRoleId, id)
        );
        return ResultData.success("permissionIds", links.stream().map(QfRolePermission::getPermissionId).collect(Collectors.toList()), "角色权限");
    }

    @PutMapping("/roles/{id}/permissions")
    @RequiresPermission("admin:role:permission")
    public Object assignRolePermissions(@PathVariable Integer id, @RequestBody PermissionAssignPayload payload) {
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
}
