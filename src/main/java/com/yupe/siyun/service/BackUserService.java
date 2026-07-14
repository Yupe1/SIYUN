package com.yupe.siyun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.ObjBackUserPermission;
import com.yupe.siyun.entity.QfPermission;
import com.yupe.siyun.entity.QfRole;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface BackUserService extends IService<ObjBackUser> {
    ObjBackUser login(ObjBackUser user, HttpSession session);

    void register(ObjBackUser user);

    List<QfRole> rolesOf(Integer backUserId);

    List<QfPermission> permissionsOf(Integer backUserId);

    List<ObjBackUserPermission> permissionRowsOf(Integer backUserId);
}
