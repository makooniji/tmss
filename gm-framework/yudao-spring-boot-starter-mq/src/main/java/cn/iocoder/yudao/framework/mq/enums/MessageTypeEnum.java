package cn.iocoder.yudao.framework.mq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    REGISTER,
    BET,
    DEPOSIT,
    WITHDRAW,
    PAYOUT,

}