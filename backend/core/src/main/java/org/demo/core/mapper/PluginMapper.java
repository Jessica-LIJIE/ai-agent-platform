package org.demo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.demo.core.model.entity.Plugin;

/**
 * 插件表 Mapper 接口
 */
@Mapper
public interface PluginMapper extends BaseMapper<Plugin> {
}
