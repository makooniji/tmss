package cn.iocoder.yudao.framework.common.model.game;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameApiRtp {
    /**
     * 游戏代码。
     */
    @NotEmpty
    private String gameCode;

    @NotEmpty
    private BigDecimal rtp;


}
