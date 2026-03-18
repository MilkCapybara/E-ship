#!/bin/bash

# E-ship 船易达 - 快速启动脚本

echo "=========================================="
echo "  E-ship 船易达 - 智慧航运平台"
echo "  快速启动脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查Java
echo -e "${BLUE}[1/4] 检查Java环境...${NC}"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}✓ Java已安装: $JAVA_VERSION${NC}"
else
    echo -e "${RED}✗ 未找到Java，请先安装Java 17或更高版本${NC}"
    exit 1
fi

# 检查Node.js
echo -e "${BLUE}[2/4] 检查Node.js环境...${NC}"
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v)
    echo -e "${GREEN}✓ Node.js已安装: $NODE_VERSION${NC}"
else
    echo -e "${RED}✗ 未找到Node.js，请先安装Node.js 18或更高版本${NC}"
    exit 1
fi

# 检查PostgreSQL
echo -e "${BLUE}[3/4] 检查PostgreSQL...${NC}"
if command -v psql &> /dev/null; then
    echo -e "${GREEN}✓ PostgreSQL已安装${NC}"
else
    echo -e "${YELLOW}⚠ 未找到PostgreSQL，请确保数据库已启动${NC}"
fi

echo ""
echo -e "${BLUE}[4/4] 启动服务...${NC}"
echo ""

# 启动后端
echo -e "${YELLOW}正在启动后端服务...${NC}"
./mvnw spring-boot:run > backend.log 2>&1 &
# ./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.address=0.0.0.0" > backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}✓ 后端服务已启动 (PID: $BACKEND_PID)${NC}"
echo -e "  日志文件: backend.log"
echo -e "  访问地址: http://localhost:3473"

# 等待后端启动
echo ""
echo -e "${YELLOW}等待后端服务就绪...${NC}"
sleep 5

# 启动前端
echo ""
echo -e "${YELLOW}正在启动前端服务...${NC}"
cd frontend
npm run dev > ../frontend.log 2>&1 &
# npm run dev -- --host 0.0.0.0 > ../frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..
echo -e "${GREEN}✓ 前端服务已启动 (PID: $FRONTEND_PID)${NC}"
echo -e "  日志文件: frontend.log"
echo -e "  访问地址: http://localhost:5173"

# 保存PID
echo "$BACKEND_PID" > .backend.pid
echo "$FRONTEND_PID" > .frontend.pid

echo ""
echo "=========================================="
echo -e "${GREEN}✓ 所有服务启动成功！${NC}"
echo "=========================================="
echo ""
echo "访问地址："
echo -e "  前端: ${BLUE}http://localhost:5173${NC}"
echo -e "  后端: ${BLUE}http://localhost:3473${NC}"
echo ""
echo "查看日志："
echo "  后端: tail -f backend.log"
echo "  前端: tail -f frontend.log"
echo ""
echo "停止服务："
echo "  运行: ./stop.sh"
echo ""
echo "=========================================="
