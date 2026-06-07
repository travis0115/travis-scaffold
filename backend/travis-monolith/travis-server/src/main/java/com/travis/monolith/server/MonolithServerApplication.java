package com.travis.monolith.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneOffset;
import java.util.TimeZone;

/**
 * 应用启动入口
 *
 * @author travis
 */
@SpringBootApplication(scanBasePackages = {"${travis.info.base-package}"})
public class MonolithServerApplication {

    static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        SpringApplication.run(MonolithServerApplication.class, args);
    }
}
