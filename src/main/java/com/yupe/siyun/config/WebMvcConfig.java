package com.yupe.siyun.config;



import com.yupe.siyun.interceptor.BackAuthInterceptor;
import com.yupe.siyun.interceptor.BackRoleInterceptor;
import com.yupe.siyun.interceptor.FrontUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 1. 前台 App 用户登录拦截
    @Autowired
    private FrontUserInterceptor frontUserInterceptor;

    // 2. 后台所有员工（管理员/教师/运营/人事）的统一登录拦截
    @Autowired
    private BackAuthInterceptor backAuthInterceptor;

    // 3. 后台精细化角色/权限控制拦截器（可选：也可以用 AOP/Shiro/Spring Security 代替）
    @Autowired
    private BackRoleInterceptor backRoleInterceptor;

    @Value("${upload.root-path}")
    private String basePath;

    /**
     * 静态资源映射：让前端能访问磁盘上的本地图片、课程封面或视频
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploaded/**")
                .addResourceLocations("file:" + basePath + "uploaded/");
    }

    /**
     * 跨域配置 (保留你之前的极客局域网/本地兼容配置)
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOriginPattern("http://localhost:5173");
        config.addAllowedOriginPattern("http://127.0.0.1:5173");
        config.addAllowedOriginPattern("http://192.168.*.*:5173");
        config.addAllowedOriginPattern("http://10.*.*.*:5173");

        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 核心拦截链配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // ==================== ① 前台 App 端拦截器 ====================
        registry.addInterceptor(frontUserInterceptor)
                .addPathPatterns("/api/app/**") // 拦截所有 App 端请求
                // 放行：登录、注册、找回密码
                .excludePathPatterns("/api/app/user/login/**")
                .excludePathPatterns("/api/app/user/register/**")
                .excludePathPatterns("/api/app/user/reset-pwd/**")
                // 放行：首页大盘、课程检索、轮播图、微圈文章等“只读”公开内容
                .excludePathPatterns("/api/app/course/list/**")
                .excludePathPatterns("/api/app/course/detail/**")
                .excludePathPatterns("/api/app/category/**")
                .excludePathPatterns("/api/app/carousel/list/**")
                .excludePathPatterns("/api/app/moments/list/**")
                .excludePathPatterns("/api/app/moments/detail/**");

        // ==================== ② 后台管理端：统一登录拦截器 ====================
        registry.addInterceptor(backAuthInterceptor)
                .addPathPatterns("/api/admin/**") // 拦截所有后台管理接口
                // 放行：后台员工登录
                .excludePathPatterns("/api/admin/sys/login/**");

        // ==================== ③ 后台管理端：角色动态鉴权拦截器 ====================
        // 这一步紧跟在登录拦截后面，用来根据用户登录的 role 字段判断是否能访问具体的菜单路径
        registry.addInterceptor(backRoleInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/sys/login/**")
                // 放行：公共的个人信息查看与首页统计通知
                .excludePathPatterns("/api/admin/home/index-data/**")
                .excludePathPatterns("/api/admin/profile/**");
    }
}
