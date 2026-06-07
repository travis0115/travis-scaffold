package com.travis.monolith.server;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * 应用级模块结构验证测试。
 *
 * <p>以 {@link MonolithServerApplication} 为入口，验证顶层模块边界：
 * system、demo 等模块之间不能互相访问 internal 包。
 *
 * @author travis
 */
class ApplicationModuleStructureTest {

    ApplicationModules modules = ApplicationModules.of(MonolithServerApplication.class);

    @Test
    void verifyModuleStructure() {
        modules.verify();
    }

    @Test
    void printModuleDocumentation() {
        modules.stream().forEach(System.out::println);
    }
}
