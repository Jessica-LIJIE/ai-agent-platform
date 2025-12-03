package org.demo.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.demo.core.api.ApiResponse;
import org.demo.core.model.dto.PluginCreateRequest;
import org.demo.core.model.dto.PluginInvokeRequest;
import org.demo.core.model.dto.PluginOpenApiImportRequest;
import org.demo.core.model.dto.PluginStatusUpdateRequest;
import org.demo.core.model.dto.PluginUpdateRequest;
import org.demo.core.model.vo.PageResult;
import org.demo.core.model.vo.PluginDetailVO;
import org.demo.core.model.vo.PluginInvokeResult;
import org.demo.core.model.vo.PluginListItemVO;
import org.demo.core.model.vo.PluginOperationVO;
import org.demo.core.service.PluginService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 插件控制器
 * 提供插件的CRUD操作接口
 */
@Tag(name = "插件管理", description = "提供插件的增删改查等管理功能接口")
@RestController
@RequestMapping("/api/v1/plugins")
@RequiredArgsConstructor
public class PluginController {

    private final PluginService pluginService;

    /**
     * 列表查询插件
     * GET /plugins
     *
     * @param page     页码（可选，默认1）
     * @param pageSize 每页大小（可选，默认10）
     * @return 分页插件列表
     */
    @Operation(summary = "列表查询插件", description = "分页获取系统中所有插件的列表，返回插件的基本信息（id、name、description、create_time、update_time）和总数")
    @GetMapping
    public ApiResponse<PageResult<PluginListItemVO>> listPlugins(
            @Parameter(description = "页码，从1开始", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(required = false) Integer pageSize) {
        PageResult<PluginListItemVO> result = pluginService.listPlugins(page, pageSize);
        return ApiResponse.ok("查询成功", result);
    }

    /**
     * 获取插件详情
     * GET /plugins/{pluginId}
     *
     * @param pluginId 插件ID
     * @return 插件详细信息
     */
    @Operation(summary = "获取插件详情", description = "通过插件ID获取插件的详细信息，包括名称、描述、请求方法、端点、参数Schema、响应Schema、鉴权配置等")
    @GetMapping("/{pluginId}")
    public ApiResponse<PluginDetailVO> getPluginDetail(
            @Parameter(description = "插件的唯一标识符ID", required = true, example = "plugin_456") @PathVariable String pluginId) {
        PluginDetailVO detail = pluginService.getPluginDetail(pluginId);
        return ApiResponse.ok("查询成功", detail);
    }

    /**
     * 创建插件
     * POST /plugins
     *
     * @param request 创建插件请求
     * @return 创建的插件详情
     */
    @Operation(summary = "创建插件", description = "创建一个新的插件，需要提供插件的名称、描述、端点、请求方法、参数Schema、响应Schema、鉴权配置等信息")
    @PostMapping
    public ApiResponse<PluginDetailVO> createPlugin(
            @Parameter(description = "创建插件请求体", required = true) @RequestBody PluginCreateRequest request) {
        // TODO: 从登录上下文获取当前用户ID，暂时使用数据库中的测试用户
        String currentUserId = "user-004-dev";
        PluginDetailVO detail = pluginService.createPlugin(request, currentUserId);
        return ApiResponse.ok("创建成功", detail);
    }

    /**
     * 更新插件
     * PUT /plugins/{pluginId}
     *
     * @param pluginId 插件ID
     * @param request  更新插件请求
     * @return 更新后的插件详情
     */
    @Operation(summary = "更新插件", description = "根据ID更新指定插件的信息，支持部分更新，只更新请求中提供的非空字段")
    @PutMapping("/{pluginId}")
    public ApiResponse<PluginDetailVO> updatePlugin(
            @Parameter(description = "要更新的插件ID", required = true, example = "plugin_456") @PathVariable String pluginId,
            @Parameter(description = "更新插件请求体", required = true) @RequestBody PluginUpdateRequest request) {
        // TODO: 从登录上下文获取当前用户ID，暂时使用数据库中的测试用户
        String currentUserId = "user-004-dev";
        PluginDetailVO detail = pluginService.updatePlugin(pluginId, request, currentUserId);
        return ApiResponse.ok("更新成功", detail);
    }

    /**
     * 删除插件
     * DELETE /plugins/{pluginId}
     *
     * @param pluginId 插件ID
     * @return 删除结果
     */
    @Operation(summary = "删除插件", description = "根据ID删除指定的插件，执行物理删除操作")
    @DeleteMapping("/{pluginId}")
    public ApiResponse<Void> deletePlugin(
            @Parameter(description = "要删除的插件ID", required = true, example = "plugin_456") @PathVariable String pluginId) {
        // TODO: 从登录上下文获取当前用户ID，暂时使用数据库中的测试用户
        String currentUserId = "user-004-dev";
        pluginService.deletePlugin(pluginId, currentUserId);
        return ApiResponse.ok("删除成功", null);
    }

    /**
     * 从OpenAPI/JSON导入插件
     * POST /plugins/import-openapi
     *
     * @param request 导入请求
     * @return 创建的插件详情
     */
    @Operation(summary = "导入插件", description = "从OpenAPI规范或简化JSON格式导入插件，支持AIOT设备等场景。可以通过openapiSpec提供标准OpenAPI 3.0规范，或通过operations提供简化的操作列表")
    @PostMapping("/import-openapi")
    public ApiResponse<PluginDetailVO> importFromOpenApi(
            @Parameter(description = "导入请求体", required = true) @RequestBody PluginOpenApiImportRequest request) {
        // TODO: 从登录上下文获取当前用户ID，暂时使用数据库中的测试用户
        String currentUserId = "user-004-dev";
        PluginDetailVO detail = pluginService.importFromOpenApi(request, currentUserId);
        return ApiResponse.ok("导入成功", detail);
    }

    /**
     * 获取插件的操作列表
     * GET /plugins/{pluginId}/operations
     *
     * @param pluginId 插件ID
     * @return 操作列表
     */
    @Operation(summary = "获取插件操作列表", description = "获取指定插件的所有可用操作，每个操作包含operationId、name、method、path、description、inputSchema、outputSchema等信息")
    @GetMapping("/{pluginId}/operations")
    public ApiResponse<List<PluginOperationVO>> listOperations(
            @Parameter(description = "插件ID", required = true, example = "plugin_456") @PathVariable String pluginId) {
        List<PluginOperationVO> operations = pluginService.listOperations(pluginId);
        return ApiResponse.ok("查询成功", operations);
    }

    /**
     * 更新插件状态
     * PATCH /plugins/{pluginId}/status
     *
     * @param pluginId 插件ID
     * @param request  状态更新请求
     * @return 更新后的插件详情
     */
    @Operation(summary = "更新插件状态", description = "启用或禁用指定插件，isEnabled=true表示启用，isEnabled=false表示禁用")
    @PatchMapping("/{pluginId}/status")
    public ApiResponse<PluginDetailVO> updatePluginStatus(
            @Parameter(description = "插件ID", required = true, example = "plugin_456") @PathVariable String pluginId,
            @Parameter(description = "状态更新请求体", required = true) @RequestBody PluginStatusUpdateRequest request) {
        // TODO: 从登录上下文获取当前用户ID，暂时使用数据库中的测试用户
        String currentUserId = "user-004-dev";
        PluginDetailVO detail = pluginService.updatePluginStatus(pluginId, request.getIsEnabled(), currentUserId);
        return ApiResponse.ok("状态更新成功", detail);
    }

    /**
     * 调用插件操作
     * POST /plugins/{pluginId}/operations/{operationId}/invoke
     *
     * @param pluginId    插件ID
     * @param operationId 操作ID（对应plugin_operation表的operation_id字段）
     * @param request     调用请求（包含params参数）
     * @return 调用结果
     */
    @Operation(summary = "调用插件操作", description = "调用指定插件的指定操作，向目标服务发送HTTP请求并返回结果。可用于测试插件是否正常工作，或直接控制AIOT设备")
    @PostMapping("/{pluginId}/operations/{operationId}/invoke")
    public ApiResponse<PluginInvokeResult> invokeOperation(
            @Parameter(description = "插件ID", required = true, example = "plugin_456") @PathVariable String pluginId,
            @Parameter(description = "操作ID", required = true, example = "getSensorData") @PathVariable String operationId,
            @Parameter(description = "调用请求体", required = true) @RequestBody(required = false) PluginInvokeRequest request) {

        PluginInvokeResult result = pluginService.invokeOperation(
                pluginId,
                operationId,
                request != null ? request.getParams() : null,
                request != null ? request.getTimeout() : null);

        if ("success".equals(result.getStatus())) {
            return ApiResponse.ok("调用成功", result);
        } else if ("timeout".equals(result.getStatus())) {
            return ApiResponse.ok("调用超时", result);
        } else {
            return ApiResponse.ok("调用失败", result);
        }
    }
}
