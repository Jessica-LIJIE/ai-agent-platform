package org.demo.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.mapper.AgentMapper;
import org.demo.core.mapper.LlmModelMapper;
import org.demo.core.mapper.LlmProviderMapper;
import org.demo.core.model.entity.Agent;
import org.demo.core.model.entity.LlmModel;
import org.demo.core.model.entity.LlmProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM服务类
 * 负责与大语言模型进行交互
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final LlmModelMapper llmModelMapper;
    private final LlmProviderMapper llmProviderMapper;
    private final AgentMapper agentMapper;

    /**
     * 调用大模型获取回答
     *
     * @param agentId 智能体ID
     * @param llmModelId LLM模型ID
     * @param userQuery 用户问题
     * @param conversationHistory 会话历史（可选）
     * @return 大模型的回答
     */
    public String chat(String agentId, String llmModelId, String userQuery, List<Map<String, String>> conversationHistory) {
        try {
            // 1. 查询Agent获取系统提示词
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

            // 5. 构建请求参数
            ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                    .model(llmModel.getName()); // 使用模型名称，如 "gpt-4"

            // 添加系统提示词
            if (agent.getPrompt() != null && !agent.getPrompt().isEmpty()) {
                paramsBuilder.addSystemMessage(agent.getPrompt());
            }

            // 添加历史对话（如果有）
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (Map<String, String> msg : conversationHistory) {
                    String role = msg.get("role");
                    String content = msg.get("content");
                    if ("user".equals(role)) {
                        paramsBuilder.addUserMessage(content);
                    } else if ("assistant".equals(role)) {
                        paramsBuilder.addAssistantMessage(content);
                    }
                }
            }

            // 添加当前用户问题
            paramsBuilder.addUserMessage(userQuery);

            // 设置温度等参数（如果有配置）
            if (llmModel.getTemperature() != null) {
                paramsBuilder.temperature(llmModel.getTemperature().doubleValue());
            }
            if (llmModel.getMaxTokens() != null) {
                paramsBuilder.maxCompletionTokens(llmModel.getMaxTokens());
            }

            // 6. 调用大模型API
            ChatCompletionCreateParams params = paramsBuilder.build();
            ChatCompletion completion = client.chat().completions().create(params);

            // 7. 提取回答
            String answer = completion.choices().stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .collect(Collectors.joining());

            log.info("LLM调用成功 - 模型: {}, 输入Token: {}, 输出Token: {}", 
                    llmModel.getName(),
                    completion.usage().map(u -> u.promptTokens()).orElse(null),
                    completion.usage().map(u -> u.completionTokens()).orElse(null));

            return answer;

        } catch (Exception e) {
            log.error("调用LLM失败", e);
            throw new RuntimeException("调用大模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 简化版chat方法，不需要传入历史记录
     */
    public String chat(String agentId, String llmModelId, String userQuery) {
        return chat(agentId, llmModelId, userQuery, null);
    }
}
