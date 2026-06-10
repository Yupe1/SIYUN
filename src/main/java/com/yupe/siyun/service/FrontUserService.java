package com.yupe.siyun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupe.siyun.entity.ObjFrontUser;

public interface FrontUserService extends IService<ObjFrontUser> {
    ObjFrontUser login(ObjFrontUser user);

    void register(ObjFrontUser user);
}
