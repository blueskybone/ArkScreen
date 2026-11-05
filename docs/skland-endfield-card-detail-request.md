# 森空岛终末地角色详情请求

本文记录 2026-10-29 使用国服森空岛账号实际验证成功的请求方式。接口目前未接入 ArkScreen 的 `ApiService`；验证时复用了项目已有的账号授权和签名算法。接口属于非公开协议，后续可能变化。

## 接口

```http
GET https://zonai.skland.com/api/v1/game/endfield/card/detail?roleId={roleId}&serverId={serverId}
sk-game-role: 3_{roleId}_{serverId}
```

`roleId` 和 `serverId` 必须取自绑定列表中 `appCode == "endfield"` 的 `bindingList[].roles[]`。绑定项自身的 `uid` 与游戏角色的 `roleId` 不同；此次验证不需要把 `uid` 加进查询参数。查询参数的顺序和签名时使用的参数字符串应一致，本次成功请求使用 `roleId={roleId}&serverId={serverId}`。

## 从账号 token 到详情

1. 生成或复用一个 `dId`。项目生成方式是 `"BL" + Base64(32 个安全随机字节)`；同一次链路中的请求使用同一个值。
2. `POST https://as.hypergryph.com/user/oauth2/v2/grant`，JSON body 为 `{"appCode":"4ca99fa6b56cc2ba","token":"{鹰角账号 token}","type":0}`，请求头带 `dId`，取得 grant code。
3. `POST https://zonai.skland.com/api/v1/user/auth/generate_cred_by_code`，JSON body 为 `{"code":"{grant code}","kind":1}`，请求头带相同的 `dId`，取得 `cred` 和签名 token。
4. 对 `GET /api/v1/game/player/binding` 签名并请求，从终末地绑定项的 `roles[]` 取 `roleId`、`serverId`。
5. 对上述 `card/detail` 路径和查询参数签名，带上 `sk-game-role` 后发起 GET。

项目中已有前三步和绑定列表的实现：[鉴权数据源](../app/src/main/java/com/blueskybone/arkscreen/data/network/auth/SklandAuthRemoteDataSource.kt)、[账号仓库](../app/src/main/java/com/blueskybone/arkscreen/data/repository/AccountRepositoryImpl.kt)。

## 详情请求的签名

使用项目现有的 `HeaderProvider.createSignHeaders` 规则：

```text
path      = "/api/v1/game/endfield/card/detail"
params    = "roleId={roleId}&serverId={serverId}"
timestamp = 当前 Unix 秒级时间戳的十进制字符串
jsonArgs  = "{\"platform\":\"\",\"timestamp\":\"{timestamp}\",\"dId\":\"{dId}\",\"vName\":\"\"}"
message   = path + params + timestamp + jsonArgs
hmacHex   = 小写十六进制文本(HMAC-SHA256(签名 token, UTF-8(message)))
sign      = 小写十六进制文本(MD5(UTF-8(hmacHex)))
```

签名 token 是 `generate_cred_by_code` 返回的 `data.token`，不是最初的鹰角账号 token。请求头如下；`timestamp` 和签名输入必须使用同一个值：

```http
cred: {cred}
sign: {sign}
timestamp: {timestamp}
dId: {dId}
platform:
vName:
sk-game-role: 3_{roleId}_{serverId}
User-Agent: Skland/1.5.1 (com.hypergryph.skland; build:100501001; Android 33; ) Okhttp/4.11.0
Content-Type: application/json
```

签名实现见 [HeaderProvider.kt](../app/src/main/java/com/blueskybone/arkscreen/data/network/auth/HeaderProvider.kt)。

## 本地验证结果与数据文件

这次请求返回 HTTP 200、业务码 `0`，完整原始 JSON 已格式化保存到项目根目录的 `.local-data/skland-endfield-card-detail.json`。该文件包含账号游戏数据，`.local-data/` 已在项目 `.gitignore` 中，不会被 Git 跟踪。文档不记录实际 token、`cred`、签名或角色 ID。

国际服的[公开 API 整理](https://github.com/AixLnyt/skport-api-docs)也记录了同名路径及 `roleId`、`serverId`、`sk-game-role` 参数；国服域名和上述签名请求已由本次实际调用验证。
