package org.demo.core.workflow;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流执行上下文
 * 在工作流执行过程中传递数据
 */
public class WorkflowContext {

    /**
     * 工作流运行ID
     */
    private String runId;

    /**
     * 工作流ID
     */
    private String workflowId;

    /**
     * 当前用户ID
     */
    private String userId;

    /**
     * 工作流输入参数
     */
    private Map<String, Object> input;

    /**
     * 全局上下文变量（可跨节点共享）
     */
    private Map<String, Object> variables;

    /**
     * 各节点的执行结果
     * key: nodeId
     * value: 该节点的输出结果
     */
    private Map<String, Object> nodeOutputs;

    /**
     * 执行日志
     */
    private StringBuilder executionLog;

    public WorkflowContext() {
        this.input = new HashMap<>();
        this.variables = new HashMap<>();
        this.nodeOutputs = new HashMap<>();
        this.executionLog = new StringBuilder();
    }

    /**
     * 解析参数表达式，获取实际值
     * 支持格式:
     * - 字面量: 直接返回
     * - ${input.xxx}: 从输入参数获取
     * - ${context.xxx}: 从上下文变量获取
     * - ${nodes.nodeId.field}: 从指定节点输出获取
     */
    @SuppressWarnings("unchecked")
    public Object resolveExpression(Object expression) {
        if (expression == null) {
            return null;
        }

        if (!(expression instanceof String)) {
            // 非字符串，直接返回字面量
            return expression;
        }

        String expr = (String) expression;

        // 检查是否是表达式格式 ${...}
        if (!expr.startsWith("${") || !expr.endsWith("}")) {
            // 普通字符串字面量
            return expr;
        }

        // 解析表达式
        String path = expr.substring(2, expr.length() - 1); // 去掉 ${ 和 }
        String[] parts = path.split("\\.", 2);

        if (parts.length < 2) {
            return null;
        }

        String source = parts[0];
        String field = parts[1];

        switch (source) {
            case "input":
                return getNestedValue(input, field);
            case "context":
                return getNestedValue(variables, field);
            case "nodes":
                // 格式: nodes.nodeId.field
                String[] nodeParts = field.split("\\.", 2);
                if (nodeParts.length < 2) {
                    return nodeOutputs.get(field);
                }
                Object nodeOutput = nodeOutputs.get(nodeParts[0]);
                if (nodeOutput instanceof Map) {
                    return getNestedValue((Map<String, Object>) nodeOutput, nodeParts[1]);
                }
                return null;
            default:
                return null;
        }
    }

    /**
     * 从嵌套Map中获取值，支持点号分隔的路径
     */
    @SuppressWarnings("unchecked")
    private Object getNestedValue(Map<String, Object> map, String path) {
        if (map == null || path == null) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = map;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * 设置节点输出
     */
    public void setNodeOutput(String nodeId, Object output) {
        nodeOutputs.put(nodeId, output);
    }

    /**
     * 设置上下文变量
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 追加执行日志
     */
    public void log(String message) {
        executionLog.append("[").append(System.currentTimeMillis()).append("] ")
                .append(message).append("\n");
    }

    // =============== Getters and Setters ===============

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getNodeOutputs() {
        return nodeOutputs;
    }

    public void setNodeOutputs(Map<String, Object> nodeOutputs) {
        this.nodeOutputs = nodeOutputs;
    }

    public String getExecutionLog() {
        return executionLog.toString();
    }
}
