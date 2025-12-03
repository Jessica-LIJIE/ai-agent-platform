package org.demo.core.model.vo;

/**
 * 插件调用结果VO
 */
public class PluginInvokeResult {

    /**
     * 调用状态：success, error, timeout
     */
    private String status;

    /**
     * HTTP状态码
     */
    private Integer httpStatusCode;

    /**
     * 原始响应体
     */
    private String rawBody;

    /**
     * 解析后的JSON数据
     */
    private Object parsedData;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 请求耗时（毫秒）
     */
    private Long duration;

    /**
     * 请求的完整URL（调试用）
     */
    private String requestUrl;

    /**
     * 请求的HTTP方法
     */
    private String requestMethod;

    // =============== 静态工厂方法 ===============

    public static PluginInvokeResult success(int httpStatusCode, String rawBody, Object parsedData, long duration) {
        PluginInvokeResult result = new PluginInvokeResult();
        result.setStatus("success");
        result.setHttpStatusCode(httpStatusCode);
        result.setRawBody(rawBody);
        result.setParsedData(parsedData);
        result.setDuration(duration);
        return result;
    }

    public static PluginInvokeResult error(String errorMessage, long duration) {
        PluginInvokeResult result = new PluginInvokeResult();
        result.setStatus("error");
        result.setErrorMessage(errorMessage);
        result.setDuration(duration);
        return result;
    }

    public static PluginInvokeResult timeout(String requestUrl, long duration) {
        PluginInvokeResult result = new PluginInvokeResult();
        result.setStatus("timeout");
        result.setRequestUrl(requestUrl);
        result.setErrorMessage("请求超时");
        result.setDuration(duration);
        return result;
    }

    // =============== Getters and Setters ===============

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getRawBody() {
        return rawBody;
    }

    public void setRawBody(String rawBody) {
        this.rawBody = rawBody;
    }

    public Object getParsedData() {
        return parsedData;
    }

    public void setParsedData(Object parsedData) {
        this.parsedData = parsedData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    // =============== 便捷方法 ===============

    /**
     * 判断是否调用成功
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }

    /**
     * 判断是否超时
     */
    public boolean isTimeout() {
        return "timeout".equals(status);
    }
}
