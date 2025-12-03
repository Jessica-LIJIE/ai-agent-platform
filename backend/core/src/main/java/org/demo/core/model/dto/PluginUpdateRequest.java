package org.demo.core.model.dto;

import lombok.Data;

import java.util.Map;

/**
 * 更新插件请求DTO
 * 所有字段均为可选，传入则更新，未传入保持原配置
 */
@Data
public class PluginUpdateRequest {

    /**
     * 插件名称
     */
    private String name;

    /**
     * 插件描述
     */
    private String description;

    /**
     * 请求端点URL
     */
    private String endpoint;

    /**
     * HTTP请求方法
     */
    private String method;

    /**
     * 请求参数Schema
     */
    private Map<String, Object> paramsSchema;

    /**
     * 响应Schema
     */
    private Map<String, Object> responseSchema;

    /**
     * 鉴权类型
     */
    private String authType;

    /**
     * 鉴权配置
     */
    private Map<String, Object> authConfig;
}
