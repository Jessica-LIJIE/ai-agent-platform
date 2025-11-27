package org.demo.core.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM提供商实体类
 * 对应数据库表: llm_providers
 */
@Data
@TableName("llm_providers")
public class LlmProvider {

    /**
     * 提供商唯一标识
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 提供商代码
     */
    @TableField("code")
    private String code;

    /**
     * 提供商名称
     */
    @TableField("name")
    private String name;

    /**
     * 提供商完整标题
     */
    @TableField("title")
    private String title;

    /**
     * 提供商描述
     */
    @TableField("description")
    private String description;

    /**
     * API申请地址
     */
    @TableField("apply_url")
    private String applyUrl;

    /**
     * 文档地址
     */
    @TableField("doc_url")
    private String docUrl;

    /**
     * 默认API地址
     */
    @TableField("default_api_base")
    private String defaultApiBase;

    /**
     * 是否提供免费额度
     */
    @TableField("has_free_quota")
    private Boolean hasFreeQuota;

    /**
     * 图标URL或图标名称
     */
    @TableField("icon")
    private String icon;

    /**
     * 标签类型
     */
    @TableField("tag_type")
    private String tagType;

    /**
     * 国家
     */
    @TableField("country")
    private String country;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

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
