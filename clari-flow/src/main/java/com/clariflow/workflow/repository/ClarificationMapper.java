package com.clariflow.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clariflow.workflow.model.entity.Clarification;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for {@link Clarification}.
 *
 * <p>Inherits standard CRUD operations from {@link BaseMapper}.</p>
 */
@Mapper
public interface ClarificationMapper extends BaseMapper<Clarification> {
    // Standard CRUD inherited from BaseMapper<Clarification>
}
