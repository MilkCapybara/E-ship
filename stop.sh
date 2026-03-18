#!/bin/bash

# E-ship 船易达 - 停止脚本

echo "=========================================="
echo "  E-ship 船易达 - 停止服务"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 停止后端
if [ -f .backend.pid ]; then
    BACKEND_PID=$(cat .backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}正在停止后端服务 (PID: $BACKEND_PID)...${NC}"
        kill $BACKEND_PID
        echo -e "${GREEN}✓ 后端服务已停止${NC}"
    else
        echo -e "${YELLOW}后端服务未运行${NC}"
    fi
    rm .backend.pid
else
    echo -e "${YELLOW}未找到后端PID文件${NC}"
fi

# 停止前端
if [ -f .frontend.pid ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}正在停止前端服务 (PID: $FRONTEND_PID)...${NC}"
        kill $FRONTEND_PID
        echo -e "${GREEN}✓ 前端服务已停止${NC}"
    else
        echo -e "${YELLOW}前端服务未运行${NC}"
    fi
    rm .frontend.pid
else
    echo -e "${YELLOW}未找到前端PID文件${NC}"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}✓ 所有服务已停止${NC}"
echo "=========================================="
