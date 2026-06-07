package com.travis.monolith.system;

import org.springframework.modulith.Modulithic;

/**
 * @author Travis
 */
@Modulithic(
        additionalPackages = {
                "${travis.info.base-package}.system",
                "${travis.info.base-package}.system.log"
        })
public class ApplicationModule {}
