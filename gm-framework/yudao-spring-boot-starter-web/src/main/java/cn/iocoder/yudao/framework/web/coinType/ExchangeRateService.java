package cn.iocoder.yudao.framework.web.coinType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import cn.iocoder.yudao.framework.common.enums.CurrencyEnum;

@Service
public class ExchangeRateService {

    /**
     * 根据基础币种与目标币种进行金额换算
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null || fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        BigDecimal rate = getRate(toCurrency);
        // 保留 2 位小数，四舍五入
        return amount.divide(rate,2, RoundingMode.HALF_UP);
    }

    // 模拟从 Redis/数据库/外部API 获取最新汇率
    private BigDecimal getRate(String to) {

        CurrencyEnum currency = CurrencyEnum.fromCode(to.toUpperCase());

        return currency.getExchangeRate();
    }
}