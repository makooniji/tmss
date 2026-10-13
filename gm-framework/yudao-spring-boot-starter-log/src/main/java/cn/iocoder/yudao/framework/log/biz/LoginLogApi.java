package cn.iocoder.yudao.framework.log.biz;



import cn.iocoder.yudao.framework.log.dto.LoginLogCreateReqDTO;
import jakarta.validation.Valid;

/**
 * 登录日志的 API 接口
 *
 * @author osca
 */
public interface LoginLogApi {

    /**
     * 创建登录日志
     *
     * @param reqDTO 日志信息
     */
    void createLoginLog(@Valid LoginLogCreateReqDTO reqDTO);

}
