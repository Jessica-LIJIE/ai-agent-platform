package org.demo.core.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.core.model.entity.Plugin;
import org.demo.core.model.entity.PluginOperation;
import org.demo.core.model.vo.PluginInvokeResult;
import org.demo.core.service.PluginOperationService;
import org.demo.core.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent工具执行器
 * 负责管理和执行智能体可用的工具
 * 支持LLM Function Calling模式
 */
@Service
public class AgentToolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AgentToolExecutor.class);

    @Autowired
    private PluginService pluginService;

    @Autowired
    private PluginOperationService pluginOperationService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 工具名称到插件操作的映射缓存
     * key: toolName (格式: plugin_{pluginId}_{operationId})
     * value: ToolMapping
     */
    private final ConcurrentHashMap<String, ToolMapping> toolMappingCache = new ConcurrentHashMap<>();

    /**
     * 工具映射信息
     */
    private static class ToolMapping {
        String pluginId;
        String operationId;

        ToolMapping(String pluginId, String operationId) {
            this.pluginId = pluginId;
            this.operationId = operationId;
        }
    }

    /**
     * 获取Agent可用的所有工具定义
     * 用于发送给LLM的tools参数
     *
     * @param agentId 智能体ID
     * @return 工具定义列表（OpenAI Function Calling格式）
     */
    public List<AgentToolDefinition> getAvailableTools(String agentId) {
        List<AgentToolDefinition> tools = new ArrayList<>();

        // TODO: 根据agentId获取该智能体配置的插件列表
        // 目前简化处理：获取所有启用的插件的所有操作

        // 获取所有启用的插件及其操作
        List<Plugin> plugins = pluginService.listPlugins(1, 100).getList().stream()
                .map(vo -> {
                    // 这里需要获取完整的Plugin信息，简化处理
                    return pluginService.getPluginDetail(vo.getId());
                })
                .filter(detail -> Boolean.TRUE.equals(detail.getIsEnabled()))
                .map(detail -> {
                    Plugin p = new Plugin();
                    p.setId(detail.getId());
                    p.setName(detail.getName());
                    return p;
                })
                .toList();

        for (var pluginDetail : pluginService.listPlugins(1, 100).getList()) {
            var detail = pluginService.getPluginDetail(pluginDetail.getId());
            if (!Boolean.TRUE.equals(detail.getIsEnabled())) {
                continue;
            }

            String pluginId = detail.getId();
            String pluginName = detail.getName();

            // 获取该插件的所有操作
            var operations = pluginOperationService.listByPluginId(pluginId);

            for (PluginOperation op : operations) {
                // 生成唯一的工具名称
                String toolName = generateToolName(pluginId, op.getOperationId());

                // 创建工具定义
                AgentToolDefinition tool = AgentToolDefinition.fromPluginOperation(
                        pluginId,
                        op.getOperationId(),
                        toolName,
                        buildToolDescription(pluginName, op),
                        op.getInputSchema());

                tools.add(tool);

                // 缓存映射关系
                toolMappingCache.put(toolName, new ToolMapping(pluginId, op.getOperationId()));
            }
        }

        return tools;
    }

    /**
     * 执行工具调用
     * 当LLM返回tool_calls时调用此方法
     *
     * @param toolCallId LLM返回的tool_call_id
     * @param toolName   工具名称（function.name）
     * @param arguments  工具参数（JSON字符串）
     * @return 工具调用结果
     */
    public AgentToolCallResult executeToolCall(String toolCallId, String toolName, String arguments) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 解析工具映射
            ToolMapping mapping = toolMappingCache.get(toolName);
            if (mapping == null) {
                // 尝试从工具名称解析
                mapping = parseToolNameToMapping(toolName);
            }

            if (mapping == null) {
                return AgentToolCallResult.failure(toolCallId, toolName, "未找到工具: " + toolName);
            }

            // 2. 解析参数
            Map<String, Object> params = parseArguments(arguments);

            // 3. 调用插件
            PluginInvokeResult invokeResult = pluginService.invokeOperation(
                    mapping.pluginId,
                    mapping.operationId,
                    params,
                    30000 // 默认30秒超时
            );

            long duration = System.currentTimeMillis() - startTime;

            // 4. 构建返回结果
            if (invokeResult.isSuccess()) {
                // 将结果转换为JSON字符串供LLM读取
                String content;
                try {
                    content = objectMapper.writeValueAsString(invokeResult.getParsedData());
                } catch (JsonProcessingException e) {
                    content = invokeResult.getRawBody();
                }

                AgentToolCallResult result = AgentToolCallResult.success(
                        toolCallId,
                        toolName,
                        content,
                        invokeResult.getParsedData());
                result.setDuration(duration);
                return result;
            } else {
                AgentToolCallResult result = AgentToolCallResult.failure(
                        toolCallId,
                        toolName,
                        invokeResult.getErrorMessage());
                result.setDuration(duration);
                return result;
            }

        } catch (Exception e) {
            logger.error("工具调用执行异常: toolName={}, error={}", toolName, e.getMessage(), e);
            AgentToolCallResult result = AgentToolCallResult.failure(
                    toolCallId,
                    toolName,
                    "执行异常: " + e.getMessage());
            result.setDuration(System.currentTimeMillis() - startTime);
            return result;
        }
    }

    /**
     * 批量执行工具调用（支持并行调用）
     */
    public List<AgentToolCallResult> executeToolCalls(List<ToolCallRequest> toolCalls) {
        // 简化处理：顺序执行
        // TODO: 可优化为并行执行
        List<AgentToolCallResult> results = new ArrayList<>();
        for (ToolCallRequest call : toolCalls) {
            results.add(executeToolCall(call.getId(), call.getName(), call.getArguments()));
        }
        return results;
    }

    /**
     * 工具调用请求
     */
    public static class ToolCallRequest {
        private String id;
        private String name;
        private String arguments;

        public ToolCallRequest() {
        }

        public ToolCallRequest(String id, String name, String arguments) {
            this.id = id;
            this.name = name;
            this.arguments = arguments;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArguments() {
            return arguments;
        }

        public void setArguments(String arguments) {
            this.arguments = arguments;
        }
    }

    /**
     * 生成工具名称
     */
    private String generateToolName(String pluginId, String operationId) {
        // 格式: plugin_{简化pluginId}_{operationId}
        // 例如: plugin_66fd8f29_getSensorData
        String shortPluginId = pluginId.replace("plugin_", "");
        if (shortPluginId.length() > 8) {
            shortPluginId = shortPluginId.substring(0, 8);
        }
        return "plugin_" + shortPluginId + "_" + operationId;
    }

    /**
     * 构建工具描述
     */
    private String buildToolDescription(String pluginName, PluginOperation operation) {
        StringBuilder desc = new StringBuilder();
        desc.append("[").append(pluginName).append("] ");
        if (operation.getDescription() != null && !operation.getDescription().isEmpty()) {
            desc.append(operation.getDescription());
        } else if (operation.getName() != null) {
            desc.append(operation.getName());
        }
        return desc.toString();
    }

    /**
     * 从工具名称解析映射
     */
    private ToolMapping parseToolNameToMapping(String toolName) {
        // 尝试解析格式: plugin_{pluginId}_{operationId}
        if (!toolName.startsWith("plugin_")) {
            return null;
        }

        // 这里需要更复杂的逻辑来反向查找pluginId
        // 简化处理：遍历缓存
        // 实际生产中应该建立反向索引

        return null;
    }

    /**
     * 解析参数JSON字符串
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseArguments(String arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            return objectMapper.readValue(arguments, Map.class);
        } catch (JsonProcessingException e) {
            logger.warn("参数解析失败: {}", arguments, e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 清除工具映射缓存
     */
    public void clearCache() {
        toolMappingCache.clear();
    }

    /**
     * 注册自定义工具（非插件来源）
     */
    public void registerCustomTool(String toolName, String pluginId, String operationId) {
        toolMappingCache.put(toolName, new ToolMapping(pluginId, operationId));
    }
}
