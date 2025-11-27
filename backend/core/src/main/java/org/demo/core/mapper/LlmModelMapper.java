package org.demo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.demo.core.model.entity.LlmModel;

/**
 * LLM模型表Mapper接口
 */
@Mapper
public interface LlmModelMapper extends BaseMapper<LlmModel> {
}
