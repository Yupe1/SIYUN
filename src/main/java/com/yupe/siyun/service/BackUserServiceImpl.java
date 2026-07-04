package com.yupe.siyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupe.siyun.entity.ObjBackUser;
import com.yupe.siyun.entity.ObjFrontUser;
import com.yupe.siyun.mapper.BackUserMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import com.yupe.siyun.util.SafeUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BackUserServiceImpl extends ServiceImpl<BackUserMapper, ObjBackUser> implements BackUserService {
    @Autowired
    private SafeUtil safeUtil;

    @Override
    public ObjBackUser login(ObjBackUser user, HttpSession session) {
        ObjBackUser u = this.getOne(
                new LambdaQueryWrapper<ObjBackUser>()
                        .eq(ObjBackUser::getTel, user.getTel())
        );
        if(u == null || u.getStatus() != 0 || !safeUtil.verifyPassword(user.getPassword(), u.getPassword())){
            throw new MyException(ErrorType.WRONG_PASSWORD_ERR,"账号不存在或密码错误");
        }
        session.setAttribute("backuser", u);
        u.setPassword(null);
        return u;
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
        //密码加密后存数据库
        user.setPassword(safeUtil.transPassword(user.getPassword()));
        this.save(user);
    }
}
