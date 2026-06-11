# 体育博彩 API

## 投注：一个从钱包余额中扣款的投注交易

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/bet`

当用户进行投注时，由WooApi系统调用。\
运营商需要从用户的钱包中扣除指定的投注金额`betAmount`，并返回更新后的余额。

每笔下注交易由`transactionId`标识，运营商应该验证该下注交易之前是否已经处理过。

对于串关投注类型，请求参数中包含一个 `multipleBetIds` 数组，该数组由包含 `betId` 和 `betAmount` 的对象组成。运营方必须适当处理此数组中的每个投注，确保所有指定的投注金额被扣除，并处理相应的投注ID `betId`。

<mark style="color:red;">**重要提示**</mark>**：**&#x6B64;API调用是**幂等的**，使用相同的交易ID`transactionId`进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-Api-Key | String | 运营商的API密钥 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于标识此下注请求交易的唯一ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| betAmount<mark style="color:red;">\*</mark> | Decimal | 下注交易的金额 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| betType | Integer | <ol><li>单式投注</li><li>串关投注</li></ol> |
| oddsType | Integer | <p>赔率类型<br>0 : 特殊赔率（或平台自定义）<br>1 : 马来盘<br>2 : 中国盘（大陆盘）<br>3 : 欧洲盘（十进制盘）<br>4 : 印尼盘<br>5 : 美式盘<br>6 : 欧赔（欧式盘）<br>7 : 香港盘<br>999 : 未知赔率类型</p> |
| odds | Big Decimal | <p>赔率数值</p><ul><li>当 <strong><code>betType</code></strong> = 2（串关 / 过关 / Parlay）时，系统返回的赔率为：<br>总赔率（Total Odds / 串关总倍数）</li></ul> |
| timestamp<mark style="color:red;">\*</mark> | Number | 此交易的Unix时间戳（毫秒）。 |
| multipleBetIds<mark style="color:red;">\*</mark> | Array \[ { betId (String), betAmount (Decimal) } ] | <p>一个包含多个投注记录的数组。数组中的每个对象应具有以下属性：</p><ul><li><code>betId</code>：投注的唯一标识符。</li><li><code>betAmount</code>：相应投注ID<code>betId</code>所需扣除的金额。</li></ul> |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "OK",
  "data": {
    "username": "bob12345",
    "currency": "USD",
    "balance": 100.0,
    "timestamp": 1719792000000 // 钱包交易的时间戳（以毫秒为单位）
  }
}
```

<!-- ====================END==================== -->

<!-- ==================签名无效=================== -->

#### 200: OK 签名无效

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "INVALID_SIGNATURE"
}
```

<!-- ====================END==================== -->

<!-- ==================令牌无效=================== -->

#### 200: OK 令牌无效

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "INVALID_TOKEN"
}
```

<!-- ====================END==================== -->

<!-- ==================用户不存在=================== -->

#### 200: OK 用户不存在

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "USER_NOT_EXISTS"
}
```

<!-- ====================END==================== -->

<!-- ==================货币错误=================== -->

#### 200: OK 货币错误

```json
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456,
    "status": "WRONG_CURRENCY"
}
```

<!-- ====================END==================== -->

<!-- ==================请求无效=================== -->

#### 200: OK 请求无效

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "INVALID_REQUEST"
}
```

<!-- ====================END==================== -->

<!-- ==================余额不足=================== -->

#### 200: OK 余额不足

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "INSUFFICIENT_FUNDS"
}
```

<!-- ====================END==================== -->

## 确认投注：一个通过补充资金更新钱包余额的投注交易

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/update-bet`

当用户进行投注时，由WooApi系统调用。

运营商需要向用户的钱包中添加指定的金额`creditAmount`，并返回更新后的余额。

每笔下注交易由`transactionId`标识，运营商应该验证该下注交易之前是否已经处理过。<br>

<mark style="color:red;">**重要提示**</mark>**：**&#x6B64;API调用是**幂等的**，使用相同的交易ID`transactionId`进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-Api-Key | String | 运营商的API密钥 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于标识此下注请求交易的唯一ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| betAmount<mark style="color:red;">\*</mark> | Decimal | 该投注交易的初始下注金额。 |
| newBetAmount<mark style="color:red;">\*</mark> | Decimal | 投注交易的更新下注金额 |
| creditAmount<mark style="color:red;">\*</mark> | Decimal | 需要返还给用户的`betAmount`与`newbetAmount`之间的差额。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| oddsType | Integer | <p>赔率类型<br>0 : 特殊赔率（或平台自定义）<br>1 : 马来盘<br>2 : 中国盘（大陆盘）<br>3 : 欧洲盘（十进制盘）<br>4 : 印尼盘<br>5 : 美式盘<br>6 : 欧赔（欧式盘）<br>7 : 香港盘<br>999 : 未知赔率类型</p> |
| odds | Big Decimal | 赔率数值 |
| timestamp<mark style="color:red;">\*</mark> | Number | 此交易的Unix时间戳（毫秒）。 |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "OK",
  "data": {
    "username": "bob12345",
    "currency": "USD",
    "balance": 100.0,
    "timestamp": 1719792000000 // 钱包交易的时间戳（以毫秒为单位）
  }
}
```

<!-- ====================END==================== -->

<!-- ==================签名无效=================== -->

#### 200: OK 签名无效

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "INVALID_SIGNATURE"
}
```

<!-- ====================END==================== -->

<!-- ==================令牌无效=================== -->

#### 200: OK 令牌无效

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "INVALID_TOKEN"
}
```

<!-- ====================END==================== -->

<!-- ==================用户不存在=================== -->

#### 200: OK 用户不存在

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "USER_NOT_EXISTS"
}
```

<!-- ====================END==================== -->

<!-- ==================货币错误=================== -->

#### 200: OK 货币错误

```json
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456,
    "status": "WRONG_CURRENCY"
}
```

<!-- ====================END==================== -->

<!-- ==================请求无效=================== -->

#### 200: OK 请求无效

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "INVALID_REQUEST"
}
```

<!-- ====================END==================== -->

<!-- ==================余额不足=================== -->

#### 200: OK 余额不足

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "INSUFFICIENT_FUNDS"
}
```

<!-- ====================END==================== -->

## 退款：对投注交易的回滚操作

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/refund`

由WooApi集成系统调用，以撤销之前的投注交易。

运营商需要使用提供的下注ID`betId`查找之前的投注交易，以撤销其对用户余额的影响（贷记/借记），然后返回更新后的余额。

<mark style="color:red;">**重要提示：**</mark>此API调用是**幂等的**，使用相同的交易ID`transactionId`进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-Api-Key | String | 运营商的API密钥 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于撤销其对用户余额（存入/扣除）影响的投注交易ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| timestamp<mark style="color:red;">\*</mark> | Number | 此交易的Unix时间戳（毫秒）。 |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
    "status": "OK",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "data": {
        "username": "bob12345",
        "currency": "USD",
        "balance": 100.00，
        "timestamp": 1719792000000 // 钱包交易的时间戳（以毫秒为单位）
    }
}
```

<!-- ====================END==================== -->

## 结算投注：投注交易请求用于向用户的余额中增加资金或保持不变。

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/settled`

在接收到用户的投注结果后由WooApi集成系统调用，运营商应根据赢取金额`winAmount`调整用户的钱包余额，并返回更新后的余额。

<mark style="color:red;">**重要提示：**</mark>此API调用是**幂等的**，使用相同的交易ID`transactionId`进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-Api-Key | String | 运营商的API密钥 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于标识此下注请求交易的唯一ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| betAmount<mark style="color:red;">\*</mark> | Decimal | 下注交易的金额 |
| winAmount<mark style="color:red;">\*</mark> | Decimal | 赢取的金额，当`winAmount`金额>0时需要钱包添加金额操作 |
| effectiveTurnover<mark style="color:red;">\*</mark> | Decimal | 有效投注额的金额 |
| winLoss<mark style="color:red;">\*</mark> | Decimal | 绝对赢取或亏损的金额 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| timestamp<mark style="color:red;">\*</mark> | Number | 此交易的Unix时间戳（毫秒）。 |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "OK",
    "data": {
        "username": "bob12345",
        "currency": "USD",
        "balance": 100.00，
        "timestamp": 1719792000000 // 钱包交易的时间戳（以毫秒为单位）
    }
}
```

<!-- ====================END==================== -->

## 未结算投注：一项将投注交易恢复为未结算状态的操作

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/unsettle`

由WooApi集成系统调用，此操作将前一投注交易恢复为未结算状态。

运营商需要利用提供的`betId`定位到之前的投注交易，撤销其对用户余额的影响（增加/减少），随后返回更新后的余额。

<mark style="color:red;">**重要提示：**</mark>此API调用是**幂等的**，使用相同的交易ID`transactionId`进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-Api-Key | String | 运营商的API密钥 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于撤销其对用户余额（存入/扣除）影响的投注交易ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| timestamp<mark style="color:red;">\*</mark> | Number | 此交易的Unix时间戳（毫秒）。 |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
  "status": "OK",
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "data": {
    "username": "bob12345",
    "currency": "USD",
    "balance": 100.0,
    "timestamp": 1719792000000 // 钱包交易的时间戳（以毫秒为单位）
  }
}
```

<!-- ====================END==================== -->

## 重新结算：一项更新的投注交易请求用于调整用户余额包括资金的增加或扣除

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/resettle`

当之前的投注交易有更新时，由WooApi集成系统调用，运营商需根据存入金额`creditAmount`调整用户钱包余额，并返回更新后的余额。

<mark style="color:red;">**重要提示：**</mark>此API调用是**幂等的**，使用相同的交易ID`transactionId`进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-Api-Key | String | 运营商的API密钥 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于标识此下注请求交易的唯一ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| betAmount<mark style="color:red;">\*</mark> | Decimal | 下注交易的金额 |
| winAmount<mark style="color:red;">\*</mark> | Decimal | 下注交易的初始赢取金额。 |
| newWinAmount<mark style="color:red;">\*</mark> | Decimal | 此新下注交易的更新赢取金额。 |
| winLoss<mark style="color:red;">\*</mark> | Decimal | 绝对赢取或亏损的金额 |
| creditAmount<mark style="color:red;">\*</mark> | Decimal | 存入金额：当`creditAmount`大于0时，运营商需要向用户的钱包添加该`creditAmount`金额。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| timestamp<mark style="color:red;">\*</mark> | Number | 此交易的Unix时间戳（毫秒）。 |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "OK",
  "data": {
    "username": "bob12345",
    "currency": "USD",
    "balance": 100.0,
    "timestamp": 1719792000000 // 钱包交易的时间戳（以毫秒为单位）
  }
}
```

<!-- ====================END==================== -->

## 调整：对某一游戏回合中赢得金额的调整。

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/adjustment`

由WooApi集成系统调用，以调整之前一轮投注交易的输赢结果。运营商需要在用户钱包中添加或扣除指定金额，并返回最新的余额。

<mark style="color:red;">**重要提示：**</mark>此API调用是**幂等的**，使用相同的交易ID`transactionId`进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |
| X-Api-Key | String | 运营商的API密钥 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| amount<mark style="color:red;">\*</mark> | Decimal | <p>此交易需调整的金额：</p><ol><li>正数（增加余额）</li><li>负数（减少余额）</li></ol> |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| timestamp<mark style="color:red;">\*</mark> | Number | 此交易的Unix时间戳（毫秒）。 |

<!-- ==================响应成功=================== -->

#### 200: OK 响应成功

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "OK",
  "data": {
    "username": "bob12345",
    "currency": "USD",
    "balance": 100.0,
    "timestamp": 1719792000000 // 钱包交易的时间戳（以毫秒为单位）
  }
}
```

<!-- ====================END==================== -->
