package com.yupe.siyun.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) //注解只能在方法/类上
@Retention(RetentionPolicy.RUNTIME)             //程序运行时仍能通过反射读到
public @interface RequiresPermission {
    String[] value();
}
