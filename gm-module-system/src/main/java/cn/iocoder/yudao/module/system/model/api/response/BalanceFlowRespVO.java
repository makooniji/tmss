package cn.iocoder.yudao.module.system.model.api.response;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.math.BigDecimal;


@Data
@ExcelIgnoreUnannotated
public class BalanceFlowRespVO {

    /**
     * 交易编号
     */
    private String referenceId;
    /**
     * 流水号
     */
    private String transactionId;
    /**
     * 会员账号
     */
    private String username;
    /**
     * 货币
     */
    private String currencyCode;
    /**
     * 账变前金额
     */
    private BigDecimal beforeBalance;
    /**
     * 账变后金额
     */
    private BigDecimal afterBalance;
    /**
     * 交易金额
     */
    private BigDecimal transferAmount;
    /**
     * 时间戳
     */
    private Long timestamp;

}
