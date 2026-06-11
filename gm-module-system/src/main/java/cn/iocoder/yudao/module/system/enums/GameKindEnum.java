package cn.iocoder.yudao.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GameKindEnum {

    ELECTRONIC("电子游戏",1),
    LIVE("真人视讯", 2),
    BOARD ("棋牌游戏", 3),
    FISHING("捕鱼游戏", 4),
    SPORT("体育竞技", 5),
    LOTTERY("彩票", 6);


    private final String action;
    private final int type;

    public static GameKindEnum from(Integer type) {
        for (GameKindEnum a : values()) {
            if (a.type==type) {
                return a;
            }
        }
        return null;
    }

}