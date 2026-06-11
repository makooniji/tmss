# K 币种处理规范

### 1. **K** 币种**概述**

为简化高面额币种（如 COP、IDR、VND 等）的处理，WooApi 在 API 交互中，对支持 K 的币种使用 K 单位（即除以 1,000 后的单位）进行传输。

示例：&#x20;

* 玩家充值 100,000 COP&#x20;
* 运营商必须向 WooApi 返回 100 COP(K)&#x20;
* 运营商内部仍需按 100,000 COP 进行存储与账务处理

当系统收到类似 "amount"="100"、"currency"="COP(K)" 的数据时，这表示该金额已转换为 K 值。玩家的实际金额为 100,000 COP，而系统在与 WooApi 交互时使用的是 100 COP(K)。

若未按以下规则处理，将导致余额错误、结算不一致，以及不可逆的审计差异。

本节说明运营商应如何在 API 请求与响应中正确进行金额换算。

### **2. 币种存储规则**

判断站点当前使用的是原始币种还是 K 币种存储方式。

<table data-full-width="true"><thead><tr><th>站点配置类型</th><th>运营商内部存储</th><th>与 WooApi 交互格式</th><th>是否需要转换</th></tr></thead><tbody><tr><td><p><strong>原始币种站点</strong></p><p>（如 COP、IDR、VND）</p></td><td>存储实际金额（100,000 COP）</td><td>按 <strong>K 单位</strong> 收/发（100 COP(K)）</td><td><strong>需要</strong>（×1,000 / ÷1,000）</td></tr><tr><td><p><strong>K 币种站点</strong></p><p>（如 COP(K)）</p></td><td>直接存储 K 单位（100 COP(K)）</td><td>与 WooApi 按同样单位交互</td><td>不需要转换</td></tr></tbody></table>

### **3. 余额处理逻辑**

#### **3.1 原始币种站点示例（Base Currency Site – COP）**

**场景：**

玩家充值 **100,000 COP**

**对接要求流程**

<table data-full-width="true"><thead><tr><th width="82.26953125">步骤</th><th width="208.27734375">系统</th><th width="306.2734375">金额</th><th>说明</th></tr></thead><tbody><tr><td>1</td><td>玩家 → 站点</td><td>100,000 COP</td><td>玩家充值原始币种金额</td></tr><tr><td>2</td><td>WooApi → 运营商</td><td>余额请求</td><td>WooApi 永远发送/请求 <strong>K 单位</strong></td></tr><tr><td>3</td><td>运营商内部处理</td><td>100,000 ÷ 1,000 = <strong>100 COP(K)</strong></td><td>运营商需进行内部换算</td></tr><tr><td>4</td><td><strong>运营商 → WooApi</strong></td><td><strong>100 COP(K)</strong></td><td>返回 K 单位余额</td></tr><tr><td>5</td><td>游戏界面</td><td>100 COP(K) 或 100,000 COP</td><td>游戏可展示 K 或原始金额</td></tr><tr><td>6</td><td>运营商后台</td><td>100,000 COP</td><td>后台需保持原始金额不变</td></tr></tbody></table>

#### **3.2 K 币站点示例（K Currency Site)**

**场景：**

玩家充值 **100 COP(K)**

<table data-full-width="true"><thead><tr><th width="82.26953125">步骤</th><th width="208.27734375">系统</th><th width="306.2734375">金额</th><th>说明</th></tr></thead><tbody><tr><td>1</td><td>玩家 → 站点</td><td>100 COP(K)</td><td>运营商已使用 K 单位</td></tr><tr><td>2</td><td>WooApi → 运营商</td><td>余额请求</td><td>无需转换</td></tr><tr><td>3</td><td><strong>运营商 → WooApi</strong></td><td><strong>100 COP(K)</strong></td><td><strong>直接返回</strong></td></tr><tr><td>4</td><td>游戏界面</td><td>100 COP(K) 或 100,000 COP</td><td>游戏可展示 K 或原始金额</td></tr></tbody></table>

### **4. 投注处理逻辑**

#### **4.1 原始币种投注示例**

**场景：**

玩家投注 **1,000 COP**

<table data-full-width="true"><thead><tr><th width="82.26953125">步骤</th><th width="208.27734375">系统</th><th width="306.2734375">金额</th><th>备注</th></tr></thead><tbody><tr><td>1</td><td>游戏界面/前端</td><td>1,000 COP</td><td>玩家投注原始金额</td></tr><tr><td>2</td><td>WooApi → 运营商</td><td>1 COP(K)</td><td>WooApi 永远发送 K 单位</td></tr><tr><td>3</td><td>运营商 </td><td>扣除 1,000 COP</td><td>运营商将 K → 原始币种</td></tr><tr><td>4</td><td><strong>运营商 → WooApi</strong></td><td>99 COP(K)</td><td>例：剩余余额 99 COP(K)</td></tr><tr><td>5</td><td>游戏界面/前端</td><td>99,000 COP</td><td>显示原始币种余额</td></tr></tbody></table>

#### **4.2 K** 币种投注示例

**场景：**

玩家投注 **1 COP(K)**

<table data-full-width="true"><thead><tr><th width="82.26953125">步骤</th><th width="208.27734375">系统</th><th width="306.2734375">金额</th><th>备注</th></tr></thead><tbody><tr><td>1</td><td>游戏界面/前端</td><td>1 COP(K)</td><td>—</td></tr><tr><td>2</td><td>WooApi → 运营商</td><td>1 COP(K)</td><td>WooApi 永远发送 K 单位</td></tr><tr><td>3</td><td>运营商 </td><td>扣除 1 COP(K)</td><td>运营商已使用 K 单位</td></tr><tr><td>4</td><td><strong>运营商 → WooApi</strong></td><td>回传 K 单位的余额</td><td>例：剩余余额 99 COP(K)</td></tr><tr><td>5</td><td>游戏界面/前端</td><td>99 COP(K)</td><td>—</td></tr></tbody></table>

### **5. 快速参考表**

<table data-full-width="true"><thead><tr><th width="204.44140625">API方向</th><th width="447.3359375">原始币种站点</th><th>K 币种站点</th></tr></thead><tbody><tr><td>WooApi → 运营商</td><td>WooApi 始终发送 K 单位 → 运营商存储前必须 ×1,000</td><td>直接存储，无需转换</td></tr><tr><td>运营商 → WooApi</td><td>发送回传前需将金额 ÷1,000</td><td>直接返回</td></tr></tbody></table>

注意：\
K 转换仅适用于与 WooApi 的 API 交互层。\
运营商应在内部保持单一金额口径（即始终使用原始币种或始终使用 K 单位），避免在不同系统层之间重复换算。

### **6.** Seamless Wallet API 行为规范

#### ⚠️ **关键规则**

对于支持 K 的币种，所有 Seamless Wallet API 的请求与响应均使用 K 单位处理。

如果运营商内部存储的是原始币种金额，则必须在内部完成相应换算。

#### 6.1 **钱包** API 转换规则

<table data-full-width="true"><thead><tr><th>API端点</th><th>请求格式</th><th>运营商内部处理</th><th>返回格式</th></tr></thead><tbody><tr><td><code>wallet/balance</code></td><td>K 单位</td><td>额度 ÷1,000</td><td>K 单位</td></tr><tr><td><code>wallet/bet</code></td><td>K 单位</td><td>直接返回</td><td>返回 K 单位余额</td></tr><tr><td><code>wallet/bet_result</code></td><td>K 单位</td><td>加减结果（×1,000）</td><td>返回 K 单位余额</td></tr><tr><td><code>wallet/rollback</code></td><td>K 单位</td><td>回滚原始额度</td><td>返回 K 单位余额</td></tr><tr><td><code>wallet/adjustment</code></td><td>K 单位</td><td>调整额度（×1,000）</td><td>返回 K 单位余额</td></tr><tr><td><code>wallet/bet_debit</code></td><td>K 单位</td><td>扣减转入金额（transfer-in ×1,000)</td><td>返回 K 单位余额</td></tr><tr><td><code>wallet/bet_credit</code></td><td>K 单位</td><td>增加转出金额（transfer-out ×1,000)</td><td>返回 K 单位余额</td></tr></tbody></table>

#### 6.2 体育博彩 API 转换规则

<table data-full-width="true"><thead><tr><th>API端点</th><th>请求格式</th><th>运营商内部处理</th><th>返回格式</th></tr></thead><tbody><tr><td><code>sports/bet</code></td><td>K 单位</td><td>扣除原始额度</td><td>返回 K 单位余额</td></tr><tr><td><code>sports/update-bet</code></td><td>K 单位</td><td>增加原始金额</td><td>返回 K 单位余额</td></tr><tr><td><code>sports/refund</code></td><td>不转换</td><td>使用已存储金额冲销原交易（并确保与先前 K 换算保持一致</td><td>返回 K 单位余额</td></tr><tr><td><code>sports/unsettle</code></td><td>不转换</td><td>使用已存储金额冲销原交易（并确保与先前 K 换算保持一致</td><td>返回 K 单位余额</td></tr><tr><td><code>sports/resettle</code></td><td>K 单位</td><td>调整结算额度</td><td>返回 K 单位余额</td></tr><tr><td><code>sports/settled</code></td><td>K 单位</td><td>更新结算额度</td><td>返回 K 单位余额</td></tr><tr><td><code>sports/adjustment</code></td><td>K 单位</td><td>调整额度</td><td>返回 K 单位余额</td></tr></tbody></table>

### 7. 注意事项

<table data-full-width="true"><thead><tr><th width="302.83203125">事项</th><th>事项</th></tr></thead><tbody><tr><td>始终确认站点的币种配置</td><td>在调用任何 API 前，必须确保 运营商 明确了解系统当前使用的是 <strong>原始币种</strong> 还是 <strong>K 币种</strong></td></tr><tr><td>API 通讯始终使用 K 单位</td><td>对于支持 K 的币种，所有 Seamless Wallet 与 WooApi 的请求与响应均以 K 单位处理。</td></tr><tr><td>前端显示转换</td><td>仅用于展示时，才需要将额度放大至原始数值（×1,000）。<br>⚠️ <strong>禁止在 API 处理逻辑中使用此转换。</strong></td></tr><tr><td>错误预防</td><td>重复转换或遗漏转换会导致余额不一致问题，<br>例如将 <strong>1 COP(K)</strong> 错误处理为 <strong>1 COP</strong>，而非 <strong>1,000 COP</strong>。</td></tr></tbody></table>