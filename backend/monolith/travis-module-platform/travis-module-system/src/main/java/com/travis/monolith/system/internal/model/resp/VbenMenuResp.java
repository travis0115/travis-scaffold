package com.travis.monolith.system.internal.model.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Vben Admin 前端菜单路由格式，用于前端动态路由渲染
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private Meta meta;
    /** 子路由列表 */
    private List<VbenMenuResp> children;

    /**
     * 路由元信息，包含标题、排序和图标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {
        /** 菜单标题 */
        private String title;
        /** 排序号 */
        private Integer order;
        /** 图标名称 */
        private String icon;
        /** 是否固定在标签栏 */
        private Boolean affixTab;
    }
}
