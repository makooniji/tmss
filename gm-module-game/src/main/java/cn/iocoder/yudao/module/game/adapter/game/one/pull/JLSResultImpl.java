package cn.iocoder.yudao.module.game.adapter.game.one.pull;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.game.adapter.BaseResultTemplate;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Slf4j
public class JLSResultImpl extends BaseResultTemplate {

    @Override
    protected String getPlatform() {
        return "JLS";
    }

    @Override
    protected String getPullPath() {
        return "/transaction/list";
    }

    @Override
    protected Integer getPageSize() {
        return 1000;
    }

    @Override
    protected TimeRange buildTimeRange(String params, GameVendorDO vendor) {
        long minusMinutes = 5;
        if (StringUtils.isNotBlank(params)) {
            minusMinutes = Long.parseLong(params);
        }

        return new TimeRange(
                String.valueOf(LocalDateTime.now().minusMinutes(minusMinutes)
                        .atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()),
                String.valueOf(LocalDateTime.now()
                        .atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli())
        );
    }

    @Override
    protected Map<String, Object> buildRequestParams(String start, String end,
                                                     Integer page, Integer size,
                                                     GameVendorDO vendor) {
        Map<String, Object> params = new HashMap<>();
        params.put("traceId", IdUtil.fastUUID());
        params.put("fromTime", start);
        params.put("toTime", end);
        params.put("pageNo", page);
        params.put("pageSize", size);
        return params;
    }

    @Override
    public String sign(Map<String, Object> params, GameVendorDO vendor) {

        String data = JsonUtils.toJsonString(params);
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, vendor.getVendorKey().getBytes(StandardCharsets.UTF_8));
        return hmac.digestHex(data);
    }

    @Override
    protected Map<String, String> buildHeaders(GameVendorDO vendor, String sign) {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("X-API-Key", vendor.getVendorAgent());
        header.put("X-Signature", sign);
        return header;
    }

    @Override
    protected JSONArray parseResult(String result) {
        return JSON.parseObject(result).getJSONObject("data").getJSONArray("transactions");
    }

    @Override
    protected boolean hasNextPage(String result, Integer page, Integer size) {
        Integer total = JSON.parseObject(result)
                .getJSONObject("data")
                .getInteger("totalItems");
        return total - page * size > 0;
    }

    @Override
    protected List<GameRecordsDO> saveData(JSONArray data, String start, String end, GameVendorDO vendor) {
        List<GameRecordsDO> recordsList = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONArray row = data.getJSONArray(i);


            GameRecordsDO game = new GameRecordsDO();
            Long id = IdUtil.getSnowflakeNextId();
            game.setId(id);
            game.setVendorCode(this.getPlatform());
            String account = row.getString(3);
            String userPrefix = account.substring(0, 5);


            Boolean isFreeSpin = row.getBoolean(16);
            if (isFreeSpin) {
                continue;
            }
            //过滤其他厂商的游戏拉取
            String vendorCode = row.getString(6);

            Long userId = Long.parseLong(account.replace(userPrefix, ""));
            game.setUserId(userId);
            // 第三方交易流水号
            String transNo = row.getString(2);
            game.setRefOrderNo(transNo);
            game.setOrderNo(row.getString(0));

            game.setIssueNo(row.getString(1));

            int status = row.getIntValue(13);
            if (status == 0) {
                game.setAction(ActionEnum.DEBIT);
            } else if (Objects.equals(1, status)) {
                game.setAction(ActionEnum.UNSETTLE);
            } else if (status == 2 || status == 3) {
                game.setAction(ActionEnum.REFUND);
            }
            String gameId = row.getString(5);


            double amount = Convert.toDouble(row.getString(8));
            double payAmount = Convert.toDouble(row.getString(9));
            game.setBetAmount(BigDecimal.valueOf(amount));
            game.setWinAmount(BigDecimal.valueOf(payAmount));
            game.setUpdateTime(LocalDateTime.now());

            recordsList.add(game);
        }

        return recordsList;

    }
}