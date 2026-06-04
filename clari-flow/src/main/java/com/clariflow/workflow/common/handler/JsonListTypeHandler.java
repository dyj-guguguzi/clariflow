package com.clariflow.workflow.common.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * MyBatis-Plus TypeHandler，用于 {@code List<String>} ↔ JSON 转换。
 *
 * <p>将 Java {@code List<String>} 序列化为 JSON 数组字符串（VARCHAR）
 * 写入数据库，读取时再反序列化回来。
 * 对 null/空值进行安全处理，返回空列表。</p>
 *
 * <p>用于 {@code work_item} 表中的 {@code tags} 和 {@code acceptanceCriteria} 字段。</p>
 */
@MappedTypes({List.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class JsonListTypeHandler extends AbstractJsonTypeHandler<List<String>> {

    private static final Logger log = LoggerFactory.getLogger(JsonListTypeHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_STRING_TYPE = new TypeReference<List<String>>() {};

    /**
     * 将 JSON 数组字符串解析为 {@code List<String>}。
     *
     * @param json 来自数据库的 JSON 数组字符串
     * @return 解析后的列表，如果为 null/空/解析错误则返回空列表
     */
    @Override
    protected List<String> parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, LIST_STRING_TYPE);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON list: {}", json, e);
            return Collections.emptyList();
        }
    }

    /**
     * 将 {@code List<String>} 序列化为 JSON 数组字符串。
     *
     * @param obj 要序列化的列表
     * @return JSON 数组字符串，如果为 null/空则返回 {@code "[]"}
     */
    @Override
    protected String toJson(List<String> obj) {
        if (obj == null || obj.isEmpty()) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize list to JSON: {}", obj, e);
            return "[]";
        }
    }
}
