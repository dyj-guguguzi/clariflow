package com.clariflow.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clariflow.workflow.model.entity.WorkItemTransition;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for {@link WorkItemTransition}.
 *
 * <p>Inherits standard CRUD operations from {@link BaseMapper}.</p>
 */
@Mapper
public interface WorkItemTransitionMapper extends BaseMapper<WorkItemTransition> {
    // Standard CRUD inherited from BaseMapper<WorkItemTransition>
}
