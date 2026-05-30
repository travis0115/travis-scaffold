package com.travis.monolith.system.internal.controller;

import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.model.entity.SysDict;
import com.travis.monolith.system.internal.model.req.SysMenuReq;
import com.travis.monolith.system.internal.model.resp.SysDictItemResp;
import com.travis.monolith.system.internal.service.SysDictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器，提供字典类型及字典数据项的增删改查接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    /** 字典管理服务 */
    private final SysDictService dictService;

    /**
     * 分页查询字典类型列表
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<SysDict>> page(
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(dictService.getDictPage(dictName, dictType, status, pageNum, pageSize));
    }

    /**
     * 获取字典类型详情
     *
     * @param id 字典ID
     * @return 字典类型实体
     */
    @GetMapping("/{id}")
    public ApiResponse<SysDict> getDetail(@PathVariable Long id) {
        return ApiResponse.success(dictService.getDictDetail(id));
    }

    /**
     * 新增字典类型
     *
     * @param req 字典类型信息
     * @return 空响应
     */
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid SysMenuReq.SysDictReq req) {
        dictService.addDict(req);
        return ApiResponse.success();
    }

    /**
     * 更新字典类型
     *
     * @param id  字典ID
     * @param req 字典类型信息
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid SysMenuReq.SysDictReq req) {
        dictService.updateDict(id, req);
        return ApiResponse.success();
    }

    /**
     * 删除字典类型
     *
     * @param id 字典ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dictService.deleteDict(id);
        return ApiResponse.success();
    }

    /**
     * 查询指定字典类型下的所有数据项
     *
     * @param dictId 字典类型ID
     * @return 字典数据项列表
     */
    @GetMapping("/items/{dictId}")
    public ApiResponse<List<SysDictItemResp>> getDictItems(@PathVariable Long dictId) {
        return ApiResponse.success(dictService.getDictItems(dictId));
    }

    /**
     * 新增字典数据项
     *
     * @param req 字典数据项信息
     * @return 空响应
     */
    @PostMapping("/item")
    public ApiResponse<Void> addDictItem(@RequestBody @Valid SysMenuReq.SysDictItemReq req) {
        dictService.addDictItem(req);
        return ApiResponse.success();
    }

    /**
     * 更新字典数据项
     *
     * @param id  数据项ID
     * @param req 字典数据项信息
     * @return 空响应
     */
    @PutMapping("/item/{id}")
    public ApiResponse<Void> updateDictItem(@PathVariable Long id, @RequestBody @Valid SysMenuReq.SysDictItemReq req) {
        dictService.updateDictItem(id, req);
        return ApiResponse.success();
    }

    /**
     * 删除字典数据项
     *
     * @param id 数据项ID
     * @return 空响应
     */
    @DeleteMapping("/item/{id}")
    public ApiResponse<Void> deleteDictItem(@PathVariable Long id) {
        dictService.deleteDictItem(id);
        return ApiResponse.success();
    }
}
