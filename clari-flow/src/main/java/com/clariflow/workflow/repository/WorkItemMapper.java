package com.clariflow.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clariflow.workflow.model.entity.WorkItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@link WorkItem} 的 MyBatis-Plus Mapper。
 *
 * <p>继承自 {@link BaseMapper}，拥有标准的 CRUD 操作。
 * 可在此处按需添加自定义查询。</p>
 */
@Mapper
public interface WorkItemMapper extends BaseMapper<WorkItem> {
    // 标准 CRUD 继承自 BaseMapper<WorkItem>
}
