package com.gapache.tools.server.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * 小日常库
 *
 * @author HuSen
 * @since 2021/2/20 9:13 上午
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "tb_little_daily")
public class LittleDailyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon", nullable = false)
    private String icon;
}
