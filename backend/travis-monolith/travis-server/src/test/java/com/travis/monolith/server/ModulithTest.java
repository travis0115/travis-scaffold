package com.travis.monolith.server;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.test.ApplicationModuleTest;

/**
 * @author Travis
 */
@ApplicationModuleTest
public class ModulithTest {

    ApplicationModules modules = ApplicationModules.of(MonolithServerApplication.class);

    // 验证模块结构
    @Test
    void verifyModuleStructure() {
        modules.forEach(System.out::println);
        modules.verify(); // 验证模块隔离性
    }
}
