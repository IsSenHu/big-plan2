package com.gapache.job.server.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @author HuSen
 * @since 2021/2/3 5:20 下午
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "tb_job_group")
public class JobGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_name", unique = true, nullable = false)
    private String appName;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address_list")
    private String addressList;
}
