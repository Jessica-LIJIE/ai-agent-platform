package org.demo.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.api.ApiResponse;
import org.demo.core.mapper.AgentConversationMapper;
import org.demo.core.model.entity.AgentConversation;
import org.demo.core.model.vo.PluginInvokeResult;
import org.demo.core.service.FunctionCallingService;
import org.demo.core.service.LlmService;
import org.demo.core.service.PluginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天控制器
 * 提供会话管理和消息发送接口
 */
@Slf4j
@Tag(name = "聊天管理", description = "提供创建会话、查看消息历史、发送消息等聊天功能接口")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AgentConversationMapper agentConversationMapper;
    private final LlmService llmService;
    private final FunctionCallingService functionCallingService;
    private final PluginService pluginService;
    private final ObjectMapper objectMapper;

    /**
     * 创建会话
     *
     * @return 包含session_id的响应
     */
    @Operation(summary = "创建会话", description = "创建一个新的聊天会话，系统会自动生成唯一的session_id用于标识这个会话。后续的消息发送和历史查询都需要使用这个session_id。每个会话可以包含多轮对话")
    @PostMapping("/session")
    public ApiResponse<Map<String, String>> createSession() {
        String sessionId = UUID.randomUUID().toString();
        Map<String, String> result = new HashMap<>();
        result.put("session_id", sessionId);
        return ApiResponse.ok("会话创建成功", result);
    }

    /**
     * 查看消息历史
     *
     * @param sessionId 会话ID
     * @return 该会话的所有消息历史
     */
    @Operation(summary = "查看消息历史", description = "根据session_id查询指定会话的所有消息历史记录。返回该会话中的所有对话，包括用户提问、智能体回答、消息元数据等完整信息。消息按创建时间正序排列，便于查看对话流程")
    @GetMapping("/history/{sessionId}")
    public ApiResponse<List<AgentConversation>> getMessageHistory(
            @Parameter(description = "会话的唯一标识符session_id，用于查询特定会话的消息历史", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable String sessionId) {
        QueryWrapper<AgentConversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId)
                .orderByAsc("create_time");
        List<AgentConversation> conversations = agentConversationMapper.selectList(queryWrapper);
        return ApiResponse.ok(conversations);
    }

    /**
     * 发送消息
     *
     * @param conversation 包含session_id、agent_id、user_id、query等信息的对话对象
     * @return 创建的对话记录
     */
    @Operation(summary = "发送消息", description = "用户在指定会话中发送一条消息给智能体。需要提供session_id（会话ID）、agent_id（智能体ID）、user_id（用户ID）和query（用户提问内容）。系统会保存这条消息记录并返回完整的对话信息，包括自动生成的消息ID和创建时间。注意：此接口仅保存用户消息，实际的智能体回答需要通过其他接口获取")
    @PostMapping("/message")
    public ApiResponse<AgentConversation> sendMessage(
            @Parameter(description = "对话信息对象，需包含session_id、agent_id、user_id和query字段。其中query为用户的提问内容", required = true) @RequestBody AgentConversation conversation) {
        // 验证必填字段
        if (conversation.getSessionId() == null || conversation.getSessionId().isEmpty()) {
            return ApiResponse.fail("session_id不能为空");
        }
        if (conversation.getAgentId() == null || conversation.getAgentId().isEmpty()) {
            return ApiResponse.fail("agent_id不能为空");
        }
        if (conversation.getQuery() == null || conversation.getQuery().isEmpty()) {
            return ApiResponse.fail("query不能为空");
        }

        // 获取llmModelId
        String llmModelId = null;
        if (conversation.getMetadata() != null && conversation.getMetadata().getLlmModelId() != null) {
            llmModelId = conversation.getMetadata().getLlmModelId();
        }

        if (llmModelId == null || llmModelId.isEmpty()) {
            return ApiResponse.fail("metadata.llmModelId不能为空");
        }

        // 暂时写死 userId
        // 确保 userId 有值（如果前端没传，使用默认测试用户）
        if (conversation.getUserId() == null || conversation.getUserId().isEmpty()) {
            conversation.setUserId("user-003-tester");
        }

        try {
            // 检查是否有手动指定的插件调用（兼容旧的前端调用方式）
            String answer;
            if (conversation.getPluginCall() != null) {
                // 旧方式：手动指定插件调用
                AgentConversation.PluginCall pc = conversation.getPluginCall();
                PluginInvokeResult invokeResult = pluginService.invokeOperation(
                        pc.getPluginId(),
                        pc.getOperationId(),
                        pc.getParams(),
                        null);

                String toolSummary;
                if (invokeResult == null) {
                    toolSummary = "插件调用返回空结果";
                } else if (invokeResult.isSuccess()) {
                    Object parsed = invokeResult.getParsedData();
                    String parsedStr;
                    try {
                        parsedStr = objectMapper.writeValueAsString(parsed);
                    } catch (Exception ex) {
                        parsedStr = invokeResult.getRawBody();
                    }
                    toolSummary = "插件调用成功，结果: " + parsedStr;
                } else if (invokeResult.isTimeout()) {
                    toolSummary = "插件调用超时";
                } else {
                    toolSummary = "插件调用失败: " + invokeResult.getErrorMessage();
                }

                // 将插件结果拼接到用户问题前，作为上下文信息交给LLM
                String userQueryForLlm = toolSummary + "\n原始问题: " + conversation.getQuery();
                answer = llmService.chat(
                        conversation.getAgentId(),
                        llmModelId,
                        userQueryForLlm);
            } else {
                // 新方式：使用 Function Calling，让 LLM 自动决定是否调用插件
                // 获取对话历史，用于提取之前使用过的设备 UUID
                List<Map<String, String>> conversationHistory = new java.util.ArrayList<>();
                try {
                    QueryWrapper<AgentConversation> historyQuery = new QueryWrapper<>();
                    historyQuery.eq("session_id", conversation.getSessionId())
                            .orderByAsc("create_time");
                    List<AgentConversation> historyList = agentConversationMapper.selectList(historyQuery);
                    for (AgentConversation hist : historyList) {
                        // 添加用户消息
                        if (hist.getQuery() != null) {
                            Map<String, String> userMsg = new HashMap<>();
                            userMsg.put("role", "user");
                            userMsg.put("content", hist.getQuery());
                            conversationHistory.add(userMsg);
                        }
                        // 添加助手回答
                        if (hist.getAnswer() != null) {
                            Map<String, String> assistantMsg = new HashMap<>();
                            assistantMsg.put("role", "assistant");
                            assistantMsg.put("content", hist.getAnswer());
                            conversationHistory.add(assistantMsg);
                        }
                    }
                } catch (Exception ex) {
                    // 如果查询历史失败，继续处理当前请求
                    log.warn("获取对话历史失败: {}", ex.getMessage());
                }

                answer = functionCallingService.chatWithFunctions(
                        conversation.getAgentId(),
                        llmModelId,
                        conversation.getQuery(),
                        conversation.getSessionId(),
                        conversationHistory);
            }

            // 设置回答
            conversation.setAnswer(answer);

            // 插入对话记录
            int rows = agentConversationMapper.insert(conversation);
            if (rows > 0) {
                return ApiResponse.ok("消息发送成功", conversation);
            }
            return ApiResponse.fail("消息发送失败");

        } catch (Exception e) {
            return ApiResponse.fail("调用大模型失败: " + e.getMessage());
        }
    }
}
