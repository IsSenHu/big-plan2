package com.gapache.security.aspect;

import com.gapache.security.annotation.AuthResource;
import com.gapache.security.cache.AuthResourceCache;
import com.gapache.commons.model.SecurityException;
import com.gapache.security.holder.AccessCardHolder;
import com.gapache.security.model.AccessCard;
import com.gapache.security.model.SecurityError;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author HuSen
 * @since 2020/8/10 9:08 上午
 */
@Slf4j
@Aspect
@Component
public class PreAuthResourceAspect {

    @Before("@annotation(authResource)")
    public void before(AuthResource authResource) {
        AccessCard accessCard = AccessCardHolder.getContext();
        if (accessCard == null) {
            log.warn("accessCard is null");
            throw new SecurityException(SecurityError.FORBIDDEN);
        }
        // 校验权限
        String fullScope = AuthResourceCache.checkFullScope(authResource);
        if (!accessCard.getAuthorities().contains(fullScope)) {
            throw new SecurityException(SecurityError.FORBIDDEN);
        }
    }
}
