package org.demo.core.service;

import org.demo.core.model.dto.PluginCreateRequest;
import org.demo.core.model.dto.PluginOpenApiImportRequest;
import org.demo.core.model.dto.PluginUpdateRequest;
import org.demo.core.model.vo.PageResult;
import org.demo.core.model.vo.PluginDetailVO;
import org.demo.core.model.vo.PluginInvokeResult;
import org.demo.core.model.vo.PluginListItemVO;
import org.demo.core.model.vo.PluginOperationVO;

import java.util.List;
import java.util.Map;

/**
 * 插件服务接口
 */
public interface PluginService {

    /**
     * 分页获取插件列表
     * 
     * @param pageNo   页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<PluginListItemVO> listPlugins(Integer pageNo, Integer pageSize);

    /**
     * 获取插件详情
     * 
     * @param pluginId 插件ID
     * @return 插件详情
     */
    PluginDetailVO getPluginDetail(String pluginId);

    /**
     * 创建插件
     * 
     * @param request 创建请求
     * @param userId  当前用户ID
     * @return 创建的插件详情
     */
    PluginDetailVO createPlugin(PluginCreateRequest request, String userId);

    /**
     * 更新插件
     * 
     * @param pluginId 插件ID
     * @param request  更新请求
     * @param userId   当前用户ID
     * @return 更新后的插件详情
     */
    PluginDetailVO updatePlugin(String pluginId, PluginUpdateRequest request, String userId);

    /**
     * 删除插件
     * 
     * @param pluginId 插件ID
     * @param userId   当前用户ID
     */
    void deletePlugin(String pluginId, String userId);

    /**
     * 从OpenAPI/JSON导入插件
     * 
     * @param request 导入请求
     * @param userId  当前用户ID
     * @return 创建的插件详情
     */
    PluginDetailVO importFromOpenApi(PluginOpenApiImportRequest request, String userId);

    /**
     * 获取插件的操作列表
     * 
     * @param pluginId 插件ID
     * @return 操作列表
     */
    List<PluginOperationVO> listOperations(String pluginId);

    /**
     * 更新插件状态
     * 
     * @param pluginId  插件ID
     * @param isEnabled 是否启用
     * @param userId    当前用户ID
     * @return 更新后的插件详情
     */
    PluginDetailVO updatePluginStatus(String pluginId, Boolean isEnabled, String userId);

    /**
     * 调用插件操作
     * 
     * @param pluginId    插件ID
     * @param operationId 操作ID（对应plugin_operation表的operation_id字段）
     * @param params      调用参数
     * @param timeout     超时时间（毫秒），null则使用默认值30秒
     * @return 调用结果
     */
    PluginInvokeResult invokeOperation(String pluginId, String operationId, Map<String, Object> params,
            Integer timeout);
}
