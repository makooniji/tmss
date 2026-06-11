package cn.iocoder.yudao.module.tester.flow;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.DownstreamResult;
import cn.iocoder.yudao.module.tester.validation.SpelValidationEngine;
import cn.iocoder.yudao.module.tester.validation.ValidationCaseRule;
import cn.iocoder.yudao.module.tester.validation.ValidationResult;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;


@RequiredArgsConstructor
@Slf4j
public class FlowBuilder {

    private final SportContext context;
    private final List<FlowStep> steps = new ArrayList<>();
    private FlowStep currentStep;

    private final WebSocketMessageSender ws;


    public static FlowBuilder start(SportContext context, WebSocketMessageSender ws) {

        return new FlowBuilder(context,ws);
    }

    public FlowBuilder step(
            String stepName,
            Function<SportContext, DownstreamResult> action
    ) {
        currentStep = new FlowStep(stepName, action, null,null);
        steps.add(currentStep);
        return this;
    }

    public FlowBuilder verify(String rule) {
        currentStep.setVerifyRule(rule);
        return this;
    }
    public FlowBuilder after(
            BiConsumer<DownstreamResult, SportContext> consumer
    ) {
        currentStep.setAfter(consumer);
        return this;
    }

    public List<ValidationResult> execute() {

        List<ValidationResult> results = new ArrayList<>();
        boolean allSuccess = true;

        for (FlowStep step : steps) {

            long start = System.currentTimeMillis();
            ValidationResult result;

            try {
                DownstreamResult response =
                        step.getAction().apply(context);

                if (step.getAfter() != null) {
                    step.getAfter().accept(response, context);
                }

                boolean pass = SpelValidationEngine.validate(
                        response,
                        ValidationCaseRule.builder()
                                .caseCode(step.getName())
                                .rule(step.getVerifyRule())
                                .message(step.getName() + "验证失败")
                                .build(),
                        context
                ).isSuccess();

                result = pass
                        ? ValidationResult.success(step.getName(), "SUCCESS")
                        : ValidationResult.failure(step.getName(), "FAIL", "规则校验失败");

                result.setCaseCode(step.getName());
                result.setApiUrl(context.getApiUrl());
                result.setHeader(context.getHeader());
                result.setResponseData(response);
                result.setDuration(System.currentTimeMillis() - start);

                allSuccess &= pass;

            } catch (Exception e) {

                result = ValidationResult.failure(
                        step.getName(),
                        "ERROR",
                        e.getMessage()
                );
                result.setDuration(System.currentTimeMillis() - start);

                allSuccess = false;
            }

            results.add(result);

            // ⭐ 实时推送前端
            pushStepResult(result,context);
        }

        return results;
    }
    private void pushStepResult(ValidationResult result,SportContext context) {
        ws.send(UserTypeEnum.ADMIN.getValue(), context.getAdminId(),"tester", JSON.toJSONString(result));
    }

}