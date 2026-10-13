package cn.iocoder.yudao.report.excute.pipeline;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;

import java.util.List;

public abstract class AbstractStatPipelineHandler {

    /**
     * 模板方法：统一执行消息过滤并调用子类处理逻辑
     */
    public void handle(List<GameStaticDTO> tenantMessages) {
        if (CollUtil.isEmpty(tenantMessages)) {
            return;
        }

        // 1. 过滤出当前 Handler 支持的消息类型
        List<GameStaticDTO> filteredMessages = tenantMessages.stream()
                .filter(this::support)
                .toList();
        if(ObjectUtil.isEmpty(filteredMessages)){
            return;
        }

        // 2. 匹配到消息才触发子类落库逻辑
        if (CollUtil.isNotEmpty(filteredMessages)) {
            doHandle(filteredMessages);
        }
    }

    /**
     * 消息类型匹配断言（由子类决定支持哪些消息类型）
     *
     * @param dto 单条消息
     * @return true-需要当前处理器处理；false-过滤掉
     */
    protected abstract boolean support(GameStaticDTO dto);

    /**
     * 执行具体的管道数据转换和落库（由子类实现）
     *
     * @param tenantMessages 已过滤后的消息列表
     */
    protected abstract void doHandle(List<GameStaticDTO> tenantMessages);
}