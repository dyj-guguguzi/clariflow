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
 * MyBatis-Plus TypeHandler for {@code List<String>} ↔ JSON.
 *
 * <p>Serializes a Java {@code List<String>} to a JSON array string (VARCHAR)
 * when writing to the database, and deserializes it back when reading.
 * Handles null/empty values gracefully by returning an empty list.</p>
 *
 * <p>Used for {@code tags} and {@code acceptanceCriteria} fields
 * in the {@code work_item} table.</p>
 */
@MappedTypes({List.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class JsonListTypeHandler extends AbstractJsonTypeHandler<List<String>> {

    private static final Logger log = LoggerFactory.getLogger(JsonListTypeHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_STRING_TYPE = new TypeReference<List<String>>() {};

    /**
     * Parses a JSON array string into a {@code List<String>}.
     *
     * @param json the JSON array string from the database
     * @return parsed list, or empty list if null/empty/parse error
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
     * Serializes a {@code List<String>} to a JSON array string.
     *
     * @param obj the list to serialize
     * @return JSON array string, or {@code "[]"} if null/empty
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
