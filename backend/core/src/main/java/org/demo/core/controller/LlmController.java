package org.demo.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.demo.core.api.ApiResponse;
import org.demo.core.mapper.LlmModelMapper;
import org.demo.core.mapper.LlmProviderMapper;
import org.demo.core.model.entity.LlmModel;
import org.demo.core.model.entity.LlmProvider;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LLM控制器
 * 提供LLM模型和提供商的管理接口
 */
@Tag(name = "LLM管理", description = "提供LLM模型的增删改查以及LLM提供商的查询功能接口")
@RestController
@RequestMapping("/api/v1/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmModelMapper llmModelMapper;
    private final LlmProviderMapper llmProviderMapper;

    // ==================== LLM Model 相关接口 ====================

    /**
     * 查询所有LLM模型
     *
     * @return LLM模型列表
     */
    @Operation(
            summary = "查询所有LLM模型",
            description = "获取系统中所有LLM模型的完整列表，包括模型的基本信息、配置参数（如温度、最大token数等）、API密钥、提供商信息等"
    )
    @GetMapping("/models")
    public ApiResponse<List<LlmModel>> selectAllModels() {
        List<LlmModel> models = llmModelMapper.selectList(new QueryWrapper<>());
        return ApiResponse.ok(models);
    }

    /**
     * 根据ID查询LLM模型
     *
     * @param id 模型ID
     * @return LLM模型信息
     */
    @Operation(
            summary = "根据ID查询LLM模型",
            description = "通过模型的唯一标识符ID查询指定LLM模型的详细信息，包括模型名称、类型、提供商、API配置、参数设置（温度、top_p、频率惩罚等）等完整信息"
    )
    @GetMapping("/models/{id}")
    public ApiResponse<LlmModel> selectModelById(
            @Parameter(description = "LLM模型的唯一标识符ID", required = true, example = "1234567890")
            @PathVariable String id
    ) {
        LlmModel model = llmModelMapper.selectById(id);
        if (model == null) {
            return ApiResponse.fail("LLM模型不存在");
        }
        return ApiResponse.ok(model);
    }

    /**
     * 创建LLM模型
     *
     * @param model LLM模型信息
     * @return 创建结果
     */
    @Operation(
            summary = "创建LLM模型",
            description = "创建一个新的LLM模型配置，需要提供模型的基本信息（名称、显示名称、提供商代码等）和配置参数（API地址、密钥、温度、最大token等）。系统会自动生成唯一ID，并记录创建时间"
    )
    @PostMapping("/models")
    public ApiResponse<LlmModel> createModel(
            @Parameter(description = "LLM模型信息对象，包含名称、提供商、API配置、参数设置等字段", required = true)
            @RequestBody LlmModel model
    ) {
        int rows = llmModelMapper.insert(model);
        if (rows > 0) {
            return ApiResponse.ok("创建成功", model);
        }
        return ApiResponse.fail("创建失败");
    }

    /**
     * 更新LLM模型
     *
     * @param id    模型ID
     * @param model LLM模型信息
     * @return 更新结果
     */
    @Operation(
            summary = "更新LLM模型",
            description = "根据ID更新指定LLM模型的信息。可以修改模型的名称、显示名称、API配置、参数设置等。系统会自动更新修改时间。请求体中的ID会被路径中的ID覆盖"
    )
    @PutMapping("/models/{id}")
    @Transactional
    public ApiResponse<LlmModel> updateModel(
            @Parameter(description = "要更新的LLM模型的唯一标识符ID", required = true, example = "1234567890")
            @PathVariable String id,
            @Parameter(description = "更新后的LLM模型信息，可以包含任何需要修改的字段", required = true)
            @RequestBody LlmModel model
    ) {
        LlmModel existingModel = llmModelMapper.selectById(id);
        if (existingModel == null) {
            return ApiResponse.fail("LLM模型不存在");
        }
        model.setId(id);
        int rows = llmModelMapper.updateById(model);
        if (rows > 0) {
            return ApiResponse.ok("更新成功", model);
        }
        return ApiResponse.fail("更新失败");
    }

    /**
     * 删除LLM模型
     *
     * @param id 模型ID
     * @return 删除结果
     */
    @Operation(
            summary = "删除LLM模型",
            description = "根据ID删除指定的LLM模型配置。如果LlmModel实体配置了逻辑删除，将执行软删除操作；否则将物理删除记录。删除前会验证模型是否存在"
    )
    @DeleteMapping("/models/{id}")
    public ApiResponse<Void> deleteModel(
            @Parameter(description = "要删除的LLM模型的唯一标识符ID", required = true, example = "1234567890")
            @PathVariable String id
    ) {
        LlmModel existingModel = llmModelMapper.selectById(id);
        if (existingModel == null) {
            return ApiResponse.fail("LLM模型不存在");
        }
        int rows = llmModelMapper.deleteById(id);
        if (rows > 0) {
            return ApiResponse.ok("删除成功", null);
        }
        return ApiResponse.fail("删除失败");
    }

    // ==================== LLM Provider 相关接口 ====================

    /**
     * 查询所有LLM提供商
     *
     * @return LLM提供商列表
     */
    @Operation(
            summary = "查询所有LLM提供商",
            description = "获取系统中所有LLM提供商的完整列表，包括提供商的基本信息（代码、名称、描述）、API申请地址、文档地址、默认API地址、免费额度信息、图标、标签、国家等详细信息，按排序顺序返回"
    )
    @GetMapping("/providers")
    public ApiResponse<List<LlmProvider>> selectAllProviders() {
        List<LlmProvider> providers = llmProviderMapper.selectList(new QueryWrapper<>());
        return ApiResponse.ok(providers);
    }
}
