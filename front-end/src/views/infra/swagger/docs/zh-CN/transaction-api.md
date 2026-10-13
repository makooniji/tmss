# 交易 API

## 交易 API

## 返回特定时间段内的交易列表

<mark style="color:green;">`POST`</mark> `https://<WooApi_site>/gateway/transaction/list`&#x20;

游戏运营商可以通过结算时间查询来获取交易历史列表。&#x20;

**下注状态定义：**&#x20;

`0` = 未结算的下注

`1` = 已结算的下注

`2` = 取消的下注

`3` = 退款的下注

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-API-Key<mark style="color:red;">\*</mark> | String | 运营商的API密钥 |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| fromTime<mark style="color:red;">\*</mark> | Long | 用于检索交易开始的时间戳。 |
| toTime<mark style="color:red;">\*</mark> | Long | 用于检索交易至此的时间戳。 |
| pageNo<mark style="color:red;">\*</mark> | Integer | 页数 |
| pageSize | Integer | 页面大小。默认：2000，最大：5000 |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
  "traceId": "04cb48ce-346f-4dcc-ae49-7955805c50fc",
  "status": "OK",
  "message": "Successful response.",
  "data": {
        "currentPage": 0,
        "totalCount": {},
        "total": 0,
        "list": [
            {
                "betId": "string",
                "roundId": "string",
                "externalTransactionId": "string",
                "username": "string",
                "gameCode": "string",
                "currencyCode": "string",
                "vendorCode": "string",
                "betContent": "string",
                "betAmount": 0,
                "winAmount": 0,
                "winLoss": 0,
                "effectiveTurnover": 0,
                "jackpotAmount": 0,
                "status": 0,
                "vendorBetTime": 0,
                "vendorSettleTime": 0
            }
        ]
    }
}
```

<!-- ====================END==================== -->

## 获取特定投注交易的详细交易信息&#x20;

<mark style="color:green;">`POST`</mark> `https://<WooApi_site>/gateway/transaction/detail`

此 API 允许游戏运营商通过提供 `betId` 并指定时间范围（使用 `fromTime` 和 `toTime` 参数，均为 UNIX 时间戳格式）来获取单笔交易的详细历史记录。

`fromTime` 和 `toTime` 之间的时间范围必须在 7 天内。如果超过 7 天，查询将失败，并且不会返回任何数据。。

**下注状态定义:**

`0` = 未结算的下注

`1` = 已结算的下注

`2` = 取消的下注

`3` = 退款的下注

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-API-Key<mark style="color:red;">\*</mark> | String | 运营商的API密钥 |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| betId<mark style="color:red;">\*</mark> | String | 唯一标识此下注请求交易的ID。 |
| fromTime<mark style="color:red;">\*</mark> | Long | 查询的开始时间，UNIX 时间戳（毫秒）。 |
| toTime<mark style="color:red;">\*</mark> | Long | 查询的结束时间，UNIX 时间戳（毫秒）。 |
| displayLanguage | String | 用于本地化游戏供应商下注详情URL翻译的语言代码。 |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
    "traceId": "string",
    "status": "OK",
    "message": "string",
    "data": {
        "betId": "string",
        "roundId": "string",
        "externalTransactionId": "string",
        "username": "string",
        "gameCode": "string",
        "currencyCode": "string",
        "vendorCode": "string",
        "betContent": "string",
        "betAmount": 0,
        "winAmount": 0,
        "winLoss": 0,
        "effectiveTurnover": 0,
        "jackpotAmount": 0,
        "status": 0,
        "vendorBetTime": 0,
        "vendorSettleTime": 0
    }
}
```

<!-- ====================END==================== -->
