package com.travis.monolith.system.file.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 文件存储配置分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileStorageConfigPageReq extends PageRequest {}
