# IntelliJ IDEA 打开说明

## 正确打开方式

1. 解压压缩包，只会得到一个 `思研学术官网管理系统` 文件夹。
2. 打开 IntelliJ IDEA，选择 `Open`。
3. 选择整个 `思研学术官网管理系统` 文件夹，不要只选择 `server` 或 `client`。
4. IDEA 会识别 `server/pom.xml`，按提示加载 Maven 项目。
5. 将 Project SDK 设置为 Java 17。

## 启动 MySQL

推荐先安装并打开 Docker Desktop，然后在 IDEA Terminal 中执行：

```bash
./scripts/start-mysql.sh
```

也可以打开 IDEA 的 `Services` 工具窗口，添加并运行根目录的 `docker-compose.yml`。

默认数据库地址为 `127.0.0.1:3307`，数据库 `iesst`，账号 `iesst`，密码 `iesst123`。首次启动后端时 Flyway 会自动建表。

## 启动后端

运行：

`server/src/main/java/cn/iesst/demo/IesstDemoApplication.java`

访问：

- 官网：http://127.0.0.1:8080/
- 后台：http://127.0.0.1:8080/admin
- SCI：http://127.0.0.1:8080/SCI
- EI：http://127.0.0.1:8080/EI
- 演示账号：`admin`
- 演示密码：`admin123`

## 启动前端开发模式

在 IDEA Terminal 中执行：

```bash
cd client
npm install
npm run dev
```

访问：http://127.0.0.1:5173/

## 一键运行构建版

先启动 MySQL，再执行：

```bash
java -jar server/target/iesst-demo-server-0.1.0.jar
```

## 重新构建

```bash
./scripts/setup.sh
./scripts/build-all.sh
```
