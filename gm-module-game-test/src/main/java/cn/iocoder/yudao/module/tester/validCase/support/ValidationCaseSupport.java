package cn.iocoder.yudao.module.tester.validCase.support;

import cn.iocoder.yudao.framework.common.model.game.push.DownstreamResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.framework.common.exception.GameException;

import cn.iocoder.yudao.module.system.model.game.SportContext;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/** 验证案例公共工具：SpEL 规则、预期展示 JSON、下游余额回写等。 */
@UtilityClass
public class ValidationCaseSupport {

    public static String statusRule(String status) {
        return "status == '" + status + "'";
    }

    public static String statusRule(PlatformCodeEnum status) {
        return statusRule(status.getCode());
    }

    /** 下游余额成功：校验 status + data.username/currency/balance */
    public static String okBalanceDataRule() {
        return "status == 'OK' && data != null && data.username == #expectedUsername "
                + "&& data.currency == #expectedCurrency && data.balance != null";
    }

    /** 绑定 SpEL 变量 #expectedUsername / #expectedCurrency */
    public static void bindExpectedUser(SportContext ctx) {
        ctx.getVars().put("expectedUsername", ctx.getPullUsername());
        ctx.getVars().put("expectedCurrency", ctx.getWrapCurrency());
    }

    /** 用本步真实下游响应构造 WS 预期展示 JSON（traceId 优先取响应，否则用本步 context） */
    public static String expectedDisplayFromResponse(DownstreamResult response, String stepTraceId) {
        DownstreamResult display = new DownstreamResult();
        display.setStatus(response.getStatus());
        display.setTraceId(response.getTraceId() != null ? response.getTraceId() : stepTraceId);
        if (response.getData() != null) {
            DownstreamResult.BaseData data = new DownstreamResult.BaseData();
            data.setUsername(response.getData().getUsername());
            data.setCurrency(response.getData().getCurrency());
            data.setBalance(response.getData().getBalance());
            data.setTimestamp(response.getData().getTimestamp());
            display.setData(data);
        }
        return JsonUtils.toJsonString(display);
    }

    public static void applyBalanceIfPresent(DownstreamResult response, SportContext context) {
        if (response != null && response.getData() != null && response.getData().getBalance() != null) {
            context.setBalance(response.getData().getBalance());
        }
    }

    public static void saveBalanceToVarIfPresent(DownstreamResult response, SportContext context, String varName) {
        if (response != null && response.getData() != null && response.getData().getBalance() != null) {
            context.getVars().put(varName, response.getData().getBalance());
        }
    }

    public static BigDecimal balanceOrThrow(SportContext context) {
        BigDecimal balance = context.getBalance();
        if (balance == null) {
            throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
        }
        return balance;
    }

}
