package com.yupe.siyun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupe.siyun.entity.ObjFrontUser;
import jakarta.servlet.http.HttpSession;

public interface FrontUserService extends IService<ObjFrontUser> {
    ObjFrontUser login(ObjFrontUser user, HttpSession session);

    void register(ObjFrontUser user);

    void changePassword(ObjFrontUser user, ObjFrontUser u);
}
