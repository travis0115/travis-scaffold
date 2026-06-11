package com.travis.monolith.system.file.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.system.file.api.request.SysFileFolderReq;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import java.util.List;

public interface SysFileFolderService extends IService<SysFileFolder> {
    List<SysFileFolder> listAll();

    void create(SysFileFolderReq req);

    void update(Long id, SysFileFolderReq req);
}
