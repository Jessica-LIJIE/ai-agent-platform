package org.demo.core.agent;

/**
 * Agent工具调用结果
 * 用于封装工具执行的结果，供LLM处理
 */
public class AgentToolCallResult {

    /**
     * 工具调用ID（对应LLM返回的tool_call_id）
     */
    private String toolCallId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 是否调用成功
     */
    private boolean success;

    /**
     * 执行结果内容（JSON字符串，供LLM读取）
     */
    private String content;

    /**
     * 原始结果数据
     */
    private Object rawData;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private long duration;

    /**
     * 创建成功结果
     */
    public static AgentToolCallResult success(String toolCallId, String toolName, String content, Object rawData) {
        AgentToolCallResult result = new AgentToolCallResult();
        result.toolCallId = toolCallId;
        result.toolName = toolName;
        result.success = true;
        result.content = content;
        result.rawData = rawData;
        return result;
    }

    /**
     * 创建失败结果
     */
    public static AgentToolCallResult failure(String toolCallId, String toolName, String errorMessage) {
        AgentToolCallResult result = new AgentToolCallResult();
        result.toolCallId = toolCallId;
        result.toolName = toolName;
        result.success = false;
        result.errorMessage = errorMessage;
        result.content = "{\"error\": \"" + escapeJson(errorMessage) + "\"}";
        return result;
    }

    /**
     * JSON字符串转义
     */
    private static String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // =============== Getters and Setters ===============

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Object getRawData() {
        return rawData;
    }

    public void setRawData(Object rawData) {
        this.rawData = rawData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
