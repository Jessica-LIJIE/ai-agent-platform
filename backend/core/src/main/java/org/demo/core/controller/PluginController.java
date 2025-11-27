package org.demo.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.demo.core.api.ApiResponse;
import org.demo.core.mapper.PluginMapper;
import org.demo.core.model.entity.Plugin;
import org.springframework.transaction.annotation.Transactional;
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

    private final PluginMapper pluginMapper;

    /**
     * 查询所有插件
     *
     * @return 插件列表
     */
    @Operation(
            summary = "查询所有插件",
            description = "获取系统中所有插件的完整列表，包括插件的基本信息、OpenAPI规范、鉴权配置、状态等信息。返回包括系统插件和用户注册的插件"
    )
    @GetMapping
    public ApiResponse<List<Plugin>> selectAll() {
        List<Plugin> plugins = pluginMapper.selectList(new QueryWrapper<>());
        return ApiResponse.ok(plugins);
    }

    /**
     * 根据ID查询插件
     *
     * @param id 插件ID
     * @return 插件信息
     */
    @Operation(
            summary = "根据ID查询插件",
            description = "通过插件的唯一标识符ID查询指定插件的详细信息，包括名称、描述、标识符、OpenAPI规范、鉴权类型、鉴权配置、启用状态、注册者等完整信息"
    )
    @GetMapping("/{id}")
    public ApiResponse<Plugin> selectById(
            @Parameter(description = "插件的唯一标识符ID", required = true, example = "1234567890")
            @PathVariable String id
    ) {
        Plugin plugin = pluginMapper.selectById(id);
        if (plugin == null) {
            return ApiResponse.fail("插件不存在");
        }
        return ApiResponse.ok(plugin);
    }

    /**
     * 创建插件
     *
     * @param plugin 插件信息
     * @return 创建结果
     */
    @Operation(
            summary = "创建插件",
            description = "创建一个新的插件，需要提供插件的基本信息（名称、标识符、描述等）、OpenAPI规范、鉴权配置等。系统会自动生成唯一ID，并记录创建时间。可以配置插件的启用状态和鉴权方式"
    )
    @PostMapping
    public ApiResponse<Plugin> create(
            @Parameter(description = "插件信息对象，包含名称、标识符、描述、OpenAPI规范、鉴权配置等字段", required = true)
            @RequestBody Plugin plugin
    ) {
        int rows = pluginMapper.insert(plugin);
        if (rows > 0) {
            return ApiResponse.ok("创建成功", plugin);
        }
        return ApiResponse.fail("创建失败");
    }

    /**
     * 更新插件
     *
     * @param id     插件ID
     * @param plugin 插件信息
     * @return 更新结果
     */
    @Operation(
            summary = "更新插件",
            description = "根据ID更新指定插件的信息。可以修改插件的名称、描述、标识符、OpenAPI规范、鉴权配置、启用状态等。系统会自动更新修改时间。请求体中的ID会被路径中的ID覆盖"
    )
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Plugin> update(
            @Parameter(description = "要更新的插件的唯一标识符ID", required = true, example = "1234567890")
            @PathVariable String id,
            @Parameter(description = "更新后的插件信息，可以包含任何需要修改的字段", required = true)
            @RequestBody Plugin plugin
    ) {
        Plugin existingPlugin = pluginMapper.selectById(id);
        if (existingPlugin == null) {
            return ApiResponse.fail("插件不存在");
        }
        plugin.setId(id);
        int rows = pluginMapper.updateById(plugin);
        if (rows > 0) {
            return ApiResponse.ok("更新成功", plugin);
        }
        return ApiResponse.fail("更新失败");
    }

    /**
     * 删除插件
     *
     * @param id 插件ID
     * @return 删除结果
     */
    @Operation(
            summary = "删除插件",
            description = "根据ID删除指定的插件。执行物理删除操作，会从数据库中永久删除插件记录。删除前会检查插件是否存在，删除后将无法恢复"
    )
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "要删除的插件的唯一标识符ID", required = true, example = "1234567890")
            @PathVariable String id
    ) {
        Plugin existingPlugin = pluginMapper.selectById(id);
        if (existingPlugin == null) {
            return ApiResponse.fail("插件不存在");
        }
        int rows = pluginMapper.deleteById(id);
        if (rows > 0) {
            return ApiResponse.ok("删除成功", null);
        }
        return ApiResponse.fail("删除失败");
    }
}
