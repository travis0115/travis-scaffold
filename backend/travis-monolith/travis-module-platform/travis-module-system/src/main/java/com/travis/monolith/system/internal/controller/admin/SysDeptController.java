package com.travis.monolith.system.internal.controller.admin;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.monolith.system.internal.model.request.dept.SysDeptReq;
import com.travis.monolith.system.internal.model.response.dept.SysDeptResp;
import com.travis.monolith.system.internal.service.SysDeptService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 部门管理控制器，提供部门树的增删改查接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
@Validated
public class SysDeptController {

    /** 部门管理服务 */
    private final SysDeptService deptService;

    /**
     * 获取部门树形列表
     *
     * @return 部门树
     */
    @GetMapping("/list")
    public ApiResponse<List<SysDeptResp>> list() {
        return ApiResponse.success(deptService.getDeptTree());
    }

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SysDeptResp> getDetail(@PathVariable Long id) {
        return ApiResponse.success(deptService.getDeptDetail(id));
    }

    /**
     * 新增部门
     *
     * @param req 部门信息
     * @return 空响应
     */
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid SysDeptReq req) {
        deptService.addDept(req);
        return ApiResponse.success();
    }

    /**
     * 更新部门信息
     *
     * @param id 部门ID
     * @param req 部门信息
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid SysDeptReq req) {
        deptService.updateDept(id, req);
        return ApiResponse.success();
    }

    /**
     * 删除部门（递归删除所有下级部门，关联用户的部门字段将重置为空）
     *
     * @param id 部门ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        deptService.deleteDept(id);
        return ApiResponse.success();
    }
}
