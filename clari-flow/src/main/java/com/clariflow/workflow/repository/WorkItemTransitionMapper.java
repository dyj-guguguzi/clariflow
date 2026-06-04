package com.clariflow.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clariflow.workflow.model.entity.WorkItemTransition;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@link WorkItemTransition} 的 MyBatis-Plus Mapper。
 *
 * <p>继承自 {@link BaseMapper}，拥有标准的 CRUD 操作。</p>
 */
@Mapper
public interface WorkItemTransitionMapper extends BaseMapper<WorkItemTransition> {
    // 标准 CRUD 继承自 BaseMapper<WorkItemTransition>
}
