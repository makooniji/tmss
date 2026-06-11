# API 安全设置

在对接WooApi集成系统之前，运营商必须向WooApi请求API凭据。

- **API Key** - 一个唯一的64个字符的字母数字字符串，用于识别运营商。
- **API Secret** - 一个唯一的64个字符的字母数字字符串，用于对请求体进行签名。

## 传输层加密

运营商与游戏聚合器之间的所有通信必须通过**HTTPS**进行保护。

这是为了防止中间人攻击和窃听。

## 验证

所有来自运营商到WooApi集成系统的API调用都必须在标头中包含X-API-Key和X-Signature。

这是为了让WooApi集成系统能够验证运营商的身份。

## X-Signature

请求标头中提供的此值用于防止数据篡改。

签名是使用**HMAC-SHA256**算法生成的。

#### 请求正文:

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3455",
    "username": "bob12345",
    "gameId": 1,
    "language": "zh",
    "platform": "web",
    "currency": "CNY"
}
```

#### API Secret:

```
813e9cb10f35c37a059c2761465781275ad641d3cb85436cdd17f08b0a6b50bf
```

{% tabs %} {% tab title="Command Line" %}

<pre><code><strong>echo -n "{\"traceId\":\"f8c3de3d-1fea-4d7c-a8b0-29f63c4c3455\",\"username\":\"bob12345\",\"gameId\":1,\"language\":\"zh\",\"platform\":\"web\",\"currency\":\"CNY\"}" | openssl dgst -sha256 -hmac "813e9cb10f35c37a059c2761465781275ad641d3cb85436cdd17f08b0a6b50bf"
</strong></code></pre>

{% endtab %} {% endtabs %}

#### 生成的签名：

```
d6b093eef96f5f2557589bd188f49d030eed994b69612fbaf3690b2b8b897362
```

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

# 集成产品与API端点兼容性详细指南

| API 端点 | 游戏产品 |
| --- | --- | --- |
| <table><thead><tr><th width="313">语言代码</th><th>语言名称</th></tr></thead><tbody><tr><td>zh-CN</td><td>简体中文</td></tr><tr><td>zh-HK</td><td>繁体中文</td></tr><tr><td>en-US</td><td>英文</td></tr><tr><td>id-ID</td><td>印度尼西亚语</td></tr><tr><td>tl-PH</td><td>塔加洛语</td></tr><tr><td>vi-VN</td><td>越南语</td></tr><tr><td>hi-IN</td><td>印地语</td></tr><tr><td>pt-PT</td><td>葡萄牙语</td></tr><tr><td>th-TH</td><td>泰语</td></tr><tr><td>es-ES</td><td>西班牙语</td></tr><tr><td>ko-KR</td><td>韩语</td></tr><tr><td>ja-JP</td><td>日语</td></tr><tr><td>tr-TR</td><td>土耳其语</td></tr><tr><td>fr-FR</td><td>法语</td></tr><tr><td>de-DE</td><td>德语</td></tr><tr><td>it-IT</td><td>意大利语</td></tr><tr><td>my-MM</td><td>缅甸语</td></tr><tr><td>ru-RU</td><td>俄语</td></tr><tr><td>pl-PL</td><td>波兰语</td></tr><tr><td>bn-BD</td><td>孟加拉语</td></tr><tr><td>fi-FI</td><td>芬兰语</td></tr></tbody></table> | `/wallet/adjustment` | <ul><li><strong>PragmaticPlay</strong> 真人游戏</li></ul> |
| `/wallet/bet_debit` | <ul><li><strong>CQ9</strong> 捕鱼游戏</li></ul> |
| `/wallet/bet_credit` | <ul><li><strong>CQ9</strong> 捕鱼游戏</li></ul> |

# 支持语言

<table><thead><tr><th width="313">语言代码</th><th>语言名称</th></tr></thead><tbody><tr><td>zh</td><td>简体中文</td></tr><tr><td>hk</td><td>繁体中文</td></tr><tr><td>en</td><td>英文</td></tr><tr><td>id</td><td>印度尼西亚语</td></tr><tr><td>tl</td><td>塔加洛语</td></tr><tr><td>vi</td><td>越南语</td></tr><tr><td>hi</td><td>印地语</td></tr><tr><td>pt</td><td>葡萄牙语</td></tr><tr><td>th</td><td>泰语</td></tr><tr><td>es</td><td>西班牙语</td></tr><tr><td>ko</td><td>韩语</td></tr><tr><td>ja</td><td>日语</td></tr><tr><td>tr</td><td>土耳其语</td></tr><tr><td>fr</td><td>法语</td></tr><tr><td>de</td><td>德语</td></tr><tr><td>it</td><td>意大利语</td></tr><tr><td>my</td><td>缅甸语</td></tr><tr><td>ru</td><td>俄语</td></tr><tr><td>pl</td><td>波兰语</td></tr><tr><td>bn</td><td>孟加拉语</td></tr><tr><td>fi</td><td>芬兰语</td></tr></tbody></table>

# 支持货币

<table><thead><tr><th width="313">货币代码</th><th>货币名称</th></tr></thead><tbody><tr><td>PHP</td><td>菲律宾比索</td></tr><tr><td>IDR(K)</td><td>印度尼西亚盾</td></tr><tr><td>VND(K)</td><td>越南盾</td></tr><tr><td>INR</td><td>印度卢比</td></tr><tr><td>BRL</td><td>巴西雷亚尔</td></tr><tr><td>MYR</td><td>马来西亚令吉</td></tr><tr><td>THB</td><td>泰国铢</td></tr><tr><td>MXN</td><td>墨西哥比索</td></tr><tr><td>COP(K)</td><td>哥伦比亚比索</td></tr><tr><td>NGN</td><td>尼日利亚奈拉</td></tr><tr><td>BWP</td><td>博茨瓦纳普拉</td></tr><tr><td>ZAR</td><td>南非兰特</td></tr><tr><td>GHS</td><td>加纳塞地</td></tr><tr><td>KES</td><td>肯尼亚先令</td></tr><tr><td>CNY</td><td>人民币</td></tr><tr><td>JPY</td><td>日元</td></tr><tr><td>MMK (K)</td><td>缅元</td></tr><tr><td>KRW</td><td>韩元</td></tr><tr><td>HKD</td><td>港元</td></tr><tr><td>TRY</td><td>土耳其里拉</td></tr><tr><td>USD</td><td>美元</td></tr><tr><td>USDT</td><td>美元锚定币</td></tr></tbody></table>

# 支持平台

<table><thead><tr><th width="313">平台代码</th><th>平台名称</th></tr></thead><tbody><tr><td>H5</td><td>移动网络</td></tr><tr><td>WEB</td><td>网络</td></tr></tbody></table>

# 术语表

<table><thead><tr><th width="233">术语</th><th>定义</th></tr></thead><tbody><tr><td>API Key (X-API-Key)</td><td><p>这是一个唯一的64个字符的字母数字字符串，用于标识发起API调用的运营商系统。</p><p>由WooApi集成系统生成，并在钱包集成过程中提供给运营商。</p></td></tr><tr><td>API Secret</td><td><p>这是一个唯一的64个字符的字母数字字符串，用于通过<strong>HMAC-SHA256</strong>算法生成数字签名。</p><p>这个值由WooApi集成系统生成，并在钱包集成过程中提供给运营商。</p><p>它必须在运营商的系统中<strong>安全</strong>存储。</p></td></tr><tr><td>X-Signature</td><td>这是使用<strong>HMAC-SHA256</strong>算法生成的数字签名。通过使用运营商的API秘钥对API的请求体进行签名而产生。</td></tr><tr><td>Whitelist</td><td>加白名单</td></tr><tr><td>Operator</td><td>运营商</td></tr><tr><td>HTTPS</td><td>超文本传输安全协议。通信协议使用SSL证书进行加密，以防止中间人攻击和窃听。</td></tr><tr><td>traceId</td><td>每个请求的唯一标识符。</td></tr></tbody></table>
