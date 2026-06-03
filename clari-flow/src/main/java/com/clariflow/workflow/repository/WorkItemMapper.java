package com.clariflow.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clariflow.workflow.model.entity.WorkItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for {@link WorkItem}.
 *
 * <p>Inherits standard CRUD operations from {@link BaseMapper}.
 * Custom queries can be added here as needed.</p>
 */
@Mapper
public interface WorkItemMapper extends BaseMapper<WorkItem> {
    // Standard CRUD inherited from BaseMapper<WorkItem>
}
