package cn.iocoder.yudao.framework.log.biz;

import cn.iocoder.yudao.framework.common.biz.system.logger.OperateLogCommonApi;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.log.dto.OperateLogPageReqDTO;
import cn.iocoder.yudao.framework.log.dto.OperateLogRespDTO;

/**
 * 操作日志 API 接口
 *
 * @author osca
 */
public interface OperateLogApi extends OperateLogCommonApi {

    /**
     * 获取指定模块的指定数据的操作日志分页
     *
     * @param pageReqDTO 请求
     * @return 操作日志分页
     */
    PageResult<OperateLogRespDTO> getOperateLogPage(OperateLogPageReqDTO pageReqDTO);

}
