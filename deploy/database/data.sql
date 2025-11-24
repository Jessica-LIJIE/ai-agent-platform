-- ============================================================
-- 智能体创作平台初始数据
-- 版本: 1.0
-- 创建日期: 2025-11-24
-- 描述: 为 ai_agent_platform_db 数据库填充基础测试数据
-- ============================================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ai_agent_platform_db`;

-- ------------------------------------------------------------
-- 清空现有测试数据（可选，但推荐在测试环境中执行）
-- ------------------------------------------------------------
DELETE FROM `system_log`;
DELETE FROM `workflow_run`;
DELETE FROM `agent_conversation`;
DELETE FROM `agent`;
DELETE FROM `workflow`;
DELETE FROM `plugin`;
DELETE FROM `document`;
DELETE FROM `knowledge_base`;
DELETE FROM `user`;
-- 注意：system_config 表数据通常在建表后就固定了，不在此处删除。


-- ============================================================
-- 1. 用户表 (user) - 增加普通用户
-- 密码字段为 BCrypt 加密后的 'password123'
-- ============================================================
INSERT INTO `user` (`id`, `username`, `email`, `password`, `nickname`, `role`, `status`, `email_verified`, `create_time`) VALUES
('user-001-admin', 'sysadmin', 'admin@homeagent.com', '$2a$10$Q.2K0u4q0h.6gP2xMv4f0O/l6uR9x.QvK4zYp.E2D7T9Zz8G6g/w.', '平台管理员', 'admin', 'active', TRUE, '2025-11-01 10:00:00'),
('user-002-home', 'home_creator', 'smart_home@homeagent.com', '$2a$10$Q.2K0u4q0h.6gP2xMv4f0O/l6uR9x.QvK4zYp.E2D7T9Zz8G6g/w.', '智能家居管家', 'user', 'active', TRUE, '2025-11-01 10:05:00'),
('user-003-tester', 'testuser', 'tester@homeagent.com', '$2a$10$Q.2K0u4q0h.6gP2xMv4f0O/l6uR9x.QvK4zYp.E2D7T9Zz8G6g/w.', '家庭用户A', 'user', 'active', TRUE, '2025-11-05 15:30:00'),
('user-004-dev', 'plugin_dev', 'dev@homeagent.com', '$2a$10$Q.2K0u4q0h.6gP2xMv4f0O/l6uR9x.QvK4zYp.E2D7T9Zz8G6g/w.', '插件开发者', 'user', 'active', TRUE, '2025-11-08 09:00:00');


-- ============================================================
-- 2. 智能体表 (agent) - 增加一个仅用插件的Agent
-- ------------------------------------------------------------
INSERT INTO `agent` (`id`, `name`, `description`, `prompt`, `model_config`, `status`, `user_id`, `workflow_id`, `kb_ids`, `tools_config`, `create_time`) VALUES
('agent-001-smarthome', '智能家居助理', '你可以控制家里的LED灯，查询室内温度，并能回答关于设备文档的问题。', '你是一个友好的智能家居助理，请优先使用插件执行操作，如果用户提问关于设备的问题，请参考知识库。', '{"model": "gpt-4-turbo", "temperature": 0.2}', 'published', 'user-002-home', 'wf-001-home-ctrl', '["kb-001-dev", "kb-002-faq"]', '["plugin-001-led", "plugin-002-temp"]', '2025-11-22 10:00:00'),
('agent-002-scheduler', '日程管理Agent', '专门用于处理家庭日程、提醒和日历查询。', '你是一个日程管理专家，请利用日历插件帮助用户安排生活。', '{"model": "gpt-3.5-turbo", "temperature": 0.5}', 'draft', 'user-002-home', NULL, '[]', '["plugin-003-calendar"]', '2025-11-23 09:30:00');

-- 关联 Agent 和 Workflow
UPDATE `workflow` SET `agent_id` = 'agent-001-smarthome' WHERE `id` = 'wf-001-home-ctrl';
-- 注意：工作流 wf-002-auto-off 是一个定时任务，可能不会绑定到任何用户可见的 Agent。


-- ============================================================
-- 3. 智能体对话历史表 (agent_conversation) - 增加对话轮次
-- ------------------------------------------------------------
INSERT INTO `agent_conversation` (`id`, `session_id`, `agent_id`, `user_id`, `query`, `answer`, `conversation_type`, `create_time`, `metadata`) VALUES
('conv-001', 'sess-home-001', 'agent-001-smarthome', 'user-003-tester', '帮我把客厅的灯打开。', '好的，已调用 [智能灯光控制] 插件，客厅灯已开启。', 'chat', '2025-11-23 11:00:00', '{"token_usage": 50}'),
('conv-002', 'sess-home-001', 'agent-001-smarthome', 'user-003-tester', '现在房间里温度是多少？', '正在查询... [室内温度查询] 插件返回：当前室内温度是26.5°C，湿度55%。', 'chat', '2025-11-23 11:01:30', '{"token_usage": 65, "plugin_call": "temperature_sensor"}'),
('conv-003', 'sess-home-002', 'agent-001-smarthome', 'user-003-tester', 'LED灯的手册在哪里？', '根据知识库[智能设备开发文档]，LED灯的手册是 “LED_Manual.pdf”，请查阅。', 'chat', '2025-11-24 10:00:00', '{"token_usage": 80, "kb_hit": "kb-001-dev"}'),
('conv-004', 'sess-home-003', 'agent-001-smarthome', 'user-003-tester', '怎么给设备排除故障？', '请参考知识库 [常见问题解答] 中的 “Troubleshooting.md”，里面提供了详细的故障排除步骤。', 'chat', '2025-11-24 11:00:00', '{"token_usage": 90, "kb_hit": "kb-002-faq"}'),
('conv-005', 'sess-home-003', 'agent-001-smarthome', 'user-003-tester', '把卧室灯调成红色。', '已调用 [智能灯光控制] 插件，卧室灯颜色已调整为红色。', 'chat', '2025-11-24 11:01:30', '{"token_usage": 70, "plugin_call": "led_controller"}');


-- ============================================================
-- 4. 工作流表 (workflow) - 增加一个定时任务工作流
-- ------------------------------------------------------------
INSERT INTO `workflow` (`id`, `name`, `description`, `definition`, `graph_data`, `is_valid`, `status`, `user_id`, `create_time`) VALUES
('wf-001-home-ctrl', '智能家居控制与反馈', '根据用户请求，执行灯光控制或温度查询，并提供知识库支持。', '{"nodes": ["start", "check_intent", "call_plugin", "llm_feedback"], "edges": []}', '{"nodes": [], "edges": []}', TRUE, 'active', 'user-002-home', '2025-11-20 09:00:00'),
('wf-002-auto-off', '定时关闭卧室灯', '每天晚上11点检查卧室灯状态，如果开启则自动关闭。', '{"nodes": ["cron_trigger", "check_status", "call_led_off"], "edges": []}', TRUE, 'active', 'user-002-home', '2025-11-21 15:00:00');


-- ============================================================
-- 5. 工作流执行历史表 (workflow_run) - 增加执行记录
-- ------------------------------------------------------------
INSERT INTO `workflow_run` (`id`, `workflow_id`, `user_id`, `status`, `inputs`, `outputs`, `run_type`, `start_time`, `end_time`, `create_time`) VALUES
('run-001', 'wf-001-home-ctrl', 'user-003-tester', 'completed', '{"intent": "turn_on_light", "location": "living_room"}', '{"result": "success", "plugin_used": "led_controller"}', 'full', '2025-11-23 11:00:00', '2025-11-23 11:00:03', '2025-11-23 11:00:00'),
('run-002', 'wf-001-home-ctrl', 'user-003-tester', 'completed', '{"intent": "get_temperature", "location": "room"}', '{"result": "success", "temperature": 26.5, "plugin_used": "temperature_sensor"}', 'full', '2025-11-23 11:01:30', '2025-11-23 11:01:32', '2025-11-23 11:01:30'),
('run-003', 'wf-002-auto-off', 'user-002-home', 'completed', '{"time": "23:00"}', '{"result": "success", "status": "light already off"}', 'full', '2025-11-23 23:00:00', '2025-11-23 23:00:01', '2025-11-23 23:00:00'),
('run-004', 'wf-001-home-ctrl', 'user-003-tester', 'failed', '{"intent": "turn_on_light", "location": "balcony"}', '{"error": "Device not found"}', 'full', '2025-11-24 12:00:00', '2025-11-24 12:00:05', '2025-11-24 12:00:00');


-- ============================================================
-- 6. 知识库表 (knowledge_base) - 增加第二个知识库
-- ------------------------------------------------------------
INSERT INTO `knowledge_base` (`id`, `name`, `description`, `embedding_model`, `user_id`, `create_time`) VALUES
('kb-001-dev', '智能设备开发文档', '包含LED灯、传感器等设备的API和故障排除文档。', 'text-embedding-3-small', 'user-002-home', '2025-11-10 09:10:00'),
('kb-002-faq', '常见问题解答', '用户对智能家居系统的常见疑问及标准答案。', 'text-embedding-3-small', 'user-001-admin', '2025-11-12 14:00:00');


-- ============================================================
-- 7. 文档表 (document) - 增加文档数量
-- ------------------------------------------------------------
INSERT INTO `document` (`id`, `filename`, `file_name`, `file_url`, `file_path`, `file_size`, `file_type`, `chunk_count`, `status`, `process_status`, `knowledge_base_id`, `kb_id`, `user_id`, `create_time`) VALUES
('doc-001', 'LED_Manual.pdf', 'LED_Manual.pdf', 'http://storage.com/kb-001/led_manual.pdf', '/files/kb-001/led_manual.pdf', 1048576, 'pdf', 30, 'completed', 2, 'kb-001-dev', 'kb-001-dev', 'user-002-home', '2025-11-10 10:30:00'),
('doc-002', 'Temp_Sensor_Spec.txt', 'Temp_Sensor_Spec.txt', 'http://storage.com/kb-001/temp_spec.txt', '/files/kb-001/temp_spec.txt', 50000, 'txt', 10, 'completed', 2, 'kb-001-dev', 'kb-001-dev', 'user-002-home', '2025-11-10 11:00:00'),
('doc-003', 'Troubleshooting.md', 'Troubleshooting.md', 'http://storage.com/kb-002/trouble.md', '/files/kb-002/trouble.md', 256000, 'markdown', 20, 'completed', 2, 'kb-002-faq', 'kb-002-faq', 'user-001-admin', '2025-11-13 09:00:00'),
('doc-004', 'Installation_Guide.pdf', 'Installation_Guide.pdf', 'http://storage.com/kb-002/install.pdf', '/files/kb-002/install.pdf', 524288, 'pdf', 15, 'processing', 1, 'kb-002-faq', 'kb-002-faq', 'user-001-admin', '2025-11-13 10:00:00');


-- ============================================================
-- 8. 插件表 (plugin) - 增加一个第三方插件
-- ------------------------------------------------------------
INSERT INTO `plugin` (`id`, `name`, `identifier`, `description`, `openapi_spec`, `status`, `is_enabled`, `auth_type`, `user_id`, `create_time`) VALUES
('plugin-001-led', '智能灯光控制', 'led_controller', '用于开启、关闭和调整智能LED灯的亮度或颜色。', '{"openapi": "3.0.0", "info": {"title": "LED Control API"}, "paths": {"/light/on": {}, "/light/off": {}}}', 'enabled', TRUE, 'api_key', 'user-002-home', '2025-11-15 10:00:00'),
('plugin-002-temp', '室内温度查询', 'temperature_sensor', '获取当前房间的实时温度和湿度数据。', '{"openapi": "3.0.0", "info": {"title": "Temperature API"}, "paths": {"/sensor/current_temp": {}}}', 'enabled', TRUE, 'none', 'user-002-home', '2025-11-15 11:30:00'),
('plugin-003-calendar', '家庭日程提醒', 'family_calendar', '用于查询和添加家庭共享日历事件。', '{"openapi": "3.0.0", "info": {"title": "Calendar API"}, "paths": {"/events": {}}}', 'disabled', FALSE, 'oauth', 'user-004-dev', '2025-11-18 14:00:00');


-- ============================================================
-- 9. 系统日志表 (system_log) - 增加操作审计和错误日志
-- ------------------------------------------------------------
INSERT INTO `system_log` (`id`, `user_id`, `module`, `action`, `level`, `content`, `create_time`, `request_params`) VALUES
('log-001', 'user-002-home', 'agent', 'create', 'info', '用户 [home_creator] 创建了智能体 [智能家居助理]', '2025-11-22 10:00:00', '{"name": "智能家居助理"}'),
('log-002', 'user-002-home', 'plugin', 'enable', 'info', '用户 [home_creator] 启用了插件 [智能灯光控制]', '2025-11-15 10:05:00', '{"id": "plugin-001-led"}'),
('log-003', 'user-003-tester', 'workflow', 'execute', 'info', '工作流 [智能家居控制与反馈] (run-001) 执行成功', '2025-11-23 11:00:03', '{"run_id": "run-001"}'),
('log-004', 'user-001-admin', 'knowledge_base', 'upload_doc', 'info', '管理员上传了新文档到 [常见问题解答]', '2025-11-13 10:00:00', '{"doc_id": "doc-004"}'),
('log-005', 'user-003-tester', 'workflow', 'execute', 'error', '工作流执行失败: Device not found in location [balcony]', '2025-11-24 12:00:05', '{"run_id": "run-004"}');


-- ============================================================
-- 10. 系统配置表 (system_config) 
-- ------------------------------------------------------------
-- 这些数据已经存在，无需重复插入
/*
INSERT INTO `ai_agent_platform_db`.`system_config` (`id`, `config_key`, `config_value`, `description`) VALUES
('config_001', 'default_model', 'gpt-4', '默认大模型'),
('config_002', 'max_upload_size', '104857600', '最大上传文件大小（字节，默认100MB）'),
('config_003', 'enable_registration', 'true', '是否开放注册');
*/

SET FOREIGN_KEY_CHECKS = 1;

