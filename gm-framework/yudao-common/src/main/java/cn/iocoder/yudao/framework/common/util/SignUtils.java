package cn.iocoder.yudao.framework.common.util;

import cn.hutool.crypto.SignUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class SignUtils extends SignUtil {

    /**
     * 验签
     */
    public static Boolean verifySign(Map<String, Object> dataMap, String key, String sign) {
        String data = JsonUtils.toJsonString(dataMap);
        log.info("verifySign : 原始内容 {} ,key:{},sign:{}",data,key,sign);
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, key.getBytes(StandardCharsets.UTF_8));
        String signData = hmac.digestHex(data);
        log.info("verifySign : 加密内容{}",signData);
        return signData.equals(sign);
    }
    public static Boolean verifySign(String rawJson, String key, String sign) {

        log.info("verifySign : 原始内容 {} ,key:{}",rawJson,key);
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, key.getBytes(StandardCharsets.UTF_8));
        String signData = hmac.digestHex(rawJson);
        log.info("verifySign : 加密内容{}",signData);
        return signData.equals(sign);
    }

    /**
     * 生成签名
     */
    public static String buildSign(Map<String, Object> dataMap, String key) {
        String data = JsonUtils.toJsonString(dataMap);
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, key.getBytes(StandardCharsets.UTF_8));
        return hmac.digestHex(data);
    }

    /**
     * 生成随机密钥（推荐 32 字节）
     */
    public static String generateKey() {
        byte[] keyBytes = new byte[32]; // 256 bit
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(keyBytes);

        return Base64.getEncoder().encodeToString(keyBytes);
    }
}