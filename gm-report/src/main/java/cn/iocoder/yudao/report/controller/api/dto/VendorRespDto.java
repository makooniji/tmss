package cn.iocoder.yudao.report.controller.api.dto;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 游戏分类模型
 */
@Data
public class VendorRespDto implements Serializable {


    /**
     * ID
      */
    private String title;
    /**
     * 编码
     */

    private String code;

    private List<String> categoryCode;


}
