package cn.iocoder.yudao.report.pipeline.excute;

import cn.iocoder.yudao.framework.mq.kafka.merchant.MerchantStaticDTO;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;

import cn.iocoder.yudao.report.pipeline.StatPipelineHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MerchantStatExecutor {

    // Spring 会自动注入所有实现过 StatPipelineHandler 接口的 Bean
    private final List<StatPipelineHandler> pipelines;
    private final PlatformTransactionManager transactionManager;

    /**
     * 针对单个租户的数据执行管道流处理，提供强事务保护
     */
    public void executeInTransaction(Long tenantId, List<MerchantStaticDTO> tenantMessages) {
        // 1. 先切换租户上下文
        TenantUtils.execute(tenantId, () -> {

            // 2. 在租户上下文内部，动态创建并启动事务
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    // 3. 逐个流经管道处理器（一个失败，全部回滚）
                    for (StatPipelineHandler pipeline : pipelines) {
                        pipeline.handle(tenantMessages);
                    }
                } catch (Exception e) {
                    log.error("[事务回滚] 租户ID={} 管道处理期间发生异常", tenantId, e);
                    status.setRollbackOnly(); // 标记回滚
                    throw e; // 抛出异常让上层捕捉并记录死信
                }
            });

        });
    }
}