package cn.iocoder.yudao.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WalletActionEnum {

    /**
     * 账变类型: 1 存款 2 取款 3 投注 4 派彩 5 奖金 6 奖金转余额 7 加款 8 扣款 9 退款
     */
    BET("bet", 3),
    BET_RESULT("bet_result", 4),
    ROLLBACK("rollback", 9),
    ADJUSTMENT("adjustment", 5),
    BET_DEBIT("bet_debit", 2),
    BET_CREDIT("bet_credit", 7),
    BALANCE("balance", -1);

    private final String action;
    private final int type;

    public static Integer getType(String action) {
        for (WalletActionEnum a : values()) {
            if (a.action.equalsIgnoreCase(action)) {
                return a.type;
            }
        }
        return null;
    }
    public static WalletActionEnum from(String action) {
        for (WalletActionEnum a : values()) {
            if (a.action.equalsIgnoreCase(action)) {
                return a;
            }
        }
        return null;
    }
}
