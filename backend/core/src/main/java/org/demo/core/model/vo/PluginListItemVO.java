package org.demo.core.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 插件列表项VO
 */
@Data
public class PluginListItemVO {

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
     * 是否启用
     */
    @JsonProperty("is_enabled")
    private Boolean isEnabled;

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
