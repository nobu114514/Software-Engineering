#!/bin/bash

# 拼夕夕在线购物系统 - Linux启动脚本
# 此脚本用于在Linux环境下启动完整的应用系统

echo "拼夕夕在线购物系统启动脚本"

# 检查Docker是否已安装
if ! command -v docker &> /dev/null; then
    echo "错误: 未找到Docker，请先安装Docker"
    echo "Ubuntu/Debian: sudo apt-get install docker.io"
    echo "CentOS/RHEL: sudo yum install docker"
    exit 1
fi

# 检查docker-compose是否已安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误: 未找到docker-compose，请先安装"
    echo "安装命令: sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && sudo chmod +x /usr/local/bin/docker-compose"
    exit 1
fi

# 检查Docker服务是否正在运行
if ! docker info &> /dev/null; then
    echo "错误: Docker服务未运行，请启动Docker服务"
    echo "启动命令: sudo systemctl start docker"
    exit 1
fi

echo "准备启动拼夕夕在线购物系统..."
echo "1. 正在拉取所需镜像..."
docker-compose pull
echo "2. 正在构建应用..."
docker-compose build
echo "3. 正在启动系统..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo ""
    echo "系统启动成功！"
    echo "    前端访问地址: http://localhost"
    echo "    后端API地址: http://localhost:8081"
    echo "    数据库地址: localhost:3306 (用户名: root, 密码: root, 数据库: simple_shop)"
    echo ""
    echo "查看日志命令: docker-compose logs -f"
    echo "停止系统命令: docker-compose down"
    echo ""
else
    echo "系统启动失败，请检查错误信息"
fi