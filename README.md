# Human Assistant

一个有趣的 Android 应用，允许你通过 OpenAI 兼容的 API 来作为人类助手响应好友消息！

## 功能特点

- 启动 HTTP 服务器，提供 OpenAI 兼容的 API 端点 `/v1/chat/completions`
- 接收好友消息并通过通知提醒
- 用户可以通过 UI 回复消息
- Token 计费系统（每个好友初始 100 个 tokens
- 支持并发请求
- 管理界面显示待处理请求和好友余额

## 构建与运行

### 使用 Android Studio 导入项目并运行即可！

## API 使用示例

```bash
curl --location 'http://手机IP:8080/v1/chat/completions' \
  --header 'Authorization: Bearer test-key' \
  --header 'Content-Type: application/json' \
  --data '{
    "model": "human",
    "messages": [
        {"role": "user", "content": "记得带伞！"
    ]
}'
```

## 有效 API Keys

- `test-key` - Test Friend
- `friend1-key` - Friend 1
- `friend2-key` - Friend 2
