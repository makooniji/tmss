package cn.iocoder.yudao.module.tester.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ValidationRegistry {

    private final List<ValidationCase> cases;

    public List<ValidationCase> getCases() {
        return cases;
    }
}
