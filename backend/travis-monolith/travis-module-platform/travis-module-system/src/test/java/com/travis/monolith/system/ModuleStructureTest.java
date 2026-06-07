package com.travis.monolith.system;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * System 模块结构验证测试。
 *
 * <p>使用 Spring Modulith 验证 system 模块内部的子模块边界： 各子模块之间的依赖关系是否符合 Modulith 规范。
 *
 * @author travis
 */
class ModuleStructureTest {

    ApplicationModules modules = ApplicationModules.of("com.travis.monolith.system");

    @Test
    void verifyModuleStructure() {
        modules.verify();
    }

    @Test
    void printModuleDocumentation() {
        modules.stream().forEach(System.out::println);
    }
}
