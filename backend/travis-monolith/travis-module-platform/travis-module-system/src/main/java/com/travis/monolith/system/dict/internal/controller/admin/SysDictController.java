package com.travis.monolith.system.dict.internal.controller.admin;

import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmitNamespace;
import com.travis.monolith.system.dict.api.request.SysDictItemReq;
import com.travis.monolith.system.dict.api.request.SysDictReq;
import com.travis.monolith.system.dict.api.response.SysDictItemResp;
import com.travis.monolith.system.dict.internal.entity.SysDict;
import com.travis.monolith.system.dict.internal.service.SysDictService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 字典管理控制器，提供字典类型及字典数据项的增删改查接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
@Validated
@OperationLogModule("字典管理")
@NoRepeatSubmitNamespace("system:dict")
public class SysDictController {

    /** 字典管理服务 */
    private final SysDictService dictService;

    /** 获取字典树形数据（每个字典包含其下的数据项作为 children） */
    @GetMapping("/tree")
    public ApiResponse<List<SysDict>> getTree() {
        return ApiResponse.success(dictService.listTree());
    }

    /** 分页查询字典类型列表 */
    @GetMapping("/page")
    public ApiResponse<PageResp<SysDict>> page(
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(dictService.page(dictName, dictType, status, pageNum, pageSize));
    }

    /**
     * 获取字典类型详情
     *
     * @param id 字典ID
     * @return 字典类型实体
     */
    @GetMapping("/{id}")
    public ApiResponse<SysDict> getDetail(@PathVariable Long id) {
        return ApiResponse.success(dictService.getById(id));
    }

    /**
     * 新增字典类型
     *
     * @param req 字典类型信息
     * @return 空响应
     */
    @OperationLog(action = "新增字典")
    @NoRepeatSubmit
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid SysDictReq req) {
        dictService.create(req);
        return ApiResponse.success();
    }

    /**
     * 更新字典类型
     *
     * @param id 字典ID
     * @param req 字典类型信息
     * @return 空响应
     */
    @OperationLog(action = "更新字典")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid SysDictReq req) {
        dictService.update(id, req);
        return ApiResponse.success();
    }

    /**
     * 删除字典类型
     *
     * @param id 字典ID
     * @return 空响应
     */
    @OperationLog(action = "删除字典")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dictService.deleteById(id);
        return ApiResponse.success();
    }

    /**
     * 查询指定字典类型下的所有数据项
     *
     * @param dictId 字典类型ID
     * @return 字典数据项列表
     */
    @GetMapping("/items/{dictId}")
    public ApiResponse<List<SysDictItemResp>> listItems(@PathVariable Long dictId) {
        return ApiResponse.success(dictService.listItems(dictId));
    }

    /**
     * 新增字典数据项
     *
     * @param req 字典数据项信息
     * @return 空响应
     */
    @OperationLog(action = "新增字典项")
    @NoRepeatSubmit
    @PostMapping("/item")
    public ApiResponse<Void> createItem(@RequestBody @Valid SysDictItemReq req) {
        dictService.createItem(req);
        return ApiResponse.success();
    }

    /**
     * 更新字典数据项
     *
     * @param id 数据项ID
     * @param req 字典数据项信息
     * @return 空响应
     */
    @OperationLog(action = "更新字典项")
    @NoRepeatSubmit
    @PutMapping("/item/{id}")
    public ApiResponse<Void> updateItem(
            @PathVariable Long id, @RequestBody @Valid SysDictItemReq req) {
        dictService.updateItem(id, req);
        return ApiResponse.success();
    }

    /**
     * 删除字典数据项
     *
     * @param id 数据项ID
     * @return 空响应
     */
    @OperationLog(action = "删除字典项")
    @NoRepeatSubmit
    @DeleteMapping("/item/{id}")
    public ApiResponse<Void> deleteItemById(@PathVariable Long id) {
        dictService.deleteItemById(id);
        return ApiResponse.success();
    }
}
