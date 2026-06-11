package cn.iocoder.yudao.module.tester.validation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseValidationResult {
    private String traceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public BaseValidationResult() {
        this.startTime = LocalDateTime.now();
    }

    public void markCompleted() {
        this.endTime = LocalDateTime.now();
    }
}
