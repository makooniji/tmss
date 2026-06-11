package cn.iocoder.yudao.module.tester.validation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationResult extends BaseValidationResult {
    private String caseCode;
    private boolean success;
    private String errorMessage;
    private long duration;
    private Object responseData;
    private String validationType;
    private String apiUrl;
    private Map<String,String> header;


    public static ValidationResult success(String caseCode, String errorMessage) {
        ValidationResult result = new ValidationResult();
        result.setCaseCode(caseCode);
        result.setSuccess(true);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public static ValidationResult failure(String caseCode, String errorMessage, String s) {
        ValidationResult result = new ValidationResult();
        result.setCaseCode(caseCode);
        result.setErrorMessage(errorMessage);
        result.setSuccess(false);
        result.setErrorMessage(s);
        return result;
    }
}
