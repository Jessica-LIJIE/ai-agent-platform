package org.demo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.demo.core.model.entity.LlmProvider;

/**
 * LLM提供商Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 */
@Mapper
public interface LlmProviderMapper extends BaseMapper<LlmProvider> {
    // BaseMapper已提供基础CRUD方法，如需自定义SQL可在此添加
}
