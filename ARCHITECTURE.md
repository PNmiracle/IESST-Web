# 架构说明

```text
Vue 3 + Vue Router
  ├── /                   访客官网
  └── /admin/**           独立管理后台
           |
           | REST JSON
           v
Spring Boot API + SPA 路由回退
           |
           v
Spring JDBC + Flyway + MySQL 8
```

## 当前 Demo 边界

- 官网与管理后台位于同一个 Vue 构建项目，但通过 `/` 与 `/admin/**` 独立路由分离。
- 官网不显示后台入口；管理员需直接访问 `/admin`。
- 后台子路由受前端登录守卫保护，并支持浏览器直接刷新。
- Spring Boot 同时提供 API 和构建后的 Vue 静态页面。
- 管理端使用 Spring Security Session、HttpOnly Cookie 与 CSRF 防护。
- 数据保存在 MySQL，Flyway 管理表结构版本。
- 管理员密码使用 BCrypt 哈希保存，后台写操作进入 `audit_logs`。
- Actuator 提供服务健康检查，投稿列表由服务端完成检索与分页。
- 轮播图片和期刊文档保存在本地 `uploads/`，数据库保存访问地址。
- 数据库为空时自动写入演示数据，重启不会覆盖已有数据。

## 正式版替换关系

| Demo 实现 | 正式版实现 |
|---|---|
| `JdbcTemplate DemoStore` | 分模块 Repository / MyBatis-Plus |
| 单管理员账号 | 多管理员 + RBAC 权限模型 |
| 本地文件存储 | 腾讯云 COS 上传组件 |
| 单 Vue 应用 | Nuxt 官网 + Vue 管理后台 |
| 单库演示数据 | 生产库、备份与审计策略 |

## 建议正式版模块

```text
server/
  auth/
  banner/
  journal/
  submission/
  storage/
  audit/
  account/

web-site/
  Nuxt 官网

web-admin/
  Vue 管理后台
```
