package com.gapache.tools.server.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 我的小日常
 *
 * @author HuSen
 * @since 2021/2/20 9:19 上午
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "tb_mine_little_daily")
public class MineLittleDailyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "little_daily_id", nullable = false)
    private Long littleDailyId;

    @Column(name = "frequency", nullable = false)
    private String frequency;

    @Column(name = "scene", nullable = false)
    private Integer scene;

    @Column(name = "point", nullable = false)
    private Integer point;

    @Column(name = "remind_time")
    private LocalDateTime remindTime;

    @Column(name = "join_time", nullable = false)
    private LocalDateTime joinTime;
}
