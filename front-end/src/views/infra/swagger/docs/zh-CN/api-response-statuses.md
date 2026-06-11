# API响应状态

<table><thead><tr><th width="313">状态代码</th><th>描述</th></tr></thead><tbody><tr><td>OK</td><td>成功响应</td></tr><tr><td>UNKNOWN_ERROR</td><td>未知错误的通用状态代码。</td></tr><tr><td>INVALID_REQUEST</td><td>请求体中发送的参数有错误/缺失。</td></tr><tr><td>AUTHENTICATION_FAILED</td><td>验证失败。X-API-Key 丢失或无效。</td></tr><tr><td>INVALID_SIGNATURE</td><td>X-Signature 验证失败。</td></tr><tr><td>INVALID_TOKEN</td><td>运营商系统中的令牌无效。</td></tr><tr><td>INVALID_GAME</td><td>游戏无效</td></tr><tr><td>DUPLICATE_REQUEST</td><td>重复的请求</td></tr><tr><td>CURRENCY_NOT_SUPPORTED</td><td>不支持该货币</td></tr><tr><td>WRONG_CURRENCY</td><td>交易的货币与用户的钱包货币不同。</td></tr><tr><td>INSUFFICIENT_FUNDS</td><td>用户的钱包资金不足。</td></tr><tr><td>USER_NOT_EXISTS</td><td>在运营商系统中用户不存在。</td></tr><tr><td>USER_DISABLED</td><td>用户已被禁用，不允许下注。</td></tr><tr><td>TRANSACTION_DUPLICATED</td><td>发送了重复的交易ID。</td></tr><tr><td>TRANSACTION_NOT_EXISTS</td><td>找不到相应的投注交易。</td></tr><tr><td>VENDOR_ERROR</td><td>游戏供应商遇到错误。</td></tr><tr><td>UNDER_MAINTENANCE</td><td>游戏正在维护中。</td></tr><tr><td>MISMATCHED_DATA_TYPE</td><td>数据类型无效。</td></tr><tr><td>INVALID_RESPONSE</td><td>响应无效</td></tr><tr><td>INVALID_VENDOR</td><td>不支持该供应商。</td></tr><tr><td>INVALID_LANGUAGE</td><td>不支持该语言。</td></tr><tr><td>GAME_DISABLED</td><td>游戏已被禁用。</td></tr><tr><td>INVALID_PLATFORM</td><td>不支持该平台。</td></tr><tr><td>GAME_LANGUAGE_NOT_SUPPORTED</td><td>不支持该游戏语言</td></tr><tr><td>GAME_PLATFORM_NOT_SUPPORTED</td><td>不支持该游戏平台</td></tr><tr><td>GAME_CURRENCY_NOT_SUPPORTED</td><td>不支持该游戏货币。</td></tr><tr><td>VENDOR_LINE_DISABLED</td><td>游戏供应商线路已被禁用。</td></tr><tr><td>VENDOR_CURRENCY_NOT_SUPPORTED</td><td>不支持该游戏供应商货币。</td></tr><tr><td>VENDOR_LANGUAGE_NOT_SUPPORTED</td><td>不支持该游戏供应商语言。</td></tr><tr><td>VENDOR_PLATFORM_NOT_SUPPORTED</td><td>不支持该游戏供应商平台。</td></tr><tr><td>TRANSACTION_STILL_PROCESSING</td><td>交易仍在处理中，请稍后重试。</td></tr><tr><td>EXCEEDED_NUMBER_OF_RETRIES</td><td>超出重试次数。</td></tr><tr><td>OPERATOR_TIMEOUT</td><td>运营商已超时。</td></tr><tr><td>INVALID_FROM_TIME</td><td>数据仅可获取最近60天。</td></tr><tr><td>INVALID_DATE_RANGE</td><td>日期范围应该在一天之内。</td></tr><tr><td>REFERENCE_ID_DUPLICATED</td><td>已发送重复的参考编号 Reference ID。</td></tr><tr><td>TRANSACTION_DOES_NOT_EXIST</td><td>无法找到相应的参考编号 Reference ID。</td></tr><tr><td>INTERNAL_ERROR</td><td>内部错误。请在相关客服渠道进行检查。</td></tr><tr><td>WALLET_NOT_SUPPORTED</td><td>不支持该钱包类型</td></tr></tbody></table>

<details>

<summary>Sample response format</summary>

```json
{
    "traceId": "e62ceec8-1c18-48fc-af0e-9f9f943b2554",
    "status": "INVALID_REQUEST",
    "message": "Wrong/missing parameters sent in request body.",
    "validation": {
        "platform": "Platform is not supported."
    }
}
```

</details>