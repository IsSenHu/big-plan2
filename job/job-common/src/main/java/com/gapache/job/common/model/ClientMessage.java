package com.gapache.job.common.model;

import com.dyuproject.protostuff.Tag;
import lombok.Data;

/**
 * @author HuSen
 * @since 2021/2/4 9:52 上午
 */
@Data
public class ClientMessage {

    public enum Type {
        // 注册
        REGISTRY(1),
        // 结果
        RESULT(2);
        private final int type;

        Type(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static Type checkType(int type) {
            switch (type) {
                case 1: return REGISTRY;
                case 2: return RESULT;
                default: return null;
            }
        }
    }

    public static final int SUCCESS = 0;

    @Tag(1)
    private String messageId;
    @Tag(2)
    private int code;
    @Tag(3)
    private String error;
    @Tag(4)
    private byte[] data;
    @Tag(5)
    private int type;

    public boolean success() {
        return SUCCESS == this.code;
    }
}
