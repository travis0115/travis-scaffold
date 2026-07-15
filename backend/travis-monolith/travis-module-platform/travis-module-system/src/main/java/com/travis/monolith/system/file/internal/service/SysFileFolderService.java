package com.travis.monolith.system.file.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import java.util.List;

/** 文件夹管理服务。 */
public interface SysFileFolderService extends IService<SysFileFolder> {
    /** 查询全部文件夹。 */
    List<SysFileFolder> listAll();

    /** 创建文件夹。 */
    void create(SysFileFolderCreateReq req);

    /** 更新文件夹。 */
    void update(Long id, SysFileFolderUpdateReq req);

    /** 删除文件夹及其子文件夹。 */
    void deleteById(Long id);
}
