package com.yupe.siyun.controller;

import com.yupe.siyun.entity.ObjFrontUser;
import com.yupe.siyun.service.FrontUserService;
import com.yupe.siyun.util.ResultData;
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
    public Object login(@RequestBody ObjFrontUser user) {
        ObjFrontUser u = frontUserService.login(user);
        return ResultData.success("loginUser",u,"登录成功");
    }

    //注册
    @PostMapping("/register")
    public Object register(@RequestBody ObjFrontUser user) {
        frontUserService.register(user);
        return ResultData.success("registerUser", user, "注册成功");
    }

}
