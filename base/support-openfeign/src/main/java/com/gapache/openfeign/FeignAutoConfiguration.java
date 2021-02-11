package com.gapache.openfeign;

import com.gapache.commons.helper.AccessCardHeaderHolder;
import com.gapache.commons.model.AuthConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

/**
 * Feign传递身份凭证请求头
 *
 * @author HuSen
 * @since 2021/2/1 3:11 下午
 */
@Configuration
public class FeignAutoConfiguration implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String header = AccessCardHeaderHolder.getHeader();
        if (StringUtils.isNotBlank(header)) {
            requestTemplate.header(AuthConstants.ACCESS_CARD_HEADER, header);
        }
    }
}
