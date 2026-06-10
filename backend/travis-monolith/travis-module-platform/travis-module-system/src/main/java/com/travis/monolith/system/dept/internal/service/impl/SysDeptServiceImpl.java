package com.travis.monolith.system.dept.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.event.MessagePublisher;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.monolith.system.common.api.SystemEvent;
import com.travis.monolith.system.dept.api.event.DeptDeletedPayload;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.converter.SysDeptConverter;
import com.travis.monolith.system.dept.internal.entity.SysDept;
import com.travis.monolith.system.dept.internal.mapper.SysDeptMapper;
import com.travis.monolith.system.dept.internal.request.SysDeptReq;
import com.travis.monolith.system.dept.internal.service.SysDeptService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 部门管理服务实现，支持树形部门结构的构建
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept>
        implements SysDeptService {

    /** 对象转换器 */
    private final SysDeptConverter converter;

    /** 消息发布器（用于发布部门删除事件） */
    private final MessagePublisher messagePublisher;

    /** 获取部门树形列表 */
    @Override
    @Cacheable(value = "system:dept:tree", key = "'all'")
    public List<SysDeptResp> listTree() {
        // 查询全部部门，转为 VO 后构建树形结构
        List<SysDept> allDepts = list();
        List<SysDeptResp> voList = converter.toRespList(allDepts);
        voList.forEach(v -> v.setChildren(new ArrayList<>()));
        return buildTree(voList);
    }

    /** 根据部门ID列表批量获取部门名称映射 */
    @Override
    public Map<Long, String> getDeptNameMapByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return listByIds(ids).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
    }

    /** 获取部门详情 */
    @Override
    public SysDeptResp getById(Long id) {
        SysDept dept = super.getById(id);
        if (dept == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        SysDeptResp resp = converter.toResp(dept);
        resp.setChildren(new ArrayList<>());
        return resp;
    }

    /** 根据部门ID查询部门名称 */
    @Override
    public String getDeptNameById(Long deptId) {
        if (deptId == null) {
            return null;
        }
        SysDept dept = super.getById(deptId);
        return dept != null ? dept.getDeptName() : null;
    }

    /** 新增部门 */
    @Override
    @Transactional
    @CacheEvict(value = "system:dept:tree", key = "'all'")
    public void create(SysDeptReq req) {
        SysDept dept = new SysDept();
        dept.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        dept.setDeptName(req.getDeptName());
        dept.setSort(req.getSort());
        dept.setLeader(req.getLeader());
        dept.setMobile(req.getMobile());
        dept.setStatus(req.getStatus());
        save(dept);
    }

    /** 更新部门信息 */
    @Override
    @CacheEvict(value = "system:dept:tree", key = "'all'")
    public void update(Long id, SysDeptReq req) {
        SysDept dept = super.getById(id);
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

    /** 删除部门（递归删除所有下级部门），通过事件通知用户模块清除关联 */
    @Override
    @Transactional
    @CacheEvict(value = "system:dept:tree", key = "'all'")
    public void deleteById(Long id) {
        List<Long> ids = new ArrayList<>();
        collectAllDescendantIds(id, ids);
        ids.add(id);
        removeBatchByIds(ids);
        DeptDeletedPayload payload = new DeptDeletedPayload(ids);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        publishDeptDeletedEvent(payload);
                    }
                });
    }

    private void publishDeptDeletedEvent(DeptDeletedPayload payload) {
        try {
            messagePublisher.asyncPublish(
                    SystemEvent.DEPT_DELETED,
                    payload,
                    (event, body, options, ex) -> {
                        if (ex != null) {
                            log.error("部门删除事件发送失败, deptIds={}", payload.getDeptIds(), ex);
                        }
                    });
        } catch (RuntimeException e) {
            log.error("部门删除事件发送失败, deptIds={}", payload.getDeptIds(), e);
        }
    }

    /**
     * 递归收集所有下级部门ID
     *
     * @param parentId 父部门ID
     * @param ids 收集结果
     */
    private void collectAllDescendantIds(Long parentId, List<Long> ids) {
        List<SysDept> children =
                list(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, parentId));
        for (SysDept child : children) {
            ids.add(child.getId());
            collectAllDescendantIds(child.getId(), ids);
        }
    }

    /**
     * 根据 parentId 分组，将子节点挂到对应的父节点上，返回顶层节点（parentId=0）
     *
     * @param all 所有部门视图列表
     * @return 顶层部门树
     */
    private List<SysDeptResp> buildTree(List<SysDeptResp> all) {
        Map<Long, List<SysDeptResp>> grouped =
                all.stream().collect(Collectors.groupingBy(SysDeptResp::getParentId));
        all.forEach(
                node -> node.setChildren(grouped.getOrDefault(node.getId(), new ArrayList<>())));
        return all.stream().filter(node -> node.getParentId() == 0).collect(Collectors.toList());
    }
}
