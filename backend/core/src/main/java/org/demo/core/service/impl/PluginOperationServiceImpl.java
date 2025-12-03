package org.demo.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.demo.core.mapper.PluginOperationMapper;
import org.demo.core.model.entity.PluginOperation;
import org.demo.core.service.PluginOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 插件操作服务实现类
 */
@Service
public class PluginOperationServiceImpl implements PluginOperationService {

    @Autowired
    private PluginOperationMapper pluginOperationMapper;

    @Override
    public List<PluginOperation> listByPluginId(String pluginId) {
        LambdaQueryWrapper<PluginOperation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PluginOperation::getPluginId, pluginId)
                .orderByAsc(PluginOperation::getCreateTime);
        return pluginOperationMapper.selectList(queryWrapper);
    }

    @Override
    public PluginOperation getByPluginIdAndOperationId(String pluginId, String operationId) {
        LambdaQueryWrapper<PluginOperation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PluginOperation::getPluginId, pluginId)
                .eq(PluginOperation::getOperationId, operationId);
        return pluginOperationMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public void saveBatch(List<PluginOperation> operations) {
        for (PluginOperation operation : operations) {
            pluginOperationMapper.insert(operation);
        }
    }

    @Override
    @Transactional
    public void deleteByPluginId(String pluginId) {
        LambdaQueryWrapper<PluginOperation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PluginOperation::getPluginId, pluginId);
        pluginOperationMapper.delete(queryWrapper);
    }
}
