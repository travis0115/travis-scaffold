package com.travis.monolith.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口
 *
 * @author travis
 */

@SpringBootApplication(scanBasePackages = {"${travis.application.base-package}"})
public class MonolithServerApplication {

    static void main(String[] args) {
        SpringApplication.run(MonolithServerApplication.class, args);
    }
}

