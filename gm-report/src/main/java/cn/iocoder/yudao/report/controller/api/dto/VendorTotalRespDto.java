package cn.iocoder.yudao.report.controller.api.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Data
@ExcelIgnoreUnannotated
public class VendorTotalRespDto implements Serializable {


    @Data
    public static  class VendorCount{
        @JsonIgnore
        private String merchantCode;

        private BigDecimal winLoss;
        /**
         * 场馆编码
         */
        private String vendorCode;

        /**
         * 投注金额
         */
        private BigDecimal betAmount;

        private BigDecimal exchangeAmount =BigDecimal.ZERO;

        private String currency;

    }

    /**
     * 商户编码
     */
    private String merchantCode;
    /**
     * 输赢金额
     */
    private BigDecimal winLoss=BigDecimal.ZERO;

    /**
     * 转换后金额
     */
    private BigDecimal exchangeAmount =BigDecimal.ZERO;

    private BigDecimal betAmount=BigDecimal.ZERO;

    private String currency;

    private String country;
    /**
     * 场馆数据
     */
    private List<VendorCount> list;

}
