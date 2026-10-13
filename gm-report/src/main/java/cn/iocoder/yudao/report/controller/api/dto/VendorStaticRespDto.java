package cn.iocoder.yudao.report.controller.api.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Data
@ExcelIgnoreUnannotated
public class VendorStaticRespDto implements Serializable {


    /**
     * 分类编码
      */
    private String categoryCode;

    /**
     * 场馆编码
     */
    private String vendorCode;

    /**
     * 投注金额
     */
    private BigDecimal betAmount;

    /**
     * 创建时间
     */
    private Date createTime;
}
