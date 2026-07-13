package com.travis.monolith.system.message.api;

import com.travis.monolith.system.message.api.response.SysMessageSourceContentResp;

/** 引用型消息的来源内容解析器。 */
public interface SysMessageSourceContentProvider {

    String getSourceType();

    SysMessageSourceContentResp get(String sourceId);
}
