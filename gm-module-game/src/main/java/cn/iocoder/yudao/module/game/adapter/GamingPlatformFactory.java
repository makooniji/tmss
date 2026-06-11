package cn.iocoder.yudao.module.game.adapter;

import cn.iocoder.yudao.module.game.adapter.game.IGamingPlatform;
import cn.iocoder.yudao.module.game.adapter.sports.ISportPlatform;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GamingPlatformFactory {

    private final Map<String, IPlatform> platformMap = new HashMap<>();
    public GamingPlatformFactory(List<IPlatform> platforms) {
        for (IPlatform platform : platforms) {
            platformMap.put(platform.getVendorCode().toUpperCase(), platform);
        }

    }

    public IPlatform getPlatform(String code) {
        return platformMap.getOrDefault(code.toUpperCase(),null);
    }
}