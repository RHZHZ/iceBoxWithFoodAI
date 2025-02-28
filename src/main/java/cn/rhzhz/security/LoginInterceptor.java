package cn.rhzhz.security;

import cn.rhzhz.utils.JwtUtil;
import cn.rhzhz.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //令牌验证
        String token = request.getHeader("Authorization");
        //首先验证token
        try {
            Map<String,Object> claims = JwtUtil.parseToken(token);//有值可以转换则说明验证通过，否则出错
            //把业务数据存储到ThreadLocal
            ThreadLocalUtil.set(claims);
            return true;
        } catch (Exception e) {
            //http响应状态码401
            response.setStatus(401);
            //不放行
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }
}
