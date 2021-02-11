package com.gapache.cloud.money.management.server.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * 基金
 *
 * @author HuSen
 * @since 2021/1/13 1:45 下午
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "tb_fund")
public class FundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 基金代码
     */
    @Column(name = "code", unique = true)
    private String code;
    /**
     * 基金名称
     */
    @Column(name = "name", unique = true)
    private String name;

    /**
     * 持有
     */
    @Column(name = "hold")
    private Boolean hold;

    /**
     * 自选
     */
    @Column(name = "optional")
    private Boolean optional;
}
