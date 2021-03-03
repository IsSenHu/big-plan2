package com.gapache.blog.server.dao.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author HuSen
 * @since 2020/8/28 1:26 下午
 */
@Data
@Entity
@Table(name = "tb_tourist")
public class TouristEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "nick", unique = true)
    private String nick;
    @Column(name = "password", nullable = false, length = 1024)
    private String password;
}
