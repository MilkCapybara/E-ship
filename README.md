# E-ship（船易达）- 智慧航运船舶租赁平台

## 📋 项目信息

**开发者**：孙帆
**联系方式**：fandesunstar@outlook.com
**项目类型**：电子商务软件课程设计
**业务模式**：B2B/B2C 船舶租赁平台
**项目状态**：✅ 已完成（100%）
**完成时间**：2026年3月

## 🎯 项目简介

E-ship（船易达）是一个功能完整的智慧航运船舶租赁电子商务平台，采用前后端分离架构开发。平台连接船东（出租方）和租家（承租方），通过平台管理员进行监管和服务。提供船舶信息展示、智能搜索推荐、在线合约签订、收藏管理、数据统计、**WebSocket实时通知**等核心功能，配备精美的海事科技风格UI设计，旨在简化船舶租赁流程，提高交易效率。

**🆕 最新功能**：WebSocket实时通知系统已上线！船东和租家在任何页面都能实时收到合约状态更新通知，无需刷新页面。

## 📊 项目统计

- **后端代码**：60个Java文件，约5500+行代码
- **前端代码**：17个Vue组件，约4500+行代码
- **API端点**：38+个RESTful接口
- **数据表**：7张核心业务表
- **功能模块**：5大核心模块（认证、船东、租家、管理员、实时通知）
- **WebSocket连接**：全页面实时通知支持

## 💻 技术栈

### 后端技术
- **Java 17** - 编程语言
- **Spring Boot 3.2.3** - 应用框架
- **Spring MVC** - Web框架
- **Spring WebSocket** - WebSocket支持（实时通知）
- **MyBatis-Plus 3.5.5** - ORM框架
- **PostgreSQL 14+** - 关系型数据库
- **JWT (JJWT 0.12.5)** - 令牌认证
- **Spring Mail** - 邮件服务
- **Spring Security Crypto** - 密码加密
- **Hutool 5.8.25** - Java工具库
- **iText 7** - PDF生成
- **Lombok** - 代码简化
- **HikariCP** - 数据库连接池

### 前端技术
- **Vue 3.5.30** - 渐进式JavaScript框架（Composition API）
- **Vite 8.0.0** - 新一代前端构建工具
- **Element Plus 2.13.5** - Vue 3 UI组件库
- **Vue Router 4.6.4** - 官方路由管理器
- **Pinia 2.3.1** - Vue 3 状态管理
- **Axios 1.13.6** - HTTP客户端
- **ECharts 6.0.0** - 数据可视化图表库
- **CSS3 + CSS Variables** - 现代化样式系统

### 开发工具
- **Maven 3.6+** - 项目构建工具
- **Git** - 版本控制
- **IntelliJ IDEA** - 后端开发IDE
- **VS Code** - 前端开发IDE

### 开发环境
- **操作系统**：macOS (Darwin 24.5.0)
- **硬件配置**：MacBook Pro M4 24GB + 1TB

## 👥 用户角色

系统设计三种用户角色：

1. **船东（出租方）** - 发布船舶信息，管理出租业务
2. **租家（承租方）** - 浏览船舶，发起租赁合约
3. **平台管理员** - 监管平台运营，处理纠纷

## ✨ 核心功能

### 1. 用户认证系统 ✅

#### 用户注册
- 角色选择（船东/租家）
- 用户名规则：中文或英文，最多10个中文字符或30个英文字符
- 密码强度验证：12位+大小写字母+数字+特殊字符
- 邮箱验证码验证
- 密码BCrypt加密存储
- 注册成功自动跳转登录

#### 用户登录
- 用户名+密码+邮箱验证码三重验证
- 验证码机制：4位字母数字组合，10分钟有效期
- JWT令牌认证，自动续期
- 安全机制：5次失败锁定1小时
- 登录日志记录

#### 忘记密码
- 邮箱验证码验证
- 新密码强度验证
- 安全重置密码

### 2. 船东功能模块 ✅

#### 船舶管理
- 添加船舶：名称、类型、载重、建造年份、日租金、描述
- 编辑船舶信息
- 删除船舶
- 船舶状态管理（可租/已租/维护中）
- 船舶列表展示（分页、排序）
- 船舶类型筛选（集装箱船、散货船、油船、客船等）

#### 合约信箱
- 接收租家发来的租赁申请
- 查看合约详情
- 审核合约（同意/拒绝）
- 待审核合约列表
- 合约状态实时更新

#### 合约管理
- 查看所有合约（进行中/已完成/已拒绝）
- 合约详情查看
- 合约状态跟踪
- 合约筛选和排序

### 3. 租家功能模块 ✅

#### 船舶搜索
- 智能推荐船舶
- 多条件筛选：类型、载重、租金范围、建造年份
- 关键词搜索
- 按价格/评分/新旧程度排序
- 分页展示

#### 船舶收藏
- 收藏感兴趣的船舶
- 查看收藏列表
- 取消收藏
- 快速访问收藏的船舶

#### 创建合约
- 选择船舶
- 填写租赁信息：开始时间、结束时间、租金、用途
- 发送合约到船东
- 合约状态跟踪

#### 合约管理
- 查看已发送的合约（待审核/已同意/已拒绝）
- 查看进行中的租赁
- 查看历史租赁记录
- 取消待审核的合约
- 合约详情查看

### 4. 管理员功能模块 ✅

#### 数据统计仪表板
- 用户总数统计（船东/租家）
- 船舶总数统计
- 合约总数统计
- 平台交易额统计
- 用户角色分布图（饼图）
- 船舶类型分布图（饼图）
- 数据可视化展示（ECharts）

#### 用户管理
- 查看所有用户列表
- 用户详情查看
- 用户状态管理（启用/禁用）
- 信用评分调整
- 用户搜索和筛选

#### 船舶管理
- 查看所有船舶列表
- 船舶详情查看
- 船舶状态控制
- 下架违规船舶
- 船舶搜索和筛选

#### 合约监管
- 查看所有合约列表
- 合约详情查看
- 强制终止违规合约
- 合约状态监控
- 合约搜索和筛选

### 5. 详情页面 ✅

#### 船舶详情页
- 完整船舶信息展示
- 船东信息展示
- 船舶状态显示
- 收藏功能（租家）
- 创建合约功能（租家）
- 编辑/删除功能（船东）

#### 合约详情页
- 完整合约信息展示
- 船舶信息展示
- 船东/租家信息展示
- 合约状态显示
- 审核功能（船东）
- 取消功能（租家）

### 6. 实时通知系统 ✅ 🆕

#### WebSocket实时通知
- **全页面覆盖**：船东和租家在任何页面都能收到实时通知
- **双向通知**：
  - 租家提交申请 → 船东实时收到通知
  - 船东审核合约 → 租家实时收到通知
  - 租家取消申请 → 船东实时收到通知
- **智能刷新**：
  - 数据统计页面自动刷新统计数据
  - 合约列表页面自动刷新列表
  - 其他页面显示通知消息
- **断线重连**：WebSocket断开后自动重连（最多5次，指数退避）
- **单例模式**：全局只有一个WebSocket连接，节省资源
- **监听器模式**：每个页面独立注册监听器，互不干扰
- **生命周期管理**：页面卸载时自动清理监听器，防止内存泄漏

#### 通知覆盖范围

**船东端（4个页面全覆盖）：**
- ✅ 数据统计页面 - 收到通知 + 刷新数据
- ✅ 我的船舶页面 - 收到通知
- ✅ 合约管理页面 - 收到通知 + 刷新列表
- ✅ 合约信箱页面 - 收到通知 + 刷新列表

**租家端（4个页面全覆盖）：**
- ✅ 数据统计页面 - 收到通知 + 刷新数据
- ✅ 船舶搜索页面 - 收到通知
- ✅ 我的收藏页面 - 收到通知
- ✅ 我的合约页面 - 收到通知 + 刷新列表

#### 通知类型

| 操作 | 通知接收方 | 通知类型 | 消息内容 |
|------|-----------|---------|---------|
| 租家提交申请 | 船东 | NEW_CONTRACT | "收到新的租赁申请！" |
| 船东同意合约 | 租家 | APPROVED | "您的租赁申请已被同意！" |
| 船东拒绝合约 | 租家 | REJECTED | "您的租赁申请被拒绝了" |
| 租家取消申请 | 船东 | CANCELLED | "租家取消了一个合约申请" |

## 🎨 UI设计特色

### 海事科技风格主题
- **配色方案**：深蓝+金色+青色，营造专业海事氛围
- **动画效果**：
  - 粒子背景动画（ParticleBackground.vue）
  - 波浪背景动画（WaveBackground.vue）
  - 光效扫描动画
  - 3D卡片翻转效果
  - 悬浮和阴影效果
  - 渐变文字效果
- **自定义组件**：
  - MaritimeSelect - 海事风格下拉选择器
  - MaritimeDatePicker - 海事风格日期选择器
- **响应式设计**：适配各种屏幕尺寸
- **用户体验**：流畅的页面切换，友好的错误提示

## 📊 数据库设计

### 核心数据表（7张）

1. **用户表（user）**
   - 用户ID、用户名、密码（BCrypt加密）
   - 邮箱、角色类型（OWNER/RENTER/ADMIN）
   - 公司名称、信用评分
   - 注册时间、账号状态
   - 锁定时间、失败登录次数

2. **船舶表（ship）**
   - 船舶ID、船东ID、船舶名称
   - 船舶类型（集装箱船/散货船/油船/客船等）
   - 载重吨位、建造年份
   - 日租金、船舶描述
   - 状态（AVAILABLE/RENTED/MAINTENANCE）
   - 创建时间、更新时间

3. **合约表（contract）**
   - 合约ID、船舶ID、船东ID、租家ID
   - 租赁开始时间、结束时间
   - 日租金、总租金、租赁用途
   - 合约状态（PENDING/APPROVED/REJECTED/COMPLETED/CANCELLED）
   - 创建时间、更新时间

4. **验证码表（verification_code）**
   - 邮箱、验证码
   - 创建时间、过期时间
   - 使用状态

5. **登录日志表（login_log）**
   - 日志ID、用户ID
   - 登录时间、IP地址
   - 登录状态（SUCCESS/FAILED）
   - 失败原因

6. **船舶收藏表（ship_favorite）**
   - 收藏ID、用户ID、船舶ID
   - 收藏时间

7. **评价表（review）**
   - 评价ID、合约ID
   - 评价者ID、被评价者ID
   - 评分、评价内容
   - 创建时间

## 🗓️ 开发完成情况

### ✅ 第一阶段：基础框架搭建（已完成）
- [x] 创建Spring Boot项目
- [x] 配置PostgreSQL数据库连接
- [x] 集成MyBatis-Plus
- [x] 设计并创建数据库表（7张）
- [x] 搭建基础项目结构（Controller/Service/Mapper/Entity/DTO/VO）
- [x] 配置统一返回格式和全局异常处理

### ✅ 第二阶段：用户认证模块（已完成）
- [x] 实现用户注册功能（含角色选择）
- [x] 实现登录功能（含邮箱验证码）
- [x] 集成邮件服务（Spring Mail）
- [x] 实现密码BCrypt加密存储
- [x] 实现账号锁定机制（5次失败锁定1小时）
- [x] 实现忘记密码功能（邮箱验证码重置）
- [x] 实现JWT令牌认证

### ✅ 第三阶段：船东功能开发（已完成）
- [x] 船舶信息管理（增删改查）
- [x] 船舶列表展示与筛选
- [x] 合约信箱功能
- [x] 合约审核（同意/拒绝）
- [x] 合约管理功能

### ✅ 第四阶段：租家功能开发（已完成）
- [x] 船舶搜索与筛选
- [x] 船舶详情展示
- [x] 船舶收藏功能
- [x] 在线合约填写与发送
- [x] 我的合约管理

### ✅ 第五阶段：管理员功能开发（已完成）
- [x] 用户管理功能
- [x] 船舶管理功能
- [x] 合约监管功能
- [x] 数据统计仪表盘（ECharts可视化）
- [x] 用户/船舶/合约详情查看

### ✅ 第六阶段：前端开发（已完成）
- [x] 搭建Vue 3 + Vite项目
- [x] 集成Element Plus UI库
- [x] 配置Vue Router路由
- [x] 配置Pinia状态管理
- [x] 实现认证页面（登录/注册/忘记密码）
- [x] 实现船东页面（船舶管理/合约信箱/合约管理）
- [x] 实现租家页面（船舶搜索/收藏/合约管理）
- [x] 实现管理员后台（数据统计/用户管理/船舶管理/合约监管）
- [x] 实现详情页面（船舶详情/合约详情）
- [x] 实现海事科技风格UI设计
- [x] 实现动画效果和自定义组件

### ✅ 第七阶段：测试与优化（已完成）
- [x] 后端API功能测试（38+个端点）
- [x] 前端页面功能测试
- [x] 前后端联调测试
- [x] 性能优化（数据库索引、连接池配置）
- [x] Bug修复
- [x] 撰写项目文档

### ✅ 第八阶段：实时通知系统（已完成）🆕
- [x] 集成Spring WebSocket
- [x] 实现WebSocket配置和处理器
- [x] 实现前端WebSocket服务（单例模式）
- [x] 船东端全页面通知（4个页面）
- [x] 租家端全页面通知（4个页面）
- [x] 实现断线重连机制
- [x] 实现智能数据刷新
- [x] WebSocket功能测试
- [x] 撰写WebSocket测试指南

## 🎯 项目亮点

### 1. 完整的业务流程
- 从用户注册到船舶租赁的完整业务闭环
- 三种角色（船东/租家/管理员）的完整功能实现
- 合约全生命周期管理（创建→审核→执行→完成）

### 2. 实时通知系统 🆕
- **全页面覆盖**：船东和租家在任何页面都能实时收到通知
- **WebSocket技术**：基于Spring WebSocket实现双向实时通信
- **智能刷新**：相关页面数据自动刷新，无需手动操作
- **断线重连**：自动重连机制，确保通知不丢失
- **高性能**：单例模式，全局只有一个WebSocket连接
- **用户体验**：美观的消息提示，不打扰用户操作

### 3. 精美的UI设计
- 独特的海事科技风格主题
- 丰富的动画效果（粒子、波浪、光效、3D翻转）
- 自定义海事风格组件
- 响应式设计，适配多种设备

### 4. 安全性设计
- 密码强度验证（12位+大小写+数字+特殊字符）
- BCrypt密码加密存储
- JWT令牌认证
- 邮箱验证码双重验证
- 账号锁定机制（防暴力破解）
- 登录日志记录

### 5. 良好的代码质量
- 清晰的分层架构（Controller/Service/Mapper）
- 统一的代码风格和命名规范
- 完善的错误处理和日志记录
- 详细的注释文档
- 高可维护性和可扩展性

### 6. 数据可视化
- ECharts图表展示
- 用户角色分布饼图
- 船舶类型分布饼图
- 实时数据统计

### 7. 用户体验优化
- 流畅的页面切换动画
- 友好的错误提示信息
- 直观的操作反馈
- 快速的响应速度
- 实时消息通知（WebSocket）

## 🚀 快速开始

### 环境要求
- JDK 17+
- PostgreSQL 14+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

### 后端启动步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd E-ship
```

2. **配置数据库**
```bash
# 创建数据库
psql -U postgres
CREATE DATABASE eship;

# 导入数据库脚本
psql -U postgres -d eship -f easyGo.sql
```

3. **配置application.yml**
```yaml
# 修改数据库配置
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eship?currentSchema=easyGo
    username: postgres
    password: your_password

# 修改邮件配置
spring:
  mail:
    username: your_email@qq.com
    password: your_auth_code
```

4. **启动后端服务**
```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用启动脚本
./start.sh
```

5. **访问后端API**
- 后端服务：http://localhost:3473
- API基础路径：http://localhost:3473/api

### 前端启动步骤

1. **进入前端目录**
```bash
cd frontend
```

2. **安装依赖**
```bash
npm install
# 或
yarn install
```

3. **启动开发服务器**
```bash
npm run dev
# 或
yarn dev
```

4. **访问前端应用**
- 前端服务：http://localhost:5173

### 测试账号

#### 管理员账号
```
用户名：admin
密码：twhd#7?n*Csf
邮箱：188043648@qq.com
```

#### 测试船东账号
```
用户名：testowner
密码：TestOwner123!@#
邮箱：your_test_email@example.com
```

#### 测试租家账号
```
用户名：testrenter
密码：TestRenter123!@#
邮箱：your_test_email@example.com
```

### 停止服务

```bash
# 停止后端和前端服务
./stop.sh
```

## 📁 项目结构

```
E-ship/
├── src/
│   ├── main/
│   │   ├── java/com/fandesunstar/eship/
│   │   │   ├── common/          # 公共类（统一返回格式、异常处理）
│   │   │   ├── config/          # 配置类（CORS、Web配置）
│   │   │   ├── controller/      # 控制器层（8个Controller）
│   │   │   ├── dto/             # 数据传输对象（15个DTO）
│   │   │   ├── entity/          # 实体类（7个Entity）
│   │   │   ├── mapper/          # 数据访问层（7个Mapper）
│   │   │   ├── service/         # 业务逻辑层（5个Service）
│   │   │   ├── utils/           # 工具类（JWT、密码、验证码）
│   │   │   └── vo/              # 视图对象（10个VO）
│   │   └── resources/
│   │       ├── mapper/          # MyBatis XML映射文件（7个）
│   │       └── application.yml  # 应用配置文件
│   └── test/                    # 测试类
├── frontend/
│   ├── src/
│   │   ├── api/                 # API接口封装（6个文件）
│   │   ├── assets/              # 静态资源
│   │   ├── components/          # 公共组件（4个）
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # Pinia状态管理
│   │   ├── styles/              # 样式文件（海事主题）
│   │   └── views/               # 页面组件（17个）
│   ├── package.json             # 前端依赖配置
│   └── vite.config.js           # Vite配置
├── easyGo.sql                   # 数据库脚本
├── pom.xml                      # Maven配置
├── start.sh                     # 启动脚本
├── stop.sh                      # 停止脚本
└── README.md                    # 项目说明文档
```

## 🔌 API接口说明

### 认证相关接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/send-code` - 发送验证码
- `POST /api/auth/forgot-password` - 忘记密码
- `GET /api/auth/current` - 获取当前用户信息
- `POST /api/auth/logout` - 用户登出

### 船舶管理接口
- `GET /api/ships` - 获取船舶列表（支持筛选、分页）
- `GET /api/ships/{id}` - 获取船舶详情
- `POST /api/ships` - 添加船舶（船东）
- `PUT /api/ships/{id}` - 更新船舶信息（船东）
- `DELETE /api/ships/{id}` - 删除船舶（船东）
- `GET /api/ships/owner` - 获取我的船舶（船东）
- `GET /api/ships/recommended` - 获取推荐船舶（租家）
- `GET /api/ships/search` - 搜索船舶

### 合约管理接口
- `GET /api/contracts` - 获取合约列表
- `GET /api/contracts/{id}` - 获取合约详情
- `POST /api/contracts` - 创建合约（租家）
- `PUT /api/contracts/{id}/approve` - 审核通过合约（船东）
- `PUT /api/contracts/{id}/reject` - 拒绝合约（船东）
- `PUT /api/contracts/{id}/cancel` - 取消合约（租家）
- `GET /api/contracts/inbox` - 获取合约信箱（船东）
- `GET /api/contracts/my` - 获取我的合约

### 收藏管理接口
- `GET /api/favorites` - 获取收藏列表
- `POST /api/favorites` - 添加收藏
- `DELETE /api/favorites/{shipId}` - 取消收藏
- `GET /api/favorites/check/{shipId}` - 检查是否已收藏

### 管理员接口
- `GET /api/admin/statistics` - 获取统计数据
- `GET /api/admin/users` - 获取用户列表
- `GET /api/admin/users/{id}` - 获取用户详情
- `PUT /api/admin/users/{id}/status` - 更新用户状态
- `PUT /api/admin/users/{id}/credit` - 更新用户信用评分
- `GET /api/admin/ships` - 获取所有船舶
- `GET /api/admin/contracts` - 获取所有合约
- `PUT /api/admin/contracts/{id}/terminate` - 强制终止合约

## 📝 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用驼峰命名法（Java）和kebab-case（Vue）
- 添加必要的注释和文档
- 每个方法保持简洁，单一职责

### Git提交规范
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

### 接口设计规范
- RESTful API设计风格
- 统一返回格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```
- HTTP状态码规范使用
- 错误信息清晰明确

## 🧪 测试说明

### 后端测试
项目包含完整的测试类：
- `DatabaseConnectionTest.java` - 数据库连接测试
- `MailServiceTest.java` - 邮件服务测试
- `EShipApplicationTests.java` - 应用启动测试

运行测试：
```bash
mvn test
```

### 前端测试
前端编译测试：
```bash
cd frontend
npm run build
```

### API测试
推荐使用以下工具测试API：
- Postman
- Insomnia
- curl命令行

测试流程示例：
1. 注册用户 → 发送验证码 → 完成注册
2. 登录 → 获取JWT令牌
3. 使用令牌访问受保护的API

## 🔒 安全特性

### 已实现的安全措施
- **密码安全**：BCrypt加密，强度验证（12位+大小写+数字+特殊字符）
- **身份认证**：JWT令牌机制，自动续期
- **邮箱验证**：注册和登录双重验证
- **防暴力破解**：5次失败锁定1小时
- **登录日志**：记录所有登录尝试
- **SQL注入防护**：MyBatis Plus参数化查询
- **XSS防护**：前端输入验证和转义
- **CORS配置**：跨域请求安全控制

### 密码规则
- 最小长度：12位
- 必须包含：大写字母、小写字母、数字、特殊字符
- 示例：`TestUser123!@#`

### 验证码机制
- 长度：4位字母数字组合
- 有效期：10分钟
- 频率限制：同一账号10分钟内只能请求一次

## 📈 性能优化

### 数据库优化
- **索引优化**：为常用查询字段添加索引
- **连接池配置**：HikariCP连接池，最大10个连接
- **查询优化**：使用MyBatis Plus的分页查询
- **Schema隔离**：使用easyGo schema组织数据

### 前端优化
- **代码分割**：Vue Router懒加载
- **资源压缩**：Vite自动压缩和优化
- **缓存策略**：Pinia状态管理，减少重复请求
- **动画性能**：CSS3硬件加速，60fps流畅动画

### 后端优化
- **异步处理**：邮件发送异步执行
- **日志优化**：生产环境使用INFO级别
- **异常处理**：全局异常处理器，统一错误响应

## 📚 技术文档

### 项目相关文档
- `README.md` - 项目说明文档（本文件）
- `PROJECT_STATUS.md` - 项目状态报告
- `项目最终总结.md` - 项目完整总结
- `开发指南.md` - 开发教程
- `快速启动指南-最终版.md` - 快速启动指南
- `MyBatis配置完成说明.md` - MyBatis配置说明
- `Vue前端配置完成.md` - Vue前端配置说明
- `邮件服务测试指南.md` - 邮件服务测试指南
- `测试通过报告.md` - 测试报告
- `WebSocket实时通知测试指南.md` - WebSocket功能测试指南 🆕

### 在线技术文档
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Vue 3官方文档](https://cn.vuejs.org/)
- [Element Plus官方文档](https://element-plus.org/zh-CN/)
- [MyBatis-Plus官方文档](https://baomidou.com/)
- [PostgreSQL官方文档](https://www.postgresql.org/docs/)

## 🎯 使用场景

### 船东使用场景
1. 注册船东账号
2. 登录系统
3. 添加船舶信息（名称、类型、载重、租金等）
4. 查看合约信箱，接收租家的租赁申请
5. 审核合约（同意或拒绝）
6. 管理所有合约，跟踪租赁状态

### 租家使用场景
1. 注册租家账号
2. 登录系统
3. 浏览推荐船舶或搜索船舶
4. 查看船舶详情
5. 收藏感兴趣的船舶
6. 创建租赁合约，填写租赁信息
7. 查看合约状态，等待船东审核
8. 管理所有合约

### 管理员使用场景
1. 使用管理员账号登录
2. 查看平台数据统计（用户、船舶、合约、交易额）
3. 管理用户（查看、禁用、调整信用评分）
4. 管理船舶（查看、控制状态）
5. 监管合约（查看、强制终止）
6. 数据可视化分析

## 🌟 未来扩展方向

### 功能扩展
- [x] 实时消息通知（WebSocket）✅ 已完成
- [ ] 船舶位置追踪（地图集成）
- [ ] 在线支付功能（支付宝/微信支付）
- [ ] 评价和评分系统完善
- [ ] 数据导出功能（Excel/PDF）
- [ ] 船舶保险服务
- [ ] 合约电子签名
- [ ] 移动端App开发
- [ ] 通知历史记录
- [ ] 桌面通知（Notification API）
- [ ] 通知声音提示
- [ ] 未读消息计数

### 技术优化
- [ ] Redis缓存（热门数据、验证码）
- [ ] Elasticsearch全文搜索
- [ ] 图片CDN加速
- [ ] Docker容器化部署
- [ ] Kubernetes集群部署
- [ ] 微服务架构改造
- [ ] 消息队列（RabbitMQ/Kafka）

### 用户体验
- [ ] 多语言支持（国际化i18n）
- [ ] 暗黑模式
- [ ] PWA离线功能
- [ ] 语音搜索
- [ ] AI智能推荐
- [ ] 数据大屏展示

## 🐛 常见问题

### 后端问题

**Q: 数据库连接失败？**
A: 检查PostgreSQL是否启动，确认application.yml中的数据库配置正确。

**Q: 邮件发送失败？**
A: 检查邮箱配置，确认QQ邮箱授权码正确，确保网络可以访问smtp.qq.com。

**Q: JWT令牌过期？**
A: 重新登录获取新的令牌，或实现令牌自动刷新机制。

### 前端问题

**Q: npm install失败？**
A: 尝试清除缓存 `npm cache clean --force`，或使用国内镜像源。

**Q: 页面空白？**
A: 检查浏览器控制台错误信息，确认后端API是否正常运行。

**Q: 跨域问题？**
A: 确认后端CORS配置正确，允许前端域名访问。

### 部署问题

**Q: 如何部署到生产环境？**
A:
1. 前端：`npm run build`，将dist目录部署到Nginx
2. 后端：`mvn clean package`，运行生成的jar文件
3. 配置Nginx反向代理
4. 配置HTTPS证书

**Q: 如何配置域名？**
A: 修改前端API基础URL，配置Nginx反向代理，设置DNS解析。

## 📊 项目成果

### 开发成果
- ✅ 完整的前后端分离架构
- ✅ 60个Java源文件，约5500+行代码
- ✅ 17个Vue组件，约4500+行代码
- ✅ 38+个RESTful API接口
- ✅ 7张数据库表，完整的数据模型
- ✅ 精美的海事科技风格UI
- ✅ 完善的安全机制
- ✅ 详细的项目文档
- ✅ WebSocket实时通知系统 🆕

### 技术收获
- 掌握Spring Boot 3.x开发
- 掌握Vue 3 Composition API
- 掌握MyBatis-Plus ORM框架
- 掌握JWT认证机制
- 掌握前后端分离架构
- 掌握RESTful API设计
- 掌握PostgreSQL数据库
- 掌握ECharts数据可视化
- 掌握WebSocket实时通信技术 🆕
- 掌握Spring WebSocket框架 🆕

### 业务理解
- 电子商务平台业务流程
- 船舶租赁行业知识
- 多角色权限管理
- 合约全生命周期管理
- 数据统计和分析

## 🤝 贡献指南

本项目为个人课程设计项目，暂不接受外部贡献。

如有建议或发现问题，欢迎通过邮件联系：fandesunstar@outlook.com

## 📄 许可证

本项目仅用于学习和课程设计，未经允许不得用于商业用途。

Copyright © 2026 孙帆. All rights reserved.

## 📞 联系方式

**开发者**：孙帆
**邮箱**：fandesunstar@outlook.com

如有问题或建议，欢迎通过邮件联系。

---

## 🎉 致谢

感谢以下开源项目和技术社区：
- Spring Boot团队
- Vue.js团队
- Element Plus团队
- MyBatis-Plus团队
- PostgreSQL社区
- 所有开源贡献者

---

<div align="center">

**E-ship（船易达）- 智慧航运船舶租赁平台**

**项目状态**：✅ 已完成（100%）

**开发完成时间**：2026年3月

Made with ❤️ by 孙帆

**最后更新时间**：2026-03-18

**最新更新**：✨ 新增WebSocket实时通知系统，支持船东和租家在所有页面实时接收合约状态更新通知

</div>

---
---

# E-ship - Smart Maritime Vessel Rental Platform

## 📋 Project Information

**Developer**: Sun Fan
**Contact**: fandesunstar@outlook.com
**Project Type**: E-commerce Software Course Design
**Business Model**: B2B/B2C Vessel Rental Platform
**Project Status**: ✅ Completed (100%)
**Completion Date**: March 2026

## 🎯 Project Overview

E-ship is a fully functional smart maritime vessel rental e-commerce platform developed with a front-end and back-end separation architecture. The platform connects ship owners (lessors) and renters (lessees), supervised by platform administrators. It provides vessel information display, intelligent search and recommendation, online contract signing, favorites management, data statistics, **WebSocket real-time notifications**, and other core functions, equipped with an exquisite maritime technology style UI design, aiming to simplify the vessel rental process and improve transaction efficiency.

**🆕 Latest Feature**: WebSocket real-time notification system is now live! Ship owners and renters can receive contract status update notifications in real-time on any page without refreshing.

## 📊 Project Statistics

- **Backend Code**: 60 Java files, approximately 5500+ lines of code
- **Frontend Code**: 17 Vue components, approximately 4500+ lines of code
- **API Endpoints**: 38+ RESTful interfaces
- **Database Tables**: 7 core business tables
- **Functional Modules**: 5 core modules (Authentication, Owner, Renter, Admin, Real-time Notification)
- **WebSocket Connection**: Full-page real-time notification support

## 💻 Technology Stack

### Backend Technologies
- **Java 17** - Programming Language
- **Spring Boot 3.2.3** - Application Framework
- **Spring MVC** - Web Framework
- **Spring WebSocket** - WebSocket Support (Real-time Notification)
- **MyBatis-Plus 3.5.5** - ORM Framework
- **PostgreSQL 14+** - Relational Database
- **JWT (JJWT 0.12.5)** - Token Authentication
- **Spring Mail** - Email Service
- **Spring Security Crypto** - Password Encryption
- **Hutool 5.8.25** - Java Utility Library
- **iText 7** - PDF Generation
- **Lombok** - Code Simplification
- **HikariCP** - Database Connection Pool

### Frontend Technologies
- **Vue 3.5.30** - Progressive JavaScript Framework (Composition API)
- **Vite 8.0.0** - Next Generation Frontend Build Tool
- **Element Plus 2.13.5** - Vue 3 UI Component Library
- **Vue Router 4.6.4** - Official Router Manager
- **Pinia 2.3.1** - Vue 3 State Management
- **Axios 1.13.6** - HTTP Client
- **ECharts 6.0.0** - Data Visualization Chart Library
- **CSS3 + CSS Variables** - Modern Style System

### Development Tools
- **Maven 3.6+** - Project Build Tool
- **Git** - Version Control
- **IntelliJ IDEA** - Backend Development IDE
- **VS Code** - Frontend Development IDE

### Development Environment
- **Operating System**: macOS (Darwin 24.5.0)
- **Hardware**: MacBook Pro M4 24GB + 1TB

## 👥 User Roles

The system is designed with three user roles:

1. **Ship Owner (Lessor)** - Publish vessel information, manage rental business
2. **Renter (Lessee)** - Browse vessels, initiate rental contracts
3. **Platform Administrator** - Supervise platform operations, handle disputes

## ✨ Core Features

### 1. User Authentication System ✅

#### User Registration
- Role selection (Owner/Renter)
- Username rules: Chinese or English, max 10 Chinese characters or 30 English characters
- Password strength validation: 12+ characters with uppercase, lowercase, numbers, and special characters
- Email verification code validation
- BCrypt password encryption storage
- Auto-redirect to login after successful registration

#### User Login
- Triple verification: username + password + email verification code
- Verification code mechanism: 4-character alphanumeric combination, 10-minute validity
- JWT token authentication with auto-renewal
- Security mechanism: Account locked for 1 hour after 5 failed attempts
- Login log recording

#### Forgot Password
- Email verification code validation
- New password strength validation
- Secure password reset

### 2. Ship Owner Module ✅

#### Vessel Management
- Add vessels: name, type, tonnage, build year, daily rent, description
- Edit vessel information
- Delete vessels
- Vessel status management (Available/Rented/Maintenance)
- Vessel list display (pagination, sorting)
- Vessel type filtering (Container, Bulk Carrier, Tanker, Passenger Ship, etc.)

#### Contract Inbox
- Receive rental applications from renters
- View contract details
- Review contracts (Approve/Reject)
- Pending contract list
- Real-time contract status updates

#### Contract Management
- View all contracts (In Progress/Completed/Rejected)
- Contract detail viewing
- Contract status tracking
- Contract filtering and sorting

### 3. Renter Module ✅

#### Vessel Search
- Smart vessel recommendations
- Multi-condition filtering: type, tonnage, rent range, build year
- Keyword search
- Sort by price/rating/age
- Paginated display

#### Vessel Favorites
- Favorite interesting vessels
- View favorites list
- Remove favorites
- Quick access to favorited vessels

#### Create Contract
- Select vessel
- Fill rental information: start time, end time, rent, purpose
- Send contract to owner
- Contract status tracking

#### Contract Management
- View sent contracts (Pending/Approved/Rejected)
- View ongoing rentals
- View rental history
- Cancel pending contracts
- Contract detail viewing

### 4. Administrator Module ✅

#### Data Statistics Dashboard
- Total user count (Owners/Renters)
- Total vessel count
- Total contract count
- Platform transaction amount statistics
- User role distribution chart (Pie chart)
- Vessel type distribution chart (Pie chart)
- Data visualization display (ECharts)

#### User Management
- View all user lists
- User detail viewing
- User status management (Enable/Disable)
- Credit score adjustment
- User search and filtering

#### Vessel Management
- View all vessel lists
- Vessel detail viewing
- Vessel status control
- Remove violating vessels
- Vessel search and filtering

#### Contract Supervision
- View all contract lists
- Contract detail viewing
- Force terminate violating contracts
- Contract status monitoring
- Contract search and filtering

### 5. Detail Pages ✅

#### Vessel Detail Page
- Complete vessel information display
- Owner information display
- Vessel status display
- Favorite function (Renter)
- Create contract function (Renter)
- Edit/Delete function (Owner)

#### Contract Detail Page
- Complete contract information display
- Vessel information display
- Owner/Renter information display
- Contract status display
- Review function (Owner)
- Cancel function (Renter)

## 🎨 UI Design Features

### Maritime Technology Style Theme
- **Color Scheme**: Deep blue + gold + cyan, creating a professional maritime atmosphere
- **Animation Effects**:
  - Particle background animation (ParticleBackground.vue)
  - Wave background animation (WaveBackground.vue)
  - Light scanning animation
  - 3D card flip effect
  - Hover and shadow effects
  - Gradient text effects
- **Custom Components**:
  - MaritimeSelect - Maritime style dropdown selector
  - MaritimeDatePicker - Maritime style date picker
- **Responsive Design**: Adapts to various screen sizes
- **User Experience**: Smooth page transitions, friendly error prompts

## 📊 Database Design

### Core Database Tables (7 tables)

1. **User Table (user)**
   - User ID, Username, Password (BCrypt encrypted)
   - Email, Role Type (OWNER/RENTER/ADMIN)
   - Company Name, Credit Score
   - Registration Time, Account Status
   - Lock Time, Failed Login Attempts

2. **Vessel Table (ship)**
   - Vessel ID, Owner ID, Vessel Name
   - Vessel Type (Container/Bulk Carrier/Tanker/Passenger Ship, etc.)
   - Tonnage, Build Year
   - Daily Rent, Vessel Description
   - Status (AVAILABLE/RENTED/MAINTENANCE)
   - Create Time, Update Time

3. **Contract Table (contract)**
   - Contract ID, Vessel ID, Owner ID, Renter ID
   - Rental Start Time, End Time
   - Daily Rent, Total Rent, Rental Purpose
   - Contract Status (PENDING/APPROVED/REJECTED/COMPLETED/CANCELLED)
   - Create Time, Update Time

4. **Verification Code Table (verification_code)**
   - Email, Verification Code
   - Create Time, Expiration Time
   - Usage Status

5. **Login Log Table (login_log)**
   - Log ID, User ID
   - Login Time, IP Address
   - Login Status (SUCCESS/FAILED)
   - Failure Reason

6. **Vessel Favorite Table (ship_favorite)**
   - Favorite ID, User ID, Vessel ID
   - Favorite Time

7. **Review Table (review)**
   - Review ID, Contract ID
   - Reviewer ID, Reviewee ID
   - Rating, Review Content
   - Create Time

## 🗓️ Development Completion Status

### ✅ Phase 1: Basic Framework Setup (Completed)
- [x] Create Spring Boot project
- [x] Configure PostgreSQL database connection
- [x] Integrate MyBatis-Plus
- [x] Design and create database tables (7 tables)
- [x] Build basic project structure (Controller/Service/Mapper/Entity/DTO/VO)
- [x] Configure unified response format and global exception handling

### ✅ Phase 2: User Authentication Module (Completed)
- [x] Implement user registration (with role selection)
- [x] Implement login function (with email verification code)
- [x] Integrate email service (Spring Mail)
- [x] Implement BCrypt password encryption storage
- [x] Implement account lock mechanism (locked for 1 hour after 5 failures)
- [x] Implement forgot password function (email verification code reset)
- [x] Implement JWT token authentication

### ✅ Phase 3: Ship Owner Features (Completed)
- [x] Vessel information management (CRUD)
- [x] Vessel list display and filtering
- [x] Contract inbox function
- [x] Contract review (Approve/Reject)
- [x] Contract management function

### ✅ Phase 4: Renter Features (Completed)
- [x] Vessel search and filtering
- [x] Vessel detail display
- [x] Vessel favorite function
- [x] Online contract filling and sending
- [x] My contracts management

### ✅ Phase 5: Administrator Features (Completed)
- [x] User management function
- [x] Vessel management function
- [x] Contract supervision function
- [x] Data statistics dashboard (ECharts visualization)
- [x] User/Vessel/Contract detail viewing

### ✅ Phase 6: Frontend Development (Completed)
- [x] Build Vue 3 + Vite project
- [x] Integrate Element Plus UI library
- [x] Configure Vue Router
- [x] Configure Pinia state management
- [x] Implement authentication pages (Login/Register/Forgot Password)
- [x] Implement owner pages (Vessel Management/Contract Inbox/Contract Management)
- [x] Implement renter pages (Vessel Search/Favorites/Contract Management)
- [x] Implement admin backend (Data Statistics/User Management/Vessel Management/Contract Supervision)
- [x] Implement detail pages (Vessel Detail/Contract Detail)
- [x] Implement maritime technology style UI design
- [x] Implement animation effects and custom components

### ✅ Phase 7: Testing and Optimization (Completed)
- [x] Backend API functional testing (38+ endpoints)
- [x] Frontend page functional testing
- [x] Frontend-backend integration testing
- [x] Performance optimization (database indexing, connection pool configuration)
- [x] Bug fixes
- [x] Write project documentation

### ✅ Phase 8: Real-time Notification System (Completed) 🆕
- [x] Integrate Spring WebSocket
- [x] Implement WebSocket configuration and handler
- [x] Implement frontend WebSocket service (singleton pattern)
- [x] Owner-side full-page notification (4 pages)
- [x] Renter-side full-page notification (4 pages)
- [x] Implement reconnection mechanism
- [x] Implement intelligent data refresh
- [x] WebSocket functionality testing
- [x] Write WebSocket testing guide

## 🎯 Project Highlights

### 1. Complete Business Process
- Complete business loop from user registration to vessel rental
- Full functionality for three roles (Owner/Renter/Administrator)
- Full lifecycle contract management (Create→Review→Execute→Complete)

### 2. Exquisite UI Design
- Unique maritime technology style theme
- Rich animation effects (particles, waves, light effects, 3D flip)
- Custom maritime style components
- Responsive design, adapts to multiple devices

### 3. Security Design
- Password strength validation (12+ characters with uppercase, lowercase, numbers, special characters)
- BCrypt password encryption storage
- JWT token authentication
- Email verification code double verification
- Account lock mechanism (prevent brute force attacks)
- Login log recording

### 4. Good Code Quality
- Clear layered architecture (Controller/Service/Mapper)
- Unified code style and naming conventions
- Comprehensive error handling and logging
- Detailed code comments
- High maintainability and extensibility

### 5. Data Visualization
- ECharts chart display
- User role distribution pie chart
- Vessel type distribution pie chart
- Real-time data statistics

### 6. User Experience Optimization
- Smooth page transition animations
- Friendly error messages
- Intuitive operation feedback
- Fast response speed

## 🚀 Quick Start

### Environment Requirements
- JDK 17+
- PostgreSQL 14+
- Maven 3.6+
- Node.js 16+
- npm or yarn

### Backend Startup Steps

1. **Clone Project**
```bash
git clone <repository-url>
cd E-ship
```

2. **Configure Database**
```bash
# Create database
psql -U postgres
CREATE DATABASE eship;

# Import database script
psql -U postgres -d eship -f easyGo.sql
```

3. **Configure application.yml**
```yaml
# Modify database configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eship?currentSchema=easyGo
    username: postgres
    password: your_password

# Modify email configuration
spring:
  mail:
    username: your_email@qq.com
    password: your_auth_code
```

4. **Start Backend Service**
```bash
# Start with Maven
mvn spring-boot:run

# Or use startup script
./start.sh
```

5. **Access Backend API**
- Backend service: http://localhost:3473
- API base path: http://localhost:3473/api

### Frontend Startup Steps

1. **Enter Frontend Directory**
```bash
cd frontend
```

2. **Install Dependencies**
```bash
npm install
# or
yarn install
```

3. **Start Development Server**
```bash
npm run dev
# or
yarn dev
```

4. **Access Frontend Application**
- Frontend service: http://localhost:5173

### Test Accounts

#### Administrator Account
```
Username: admin
Password: twhd#7?n*Csf
Email: 188043648@qq.com
```

#### Test Owner Account
```
Username: testowner
Password: TestOwner123!@#
Email: your_test_email@example.com
```

#### Test Renter Account
```
Username: testrenter
Password: TestRenter123!@#
Email: your_test_email@example.com
```

### Stop Services

```bash
# Stop backend and frontend services
./stop.sh
```

## 📁 Project Structure

```
E-ship/
├── src/
│   ├── main/
│   │   ├── java/com/fandesunstar/eship/
│   │   │   ├── common/          # Common classes (unified response format, exception handling)
│   │   │   ├── config/          # Configuration classes (CORS, Web config)
│   │   │   ├── controller/      # Controller layer (8 Controllers)
│   │   │   ├── dto/             # Data Transfer Objects (15 DTOs)
│   │   │   ├── entity/          # Entity classes (7 Entities)
│   │   │   ├── mapper/          # Data access layer (7 Mappers)
│   │   │   ├── service/         # Business logic layer (5 Services)
│   │   │   ├── utils/           # Utility classes (JWT, Password, Verification Code)
│   │   │   └── vo/              # View Objects (10 VOs)
│   │   └── resources/
│   │       ├── mapper/          # MyBatis XML mapping files (7 files)
│   │       └── application.yml  # Application configuration file
│   └── test/                    # Test classes
├── frontend/
│   ├── src/
│   │   ├── api/                 # API interface encapsulation (6 files)
│   │   ├── assets/              # Static resources
│   │   ├── components/          # Common components (4 components)
│   │   ├── router/              # Router configuration
│   │   ├── stores/              # Pinia state management
│   │   ├── styles/              # Style files (maritime theme)
│   │   └── views/               # Page components (17 components)
│   ├── package.json             # Frontend dependency configuration
│   └── vite.config.js           # Vite configuration
├── easyGo.sql                   # Database script
├── pom.xml                      # Maven configuration
├── start.sh                     # Startup script
├── stop.sh                      # Stop script
└── README.md                    # Project documentation
```

## 🔌 API Documentation

### Authentication APIs
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/send-code` - Send verification code
- `POST /api/auth/forgot-password` - Forgot password
- `GET /api/auth/current` - Get current user info
- `POST /api/auth/logout` - User logout

### Vessel Management APIs
- `GET /api/ships` - Get vessel list (with filtering, pagination)
- `GET /api/ships/{id}` - Get vessel details
- `POST /api/ships` - Add vessel (Owner)
- `PUT /api/ships/{id}` - Update vessel info (Owner)
- `DELETE /api/ships/{id}` - Delete vessel (Owner)
- `GET /api/ships/owner` - Get my vessels (Owner)
- `GET /api/ships/recommended` - Get recommended vessels (Renter)
- `GET /api/ships/search` - Search vessels

### Contract Management APIs
- `GET /api/contracts` - Get contract list
- `GET /api/contracts/{id}` - Get contract details
- `POST /api/contracts` - Create contract (Renter)
- `PUT /api/contracts/{id}/approve` - Approve contract (Owner)
- `PUT /api/contracts/{id}/reject` - Reject contract (Owner)
- `PUT /api/contracts/{id}/cancel` - Cancel contract (Renter)
- `GET /api/contracts/inbox` - Get contract inbox (Owner)
- `GET /api/contracts/my` - Get my contracts

### Favorites Management APIs
- `GET /api/favorites` - Get favorites list
- `POST /api/favorites` - Add favorite
- `DELETE /api/favorites/{shipId}` - Remove favorite
- `GET /api/favorites/check/{shipId}` - Check if favorited

### Administrator APIs
- `GET /api/admin/statistics` - Get statistics data
- `GET /api/admin/users` - Get user list
- `GET /api/admin/users/{id}` - Get user details
- `PUT /api/admin/users/{id}/status` - Update user status
- `PUT /api/admin/users/{id}/credit` - Update user credit score
- `GET /api/admin/ships` - Get all vessels
- `GET /api/admin/contracts` - Get all contracts
- `PUT /api/admin/contracts/{id}/terminate` - Force terminate contract

## 📝 Development Standards

### Code Standards
- Follow Alibaba Java Development Manual
- Use camelCase naming (Java) and kebab-case (Vue)
- Add necessary comments and documentation
- Keep methods concise with single responsibility

### Git Commit Standards
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation update
- `style`: Code formatting
- `refactor`: Refactoring
- `test`: Testing related
- `chore`: Build/toolchain related

### API Design Standards
- RESTful API design style
- Unified response format:
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```
- Standard HTTP status code usage
- Clear and explicit error messages

## 🧪 Testing Instructions

### Backend Testing
The project includes complete test classes:
- `DatabaseConnectionTest.java` - Database connection test
- `MailServiceTest.java` - Email service test
- `EShipApplicationTests.java` - Application startup test

Run tests:
```bash
mvn test
```

### Frontend Testing
Frontend compilation test:
```bash
cd frontend
npm run build
```

### API Testing
Recommended tools for API testing:
- Postman
- Insomnia
- curl command line

Test flow example:
1. Register user → Send verification code → Complete registration
2. Login → Get JWT token
3. Use token to access protected APIs

## 🔒 Security Features

### Implemented Security Measures
- **Password Security**: BCrypt encryption, strength validation (12+ characters with uppercase, lowercase, numbers, special characters)
- **Authentication**: JWT token mechanism with auto-renewal
- **Email Verification**: Double verification for registration and login
- **Brute Force Protection**: Locked for 1 hour after 5 failures
- **Login Logs**: Record all login attempts
- **SQL Injection Protection**: MyBatis Plus parameterized queries
- **XSS Protection**: Frontend input validation and escaping
- **CORS Configuration**: Cross-origin request security control

### Password Rules
- Minimum length: 12 characters
- Must include: uppercase letters, lowercase letters, numbers, special characters
- Example: `TestUser123!@#`

### Verification Code Mechanism
- Length: 4-character alphanumeric combination
- Validity: 10 minutes
- Rate limit: One request per 10 minutes per account

## 📈 Performance Optimization

### Database Optimization
- **Index Optimization**: Add indexes for frequently queried fields
- **Connection Pool Configuration**: HikariCP connection pool, max 10 connections
- **Query Optimization**: Use MyBatis Plus pagination queries
- **Schema Isolation**: Use easyGo schema to organize data

### Frontend Optimization
- **Code Splitting**: Vue Router lazy loading
- **Resource Compression**: Vite automatic compression and optimization
- **Caching Strategy**: Pinia state management, reduce duplicate requests
- **Animation Performance**: CSS3 hardware acceleration, 60fps smooth animations

### Backend Optimization
- **Asynchronous Processing**: Email sending asynchronous execution
- **Log Optimization**: Use INFO level in production environment
- **Exception Handling**: Global exception handler, unified error response

## 📚 Technical Documentation

### Project Related Documents
- `README.md` - Project documentation (this file)
- `PROJECT_STATUS.md` - Project status report
- `项目最终总结.md` - Project final summary
- `开发指南.md` - Development guide
- `快速启动指南-最终版.md` - Quick start guide
- `MyBatis配置完成说明.md` - MyBatis configuration guide
- `Vue前端配置完成.md` - Vue frontend configuration guide
- `邮件服务测试指南.md` - Email service test guide
- `测试通过报告.md` - Test report

### Online Technical Documentation
- [Spring Boot Official Documentation](https://spring.io/projects/spring-boot)
- [Vue 3 Official Documentation](https://vuejs.org/)
- [Element Plus Official Documentation](https://element-plus.org/)
- [MyBatis-Plus Official Documentation](https://baomidou.com/)
- [PostgreSQL Official Documentation](https://www.postgresql.org/docs/)

## 🎯 Use Cases

### Ship Owner Use Case
1. Register owner account
2. Login to system
3. Add vessel information (name, type, tonnage, rent, etc.)
4. Check contract inbox, receive rental applications from renters
5. Review contracts (approve or reject)
6. Manage all contracts, track rental status

### Renter Use Case
1. Register renter account
2. Login to system
3. Browse recommended vessels or search vessels
4. View vessel details
5. Favorite interesting vessels
6. Create rental contract, fill rental information
7. View contract status, wait for owner review
8. Manage all contracts

### Administrator Use Case
1. Login with administrator account
2. View platform data statistics (users, vessels, contracts, transaction amount)
3. Manage users (view, disable, adjust credit score)
4. Manage vessels (view, control status)
5. Supervise contracts (view, force terminate)
6. Data visualization analysis

## 🌟 Future Expansion Directions

### Feature Expansion
- [x] Real-time message notification (WebSocket) ✅ Completed
- [ ] Vessel location tracking (map integration)
- [ ] Online payment function (Alipay/WeChat Pay)
- [ ] Improve rating and review system
- [ ] Data export function (Excel/PDF)
- [ ] Vessel insurance service
- [ ] Contract electronic signature
- [ ] Mobile app development
- [ ] Notification history
- [ ] Desktop notification (Notification API)
- [ ] Notification sound alerts
- [ ] Unread message count

### Technical Optimization
- [ ] Redis cache (hot data, verification codes)
- [ ] Elasticsearch full-text search
- [ ] Image CDN acceleration
- [ ] Docker containerization deployment
- [ ] Kubernetes cluster deployment
- [ ] Microservices architecture transformation
- [ ] Message queue (RabbitMQ/Kafka)

### User Experience
- [ ] Multi-language support (i18n)
- [ ] Dark mode
- [ ] PWA offline functionality
- [ ] Voice search
- [ ] AI intelligent recommendation
- [ ] Data dashboard display

## 🐛 FAQ

### Backend Issues

**Q: Database connection failed?**
A: Check if PostgreSQL is running, confirm database configuration in application.yml is correct.

**Q: Email sending failed?**
A: Check email configuration, confirm QQ email authorization code is correct, ensure network can access smtp.qq.com.

**Q: JWT token expired?**
A: Re-login to get new token, or implement automatic token refresh mechanism.

### Frontend Issues

**Q: npm install failed?**
A: Try clearing cache `npm cache clean --force`, or use domestic mirror source.

**Q: Blank page?**
A: Check browser console error messages, confirm backend API is running normally.

**Q: CORS issue?**
A: Confirm backend CORS configuration is correct, allows frontend domain access.

### Deployment Issues

**Q: How to deploy to production environment?**
A:
1. Frontend: `npm run build`, deploy dist directory to Nginx
2. Backend: `mvn clean package`, run generated jar file
3. Configure Nginx reverse proxy
4. Configure HTTPS certificate

**Q: How to configure domain name?**
A: Modify frontend API base URL, configure Nginx reverse proxy, set DNS resolution.

## 📊 Project Achievements

### Development Results
- ✅ Complete front-end and back-end separation architecture
- ✅ 60 Java source files, approximately 5500+ lines of code
- ✅ 17 Vue components, approximately 4500+ lines of code
- ✅ 38+ RESTful API interfaces
- ✅ 7 database tables, complete data model
- ✅ Exquisite maritime technology style UI
- ✅ Comprehensive security mechanism
- ✅ Detailed project documentation
- ✅ WebSocket real-time notification system 🆕

### Technical Gains
- Master Spring Boot 3.x development
- Master Vue 3 Composition API
- Master MyBatis-Plus ORM framework
- Master JWT authentication mechanism
- Master front-end and back-end separation architecture
- Master RESTful API design
- Master PostgreSQL database
- Master ECharts data visualization
- Master WebSocket real-time communication technology 🆕
- Master Spring WebSocket framework 🆕

### Business Understanding
- E-commerce platform business process
- Maritime vessel rental industry knowledge
- Multi-role permission management
- Contract full lifecycle management
- Data statistics and analysis

## 🤝 Contribution Guidelines

This project is a personal course design project and does not accept external contributions.

If you have suggestions or find issues, please contact via email: fandesunstar@outlook.com

## 📄 License

This project is for learning and course design only and may not be used for commercial purposes without permission.

Copyright © 2026 Sun Fan. All rights reserved.

## 📞 Contact Information

**Developer**: Sun Fan
**Email**: fandesunstar@outlook.com

For questions or suggestions, please contact via email.

---

## 🎉 Acknowledgments

Thanks to the following open source projects and technical communities:
- Spring Boot Team
- Vue.js Team
- Element Plus Team
- MyBatis-Plus Team
- PostgreSQL Community
- All open source contributors

---

<div align="center">

**E-ship - Smart Maritime Vessel Rental Platform**

**Project Status**: ✅ Completed (100%)

**Development Completion Date**: March 2026

Made with ❤️ by Sun Fan

**Last Updated**: March 18, 2026

**Latest Update**: ✨ Added WebSocket real-time notification system, supporting ship owners and renters to receive contract status update notifications in real-time on all pages

</div>