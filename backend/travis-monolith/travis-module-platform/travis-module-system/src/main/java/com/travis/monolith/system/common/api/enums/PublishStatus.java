package com.travis.monolith.system.common.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 通用发布状态。 */
@Getter
@AllArgsConstructor
public enum PublishStatus {
    /** 草稿。 */
    DRAFT(0),

    /** 已发布。 */
    PUBLISHED(1),

    /** 已撤回。 */
    REVOKED(2);

    private final Integer value;
}
