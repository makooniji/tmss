# K Currency Conversion and Handling

### 1. **K Currency Overview**

To simplify processing for high-denomination currencies (COP, IDR, VND, etc), WooApi uses K units (divided by 1,000) for K-supported currencies in API exchanges.

Example:

* Player deposits **100,000 COP**
* Operator must send **100 COP(K)** to WooApi
* Operator must convert to **100,000 COP** internally

When your system receives an amount like `"amount"="100"`, `"currency"="COP(K)"`, it represents a **converted K-value**, where the player’s **actual amount is 100,000 COP**, and **the system uses 100 COP(K)** for WooApi processing.

Failure to apply the rules below will cause incorrect balances, mis-settlements, and irreversible audit discrepancies.

This section explains how to convert values correctly during all API requests and responses.

<table data-full-width="true"><thead><tr><th width="244.1796875" valign="top">Concept</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top">K Currency</td><td valign="top">A compact unit representation where 1 K unit = 1,000 of the base currency (e.g., 1 COP(K) = 1,000 COP).</td></tr><tr><td valign="top">Purpose</td><td valign="top">To standardise transaction values and reduce large number handling in API transactions.</td></tr><tr><td valign="top">K-Supported Currencies</td><td valign="top">K-Supported Currencies: COP(K), IDR(K), VND(K), and other configured K-format currencies depending on vendor or operator setup.</td></tr><tr><td valign="top">Base Currency Example</td><td valign="top">COP (Colombian Peso) without K conversion (e.g., 100,000 COP).</td></tr><tr><td valign="top">K Currency Example</td><td valign="top">COP(K) — representing 1/1000th of base (e.g., 100 COP(K) = 100,000 COP).</td></tr></tbody></table>

### **2. Conversion Format Rules**

<table data-full-width="true"><thead><tr><th width="244.1796875" valign="top">Site Configuration</th><th>Operator Storage</th><th valign="top">Exchange Format with WooApi</th><th valign="top">Conversion Required</th></tr></thead><tbody><tr><td valign="top"><p><strong>Base Currency Site</strong> </p><p>(COP, IDR, VND)</p></td><td>Store full value (<code>100,000 COP</code>)</td><td valign="top">Send/receive <strong>K units</strong> (<code>100 COP(K)</code>)</td><td valign="top"><strong>YES</strong> (×1,000 / ÷1,000)</td></tr><tr><td valign="top"><p><strong>K Currency Site</strong> </p><p>(COP(K), IDR(K), VND(K))</p></td><td>Store in K units (<code>100 COP(K)</code>)</td><td valign="top">Send/receive <strong>same units</strong></td><td valign="top">NO conversion</td></tr></tbody></table>

### **3. Balance Handling Logic**

#### 3.1 Base Currency Site Example (COP)

**Scenario:**

Player deposits **100,000 COP**

**Required Integration Behavior**

<table data-full-width="true"><thead><tr><th width="119.26171875" valign="top">Step</th><th valign="top">System</th><th valign="top">Amount</th><th valign="top">Explanation</th></tr></thead><tbody><tr><td valign="top">1</td><td valign="top">Player → Operator Site</td><td valign="top">100,000 COP</td><td valign="top">Deposit in full COP value</td></tr><tr><td valign="top">2</td><td valign="top">WooApi → Operator</td><td valign="top">(Balance Request)</td><td valign="top">WooApi always uses K units</td></tr><tr><td valign="top">3</td><td valign="top">Operator</td><td valign="top">Divide by 1,000 → <strong>100 COP(K)</strong></td><td valign="top">Convert before responding</td></tr><tr><td valign="top">4</td><td valign="top"><strong>Operator → WooApi</strong></td><td valign="top"><strong>100 COP(K)</strong></td><td valign="top">WooApi receives K units</td></tr><tr><td valign="top">5</td><td valign="top">Game UI</td><td valign="top">100 COP(K) or 100,000 COP</td><td valign="top">Game may display either value</td></tr><tr><td valign="top">6</td><td valign="top">Operator UI</td><td valign="top">100,000 COP</td><td valign="top">Display original base currency</td></tr></tbody></table>

#### 3.2 K Currency Site Example

**Scenario:**

Player deposits **100 COP(K)**

<table data-full-width="true"><thead><tr><th width="119.26171875" valign="top">Step</th><th valign="top">System</th><th valign="top">Amount</th><th valign="top">Explanation</th></tr></thead><tbody><tr><td valign="top">1</td><td valign="top">Player → Operator Site</td><td valign="top">100 COP(K)</td><td valign="top">Already K units</td></tr><tr><td valign="top">2</td><td valign="top">WooApi → Operator</td><td valign="top">(Balance Request)</td><td valign="top">No conversion</td></tr><tr><td valign="top">3</td><td valign="top"><strong>Operator → WooApi</strong></td><td valign="top"><strong>100 COP(K)</strong></td><td valign="top">Return directly</td></tr><tr><td valign="top">4</td><td valign="top">Game/UI</td><td valign="top">100 COP(K) or 100,000 COP</td><td valign="top">Game may expand to full unit</td></tr></tbody></table>

### **4. Betting Handling Logic**

#### 4.1 Base Currency Betting Example

**Scenario:**

Player bets **1,000 COP**

**Required Behaviour**

<table data-full-width="true"><thead><tr><th width="119.26171875" valign="top">Step</th><th valign="top">System</th><th valign="top">Amount</th><th valign="top">Notes</th></tr></thead><tbody><tr><td valign="top">1</td><td valign="top">Game Front-end/UI</td><td valign="top">1,000 COP</td><td valign="top">—</td></tr><tr><td valign="top">2</td><td valign="top"><strong>WooApi → Operator</strong></td><td valign="top"><strong>1 COP(K)</strong></td><td valign="top">WooApi always sends K units</td></tr><tr><td valign="top">3</td><td valign="top">Operator</td><td valign="top">Deduct 1,000 COP</td><td valign="top">Operator converts K → Full</td></tr><tr><td valign="top">4</td><td valign="top"><strong>Operator → WooApi</strong></td><td valign="top">New balance in K</td><td valign="top">Example: <strong>99 COP(K)</strong></td></tr><tr><td valign="top">5</td><td valign="top">Game Front-end/UI</td><td valign="top">99,000 COP</td><td valign="top">Converted back to full</td></tr></tbody></table>

#### 4.2 K Currency Betting Example

**Scenario:**

Player bets **1 COP(K)**

<table data-full-width="true"><thead><tr><th width="119.26171875" valign="top">Step</th><th valign="top">System</th><th valign="top">Amount</th><th valign="top">Notes</th></tr></thead><tbody><tr><td valign="top">1</td><td valign="top">Game Front-end/UI</td><td valign="top">1 COP(K)</td><td valign="top">—</td></tr><tr><td valign="top">2</td><td valign="top"><strong>WooApi → Operator</strong></td><td valign="top"><strong>1 COP(K)</strong></td><td valign="top">WooApi always sends K units</td></tr><tr><td valign="top">3</td><td valign="top">Operator</td><td valign="top">Deduct 1 COP(K)</td><td valign="top">Already K units</td></tr><tr><td valign="top">4</td><td valign="top"><strong>Operator → WooApi</strong></td><td valign="top">Return balance in K</td><td valign="top">Example: <strong>99 COP(K)</strong></td></tr><tr><td valign="top">5</td><td valign="top">Game Front-end/UI</td><td valign="top">99 COP(K)</td><td valign="top">—</td></tr></tbody></table>

### **5. Quick Reference Table**

<table data-full-width="true"><thead><tr><th width="225" valign="top">Direction</th><th valign="top">Base Currency Site</th><th valign="top">K Currency Site</th></tr></thead><tbody><tr><td valign="top">WooApi → Operator</td><td valign="top">Always sends K units → Operator must <strong>×1,000</strong> before storing</td><td valign="top">Store directly (no conversion)</td></tr><tr><td valign="top">Operator → WooApi</td><td valign="top">Convert stored amount <strong>÷1,000</strong> before responding</td><td valign="top">Return directly (already K units)</td></tr></tbody></table>

Note:\
K conversion applies only at the API exchange layer with WooApi.\
Operators should maintain a single source of truth internally (either base or K units) and avoid double conversion across different system layers.

### **6. Seamless Wallet API Behavior**

**⚠️ Critical Rule**

All Seamless Wallet API requests and responses for K-supported currencies use K units.\
Operators must ensure internal conversion if storing base currency values.

#### 6.1 Wallet API Conversion Rules

<table data-full-width="true"><thead><tr><th width="169.4765625" valign="top">API Endpoint</th><th width="191.09375" valign="top">Request Format</th><th valign="top">Operator Internal Handling</th><th valign="top">Response Format</th></tr></thead><tbody><tr><td valign="top"><code>wallet/balance</code></td><td valign="top">K units</td><td valign="top">Convert stored amount ÷1,000</td><td valign="top">K units</td></tr><tr><td valign="top"><code>wallet/bet</code></td><td valign="top">K units</td><td valign="top">Deduct (amount ×1,000)</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>wallet/bet_result</code></td><td valign="top">K units</td><td valign="top">Add/subtract (×1,000)</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>wallet/rollback</code></td><td valign="top">Reference-based (may not include explicit amount)</td><td valign="top">Reverse the original transaction consistently with prior K conversion</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>wallet/adjustment</code></td><td valign="top">K units</td><td valign="top">Adjust (×1,000)</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>wallet/bet_debit</code></td><td valign="top">K units</td><td valign="top">Deduct (transfer-in ×1,000)</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>wallet/bet_credit</code></td><td valign="top">K units</td><td valign="top">Credit (transfer-out ×1,000)</td><td valign="top">Return K balance</td></tr></tbody></table>

#### 6.2 Sportsbook API Conversion Rules

<table data-full-width="true"><thead><tr><th width="169.4765625" valign="top">API Endpoint</th><th width="191.09375" valign="top">Request Format</th><th valign="top">Operator Internal Handling</th><th valign="top">Response Format</th></tr></thead><tbody><tr><td valign="top"><code>sports/bet</code></td><td valign="top">K units</td><td valign="top">Deduct full amount</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>sports/update-bet</code></td><td valign="top">K units</td><td valign="top">Credit full amount</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>sports/refund</code></td><td valign="top">No conversion</td><td valign="top">Reverse original transaction using stored value (ensure consistency with prior K conversion)</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>sports/unsettle</code></td><td valign="top">No conversion</td><td valign="top">Reverse original settlement using stored value (ensure consistency with prior K conversion)</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>sports/resettle</code></td><td valign="top">K units</td><td valign="top">Adjust full balance</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>sports/settled</code></td><td valign="top">K units</td><td valign="top">Adjust full balance</td><td valign="top">Return K balance</td></tr><tr><td valign="top"><code>sports/adjustment</code></td><td valign="top">K units</td><td valign="top">Adjust full balance</td><td valign="top">Return K balance</td></tr></tbody></table>

### **7. Key Reminders**

<table data-full-width="true"><thead><tr><th width="333.92578125" valign="top">Item</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top">Always confirm site currency configuration</td><td valign="top">Ensure the Operator knows whether the system uses Base or K Currency before calling APIs.</td></tr><tr><td valign="top">API communication always uses K units</td><td valign="top">All Seamless Wallet transactions with WooApi are processed in K units.</td></tr><tr><td valign="top">Frontend conversion</td><td valign="top">Convert back to full amount (×1,000) for player display only — not for API processing.</td></tr><tr><td valign="top">Error prevention</td><td valign="top"><p>Double conversion or missed conversion can cause mismatched balances </p><p>(e.g., 1 COP(K) = 1 COP instead of 1,000 COP).</p></td></tr></tbody></table>