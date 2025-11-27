package org.demo.core.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 智能体实体类
 * 对应数据库表: agent
 */
@Data
@TableName(value = "agent", autoResultMap = true)
public class Agent {

    /**
     * 智能体唯一标识
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 智能体名称（必填）
     */
    @TableField("name")
    private String name;

    /**
     * 智能体描述
     */
    @TableField("description")
    private String description;

    /**
     * 智能体提示词
     */
    @TableField("prompt")
    private String prompt;

    /**
     * 系统提示词模板（别名字段）
     */
    @TableField("prompt_template")
    private String promptTemplate;

    /**
     * 模型配置（存储model、temperature、api_key_id等参数）
     */
    @TableField(value = "model_config", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> modelConfig;

    /**
     * 智能体状态（draft/published）
     */
    @TableField("status")
    private String status;

    /**
     * 创建者ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 绑定的工作流ID
     */
    @TableField("workflow_id")
    private String workflowId;

    /**
     * 绑定的知识库ID
     */
    @TableField("knowledge_base_id")
    private String knowledgeBaseId;

    /**
     * 关联的知识库ID列表（JSON数组格式）
     */
    @TableField(value = "kb_ids", typeHandler = JacksonTypeHandler.class)
    private List<String> kbIds;

    /**
     * 绑定的插件配置（JSON数组存储插件ID列表）
     */
    @TableField(value = "tools_config", typeHandler = JacksonTypeHandler.class)
    private List<String> toolsConfig;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
