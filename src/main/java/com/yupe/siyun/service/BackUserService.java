package com.yupe.siyun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.ObjFrontUser;
import com.yupe.siyun.mapper.BackUserMapper;
import jakarta.servlet.http.HttpSession;

public interface BackUserService extends IService<ObjBackUser> {
    ObjBackUser login(ObjBackUser user, HttpSession session);

    void register(ObjBackUser user);
}
