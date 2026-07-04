package com.yupe.siyun.controller;

import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.ObjFrontUser;
import com.yupe.siyun.service.BackUserService;
import com.yupe.siyun.service.FrontUserService;
import com.yupe.siyun.util.ResultData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staff")
public class BackUserController {
    @Autowired
    private BackUserService backUserService;


    //登录
    @PostMapping("/login")
    public Object login(@RequestBody ObjBackUser user, HttpSession session) {
        ObjBackUser u = backUserService.login(user,session);
        return ResultData.success("loginUser",u,"登录成功");
    }

    //注册
    @PostMapping("/register")
    public Object register(@RequestBody ObjBackUser user) {
        backUserService.register(user);
        return ResultData.success("registerUser", user, "注册成功");
    }

    //登出
    @PostMapping("/logout")
    public Object logout(HttpSession session) {
        session.invalidate();
        return ResultData.success("logout", null, "登出成功");
    }
}
