package cn.iocoder.yudao.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
@AllArgsConstructor
public enum GameKindEnum {

    ELECTRONIC("电子游戏", 1),
    LIVE("真人视讯", 2),
    BOARD("棋牌游戏", 3),
    FISHING("捕鱼游戏", 4),
    SPORT("体育竞技", 5),
    LOTTERY("彩票", 6),
    MINI("迷你", 7);


    private final String action;
    private final int type;

    public static GameKindEnum from(Integer type) {
        for (GameKindEnum a : values()) {
            if (a.type == type) {
                return a;
            }
        }
        return null;
    }

    /**
     * 上游/厂商游戏类型别名 -> 标准分类。
     * 如 SLOT/SLOTS -> SLOTS，LIVE/LIVES -> LIVE。
     */
    public static GameKindEnum resolve(String gameType) {
        return resolve(null, gameType);
    }

    /**
     * 上游/厂商游戏类型别名 -> 标准分类。
     *
     * @param vendorCode 厂商编码（仅用于异常信息，可为 null）
     * @param gameType   上游原始类型，如 SLOT、LIVE、FISH
     */
    public static GameKindEnum resolve(String vendorCode, String gameType) {
        if (!StringUtils.hasText(gameType)) {
            throw new IllegalArgumentException("游戏类型为空" +
                    (StringUtils.hasText(vendorCode) ? ", vendor=" + vendorCode : ""));
        }
        String type = gameType.trim().toUpperCase();
        return switch (type) {
            case "SLOT", "SLOTS", "ELECTRONIC" -> ELECTRONIC;
            case "LIVE", "LIVES" -> LIVE;
            case "FISH", "FISHING" -> FISHING;
            case "BOARD", "POKER", "CARD" -> BOARD;
            case "SPORT", "SPORTS" -> SPORT;
            case "LOTTERY", "LOTTO" -> LOTTERY;
            case "MINI" -> MINI;
            default -> {
                try {
                    yield valueOf(type);
                } catch (IllegalArgumentException e) {
                    String tip = "未知游戏类型: " + gameType;
                    if (StringUtils.hasText(vendorCode)) {
                        tip += " (vendor=" + vendorCode + ")";
                    }
                    throw new IllegalArgumentException(tip, e);
                }
            }
        };
    }

}
