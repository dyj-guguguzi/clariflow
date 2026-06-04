package com.clariflow.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClariFlow 工作项流转应用
 *
 * <p>工作项状态流转管理系统的 Spring Boot 入口。
 * 在 repository 包下扫描 MyBatis-Plus Mapper。</p>
 */
@SpringBootApplication
@MapperScan("com.clariflow.workflow.repository")
public class ClariFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClariFlowApplication.class, args);
    }
}
