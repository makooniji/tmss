package cn.iocoder.yudao.module.system.model.api.response;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.math.BigDecimal;


@Data
@ExcelIgnoreUnannotated
public class BalanceRespVO {


    /**
     * 会员账号
     */
    private String username;
    /**
     * 货币
     */
    private String currency;

    /**
     * 交易金额
     */
    private BigDecimal amount;
    /**
     * 时间戳
     */
    private Long timestamp;

}
