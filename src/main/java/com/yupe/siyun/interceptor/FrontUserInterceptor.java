package com.yupe.siyun.interceptor;

import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class FrontUserInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行跨域预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 校验前台会员是否登录
        HttpSession session = request.getSession();
        if (session.getAttribute("frontUser") == null) {
            throw new MyException(ErrorType.NOT_LOGIN, "未登录客户端，无权操作");
        }
        return true;
    }
}
