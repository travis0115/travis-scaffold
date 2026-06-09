package com.travis.monolith.system.common.internal;

import org.jspecify.annotations.NonNull;
import org.springframework.modulith.core.ApplicationModuleSourceFactory;

import java.util.List;

/**
 *
 * @author Travis
 */
public class SystemModuleSourceFactory
        implements ApplicationModuleSourceFactory {

    @Override
    public @NonNull List<String> getRootPackages() {
        return List.of("com.travis.monolith.system");
    }

}
