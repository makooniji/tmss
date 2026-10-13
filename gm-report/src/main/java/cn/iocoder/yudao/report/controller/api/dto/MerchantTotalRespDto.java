package cn.iocoder.yudao.report.controller.api.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
@ExcelIgnoreUnannotated
public class MerchantTotalRespDto implements Serializable {


    @Data
    public static  class  CountData{

        /**
         * 商户数量
         */
        private Long merchantCount;

        /**
         * 运营数量
         */
        private Long runningCount;

        /**
         * 投注额
         */
        private BigDecimal betAmount;

        /**
         * 派彩金额
         */
        private BigDecimal payAmount;

        /**
         * 输赢金额
         */
        private BigDecimal winLoss;
        /**
         * 活跃人数
         */
        private Long activeUserCount;
    }

    /**
     * 总计
     */
    private CountData total;
    /**
     * 上周
     */
    private CountData prevWeek;
    /**
     * 当前周
     */
    private CountData thisWeek;

}
