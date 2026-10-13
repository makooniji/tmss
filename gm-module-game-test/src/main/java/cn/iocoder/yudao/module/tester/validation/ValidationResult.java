package cn.iocoder.yudao.module.tester.validation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationResult extends BaseValidationResult {

    public static final String TYPE_SUCCESS = "SUCCESS";
    public static final String TYPE_FAIL = "FAIL";
    public static final String TYPE_ERROR = "ERROR";

    private String caseCode;
    private boolean success;
    private String errorMessage;
    private long duration;
    private Object responseData;
    /** 步骤结果类型：SUCCESS / FAIL / ERROR */
    private String validationType;
    private String apiUrl;
    private Map<String, String> header;
    /** 平台发往商户的请求体 */
    private Object requestBody;
    /** 预期商户响应（展示用 JSON） */
    private String expectedResult;
    /** 实际商户响应 JSON */
    private String actualResult;
    /** 步骤展示状态：待执行 / 成功 / 失败 / 异常（与 ValidationMeta.result 对齐，供前端更新列表） */
    private String result;
    /** 接口路径，如 wallet/balance（供前端与地图项匹配） */
    private String api;
    /** 案例内各步骤结果（案例级汇总时附带，供 Runner 统计） */
    private List<ValidationResult> steps;

    public static ValidationResult success(String caseCode, String message) {
        ValidationResult result = new ValidationResult();
        result.setCaseCode(caseCode);
        result.setSuccess(true);
        result.setValidationType(TYPE_SUCCESS);
        result.setErrorMessage(message);
        result.setResult("成功");
        return result;
    }

    public static ValidationResult failure(String caseCode, String validationType, String errorMessage) {
        ValidationResult result = new ValidationResult();
        result.setCaseCode(caseCode);
        result.setSuccess(false);
        result.setValidationType(validationType);
        result.setErrorMessage(errorMessage);
        result.setResult(TYPE_ERROR.equals(validationType) ? "异常" : "失败");
        return result;
    }

    /** 按步骤结果汇总为案例级结果：全部 SUCCESS 则案例成功 */
    public static ValidationResult fromSteps(String caseCode, List<ValidationResult> steps) {
        int total = steps.size();
        long successCount = steps.stream()
                .filter(s -> TYPE_SUCCESS.equals(s.getValidationType()))
                .count();

        ValidationResult result = new ValidationResult();
        result.setCaseCode(caseCode);
        result.setSteps(steps);
        result.setDuration(steps.stream().mapToLong(ValidationResult::getDuration).sum());
        result.setSuccess(total > 0 && successCount == total);
        result.setValidationType(result.isSuccess() ? TYPE_SUCCESS : TYPE_FAIL);
        result.setErrorMessage("成功 " + successCount + "/" + total);
        result.setResult(result.isSuccess() ? "成功" : "失败");
        return result;
    }
}
