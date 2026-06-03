package com.clariflow.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClariFlow WorkItem Flow Application
 *
 * <p>Spring Boot entry point for the work item state flow management system.
 * Scans MyBatis-Plus mappers under the repository package.</p>
 */
@SpringBootApplication
@MapperScan("com.clariflow.workflow.repository")
public class ClariFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClariFlowApplication.class, args);
    }
}
