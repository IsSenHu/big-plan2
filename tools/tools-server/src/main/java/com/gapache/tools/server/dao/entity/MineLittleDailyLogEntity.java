package com.gapache.tools.server.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author HuSen
 * @since 2021/2/20 9:29 上午
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "tb_mine_little_daily_log")
public class MineLittleDailyLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "mine_little_daily_id", nullable = false)
    private Long mineLittleDailyId;
}
