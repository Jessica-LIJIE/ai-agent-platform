package org.demo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.demo.core.model.entity.PluginOperation;

/**
 * 插件操作 Mapper 接口
 */
@Mapper
public interface PluginOperationMapper extends BaseMapper<PluginOperation> {
}
