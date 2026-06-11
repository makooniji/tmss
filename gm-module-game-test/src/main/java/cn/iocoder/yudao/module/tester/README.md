# signature 子包 - JLS 平台验证案例详解

## 📖 子包概述

`signature` 子包包含了 JLS 游戏平台的所有核心验证案例实现。每个验证案例都代表 JLS 平台的一个关键操作，通过自动化测试确保这些操作的正确性和稳定性。

## 🏗️ 子包结构

```
signature/
├── BalanceValidationCase.java       # 余额查询验证
├── BetValidationCase.java           # 下注操作验证
├── RollbackValidationCase.java      # 回滚操作验证
├── BetResultValidationCase.java     # 结算操作验证
└── AdjustmentValidationCase.java    # 调整操作验证
```

## 📦 验证案例详解

### 1. BalanceValidationCase - 余额查询

**用途**: 验证用户余额查询接口的正确性

**验证内容**:
- ✅ 用户存在性验证
- ✅ 余额数据有效性
- ✅ 货币代码匹配性
- ✅ 接口响应正确性

**数据流**:
```
请求:
{
  "gameCode": "JLS",
  "currency": "CNY",
  "username": "player_001"
}
    ↓
验证逻辑:
1. 验证用户存在
2. 获取用户余额
3. 验证余额 > 0
4. 验证货币代码
    ↓
响应:
{
  "success": true,
  "balance": "1000.00",
  "currency": "CNY",
  "duration": 150
}
```

**关键代码**:
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceValidationCase implements ValidationCase {
    
    private final DSManager dsManager;
    
    @Override
    public String code() {
        return ActionEnum.BALANCE.getAction();
    }
    
    @Override
    public String vendor() {
        return "JLS";
    }
    
    @Override
    public ValidationResult execute(ValidationRunRequest request) {
        ValidationResult result = ValidationResult.success(code(), vendor(), code());
        
        try {
            // 创建上下文
            SportContext ctx = new SportContext();
            ctx.setTraceId(IdUtil.fastUUID());
            ctx.setTimestamp(Instant.now().toEpochMilli());
            ctx.setCurrency(request.getCurrency());
            
            // 调用业务逻辑
            DSBalance balance = new DSBalance();
            balance.setUsername(request.getUsername());
            
            DownstreamResult downstreamResult = dsManager.push(ctx, request.getGameCode(), 
                                                              ActionEnum.BALANCE, balance);
            
            // 验证结果
            if (downstreamResult.isSuccess()) {
                result.setBalanceValid(true);
                result.setResponseData(Map.of(
                    "balance", downstreamResult.getData().getBalance(),
                    "currency", request.getCurrency()
                ));
            } else {
                result = ValidationResult.failure(code(), vendor(), code(), 
                    "余额查询失败");
            }
            
        } catch (Exception e) {
            result = ValidationResult.failure(code(), vendor(), code(), 
                "异常: " + e.getMessage());
        }
        
        return result;
    }
}
```

**API 调用**:
```bash
POST /validation/balance
Content-Type: application/json

{
  "gameCode": "JLS",
  "currency": "CNY",
  "username": "player_001"
}
```

---

### 2. BetValidationCase - 下注操作

**用途**: 验证用户下注操作的完整性和正确性

**验证内容**:
- ✅ 下注参数完整性
- ✅ 下注金额有效性
- ✅ 用户余额充足性
- ✅ 交易记录创建
- ✅ 余额正确扣减

**数据流**:
```
请求:
{
  "gameCode": "JLS",
  "currency": "CNY",
  "username": "player_001",
  "betId": "bet_001",
  "roundId": "round_001",
  "betAmount": 100.00
}
    ↓
验证逻辑:
1. 验证参数完整 ✓
2. 检查金额有效 (> 0) ✓
3. 检查余额充足 (>= 100.00) ✓
4. 扣减用户余额 ✓
5. 创建交易记录 ✓
6. 返回新余额
    ↓
响应:
{
  "success": true,
  "transactionValid": true,
  "newBalance": "900.00",
  "betId": "bet_001",
  "duration": 200
}
```

**关键逻辑**:
```java
public ValidationResult execute(ValidationRunRequest request) {
    ValidationResult result = ValidationResult.success(code(), vendor(), code());
    
    try {
        // 1. 创建上下文
        SportContext ctx = buildContext(request);
        
        // 2. 验证参数
        if (request.getBetAmount() == null || request.getBetAmount().signum() <= 0) {
            return ValidationResult.failure(code(), vendor(), code(), 
                "下注金额必须大于 0");
        }
        
        // 3. 检查余额
        BigDecimal currentBalance = getBalance(request.getUsername());
        if (currentBalance.compareTo(request.getBetAmount()) < 0) {
            return ValidationResult.failure(code(), vendor(), code(), 
                "余额不足");
        }
        
        // 4. 执行扣减
        BigDecimal newBalance = currentBalance.subtract(request.getBetAmount());
        updateBalance(request.getUsername(), newBalance);
        
        // 5. 记录交易
        saveBetRecord(request);
        
        result.setTransactionValid(true);
        result.setResponseData(Map.of(
            "newBalance", newBalance,
            "betId", request.getBetId()
        ));
        
    } catch (Exception e) {
        result = ValidationResult.failure(code(), vendor(), code(), e.getMessage());
    }
    
    return result;
}
```

**API 调用**:
```bash
POST /validation/bet
Content-Type: application/json

{
  "gameCode": "JLS",
  "username": "player_001",
  "betId": "bet_001",
  "roundId": "round_001",
  "betAmount": 100.00,
  "validateBalance": true
}
```

---

### 3. RollbackValidationCase - 回滚操作

**用途**: 验证下注回滚的正确性（取消无效的下注）

**验证内容**:
- ✅ 原始交易存在性
- ✅ 原始金额准确性
- ✅ 余额恢复正确性
- ✅ 交易状态更新

**数据流**:
```
请求:
{
  "gameCode": "JLS",
  "username": "player_001",
  "betId": "bet_001"
}
    ↓
验证逻辑:
1. 查找原始下注记录 ✓
2. 验证记录存在 ✓
3. 获取原始金额 ✓
4. 恢复用户余额 ✓
5. 更新交易状态为已回滚 ✓
    ↓
响应:
{
  "success": true,
  "transactionValid": true,
  "refundAmount": 100.00,
  "newBalance": "1000.00",
  "duration": 150
}
```

**场景示例**:
```
场景: 用户下注被判定为无效

初始状态:
  用户余额: 1000.00

操作 1 - 下注 100.00:
  用户余额: 900.00
  交易状态: 待结算

操作 2 - 回滚:
  检查交易
  恢复金额 100.00
  用户余额: 1000.00
  交易状态: 已回滚
```

---

### 4. BetResultValidationCase - 结算操作

**用途**: 验证游戏结算的正确性和金额计算

**验证内容**:
- ✅ 结算参数完整性
- ✅ 结果类型有效性
- ✅ 金额计算正确性
- ✅ 余额更新准确性
- ✅ 结算记录创建

**金额计算公式**:
```
变化金额 = 赢金额 - 下注金额 + 奖池金额
最终余额 = 当前余额 + 变化金额
```

**数据流**:
```
请求:
{
  "gameCode": "JLS",
  "username": "player_001",
  "betId": "bet_001",
  "betAmount": 100.00,
  "winAmount": 200.00,
  "jackpotAmount": 50.00,
  "resultType": "WIN"
}
    ↓
验证逻辑:
1. 验证参数完整 ✓
2. 验证结果类型 (WIN/LOSE/END) ✓
3. 查找原始下注
4. 计算变化金额:
   200.00 - 100.00 + 50.00 = 150.00
5. 更新余额:
   900.00 + 150.00 = 1050.00
6. 记录结算结果
    ↓
响应:
{
  "success": true,
  "transactionValid": true,
  "balanceChange": 150.00,
  "newBalance": "1050.00",
  "resultType": "WIN",
  "duration": 200
}
```

**结果类型说明**:
| 类型 | 说明 | 金额变化 |
|------|------|----------|
| WIN | 玩家获胜 | +赢金额 - 下注 + 奖池 |
| LOSE | 玩家失败 | -下注金额 |
| PUSH | 平局 | 0（返还下注） |
| END | 游戏结束 | 根据规则计算 |

---

### 5. AdjustmentValidationCase - 调整操作

**用途**: 验证手动调整玩家余额的操作

**验证内容**:
- ✅ 调整金额有效性
- ✅ 调整理由完整性
- ✅ 余额更新准确性
- ✅ 调整审计记录

**数据流**:
```
请求:
{
  "gameCode": "JLS",
  "username": "player_001",
  "roundId": "round_001",
  "amount": 50.00,
  "reason": "赔付异常游戏"
}
    ↓
验证逻辑:
1. 验证金额有效 (不为 0) ✓
2. 验证理由存在 ✓
3. 计算新余额:
   current + amount
4. 更新用户余额
5. 创建调整审计记录
    ↓
响应:
{
  "success": true,
  "balanceChange": 50.00,
  "newBalance": "950.00",
  "adjustmentId": "adj_001",
  "duration": 100
}
```

**使用场景**:
- 赔付异常结算
- 修正系统错误
- 特殊活动奖励
- 投诉补偿

---

## 🔄 完整业务流程示例

### 场景: 一局完整的游戏交易

```
┌─────────────────────────────────────────┐
│ 1. 查询余额 (BalanceValidationCase)      │
│    余额: 1000.00                         │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 2. 下注 (BetValidationCase)              │
│    下注金额: 100.00                       │
│    新余额: 900.00                         │
│    状态: 待结算                           │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 3. 游戏执行 (第三方游戏平台)              │
│    生成随机结果                           │
│    结果: 获胜 (赢 200)                    │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 4. 结算 (BetResultValidationCase)        │
│    赢金额: 200.00                        │
│    计算: 200 - 100 = +100               │
│    新余额: 1000.00                       │
│    状态: 已结算                           │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 最终结果                                  │
│ 初始余额: 1000.00                        │
│ 最终余额: 1000.00                        │
│ 游戏收益: 0.00                           │
│ 交易状态: 完成 ✓                         │
└─────────────────────────────────────────┘
```

### 异常场景: 回滚处理

```
┌─────────────────────────────────────────┐
│ 1. 下注                                   │
│    余额: 900.00                          │
│    状态: 待结算                           │
└─────────────────────────────────────────┘
                ↓
        [异常发生]
                ↓
┌─────────────────────────────────────────┐
│ 2. 回滚 (RollbackValidationCase)         │
│    检测到异常交易                         │
│    执行回滚                               │
│    恢复金额: 100.00                       │
│    新余额: 1000.00                       │
│    状态: 已回滚                           │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 结果: 用户余额恢复正常                     │
└─────────────────────────────────────────┘
```

## 📊 验证案例对应关系

| 操作 | 验证案例 | 对应方法 | 关键验证 |
|------|---------|---------|----------|
| 查询余额 | BalanceValidationCase | BALANCE | 余额准确性 |
| 下注 | BetValidationCase | BET | 余额扣减 |
| 回滚 | RollbackValidationCase | ROLLBACK | 余额恢复 |
| 结算 | BetResultValidationCase | BET_RESULT | 金额计算 |
| 调整 | AdjustmentValidationCase | ADJUSTMENT | 余额更新 |

## 🎯 API 集成示例

### 使用 REST API

```bash
# 1. 查询余额
curl -X POST http://localhost:8080/validation/balance \
  -H "Content-Type: application/json" \
  -d '{"gameCode":"JLS","currency":"CNY","username":"player_001"}'

# 2. 验证下注
curl -X POST http://localhost:8080/validation/bet \
  -H "Content-Type: application/json" \
  -d '{
    "gameCode":"JLS",
    "username":"player_001",
    "betId":"bet_001",
    "betAmount":100.00
  }'

# 3. 验证结算
curl -X POST http://localhost:8080/validation/settle \
  -H "Content-Type: application/json" \
  -d '{
    "gameCode":"JLS",
    "username":"player_001",
    "betId":"bet_001",
    "winAmount":200.00,
    "resultType":"WIN"
  }'
```

### 使用 Java API

```java
@Service
public class GameValidationService {
    
    @Autowired
    private ValidationRunner validationRunner;
    
    public void validateComplete Flow() {
        ValidationRunRequest request = new ValidationRunRequest();
        request.setGameCode("JLS");
        request.setUsername("player_001");
        
        // 1. 验证余额
        request.setAction("BALANCE");
        String traceId1 = validationRunner.runAsync(request);
        
        // 2. 验证下注
        request.setAction("BET");
        request.setBetAmount(new BigDecimal("100.00"));
        String traceId2 = validationRunner.runAsync(request);
        
        // 3. 验证结算
        request.setAction("BET_RESULT");
        request.setWinAmount(new BigDecimal("200.00"));
        request.setResultType("WIN");
        String traceId3 = validationRunner.runAsync(request);
    }
}
```

## 🔍 监控和告警

### 关键指标

```
验证执行率: 成功次数 / 总次数
平均执行时间: 所有验证耗时的平均值
失败率: 失败次数 / 总次数

告警阈值:
- 失败率 > 10% → 严重告警
- 平均耗时 > 1000ms → 性能告警
- 连续失败 > 3 次 → 系统告警
```

### 日志监控

```
[INFO] 余额验证完成: duration=150ms, balance=1000.00
[INFO] 下注验证完成: duration=200ms, newBalance=900.00
[WARN] 结算验证异常: 原始下注不存在
[ERROR] 系统异常: NullPointerException
```

---

## 📚 参考资源

- [主包指南](../PACKAGE_GUIDE.md)
- [主 README](../README.md)
- [ValidationCase 接口](ValidationCase.java)
- [ValidationRunner 执行器](ValidationRunner.java)

---

**版本**: v1.0.0  
**最后更新**: 2026-03-30  
**维护者**: Game Manager Team

