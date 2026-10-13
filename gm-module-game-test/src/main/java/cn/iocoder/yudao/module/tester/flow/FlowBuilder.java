package cn.iocoder.yudao.module.tester.flow;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.model.game.push.DownstreamResult;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.framework.common.exception.GameException;

import cn.iocoder.yudao.module.system.model.game.SportContext;
import cn.iocoder.yudao.module.tester.validCase.support.ValidationCaseSupport;
import cn.iocoder.yudao.module.tester.validation.SpelValidationEngine;
import cn.iocoder.yudao.module.tester.validation.ValidationCaseRule;
import cn.iocoder.yudao.module.tester.validation.ValidationResult;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RequiredArgsConstructor
@Slf4j
public class FlowBuilder {

    private static final Pattern STATUS_ONLY_RULE =
            Pattern.compile("^status\\s*==\\s*'([^']+)'\\s*$");

    private final SportContext context;
    private final List<FlowStep> steps = new ArrayList<>();
    private FlowStep currentStep;

    private final WebSocketMessageSender ws;

    /** 为 true 时，任一步 FAIL/ERROR 后不再执行本段后续步骤 */
    private boolean stopOnFailure;


    public static FlowBuilder start(SportContext context, WebSocketMessageSender ws) {

        return new FlowBuilder(context, ws);
    }

    public FlowBuilder stopOnFailure() {
        this.stopOnFailure = true;
        return this;
    }

    /** 仅本步失败时中断本段；与 stopOnFailure（任一步失败即中断）互斥使用 */
    public FlowBuilder abortSegmentOnFailure() {
        currentStep.setAbortSegmentOnFailure(true);
        return this;
    }

    public FlowBuilder step(
            String stepName,
            Function<SportContext, DownstreamResult> action
    ) {
        currentStep = new FlowStep(stepName, action, null, null, false, false, null);
        steps.add(currentStep);
        return this;
    }

    public FlowBuilder verify(String rule) {
        currentStep.setVerifyRule(rule);
        return this;
    }

    /** rule 用于 SpEL 校验；expectedDisplay 为预期商户响应 JSON，原样推送给前端 */
    public FlowBuilder verify(String rule, String expectedDisplay) {
        currentStep.setVerifyRule(rule);
        currentStep.setExpectedDisplay(expectedDisplay);
        currentStep.setExpectedFromResponse(false);
        return this;
    }

    /** SpEL 校验；预期展示 JSON 取自本步真实下游响应（traceId、balance 等） */
    public FlowBuilder verifyFromResponse(String rule) {
        currentStep.setVerifyRule(rule);
        currentStep.setExpectedDisplay(null);
        currentStep.setExpectedFromResponse(true);
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

        for (FlowStep step : steps) {

            long start = System.currentTimeMillis();
            ValidationResult result;
            DownstreamResult response  = null;

            try {
                context.setTraceId(IdUtil.fastUUID());
                context.setTimestamp(Instant.now().toEpochMilli());
                response = step.getAction().apply(context);

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
                        : ValidationResult.failure(step.getName(), ValidationResult.TYPE_FAIL, "规则校验失败");

                fillStepContext(result, step, context, response);
                result.setResponseData(response);
                result.setDuration(System.currentTimeMillis() - start);

            } catch (Exception e) {
                log.warn("[execute][step({}) 执行异常]", step.getName(), e);

                result = ValidationResult.failure(
                        step.getName(),
                        ValidationResult.TYPE_ERROR,
                        resolveErrorMessage(e)
                );
                fillStepContext(result, step, context, response);
                if (response != null) {
                    result.setResponseData(response);
                }
                result.setDuration(System.currentTimeMillis() - start);
            }

            results.add(result);

            pushStepResult(result, context);

            if (!ValidationResult.TYPE_SUCCESS.equals(result.getValidationType())
                    && (stopOnFailure || step.isAbortSegmentOnFailure())) {
                break;
            }
        }

        return results;
    }

    private void fillStepContext(ValidationResult result, FlowStep step, SportContext context,
                                 DownstreamResult response) {
        result.setApiUrl(context.getApiUrl());
        result.setApi(extractApiPath(context.getApiUrl()));
        result.setHeader(context.getHeader());
        result.setRequestBody(context.getBody());
        result.setTraceId(context.getTraceId());
        result.setExpectedResult(resolveExpectedResult(step, context, response));
        if (response != null) {
            result.setActualResult(JSON.toJSONString(response));
        }
    }

    /** 纯 status 规则转为商户 JSON；复合规则或显式 expectedDisplay 则按原值 */
    private static String resolveExpectedResult(FlowStep step, SportContext context,
                                                DownstreamResult response) {
        if (step.isExpectedFromResponse() && response != null) {
            return ValidationCaseSupport.expectedDisplayFromResponse(response, context.getTraceId());
        }
        if (step.getExpectedDisplay() != null) {
            return step.getExpectedDisplay();
        }
        String rule = step.getVerifyRule();
        if (rule == null) {
            return null;
        }
        Matcher matcher = STATUS_ONLY_RULE.matcher(rule.trim());
        if (matcher.matches()) {
            return "{\"status\":\"" + matcher.group(1) + "\"}";
        }
        return rule;
    }

    private static String extractApiPath(String apiUrl) {
        if (apiUrl == null || apiUrl.isBlank()) {
            return null;
        }
        int walletIdx = apiUrl.indexOf("/wallet/");
        if (walletIdx >= 0) {
            return apiUrl.substring(walletIdx + 1);
        }
        int sportsIdx = apiUrl.indexOf("/sports/");
        if (sportsIdx >= 0) {
            return apiUrl.substring(sportsIdx + 1);
        }
        return apiUrl;
    }

    private static String resolveErrorMessage(Exception e) {
        if (e instanceof GameException ge) {
            if (ge.getCode() != null && ge.getMessage() != null) {
                return ge.getCode() + ": " + ge.getMessage();
            }
            if (ge.getCode() != null) {
                return ge.getCode();
            }
        }
        String message = e.getMessage();
        return message != null ? message : e.getClass().getSimpleName();
    }

    private void pushStepResult(ValidationResult result, SportContext context) {
        result.markCompleted();
        if (context.getAdminId() == null) {
            return;
        }
        ws.send(UserTypeEnum.ADMIN.getValue(), context.getAdminId(), "tester", JSON.toJSONString(result));
    }

}
