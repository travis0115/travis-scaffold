package com.travis.monolith.system.file.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import java.util.List;

public interface SysFileFolderService extends IService<SysFileFolder> {
    List<SysFileFolder> listAll();

    void create(SysFileFolderCreateReq req);

    void update(Long id, SysFileFolderUpdateReq req);
}
