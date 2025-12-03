package org.demo.core.model.vo;

import lombok.Data;

import java.util.Map;

/**
 * 插件操作VO
 */
@Data
public class PluginOperationVO {

    /**
     * 操作记录ID
     */
    private String id;

    /**
     * OpenAPI中的operationId（如getSensorData）
     */
    private String operationId;

    /**
     * 操作名称，用于前端展示
     */
    private String name;

    /**
     * HTTP方法（GET/POST/PUT/DELETE等）
     */
    private String method;

    /**
     * 请求路径（如/plugin/sensor-data）
     */
    private String path;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 入参结构（JSON Schema）
     */
    private Map<String, Object> inputSchema;

    /**
     * 出参结构（JSON Schema）
     */
    private Map<String, Object> outputSchema;
}
