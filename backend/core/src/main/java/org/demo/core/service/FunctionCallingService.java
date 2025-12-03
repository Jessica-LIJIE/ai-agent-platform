package org.demo.core.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.mapper.AgentMapper;
import org.demo.core.mapper.LlmModelMapper;
import org.demo.core.mapper.LlmProviderMapper;
import org.demo.core.mapper.PluginMapper;
import org.demo.core.mapper.PluginOperationMapper;
import org.demo.core.model.entity.*;
import org.demo.core.model.vo.PluginInvokeResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Function Calling 服务
 * 支持 LLM 自动调用插件
 * 
 * 工作流程：
 * 1. 构建 tools 列表（从智能体绑定的插件）
 * 2. 第一次调用 LLM，带上 tools 参数
 * 3. 如果 LLM 返回 tool_calls，执行插件调用
 * 4. 将工具结果作为 tool message 发给 LLM
 * 5. LLM 总结工具结果，生成最终回答
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FunctionCallingService {

    private final LlmModelMapper llmModelMapper;
    private final LlmProviderMapper llmProviderMapper;
    private final AgentMapper agentMapper;
    private final PluginMapper pluginMapper;
    private final PluginOperationMapper pluginOperationMapper;
    private final PluginService pluginService;
    private final ObjectMapper objectMapper;

    /**
     * 会话级别的设备 UUID 缓存
     * key: sessionId, value: 最近使用的设备 UUID
     */
    private final Map<String, String> sessionDeviceUuidCache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 从历史对话中提取设备 UUID 并缓存
     * 扫描 user 和 assistant 消息，提取最新的 UUID
     */
    private void extractAndCacheUuidFromHistory(String sessionId, List<Map<String, String>> conversationHistory) {
        if (sessionId == null || conversationHistory == null) {
            return;
        }

        // 从历史对话中查找 UUID（从最新到最老）
        for (int i = conversationHistory.size() - 1; i >= 0; i--) {
            Map<String, String> msg = conversationHistory.get(i);
            String content = msg.get("content");
            if (content != null && !content.isEmpty()) {
                String uuid = extractUuidFromQuery(content);
                if (uuid != null) {
                    log.info("从历史对话中提取到设备UUID: {} (sessionId: {})", uuid, sessionId);
                    sessionDeviceUuidCache.put(sessionId, uuid);
                    return;
                }
            }
        }

        log.info("历史对话中未找到设备UUID (sessionId: {})", sessionId);
    }

    /**
     * 从缓存中获取会话的设备 UUID
     */
    private String getCachedUuid(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        String uuid = sessionDeviceUuidCache.get(sessionId);
        if (uuid != null) {
            log.info("从缓存获取到设备UUID: {} (sessionId: {})", uuid, sessionId);
        }
        return uuid;
    }

    /**
     * 支持 Function Calling 的聊天方法（带会话上下文）
     * 
     * @param agentId             智能体ID
     * @param llmModelId          LLM模型ID
     * @param userQuery           用户问题
     * @param sessionId           会话ID（用于缓存设备UUID）
     * @param conversationHistory 历史对话（可选，用于提取之前的设备UUID）
     * @return 大模型的回答（可能包含工具调用结果）
     */
    public String chatWithFunctions(String agentId, String llmModelId, String userQuery,
            String sessionId, List<Map<String, String>> conversationHistory) {
        // 尝试从历史对话中提取设备 UUID 并缓存
        if (sessionId != null && conversationHistory != null && !conversationHistory.isEmpty()) {
            extractAndCacheUuidFromHistory(sessionId, conversationHistory);
        }

        return chatWithFunctionsInternal(agentId, llmModelId, userQuery, sessionId);
    }

    /**
     * 支持 Function Calling 的聊天方法
     * 
     * @param agentId    智能体ID
     * @param llmModelId LLM模型ID
     * @param userQuery  用户问题
     * @return 大模型的回答（可能包含工具调用结果）
     */
    public String chatWithFunctions(String agentId, String llmModelId, String userQuery) {
        return chatWithFunctionsInternal(agentId, llmModelId, userQuery, null);
    }

    /**
     * 内部聊天方法
     */
    private String chatWithFunctionsInternal(String agentId, String llmModelId, String userQuery, String sessionId) {
        try {
            // 1. 查询Agent获取系统提示词和绑定的插件
            Agent agent = agentMapper.selectById(agentId);
            if (agent == null) {
                throw new RuntimeException("Agent不存在: " + agentId);
            }

            // 2. 查询LlmModel信息
            LlmModel llmModel = llmModelMapper.selectById(llmModelId);
            if (llmModel == null) {
                throw new RuntimeException("LLM模型不存在: " + llmModelId);
            }

            // 3. 查询Provider信息
            QueryWrapper<LlmProvider> providerQuery = new QueryWrapper<>();
            providerQuery.eq("code", llmModel.getProvider());
            LlmProvider provider = llmProviderMapper.selectOne(providerQuery);
            if (provider == null) {
                throw new RuntimeException("Provider不存在: " + llmModel.getProvider());
            }

            // 4. 构建OpenAI客户端
            String apiKey = llmModel.getApiKey();
            String baseUrl = llmModel.getApiBase() != null ? llmModel.getApiBase() : provider.getDefaultApiBase();

            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("API Key未配置");
            }

            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .build();

            // 5. 获取智能体绑定的插件操作，构建 tools JSON
            List<Map<String, Object>> tools = buildToolsFromAgent(agent);

            // 6. 智能检测是否需要调用工具（基于用户问题和可用工具）
            // 先尝试自动匹配工具并执行，传入 sessionId 以支持从缓存获取 UUID
            String autoToolResult = tryAutoToolCall(userQuery, tools, sessionId);

            // 检查是否需要用户提供 UUID
            if ("NEED_UUID".equals(autoToolResult)) {
                log.info("需要用户提供设备UUID");
                return "我检测到您想要查询设备信息，但我需要知道您的设备UUID才能获取数据。\n\n" +
                        "请提供您的设备UUID，格式类似：`1fcb3c12-63eb-4a67-9f85-293e24bf367c`\n\n" +
                        "您可以这样告诉我：\n" +
                        "- \"设备UUID是 xxx，查询温度\"\n" +
                        "- \"uuid: xxx 现在温度多少\"\n" +
                        "- 或直接发送包含UUID的消息";
            }

            if (autoToolResult != null) {
                // 有工具结果，调用 LLM 生成基于真实数据的回答
                ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                        .model(llmModel.getName());

                if (agent.getPrompt() != null && !agent.getPrompt().isEmpty()) {
                    paramsBuilder.addSystemMessage(agent.getPrompt());
                }

                String contextWithToolResult = String.format(
                        "用户问题：%s\n\n我已经通过物联网设备获取了真实数据：%s\n\n请根据这些真实数据，用自然、友好的语言回答用户的问题。不要编造数据，直接使用上面提供的真实数据。",
                        userQuery, autoToolResult);
                paramsBuilder.addUserMessage(contextWithToolResult);

                if (llmModel.getTemperature() != null) {
                    paramsBuilder.temperature(llmModel.getTemperature().doubleValue());
                }
                if (llmModel.getMaxTokens() != null) {
                    paramsBuilder.maxCompletionTokens(llmModel.getMaxTokens());
                }

                ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());

                String finalAnswer = completion.choices().stream()
                        .flatMap(choice -> choice.message().content().stream())
                        .collect(Collectors.joining());

                log.info("Function Calling 完成（智能检测方式）- 工具结果: {}", autoToolResult);
                return finalAnswer;
            }

            // 7. 没有匹配到工具，正常调用 LLM
            ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                    .model(llmModel.getName());

            // 添加系统提示词
            if (agent.getPrompt() != null && !agent.getPrompt().isEmpty()) {
                paramsBuilder.addSystemMessage(agent.getPrompt());
            }

            // 添加用户问题
            paramsBuilder.addUserMessage(userQuery);

            // 设置温度等参数
            if (llmModel.getTemperature() != null) {
                paramsBuilder.temperature(llmModel.getTemperature().doubleValue());
            }
            if (llmModel.getMaxTokens() != null) {
                paramsBuilder.maxCompletionTokens(llmModel.getMaxTokens());
            }

            ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());

            // 提取回答
            String answer = completion.choices().stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .collect(Collectors.joining());

            log.info("LLM调用成功（无工具调用） - 模型: {}", llmModel.getName());

            return answer;

        } catch (Exception e) {
            log.error("Function Calling 失败", e);
            throw new RuntimeException("调用大模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 智能检测并执行工具调用
     * 基于用户问题关键词匹配可用工具
     * 
     * @param userQuery 用户问题
     * @param tools     可用工具列表
     * @param sessionId 会话ID（用于获取缓存的UUID）
     * @return null: 未匹配到工具; "NEED_UUID": 需要用户提供设备UUID; 其他字符串: 工具调用结果
     */
    private String tryAutoToolCall(String userQuery, List<Map<String, Object>> tools, String sessionId) {
        if (tools.isEmpty()) {
            log.info("没有可用工具");
            return null;
        }

        String queryLower = userQuery.toLowerCase();

        // 关键词到操作的映射
        Map<String, String> keywordToOperation = new LinkedHashMap<>();
        // 传感器相关
        keywordToOperation.put("温度", "getSensorData");
        keywordToOperation.put("湿度", "getSensorData");
        keywordToOperation.put("传感器", "getSensorData");
        keywordToOperation.put("环境", "getSensorData");
        keywordToOperation.put("检测", "getSensorData");
        keywordToOperation.put("测量", "getSensorData");
        keywordToOperation.put("多少度", "getSensorData");
        keywordToOperation.put("气温", "getSensorData");
        // LED相关 - lorem 插件使用 controlDevice 操作
        keywordToOperation.put("led", "controlDevice");
        keywordToOperation.put("灯", "controlDevice");
        keywordToOperation.put("开灯", "controlDevice");
        keywordToOperation.put("关灯", "controlDevice");
        keywordToOperation.put("亮", "controlDevice");
        keywordToOperation.put("灭", "controlDevice");
        // 预设相关 - lorem 插件使用 executePreset 操作
        keywordToOperation.put("预设", "executePreset");
        keywordToOperation.put("preset", "executePreset");
        keywordToOperation.put("模式", "executePreset");

        // 查找匹配的工具
        for (Map.Entry<String, String> entry : keywordToOperation.entrySet()) {
            if (queryLower.contains(entry.getKey())) {
                String targetOperation = entry.getValue();

                // 在工具列表中查找匹配的工具
                for (Map<String, Object> tool : tools) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> function = (Map<String, Object>) tool.get("function");
                    String functionName = (String) function.get("name");

                    if (functionName.contains(targetOperation)) {
                        log.info("智能匹配到工具: {} (关键词: {})", functionName, entry.getKey());

                        // 构建参数，传入 sessionId 以便从缓存获取 UUID
                        Map<String, Object> params = buildParams(targetOperation, queryLower, sessionId);

                        // 检查是否缺少 UUID（如果从当前问题和缓存都找不到，才提示用户）
                        if (isMissingUuid(params, targetOperation)) {
                            log.info("缺少设备UUID，需要用户提供");
                            return "NEED_UUID";
                        }

                        try {
                            String argsJson = objectMapper.writeValueAsString(params);
                            String result = executeToolCall(functionName, argsJson);

                            // 如果调用成功，返回结果
                            if (result != null && !result.startsWith("错误") && !result.startsWith("插件调用失败")) {
                                return result;
                            }
                        } catch (JsonProcessingException e) {
                            log.error("序列化参数失败", e);
                        }
                    }
                }
            }
        }

        log.info("未匹配到任何工具");
        return null;
    }

    /**
     * 构建工具调用参数
     * 
     * 注意：lorem 插件的 getSensorData API 参数格式：
     * - uuid: 设备UUID
     * - sensor: 传感器名称，可选值：DHT11_temperature, DHT11_humidity, DS18B20_temperature
     * 
     * UUID 获取顺序：
     * 1. 首先从用户当前问题中提取
     * 2. 如果没有，从会话缓存中获取（之前对话中使用过的 UUID）
     * 3. 如果都没有，返回 null 让调用方知道需要提示用户
     * 
     * @param operation  操作类型
     * @param queryLower 用户问题（小写）
     * @param sessionId  会话ID（用于获取缓存的UUID）
     * @return 参数Map
     */
    private Map<String, Object> buildParams(String operation, String queryLower, String sessionId) {
        Map<String, Object> params = new HashMap<>();

        // 尝试从用户问题中提取 UUID
        String uuid = extractUuidFromQuery(queryLower);

        // 如果当前问题中没有 UUID，尝试从会话缓存获取
        if (uuid == null && sessionId != null) {
            uuid = getCachedUuid(sessionId);
            if (uuid != null) {
                log.info("使用会话缓存的UUID: {} (sessionId: {})", uuid, sessionId);
            }
        }

        // 如果找到了 UUID，同时更新缓存（确保最新使用的 UUID 被记录）
        if (uuid != null && sessionId != null) {
            sessionDeviceUuidCache.put(sessionId, uuid);
        }

        if (uuid != null) {
            params.put("uuid", uuid);
        }
        // 如果没有 UUID，params 中不包含 uuid 字段，后续会检测并提示用户

        if ("getSensorData".equals(operation)) {
            // 根据问题确定传感器类型
            // API 参数名是 "sensor"，值是 DHT11_temperature, DHT11_humidity, DS18B20_temperature
            if (queryLower.contains("湿度")) {
                params.put("sensor", "DHT11_humidity");
            } else {
                params.put("sensor", "DHT11_temperature");
            }
        } else if ("controlDevice".equals(operation)) {
            // lorem 插件的设备控制参数
            // device_uuid, port_type, port_id, action, value(可选)
            if (uuid != null) {
                params.put("device_uuid", uuid);
            }
            params.put("port_type", "led");

            // 确定端口
            if (queryLower.contains("led2") || queryLower.contains("2号") || queryLower.contains("二号")) {
                params.put("port_id", 2);
            } else {
                params.put("port_id", 1);
            }
            // 确定状态
            if (queryLower.contains("关") || queryLower.contains("灭") || queryLower.contains("off")) {
                params.put("action", "off");
            } else {
                params.put("action", "on");
            }
            // 移除 uuid，因为这个操作用的是 device_uuid
            params.remove("uuid");
        } else if ("executePreset".equals(operation)) {
            // lorem 插件的预设执行参数
            if (uuid != null) {
                params.put("device_uuid", uuid);
            }
            if (queryLower.contains("工作")) {
                params.put("preset_name", "work");
            } else if (queryLower.contains("休息") || queryLower.contains("睡眠")) {
                params.put("preset_name", "rest");
            } else {
                params.put("preset_name", "default");
            }
            // 移除 uuid，因为这个操作用的是 device_uuid
            params.remove("uuid");
        }

        return params;
    }

    /**
     * 从用户问题中提取 UUID
     * 支持多种格式：
     * - 标准 UUID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     * - 简化 UUID: 32个连续的十六进制字符
     * - 关键词后面的 UUID: "设备xxx" "uuid:xxx" "uuid是xxx" "uuid=xxx"
     */
    private String extractUuidFromQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        // 标准 UUID 格式（带连字符）
        java.util.regex.Pattern standardUuidPattern = java.util.regex.Pattern.compile(
                "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
        java.util.regex.Matcher standardMatcher = standardUuidPattern.matcher(query);
        if (standardMatcher.find()) {
            String uuid = standardMatcher.group();
            log.info("从用户问题中提取到标准UUID: {}", uuid);
            return uuid;
        }

        // 简化 UUID 格式（32个连续十六进制字符）
        java.util.regex.Pattern simpleUuidPattern = java.util.regex.Pattern.compile(
                "(?<![0-9a-fA-F])[0-9a-fA-F]{32}(?![0-9a-fA-F])");
        java.util.regex.Matcher simpleMatcher = simpleUuidPattern.matcher(query);
        if (simpleMatcher.find()) {
            String uuid = simpleMatcher.group();
            // 转换为标准格式
            String standardUuid = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-"
                    + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
            log.info("从用户问题中提取到简化UUID: {} -> {}", uuid, standardUuid);
            return standardUuid;
        }

        log.info("用户问题中未找到设备UUID");
        return null;
    }

    /**
     * 检查参数中是否缺少必要的 UUID
     */
    private boolean isMissingUuid(Map<String, Object> params, String operation) {
        if ("getSensorData".equals(operation)) {
            return !params.containsKey("uuid") || params.get("uuid") == null;
        } else if ("controlDevice".equals(operation) || "executePreset".equals(operation)) {
            return !params.containsKey("device_uuid") || params.get("device_uuid") == null;
        }
        return false;
    }

    /**
     * 根据智能体绑定的插件构建 tools 列表
     */
    private List<Map<String, Object>> buildToolsFromAgent(Agent agent) {
        List<Map<String, Object>> tools = new ArrayList<>();

        List<String> pluginIds = agent.getToolsConfig();
        if (pluginIds == null || pluginIds.isEmpty()) {
            log.info("智能体没有绑定插件");
            return tools;
        }

        log.info("智能体绑定了 {} 个插件: {}", pluginIds.size(), pluginIds);

        for (String pluginId : pluginIds) {
            try {
                // 查询插件
                Plugin plugin = pluginMapper.selectById(pluginId);
                if (plugin == null) {
                    log.warn("插件不存在: {}", pluginId);
                    continue;
                }
                if (!Boolean.TRUE.equals(plugin.getIsEnabled())) {
                    log.warn("插件已禁用: {}", pluginId);
                    continue;
                }

                // 查询插件的所有操作
                LambdaQueryWrapper<PluginOperation> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(PluginOperation::getPluginId, pluginId);
                List<PluginOperation> operations = pluginOperationMapper.selectList(queryWrapper);

                log.info("插件 {} 有 {} 个操作", plugin.getName(), operations.size());

                for (PluginOperation op : operations) {
                    Map<String, Object> tool = buildToolFromOperation(plugin, op);
                    if (tool != null) {
                        tools.add(tool);
                        log.info("添加工具: {}", tool.get("function"));
                    }
                }
            } catch (Exception e) {
                log.warn("构建插件工具失败: pluginId={}, error={}", pluginId, e.getMessage());
            }
        }

        log.info("为智能体构建了 {} 个工具", tools.size());
        return tools;
    }

    /**
     * 从插件操作构建单个 tool（JSON Map 格式）
     */
    private Map<String, Object> buildToolFromOperation(Plugin plugin, PluginOperation op) {
        try {
            // 构建函数名：pluginId__operationId（使用双下划线分隔，便于后续解析）
            String functionName = plugin.getId() + "__" + op.getOperationId();

            // 构建函数描述
            String description = String.format("[%s] %s", plugin.getName(),
                    op.getDescription() != null ? op.getDescription() : op.getName());

            // 构建参数 schema
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("type", "object");

            Map<String, Object> inputSchema = op.getInputSchema();
            if (inputSchema != null && inputSchema.containsKey("properties")) {
                parameters.put("properties", inputSchema.get("properties"));

                if (inputSchema.containsKey("required")) {
                    parameters.put("required", inputSchema.get("required"));
                }
            } else {
                parameters.put("properties", new HashMap<>());
            }

            // 构建 function 对象
            Map<String, Object> function = new LinkedHashMap<>();
            function.put("name", functionName);
            function.put("description", description);
            function.put("parameters", parameters);

            // 构建 tool 对象
            Map<String, Object> tool = new LinkedHashMap<>();
            tool.put("type", "function");
            tool.put("function", function);

            return tool;

        } catch (Exception e) {
            log.warn("构建工具定义失败: operationId={}, error={}", op.getOperationId(), e.getMessage());
            return null;
        }
    }

    /**
     * 执行工具调用
     * 
     * @param functionName 函数名（格式：pluginId__operationId）
     * @param arguments    参数JSON字符串
     * @return 调用结果
     */
    private String executeToolCall(String functionName, String arguments) {
        try {
            // 解析函数名
            String[] parts = functionName.split("__");
            if (parts.length != 2) {
                return "错误：无效的函数名格式";
            }

            String pluginId = parts[0];
            String operationId = parts[1];

            // 解析参数
            Map<String, Object> params = new HashMap<>();
            if (arguments != null && !arguments.isEmpty() && !arguments.equals("{}")) {
                params = objectMapper.readValue(arguments, new TypeReference<Map<String, Object>>() {
                });
            }

            log.info("执行插件调用: pluginId={}, operationId={}, params={}", pluginId, operationId, params);

            // 调用插件
            PluginInvokeResult result = pluginService.invokeOperation(pluginId, operationId, params, 30000);

            if (result == null) {
                return "插件调用返回空结果";
            }

            if (result.isSuccess()) {
                // 返回解析后的数据
                Object parsedData = result.getParsedData();
                if (parsedData != null) {
                    String jsonResult = objectMapper.writeValueAsString(parsedData);
                    log.info("插件调用成功，结果: {}", jsonResult);
                    return jsonResult;
                }
                log.info("插件调用成功，原始结果: {}", result.getRawBody());
                return result.getRawBody();
            } else if (result.isTimeout()) {
                log.warn("插件调用超时");
                return "插件调用超时";
            } else {
                log.error("插件调用失败: {}", result.getErrorMessage());
                return "插件调用失败: " + result.getErrorMessage();
            }

        } catch (JsonProcessingException e) {
            log.error("解析工具调用参数失败", e);
            return "参数解析失败: " + e.getMessage();
        } catch (Exception e) {
            log.error("执行工具调用失败", e);
            return "工具调用失败: " + e.getMessage();
        }
    }
}
