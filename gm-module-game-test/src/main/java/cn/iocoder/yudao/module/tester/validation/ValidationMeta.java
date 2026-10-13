package cn.iocoder.yudao.module.tester.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationMeta {

    /**
     * 1.01.01
     */
    private String caseCode;

    /**
     * wallet/balance
     */
    private String api;

    /**
     * 步骤展示状态（运行后由 WS 更新）：待执行 / 成功 / 失败 / 异常
     */
    private String result;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 耗时
     */
    private Long duration;
    /**
     * 头部
     */
    private Map<String, String> header;
    /**
     * 请求体
     */
    private String body;
    /**
     * 备注
     */
    private String remark;

    /**
     * 规范预期（文档描述，如 成功 / 余额不足），非运行结果
     */
    private String expectedResult;
    /**
     * 实际商户响应（运行后由 WS 更新）
     */
    private String actualResult;

    public ValidationMeta(String caseCode, String api, String expectedResult) {
        this.caseCode = caseCode;
        this.api = api;
        this.expectedResult = expectedResult;
        this.result = "待执行";
    }
}