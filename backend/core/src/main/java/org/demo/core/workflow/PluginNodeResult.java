package org.demo.core.workflow;

import java.time.LocalDateTime;

/**
 * 插件节点执行结果
 */
public class PluginNodeResult {

    /**
     * 是否执行成功
     */
    private boolean success;

    /**
     * 执行结果数据
     */
    private Object data;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * HTTP状态码（如果调用了HTTP接口）
     */
    private Integer httpStatus;

    /**
     * 执行耗时（毫秒）
     */
    private long duration;

    /**
     * 执行开始时间
     */
    private LocalDateTime startTime;

    /**
     * 执行结束时间
     */
    private LocalDateTime endTime;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 创建成功结果
     */
    public static PluginNodeResult success(Object data) {
        PluginNodeResult result = new PluginNodeResult();
        result.success = true;
        result.data = data;
        return result;
    }

    /**
     * 创建失败结果
     */
    public static PluginNodeResult failure(String errorCode, String errorMessage) {
        PluginNodeResult result = new PluginNodeResult();
        result.success = false;
        result.errorCode = errorCode;
        result.errorMessage = errorMessage;
        return result;
    }

    /**
     * 创建超时失败结果
     */
    public static PluginNodeResult timeout(String message) {
        return failure("TIMEOUT", message);
    }

    /**
     * 创建网络错误结果
     */
    public static PluginNodeResult networkError(String message) {
        return failure("NETWORK_ERROR", message);
    }

    /**
     * 设置计时信息
     */
    public PluginNodeResult withTiming(LocalDateTime startTime, LocalDateTime endTime, long duration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        return this;
    }

    // =============== Getters and Setters ===============

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
