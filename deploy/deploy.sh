#!/bin/bash

# ===========================
# AI-Agent-Platform 一键部署脚本
# ===========================

echo "🚀 正在进入 deploy 目录..."
cd deploy || exit

echo "🧹 停止运行中的容器..."
docker compose -f docker-compose.prod.yml --env-file .env.prod down

echo "📦 检查并删除旧的 MySQL 数据卷（如存在）..."
docker volume rm deploy_mysql-data 2>/dev/null

echo "🔧 开始重新构建镜像并启动所有服务..."
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

echo "⏳ 等待容器启动..."
sleep 5

echo "📋 当前容器状态："
docker ps

echo "🎉 部署完成！"

echo ""
echo "📌 前端访问地址:  http://localhost"
echo "📌 后端 Swagger: http://localhost:8080/doc.html"
echo ""
echo "如需查看日志："
echo "  docker logs aiagent-frontend"
echo "  docker logs aiagent-backend"
echo "  docker logs aiagent-mysql"
