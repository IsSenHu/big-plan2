package com.gapache.cloud.money.management.server.dao.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author HuSen
 * @since 2020/11/11 3:12 下午
 */
@Data
@Entity
@Table(name = "tb_thrift")
public class ThriftEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "time")
    private LocalDateTime time;
    @Column(name = "money")
    private BigDecimal money;
}
