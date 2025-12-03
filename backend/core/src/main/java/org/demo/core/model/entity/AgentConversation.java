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
 * 智能体对话历史实体类
 * 对应数据库表: agent_conversation
 */
@Data
@TableName(value = "agent_conversation", autoResultMap = true)
public class AgentConversation {

    /**
     * 对话元数据内部类
     */
    @Data
    public static class ConversationMetadata {
        /**
         * 使用的LLM模型ID
         */
        private String llmModelId;

        /**
         * 输入Token数
         */
        private Integer promptTokens;

        /**
         * 输出Token数
         */
        private Integer completionTokens;

        /**
         * 总Token数
         */
        private Integer totalTokens;

        /**
         * 引用来源列表
         */
        private List<String> references;

        /**
         * 其他扩展信息
         */
        private String remarks;
    }

    /**
     * 对话记录ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 会话ID（聚合多轮对话）
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 智能体ID
     */
    @TableField("agent_id")
    private String agentId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 用户提问
     */
    @TableField("query")
    private String query;

    /**
     * 智能体回答
     */
    @TableField("answer")
    private String answer;

    /**
     * 元数据（引用来源、Token消耗）
     */
    @TableField(value = "metadata", typeHandler = JacksonTypeHandler.class)
    private ConversationMetadata metadata;

    /**
     * 类型（chat/debug）
     */
    @TableField("conversation_type")
    private String conversationType = "chat"; // 默认为chat类型

    /**
     * 可选的前端请求中携带的插件调用指令（仅在请求/内存中使用，不持久化）
     */
    @TableField(exist = false)
    private PluginCall pluginCall;

    @Data
    public static class PluginCall {
        private String pluginId;
        private String operationId;
        private Map<String, Object> params;
    }

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
