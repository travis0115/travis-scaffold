package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.monolith.system.internal.converter.SysDeptConverter;
import com.travis.monolith.system.internal.mapper.SysDeptMapper;
import com.travis.monolith.system.internal.mapper.SysUserMapper;
import com.travis.monolith.system.internal.model.entity.SysDept;
import com.travis.monolith.system.internal.model.entity.SysUser;
import com.travis.monolith.system.internal.model.request.dept.SysDeptReq;
import com.travis.monolith.system.internal.model.response.dept.SysDeptResp;
import com.travis.monolith.system.internal.service.SysDeptService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门管理服务实现，支持树形部门结构的构建
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept>
        implements SysDeptService {

    /** 对象转换器 */
    private final SysDeptConverter converter;

    /** 用户 Mapper（用于检查部门关联用户） */
    private final SysUserMapper userMapper;

    /** 获取部门树形列表 */
    @Override
    @Cacheable(value = "system:dept:tree", key = "'all'")
    public List<SysDeptResp> getDeptTree() {
        // 查询全部部门，转为 VO 后构建树形结构
        List<SysDept> allDepts = list();
        List<SysDeptResp> voList = converter.toRespList(allDepts);
        voList.forEach(v -> v.setChildren(new ArrayList<>()));
        return buildTree(voList);
    }

    /** 获取部门详情 */
    @Override
    public SysDeptResp getDeptDetail(Long id) {
        SysDept dept = getById(id);
        if (dept == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        SysDeptResp resp = converter.toResp(dept);
        resp.setChildren(new ArrayList<>());
        return resp;
    }

    /** 新增部门 */
    @Override
    @Transactional
    @CacheEvict(value = "system:dept:tree", key = "'all'")
    public void addDept(SysDeptReq req) {
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
    public void updateDept(Long id, SysDeptReq req) {
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

    /** 删除部门（递归删除所有下级部门），删除前将关联用户的部门重置为 null */
    @Override
    @Transactional
    @CacheEvict(value = "system:dept:tree", key = "'all'")
    public void deleteDept(Long id) {
        List<Long> ids = new ArrayList<>();
        collectAllDescendantIds(id, ids);
        ids.add(id);
        // 将关联用户的部门重置为 null
        for (Long deptId : ids) {
            List<SysUser> users =
                    userMapper.selectList(
                            new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId));
            for (SysUser user : users) {
                user.setDeptId(null);
                userMapper.updateById(user);
            }
        }
        removeBatchByIds(ids);
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
