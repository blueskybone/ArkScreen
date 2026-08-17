# 森空岛 `/api/v1/game/player/info` 请求链路说明

本文依据 ArkScreen 当前代码实现整理，重点追踪了 `data/network` 与
`data/repository`，并补充查看了账号持久化、时间校准和依赖注入部分。

目标接口：

```text
GET https://zonai.skland.com/api/v1/game/player/info?uid={uid}
```

> 注意：这不是一个拿到登录 token 后就能直接调用的接口。请求前必须依次取得
> `grant code`、`cred` 和用于 HMAC 的签名 token，还必须知道一个已经绑定的游戏
> 角色 `uid`。

## 1. 一句话结论

完整链路如下：

```text
生成并固定 dId
  -> 获得鹰角账号 token（登录或外部导入）
  -> 鹰角 OAuth grant
  -> 森空岛 generate_cred_by_code
  -> 森空岛 player/binding（获得明日方舟角色 uid）
  -> 按 path + query + timestamp + header-json 计算签名
  -> 请求 player/info?uid=...
```

其中存在两个名字都叫 `token`、但用途完全不同的值：

| 文档中的名称 | 来源 | 用途 |
| --- | --- | --- |
| `hgToken` | 鹰角账号登录接口或外部导入 | 换取 OAuth grant code |
| `signToken` | `generate_cred_by_code` 返回的 `data.token` | HMAC-SHA256 的密钥 |
| `cred` | `generate_cred_by_code` 返回的 `data.cred` | 最终签名请求的 `cred` 请求头 |

最终接口必须使用 `cred + signToken`，不能拿 `hgToken` 直接签名。

## 2. 固定参数和域名

当前项目使用：

```text
森空岛 API：https://zonai.skland.com
鹰角账号 API：https://as.hypergryph.com
appCode：4ca99fa6b56cc2ba
User-Agent：Skland/1.5.1 (com.hypergryph.skland; build:100501001; Android 33; ) Okhttp/4.11.0
```

基础请求头为：

```http
User-Agent: Skland/1.5.1 (com.hypergryph.skland; build:100501001; Android 33; ) Okhttp/4.11.0
Content-Type: application/json
Connection: close
```

`appCode` 与 User-Agent 都是当前仓库代码中的固定值，服务端协议升级后可能需要同步
更新。

## 3. 第零步：准备并长期复用 `dId`

项目生成 32 个安全随机字节，做标准 Base64 后加 `BL` 前缀：

```text
dId = "BL" + Base64(randomBytes[32])
```

例如它的形状会类似：

```text
BLxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx=
```

要求：

1. 一次登录/授权链路中的每个请求必须使用同一个 `dId`。
2. 成功导入账号后，应将 `dId` 与 `hgToken`、角色 `uid` 一起持久化。
3. 后续刷新 `cred`、生成签名和请求业务接口时继续使用这个 `dId`。
4. 若外部导入时没有 `dId`，本项目会新生成一个；若提供了非空 `dId`，则直接复用。

项目中的 `dId` 不是 Android 系统设备 ID，也不是 UUID。

## 4. 第一步：获得鹰角账号 token

如果调用方已经持有有效的鹰角账号 token，可跳过本步。ArkScreen 也支持直接导入
token。

项目内的账号密码登录请求为：

```http
POST https://as.hypergryph.com/user/auth/v1/token_by_phone_password
User-Agent: Skland/1.5.1 (com.hypergryph.skland; build:100501001; Android 33; ) Okhttp/4.11.0
Content-Type: application/json
Connection: close
dId: {同一个 dId}

{
  "phone": "{手机号}",
  "password": "{密码}"
}
```

取响应中的：

```json
{
  "data": {
    "token": "..."
  }
}
```

记为 `hgToken`。当前仓库的领域接口参数名写作 `code`，但它最终被放进 JSON 的
`password` 字段，实际语义仍是密码。

## 5. 第二步：用 `hgToken` 换取 grant code

```http
POST https://as.hypergryph.com/user/oauth2/v2/grant
User-Agent: Skland/1.5.1 (com.hypergryph.skland; build:100501001; Android 33; ) Okhttp/4.11.0
Content-Type: application/json
Connection: close
dId: {同一个 dId}

{
  "appCode": "4ca99fa6b56cc2ba",
  "token": "{hgToken}",
  "type": 0
}
```

取：

```text
grantCode = response.data.code
```

这一步仍请求 `as.hypergryph.com`，不是 `zonai.skland.com`。

## 6. 第三步：用 grant code 换取 `cred` 和签名 token

```http
POST https://zonai.skland.com/api/v1/user/auth/generate_cred_by_code
User-Agent: Skland/1.5.1 (com.hypergryph.skland; build:100501001; Android 33; ) Okhttp/4.11.0
Content-Type: application/json
Connection: close
dId: {同一个 dId}

{
  "code": "{grantCode}",
  "kind": 1
}
```

响应中的两个值都要保留：

```json
{
  "data": {
    "cred": "...",
    "token": "..."
  }
}
```

本文将它们分别称为 `cred` 和 `signToken`。在项目运行时，每次读取玩家数据前都会
重新执行“grant -> cred”两步，而不是把 `cred/signToken` 作为长期账号凭据保存。

## 7. 第四步：取得正确的明日方舟角色 `uid`

如果已经保存了角色 `uid`，可以跳过本步。首次导入账号时，项目会调用绑定列表：

```http
GET https://zonai.skland.com/api/v1/game/player/binding
cred: {cred}
sign: {按本节路径签出的 sign}
timestamp: {Unix 秒}
dId: {同一个 dId}
platform:
vName:
User-Agent: Skland/1.5.1 (com.hypergryph.skland; build:100501001; Android 33; ) Okhttp/4.11.0
Content-Type: application/json
Connection: close
```

它的签名输入是：

```text
path   = /api/v1/game/player/binding
params = 空字符串
```

从 `response.data.list` 中选择：

```text
appCode == "arknights"
```

再从该项的 `bindingList` 中选择目标角色，使用其 `uid`。项目同时保存
`nickName`、`channelMasterId`、`isOfficial`，但请求 `player/info` 本身只需要
`uid`。

不要把 Endfield 的 `roleId`、数据库自增主键或昵称误当成这里的 `uid`。

## 8. 第五步：计算 `player/info` 签名

### 8.1 精确输入

```text
api       = "/api/v1/game/player/info"
params    = "uid=" + uid
timestamp = 当前 Unix 时间戳（秒，十进制字符串）
dId       = 前面一直复用的 dId
key       = signToken
```

构造紧凑 JSON。字段顺序、空字符串和无空格格式均应保持如下：

```text
{"platform":"","timestamp":"{timestamp}","dId":"{dId}","vName":""}
```

然后无分隔符拼接：

```text
data = api + params + timestamp + jsonArgs
```

即：

```text
/api/v1/game/player/infouid={uid}{timestamp}{"platform":"","timestamp":"{timestamp}","dId":"{dId}","vName":""}
```

计算过程：

```text
hmacHex = lowercase_hex(HMAC-SHA256(key = signToken, message = data))
sign    = lowercase_hex(MD5(UTF8(hmacHex)))
```

第二步 MD5 的输入是 HMAC 结果的“小写十六进制文本”，不是 HMAC 的原始 32 字节。

### 8.2 Kotlin 等价实现

```kotlin
fun calculatePlayerInfoSign(
    uid: String,
    signToken: String,
    timestamp: String,
    dId: String,
): String {
    val path = "/api/v1/game/player/info"
    val params = "uid=$uid"
    val jsonArgs =
        """{"platform":"","timestamp":"$timestamp","dId":"$dId","vName":""}"""
    val message = path + params + timestamp + jsonArgs

    val mac = javax.crypto.Mac.getInstance("HmacSHA256")
    mac.init(
        javax.crypto.spec.SecretKeySpec(
            signToken.toByteArray(Charsets.UTF_8),
            "HmacSHA256",
        ),
    )
    val hmacHex = mac.doFinal(message.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    return java.security.MessageDigest.getInstance("MD5")
        .digest(hmacHex.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
```

### 8.3 Python 等价实现

下面的函数可用于独立客户端或调试脚本：

```python
import hashlib
import hmac
import json
import time


def make_player_info_headers(uid: str, cred: str, sign_token: str, did: str):
    timestamp = str(int(time.time()))
    path = "/api/v1/game/player/info"
    params = f"uid={uid}"
    json_args = json.dumps(
        {
            "platform": "",
            "timestamp": timestamp,
            "dId": did,
            "vName": "",
        },
        ensure_ascii=False,
        separators=(",", ":"),
    )
    message = path + params + timestamp + json_args
    hmac_hex = hmac.new(
        sign_token.encode("utf-8"),
        message.encode("utf-8"),
        hashlib.sha256,
    ).hexdigest()
    sign = hashlib.md5(hmac_hex.encode("utf-8")).hexdigest()

    return {
        "cred": cred,
        "User-Agent": (
            "Skland/1.5.1 "
            "(com.hypergryph.skland; build:100501001; Android 33; ) "
            "Okhttp/4.11.0"
        ),
        "Connection": "close",
        "Content-Type": "application/json",
        "sign": sign,
        "platform": "",
        "timestamp": timestamp,
        "dId": did,
        "vName": "",
    }
```

## 9. 第六步：发送最终请求

生成签名后应立即发出请求，不能重新生成时间戳。URL 查询参数与签名中的 `params`
必须表达同一个值：

```http
GET /api/v1/game/player/info?uid={uid} HTTP/1.1
Host: zonai.skland.com
cred: {cred}
User-Agent: Skland/1.5.1 (com.hypergryph.skland; build:100501001; Android 33; ) Okhttp/4.11.0
Connection: close
Content-Type: application/json
sign: {sign}
platform:
timestamp: {签名时使用的同一个 timestamp}
dId: {同一个 dId}
vName:
```

Python 完整调用片段：

```python
import requests

uid = "替换为 binding 返回的角色 uid"
cred = "替换为 generate_cred_by_code 返回的 data.cred"
sign_token = "替换为 generate_cred_by_code 返回的 data.token"
did = "替换为本次账号链路固定使用的 dId"

headers = make_player_info_headers(uid, cred, sign_token, did)
response = requests.get(
    "https://zonai.skland.com/api/v1/game/player/info",
    params={"uid": uid},
    headers=headers,
    timeout=15,
)
response.raise_for_status()
payload = response.json()

# HTTP 2xx 不等价于业务成功，独立实现还应检查业务 code/message。
if payload.get("code") != 0:
    raise RuntimeError(payload.get("message") or f"Skland error: {payload!r}")
```

若用 curl，需要先由程序计算好同一时刻的 `timestamp` 和 `sign`：

```bash
curl --get 'https://zonai.skland.com/api/v1/game/player/info' \
  --data-urlencode 'uid=角色UID' \
  -H 'cred: CRED' \
  -H 'sign: SIGN' \
  -H 'timestamp: UNIX秒' \
  -H 'dId: DID' \
  -H 'platform:' \
  -H 'vName:' \
  -H 'Content-Type: application/json' \
  -H 'Connection: close' \
  -H 'User-Agent: Skland/1.5.1 (com.hypergryph.skland; build:100501001; Android 33; ) Okhttp/4.11.0'
```

## 10. 时间戳与时间校准

签名使用 Unix 秒。项目默认取本机时间；开启“时间校准”后，使用：

```text
签名时间 = 本机 Unix 秒 + 已记录的服务端偏移秒数
```

校准过程会无鉴权请求同一个 `/api/v1/game/player/info`，优先读取响应 `Date` 头，其次
从响应或错误响应中的 `timestamp` 取服务端时间，并用请求开始/结束时间的中点降低
网络往返耗时影响。

需要强调：

- 这个无鉴权请求只是借响应取得服务器时间，不能拿到玩家数据。
- 项目只在用户从设置界面触发时执行校准，并非每次请求自动校准。
- 独立实现若签名报时间相关错误，应先检查系统时间；必要时依据服务器 `Date` 头校正。

## 11. 最常见的失败原因

| 表现或错误点 | 检查内容 |
| --- | --- |
| 签名始终无效 | 是否误用 `hgToken`；密钥必须是 cred 接口返回的 `data.token` |
| 签名始终无效 | path 是否严格为 `/api/v1/game/player/info`，不能含域名、`?uid=` 或尾部 `/` |
| 签名始终无效 | params 是否严格为 `uid={uid}`，不能带前导 `?` |
| 签名始终无效 | 是否先把 HMAC 转为小写 hex 文本，再对该文本做 MD5 |
| 签名始终无效 | JSON 字段顺序、大小写、空字符串和紧凑格式是否一致 |
| 时间戳错误 | 是否使用秒而不是毫秒，签名和请求头是否复用同一 timestamp |
| 凭据/设备错误 | grant、cred、binding、info 是否始终使用同一个 `dId` |
| 角色不存在 | uid 是否来自 `appCode == "arknights"` 的 `bindingList` |
| grant 失败 | 请求是否发给 `as.hypergryph.com`，appCode/type 是否正确 |
| cred 失败 | 请求是否发给 `zonai.skland.com`，`kind` 是否为 `1` |
| HTTP 成功但业务失败 | 除 HTTP 状态外，还要检查响应 JSON 的 `code` 与 `message` |

另外，任何日志、异常报告或文档示例都不应记录真实密码、`hgToken`、grant code、
`cred`、`signToken` 或完整签名。它们都应按敏感凭据处理。

## 12. ArkScreen 内部实际调用时序

首次通过手机号密码导入：

```text
AccountRepositoryImpl.loginByPhonePassword
  -> generateDId
  -> fetchToken
     -> as.hypergryph.com/token_by_phone_password
  -> fetchCredInfo
     -> as.hypergryph.com/oauth2/v2/grant
     -> zonai.skland.com/generate_cred_by_code
  -> fetchPlayerBinding
     -> 签名请求 zonai.skland.com/player/binding
  -> handleBindingResponse
     -> 保存 hgToken、dId、uid、channelMasterId 等
```

已经导入账号后读取玩家信息：

```text
SklandRepositoryImpl.fetchRealTimeData / fetchCharAssets
  -> fetchCredInfo（每次重新取得 cred + signToken）
  -> fetchGameData
     -> HeaderProvider.createSignHeaders
     -> ApiService.getPlayerInfo(uid, headers)
  -> RealTimeMapper / OperatorMapper
```

## 13. 对应源码索引

- `data/network/RetrofitClient.kt`：三个相关 Retrofit base URL。
- `data/network/ApiService.kt`：登录、grant、cred、binding 和 player info 接口声明。
- `data/network/auth/HeaderProvider.kt`：固定请求头和完整签名算法。
- `data/network/auth/Utils.kt`：HMAC-SHA256、hex 与 MD5 实现。
- `data/repository/utils/SklandAuthUtils.kt`：`hgToken -> grant -> cred/signToken`。
- `data/repository/utils/DeviceIdGenerator.kt`：`dId` 生成规则。
- `data/repository/AccountRepositoryImpl.kt`：登录、binding 与账号落库链路。
- `data/repository/SklandRepositoryImpl.kt`：最终 `player/info` 请求及其参数。
- `data/repository/mapper/AccountMapper.kt`：binding 数据到明日方舟账号的映射。
- `data/time/SklandServerTimeCalibrator.kt`：服务器时间偏移计算。
- `data/time/PreferenceAppClock.kt`：签名时间戳来源。

## 14. 当前实现中值得留意的边界

1. `safeApiCall` 只统一检查 HTTP 是否成功和 body 是否为空；最终业务响应的 `code`
   是否成功，需要调用方或映射层继续确认。独立客户端应显式检查。
2. `getServerTimestamp()` 与 `getPlayerInfo()` 共用相同路径，但前者不带 uid 和鉴权，
   其目的只是从错误响应或 `Date` 头取时间。
3. Retrofit 最终会对 query 参数做 URL 编码；当前 `uid` 通常是简单角色标识。若在别的
   接口复用签名算法，必须特别确认“签名 params 字符串”与实际 query 编码规则。
4. 文档描述的是本仓库在当前版本中的实际协议实现，不代表服务端公开、稳定或永久兼容
   的 API 契约。
