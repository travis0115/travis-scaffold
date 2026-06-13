package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysFileStorageConfigServiceImpl
        extends ServiceImpl<SysFileStorageConfigMapper, SysFileStorageConfig>
        implements SysFileStorageConfigService {
    @Override
    @Transactional
    public void create(SysFileStorageConfigCreateReq req) {
        resetDefault(req.getIsDefault());
        var entity = new SysFileStorageConfig();
        BeanUtils.copyProperties(req, entity);
        save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, SysFileStorageConfigUpdateReq req) {
        resetDefault(req.getIsDefault());
        var entity = new SysFileStorageConfig();
        BeanUtils.copyProperties(req, entity);
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
