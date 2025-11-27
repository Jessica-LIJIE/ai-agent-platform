package org.demo.core.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LLM模型表实体类
 */
@Data
@TableName("llm_models")
public class LlmModel {

    /**
     * 模型唯一标识
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 模型名称
     */
    @TableField("name")
    private String name;

    /**
     * 模型显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 提供商代码
     */
    @TableField("provider")
    private String provider;

    /**
     * 模型类型
     */
    @TableField("model_type")
    private String modelType;

    /**
     * API基础URL
     */
    @TableField("api_base")
    private String apiBase;

    /**
     * API密钥
     */
    @TableField("api_key")
    private String apiKey;

    /**
     * API版本
     */
    @TableField("api_version")
    private String apiVersion;

    /**
     * 最大token数
     */
    @TableField("max_tokens")
    private Integer maxTokens;

    /**
     * 温度参数
     */
    @TableField("temperature")
    private BigDecimal temperature;

    /**
     * top_p参数
     */
    @TableField("top_p")
    private BigDecimal topP;

    /**
     * 是否启用深度思考
     */
    @TableField("enable_deep_thinking")
    private Boolean enableDeepThinking;

    /**
     * 频率惩罚参数
     */
    @TableField("frequency_penalty")
    private BigDecimal frequencyPenalty;

    /**
     * 存在惩罚参数
     */
    @TableField("presence_penalty")
    private BigDecimal presencePenalty;

    /**
     * 其他配置参数
     */
    @TableField("config")
    private String config;

    /**
     * 模型描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 是否默认模型
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 是否系统内置
     */
    @TableField("is_system")
    private Boolean isSystem;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
