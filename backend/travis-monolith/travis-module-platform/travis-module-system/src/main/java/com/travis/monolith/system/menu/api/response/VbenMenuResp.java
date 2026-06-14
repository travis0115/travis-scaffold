package com.travis.monolith.system.menu.api.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vben Admin 前端菜单路由格式，用于前端动态路由渲染
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VbenMenuResp {
    /** 路由名称（对应 Vue Router 的 name） */
    private String name;

    /** 路由路径 */
    private String path;

    /** 前端组件（一级目录使用 BasicLayout） */
    private String component;

    /** 重定向路径 */
    private String redirect;

    /** 路由元信息 */
    private Map<String, Object> meta;

    /** 子路由列表 */
    private List<VbenMenuResp> children;
}
