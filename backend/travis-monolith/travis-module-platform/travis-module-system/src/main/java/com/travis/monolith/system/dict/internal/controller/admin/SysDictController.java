package com.travis.monolith.system.dict.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.dict.api.request.*;
import com.travis.monolith.system.dict.api.response.SysDictItemResp;
import com.travis.monolith.system.dict.api.response.SysDictResp;
import com.travis.monolith.system.dict.internal.service.SysDictItemService;
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
public class SysDictController {

    /** 字典管理服务 */
    private final SysDictService dictService;

    /** 字典数据项服务 */
    private final SysDictItemService dictItemService;

    /** 获取字典树形数据（每个字典包含其下的数据项作为 children） */
    @GetMapping("/tree")
    @SaCheckPermission(value = SystemPermission.DICT_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysDictResp>> getTree() {
        return ApiResponse.success(dictService.listTree());
    }

    /** 分页查询字典类型列表 */
    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.DICT_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysDictResp>> page(@Valid SysDictPageReq req) {
        return ApiResponse.success(dictService.page(req));
    }

    /**
     * 获取字典类型详情
     *
     * @param id 字典ID
     * @return 字典类型实体
     */
    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.DICT_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysDictResp> get(@PathVariable Long id) {
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
    @SaCheckPermission(value = SystemPermission.DICT_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> add(@RequestBody @Valid SysDictCreateReq req) {
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
    @SaCheckPermission(value = SystemPermission.DICT_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysDictUpdateReq req) {
        dictService.update(id, req);
        return ApiResponse.success();
    }

    /** 修改字典状态 */
    @OperationLog(action = "修改字典状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.DICT_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        dictService.updateStatus(id, status);
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
    @SaCheckPermission(value = SystemPermission.DICT_DELETE, type = LoginType.ADMIN)
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
    @SaCheckPermission(value = SystemPermission.DICT_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysDictItemResp>> listItems(@PathVariable Long dictId) {
        return ApiResponse.success(dictItemService.listItemByDictId(dictId));
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
    @SaCheckPermission(value = SystemPermission.DICT_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> createItem(@RequestBody @Valid SysDictItemCreateReq req) {
        dictItemService.create(req);
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
    @SaCheckPermission(value = SystemPermission.DICT_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateItem(
            @PathVariable Long id, @RequestBody @Valid SysDictItemUpdateReq req) {
        dictItemService.update(id, req);
        return ApiResponse.success();
    }

    /** 修改字典数据项状态 */
    @OperationLog(action = "修改字典项状态")
    @NoRepeatSubmit
    @PutMapping("/item/{id}/status")
    @SaCheckPermission(value = SystemPermission.DICT_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateItemStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        dictItemService.updateStatus(id, status);
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
    @SaCheckPermission(value = SystemPermission.DICT_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> deleteItemById(@PathVariable Long id) {
        dictItemService.deleteById(id);
        return ApiResponse.success();
    }
}
