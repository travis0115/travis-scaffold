package com.travis.monolith.ops.common.internal;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.modulith.core.ApplicationModuleSourceFactory;

/** ops 模块来源工厂。 */
public class OpsModuleSourceFactory implements ApplicationModuleSourceFactory {

    @Override
    public @NonNull List<String> getRootPackages() {
        return List.of("com.travis.monolith.ops");
    }
}
