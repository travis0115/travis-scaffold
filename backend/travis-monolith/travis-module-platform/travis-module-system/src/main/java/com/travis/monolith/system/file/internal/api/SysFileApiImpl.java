package com.travis.monolith.system.file.internal.api;

import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.file.internal.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 文件模块对外 API 实现，委托调用内部 Service
 *
 * @author travis
 */
@Component
@RequiredArgsConstructor
public class SysFileApiImpl implements SysFileApi {

    private final SysFileService fileService;

    @Override
    public String getFileUrl(String path) {
        return fileService.getFileUrl(path);
    }
}
