package com.guanshi.contentcenter.auth;

import com.guanshi.contentcenter.util.JwtOperator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthAspect {
    private final JwtOperator jwtOperator;

    @Around("@annotation(com.guanshi.contentcenter.auth.CheckLogin)")
    public Object checkLogin(ProceedingJoinPoint point) throws Throwable {
            checkToken();
            return point.proceed();
    }

    private void checkToken() {
        try {
        //        从header里面获取token，静态方法拿request
            HttpServletRequest request = getHttpServletRequest();

            String token = request.getHeader("X-Token");
    //        校验token是否合法，是否过期，不合法抛异常，合法放行
            Boolean isValid = jwtOperator.validateToken(token);
            if (!isValid) {
                throw new SecurityException("Token不合法！");
            }
    //        如果校验成功，那么将用户的信息设置到request的attributes里
            Claims claims = jwtOperator.getClaimsFromToken(token);
            request.setAttribute("id", claims.get("id"));
            request.setAttribute("wxNickName", claims.get("wxNickName"));
            request.setAttribute("role", claims.get("role"));
        } catch (Throwable e) {
            throw new SecurityException("Token不合法！");
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = attributes.getRequest();
        return request;
    }

    @Around("@annotation(com.guanshi.contentcenter.auth.CheckAuthorization)")
    public Object checkAuthorization(ProceedingJoinPoint point) throws Throwable {
        try {
            //        校验token是否合法，是否过期，不合法抛异常，合法放行
            checkToken();
            //        验证用户角色是否匹配
            HttpServletRequest request = getHttpServletRequest();
            String role = (String) request.getAttribute("role");

            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            CheckAuthorization annotation = method.getAnnotation(CheckAuthorization.class);

            String value = annotation.value();
            if (!Objects.equals(value, role)) {
                throw new SecurityException("用户无权限问题！");
            }
        } catch (Throwable e) {
            throw new SecurityException("用户无权限问题！", e);
        }
        return point.proceed();
    }
}
