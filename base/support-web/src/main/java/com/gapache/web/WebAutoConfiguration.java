package com.gapache.web;

import com.gapache.commons.model.*;
import com.gapache.commons.utils.ContextUtils;
import com.gapache.web.utils.ValidatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HuSen
 * create on 2020/1/14 17:54
 */
@Configuration
public class WebAutoConfiguration {

    @Slf4j
    @RestControllerAdvice
    @ConditionalOnBean(annotation = EnableCommonAdvice.class)
    public static class Advice {

        @ExceptionHandler(Exception.class)
        public JsonResult<String> exceptionHandler(HttpServletRequest request, Exception e) {
            if (e instanceof BusinessException) {
                BusinessException iE = (BusinessException) e;
                log.error("{}.{}业务错误:", request.getMethod(), request.getRequestURI(), e);
                return JsonResult.of(iE.getError());
            } else if (e instanceof ParamException) {
                ParamException pE = (ParamException) e;
                StringBuilder error = new StringBuilder();
                pE.getErrors().forEach((x, y) -> error.append(x).append(":").append(y.toString()).append(";"));
                return JsonResult.of(CommonError.PARAM_ERROR.getCode(), error.substring(0, error.length() - 1));
            } else {
                log.error("{}.{}发生未知异常:", request.getMethod(), request.getRequestURI(), e);
                return JsonResult.of(SystemError.SERVER_EXCEPTION);
            }
        }
    }

    @Aspect
    @Slf4j
    @Configuration
    public static class AutoValidation {

        @Pointcut("@within(Validating)")
        public void pointCut() {}

        @Before("pointCut()")
        public void validating(JoinPoint pjp) {
            Map<String, StringBuilder> result = new HashMap<>(16);
            Object[] args = pjp.getArgs();
            Signature signature = pjp.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            Parameter[] parameters = method.getParameters();
            int length = parameters.length;
            for (int i = 0; i < length; i++) {
                Parameter parameter = parameters[i];
                Object arg = args[i];
                if (parameter.isAnnotationPresent(Check.class)) {
                    ValidatorUtil.validate(arg)
                            .then(errors -> {
                                if (errors != null) {
                                    errors.forEach((f, e) -> log.error("参数:{}, 错误:{}", f, e));
                                }
                            })
                            .then(errors -> {
                                if (errors != null) {
                                    result.putAll(errors);
                                }
                            })
                            .end();
                }
            }
            if (!result.isEmpty()) {
                throw new ParamException(result);
            }
        }
    }

//    @Bean
//    public CorsFilter corsFilter() {
//        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
//        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", buildConfig());
//        return new CorsFilter(urlBasedCorsConfigurationSource);
//    }
//
//    private CorsConfiguration buildConfig() {
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        // 允许任何域名
//        corsConfiguration.addAllowedOrigin("*");
//        // 允许任何头
//        corsConfiguration.addAllowedHeader("*");
//        // 允许任何方法
//        corsConfiguration.addAllowedMethod("*");
//        return corsConfiguration;
//    }

    @Bean
    public ContextUtils contextUtils() {
        return new ContextUtils();
    }
}
