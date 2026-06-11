package cn.iocoder.yudao.module.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionEnum {
    HEALTHCHECK("/healthcheck",-1,"","/healthcheck","/healthcheck"),
    BALANCE("/GetBalance", -1, "查询余额", "/wallet/balance", "/wallet/balance"),
    BET("/PlaceBet", 0, "投注", "/wallet/bet", "/sports/bet"),
    BET_UPDATE("/ConfirmBet", 0, "确认投注", "/wallet/update-bet", "/sports/update-bet"),
    REFUND("/CancelBet", 3,"退款","/wallet/rollback","/sports/refund"),
    ADJUSTMENT("/AdjustBalance", -1, "赠送", "/wallet/adjustment", "/sports/adjustment"),
    SETTLED("/Settle", 1, "结算", "/wallet/bet_result", "/sports/settled"),
    RESETTLE("/Resettle", 1, "重新结算", "/wallet/resettle", "/sports/resettle"),
    UNSETTLE("/Unsettle", 2, "撤销结算", "/wallet/unsettle", "/sports/unsettle"),
    MARKETPLACE("/PlaceBetParlay", 0, "串关投注", "/wallet/bet", "/sports/bet"),
    CONFIRMATORY("/ConfirmBetParlay", 0, "串关确认", "/wallet/update-bet", "/sports/update-bet"),
    CREDIT("/credit", 1, "CREDIT", "/wallet/bet_credit", "/wallet/bet_credit"),
    DEBIT("/debit", 1, "DEBIT", "/wallet/bet_debit", "/wallet/bet_debit")
    ;


    private final String action;
    /**
     * 0 = 未结算的下注
     * <p>
     * 1 = 已结算的下注
     * <p>
     * 2 = 取消的下注
     * <p>
     * 3 = 退款的下注
     */
    @EnumValue
    private final int type;

    private final String desc;
    private final String path;
    private final String sportPath;

    public static Integer getType(String action) {
        for (ActionEnum a : values()) {
            if (a.action.equalsIgnoreCase(action)) {
                return a.type;
            }
        }
        return null;
    }
    public static ActionEnum from(String action) {
        for (ActionEnum a : values()) {
            if (a.action.equalsIgnoreCase(action)) {
                return a;
            }
        }
        return null;
    }
    public static ActionEnum from(Integer type) {
        for (ActionEnum a : values()) {
            if (a.type==type) {
                return a;
            }
        }
        return null;
    }

}