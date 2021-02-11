package com.gapache.cloud.money.management.server.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author HuSen
 * @since 2021/1/14 10:07 上午
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "tb_fund_net_worth")
public class FundNetWorthEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "net_worth")
    private Double netWorth;

    @Column(name = "add_up_net_worth")
    private Double addUpNetWorth;

    @Column(name = "day")
    private LocalDate day;
}
