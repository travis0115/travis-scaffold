package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.internal.converter.SysFileStorageConfigConverter;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysFileStorageConfigServiceImpl
        extends ServiceImpl<SysFileStorageConfigMapper, SysFileStorageConfig>
        implements SysFileStorageConfigService {

    private final SysFileStorageConfigConverter converter;

    @Override
    @Transactional
    public void create(SysFileStorageConfigCreateReq req) {
        resetDefault(req.getIsDefault());
        save(converter.toEntity(req));
    }

    @Override
    @Transactional
    public void update(Long id, SysFileStorageConfigUpdateReq req) {
        resetDefault(req.getIsDefault());
        var entity = converter.toEntity(req);
        entity.setId(id);
        updateById(entity);
    }

    private void resetDefault(Integer isDefault) {
        if (Integer.valueOf(1).equals(isDefault)) {
            lambdaUpdate()
                    .eq(SysFileStorageConfig::getIsDefault, 1)
                    .set(SysFileStorageConfig::getIsDefault, 0)
                    .update();
        }
    }
}
