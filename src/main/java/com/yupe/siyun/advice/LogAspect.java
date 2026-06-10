package com.yupe.siyun.advice;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

    // 针对 AuthController 里的登录和登出方法
//    @AfterReturning("execution(* com.yupe.vue3demo.controller.BookController.adminLogin(..))")
//    public void AdminloginLog(JoinPoint joinPoint) {
//        System.out.println("【日志记录】管理员执行了登录操作，时间：" + LocalDateTime.now());
//    }
//
//    @AfterReturning("execution(* com.yupe.bootdemo12.controller.AuthController.userLogin(..))")
//    public void UserloginLog(JoinPoint joinPoint) {
//        System.out.println("【日志记录】用户执行了登录操作，时间：" + LocalDateTime.now());
//    }
//
//    @Before("execution(* com.yupe.bootdemo12.controller.AuthController.logout(..))")
//    public void logoutLog(JoinPoint joinPoint) {
//        System.out.println("【日志记录】执行了登出操作，正在清理 Session...");
//    }
}
