package com.gapache.blog.admin.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author HuSen
 * create on 2020/4/5 03:44
 */
@Data
public class BlogCreateDTO implements Serializable {
    private static final long serialVersionUID = 5318099956106605558L;

    /**
     * 标题
     */
    @NotBlank
    private String title;
    /**
     * 介绍
     */
    @NotBlank
    private String introduction;
    /**
     * 分类
     */
    @NotBlank
    private String category;
    /**
     * 标签
     */
    @NotBlank
    private String tags;
}
