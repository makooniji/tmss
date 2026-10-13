# 转账钱包 API

单一转账钱包端点

## 请求向玩家的钱包存入额度，需指定用户名、金额及货币类型。

<mark style="color:green;">`POST`</mark> `https://<wooapi_site>/gateway/cash/deposit`

OneAPI系统正在对所有API操作实施幂等操作。在以下情况下，运营商需要使用相同的参考号 referenceId重新发送请求：

*SC\_MISMATCHED\_DATA\_TYPE,  SC\_INVALID\_REQUEST, SC\_AUTHENTICATION\_FAILED, SC\_INVALID\_SIGNATURE, SC\_WRONG\_CURRENCY, SC\_CURRENCY\_NOT\_SUPPORTED*

<mark style="color:red;">**重要提示**</mark>: referenceId 是每笔交易的唯一标识符。运营商需要为每个转账请求发送新的 referenceId。

在考虑重新发送转账请求之前，建议运营商使用 '`/cash/getsingletransaction`' 端点验证交易状态，以防出现其他失败情况。

#### Headers

| Name                                           | Type   | Description                                 |
| ---------------------------------------------- | ------ | ------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                            |
| X-Signature<mark style="color:red;">\*</mark>  | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-API-Key<mark style="color:red;">\*</mark>    | String | 运营商的API密钥                                   |

#### Request Body

| Name                                             | Type   | Description                     |
| ------------------------------------------------ | ------ | ------------------------------- |
| username<mark style="color:red;">\*</mark>       | String | 运营商系统中用户的用户名                    |
| traceId<mark style="color:red;">\*</mark>        | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| transferAmount<mark style="color:red;">\*</mark> | String | 转账金额（最多8位小数，但不得低于0.00000001）    |
| currency<mark style="color:red;">\*</mark>       | String | 玩家的货币。ISO-4217货币代码（例如，USD）      |
| referenceId<mark style="color:red;">\*</mark>    | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |

{% tabs %}
{% tab title="200: OK 响应成功" %}

```javascript
{
    "traceId": "a6cb8549-bf0e-4144-8181-6e786f768d84",
    "status": "OK",
    "message": "Successful response.",
    "data": {
        "referenceId": "81a8c42e-bb71-4769-b8ea-1aaaf95834fe",
        "transactionId": "e7744bb4-469e-43db-b637-9b634b7181c6",
        "username": "bob12345",
        "currencyCode": "USD",
        "beforeBalance": 191.50,
        "afterBalance": 291.50,
        "transferAmount": 100.00,
        "timestamp": 1706505588314
    }
}
```

{% endtab %}

{% tab title="200: OK 未经授权的访问" %}

```javascript
{
    "status": "INVALID_OPERATOR",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 验证失败" %}

```javascript
{
    "status": "INVALID_SIGNATURE",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 不支持该货币" %}

```javascript
{
    "status": "WRONG_CURRENCY",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 请求无效" %}

```javascript
{
    "status": "INVALID_REQUEST",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "validation": {
        "username": "Invalid value."
    }
}
```

{% endtab %}

{% tab title="200: OK 未知错误" %}

```javascript
{
    "status": "UNKNOWN_ERROR",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 请求重复" %}

```json
{
    "traceId": "aec7f6b7-16ee-462e-8e0c-8990dcba9e07",
    "status": "DUPLICATE_REQUEST"
}
```

{% endtab %}

{% tab title="200: OK 已发送重复的参考号" %}

```json
{
    "traceId": "5a100947-df34-4695-acb1-9eb4a9b25bf7",
    "status": "REFERENCE_ID_DUPLICATED"
}
```

{% endtab %}

{% tab title="200: OK 钱包类型不受支持" %}

```
{
    "status": "WALLET_NOT_SUPPORTED",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}
{% endtabs %}

## 请求从玩家钱包提取额度，需指定用户名、金额和货币。 <a href="#transfer-withdrawal" id="transfer-withdrawal"></a>

<mark style="color:green;">`POST`</mark> `https://<wooapi_site>/gateway/cash/withdraw`

OneAPI系统正在对所有API操作实施幂等操作。在以下情况下，运营商需要使用相同的参考号 referenceId重新发送请求:&#x20;

*SC\_MISMATCHED\_DATA\_TYPE,  SC\_INVALID\_REQUEST, SC\_AUTHENTICATION\_FAILED, SC\_INVALID\_SIGNATURE, SC\_WRONG\_CURRENCY, SC\_CURRENCY\_NOT\_SUPPORTED*

<mark style="color:red;">**重要提示**</mark>: referenceId 是每笔交易的唯一标识符。运营商需要为每个转账请求发送新的 referenceId。

在考虑重新发送转账请求之前，建议运营商使用 '`/cash/getsingletransaction`' 端点验证交易状态，以防出现其他失败情况。

#### Headers

| Name                                           | Type   | Description                                 |
| ---------------------------------------------- | ------ | ------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                            |
| X-Signature<mark style="color:red;">\*</mark>  | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-API-Key<mark style="color:red;">\*</mark>    | String | 运营商的API密钥                                   |

#### Request Body

| Name                                             | Type   | Description                     |
| ------------------------------------------------ | ------ | ------------------------------- |
| username<mark style="color:red;">\*</mark>       | String | 运营商系统中用户的用户名                    |
| traceId<mark style="color:red;">\*</mark>        | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| transferAmount<mark style="color:red;">\*</mark> | String | 转账金额（最多8位小数，但不得低于0.00000001）    |
| currency<mark style="color:red;">\*</mark>       | String | 玩家的货币。ISO-4217货币代码（例如，USD）      |
| referenceId<mark style="color:red;">\*</mark>    | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |

{% tabs %}
{% tab title="200: OK 响应成功" %}

```javascript
{
    "traceId": "4214e0c8-c292-42f1-98d3-46507808eef0",
    "status": "OK",
    "message": "Successful response.",
    "data": {
        "referenceId": "1a637709-b72a-4171-99ac-66f608cee50a",
        "transactionId": "34dc1d0e-64ac-4e05-a831-8c70b76b2419",
        "username": "bob12345",
        "currencyCode": "USD",
        "beforeBalance": 291.59,
        "afterBalance": 290.59,
        "transferAmount": 1,
        "timestamp": 1706506524415
    }
}
```

{% endtab %}

{% tab title="200: OK 未经授权的访问" %}

```javascript
{
    "status": "INVALID_OPERATOR",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 验证失败" %}

```javascript
{
    "status": "INVALID_SIGNATURE",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 不支持该货币" %}

```javascript
{
    "status": "WRONG_CURRENCY",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 请求无效" %}

```javascript
{
    "status": "INVALID_REQUEST",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "validation": {
        "username": "Invalid value."
    }
}
```

{% endtab %}

{% tab title="200: OK 未知错误" %}

```javascript
{
    "status": "UNKNOWN_ERROR",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 请求重复" %}

```json
{
    "traceId": "aec7f6b7-16ee-462e-8e0c-8990dcba9e07",
    "status": "DUPLICATE_REQUEST"
}
```

{% endtab %}

{% tab title="200: OK 已发送重复的参考号" %}

```json
{
    "traceId": "5a100947-df34-4695-acb1-9eb4a9b25bf7",
    "status": "REFERENCE_ID_DUPLICATED"
}
```

{% endtab %}

{% tab title="200: OK 用户的钱包资金不足" %}

```
{
    "traceId": "9eff88b6-41bf-40d1-b593-32c95d4b362c",
    "status": "INSUFFICIENT_FUNDS",
    "message": "User's wallet does not have enough funds.",
    "data": {
        "referenceId": "398eafce-fab1-4460-a41e-2d548ef41f95",
        "transactionId": "2b61b558-7c7e-48fb-95da-824b9e60e7c1",
        "username": "bob12345",
        "currencyCode": "USD",
        "beforeBalance": 291.59,
        "afterBalance": 291.59,
        "transferAmount": 10000,
        "timestamp": 1706506887016
    }
}
```

{% endtab %}

{% tab title="200: OK 钱包类型不受支持" %}

```
{
    "status": "WALLET_NOT_SUPPORTED",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}
{% endtabs %}

## 请求检查特定玩家的钱包余额，需指定用户名和货币. <a href="#transfer-balance" id="transfer-balance"></a>

<mark style="color:green;">`POST`</mark> `https://<wooapi_site>/gateway/cash/balance`

#### Headers

| Name                                           | Type   | Description                                 |
| ---------------------------------------------- | ------ | ------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                            |
| X-Signature<mark style="color:red;">\*</mark>  | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-API-Key<mark style="color:red;">\*</mark>    | String | 运营商的API密钥                                   |

#### Request Body

| Name                                       | Type   | Description                     |
| ------------------------------------------ | ------ | ------------------------------- |
| username<mark style="color:red;">\*</mark> | String | 运营商系统中用户的用户名                    |
| traceId<mark style="color:red;">\*</mark>  | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| currency<mark style="color:red;">\*</mark> | String | 玩家的货币。ISO-4217货币代码（例如，USD）      |

{% tabs %}
{% tab title="200: OK 响应成功" %}

```javascript
{
  "traceId": "9eff88b6-41bf-40d1-b593-32c95d4b362c",
  "status": "OK",
  "data": {
    "username": "bob12345",
    "currency": "USD",
    "amount": 291.59,
    "timestamp": 1706506887016
  }
}
```

{% endtab %}

{% tab title="200: OK 未经授权的访问" %}

```javascript
{
    "status": "INVALID_OPERATOR",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 验证失败" %}

```javascript
{
    "status": "INVALID_SIGNATURE",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 不支持该货币" %}

```javascript
{
    "status": "WRONG_CURRENCY",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 请求无效" %}

```javascript
{
    "status": "INVALID_REQUEST",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "validation": {
        "username": "Invalid value."
    }
}
```

{% endtab %}

{% tab title="200: OK 未知错误" %}

```javascript
{
    "status": "UNKNOWN_ERROR",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 请求重复" %}

```json
{
    "traceId": "aec7f6b7-16ee-462e-8e0c-8990dcba9e07",
    "status": "DUPLICATE_REQUEST"
}
```

{% endtab %}

{% tab title="200: OK 钱包类型不受支持" %}

```
{
    "status": "WALLET_NOT_SUPPORTED",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}
{% endtabs %}

## 请求通过搜索参考号 referenceId 获取特定玩家的单笔存款/提款交易历史记录。 <a href="#transfer-single-transaction" id="transfer-single-transaction"></a>

<mark style="color:green;">`POST`</mark> `https://<wooapi_site>/gateway/cash/getsingletransaction`

响应参数可能会根据不同的状态而变化

transactionStatus (交易状态): PROCESSING(处理中)/SUCCESS(成功)/FAIL(失败)

transactionType (交易类型): DEPOSIT(存款)/WITHDRAWAL(取款)

#### Headers

| Name                                           | Type   | Description                                 |
| ---------------------------------------------- | ------ | ------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                            |
| X-Signature<mark style="color:red;">\*</mark>  | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-API-Key<mark style="color:red;">\*</mark>    | String | 运营商的API密钥                                   |

#### Request Body

| Name                                          | Type   | Description                     |
| --------------------------------------------- | ------ | ------------------------------- |
| username<mark style="color:red;">\*</mark>    | String | 运营商系统中用户的用户名                    |
| traceId<mark style="color:red;">\*</mark>     | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| currency<mark style="color:red;">\*</mark>    | String | 玩家的货币。ISO-4217货币代码（例如，USD）      |
| referenceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |

{% tabs %}
{% tab title="200: OK 响应成功" %}

```javascript
{
  "traceId": "9eff88b6-41bf-40d1-b593-32c95d4b362c",
  "status": "OK",
  "data": {
    "referenceId": "398eafce-fab1-4460-a41e-2d548ef41f95",
    "transactionId": "2b61b558-7c7e-48fb-95da-824b9e60e7c1",
    "transactionStatus": "SUCCESS",
    "transactionType": "DEPOSIT",
    "username": "bob12345",
    "currency": "USD",
    "beforeBalance": 291,
    "afterBalance": 293,
    "transferAmount": 2,
    "timestamp": 1706506887016
  }
}
```

{% endtab %}

{% tab title="200: OK 未经授权的访问" %}

```javascript
{
    "status": "INVALID_OPERATOR",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 验证失败" %}

```javascript
{
    "status": "INVALID_SIGNATURE",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 不支持的货币" %}

```javascript
{
    "status": "WRONG_CURRENCY",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 请求无效" %}

```javascript
{
    "status": "INVALID_REQUEST",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "validation": {
        "username": "Invalid value."
    }
}
```

{% endtab %}

{% tab title="200: OK 未知错误" %}

```javascript
{
    "status": "UNKNOWN_ERROR",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}

{% tab title="200: OK 请求重复" %}

```json
{
    "traceId": "aec7f6b7-16ee-462e-8e0c-8990dcba9e07",
    "status": "DUPLICATE_REQUEST"
}
```

{% endtab %}

{% tab title="200: OK 钱包类型不受支持" %}

```
{
    "status": "WALLET_NOT_SUPPORTED",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456"
}
```

{% endtab %}
{% endtabs %}