package cn.iocoder.yudao.module.tester.flow;


import cn.iocoder.yudao.framework.common.model.game.push.DownstreamResult;
import cn.iocoder.yudao.module.system.model.game.SportContext;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Data
@AllArgsConstructor
public class FlowStep {

    private String name;
    private Function<SportContext, DownstreamResult> action;
    private String verifyRule;
    /** 预期商户响应（展示用，原样下发） */
    private String expectedDisplay;
    /** 为 true 时，预期展示取自本步真实下游响应（traceId/balance 等） */
    private boolean expectedFromResponse;
    /** 本步 FAIL/ERROR 后不再执行本段后续步骤（下一大段仍继续） */
    private boolean abortSegmentOnFailure;
    private BiConsumer<DownstreamResult, SportContext> after;

}
