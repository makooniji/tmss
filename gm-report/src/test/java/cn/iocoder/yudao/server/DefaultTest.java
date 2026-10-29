package cn.iocoder.yudao.server;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpDownloader;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.web.currency.CurrencyContextHolder;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.system.mapper.GameInfoMapper;
import cn.iocoder.yudao.module.system.mapper.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.model.GameInfoDO;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.report.GMReportApp;
import cn.iocoder.yudao.report.controller.api.dto.MerchantTotalRespDto;
import cn.iocoder.yudao.report.service.TenantDayStaticService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaTypeFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest(classes = GMReportApp.class)
@ContextConfiguration(classes = GMReportApp.class)
@ActiveProfiles("local")
@RunWith(SpringRunner.class)
@Slf4j
public class DefaultTest {

    @Resource
    private TenantDayStaticService tenantDayStaticService;

    @Value("${merchant.domain}")
    private String merchantDomain;

    @Test
    public void supportIcon() {
        String path= merchantDomain+"/api/client/tenant/get-browser-icon";
        String body = HttpUtil.get(path);
        JSONObject s = JSONObject.parseObject(body);
        String data = s.getString("data");
        log.info(data);
    }
    @Test
    public void  testTenant(){
        CurrencyContextHolder.setCurrency("BRL");
        MerchantTotalRespDto supportMerchantTotal = tenantDayStaticService.getSupportMerchantTotal(1);
        Assert.assertNotNull(supportMerchantTotal);
    }


    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private GameInfoMapper gameInfoMapper;

    @Data
    public static class PGGame{

        private Integer id;
        private String name;
        private String enname;
        private String category;
        private String ico;
    }

    @Test
    public void  testPG(){
        String body = HttpUtil.get("https://api.gamecsjshqk923.cyou/api/games");
        List<GameInfoDO> gameInfoDOS = new ArrayList<>();
        List<PGGame> jsonObject = JSONObject.parseArray(body, PGGame.class);
        Map<String, List<PGGame>> gamesByCategory = jsonObject.stream().collect(Collectors.groupingBy(p->p.category.toUpperCase()));
        gamesByCategory.forEach((category, games) -> {
            log.info("Category: {}", category);

            if(category.equalsIgnoreCase("dggaming")){
                category="DG";
            }

            for (PGGame game : games) {



                    String gameId = game.getId().toString();
                    String gameName = game.getEnname();
                    String logo = game.getIco();
                    GameInfoDO gameInfoDO = new GameInfoDO();
                    gameInfoDO.setGameCode(gameId);
                    gameInfoDO.setGameName(gameName);
                    gameInfoDO.setLogo(logo);
                    gameInfoDO.setVendorCode(category);
                    gameInfoDO.setVendorName(category);
                    gameInfoDO.setCateId(1L);
                    gameInfoDO.setSubCateId(19L);
                    gameInfoDO.setSort(1);
                    gameInfoDO.setMaintain(0);
                    gameInfoDO.setStatus(0);
                    gameInfoDO.setCreateTime(LocalDateTime.now());
                    gameInfoDO.setUpdateTime(LocalDateTime.now());
                    gameInfoDO.setOnlineCount(0);
                    gameInfoDO.setSupportCurrency(List.of( "BRL"));
                    gameInfoDO.setSupportLang(List.of( "zh-CN","en-US"));

                    gameInfoDOS.add(gameInfoDO);

            }
        });

        gameInfoMapper.insertIgnoreBatch(gameInfoDOS);

    }

    @Test
    public void  testMap(){
        TenantMapper.TenantStatusCount tenantStatusCount = tenantMapper.selectTenantStatus(
                new LambdaQueryWrapperX<TenantDO>()
                        .gt(TenantDO::getId, 1)
                        .apply("deleted = 0"));
        log.info("{}",tenantStatusCount);
    }

    @Resource
    private FileService fileService;


    /**
     * 修复非存储同资源
     */
    @Test
    public void uploadFile() {

        List<GameInfoDO> gameInfoDOS = gameInfoMapper.selectList(Wrappers.<GameInfoDO>lambdaQuery()
                .isNotNull(GameInfoDO::getLogo)
                .ne(GameInfoDO::getLogo, "")
                .apply("logo  LIKE 'https://m3.dggame365%'"));


        for (GameInfoDO gameInfo : gameInfoDOS) {
            String oldLogoUrl = gameInfo.getLogo();
            if (StrUtil.isBlank(oldLogoUrl)) {
                continue;
            }

            try {
                // 2. 通过 URL 下载远程图片转化为 byte[]（使用 Hutool HttpUtil）
                byte[] content = HttpDownloader.downloadBytes(oldLogoUrl,3000);
                if (ArrayUtil.isEmpty(content)) {
                    log.warn("下载 Logo 失败或文件内容为空，gameId: {}, url: {}", gameInfo.getId(), oldLogoUrl);
                    continue;
                }

                // 3. 提取原文件名（若路径无后缀则兜底使用 gameId.png）
                String originalFilename = FileUtil.getName(oldLogoUrl);
                if (StrUtil.isBlank(originalFilename) || !originalFilename.contains(".")) {
                    originalFilename = gameInfo.getId() + ".png";
                }
                String contentType = MediaTypeFactory.getMediaType(originalFilename)
                        .map(Object::toString)
                        .orElse("image/png");

                // 4. 调用文件服务重新上传到 S3 / CDN 存储桶并获取新地址
                // 请根据你本地 FileService 的实际签名匹配参数，例如：createFile(content, originalFilename, path) 或 createFile(content, originalFilename)
                String newLogoUrl = fileService.createFile(content, originalFilename,"",contentType);

                // 5. 更新数据库
                gameInfo.setLogo(newLogoUrl);
                gameInfoMapper.updateById(gameInfo);

                log.info("Logo 更新成功！gameId: {}, 新地址: {}", gameInfo.getId(), newLogoUrl);

            } catch (Exception e) {
                // 单条处理失败时不中断整个循环
                log.error("处理 Logo 异常，gameId: {}, oldUrl: {}", gameInfo.getId(), oldLogoUrl, e);
            }
        }

    }
}