package cn.iocoder.yudao.report.controller.game.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.io.Serializable;


@Data
@ExcelIgnoreUnannotated
public class TenantStatusCountVO implements Serializable {

    /**
     * 开通的数量
     */
    private Long openCounts;
    /**
     * 关闭的数量
     */
    private Long closeCounts;
    /**
     * 运营的数量
     */
    private Long operCounts;

}
