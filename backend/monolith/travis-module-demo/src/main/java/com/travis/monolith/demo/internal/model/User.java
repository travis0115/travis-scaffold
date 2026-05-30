package com.travis.monolith.demo.internal.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import com.travis.infrastructure.framework.mybatis.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * 对应数据库user表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class User extends BaseEntity {
    /**
     * 用户名
     */
    private String name;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 年龄
     */
    private Integer age;

    /**
     * 状态:1-正常,0-禁用
     */
    private Integer status;

    /**
     * 乐观锁版本号
     *
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
