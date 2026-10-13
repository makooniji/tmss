package cn.iocoder.yudao.framework.log.biz;



import cn.iocoder.yudao.framework.common.biz.logger.ApiErrorLogCommonApi;
import cn.iocoder.yudao.framework.common.biz.logger.dto.ApiErrorLogCreateReqDTO;

import cn.iocoder.yudao.framework.log.service.ApiErrorLogService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * API 访问日志的 API 接口
 *
 * @author osca
 */
@Service
@Validated
public class ApiErrorLogApiImpl implements ApiErrorLogCommonApi {

    @Resource
    private ApiErrorLogService apiErrorLogService;

    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {
        apiErrorLogService.createApiErrorLog(createDTO);
    }

}
