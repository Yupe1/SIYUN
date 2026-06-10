package com.yupe.siyun.interceptor;

import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BackRoleInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行跨域预检
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 2. 如果请求的不是 Controller 的方法（比如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 3. 校验后台登录状态
        HttpSession session = request.getSession();
        // 假设登录成功后，我们在 Session 存了后台用户对象 backUser 和他的角色字符串 backRole
        Object backUser = session.getAttribute("backUser");
        String backRole = (String) session.getAttribute("backRole");

        if (backUser == null || backRole == null) {
            throw new MyException(ErrorType.NOT_LOGIN, "后台管理会话过期，请重新登录");
        }

        // 🌟 超级管理员（ADMIN）拥有至高无上的权限，直接放行一切后台接口
        if ("ADMIN".equals(backRole)) {
            return true;
        }

        // 4. 获取目标方法或类上的 @RequiresRole 注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 先检查方法上有没有注解
        RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
        // 如果方法上没有，再检查类上有没有注解
        if (requiresRole == null) {
            requiresRole = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
        }

        // 5. 如果接口加了角色注解，开始比对角色权限
        if (requiresRole != null) {
            String[] allowedRoles = requiresRole.value();
            boolean hasPermission = false;

            for (String role : allowedRoles) {
                if (role.equalsIgnoreCase(backRole)) {
                    hasPermission = true;
                    break;
                }
            }

            // 如果当前员工的角色不在允许的列表里，直接抛出“权限不足”
            if (!hasPermission) {
                throw new MyException(ErrorType.PERMISSION_DENIED, "对不起，您无权操作此业务模块");
            }
        }

        return true;
    }
}
