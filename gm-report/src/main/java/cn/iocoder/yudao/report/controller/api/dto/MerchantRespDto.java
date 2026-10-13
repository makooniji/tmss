package cn.iocoder.yudao.report.controller.api.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Data
@ExcelIgnoreUnannotated
public class MerchantRespDto implements Serializable {


    /**
     * 租户ID
      */
    private Long tid;

    /**
     * 商户ID
     */
    @JsonProperty("merchantId")
    private String code;

    /**
     * 商户账号
     */
    @JsonProperty("merchantName")
    private String name;


    /**
     * 国家
     */
    @ExcelProperty("国家")
    private List<String> currency;


    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 运营时间
     */
    private LocalDateTime operateTime;
    /**
     * 账户章台
     */
    private Integer status;
    /**
     * 运营状态
     */
    private Integer operateStatus;
}
