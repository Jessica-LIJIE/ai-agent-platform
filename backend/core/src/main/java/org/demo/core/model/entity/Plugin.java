package org.demo.core.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 插件表实体类
 * 功能: 存储插件的注册信息、配置及状态
 * 关联用户故事: US-014, US-015, US-016
 */
@Data
@TableName(value = "plugin", autoResultMap = true)
public class Plugin {

    /**
     * 插件唯一标识
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 插件名称
     */
    @TableField("name")
    private String name;

    /**
     * 插件唯一标识符（key）
     */
    @TableField("identifier")
    private String identifier;

    /**
     * 插件描述
     */
    @TableField("description")
    private String description;

    /**
     * OpenAPI规范内容
     */
    @TableField(value = "openapi_spec", typeHandler = JacksonTypeHandler.class)
    private Object openapiSpec;

    /**
     * OpenAPI规范（别名字段）
     */
    @TableField(value = "openapi_schema", typeHandler = JacksonTypeHandler.class)
    private Object openapiSchema;

    /**
     * 插件状态（enabled/disabled）
     */
    @TableField("status")
    private String status;

    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Boolean isEnabled;

    /**
     * 鉴权信息（存储API Key等）
     */
    @TableField(value = "auth_info", typeHandler = JacksonTypeHandler.class)
    private Object authInfo;

    /**
     * 鉴权类型（none/api_key/oauth等）
     */
    @TableField("auth_type")
    private String authType;

    /**
     * 鉴权配置（别名字段）
     */
    @TableField(value = "auth_config", typeHandler = JacksonTypeHandler.class)
    private Object authConfig;

    /**
     * 注册者ID（NULL代表系统插件）
     */
    @TableField("user_id")
    private String userId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
