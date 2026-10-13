package cn.iocoder.yudao.module.game.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GamingPlatformFactory {

    private final Map<String, IPlatform> platformMap = new HashMap<>();

    public GamingPlatformFactory(List<IPlatform> platforms) {
        for (IPlatform platform : platforms) {
            platformMap.put(platform.getVendorCode().toUpperCase(), platform);
        }
    }

    public IPlatform getPlatform(String code) {
        if (code == null || code.isBlank()) {
            log.error("获取游戏平台失败：adapter 为空");
            return null;
        }
        return platformMap.get(code.toUpperCase());
    }
}
