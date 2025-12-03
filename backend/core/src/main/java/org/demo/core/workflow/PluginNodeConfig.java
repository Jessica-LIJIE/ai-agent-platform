package org.demo.core.workflow;

import java.util.Map;

/**
 * 工作流插件节点配置
 * 用于工作流definition中定义Plugin类型节点
 * 
 * 示例JSON配置:
 * {
 * "nodeId": "node_plugin_001",
 * "nodeType": "plugin",
 * "pluginConfig": {
 * "pluginId": "plugin_66fd8f296525",
 * "operationId": "controlDevice",
 * "paramMappings": {
 * "device_uuid": "${context.deviceId}",
 * "port_type": "led",
 * "port_id": "${input.portId}",
 * "action": "${input.action}"
 * },
 * "outputMapping": "pluginResult"
 * }
 * }
 */
public class PluginNodeConfig {

    /**
     * 节点ID（工作流中唯一标识）
     */
    private String nodeId;

    /**
     * 插件ID
     */
    private String pluginId;

    /**
     * 操作ID（对应plugin_operation表的operation_id）
     */
    private String operationId;

    /**
     * 参数映射规则
     * key: 插件参数名
     * value: 参数来源表达式，支持:
     * - 字面量: "led", 1, true
     * - 上下文变量: "${context.xxx}"
     * - 输入参数: "${input.xxx}"
     * - 上一节点输出: "${nodes.nodeId.outputField}"
     */
    private Map<String, Object> paramMappings;

    /**
     * 输出结果在上下文中的变量名
     * 执行结果会存储到 context[outputMapping] 中供后续节点使用
     */
    private String outputMapping;

    /**
     * 超时时间（毫秒），默认30秒
     */
    private Integer timeout;

    /**
     * 失败时是否继续执行后续节点
     */
    private Boolean continueOnError;

    /**
     * 重试次数（默认0不重试）
     */
    private Integer retryCount;

    /**
     * 重试间隔（毫秒）
     */
    private Integer retryInterval;

    // =============== Getters and Setters ===============

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public Map<String, Object> getParamMappings() {
        return paramMappings;
    }

    public void setParamMappings(Map<String, Object> paramMappings) {
        this.paramMappings = paramMappings;
    }

    public String getOutputMapping() {
        return outputMapping;
    }

    public void setOutputMapping(String outputMapping) {
        this.outputMapping = outputMapping;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(Boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    /**
     * 便捷方法：是否在错误时继续执行
     */
    public boolean isContinueOnError() {
        return Boolean.TRUE.equals(continueOnError);
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }
}
