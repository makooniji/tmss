package cn.iocoder.yudao.module.game.adapter.sports.saba.enums;

public enum SabaParlayTypeEnum {

    // ===== 标准串关 =====
    DOUBLES("Doubles", "2串1", "Double"),
    TREBLES("Trebles", "3串1", "Treble"),

    FOLD4("Fold4", "4串1", "4-Fold"),
    FOLD5("Fold5", "5串1", "5-Fold"),
    FOLD6("Fold6", "6串1", "6-Fold"),
    FOLD7("Fold7", "7串1", "7-Fold"),
    FOLD8("Fold8", "8串1", "8-Fold"),
    FOLD9("Fold9", "9串1", "9-Fold"),
    FOLD10("Fold10", "10串1", "10-Fold"),
    FOLD11("Fold11", "11串1", "11-Fold"),
    FOLD12("Fold12", "12串1", "12-Fold"),
    FOLD13("Fold13", "13串1", "13-Fold"),
    FOLD14("Fold14", "14串1", "14-Fold"),
    FOLD15("Fold15", "15串1", "15-Fold"),
    FOLD16("Fold16", "16串1", "16-Fold"),
    FOLD17("Fold17", "17串1", "17-Fold"),
    FOLD18("Fold18", "18串1", "18-Fold"),
    FOLD19("Fold19", "19串1", "19-Fold"),
    FOLD20("Fold20", "20串1", "20-Fold"),

    // ===== 特殊串关 =====
    BET_BUILDER("-1", "同场串关", "Bet Builder"),

    TRIXIE("Trixie", "3串4", "Trixie"),
    YANKEE("Yankee", "4串11", "Yankee"),
    CANADIAN("Canadian", "5串26", "Canadian"),
    HEINZ("Heinz", "6串57", "Heinz"),
    GOLIATH("Goliath", "8串247", "Goliath"),

    LUCKY7("Lucky7", "组合串关", "Lucky 7"),
    LUCKY15("Lucky15", "组合串关", "Lucky 15"),
    LUCKY31("Lucky31", "组合串关", "Lucky 31"),
    LUCKY63("Lucky63", "组合串关", "Lucky 63"),
    LUCKY127("Lucky127", "组合串关", "Lucky 127"),
    LUCKY255("Lucky255", "组合串关", "Lucky 255");

    private final String code;
    private final String label;
    private final String enLabel;

    SabaParlayTypeEnum(String code, String label, String enLabel) {
        this.code = code;
        this.label = label;
        this.enLabel = enLabel;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getEnLabel() {
        return enLabel;
    }

    // ========================
    // 中文解析（你原来的逻辑）
    // ========================
    public static String resolve(String name) {

        if (name == null) {
            return "未知串关";
        }

        for (SabaParlayTypeEnum e : values()) {
            if (e.code.equalsIgnoreCase(name)) {
                return e.label;
            }
        }

        if (name.startsWith("Fold")) {
            return name.replace("Fold", "") + "串1";
        }

        return "特殊串关";
    }

    // ========================
    // 英文解析（新增）
    // ========================
    public static String resolveEn(String name) {

        if (name == null) {
            return "Unknown";
        }

        for (SabaParlayTypeEnum e : values()) {
            if (e.code.equalsIgnoreCase(name)) {
                return e.enLabel;
            }
        }

        if (name.startsWith("Fold")) {
            return name.replace("Fold", "") + "-Fold";
        }

        return "Special";
    }
}