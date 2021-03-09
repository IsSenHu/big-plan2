package com.gapache.job.common.model;

import com.gapache.vertx.web.zeus.MetadataHolder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HuSen
 * @since 2021/3/4 4:10 下午
 */
@Getter
@Setter
public class ZeusClientMessage implements MetadataHolder {

    private Map<String, String> metadata;

    private ClientMessage clientMessage;

    @Override
    public void setMetadata(Map<String, String> metadata) {
        if (MapUtils.isNotEmpty(this.metadata) && metadata != null) {
            metadata.forEach(this.metadata::put);
        } else {
            this.metadata = metadata;
        }
    }
}
