-- ============================================================
-- 智能体创作平台数据库初始化脚本
-- 版本: 1.0
-- 创建日期: 2025-11-24
-- 描述: 根据数据库设计文档生成的完整表结构定义
-- ============================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 创建数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS `ai_agent_platform_db` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `ai_agent_platform_db`;

-- ============================================================
-- 1. 用户表 (user)
-- 功能: 存储用户账号信息及认证数据
-- 关联用户故事: US-019, US-020, US-021
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`user` (
    `id` VARCHAR(64) NOT NULL COMMENT '用户唯一标识',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名（必填,用于登录）',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱地址（必填,用于登录和验证）',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称（可选）',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL（可选）',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL（别名字段）',
    `bio` TEXT DEFAULT NULL COMMENT '个人简介（可选）',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '用户角色（user/admin）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '账户状态（active/locked/inactive）',
    `email_verified` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '邮箱是否已验证',
    `login_attempts` INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
    `locked_until` DATETIME DEFAULT NULL COMMENT '账户锁定截止时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 智能体表 (agent)
-- 功能: 存储智能体的基本信息、配置及状态
-- 关联用户故事: US-001, US-002, US-003, US-004, US-005, US-017, US-022
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`agent` (
    `id` VARCHAR(64) NOT NULL COMMENT '智能体唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '智能体名称（必填）',
    `description` TEXT DEFAULT NULL COMMENT '智能体描述',
    `prompt` TEXT DEFAULT NULL COMMENT '智能体提示词',
    `prompt_template` TEXT DEFAULT NULL COMMENT '系统提示词模板（别名字段）',
    `model_config` JSON DEFAULT NULL COMMENT '模型配置（存储model、temperature、api_key_id等参数）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '智能体状态（draft/published）',
    `user_id` VARCHAR(64) NOT NULL COMMENT '创建者ID',
    `workflow_id` VARCHAR(64) DEFAULT NULL COMMENT '绑定的工作流ID',
    `knowledge_base_id` VARCHAR(64) DEFAULT NULL COMMENT '绑定的知识库ID',
    `kb_ids` JSON DEFAULT NULL COMMENT '关联的知识库ID列表（JSON数组格式）',
    `tools_config` JSON DEFAULT NULL COMMENT '绑定的插件配置（JSON数组存储插件ID列表）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_id`, `name`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_workflow` (`workflow_id`),
    CONSTRAINT `fk_agent_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体表';

-- ============================================================
-- 3. 智能体对话历史表 (agent_conversation)
-- 功能: 存储智能体的对话历史记录
-- 关联用户故事: US-004, US-018
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`agent_conversation` (
    `id` VARCHAR(64) NOT NULL COMMENT '对话记录ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID（聚合多轮对话）',
    `agent_id` VARCHAR(64) NOT NULL COMMENT '智能体ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `query` TEXT NOT NULL COMMENT '用户提问',
    `answer` LONGTEXT NOT NULL COMMENT '智能体回答',
    `metadata` JSON DEFAULT NULL COMMENT '元数据（引用来源、Token消耗）',
    `conversation_type` VARCHAR(20) NOT NULL COMMENT '类型（chat/debug）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_time` (`session_id`, `create_time`),
    KEY `idx_agent_user_time` (`agent_id`, `user_id`, `create_time`),
    CONSTRAINT `fk_conversation_agent` FOREIGN KEY (`agent_id`) REFERENCES `agent` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_conversation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体对话历史表';

-- ============================================================
-- 4. 工作流表 (workflow)
-- 功能: 存储工作流的定义、配置及状态
-- 关联用户故事: US-011, US-012, US-013
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`workflow` (
    `id` VARCHAR(64) NOT NULL COMMENT '工作流唯一标识',
    `agent_id` VARCHAR(64) DEFAULT NULL COMMENT '所属智能体ID',
    `name` VARCHAR(100) NOT NULL COMMENT '工作流名称',
    `description` TEXT DEFAULT NULL COMMENT '工作流描述',
    `definition` JSON NOT NULL COMMENT '工作流定义（存储节点和连线信息）',
    `graph_data` JSON DEFAULT NULL COMMENT '画布数据（存储前端Reactflow/X6的节点坐标、连线信息）',
    `is_valid` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'DAG校验是否通过（0-否, 1-是）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '工作流状态（active/inactive）',
    `user_id` VARCHAR(64) NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_id`, `name`),
    KEY `idx_agent` (`agent_id`),
    CONSTRAINT `fk_workflow_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_workflow_agent` FOREIGN KEY (`agent_id`) REFERENCES `agent` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流表';

-- ============================================================
-- 5. 工作流执行历史表 (workflow_run)
-- 功能: 记录工作流的每一次动态执行实例
-- 关联用户故事: US-012, US-013
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`workflow_run` (
    `id` VARCHAR(64) NOT NULL COMMENT '执行记录ID',
    `workflow_id` VARCHAR(64) NOT NULL COMMENT '工作流ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '执行者ID',
    `status` VARCHAR(20) NOT NULL COMMENT '状态（pending/running/completed/failed/terminated）',
    `inputs` JSON DEFAULT NULL COMMENT '初始输入',
    `outputs` JSON DEFAULT NULL COMMENT '最终输出',
    `error` TEXT DEFAULT NULL COMMENT '错误信息',
    `node_states` JSON DEFAULT NULL COMMENT '节点执行快照',
    `run_type` VARCHAR(20) NOT NULL COMMENT '类型（full/debug）',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_workflow_time` (`workflow_id`, `create_time`),
    CONSTRAINT `fk_run_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `workflow` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_run_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流执行历史表';

-- ============================================================
-- 6. 知识库表 (knowledge_base)
-- 功能: 存储知识库的基本信息
-- 关联用户故事: US-006, US-008, US-010
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`knowledge_base` (
    `id` VARCHAR(64) NOT NULL COMMENT '知识库唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '知识库名称（必填）',
    `description` TEXT DEFAULT NULL COMMENT '知识库描述',
    `embedding_model` VARCHAR(50) DEFAULT NULL COMMENT '向量模型（如text-embedding-3）',
    `user_id` VARCHAR(64) NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_id`, `name`),
    CONSTRAINT `fk_kb_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- ============================================================
-- 7. 文档表 (document)
-- 功能: 存储知识库中上传的文档信息及处理状态
-- 关联用户故事: US-007, US-008
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`document` (
    `id` VARCHAR(64) NOT NULL COMMENT '文档唯一标识',
    `filename` VARCHAR(255) NOT NULL COMMENT '文档文件名（原始文件名）',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文档文件名（别名字段）',
    `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件存储URL（对象存储地址）',
    `file_path` VARCHAR(512) NOT NULL COMMENT '文档存储路径',
    `file_size` BIGINT NOT NULL COMMENT '文档大小（字节）',
    `file_type` VARCHAR(50) NOT NULL COMMENT '文档类型（txt/markdown/pdf）',
    `chunk_count` INT NOT NULL DEFAULT 0 COMMENT '切分片段数量',
    `status` VARCHAR(20) NOT NULL DEFAULT 'processing' COMMENT '处理状态（processing/completed/failed）',
    `process_status` TINYINT DEFAULT NULL COMMENT '处理状态数值（0-等待, 1-处理中, 2-完成, 3-失败）',
    `error_msg` TEXT DEFAULT NULL COMMENT '处理失败原因',
    `knowledge_base_id` VARCHAR(64) NOT NULL COMMENT '所属知识库ID',
    `kb_id` VARCHAR(64) DEFAULT NULL COMMENT '所属知识库ID（别名字段）',
    `user_id` VARCHAR(64) NOT NULL COMMENT '上传者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_kb_status` (`knowledge_base_id`, `status`),
    CONSTRAINT `fk_doc_kb` FOREIGN KEY (`knowledge_base_id`) REFERENCES `knowledge_base` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_doc_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- ============================================================
-- 8. 插件表 (plugin)
-- 功能: 存储插件的注册信息、配置及状态
-- 关联用户故事: US-014, US-015, US-016
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`plugin` (
    `id` VARCHAR(64) NOT NULL COMMENT '插件唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '插件名称',
    `identifier` VARCHAR(100) DEFAULT NULL COMMENT '插件唯一标识符（key）',
    `description` TEXT DEFAULT NULL COMMENT '插件描述',
    `type` VARCHAR(20) NOT NULL DEFAULT 'http' COMMENT '插件类型（http/mqtt/local等）',
    `base_url` VARCHAR(255) DEFAULT NULL COMMENT '基础请求地址（如 https://plugin.aiot.hello1023.com）',
    `openapi_spec` JSON NOT NULL COMMENT 'OpenAPI规范内容',
    `openapi_schema` JSON DEFAULT NULL COMMENT 'OpenAPI规范（别名字段）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'disabled' COMMENT '插件状态（enabled/disabled）',
    `is_enabled` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否启用',
    `auth_info` JSON DEFAULT NULL COMMENT '鉴权信息（存储API Key等）',
    `auth_type` VARCHAR(20) DEFAULT 'none' COMMENT '鉴权类型（none/api_key/oauth等）',
    `auth_config` JSON DEFAULT NULL COMMENT '鉴权配置（别名字段）',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '注册者ID（NULL代表系统插件）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_identifier` (`identifier`),
    UNIQUE KEY `uk_user_name` (`user_id`, `name`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_plugin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='插件表';

-- ============================================================
-- 9. 插件操作表 (plugin_operation)
-- 功能: 存储每个插件的接口操作信息
-- 关联用户故事: US-014, US-015, US-016
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`plugin_operation` (
    `id` VARCHAR(64) NOT NULL COMMENT '插件操作唯一标识',
    `plugin_id` VARCHAR(64) NOT NULL COMMENT '所属插件ID',
    `operation_id` VARCHAR(100) NOT NULL COMMENT 'OpenAPI中的operationId（如getSensorData）',
    `name` VARCHAR(100) NOT NULL COMMENT '操作名称，用于前端展示',
    `method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法（GET/POST/PUT/DELETE等）',
    `path` VARCHAR(255) NOT NULL COMMENT '请求路径（如/plugin/sensor-data）',
    `description` TEXT DEFAULT NULL COMMENT '操作描述',
    `input_schema` JSON DEFAULT NULL COMMENT '入参结构（从OpenAPI解析的参数信息）',
    `output_schema` JSON DEFAULT NULL COMMENT '出参结构（从OpenAPI解析的响应信息）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_plugin_id` (`plugin_id`),
    CONSTRAINT `fk_operation_plugin` FOREIGN KEY (`plugin_id`) REFERENCES `plugin` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='插件操作表';

-- ============================================================
-- 10. 系统日志表 (system_log)
-- 功能: 存储系统操作日志及审计信息
-- 关联用户故事: US-023
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`system_log` (
    `id` VARCHAR(64) NOT NULL COMMENT '日志唯一标识',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '操作人ID（NULL表示系统操作）',
    `module` VARCHAR(50) NOT NULL COMMENT '操作模块（agent/workflow/plugin/knowledge_base等）',
    `action` VARCHAR(50) NOT NULL COMMENT '操作动作（create/update/delete/execute等）',
    `level` VARCHAR(20) NOT NULL DEFAULT 'info' COMMENT '日志级别（info/warn/error）',
    `content` TEXT DEFAULT NULL COMMENT '日志详情/错误堆栈',
    `request_params` JSON DEFAULT NULL COMMENT '请求参数快照（用于审计）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_module` (`module`),
    KEY `idx_user` (`user_id`),
    CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- ============================================================
-- 11. 系统配置表 (system_config)
-- 功能: 存储系统全局配置信息
-- 关联用户故事: US-022
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`system_config` (
    `id` VARCHAR(64) NOT NULL COMMENT '配置唯一标识',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（如default_model、max_upload_size）',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================
-- 12. LLM提供商表 (llm_providers)
-- 功能: 存储LLM提供商信息
-- 关联用户故事: US-022
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`llm_providers` (
    `id` VARCHAR(64) NOT NULL COMMENT '提供商唯一标识',
    `code` VARCHAR(50) NOT NULL COMMENT '提供商代码',
    `name` VARCHAR(100) NOT NULL COMMENT '提供商名称',
    `title` VARCHAR(200) NOT NULL COMMENT '提供商完整标题',
    `description` TEXT DEFAULT NULL COMMENT '提供商描述',
    `apply_url` VARCHAR(500) DEFAULT NULL COMMENT 'API申请地址',
    `doc_url` VARCHAR(500) DEFAULT NULL COMMENT '文档地址',
    `default_api_base` VARCHAR(500) DEFAULT NULL COMMENT '默认API地址',
    `has_free_quota` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否提供免费额度',
    `icon` VARCHAR(200) DEFAULT NULL COMMENT '图标URL或图标名称',
    `tag_type` VARCHAR(20) DEFAULT NULL COMMENT '标签类型',
    `country` VARCHAR(20) DEFAULT NULL COMMENT '国家',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM提供商表';

-- ============================================================
-- 13. LLM模型表 (llm_models)
-- 功能: 存储LLM模型配置信息
-- 关联用户故事: US-022
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_agent_platform_db`.`llm_models` (
    `id` VARCHAR(64) NOT NULL COMMENT '模型唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `display_name` VARCHAR(100) NOT NULL COMMENT '模型显示名称',
    `provider` VARCHAR(50) NOT NULL COMMENT '提供商代码',
    `model_type` VARCHAR(50) DEFAULT NULL COMMENT '模型类型',
    `api_base` VARCHAR(500) DEFAULT NULL COMMENT 'API基础URL',
    `api_key` VARCHAR(500) DEFAULT NULL COMMENT 'API密钥',
    `api_version` VARCHAR(50) DEFAULT NULL COMMENT 'API版本',
    `max_tokens` INT DEFAULT NULL COMMENT '最大token数',
    `temperature` DECIMAL(3,2) DEFAULT NULL COMMENT '温度参数',
    `top_p` DECIMAL(3,2) DEFAULT NULL COMMENT 'top_p参数',
    `enable_deep_thinking` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用深度思考',
    `frequency_penalty` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '频率惩罚参数',
    `presence_penalty` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '存在惩罚参数',
    `config` JSON DEFAULT NULL COMMENT '其他配置参数',
    `description` TEXT DEFAULT NULL COMMENT '模型描述',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认模型',
    `is_system` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统内置',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_provider` (`provider`),
    KEY `idx_is_active_is_default` (`is_active`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM模型表';

-- ============================================================
-- 初始化系统配置数据
-- ============================================================
INSERT INTO `ai_agent_platform_db`.`system_config` (`id`, `config_key`, `config_value`, `description`) VALUES
('config_001', 'default_model', 'gpt-4', '默认大模型'),
('config_002', 'max_upload_size', '104857600', '最大上传文件大小（字节，默认100MB）'),
('config_003', 'enable_registration', 'true', '是否开放注册');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 脚本执行完成
-- ============================================================
