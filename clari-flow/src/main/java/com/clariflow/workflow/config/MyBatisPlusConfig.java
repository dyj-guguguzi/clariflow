package com.clariflow.workflow.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus configuration.
 *
 * <p>Registers the pagination plugin and the optimistic locker interceptor.
 * Pagination supports H2 (MySQL-compatible mode).</p>
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * Configures the MyBatis-Plus interceptor chain with pagination
     * and optimistic locking support.
     *
     * @return configured MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // Pagination plugin (H2 compatible)
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        // Optimistic locker plugin (version field)
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
