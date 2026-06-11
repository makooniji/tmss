package cn.iocoder.yudao.module.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public enum CurrencyEnum {
    TEST(20, "TEST", "TEST", BigDecimal.ONE),
    MYR(2, "MYR", "马元", BigDecimal.ONE),
    USD(3, "USD", "美元", BigDecimal.ONE),
    THB(4, "THB", "泰铢", BigDecimal.ONE),
    EUR(6, "EUR", "欧元", BigDecimal.ONE),
    GBP(12, "GBP", "英镑", BigDecimal.ONE),
    RMB(13, "RMB", "人民币", BigDecimal.ONE),
    // 1:1000 的特殊货币
    IDR(15, "IDR", "印度尼西亚盾", new BigDecimal("1000")),
    UUS(20, "UUS", "测试货币", BigDecimal.ONE),
    
    JPY(32, "JPY", "日圆", BigDecimal.ONE),
    CHF(41, "CHF", "瑞士法郎", BigDecimal.ONE),
    PHP(42, "PHP", "菲律宾比索", BigDecimal.ONE),
    WON(45, "WON", "韩元", BigDecimal.ONE),
    BND(46, "BND", "文莱元", BigDecimal.ONE),
    ZAR(48, "ZAR", "南非兰特", BigDecimal.ONE),
    MXN(49, "MXN", "墨西哥比绍", BigDecimal.ONE),
    CAD(50, "CAD", "加币", BigDecimal.ONE),
    // 1:1000 的特殊货币
    VND(51, "VND", "越南盾", new BigDecimal("1000")),
    
    DKK(52, "DKK", "丹麦克朗", BigDecimal.ONE),
    SEK(53, "SEK", "瑞典克朗", BigDecimal.ONE),
    NOK(54, "NOK", "挪威克朗", BigDecimal.ONE),
    RUB(55, "RUB", "俄罗斯卢布", BigDecimal.ONE),
    PLN(56, "PLN", "波兰兹罗提", BigDecimal.ONE),
    CZK(57, "CZK", "捷克克朗", BigDecimal.ONE),
    RON(58, "RON", "罗马尼亚列伊", BigDecimal.ONE),
    INR(61, "INR", "印度卢比", BigDecimal.ONE),
    NZD(63, "NZD", "新西兰元", BigDecimal.ONE),
    MMK(70, "MMK", "缅甸元", new BigDecimal("1000")),
    KHR(71, "KHR", "柬埔寨瑞尔", new BigDecimal("1000")),
    TRY(73, "TRY", "土耳其里拉", BigDecimal.ONE), // 原文 LIR(TRY)
    KES(79, "KES", "肯尼亚先令", BigDecimal.ONE),
    GHS(80, "GHS", "加纳塞地", BigDecimal.ONE),
    BRL(82, "BRL", "巴西雷亚尔", BigDecimal.ONE),
    CLP(83, "CLP", "智利比索", BigDecimal.ONE),
    COP(84, "COP", "哥伦比亚比索", BigDecimal.ONE),
    PEN(85, "PEN", "秘鲁新索尔", BigDecimal.ONE),
    ARS(86, "ARS", "阿根廷比索", BigDecimal.ONE),
    VES(87, "VES", "委内瑞拉玻利瓦尔", BigDecimal.ONE),
    PYG(88, "PYG", "巴拉圭瓜拉尼", BigDecimal.ONE),
    UYU(89, "UYU", "乌拉圭比索", BigDecimal.ONE),
    AED(90, "AED", "阿联酋迪拉姆", BigDecimal.ONE),
    LAK(93, "LAK", "老挝基普", new BigDecimal("1000")),
    USDT(96, "USDT", "USDT", BigDecimal.ONE),
    BDT(97, "BDT", "孟加拉国塔卡", BigDecimal.ONE),
    PKR(121, "PKR", "巴基斯坦卢比", BigDecimal.ONE),
    KZT(122, "KZT", "哈萨克坦吉", BigDecimal.ONE),
    NPR(123, "NPR", "尼泊尔卢比", BigDecimal.ONE),

    // 沙巴白牌中国版特定货币
    RMB2(124, "RMB2", "人民币(中国版)", BigDecimal.ONE),
    VND2(125, "VND2", "越南盾(中国版)", new BigDecimal("1000")), // 原文 INH2
    THB2(126, "THB2", "泰铢(中国版)", BigDecimal.ONE),            // 原文 TB2
    INR2(127, "INR2", "印度卢比(中国版)", BigDecimal.ONE),
    PHP2(128, "PHP2", "菲律宾比索(中国版)", BigDecimal.ONE),      // 原文 PE2
    IDR2(129, "IDR2", "印度尼西亚盾(中国版)", new BigDecimal("1000")), // 原文 IN2
    KRW2(130, "KRW2", "韩元(中国版)", BigDecimal.ONE),           // 原文 Won2
    MYR2(131, "MYR2", "马元(中国版)", BigDecimal.ONE),           // 原文 RM2
    USD2(132, "USD2", "美元(中国版)", BigDecimal.ONE),           // 原文 US$2
    TND(133, "TND", "突尼斯第纳尔", BigDecimal.ONE),
    ZMW(134, "ZMW", "赞比亚克瓦查", BigDecimal.ONE),
    TZS(135, "TZS", "坦桑尼亚先令", BigDecimal.ONE),

    // 沙巴白牌中国版特定货币 (203-206)
    JPY2(203, "JPY2", "日圆(中国版)", BigDecimal.ONE),
    EUR2(204, "EUR2", "欧元(中国版)", BigDecimal.ONE),
    BRL2(205, "BRL2", "巴西雷亚尔(中国版)", BigDecimal.ONE),
    USDT2(206, "USDT2", "USDT(中国版)", BigDecimal.ONE),

    NGN(207, "NGN", "尼日利亚奈拉", BigDecimal.ONE),
    DZD(210, "DZD", "阿尔及利亚第纳尔", BigDecimal.ONE),
    MAD(211, "MAD", "摩洛哥迪拉姆", BigDecimal.ONE),
    FRF(212, "FRF", "法国法郎", BigDecimal.ONE),
    BDT2(215, "BDT2", "孟加拉国塔卡(中国版)", BigDecimal.ONE),
    EGP(216, "EGP", "埃及镑", BigDecimal.ONE),

    // 1:1000 特殊比例
    LBP(217, "LBP", "黎巴嫩鎊", new BigDecimal("1000")),

    PGK(219, "PGK", "巴布亚新几内亚基那", BigDecimal.ONE),
    ETB(220, "ETB", "埃塞俄比亚比尔", BigDecimal.ONE),
    SAR(221, "SAR", "沙特里亚尔", BigDecimal.ONE),
    LKR(222, "LKR", "斯里兰卡卢比", BigDecimal.ONE),
    AOA(235, "AOA", "安哥拉宽扎", BigDecimal.ONE),
    XAF(236, "XAF", "中非法郎", BigDecimal.ONE),
    BOB(237, "BOB", "玻利维亚诺", BigDecimal.ONE),
    CRC(238, "CRC", "哥斯达黎加科朗", BigDecimal.ONE),
    BHD(239, "BHD", "巴林第纳尔", BigDecimal.ONE),
    KWD(240, "KWD", "科威特第纳尔", BigDecimal.ONE),
    GEL(241, "GEL", "格鲁吉亚拉里", BigDecimal.ONE),
    OMR(242, "OMR", "阿曼里亚尔", BigDecimal.ONE),
    UNKNOWN(243, "UNKNOWN", "未知类型", BigDecimal.ONE);


    private final Integer saba;
    @EnumValue
    private final String code;   // ISO 货币代码
    private final String name;   // 中文名称
    private final BigDecimal unit; // 比例单位 (1 或 1000)

    /**
     * 根据 ID 获取枚举
     */
    public static CurrencyEnum getSaBa(Integer id) {
        for (CurrencyEnum e : values()) {
            if (e.saba.equals(id)) return e;
        }
        return CurrencyEnum.TEST;
    }

    /**
     * 根据 Code 获取枚举（忽略大小写）
     */
    public static CurrencyEnum fromCode(String code) {
        for (CurrencyEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) return e;
        }
        return CurrencyEnum.TEST;
    }
}