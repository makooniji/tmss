package cn.iocoder.yudao.framework.common.model.game;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameApiDetailRequest {

    /**
     * 由运营商系统为每个API请求生成的通用唯一标识符 (UUID)。
     */
    @NotEmpty
    private String traceId;

    /**
     * 注单号
     */
    @NotEmpty
    private String betId;



    @NotNull
    private Integer pageSize;

}
