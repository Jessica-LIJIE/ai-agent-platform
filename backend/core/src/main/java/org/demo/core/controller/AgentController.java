package org.demo.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.demo.core.api.ApiResponse;
import org.demo.core.mapper.AgentMapper;
import org.demo.core.model.entity.Agent;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能体控制器
 * 提供智能体的CRUD操作接口
 */
@Tag(name = "智能体管理", description = "提供智能体的增删改查等管理功能接口")
@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentMapper agentMapper;

    /**
     * 查询所有智能体
     *
     * @return 智能体列表
     */
    @Operation(
            summary = "查询所有智能体",
            description = "获取系统中所有智能体的完整列表，包括智能体的基本信息、配置参数、关联的知识库和插件等信息"
    )
    @GetMapping
    public ApiResponse<List<Agent>> selectAll() {
        List<Agent> agents = agentMapper.selectList(new QueryWrapper<>());
        return ApiResponse.ok(agents);
    }

    /**
     * 根据ID查询智能体
     *
     * @param id 智能体ID
     * @return 智能体信息
     */
    @Operation(
            summary = "根据ID查询智能体",
            description = "通过智能体的唯一标识符ID查询指定智能体的详细信息，包括名称、描述、提示词、模型配置、绑定的知识库和插件等完整信息"
    )
    @GetMapping("/{id}")
    public ApiResponse<Agent> selectById(
            @Parameter(description = "智能体的唯一标识符ID", required = true, example = "1234567890")
            @PathVariable String id
    ) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            return ApiResponse.fail("智能体不存在");
        }
        return ApiResponse.ok(agent);
    }

    /**
     * 创建智能体
     *
     * @param agent 智能体信息
     * @return 创建结果
     */
    @Operation(
            summary = "创建智能体",
            description = "创建一个新的智能体，需要提供智能体的基本信息（名称、描述、提示词等）。系统会自动生成唯一ID，并记录创建时间。可以配置模型参数、关联知识库和插件"
    )
    @PostMapping
    public ApiResponse<Agent> create(
            @Parameter(description = "智能体信息对象，包含名称、描述、提示词、模型配置等字段", required = true)
            @RequestBody Agent agent
    ) {
        int rows = agentMapper.insert(agent);
        if (rows > 0) {
            return ApiResponse.ok("创建成功", agent);
        }
        return ApiResponse.fail("创建失败");
    }

    /**
     * 更新智能体
     *
     * @param id    智能体ID
     * @param agent 智能体信息
     * @return 更新结果
     */
    @Operation(
            summary = "更新智能体",
            description = "根据ID更新指定智能体的信息。可以修改智能体的名称、描述、提示词、模型配置、关联的知识库和插件等。系统会自动更新修改时间。请求体中的ID会被路径中的ID覆盖"
    )
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Agent> update(
            @Parameter(description = "要更新的智能体的唯一标识符ID", required = true, example = "1234567890")
            @PathVariable String id,
            @Parameter(description = "更新后的智能体信息，可以包含任何需要修改的字段", required = true)
            @RequestBody Agent agent
    ) {
        Agent existingAgent = agentMapper.selectById(id);
        if (existingAgent == null) {
            return ApiResponse.fail("智能体不存在");
        }
        agent.setId(id);
        int rows = agentMapper.updateById(agent);
        if (rows > 0) {
            return ApiResponse.ok("更新成功", agent);
        }
        return ApiResponse.fail("更新失败");
    }

    /**
     * 删除智能体
     *
     * @param id 智能体ID
     * @return 删除结果
     */
    @Operation(
            summary = "删除智能体",
            description = "根据ID删除指定的智能体。执行物理删除操作，直接从数据库中移除记录"
    )
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "要删除的智能体的唯一标识符ID", required = true, example = "1234567890")
            @PathVariable String id
    ) {
        Agent existingAgent = agentMapper.selectById(id);
        if (existingAgent == null) {
            return ApiResponse.fail("智能体不存在");
        }
        int rows = agentMapper.deleteById(id);
        if (rows > 0) {
            return ApiResponse.ok("删除成功", null);
        }
        return ApiResponse.fail("删除失败");
    }
}
