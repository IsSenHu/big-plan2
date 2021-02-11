package com.gapache.blog.admin.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author HuSen
 * create on 2020/4/5 03:44
 */
@Data
public class BlogUpdateDTO implements Serializable {
    private static final long serialVersionUID = 5318099956106605558L;

    /**
     * ID
     */
    @NotBlank
    private String id;
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
     * 发布时间
     */
    @NotBlank
    private String publishTime;
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
