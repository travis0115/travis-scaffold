package com.travis.monolith.system.common.internal;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.modulith.core.ApplicationModuleSourceFactory;

/**
 * @author Travis
 */
public class SystemModuleSourceFactory implements ApplicationModuleSourceFactory {

    @Override
    public @NonNull List<String> getRootPackages() {
        return List.of("com.travis.monolith.system");
    }
}
