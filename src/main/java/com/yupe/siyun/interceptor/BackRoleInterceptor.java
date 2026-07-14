package com.yupe.siyun.interceptor;

import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new MyException(ErrorType.NOT_LOGIN, "后台管理会话过期，请重新登录");
        }

        Object backUser = session.getAttribute("backUser");
        Set<String> backRoles = toStringSet(session.getAttribute("backRoles"));
        Object primaryRole = session.getAttribute("backRole");
        if (primaryRole != null) {
            backRoles.add(primaryRole.toString());
        }

        if (backUser == null || backRoles.isEmpty()) {
            throw new MyException(ErrorType.NOT_LOGIN, "后台管理会话过期，请重新登录");
        }

        // 超级管理员拥有全部后台接口权限
        if (backRoles.contains("ADMIN")) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresPermission requiresPermission = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (requiresPermission == null) {
            requiresPermission = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
        }

        if (requiresPermission != null) {
            Set<String> backPerms = toStringSet(session.getAttribute("backPerms"));
            boolean hasPermission = false;

            for (String perm : requiresPermission.value()) {
                if (backPerms.contains(perm) || hasWildcardPerm(backPerms, perm)) {
                    hasPermission = true;
                    break;
                }
            }

            if (!hasPermission) {
                throw new MyException(ErrorType.PERMISSION_DENIED, "对不起，您无权操作此功能");
            }
        }

        return true;
    }

    private Set<String> toStringSet(Object value) {
        Set<String> set = new HashSet<>();
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item != null) {
                    set.add(item.toString());
                }
            }
            return set;
        }
        if (value != null) {
            set.add(value.toString());
        }
        return set;
    }

    private boolean hasWildcardPerm(Set<String> ownedPerms, String requiredPerm) {
        for (String ownedPerm : ownedPerms) {
            if (!ownedPerm.endsWith("*")) {
                continue;
            }
            String prefix = ownedPerm.substring(0, ownedPerm.length() - 1);
            if (requiredPerm.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
