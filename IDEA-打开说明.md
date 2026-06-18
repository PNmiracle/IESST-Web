# IntelliJ IDEA 启动教程

> 适用版本：IntelliJ IDEA 2023.2+（Community 或 Ultimate 均可）

---

## 一、环境准备

在开始之前，请确保你的 Mac 上已安装以下工具：

| 工具 | 用途 | 验证命令 |
|---|---|---|
| Java 17 | 后端运行环境 | `java -version` |
| Docker Desktop | 运行 MySQL 容器 | `docker info` |
| Node.js 18+ | 前端开发环境 | `node -v` |
| IntelliJ IDEA | 开发 IDE | — |

> **说明**：项目内置了 Maven 3.9.9（`.tools/apache-maven-3.9.9/`），无需单独安装 Maven。

---

## 二、导入项目到 IDEA

### 2.1 打开项目

1. 启动 IntelliJ IDEA，点击 **Open**（不是 New Project）。
2. 在弹出的文件选择器中，选中整个 `IESST-Academic-Platform` 文件夹，点击 **Open**。
   > ⚠️ **不要**只选择 `server` 或 `client` 子目录，否则 Maven 无法被识别。
3. IDEA 会自动检测到 `server/pom.xml`，右下角弹出提示 **"Maven build scripts found"**，点击 **Load** 加载 Maven 项目。

### 2.2 配置 JDK

1. 打开 **File → Project Structure**（快捷键 `⌘;`）。
2. 在 **Project Settings → Project** 中，将 **SDK** 设置为 **Java 17**。
   > 如果下拉列表中没有 Java 17，点击 **Add SDK → Download JDK**，选择 **Eclipse Temurin 17** 下载。
3. **Language level** 选择 **17 - Sealed types, always-strict floating-point semantics**。
4. 点击 **OK** 保存。

### 2.3 配置 Maven

1. 打开 **File → Settings**（快捷键 `⌘,`），导航到 **Build, Execution, Deployment → Build Tools → Maven**。
2. 将 **Maven home path** 设置为项目内置的 Maven：
   ```
   /Users/你的用户名/Downloads/IESST-Academic-Platform/.tools/apache-maven-3.9.9
   ```
   也可以点击右侧的 `…` 浏览选择。
3. 点击 **OK** 保存。

### 2.4 等待索引完成

项目导入后，IDEA 会自动下载 Maven 依赖并建立索引。等待右下角的进度条消失，直到状态栏显示 **Ready**。

---

## 三、启动 MySQL 数据库

项目使用 Docker 运行 MySQL 8 容器，默认端口 **3307**（避免与本地 MySQL 冲突）。

### 方式一：终端脚本（推荐）

1. 打开 IDEA 底部工具栏的 **Terminal**（快捷键 `⌥F12`）。
2. 执行：
   ```bash
   ./scripts/start-mysql.sh
   ```
3. 看到 `ready for connections` 字样即启动成功。

### 方式二：Docker Compose（IDEA Services 面板）

1. 打开 IDEA 左侧 **Services** 工具窗口（快捷键 `⌘8`）。
2. 点击 `+` → **Docker Compose**，选择根目录的 `docker-compose.yml`。
3. 点击绿色的 ▶ 运行按钮启动 MySQL 容器。

### 数据库连接参数

| 参数 | 值 |
|---|---|
| 地址 | `127.0.0.1:3307` |
| 数据库名 | `iesst` |
| 用户名 | `iesst` |
| 密码 | `iesst123` |

> 首次启动后端时，**Flyway 会自动建表并写入演示数据**，无需手动操作数据库。

---

## 四、启动后端

### 4.1 创建运行配置

1. 在项目工具窗口中，展开路径：
   ```
   server → src → main → java → cn.iesst.demo → IesstDemoApplication
   ```
2. 右键点击 `IesstDemoApplication`，选择 **Run 'IesstDemoApplication'**。
3. 首次运行后，IDEA 右上角会出现该运行配置，下次直接点击绿色 ▶ 即可启动。

### 4.2 验证启动

控制台出现以下日志表示启动成功：
```
Started IesstDemoApplication in X.XXX seconds
```

浏览器访问健康检查接口确认：
```
http://127.0.0.1:8080/actuator/health
```
返回 `{"status":"UP"}` 即一切正常。

### 4.3 访问页面

| 页面 | 地址 |
|---|---|
| 访客官网 | http://127.0.0.1:8080/ |
| 管理后台 | http://127.0.0.1:8080/admin |
| SCI 期刊 | http://127.0.0.1:8080/SCI |
| EI 期刊 | http://127.0.0.1:8080/EI |

**后台演示账号：** `admin` / `admin123123`

---

## 五、启动前端开发模式（可选）

后端启动后已包含构建好的前端静态页面。如果你需要**修改前端代码并实时预览**，才需要额外启动前端开发服务器。

1. 在 IDEA Terminal 中执行：
   ```bash
   cd client
   npm install
   npm run dev
   ```
2. 浏览器访问 http://127.0.0.1:5173/ ，修改 `client/src/` 下的代码会自动热更新。

> **Vite 开发服务器**会将 `/api` 请求代理到 `http://127.0.0.1:8080`，所以后端必须先启动。

---

## 六、一键命令速查

| 操作 | 命令 | 说明 |
|---|---|---|
| 首次安装依赖 | `./scripts/setup.sh` | 安装前端 npm 依赖 + 后端 Maven 依赖 |
| 启动 MySQL | `./scripts/start-mysql.sh` | Docker 运行 MySQL 容器 |
| 启动后端 | `./scripts/start-server.sh` | Maven 编译并启动 Spring Boot |
| 启动前端 | `./scripts/start-client.sh` | 启动 Vite 开发服务器 |
| 构建单体 JAR | `./scripts/build-all.sh` | 将前后端打包成一个 JAR |
| 运行 JAR | `java -jar server/target/iesst-demo-server-0.1.0.jar` | 一键启动生产构建版 |
| 备份数据库 | `./scripts/backup-mysql.sh` | 导出 MySQL 数据到 backups 目录 |

---

## 七、常见问题

### Q1: IDEA 没有识别到 Maven 项目？
手动添加：右键点击 `server/pom.xml` → **Add as Maven Project**。

### Q2: 启动报 `Communications link failure`？
MySQL 容器没有运行。先执行 `./scripts/start-mysql.sh` 启动数据库。

### Q3: 端口 8080 或 3307 被占用？
修改 `.env` 文件中的 `APP_PORT` 或 `MYSQL_PORT`，然后重启对应服务。

### Q4: 前端开发模式请求后端报 404？
确认后端已在 `8080` 端口运行。Vite 开发代理配置在 `client/vite.config.js` 中。

### Q5: 如何重置数据库？
```bash
docker compose down -v   # 删除容器和数据卷
./scripts/start-mysql.sh  # 重新启动，Flyway 会自动重建表和数据
```

### Q6: IDEA 中 Maven 依赖下载太慢？
在 Settings → Maven → User settings file 中配置阿里云镜像，或参阅 `.mvn/maven.config` 中的仓库配置。

---

## 八、项目结构速览

```
IESST-Academic-Platform/
├── server/                  # Spring Boot 后端 (Java 17 + Maven)
│   ├── src/main/java/       # Java 源码
│   ├── src/main/resources/  # 配置文件（application.yml、Flyway 迁移脚本）
│   └── pom.xml              # Maven 依赖声明
├── client/                  # Vue 3 前端 (Vite)
│   ├── src/                 # Vue 组件与页面
│   ├── public/              # 静态资源
│   └── package.json         # npm 依赖声明
├── scripts/                 # Shell 脚本
│   ├── start-mysql.sh       # 启动 MySQL 容器
│   ├── start-server.sh      # 启动后端
│   ├── start-client.sh      # 启动前端
│   ├── setup.sh             # 首次安装依赖
│   ├── build-all.sh         # 构建单体 JAR
│   └── backup-mysql.sh      # 备份数据库
├── .tools/                  # 内置工具
│   └── apache-maven-3.9.9/  # Maven（无需单独安装）
├── uploads/                 # 文件上传目录
├── docker-compose.yml       # MySQL 容器编排
├── .env                     # 环境变量（端口、密码）
└── README.md                # 完整项目文档
```
