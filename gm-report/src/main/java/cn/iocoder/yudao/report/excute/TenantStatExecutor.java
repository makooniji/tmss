package cn.iocoder.yudao.report.excute;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.report.convert.StaticConvert;
import cn.iocoder.yudao.report.mapper.SystemMessageFlowMapper;
import cn.iocoder.yudao.report.model.SystemMessageFlowDo;
import cn.iocoder.yudao.report.excute.pipeline.AbstractStatPipelineHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TenantStatExecutor {
    private final SystemMessageFlowMapper flowMapper;
    // Spring 会自动注入所有实现过 StatPipelineHandler 接口的 Bean
    private final List<AbstractStatPipelineHandler> pipelineHandlers;
    private final PlatformTransactionManager transactionManager;

    /**
     * 针对单个租户的数据执行管道流处理，提供强事务保护
     */
    public void executeInTransaction(Long tenantId, List<GameStaticDTO> tenantMessages) {
        // 1. 先切换租户上下文
        TenantUtils.execute(tenantId, () -> {

            // 2. 在租户上下文内部，动态创建并启动事务
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    // 🎯 阶段 1：先统一处理重复消费校验（过滤出未消费过的消息）
                    List<GameStaticDTO> unconsumedMessages = filterAndRecordUnconsumedMessages(tenantMessages);

                    if (CollUtil.isEmpty(unconsumedMessages)) {
                        log.info("[Pipeline] 租户ID={} 批次内所有消息均已被消费，自动跳过 Pipeline 执行", tenantId);
                        return;
                    }
                    // 3. 逐个流经管道处理器（一个失败，全部回滚）
                    for (AbstractStatPipelineHandler pipeline : pipelineHandlers) {
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
    /**
     * 统一防重与流水保存：过滤掉已被消费过的消息
     */
    private List<GameStaticDTO> filterAndRecordUnconsumedMessages(List<GameStaticDTO> messages) {
        List<GameStaticDTO> unconsumedList = new ArrayList<>();

        for (GameStaticDTO msg : messages) {
            try {
                SystemMessageFlowDo flow = StaticConvert.INSTANCE.toMessageFLow(msg);
                // 存储原始 DTO 镜像 JSON
                flow.setContent(JsonUtils.toJsonString(msg));

                flowMapper.insert(flow);

                // 插入成功，说明是第一次消费
                unconsumedList.add(msg);
            } catch (DuplicateKeyException e) {
                // 唯一索引冲突，说明消息之前已被成功消费过，直接阻断跳过
                log.info("[防重拦截] 消息已被消费过，忽略处理: messageId={}", msg.getId());
            } catch (Exception e) {
                log.error("[流水防重] 插入消息流水失败: messageId={}", msg.getId(), e);
                // 防重服务异常时根据业务决定：抛异常阻止后续执行并让 MQ 重试，或跳过
                throw e;
            }
        }

        return unconsumedList;
    }
}