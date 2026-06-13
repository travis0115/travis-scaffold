package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import com.travis.monolith.system.file.internal.mapper.SysFileFolderMapper;
import com.travis.monolith.system.file.internal.service.SysFileFolderService;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class SysFileFolderServiceImpl extends ServiceImpl<SysFileFolderMapper, SysFileFolder>
        implements SysFileFolderService {
    @Override
    public List<SysFileFolder> listAll() {
        return list(new LambdaQueryWrapperX<SysFileFolder>().orderByAsc(SysFileFolder::getSort));
    }

    @Override
    public void create(SysFileFolderCreateReq req) {
        var entity = new SysFileFolder();
        BeanUtils.copyProperties(req, entity);
        save(entity);
    }

    @Override
    public void update(Long id, SysFileFolderUpdateReq req) {
        var entity = new SysFileFolder();
        BeanUtils.copyProperties(req, entity);
        entity.setId(id);
        updateById(entity);
    }
}
