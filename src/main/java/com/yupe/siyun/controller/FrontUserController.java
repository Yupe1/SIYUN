package com.yupe.siyun.controller;

import com.yupe.siyun.entity.ObjFrontUser;
import com.yupe.siyun.service.FrontUserService;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class FrontUserController {

    @Autowired
    private FrontUserService frontUserService;

    //登录
    @PostMapping("/login")
    public Object login(@RequestBody ObjFrontUser user, HttpSession session) {
        ObjFrontUser u = frontUserService.login(user,session);
        return ResultData.success("loginUser",u,"登录成功");
    }

    //注册
    @PostMapping("/register")
    public Object register(@RequestBody ObjFrontUser user) {
        frontUserService.register(user);
        return ResultData.success("registerUser", user, "注册成功");
    }

    //登出
    @PostMapping("/logout")
    public Object logout(HttpSession session) {
        session.invalidate();
        return ResultData.success("logout", null, "登出成功");
    }

    //改密码
    @PostMapping("/changePassword")
    public Object changePassword(@RequestBody ObjFrontUser user, HttpSession session) {
        ObjFrontUser u = (ObjFrontUser) session.getAttribute("user");
        if (u == null) {
            return ResultData.error(ErrorType.UNAUTHORIZED, "请先登录");
        }
        frontUserService.changePassword(user, u);
        return ResultData.success("改密码成功");
    }


}
