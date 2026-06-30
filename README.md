# IESST 前后端联动 Demo

可继续迭代和部署的企业应用基础版，包含 Vue 官网、独立管理后台、Spring Boot API 与 MySQL 数据层。

## 在线演示

- GitHub Pages 静态演示版：https://pnmiracle.github.io/IESST-Web/
- 说明：首次启用或重新部署 GitHub Pages 后，GitHub Actions 可能需要几分钟完成构建；在部署完成前访问该地址可能会暂时返回 404。

## 技术栈

- 前端：Vue 3 + Vite
- 后端：Spring Boot 3.3.3 + Java 17
- 数据：MySQL 8 + Spring JDBC + Flyway
- 鉴权：Spring Security Session + HttpOnly Cookie + CSRF
- 运维：Spring Boot Actuator 健康检查

## 已实现功能

- 官网读取后台维护的轮播图
- 官网读取已上架的 SCI / EI 期刊
- SCI / EI 期刊目录、筛选与独立详情页
- 翻译润色、科学编辑和关于我们独立页面
- 服务咨询与期刊评估自动带入在线投稿表单
- 首页发表优势、SCI 全流程方案、专家团队与合作资源
- 后台维护翻译润色、科学编辑服务套餐
- 后台维护专家团队并控制官网展示状态
- 用户提交稿件信息
- 管理员登录
- 管理员增删改查轮播图
- 轮播图图片上传与预览
- 管理员增删改查期刊
- 期刊资料文档上传与下载
- 管理员查看投稿记录并更新处理状态
- 投稿记录按当前筛选导出 CSV
- 管理后台数据概览与最新投稿看板
- 管理员密码修改
- 后台写操作审计日志
- 审计日志服务端筛选与分页
- 服务端投稿检索与分页
- 参数校验和统一 API 错误结构
- 访客官网与管理后台使用独立路由
- 后台路由登录守卫与登录失效自动跳转
- 轮播自动播放、期刊筛选、删除确认和加载状态

## 演示账号

- 账号：`admin`
- 密码：`admin123123`

## 启动 MySQL

已安装 Docker Desktop 时：

```bash
./scripts/start-mysql.sh
```

也可以在 IDEA 的 `Services` 中运行根目录的 `docker-compose.yml`。

默认数据库参数：

```text
地址：127.0.0.1:3307
数据库：iesst
账号：iesst
密码：iesst123
```

如使用已有 MySQL，请先创建数据库，并通过环境变量覆盖连接参数：

```bash
export DB_URL='jdbc:mysql://127.0.0.1:3307/iesst?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
export DB_USERNAME='iesst'
export DB_PASSWORD='你的密码'
```

首次启动时 Flyway 会自动创建数据表。官网基础内容仅在 `dev` 环境补全；学生演示账号和演示订单只有显式启用 `dev-demo` Profile 才会创建，生产环境不会初始化 Demo 数据。

## 最简单启动方式

构建后的 Spring Boot JAR 已包含 Vue 页面，只需运行：

```bash
java -jar server/target/iesst-demo-server-0.1.0.jar
```

打开：`http://127.0.0.1:8080`

页面入口：

```text
访客官网：http://127.0.0.1:8080/
管理后台：http://127.0.0.1:8080/admin
```

访客官网子页面：

```text
/SCI
/SCI/{id}
/EI
/EI/{id}
/services/translation
/services/editing
/about
/submit
```

后台子页面：

```text
/admin/login
/admin/dashboard
/admin/banners
/admin/journals
/admin/submissions
/admin/content
/admin/governance
```

## 前后端分离开发方式

首次开发前安装项目依赖：

```bash
./scripts/setup.sh
```

终端 1：

```bash
./scripts/start-server.sh
```

终端 2：

```bash
./scripts/start-client.sh
```

打开：`http://127.0.0.1:5173`

重新构建单体 JAR：

```bash
./scripts/build-all.sh
```

## 核心接口

| 方法 | 地址 | 用途 |
|---|---|---|
| GET | `/api/public/banners` | 官网轮播图 |
| GET | `/api/public/journals` | 官网期刊列表 |
| GET | `/api/public/journals/{id}` | 官网期刊详情 |
| GET | `/api/public/services` | 官网服务套餐 |
| GET | `/api/public/experts` | 官网专家团队 |
| POST | `/api/public/submissions` | 提交稿件 |
| POST | `/api/auth/login` | 管理员登录 |
| GET | `/api/auth/me` | 查询当前登录会话 |
| POST | `/api/auth/logout` | 退出登录 |
| GET | `/api/admin/dashboard` | 后台数据概览 |
| GET/POST/PUT/DELETE | `/api/admin/banners` | 轮播图管理 |
| POST | `/api/admin/uploads/image` | 上传轮播图片 |
| GET/POST/PUT/DELETE | `/api/admin/journals` | 期刊管理 |
| POST | `/api/admin/uploads/document` | 上传期刊文档 |
| GET | `/api/admin/submissions` | 投稿记录 |
| GET | `/api/admin/submissions/export` | 导出投稿 CSV |
| PUT | `/api/admin/submissions/{id}/status` | 更新投稿状态 |
| PUT | `/api/admin/account/password` | 修改管理员密码 |
| GET | `/api/admin/audit-logs` | 查询后台操作审计 |
| GET/POST/PUT/DELETE | `/api/admin/services` | 服务套餐管理 |
| GET/POST/PUT/DELETE | `/api/admin/experts` | 专家团队管理 |

健康检查：

```text
GET /actuator/health
```

## GitHub Pages 静态演示版

仓库包含 `.github/workflows/deploy-pages.yml`，推送到 `main` 后会将 Vue 前端发布到 GitHub Pages。该版本使用内置公开演示数据，并采用 Hash 路由；GitHub Pages 无法运行 Spring Boot 与 MySQL，因此登录、投稿、咨询、文件上传和管理后台等功能仍需完整后端部署。

本地预览 Pages 构建：

```bash
cd client
VITE_STATIC_DEMO=true VITE_BASE_PATH=/IESST-Web/ npm run build:pages
npm run preview
```

线上地址：`https://pnmiracle.github.io/IESST-Web/`

## 生产环境

生产部署使用 `prod` Profile，并必须通过环境变量注入数据库和管理员初始凭据：

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL='jdbc:mysql://mysql-host:3306/iesst?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=true'
export DB_USERNAME='iesst_app'
export DB_PASSWORD='请使用强密码'
export ADMIN_USERNAME='admin'
export ADMIN_PASSWORD='请使用至少 12 位强密码'
export PII_ENCRYPTION_KEY="$(openssl rand -base64 32)"
export PII_LOOKUP_KEY="$(openssl rand -base64 32)"
export MANUSCRIPT_STORAGE_BACKEND=oss
export OSS_ENDPOINT='https://oss-cn-hangzhou.aliyuncs.com'
export OSS_BUCKET='你的私有 Bucket'
export OSS_ACCESS_KEY_ID='仅限该 Bucket 的 RAM AccessKey'
export OSS_ACCESS_KEY_SECRET='RAM AccessKey Secret'
export MALWARE_SCAN_ENABLED=true
export CLAMAV_HOST=clamav
java -jar server/target/iesst-demo-server-0.1.0.jar
```

生产环境应通过 HTTPS 反向代理访问。稿件保存到私有 OSS Bucket，下载经登录鉴权后生成短期签名地址；图片和公开资料仍使用挂载卷。PII 和 OSS 密钥应存入阿里云 KMS/容器密钥，不得提交到 Git 或写入镜像。

Docker 生产示例：

```bash
cp .env.example .env
# 将 .env 中每一个 replace/please-change 占位值替换为真实强密钥
docker compose -f docker-compose.prod.yml up -d --build
```

数据库备份：

```bash
./scripts/backup-mysql.sh
```

清理旧 Demo 与自动化测试记录（脚本会先备份数据库）：

```bash
./scripts/cleanup-demo-data.sh
```

## 下一版建议

1. 将官网迁移至 Nuxt，支持 SEO 和服务端渲染。
2. 增加多管理员、角色权限与通知中心。
3. 接入阿里云短信完成学生自助找回密码。
4. 增加日志采集、指标监控和自动备份告警。
