package cn.iocoder.yudao.module.tester.validation;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.system.model.api.SportContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationRunner {

    private final ValidationRegistry registry;


    @Async
    public String runAsync(ValidationRunRequest request) {
        String traceId = UUID.randomUUID().toString();

        try {
            log.info("开始执行验证任务: traceId={}, gameCode={}",
                     traceId, request.getGameCode());

            // 获取并过滤验证案例
            List<ValidationCase> cases = registry.getCases();
            if (ObjectUtil.isNotEmpty(request.getGroupCode())) {
                cases = cases.stream().filter(p -> p.code().equals(request.getGroupCode())).toList();
            }
            log.info("找到 {} 个验证案例待执行", cases.size());

            int successCount = 0;
            int totalCount = cases.size();

            SportContext ctx = new SportContext();
            ctx.setTraceId(IdUtil.fastUUID());
            ctx.setTimestamp(Instant.now().toEpochMilli());
            ctx.setCurrency(request.getCurrency());
            ctx.setTransferId(IdUtil.fastUUID());
            ctx.setOrderId(IdUtil.fastUUID());
            ctx.setTenant(request.getTenantDO());
            ctx.setPullUsername(request.getUsername());
            ctx.setGameCode(request.getGameCode());


            for (ValidationCase item : cases) {
                try {

                    ValidationResult result =item.execute(request, ctx);
                    result.markCompleted();


                    // 统计结果
                    if (result.isSuccess()) {
                        successCount++;
                        log.debug("验证案例 {} 执行成功, 耗时: {}ms", item.code(), result.getDuration());
                    } else {
                        log.warn("验证案例 {} 执行失败: {}", item.code(), result.getErrorMessage());
                    }

                } catch (Exception e) {
                    log.error("执行验证案例 {} 时发生异常", item.code(), e);
                    ValidationResult errorResult = ValidationResult.failure(
                            item.code(), item.code(),
                        "执行异常: " + e.getMessage());
                    errorResult.setTraceId(traceId);
                    errorResult.markCompleted();
                }
            }

            log.info("验证任务完成: traceId={}, 总计: {}/{}, 成功率: {:.1f}%",
                     traceId, successCount, totalCount,
                     totalCount > 0 ? (successCount * 100.0 / totalCount) : 0.0);

        } catch (Exception e) {
            log.error("验证任务执行失败: traceId={}", traceId, e);
        }

        return traceId;
    }

}
