# Human Assistant 🤖💬

一个有趣的 Android 应用，允许你通过 OpenAI 兼容的 API 来作为人类助手响应好友消息！

## 功能特点

✨ **核心功能**
- 🚀 启动 HTTP 服务器，提供 OpenAI 兼容的 API 端点 `/v1/chat/completions`
- 📱 接收好友消息并通过通知提醒
- 💬 用户可以通过简洁的 UI 回复消息
- 🔄 支持并发处理多个请求

💰 **Token 计费系统**
- 每个好友初始 100 个 Token
- 每次调用消耗 1 个 Token
- 余额不足时返回 HTTP 402 错误和幽默提示
- 使用 Room 数据库持久化存储

⏱️ **超时机制**
- 用户未在 60 秒内回复自动返回 HTTP 408 错误
- 取消相关通知

🔐 **API 认证**
- 需要 `Authorization: Bearer <API_KEY>` 头
- 预定义的有效 API Keys:
  - `test-key` - Test Friend
  - `friend1-key` - Friend 1
  - `friend2-key` - Friend 2

## 技术栈

- **HTTP 服务器**: Ktor (Kotlin 友好)
- **JSON 解析**: kotlinx.serialization
- **数据库**: Room
- **状态管理**: ViewModel + LiveData
- **通知**: NotificationCompat
- **最低 API**: 24 (Android 7.0)

## 构建说明

### 环境要求

- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 34

### 构建步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/2233qazwsx0/human-assistant.git
   cd human-assistant
   ```

2. **使用 Android Studio 打开项目**
   - File → Open → 选择项目目录
   - 等待 Gradle 同步完成

3. **构建 Debug APK**
   ```bash
   ./gradlew assembleDebug
   ```

4. **构建 Release APK**
   ```bash
   ./gradlew assembleRelease
   ```
   - 需要配置签名密钥

5. **APK 位置**
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/app-release.apk`

## 使用说明

### 启动服务器

1. 在设备上安装并打开应用
2. 点击 **"启动服务器"** 按钮
3. 服务器将在 `http://0.0.0.0:8080` 上运行
4. 界面显示局域网 IP 地址（如 `http://192.168.1.100:8080`）

### 发送 API 请求

```bash
curl --location 'http://手机IP:8080/v1/chat/completions' \
  --header 'Authorization: Bearer test-key' \
  --header 'Content-Type: application/json' \
  --data '{
    "model": "human",
    "messages": [
        {"role": "user", "content": "记得带伞！"}
    ]
}'
```

### 回复消息

1. 好友发送请求后，手机会弹出通知
2. 点击通知打开回复界面
3. 输入回复内容并点击 **"发送"**
4. 回复将作为 HTTP 响应返回给调用方

## API 端点

### POST /v1/chat/completions

**请求格式**:
```json
{
  "model": "human",
  "messages": [
    {"role": "user", "content": "消息内容"}
  ]
}
```

**响应格式**:
```json
{
  "id": "chatcmpl-xxx",
  "object": "chat.completion",
  "created": 1234567890,
  "model": "human",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "你的回复内容"
      },
      "finish_reason": "stop"
    }
  ]
}
```

**错误响应**:
- `401 Unauthorized` - 缺少或无效的 API Key
- `402 Payment Required` - Token 余额不足
- `408 Request Timeout` - 60 秒内未回复

## 项目结构

```
app/
├── src/main/
│   ├── java/com/humanassistant/
│   │   ├── MainActivity.kt          # 主界面
│   │   ├── ReplyActivity.kt         # 回复界面
│   │   ├── ServerService.kt         # 后台服务
│   │   ├── MainViewModel.kt         # ViewModel
│   │   ├── PendingRequestAdapter.kt # 待处理请求列表适配器
│   │   ├── FriendBalanceAdapter.kt   # 好友余额列表适配器
│   │   ├── data/                    # 数据层
│   │   │   ├── Friend.kt            # 好友实体
│   │   │   ├── FriendDao.kt         # DAO
│   │   │   ├── AppDatabase.kt       # Room 数据库
│   │   │   └── PendingRequest.kt     # 待处理请求数据类
│   │   └── server/                  # 服务器层
│   │       ├── HttpServer.kt        # Ktor HTTP 服务器
│   │       └── ChatModels.kt        # OpenAI API 数据模型
│   ├── res/
│   │   ├── layout/                  # 布局文件
│   │   └── values/                  # 资源文件
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## 许可证

本项目采用 **GNU Affero General Public License v3.0** (AGPL-3.0) 开源。

详细许可证内容请参阅 [LICENSE](LICENSE) 文件。

## 注意事项

⚠️ **安全警告**
- 此应用仅供本地网络使用
- 请勿在公共网络上运行服务器
- API Keys 是预定义的，请根据需要修改 `HttpServer.kt` 中的 `validApiKeys` 映射

📱 **权限说明**
- `INTERNET`: 允许 HTTP 服务器监听网络请求
- `ACCESS_WIFI_STATE`: 获取设备 IP 地址
- `POST_NOTIFICATIONS`: 发送消息通知 (Android 13+)
- `FOREGROUND_SERVICE`: 以后台服务运行

🔧 **自定义 API Keys**

编辑 `app/src/main/java/com/humanassistant/server/HttpServer.kt`:

```kotlin
private val validApiKeys = mapOf(
    "your-key" to "Your Friend Name",
    "another-key" to "Another Friend"
)
```

## 获取帮助

- 📖 查看 [Issues](https://github.com/2233qazwsx0/human-assistant/issues)
- 💡 提交 [Feature Requests](https://github.com/2233qazwsx0/human-assistant/issues/new)
- 🐛 报告 [Bug](https://github.com/2233qazwsx0/human-assistant/issues/new)

---

⭐ 如果这个项目对你有帮助，请给我们一个 Star！
