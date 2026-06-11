# 交易 API

## 交易 API

## 返回特定时间段内的交易列表

<mark style="color:green;">`POST`</mark> `https://<WooApi_site>/transaction/list`&#x20;

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
    "headers": {
      "betId": 0,
      "roundId": 1,
      "externalTransactionId": 2,
      "username": 3,
      "currencyCode": 4,
      "gameCode": 5,
      "vendorCode": 6,
      "gameCategoryCode": 7,
      "betAmount": 8,
      "winAmount": 9,
      "winLoss": 10,
      "effectiveTurnover": 11,
      "jackpotAmount": 12,
      "status": 13,
      "vendorBetTime": 14,
      "vendorSettleTime": 15,
      "isFreeSpin": 16,
      "vendorBetId": 17
    },
    "transactions": [
      [
        "3358049710",
        "8865512900",
        "8865512900",
        "alex50",
        "CNY",
        "PP_vs7monkeys",
        "PP",
        "SLOTS",
        5.6,
        11.0,
        5.4,
        5.6,
        0.0,
        2,
        1681467405636,
        1681467405862,
        "TRUE",
        "8865512900"
      ]
    ],
    "currentPage": 1,
    "totalItems": 0,
    "totalPages": 0
  }
}
```

<!-- ====================END==================== -->

## 获取特定投注交易的详细交易信息&#x20;

<mark style="color:green;">`POST`</mark> `https://<WooApi_site>/v2/transaction/detail`

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
  "traceId": "6caee2f4-f636-449d-be67-e81d63394dd0",
  "status": "OK",
  "message": "Successful response.",
  "data": {
    "detailUrl": "https://public.pg-redirect.net/history/redirect.html?trace_id=64591026-ec82-4b76-9cca-958bfce047d3&t=7D1DE1DD-883F-44BA-8003-5312F15024CF&psid=1646444472307331072&sid=1646444515999514624&lang=zh&type=operator",
    "betDetail": {
      "betId": "1646444515999514624",
      "externalTransactionId": "1646444515999514624",
      "roundId": "1646444472307331072",
      "username": "dfasdfgasdfasdf",
      "currencyCode": "CNY",
      "gameCode": "PGS_100",
      "vendorCode": "PGS",
      "gameCategoryCode": "SLOTS",
      "betAmount": 1010.0,
      "winAmount": 0.0,
      "winLoss": -1010.0,
      "effectiveTurnover": 1010.0,
      "jackpotAmount": 0.0,
      "refundAmount": 0.0,
      "status": 2,
      "isFreeSpin": "TRUE",
      "vendorBetTime": 1672985880016,
      "vendorSettleTime": 1672985880016
    }
  }
}
```

<!-- ====================END==================== -->
