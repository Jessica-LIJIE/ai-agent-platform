package org.demo.core.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 插件操作表实体类
 * 功能: 存储每个插件的接口操作信息
 * 关联用户故事: US-014, US-015, US-016
 */
@Data
@TableName(value = "plugin_operation", autoResultMap = true)
public class PluginOperation {

    /**
     * 插件操作唯一标识
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属插件ID
     */
    @TableField("plugin_id")
    private String pluginId;

    /**
     * OpenAPI中的operationId（如getSensorData）
     */
    @TableField("operation_id")
    private String operationId;

    /**
     * 操作名称，用于前端展示
     */
    @TableField("name")
    private String name;

    /**
     * HTTP方法（GET/POST/PUT/DELETE等）
     */
    @TableField("method")
    private String method;

    /**
     * 请求路径（如/plugin/sensor-data）
     */
    @TableField("path")
    private String path;

    /**
     * 操作描述
     */
    @TableField("description")
    private String description;

    /**
     * 入参结构（从OpenAPI解析的参数信息）
     */
    @TableField(value = "input_schema", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> inputSchema;

    /**
     * 出参结构（从OpenAPI解析的响应信息）
     */
    @TableField(value = "output_schema", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> outputSchema;

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
