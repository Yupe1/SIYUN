package com.yupe.siyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.ObjBackUserPermission;
import com.yupe.siyun.entity.QfPermission;
import com.yupe.siyun.entity.QfRole;
import com.yupe.siyun.entity.QfUserRole;
import com.yupe.siyun.mapper.*;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.SafeUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BackUserServiceImpl extends ServiceImpl<BackUserMapper, ObjBackUser> implements BackUserService {
    @Autowired
    private SafeUtil safeUtil;
    @Autowired
    private QfUserRoleMapper qfUserRoleMapper;
    @Autowired
    private QfRoleMapper qfRoleMapper;
    @Autowired
    private QfPermissionMapper qfPermissionMapper;
    @Autowired
    private ObjBackUserPermissionMapper objBackUserPermissionMapper;

    @Override
    public ObjBackUser login(ObjBackUser user, HttpSession session) {
        ObjBackUser u = this.getOne(
                new LambdaQueryWrapper<ObjBackUser>()
                        .eq(ObjBackUser::getTel, user.getTel())
        );
        if(u == null || u.getStatus() != 0 || !passwordMatches(user.getPassword(), u.getPassword())){
            throw new MyException(ErrorType.WRONG_PASSWORD_ERR,"账号不存在或密码错误");
        }
        List<QfRole> roles = rolesOf(u.getId());
        if (roles.isEmpty()) {
            throw new MyException(ErrorType.PERMISSION_DENIED, "该后台账号尚未分配角色");
        }
        List<QfPermission> permissions = permissionsOf(u.getId());
        List<String> roleKeys = roles.stream().map(QfRole::getRoleKey).collect(Collectors.toList());
        List<String> perms = permissions.stream().map(QfPermission::getPerms).distinct().collect(Collectors.toList());

        ObjBackUser loginUser = copyWithoutPassword(u);
        session.setAttribute("backUser", loginUser);
        session.setAttribute("backRole", roleKeys.contains("ADMIN") ? "ADMIN" : roleKeys.get(0));
        session.setAttribute("backRoles", roleKeys);
        session.setAttribute("backPerms", perms);
        return loginUser;
    }

    @Override
    public void register(ObjBackUser user) {
        ObjBackUser u = this.getOne(
                new LambdaQueryWrapper<ObjBackUser>()
                        .eq(ObjBackUser::getTel, user.getTel())
        );
        if(u != null){
            throw new MyException(ErrorType.WRONG_INFO,"该手机号已注册");
        }
        if (user.getId() == null) {
            user.setId(nextBackUserId());
        }
        fillBackUserDefaults(user);
        //密码加密后存数据库
        if (user.getPassword() == null || !user.getPassword().contains("$")) {
            user.setPassword(safeUtil.transPassword(user.getPassword()));
        }
        this.save(user);
    }

    @Override
    public List<QfRole> rolesOf(Integer backUserId) {
        List<QfUserRole> userRoles = qfUserRoleMapper.selectList(
                new LambdaQueryWrapper<QfUserRole>().eq(QfUserRole::getBackUserId, backUserId)
        );
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> roleIds = userRoles.stream().map(QfUserRole::getRoleId).collect(Collectors.toList());
        return qfRoleMapper.selectList(
                new LambdaQueryWrapper<QfRole>()
                        .in(QfRole::getId, roleIds)
                        .eq(QfRole::getStatus, 1)
        );
    }

    @Override
    public List<QfPermission> permissionsOf(Integer backUserId) {
        List<QfRole> roles = rolesOf(backUserId);
        boolean admin = roles.stream().anyMatch(role -> Objects.equals("ADMIN", role.getRoleKey()));
        if (admin) {
            return qfPermissionMapper.selectList(
                    new LambdaQueryWrapper<QfPermission>()
                            .eq(QfPermission::getStatus, 1)
                            .orderByAsc(QfPermission::getParentId)
                            .orderByAsc(QfPermission::getId)
            );
        }
        List<String> perms = permissionRowsOf(backUserId).stream()
                .map(ObjBackUserPermission::getPerms)
                .distinct()
                .collect(Collectors.toList());
        if (perms.isEmpty()) {
            return Collections.emptyList();
        }
        return qfPermissionMapper.selectList(
                new LambdaQueryWrapper<QfPermission>()
                        .in(QfPermission::getPerms, perms)
                        .eq(QfPermission::getStatus, 1)
                        .orderByAsc(QfPermission::getParentId)
                        .orderByAsc(QfPermission::getId)
        );
    }

    @Override
    public List<ObjBackUserPermission> permissionRowsOf(Integer backUserId) {
        return objBackUserPermissionMapper.selectList(
                new LambdaQueryWrapper<ObjBackUserPermission>()
                        .eq(ObjBackUserPermission::getUserId, backUserId)
        );
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        return safeUtil.verifyPassword(rawPassword, storedPassword)
                || (rawPassword != null && rawPassword.equals(storedPassword));
    }

    private ObjBackUser copyWithoutPassword(ObjBackUser source) {
        ObjBackUser user = new ObjBackUser();
        user.setId(source.getId());
        user.setName(source.getName());
        user.setGender(source.getGender());
        user.setAvataUrl(source.getAvataUrl());
        user.setEmail(source.getEmail());
        user.setTel(source.getTel());
        user.setChinaId(source.getChinaId());
        user.setBirth(source.getBirth());
        user.setDeptId(source.getDeptId());
        user.setRegieterDate(source.getRegieterDate());
        user.setRegisterIp(source.getRegisterIp());
        user.setStatus(source.getStatus());
        user.setLevel(source.getLevel());
        user.setSalary(source.getSalary());
        user.setRemark(source.getRemark());
        return user;
    }

    private void fillBackUserDefaults(ObjBackUser user) {
        if (user.getName() == null) user.setName(user.getTel());
        if (user.getGender() == null) user.setGender(0);
        if (user.getAvataUrl() == null) user.setAvataUrl("/uploaded/avatars/head.jpg");
        if (user.getEmail() == null) user.setEmail("");
        if (user.getChinaId() == null) user.setChinaId("000000000000000000");
        if (user.getBirth() == null) user.setBirth(LocalDate.now());
        if (user.getDeptId() == null) user.setDeptId(1);
        if (user.getRegieterDate() == null) user.setRegieterDate(LocalDate.now());
        if (user.getRegisterIp() == null) user.setRegisterIp("0.0.0.0");
        if (user.getStatus() == null) user.setStatus(0);
        if (user.getLevel() == null) user.setLevel(1);
    }

    private Integer nextBackUserId() {
        List<Object> max = this.getBaseMapper().selectObjs(
                new QueryWrapper<ObjBackUser>().select("COALESCE(MAX(id),0)")
        );
        if (max.isEmpty() || max.get(0) == null) {
            return 1;
        }
        return Integer.parseInt(max.get(0).toString()) + 1;
    }
}
