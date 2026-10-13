package cn.iocoder.yudao.module.tester.validation;


import cn.iocoder.yudao.module.system.model.game.SportContext;

public interface ValidationCase {
    String code();
    ValidationResult execute(ValidationRunRequest request, SportContext context);
}
