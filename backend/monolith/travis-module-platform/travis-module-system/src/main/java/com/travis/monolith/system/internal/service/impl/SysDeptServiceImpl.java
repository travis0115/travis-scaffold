package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.exception.IErrorCode;
import com.travis.monolith.system.internal.converter.SystemConverter;
import com.travis.monolith.system.internal.mapper.SysDeptMapper;
import com.travis.monolith.system.internal.model.entity.SysDept;
import com.travis.monolith.system.internal.model.req.SysMenuReq;
import com.travis.monolith.system.internal.model.resp.SysDeptResp;
import com.travis.monolith.system.internal.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门管理服务实现，支持树形部门结构的构建
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    /** 对象转换器 */
    private final SystemConverter converter;

    /**
     * 获取部门树形列表
     */
    @Override
    @Cacheable(value = "system:dept:tree", key = "'all'")
    public List<SysDeptResp> getDeptTree() {
        // 查询全部部门，转为 VO 后构建树形结构
        List<SysDept> allDepts = list();
        List<SysDeptResp> voList = converter.toDeptRespList(allDepts);
        voList.forEach(v -> v.setChildren(new ArrayList<>()));
        return buildTree(voList);
    }

    /**
     * 获取部门详情
     */
    @Override
    public SysDeptResp getDeptDetail(Long id) {
        SysDept dept = getById(id);
        if (dept == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        SysDeptResp resp = converter.toDeptResp(dept);
        resp.setChildren(new ArrayList<>());
        return resp;
    }

    /**
     * 新增部门
     */
    @Override
    @Transactional
    @CacheEvict(value = "system:dept:tree", key = "'all'")
    public void addDept(SysMenuReq.SysDeptReq req) {
        SysDept dept = new SysDept();
        dept.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        dept.setDeptName(req.getDeptName());
        dept.setSort(req.getSort());
        dept.setLeader(req.getLeader());
        dept.setMobile(req.getMobile());
        dept.setStatus(req.getStatus());
        save(dept);
    }

    /**
     * 更新部门信息
     */
    @Override
    @CacheEvict(value = "system:dept:tree", key = "'all'")
    public void updateDept(Long id, SysMenuReq.SysDeptReq req) {
        SysDept dept = getById(id);
        if (dept == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        dept.setParentId(req.getParentId());
        dept.setDeptName(req.getDeptName());
        dept.setSort(req.getSort());
        dept.setLeader(req.getLeader());
        dept.setMobile(req.getMobile());
        dept.setStatus(req.getStatus());
        updateById(dept);
    }

    /**
     * 删除部门，存在子部门时禁止删除
     */
    @Override
    @Transactional
    @CacheEvict(value = "system:dept:tree", key = "'all'")
    public void deleteDept(Long id) {
        long childCount = count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, id));
        if (childCount > 0) {
            throw new BizException(new IErrorCode() {
                @Override
                public String getCode() {
                    return CommonErrorCode.BAD_REQUEST.getCode();
                }

                @Override
                public String getMsg() {
                    return "存在子部门，无法删除";
                }
            }, null);
        }
        removeById(id);
    }

    /**
     * 根据 parentId 分组，将子节点挂到对应的父节点上，返回顶层节点（parentId=0）
     *
     * @param all 所有部门视图列表
     * @return 顶层部门树
     */
    private List<SysDeptResp> buildTree(List<SysDeptResp> all) {
        Map<Long, List<SysDeptResp>> grouped = all.stream()
                .collect(Collectors.groupingBy(SysDeptResp::getParentId));
        all.forEach(node -> node.setChildren(grouped.getOrDefault(node.getId(), new ArrayList<>())));
        return all.stream()
                .filter(node -> node.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
