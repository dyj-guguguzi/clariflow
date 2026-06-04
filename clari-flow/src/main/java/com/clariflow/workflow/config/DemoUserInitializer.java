package com.clariflow.workflow.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clariflow.workflow.model.entity.User;
import com.clariflow.workflow.repository.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 首次启动时创建演示用户（admin / pm）。
 */
@Configuration
public class DemoUserInitializer {

    private static final Logger log = LoggerFactory.getLogger(DemoUserInitializer.class);

    @Bean
    public CommandLineRunner initUsers(UserMapper userMapper) {
        return args -> {
            if (!exists(userMapper, "admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("123456"));
                admin.setEmail("admin@clariflow.com");
                admin.setRole("ADMIN");
                userMapper.insert(admin);
                log.info("Demo user created: admin / 123456");
            }
            if (!exists(userMapper, "pm")) {
                User pm = new User();
                pm.setUsername("pm");
                pm.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("123456"));
                pm.setEmail("pm@clariflow.com");
                pm.setRole("USER");
                userMapper.insert(pm);
                log.info("Demo user created: pm / 123456");
            }
        };
    }

    private boolean exists(UserMapper mapper, String username) {
        return mapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }
}
