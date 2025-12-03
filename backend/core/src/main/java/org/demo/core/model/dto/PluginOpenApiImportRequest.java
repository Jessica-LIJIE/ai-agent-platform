package org.demo.core.model.dto;

import java.util.List;
import java.util.Map;

/**
 * 插件OpenAPI/JSON导入请求DTO
 * 支持两种模式：
 * 1. OpenAPI规范导入（标准OpenAPI 3.0格式）
 * 2. 简化JSON导入（AIOT设备等场景）
 */
public class PluginOpenApiImportRequest {

    /**
     * 插件名称
     */
    private String name;

    /**
     * 插件描述
     */
    private String description;

    /**
     * 插件类型：openapi, rest, aiot
     */
    private String type;

    /**
     * 基础URL（API服务地址）
     */
    private String baseUrl;

    /**
     * OpenAPI规范内容（JSON格式的字符串或Map）
     * 如果提供此字段，将解析OpenAPI规范自动提取operations
     */
    private Object openapiSpec;

    /**
     * 简化的操作列表（当不使用OpenAPI规范时）
     */
    private List<OperationDefinition> operations;

    /**
     * 鉴权类型：none, api_key, bearer, oauth2
     */
    private String authType;

    /**
     * 鉴权配置
     */
    private Map<String, Object> authConfig;

    // =============== Getters and Setters ===============

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Object getOpenapiSpec() {
        return openapiSpec;
    }

    public void setOpenapiSpec(Object openapiSpec) {
        this.openapiSpec = openapiSpec;
    }

    public List<OperationDefinition> getOperations() {
        return operations;
    }

    public void setOperations(List<OperationDefinition> operations) {
        this.operations = operations;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public Map<String, Object> getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(Map<String, Object> authConfig) {
        this.authConfig = authConfig;
    }

    /**
     * 操作定义（简化模式）
     */
    public static class OperationDefinition {
        /**
         * 操作ID（唯一标识）
         */
        private String operationId;

        /**
         * 操作名称
         */
        private String name;

        /**
         * HTTP方法：GET, POST, PUT, DELETE等
         */
        private String method;

        /**
         * 请求路径（相对于baseUrl）
         */
        private String path;

        /**
         * 操作描述
         */
        private String description;

        /**
         * 输入参数Schema
         */
        private Map<String, Object> inputSchema;

        /**
         * 输出响应Schema
         */
        private Map<String, Object> outputSchema;

        // =============== Getters and Setters ===============

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String operationId) {
            this.operationId = operationId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> getInputSchema() {
            return inputSchema;
        }

        public void setInputSchema(Map<String, Object> inputSchema) {
            this.inputSchema = inputSchema;
        }

        public Map<String, Object> getOutputSchema() {
            return outputSchema;
        }

        public void setOutputSchema(Map<String, Object> outputSchema) {
            this.outputSchema = outputSchema;
        }
    }
}
