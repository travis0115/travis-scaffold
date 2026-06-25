package com.travis.monolith.system.notice.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.notice.internal.entity.SysAnnouncement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAnnouncementMapper extends BaseMapperX<SysAnnouncement> {}
