package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.converter.SysFileFolderConverter;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import com.travis.monolith.system.file.internal.mapper.SysFileFolderMapper;
import com.travis.monolith.system.file.internal.service.SysFileFolderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysFileFolderServiceImpl extends ServiceImpl<SysFileFolderMapper, SysFileFolder>
        implements SysFileFolderService {

    private final SysFileFolderConverter converter;

    @Override
    public List<SysFileFolder> listAll() {
        return list(new LambdaQueryWrapperX<SysFileFolder>().orderByAsc(SysFileFolder::getSort));
    }

    @Override
    public void create(SysFileFolderCreateReq req) {
        save(converter.toEntity(req));
    }

    @Override
    public void update(Long id, SysFileFolderUpdateReq req) {
        var entity = converter.toEntity(req);
        entity.setId(id);
        updateById(entity);
    }
}
