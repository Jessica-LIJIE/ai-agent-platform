package org.demo.core.model.dto;

import lombok.Data;

import java.util.Map;

/**
 * 创建插件请求DTO
 */
@Data
public class PluginCreateRequest {

    /**
     * 插件名称（必填）
     */
    private String name;

    /**
     * 插件描述
     */
    private String description;

    /**
     * 请求端点URL（如 https://api.example.com/order/query）
     */
    private String endpoint;

    /**
     * HTTP请求方法（GET/POST/PUT/DELETE）
     */
    private String method;

    /**
     * 请求参数Schema（JSON Schema格式）
     */
    private Map<String, Object> paramsSchema;

    /**
     * 响应Schema（JSON Schema格式）
     */
    private Map<String, Object> responseSchema;

    /**
     * 鉴权类型（none/API_KEY/oauth等）
     */
    private String authType;

    /**
     * 鉴权配置
     */
    private Map<String, Object> authConfig;
}
