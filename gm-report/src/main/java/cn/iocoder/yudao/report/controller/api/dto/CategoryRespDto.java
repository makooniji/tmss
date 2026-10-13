package cn.iocoder.yudao.report.controller.api.dto;


import lombok.Data;

import java.io.Serializable;

/**
 * 游戏分类模型
 */
@Data
public class CategoryRespDto implements Serializable {


    /**
     * ID
      */
    private String title;


    /**
     * 分类编码
     */

    private String code;


}
