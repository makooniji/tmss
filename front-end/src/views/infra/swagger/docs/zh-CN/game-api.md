# 游戏 API

## Games API

## 请求游戏落地页URL

<mark style="color:green;">`POST`</mark> `https://<WooApi_site>/game/url`

游戏运营商将打开一个新窗口，使用提供的游戏URL，以便用户访问游戏。

部分厂商在返回 gameUrl 之前，会先进行钱包余额校验。

在这种情况下，只有当 /balance 校验成功后，才会返回 gameUrl。

目前具有此行为的厂商包括：JILI、TADA、CQ9、JDB。

请确保：

- 在调用此 API 前，/balance 接口已正常接入且响应稳定
- /balance 接口超时或失败，可能会导致游戏无法正常启动

此行为为厂商特定逻辑，可能不适用于所有集成。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-API-Key<mark style="color:red;">\*</mark> | String | 运营商的API密钥 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| username<mark style="color:red;">\*</mark> | String | 运营商系统中用户的用户名。 |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符 (UUID)。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| language<mark style="color:red;">\*</mark> | String | <p>游戏应该打开的选择语言。<br><br>默认：'en'</p> |
| platform<mark style="color:red;">\*</mark> | String | <p>游戏将在其上打开的平台。</p><p>可能的值：</p><p>1. web (默认)</p><p>2. H5</p> |
| currency<mark style="color:red;">\*</mark> | String | ISO-4217货币代码（例如，USD） |
| lobbyUrl<mark style="color:red;">\*</mark> | String | 运营商网站URL，以将用户带回游戏大厅。 |
| ipAddress<mark style="color:red;">\*</mark> | String | 用户所在地的IP地址，可以是IPv4或IPv6格式。 |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
  "status": "OK",
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "data": {
    "gameUrl": "https://zf006.prerelease-env.biz/gs2c/playGame.do?key=token%3Df8c3de3d-1fea-4d7c-a8b0-29f63c4ab146%26symbol%3Dvswaysfltdrg%26platform%3DWEB%26language%3Dzh%26currency%3DCNY&stylename=zf06_rtw015sw",
    "token": "f8c3de3d-1fea-4d7c-a8b0-29f63c4ab146"
  }
}
```

<!-- ====================END==================== -->

<!-- ==================验证失败=================== -->

#### 200: OK 未经授权的访问

```json
{
  "status": "INVALID_OPERATOR",
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

<!-- ====================END==================== -->

<!-- ==================验证失败=================== -->

#### 200: OK 验证失败

```json
{
  "status": "INVALID_SIGNATURE",
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

<!-- ====================END==================== -->

<!-- ==================不支持该游戏=================== -->

#### 200: OK 不支持该游戏

```json
{
  "status": "INVALID_GAME",
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

<!-- ====================END==================== -->

<!-- ==================不支持该货币=================== -->

#### 200: OK 不支持该货币

```json
{
  "status": "WRONG_CURRENCY",
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

<!-- ====================END==================== -->

<!-- ==================请求无效=================== -->

#### 200: OK 请求无效

```json
{
  "status": "INVALID_REQUEST",
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "validation": {
    "username": "Invalid value."
  }
}
```

<!-- ====================END==================== -->

<!-- ==================未知错误=================== -->

#### 200: OK 未知错误

```json
{
  "status": "UNKNOWN_ERROR",
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

<!-- ====================END==================== -->

## 返回运营商支持的供应商列表

<mark style="color:green;">`POST`</mark> `https://<WooApi_site>/game/vendors`

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-API-Key<mark style="color:red;">\*</mark> | String | 运营商的API密钥 |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符 (UUID)。 |
| language | String | 用于本地化游戏供应商名称翻译的语言代码。 |
| currency | String | ISO-4217货币代码（例如，USD） |

#### 200: OK 响应成功

```json
{
  "traceId": "948f8c02-e549-438e-92f5-2ce4295f4730",
  "status": "OK",
  "message": "Successful response.",
  "data": [
    {
      "vendorCode": "ONEAPI",
      "vendorName": "ONEAPI Slots",
      "supportLang": ["zh-CN", "en-US"],
      "supportCurrency": []
    },
    {
      "vendorCode": "SABA",
      "vendorName": "Saba Sports",
      "supportLang": ["zh-CN", "en-US"],
      "supportCurrency": []
    },
    {
      "vendorCode": "PG",
      "vendorName": "PG Slots",
      "supportLang": ["zh-CN", "en-US", "cs-CS"],
      "supportCurrency": ["PKR", "CNY"]
    }
  ]
}
```

## 返回运营商支持的游戏列表

<mark style="color:green;">`POST`</mark> `https://<WooApi_site>/game/list`

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-API-Key<mark style="color:red;">\*</mark> | String | 运营商的API密钥 |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符 (UUID)。 |
| vendorCode<mark style="color:red;">\*</mark> | String | 供应商代码 |
| pageNo<mark style="color:red;">\*</mark> | Integer | 页码 |
| pageSize | Integer | 页大小。默认：100 |
| language | String | 用于本地化游戏名称和图像文本翻译的语言代码 |
| currency | String | ISO-4217货币代码（例如，USD） |

#### 200: OK 响应成功

```json
{
  "traceId": "77f5520a-a8f1-47b3-8953-2e95e298e9a6",
  "status": "OK",
  "message": "Successful response.",
  "data": {
    "currentPage": 1,
    "total": 3,
    "list": [
      {
        "vendorCode": "ONEAPI",
        "gameName": "Fortune Ox",
        "gameCode": "PGS_98",
        "categoryCode": "Electronic,Slots",
        "logo": "https://cdn.iaming-fun.com/igaming/3b8d5e44652a48e5b7ab0c17cabe4ff3.png",
        "supportLang": ["zh-CN", "en-US"],
        "supportCurrency": ["MYR", "CNY", "PKR", "INR"]
      },
      {
        "vendorCode": "ONEAPI",
        "gameName": "Jack Frost's Winter",
        "gameCode": "PGS_95",
        "categoryCode": "Electronic,Slots",
        "logo": "https://cdn.iaming-fun.com/igaming/b04bf3d698914a46a6355ad10edf338b.png",
        "supportLang": ["zh-CN", "en-US"],
        "supportCurrency": ["MYR", "CNY", "PKR", "INR"]
      },
      {
        "vendorCode": "DB",
        "gameName": "Majestic Treasures",
        "gameCode": "FDNI15",
        "categoryCode": "Electronic,Slots",
        "logo": "https://cdn.iaming-fun.com/igaming/ded0a86d0c244b2992627c072d4004f2.png",
        "supportLang": ["zh-CN", "en-US"],
        "supportCurrency": ["MYR", "CNY", "PKR", "INR"]
      }
    ]
  }
}
```

## 终止玩家的游戏会话

<mark style="color:green;">`POST`</mark> `https://<WooApi_site>/game/terminate`

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-API-Key<mark style="color:red;">\*</mark> | String | 运营商的API密钥 |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符 (UUID)。 |
| username<mark style="color:red;">\*</mark> | String | 运营商系统中用户的用户名。 |

#### 200: OK 响应成功

```json
{
  "traceId": "639cb889-1a49-448b-a1e0-82e9d2ecacb5",
  "status": "OK",
  "message": "Successful response."
}
```
