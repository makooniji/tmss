# 钱包 API

## 检索用户的最新余额

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/balance`

由运营商实现。WooApi集成系统将调用此端点以检索用户的最新钱包余额。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| currency<mark style="color:red;">\*</mark> | String | ISO-4217货币代码（例如，USD） |
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

<!-- ==================用户不存在=================== -->

#### 200: OK 用户不存在

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "USER_NOT_EXISTS"
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

<!-- ==================签名无效=================== -->

#### 200: OK 签名无效

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "INVALID_SIGNATURE"
}
```

<!-- ====================END==================== -->

<!-- ==================货币错误=================== -->

#### 200: OK 货币错误

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "WRONG_CURRENCY"
}
```

<!-- ====================END==================== -->

<!-- ==================请求无效=================== -->

#### 200: OK 请求无效

```json
{
  "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
  "status": "WRONG_PARAMETERS"
}
```

<!-- ====================END==================== -->

## 下注交易，从钱包余额中扣除金额

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/bet`

当用户下注时，由WooApi集成系统调用。\
运营商应该从用户的钱包中扣除给定的`amount`，并返回最新的余额。

每笔下注交易由`transactionId`标识，运营商应该验证该下注交易之前是否已经处理过。

<mark style="color:red;">**重要提示**</mark>: 此API调用是**幂等的**，使用相同的交易ID `transactionId` 进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于标识此下注请求交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| amount<mark style="color:red;">\*</mark> | Decimal | 下注交易的金额。 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| token<mark style="color:red;">\*</mark> | String | 由WooApi集成生成的用户会话令牌。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
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

## 下注交易请求，从用户余额中借记和/或贷记资金

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/bet_result`

当用户下注或收到下注结果时，由WooApi集成系统调用。\
运营商应该根据结果类型 `resultType` 更新用户的钱包余额，并返回最终余额。

<mark style="color:red;">**重要提示**</mark>: 此API调用是**幂等的**，使用相同的交易ID `transactionId` 进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于标识此下注请求交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| betAmount<mark style="color:red;">\*</mark> | Decimal | 下注交易的金额。 |
| winAmount<mark style="color:red;">\*</mark> | Decimal | 赢取的金额，当`winAmount`金额>0时需要钱包贷记操作。 |
| effectiveTurnover<mark style="color:red;">\*</mark> | Decimal | 有效投注额的金额。 |
| winLoss<mark style="color:red;">\*</mark> | Decimal | 绝对赢取或亏损的金额 |
| jackpotAmount | Decimal | 奖池金额，当`jackpotAmount`金额>0时需要钱包贷记操作。 |
| resultType<mark style="color:red;">\*</mark> | String | <p>交易过程的类型</p><p>1）"WIN" - (玩家赢得一笔下注)</p><p>2）"BET_WIN" - (玩家下注并赢得)</p><p>3）"BET_LOSE" - (玩家下注并输掉)</p><p>4）"LOSE" - (玩家输掉一笔下注)</p><p>5）"END" - (通知运营商回合已结束，不需要钱包借记或贷记操作)</p> |
| isFreespin<mark style="color:red;">\*</mark> | Integer (0,1) | 用于指示下注为免费旋转下注的状态。 |
| isEndRound<mark style="color:red;">\*</mark> | Integer (0,1) | 用于指示下注已完成的状态。 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| token<mark style="color:red;">\*</mark> | String | 由WooApi集成生成的用户会话令牌。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| betTime<mark style="color:red;">\*</mark> | Number | 此交易的初始请求的Unix时间戳（毫秒）。 |
| settledTime | Number | 下注结算的Unix时间戳（毫秒）此交易。 |
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

## 对下注交易的回滚操作

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/rollback`

由WooApi集成系统调用，以撤销先前的下注交易。\
运营商应该根据给定的下注ID找到先前的下注交易，以撤销其对用户余额的影响（借记/贷记），并返回最新的余额。

<mark style="color:red;">**重要提示**</mark>: 此API调用是**幂等的**，使用相同的交易ID进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于撤销其对用户余额的影响（借记/贷记）的下注交易ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| username<mark style="color:red;">\*</mark> | String | 运营商系统中用户的用户名。 |
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

## 对游戏回合中赢取金额的调整

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/adjustment`

由WooApi集成系统调用，以调整先前下注交易的赢取/输掉结果。运营商应该在用户的钱包中添加或扣除给定的金额，并返回最新的余额。

此API端点支持特定游戏产品；完整的支持产品列表请参阅《[ji-cheng-chan-pin-yu-api-duan-dian-jian-rong-xing-xiang-xi-zhi-nan](https://apidoc-gasea.gitbook.io/zh-operator-seamless-wallet-api-documentation-v3/api-reference/ji-cheng-chan-pin-yu-api-duan-dian-jian-rong-xing-xiang-xi-zhi-nan 'mention')》页面。

<mark style="color:red;">**重要提示**</mark>: 此API调用是幂等的，使用相同的交易ID进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 运营商系统中用户的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 要调整的先前交易的游戏回合ID。 |
| amount<mark style="color:red;">\*</mark> | Decimal | <p>此交易的调整金额</p><ol><li>正数 (增加余额)</li><li>负数 (扣除余额)</li></ol> |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中的唯一游戏标识符。 |
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

## 扣款转账：入场时的扣除余额操作

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/bet_debit`

当用户进入游戏房间并将额度转入游戏时，由WooApi集成系统调用。运营商需从用户钱包中扣除指定金额，并返回用户的最新余额。

此API端点支持特定游戏产品；完整的支持产品列表请参阅《[ji-cheng-chan-pin-yu-api-duan-dian-jian-rong-xing-xiang-xi-zhi-nan](https://apidoc-gasea.gitbook.io/zh-operator-seamless-wallet-api-documentation-v3/api-reference/ji-cheng-chan-pin-yu-api-duan-dian-jian-rong-xing-xiang-xi-zhi-nan 'mention')》页面。

<mark style="color:red;">**重要提示**</mark>: 此API调用是**幂等的**，使用相同的交易ID `transactionId` 进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| takeAll | Integer (0,1) | <p>用于指示是全额扣款还是部分扣款状态：</p><ul><li><strong>true</strong> (<code>1</code>)，则扣除用户钱包的全部余额 ，<code>amount</code> 会设置为 0。</li><li><strong>false</strong> (<code>0</code>), 则根据提供的 <code>amount</code> 扣除部分金额。</li></ul> |
| amount<mark style="color:red;">\*</mark> | Decimal | 从用户钱包中扣除的金额 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中的唯一游戏标识符。 |
| token<mark style="color:red;">\*</mark> | String | 由WooApi集成生成的用户会话令牌。 |
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
    "balance": 100.0, // 扣款后的用户最新余额
    "timestamp": 1719792000000 // 钱包交易的时间戳（以毫秒为单位）
  }
}
```

<!-- ====================END==================== -->

## 加款转账：完成投注结算并更新用户余额操作

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/bet_credit`

由WooApi集成系统调用，用于在游戏回合结束或需要余额调整时更新玩家的钱包余额。该端点可用于结算投注和中奖记录或处理余额调整。运营方必须将 `amount` 参数中指定的剩余余额加回到用户的钱包中。`betAmount` 和 `winAmount` 参数仅供参考，无需进一步处理。

此API端点支持特定游戏产品；完整的支持产品列表请参阅《[ji-cheng-chan-pin-yu-api-duan-dian-jian-rong-xing-xiang-xi-zhi-nan](https://apidoc-gasea.gitbook.io/zh-operator-seamless-wallet-api-documentation-v3/api-reference/ji-cheng-chan-pin-yu-api-duan-dian-jian-rong-xing-xiang-xi-zhi-nan 'mention')》页面。

<mark style="color:red;">**重要提示**</mark>: 此API调用是**幂等的**，使用相同的交易ID `transactionId` 进行多次调用不应再次处理，如果运营商之前成功处理过该交易，则应返回用户的最新余额（SC_OK）。

#### Headers

| Name | Type | Description |
| --- | --- | --- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json |
| X-Signature<mark style="color:red;">\*</mark> | String | 使用HMAC-SHA256算法生成的数字签名，使用运营商的API秘钥对请求体进行签名。 |

#### Request Body

| Name | Type | Description |
| --- | --- | --- |
| traceId<mark style="color:red;">\*</mark> | String | 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。 |
| username<mark style="color:red;">\*</mark> | String | 在运营商系统中的用户名。 |
| transactionId<mark style="color:red;">\*</mark> | String | 用于标识此交易的唯一ID。 |
| betId<mark style="color:red;">\*</mark> | String | 用于标识此下注请求交易的唯一ID。 |
| externalTransactionId<mark style="color:red;">\*</mark> | String | 由游戏供应商提供的外部交易ID。 |
| roundId<mark style="color:red;">\*</mark> | String | 用于将所有下注和赢取分组在单个回合中的游戏回合ID。 |
| isRefund<mark style="color:red;">\*</mark> | Integer (0,1) | <p>状态指示此请求是否为先前扣款交易的退款请求。</p><ul><li><strong>true</strong> (<code>1</code>)：表示这是针对之前扣款的退款请求。</li><li><strong>false</strong> (<code>0</code>)：表示这不是退款请求。</li></ul> |
| amount<mark style="color:red;">\*</mark> | Decimal | 游戏中的剩余余额，将加回用户钱包 |
| betAmount<mark style="color:red;">\*</mark> | Decimal | 下注交易的金额。 |
| winAmount<mark style="color:red;">\*</mark> | Decimal | 赢取的金额。 |
| effectiveTurnover<mark style="color:red;">\*</mark> | Decimal | 有效投注额的金额。 |
| winLoss<mark style="color:red;">\*</mark> | Decimal | 绝对赢取或亏损的金额 |
| jackpotAmount | Decimal | 奖池金额，当`jackpotAmount`金额>0时需要钱包贷记操作。 |
| currency<mark style="color:red;">\*</mark> | String | 用于表示此交易所使用的货币的ISO-4217货币代码。 |
| token<mark style="color:red;">\*</mark> | String | 由WooApi集成生成的用户会话令牌。 |
| gameCode<mark style="color:red;">\*</mark> | String | 在WooApi集成系统中选定游戏的游戏代码。 |
| betTime<mark style="color:red;">\*</mark> | Number | 此交易的初始请求的Unix时间戳（毫秒）。 |
| settledTime | Number | 下注结算的Unix时间戳（毫秒）此交易。 |
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
