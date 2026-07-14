package com.yupe.siyun.interceptor;

import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BackAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行所有浏览器的 OPTIONS 跨域预检请求，确保前后端分离跨域顺利
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 2. 验证后台 Session 中是否存在登录标志
        HttpSession session = request.getSession(false);

        // 对应我们在登录成功时存入 Session 的键名
        if (session == null || session.getAttribute("backUser") == null) {
            // 抛出你统一定义的未登录异常，由全局异常处理器拦截并返回给前端
            throw new MyException(ErrorType.NOT_LOGIN, "后台管理会话失效，请重新登录");
        }

        // 3. 验证通过，放行请求
        return true;
    }
}
