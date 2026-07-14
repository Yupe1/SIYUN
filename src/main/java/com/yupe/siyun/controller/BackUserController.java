package com.yupe.siyun.controller;

import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.QfPermission;
import com.yupe.siyun.entity.QfRole;
import com.yupe.siyun.interceptor.RequiresPermission;
import com.yupe.siyun.service.BackUserService;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/sys")
public class BackUserController {
    @Autowired
    private BackUserService backUserService;


    //登录
    @PostMapping("/login")
    public Object login(@RequestBody ObjBackUser user, HttpSession session) {
        ObjBackUser u = backUserService.login(user,session);
        List<QfRole> roles = backUserService.rolesOf(u.getId());
        List<QfPermission> permissions = backUserService.permissionsOf(u.getId());
        return ResultData.success(
                new String[]{"loginUser", "roles", "perms", "permissions"},
                new Object[]{
                        u,
                        roles,
                        permissions.stream().map(QfPermission::getPerms).collect(Collectors.toList()),
                        permissions
                },
                "登录成功"
        );
    }

    //注册
    @PostMapping("/register")
    @RequiresPermission("admin:staff:add")
    public Object register(@RequestBody ObjBackUser user, HttpServletRequest request) {
        user.setRegisterIp(request.getRemoteAddr());
        backUserService.register(user);
        user.setPassword(null);
        return ResultData.success("registerUser", user, "注册成功");
    }

    @GetMapping("/me")
    public Object me(HttpSession session) {
        ObjBackUser user = (ObjBackUser) session.getAttribute("backUser");
        return ResultData.success(
                new String[]{"loginUser", "roles", "perms"},
                new Object[]{
                        user,
                        session.getAttribute("backRoles"),
                        session.getAttribute("backPerms")
                },
                "当前后台用户"
        );
    }

    @GetMapping("/menus")
    public Object menus(HttpSession session) {
        ObjBackUser user = (ObjBackUser) session.getAttribute("backUser");
        List<QfPermission> permissions = backUserService.permissionsOf(user.getId());
        return ResultData.success("permissions", permissions, "菜单权限加载成功");
    }

    //登出
    @PostMapping("/logout")
    public Object logout(HttpSession session) {
        session.invalidate();
        return ResultData.success("logout", null, "登出成功");
    }
}
