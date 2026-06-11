package cn.iocoder.yudao.module.game.adapter.game.pg.pull;
import cn.iocoder.yudao.module.game.adapter.BaseResultTemplate;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PGResultImpl extends BaseResultTemplate {




    @Override
    protected TimeRange buildTimeRange(String params, GameVendorDO vendor) {
//        long minusMinutes = 5;
//        if (StringUtils.isNotBlank(params)) {
//            minusMinutes = Long.parseLong(params);
//        }
//        String startTime = "";
//        String endTime = "";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//        // 区分时区
//        if (StringUtils.isNotBlank(vendor.getVendorZone())) {
//            ZoneId indiaZone = ZoneId.of(vendor.getVendorZone());
//            LocalDateTime startOfDay = LocalDateTime.now(indiaZone).minusMinutes(minusMinutes);
//            LocalDateTime endOfDay = LocalDateTime.now(indiaZone);
//            startTime = startOfDay.format(formatter);
//            endTime = endOfDay.format(formatter);
//        } else {
//            // 默认当前时间往前 5 分钟
//            LocalDateTime startOfDay = LocalDateTime.now().minusMinutes(minusMinutes);
//            // 当前时间往前 1 分钟
//            LocalDateTime endOfDay = LocalDateTime.now();
//            startTime = startOfDay.format(formatter);
//            endTime = endOfDay.format(formatter);
//        }
        return null;
    }

    @Override
    protected String getPlatform() {
        return "PG";
    }

    @Override
    protected String getPullPath() {
        return "?action=getGameLog";
    }

    @Override
    protected Integer getPageSize() {
        return 1000;
    }

    @Override
    protected Map<String, Object> buildRequestParams(String startTime, String endTime, Integer pageNum, Integer pageSize, GameVendorDO vendor) {
        Map<String, Object> params = new HashMap<>();
        params.put("appId", vendor.getVendorAgent());
        params.put("com_name", getPlatform());
        params.put("stime", startTime);
        params.put("etime", endTime);
        params.put("page", pageNum);
        params.put("page_size", pageSize);
//        String sign = IgsSignUtils.buildSign(params, vendor.getVendorKey());
//        params.put("sign", sign);
//        reqBody = JSON.toJSONString(params);
//        log.info("PG接口请求reqUrl:{}, 查询时间:{}", reqBody, startTime + "-" + endTime);
        return params;
    }

    @Override
    protected String sign(Map<String, Object> params, GameVendorDO vendor) {
        return "";
    }

    @Override
    protected Map<String, String> buildHeaders(GameVendorDO vendor, String sign) {
        return Map.of();
    }

    @Override
    protected JSONArray parseResult(String result) {
        return null;
    }

    @Override
    protected boolean hasNextPage(String result, Integer pageNum, Integer pageSize) {
        return false;
    }

    @Override
    protected List<GameRecordsDO> saveData(JSONArray data, String startTime, String endTime, GameVendorDO vendor) {
        return List.of();
    }
}
