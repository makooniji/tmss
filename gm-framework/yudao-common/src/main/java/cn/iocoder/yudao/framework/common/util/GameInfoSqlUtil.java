package cn.iocoder.yudao.framework.common.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * game_info 导入 SQL 工具
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GameInfoSqlUtil {

    private static final int DEFAULT_BATCH_SIZE = 50;

    /**
     * 根据指定 locale 构建 lang JSON（自动识别中/英，同语言不重复翻译）。
     */
    public static String buildLang(String gameName, String translateLocales) {
        if (StringUtils.isBlank(translateLocales)) {
            return "{}";
        }
        return GoogleTranslateUtil.buildGameNameLangJson(gameName, translateLocales.split(","));
    }

    /**
     * 生成 game_info Upsert SQL（game_code 为唯一键，单行）。
     */
    public static String buildUpsertSql(String vendorCode, String vendorName, String rawGameName, String gameCode,
                                        int cateId, String logo, String supportLang, String translateLocales) {
        String englishName = GoogleTranslateUtil.resolveEnglishName(rawGameName);
        String lang = escapeSql(buildLang(rawGameName, translateLocales));
        return String.format(
                "INSERT INTO `game_info` (`vendor_code`,`vendor_name`,`game_name`,`game_code`,`cate_id`,`logo`,`sort`,`status`,`lang`,`support_lang`,`create_time`,`update_time`,`creator`,`updater`) " +
                        "VALUES ('%s','%s','%s','%s',%d,'%s',20,0,'%s','%s',NOW(),NOW(),'system','system') " +
                        "ON DUPLICATE KEY UPDATE `lang`=VALUES(`lang`),`support_lang`=VALUES(`support_lang`),`update_time`=NOW();\n",
                escapeSql(vendorCode),
                escapeSql(vendorName),
                escapeSql(englishName),
                escapeSql(gameCode),
                cateId,
                escapeSql(logo),
                lang,
                escapeSql(supportLang));
    }

    /**
     * 批量生成 game_info Upsert SQL（多行 VALUES + ON DUPLICATE KEY UPDATE）。
     */
    public static String buildGameInfoBatchUpsertSql(List<GameInfoRow> rows) {
        return buildGameInfoBatchUpsertSql(rows, DEFAULT_BATCH_SIZE);
    }

    public static String buildGameInfoBatchUpsertSql(List<GameInfoRow> rows, int batchSize) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < rows.size(); i += batchSize) {
            int end = Math.min(i + batchSize, rows.size());
            List<GameInfoRow> batch = rows.subList(i, end);
            sql.append("INSERT INTO `game_info` (`vendor_code`,`vendor_name`,`game_name`,`game_code`,`cate_id`,`logo`,`sort`,`status`,`lang`,`support_lang`,`create_time`,`update_time`,`creator`,`updater`) VALUES\n");
            for (int j = 0; j < batch.size(); j++) {
                GameInfoRow row = batch.get(j);
                sql.append(String.format(
                        "('%s','%s','%s','%s',%d,'%s',20,0,'%s','%s',NOW(),NOW(),'system','system')",
                        escapeSql(row.getVendorCode()),
                        escapeSql(row.getVendorName()),
                        escapeSql(row.getGameName()),
                        escapeSql(row.getGameCode()),
                        row.getCateId(),
                        escapeSql(row.getLogo()),
                        escapeSql(row.getLang()),
                        escapeSql(row.getSupportLang())));
                sql.append(j < batch.size() - 1 ? ",\n" : "\n");
            }
            sql.append("ON DUPLICATE KEY UPDATE\n");
            sql.append("  `lang`=VALUES(`lang`), `support_lang`=VALUES(`support_lang`), `update_time`=NOW();\n\n");
        }
        return sql.toString();
    }

    /**
     * 生成 game_code IN 对比 SQL：查库里有、但接口未返回的游戏。
     */
    public static String buildGameCodeCompareSql(String vendorCode, List<String> gameCodes) {
        if (gameCodes == null || gameCodes.isEmpty()) {
            return "-- 接口未返回任何 game_code\n";
        }
        StringBuilder inList = new StringBuilder();
        for (int i = 0; i < gameCodes.size(); i++) {
            if (i > 0) {
                inList.append(',');
            }
            inList.append('\'').append(escapeSql(gameCodes.get(i))).append('\'');
        }
        return String.format(
                "-- 接口返回 %d 个 game_code（vendor=%s）\n" +
                        "-- 查当前库存在、但接口未返回的游戏：\n" +
                        "SELECT game_code, game_name, status, update_time\n" +
                        "FROM game_info\n" +
                        "WHERE vendor_code = '%s'\n" +
                        "  AND game_code NOT IN (%s);\n\n",
                gameCodes.size(), escapeSql(vendorCode), escapeSql(vendorCode), inList);
    }

    private static String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GameInfoRow {
        private String vendorCode;
        private String vendorName;
        private String gameName;
        private String gameCode;
        private int cateId;
        private String logo;
        private String lang;
        private String supportLang;
    }

    public static GameInfoRow buildGameInfoRow(String vendorCode, String vendorName, String rawGameName,
                                               String gameCode, int cateId, String logo,
                                               String supportLang, String translateLocales) {
        GameInfoRow row = new GameInfoRow();
        row.setVendorCode(vendorCode);
        row.setVendorName(vendorName);
        row.setGameName(GoogleTranslateUtil.resolveEnglishName(rawGameName));
        row.setGameCode(gameCode);
        row.setCateId(cateId);
        row.setLogo(logo);
        row.setLang(buildLang(rawGameName, translateLocales));
        row.setSupportLang(supportLang);
        return row;
    }

    /**
     * 已有中英文名：game_name 用英文；lang 中 zh/en 直填，其他语种优先从英文翻译。
     * OneAdapter 请继续用 {@link #buildGameInfoRow}。
     */
    public static GameInfoRow buildGameInfoRowWithZhEn(String vendorCode, String vendorName,
                                                      String nameZh, String nameEn,
                                                      String gameCode, int cateId, String logo,
                                                      String supportLang, String translateLocales) {
        GameInfoRow row = new GameInfoRow();
        row.setVendorCode(vendorCode);
        row.setVendorName(vendorName);
        row.setGameName(StringUtils.isNotBlank(nameEn) ? nameEn.trim()
                : GoogleTranslateUtil.resolveEnglishName(nameZh));
        row.setGameCode(gameCode);
        row.setCateId(cateId);
        row.setLogo(logo);
        String locales = StringUtils.defaultString(translateLocales);
        row.setLang(StringUtils.isBlank(locales) ? "{}"
                : GoogleTranslateUtil.buildGameNameLangJsonWithZhEn(nameZh, nameEn, locales.split(",")));
        row.setSupportLang(supportLang);
        return row;
    }
}
