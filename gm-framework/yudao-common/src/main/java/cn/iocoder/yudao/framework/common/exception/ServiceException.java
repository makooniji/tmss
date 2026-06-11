package cn.iocoder.yudao.framework.common.exception;

import cn.iocoder.yudao.framework.common.exception.enums.ServiceErrorCodeRange;
import cn.iocoder.yudao.framework.common.util.MessageUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务逻辑异常 Exception
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ServiceException extends RuntimeException {

    /**
     * 业务错误码
     *
     * @see ServiceErrorCodeRange
     */
    private Integer code;
    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException() {
    }

    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

    public ServiceException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public ServiceException(String message, Object[] args) {

        this.message=message;
        this.args=args;
    }

    public Integer getCode() {
        return code;
    }

    public ServiceException setCode(Integer code) {
        this.code = code;
        return this;
    }

    @Override
    public String getMessage() {
        String message = null;
        if (!StrUtils.isEmpty(this.message)) {
            message = MessageUtils.message(this.message, args);
        }
        if (message == null) {
            message = this.message;
        }
        return message;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

}
