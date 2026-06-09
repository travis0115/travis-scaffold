package com.travis.monolith.system.dept.api.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

/**
 * 部门删除事件，通知其他模块清理与被删除部门相关的数据
 *
 * @author travis
 */
@Getter
public class DeptDeletedEvent {

    /** 被删除的部门ID列表（包含所有下级部门） */
    private final List<Long> deptIds;

    @JsonCreator
    public DeptDeletedEvent(@JsonProperty("deptIds") List<Long> deptIds) {
        this.deptIds = deptIds;
    }
}
