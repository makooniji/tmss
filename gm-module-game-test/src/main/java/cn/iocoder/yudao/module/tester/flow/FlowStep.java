package cn.iocoder.yudao.module.tester.flow;

import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.DownstreamResult;
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
    /**
     * 执行后处理（核心）
     */
    private BiConsumer<DownstreamResult, SportContext> after;
}