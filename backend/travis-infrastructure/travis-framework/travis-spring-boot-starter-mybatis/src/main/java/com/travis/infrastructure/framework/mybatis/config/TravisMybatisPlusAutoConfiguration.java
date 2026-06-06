package com.travis.infrastructure.framework.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.travis.infrastructure.framework.mybatis.core.handler.DefaultMetaObjectHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 自动配置类
 *
 * <p>注册分页插件、乐观锁插件、防全表更新/删除插件，以及默认的字段自动填充处理器。
 *
 * @author travis
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.mapper.BaseMapper")
@MapperScan(basePackages = "${travis.info.base-package}", markerInterface = BaseMapper.class)
public class TravisMybatisPlusAutoConfiguration {

    /**
     * 注册 MyBatis-Plus 拦截器
     *
     * <p>包含以下插件（注意顺序）：
     *
     * <ol>
     *   <li>分页插件 {@link PaginationInnerInterceptor}
     *   <li>乐观锁插件 {@link OptimisticLockerInnerInterceptor}
     *   <li>防全表更新/删除插件 {@link BlockAttackInnerInterceptor}
     * </ol>
     *
     * @return MybatisPlusInterceptor 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        var interceptor = new MybatisPlusInterceptor();
        // 分页插件，默认使用 MySQL 方言
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 注册默认的字段自动填充处理器
     *
     * <p>可通过自定义 {@link MetaObjectHandler} Bean 覆盖此默认实现。
     *
     * @return MetaObjectHandler 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public MetaObjectHandler defaultMetaObjectHandler() {
        return new DefaultMetaObjectHandler();
    }
}
