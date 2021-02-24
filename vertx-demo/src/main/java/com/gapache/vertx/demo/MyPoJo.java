package com.gapache.vertx.demo;

import java.io.Serializable;

/**
 * @author HuSen
 * @since 2021/2/24 7:22 下午
 */
public class MyPoJo implements Serializable {
    private static final long serialVersionUID = -5934922985134875640L;

    private String name;

    public MyPoJo() {
        this.name = "world";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
