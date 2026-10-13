package cn.iocoder.yudao.module.tester.validation;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;

import cn.iocoder.yudao.module.system.model.game.SportContext;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationRunner {

    /** 暂仅开放 v1–v3；v4 及以后案例待改造后再启用 */
    private static final Set<String> ENABLED_CASE_CODES = Set.of("v1", "v2", "v3");

    private final ValidationRegistry registry;
    private final WebSocketMessageSender ws;


    @Async
    public void runAsync(ValidationRunRequest request, String traceId) {
        try {
            log.info("开始执行验证任务: traceId={}, gameCode={}",
                     traceId, request.getGameCode());

            // 获取并过滤验证案例
//            List<ValidationCase> cases = registry.getCases();
            // 获取并过滤验证案例（暂仅 v1–v3）
            List<ValidationCase> cases = registry.getCases().stream()
                    .filter(c -> ENABLED_CASE_CODES.contains(c.code()))
                    .toList();
            if (ObjectUtil.isNotEmpty(request.getGroupCode())) {
                cases = cases.stream().filter(p -> p.code().equals(request.getGroupCode())).toList();
            }
            log.info("找到 {} 个验证案例待执行", cases.size());

            int successCount = 0;
            int totalCount = cases.size();

            for (ValidationCase item : cases) {
                SportContext ctx = buildSportContext(request);
                try {

                    ValidationResult result = item.execute(request, ctx);
                    result.markCompleted();

                    if (ObjectUtil.isNotEmpty(result.getSteps())) {
                        logStepSummary(item.code(), result);
                    } else if (result.isSuccess()) {
                        log.debug("验证案例 {} 执行成功, 耗时: {}ms", item.code(), result.getDuration());
                    } else {
                        log.warn("验证案例 {} 执行失败: {}", item.code(), result.getErrorMessage());
                    }

                    if (result.isSuccess()) {
                        successCount++;
                    }

                } catch (Exception e) {
                    log.error("执行验证案例 {} 时发生异常", item.code(), e);
                    ValidationResult errorResult = ValidationResult.failure(
                            item.code(),
                            ValidationResult.TYPE_ERROR,
                            "执行异常: " + e.getMessage());
                    errorResult.setTraceId(traceId);
                    errorResult.setResult("异常");
                    errorResult.markCompleted();
                    pushCaseError(ctx, errorResult);
                }
            }

            double successRate = totalCount > 0 ? (successCount * 100.0 / totalCount) : 0.0;
            log.info("验证任务完成: traceId={}, 案例成功 {}/{}, 成功率: {}%",
                     traceId, successCount, totalCount, String.format("%.1f", successRate));

        } catch (Exception e) {
            log.error("验证任务执行失败: traceId={}", traceId, e);
        }
    }

    /**
     * 每个验证案例使用独立的 SportContext；traceId/timestamp 由 FlowBuilder 每步 push 前生成。
     * Controller 同步生成 traceId 后调用本方法；traceId 仅作本次任务批次关联，不写入商户请求。
     */
    private static SportContext buildSportContext(ValidationRunRequest request) {
        SportContext ctx = new SportContext();
        ctx.setTimestamp(Instant.now().toEpochMilli());
        ctx.setCurrency(request.getCurrency());
        ctx.setWrapCurrency(request.getCurrency());
        ctx.setTransferId(IdUtil.fastUUID());
        ctx.setOrderId(IdUtil.fastUUID());
        ctx.setRoundId(IdUtil.fastUUID());
        ctx.setTenant(request.getTenantDO());
        ctx.setPullUsername(request.getUsername());
        ctx.setGameCode(request.getGameCode());
        ctx.setAdminId(request.getUserId());
        return ctx;
    }

    private void logStepSummary(String caseCode, ValidationResult result) {
        List<ValidationResult> steps = result.getSteps();
        long stepSuccess = steps.stream()
                .filter(s -> ValidationResult.TYPE_SUCCESS.equals(s.getValidationType()))
                .count();
        log.info("验证案例 {} 完成: 成功 {}/{} 步, 耗时 {}ms",
                 caseCode, stepSuccess, steps.size(), result.getDuration());
        steps.stream()
                .filter(s -> !ValidationResult.TYPE_SUCCESS.equals(s.getValidationType()))
                .forEach(s -> log.warn("  步骤 {} [{}]: {}",
                        s.getCaseCode(), s.getValidationType(), s.getErrorMessage()));
    }

    private void pushCaseError(SportContext ctx, ValidationResult result) {
        if (ctx.getAdminId() == null) {
            return;
        }
        ws.send(UserTypeEnum.ADMIN.getValue(), ctx.getAdminId(), "tester", JSON.toJSONString(result));
    }

}
