package com.gapache.job.common.model;

import com.dyuproject.protostuff.Tag;
import lombok.Data;

/**
 * @author HuSen
 * @since 2021/2/4 10:03 上午
 */
@Data
public class ServerMessage {

    public static final int SUCCESS = 0;
    public static final int ERROR = 5000;

    public enum Type {
        //
        TASK(1),
        CLOSE(2);
        private final int type;

        Type(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static Type check(int type) {
            switch (type) {
                case 1: return TASK;
                case 2: return CLOSE;
                default: return null;
            }
        }
    }

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
        return this.code == SUCCESS;
    }
}
