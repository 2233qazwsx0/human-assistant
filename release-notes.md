# Human Assistant v1.0.0

一个有趣的 Android 应用，让你作为人类助手响应好友消息！

## 功能特性：

- 启动 HTTP 服务器，提供 OpenAI 兼容的 API 端点
- 接收消息后在手机上弹出通知
- 用户可通过 UI 界面回复消息
- Token 计费系统（每个好友初始 100 个 Token）
- 支持并发多个请求

## 构建方式：

1. 克隆项目到本地
2. 用 Android Studio 打开项目
3. 同步 Gradle
4. 点击 Build > Build Bundle(s) / APK(s) > Build APK(s)
5. 构建成功后的 APK 在：`app/build/outputs/apk/debug/` 下

## 使用方式：

1. 安装到 Android 手机
2. 打开应用，点击启动服务器
3. 查看界面上显示的服务器地址
4. 使用 curl 或其他工具发送 API 请求：

```bash
curl --location 'http://手机IP:8080/v1/chat/completions' \
  --header 'Authorization: Bearer test-key' \
  --header 'Content-Type: application/json' \
  --data '{
    "model": "human",
    "messages": [{"role": "user", "content": "记得带伞！"}]
}'
```

## 有效 API Keys：

- `test-key` - Test Friend
- `friend1-key` - Friend 1
- `friend2-key` - Friend 2

## 技术栈：

- Kotlin
- Ktor (HTTP 服务器)
- Room 已简化为 SharedPreferences (便于构建)
- ViewModel + LiveData
- NotificationCompat

## 许可证：

GNU Affero General Public License v3.0 (AGPL-3.0)
