package com.clariflow.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clariflow.workflow.model.entity.Clarification;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@link Clarification} 的 MyBatis-Plus Mapper。
 *
 * <p>继承自 {@link BaseMapper}，拥有标准的 CRUD 操作。</p>
 */
@Mapper
public interface ClarificationMapper extends BaseMapper<Clarification> {
    // 标准 CRUD 继承自 BaseMapper<Clarification>
}
