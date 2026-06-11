package cn.iocoder.yudao.module.game.adapter.sports.base;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.game.adapter.sports.AbstractSportPlatform;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import cn.iocoder.yudao.module.system.exception.GameException;
import cn.iocoder.yudao.module.system.model.api.PlatformRequest;
import cn.iocoder.yudao.module.system.model.api.PlatformResponse;
import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.DSAdjustment;
import cn.iocoder.yudao.module.system.model.api.push.DSRollback;
import cn.iocoder.yudao.module.system.model.api.push.sports.*;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 体育核心基础抽象类
 */
@Slf4j
@Service
public abstract class BaseSportAdapter extends AbstractSportPlatform {

    protected abstract String gameCode();

    @Resource
    protected DSManager DSManager;

    protected abstract PlatformResponse<Object> buildBalanceResult(SportContext sportContext);

    protected abstract PlatformResponse<Object> buildIdleResult(SportContext sportContext);

    protected JSONObject doRequest(String url, Map<String, Object> paramMap, String remark, String sign, Boolean async) {
        JSONObject parseObject;
        try {
            log.info("{}，请求参数 paramMap={}", url, paramMap);
            CompletableFuture<JSONObject> result = HttpUtils.doFormRequest(url, paramMap, null, async);

            if (ObjectUtil.isEmpty(result)) {
                throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
            }
            parseObject = result.get(20, TimeUnit.SECONDS);
        }
        catch (TimeoutException e) {
            log.error("Sports请求超时，url={}, paramMap={}", url, paramMap, e);
            throw new GameException(PlatformCodeEnum.TIMEOUT_ERROR);
        }catch (Exception e) {
            log.error("Sports请求异常:", e);
            throw new GameException(PlatformCodeEnum.INTERNAL_ERROR.getCode(), "请求失败：" + e.getMessage());
        }
        String code = parseObject.getString("error_code");
        if (!code.equals("0")) {
            log.error("{}，错误代码 code={}", remark, code);
            throw new GameException(parseObject.getString("message"));
        }
        return parseObject;
    }

    protected abstract DSBetRequest buildBet(PlatformRequest param, SportContext context);

    protected abstract DSBetRequest buildBetPar(PlatformRequest param, ActionEnum actionEnum, SportContext context);

    protected abstract List<DSUpdateBet> buildConfirm(PlatformRequest param, ActionEnum actionEnum, SportContext context);

    protected abstract List<DSRollback> buildRefund(PlatformRequest param, ActionEnum actionEnum, SportContext context);

    protected abstract List<DSSettle> buildSettle(PlatformRequest param, ActionEnum actionEnum, SportContext context);

    protected abstract List<DSUnSettle> buildUnSettle(PlatformRequest param, ActionEnum actionEnum, SportContext context);

    protected abstract List<DSReSettle> buildReSettle(PlatformRequest param, ActionEnum actionEnum, SportContext context);

    protected abstract DSAdjustment buildAdjustment(PlatformRequest param, ActionEnum actionEnum, SportContext context);

}