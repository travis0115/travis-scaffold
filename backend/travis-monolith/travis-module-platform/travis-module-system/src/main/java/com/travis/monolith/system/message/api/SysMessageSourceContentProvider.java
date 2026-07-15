package com.travis.monolith.system.message.api;

import com.travis.monolith.system.message.api.response.SysMessageSourceContentResp;

/** 引用型消息的来源内容解析器。 */
public interface SysMessageSourceContentProvider {

    /** 获取当前提供者支持的业务来源类型。 */
    String getSourceType();

    /** 根据业务来源 ID 查询可展示的来源内容。 */
    SysMessageSourceContentResp get(String sourceId);
}
