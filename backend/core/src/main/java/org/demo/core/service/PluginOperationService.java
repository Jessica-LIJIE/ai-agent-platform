package org.demo.core.service;

import org.demo.core.model.entity.PluginOperation;

import java.util.List;

/**
 * 插件操作服务接口
 */
public interface PluginOperationService {

    /**
     * 根据插件ID获取所有操作
     * 
     * @param pluginId 插件ID
     * @return 操作列表
     */
    List<PluginOperation> listByPluginId(String pluginId);

    /**
     * 根据插件ID和操作ID获取操作
     * 
     * @param pluginId    插件ID
     * @param operationId 操作ID（如getSensorData）
     * @return 操作实体
     */
    PluginOperation getByPluginIdAndOperationId(String pluginId, String operationId);

    /**
     * 批量保存插件操作
     * 
     * @param operations 操作列表
     */
    void saveBatch(List<PluginOperation> operations);

    /**
     * 根据插件ID删除所有操作
     * 
     * @param pluginId 插件ID
     */
    void deleteByPluginId(String pluginId);
}
