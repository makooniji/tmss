package cn.iocoder.yudao.module.game.adapter;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public abstract class BasePullTemplate {


    protected final List<GameRecordsDO> pull(String startTime, String endTime,
                                             Integer pageNum, Integer pageSize,
                                             GameVendorDO vendor) {

        List<GameRecordsDO> recordsDOS = new ArrayList<>();
        String requestUrl = vendor.getApiUrl() + getPullPath();
        String requestBody = "";
        int number = 0;
        ThreadUtil.sleep(this.interval());
        try {
            Map<String, Object> params = buildRequestParams(startTime, endTime, pageNum, pageSize, vendor);
            String sign = sign(params, vendor);

            requestBody = JSON.toJSONString(params);


            Map<String, String> headers = buildHeaders(vendor, sign);

            log.info("{} 拉单 page:{} param:{},headers:{}", getPlatform(), pageNum, params, headers);

            String result = HttpUtils.doJsonRequest(requestUrl, params, headers, true).get();
            log.info("{} 拉单 结果:{}", getPlatform(), result);

            JSONArray data = parseResult(result);

            if (data == null || data.isEmpty()) {
                log.info("{} 拉单 结果:{}", getPlatform(), result);
                return null;
            }

            number = data.size();
            recordsDOS.addAll(saveData(data, startTime, endTime, vendor));
            if (hasNextPage(result, pageNum, pageSize)) {
                List<GameRecordsDO> pull = pull(startTime, endTime, pageNum + 1, pageSize, vendor);
                if (ObjectUtil.isNotEmpty(pull)) {
                    recordsDOS.addAll(pull);
                }

            }

        } catch (Exception e) {
            log.error("{} 拉单异常", getPlatform(), e);
        }
        return recordsDOS;
    }


    /**
     * 拉单间隔时间
     *
     * @return
     */
    protected abstract Integer interval();

    protected abstract String getPlatform();

    protected abstract String getPullPath();

    protected abstract Integer getPageSize();

    protected abstract Map<String, Object> buildRequestParams(String startTime, String endTime,
                                                              Integer pageNum, Integer pageSize,
                                                              GameVendorDO vendor);

    protected abstract String sign(Map<String, Object> params, GameVendorDO vendor);

    protected abstract Map<String, String> buildHeaders(GameVendorDO vendor, String sign);

    protected abstract JSONArray parseResult(String result);

    protected abstract boolean hasNextPage(String result, Integer pageNum, Integer pageSize);

    protected abstract List<GameRecordsDO> saveData(JSONArray data, String startTime, String endTime, GameVendorDO vendor);

}