package cn.iocoder.yudao.server.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;

/**
 * 默认 Controller，解决部分 module 未开启时的 404 提示。
 * 例如说，/bpm/** 路径，工作流
 *
 * @author osca
 */
@RestController
@Slf4j
public class DefaultController {

    @Value("${merchant.domain}")
    private String merchantDomain;

    /**
     * 获取系统icon
     * @param code
     * @return
     */
    @RequestMapping("/api/client/support-icon")
    @PermitAll
    public  CommonResult<String> supportIcon(@RequestParam(required = false) String code) {
       String path= merchantDomain+"/api/client/tenant/get-browser-icon";
        Map<String,Object> map = new HashMap<>();
        if(ObjectUtil.isNotEmpty(code)){
            map.put("code",code);
        }
        String body = HttpUtil.get(path,map);
        JSONObject s = JSONObject.parseObject(body);
        return CommonResult.success( s.getString("data"));
    }


    /**
     * 测试接口：打印 query、header、body
     */
    @RequestMapping(value = { "/test" })
    @PermitAll
    public CommonResult<Boolean> test(HttpServletRequest request) {
        // 打印查询参数
        log.info("Query: {}", ServletUtils.getParamMap(request));
        // 打印请求头
        log.info("Header: {}", ServletUtils.getHeaderMap(request));
        // 打印请求体
        log.info("Body: {}", ServletUtils.getBody(request));
        return CommonResult.success(true);
    }

}
