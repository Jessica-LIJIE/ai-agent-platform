package org.demo.core.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 插件详情VO
 */
@Data
public class PluginDetailVO {

    /**
     * 插件ID
     */
    private String id;

    /**
     * 插件名称
     */
    private String name;

    /**
     * 插件描述
     */
    private String description;

    /**
     * 插件类型（http/aiot等）
     */
    private String type;

    /**
     * 基础请求地址
     */
    private String baseUrl;

    /**
     * HTTP请求方法（兼容旧API，取第一个operation的method）
     */
    private String method;

    /**
     * 请求端点URL（兼容旧API，取第一个operation的path）
     */
    private String endpoint;

    /**
     * 请求参数Schema（兼容旧API，取第一个operation的inputSchema）
     */
    private Map<String, Object> paramsSchema;

    /**
     * 响应Schema（兼容旧API，取第一个operation的outputSchema）
     */
    private Map<String, Object> responseSchema;

    /**
     * 鉴权类型
     */
    private String authType;

    /**
     * 鉴权配置（敏感信息会被脱敏）
     */
    private Map<String, Object> authConfig;

    /**
     * 插件状态（enabled/disabled）
     */
    private String status;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 插件操作列表（一个插件可以有多个接口）
     */
    private List<PluginOperationVO> operations;

    /**
     * 创建时间（毫秒时间戳）
     */
    @JsonProperty("create_time")
    private Long createTime;

    /**
     * 更新时间（毫秒时间戳）
     */
    @JsonProperty("update_time")
    private Long updateTime;
}
