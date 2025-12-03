package org.demo.core.workflow;

import org.demo.core.model.vo.PluginInvokeResult;
import org.demo.core.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作流插件节点执行器
 * 负责在工作流中执行插件节点
 */
@Service
public class WorkflowPluginExecutor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowPluginExecutor.class);

    @Autowired
    private PluginService pluginService;

    /**
     * 执行插件节点
     *
     * @param nodeConfig 节点配置
     * @param context    工作流上下文
     * @return 执行结果
     */
    public PluginNodeResult execute(PluginNodeConfig nodeConfig, WorkflowContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        long startMillis = System.currentTimeMillis();

        String pluginId = nodeConfig.getPluginId();
        String operationId = nodeConfig.getOperationId();
        String nodeId = nodeConfig.getNodeId();

        context.log("开始执行插件节点: " + nodeId + " (plugin=" + pluginId + ", operation=" + operationId + ")");

        int retryCount = 0;
        PluginNodeResult result = null;

        // 重试循环
        do {
            try {
                // 1. 解析参数映射，将表达式转换为实际值
                Map<String, Object> resolvedParams = resolveParams(nodeConfig.getParamMappings(), context);
                context.log("解析后的参数: " + resolvedParams);

                // 2. 调用插件服务执行操作
                PluginInvokeResult invokeResult = pluginService.invokeOperation(
                        pluginId,
                        operationId,
                        resolvedParams,
                        nodeConfig.getTimeout());

                // 3. 转换结果
                result = convertToNodeResult(invokeResult, startTime, startMillis, retryCount);

                if (result.isSuccess()) {
                    // 成功，跳出重试循环
                    context.log("插件节点执行成功，耗时: " + result.getDuration() + "ms");

                    // 4. 将结果存储到上下文
                    storeResult(nodeConfig, context, result);
                    break;
                } else {
                    // 失败，检查是否需要重试
                    context.log("插件节点执行失败: " + result.getErrorMessage());
                    retryCount++;

                    if (retryCount <= nodeConfig.getRetryCount()) {
                        context.log("准备第 " + retryCount + " 次重试，等待 " + nodeConfig.getRetryInterval() + "ms");
                        Thread.sleep(nodeConfig.getRetryInterval());
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                result = PluginNodeResult.failure("INTERRUPTED", "执行被中断");
                break;
            } catch (Exception e) {
                logger.error("插件节点执行异常: nodeId={}, error={}", nodeId, e.getMessage(), e);
                result = PluginNodeResult.failure("EXECUTION_ERROR", "执行异常: " + e.getMessage());
                retryCount++;

                if (retryCount <= nodeConfig.getRetryCount()) {
                    try {
                        context.log("执行异常，准备第 " + retryCount + " 次重试");
                        Thread.sleep(nodeConfig.getRetryInterval());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } while (retryCount <= nodeConfig.getRetryCount());

        // 设置最终的计时信息
        LocalDateTime endTime = LocalDateTime.now();
        long duration = System.currentTimeMillis() - startMillis;
        result = result.withTiming(startTime, endTime, duration);
        result.setRetryCount(retryCount > 0 ? retryCount - 1 : 0);

        // 处理失败情况
        if (!result.isSuccess()) {
            if (nodeConfig.isContinueOnError()) {
                context.log("节点执行失败，但配置了continueOnError，工作流继续执行");
                // 即使失败，也将错误信息存储到上下文
                storeErrorResult(nodeConfig, context, result);
            } else {
                context.log("节点执行失败，工作流将终止");
            }
        }

        return result;
    }

    /**
     * 解析参数映射，将表达式转换为实际值
     */
    private Map<String, Object> resolveParams(Map<String, Object> paramMappings, WorkflowContext context) {
        if (paramMappings == null || paramMappings.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> resolved = new HashMap<>();
        for (Map.Entry<String, Object> entry : paramMappings.entrySet()) {
            String paramName = entry.getKey();
            Object expression = entry.getValue();

            // 使用上下文解析表达式
            Object resolvedValue = context.resolveExpression(expression);
            resolved.put(paramName, resolvedValue);
        }

        return resolved;
    }

    /**
     * 将PluginInvokeResult转换为PluginNodeResult
     */
    private PluginNodeResult convertToNodeResult(PluginInvokeResult invokeResult,
            LocalDateTime startTime,
            long startMillis,
            int retryCount) {
        PluginNodeResult result;

        if (invokeResult.isSuccess()) {
            result = PluginNodeResult.success(invokeResult.getParsedData());
        } else if (invokeResult.isTimeout()) {
            result = PluginNodeResult.timeout(invokeResult.getErrorMessage());
        } else {
            result = PluginNodeResult.failure(
                    invokeResult.getHttpStatusCode() != null ? "HTTP_" + invokeResult.getHttpStatusCode()
                            : "INVOKE_ERROR",
                    invokeResult.getErrorMessage());
        }

        result.setHttpStatus(invokeResult.getHttpStatusCode());
        result.setDuration(invokeResult.getDuration());
        result.setRetryCount(retryCount);

        return result;
    }

    /**
     * 将成功结果存储到工作流上下文
     */
    private void storeResult(PluginNodeConfig nodeConfig, WorkflowContext context, PluginNodeResult result) {
        String nodeId = nodeConfig.getNodeId();

        // 1. 始终将完整结果存储到nodeOutputs
        context.setNodeOutput(nodeId, result.getData());

        // 2. 根据outputMapping将特定字段映射到上下文变量
        String outputMapping = nodeConfig.getOutputMapping();
        if (outputMapping != null && !outputMapping.isEmpty()) {
            context.setVariable(outputMapping, result.getData());
        }
    }

    /**
     * 将失败结果存储到工作流上下文
     */
    private void storeErrorResult(PluginNodeConfig nodeConfig, WorkflowContext context, PluginNodeResult result) {
        String nodeId = nodeConfig.getNodeId();

        // 存储错误信息到nodeOutputs
        Map<String, Object> errorOutput = new HashMap<>();
        errorOutput.put("success", false);
        errorOutput.put("errorCode", result.getErrorCode());
        errorOutput.put("errorMessage", result.getErrorMessage());
        errorOutput.put("httpStatus", result.getHttpStatus());

        context.setNodeOutput(nodeId, errorOutput);
    }
}
