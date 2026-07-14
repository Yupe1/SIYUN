package com.yupe.siyun.util;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class TimeMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject,"createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject,"updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject,"collectTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject,"likeTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject,"sendTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject,"regieterDate", LocalDate.class, LocalDate.now());
        this.strictInsertFill(metaObject,"applyTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject,"updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
