package cn.iocoder.yudao.report.controller.api;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.framework.websocket.biz.WebSocketSenderApi;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import cn.iocoder.yudao.module.system.enums.notice.NoticeTypeEnum;
import cn.iocoder.yudao.module.system.mapper.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.model.CategoryDO;
import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.model.vo.tenant.TenantSaveReqVO;
import cn.iocoder.yudao.module.system.model.vo.tenant.TenantUpdateStatusReqVO;
import cn.iocoder.yudao.module.system.service.AdminUserService;
import cn.iocoder.yudao.module.system.service.TenantService;
import cn.iocoder.yudao.module.system.service.VendorService;
import cn.iocoder.yudao.module.system.service.VendorTenantService;
import cn.iocoder.yudao.module.system.service.category.CategoryService;
import cn.iocoder.yudao.module.system.service.notice.NoticeService;
import cn.iocoder.yudao.report.controller.api.dto.*;
import cn.iocoder.yudao.report.convert.StaticConvert;
import cn.iocoder.yudao.report.framework.tenant.AdminReportTenantHelper;
import cn.iocoder.yudao.report.service.TenantDayStaticService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 游戏模块内部服务
 */
@RestController
@RequestMapping("/game/support")
@Validated
@PermitAll
public class GameSupportApi {
    @Resource
    private AdminUserService userService;
    @Resource
    private CategoryService categoryService;

    @Resource
    private VendorService vendorService;


    @Resource
    private TenantService tenantService;

    @Resource
    private TenantDayStaticService tenantDayStaticService;

    @Resource
    private TenantMapper tenantMapper;


    /**
     * 查询分类
     *
     * @return
     */
    @GetMapping("/categorys")
    public CommonResult<List<CategoryRespDto>> getCategory() {
        List<CategoryDO> list = categoryService.getList();

        return success(StaticConvert.INSTANCE.convertList(list));
    }

    /**
     * 查询场馆
     *
     * @return
     */
    @GetMapping("/vendors")
    public CommonResult<List<VendorRespDto>> getVendor() {
        List<GameVendorDO> list = vendorService.getList();

        return success(StaticConvert.INSTANCE.toSupportList(list));
    }

    /**
     * 获取货币
      * @return
     */
    @GetMapping("/currencys")
    public CommonResult<List<String>> getCurrencies(@RequestParam String start, @RequestParam String end,
                                                    @RequestParam(required = false) Integer tid,@RequestParam(required = false) String merchantCode) {
        return success(tenantDayStaticService.getCurrencies(start,end,tid,merchantCode));
    }
    @Resource
    private VendorTenantService vendorTenantService;
    /**
     * 创建商户
     *
     * @param createReqVO
     * @return
     */
    @PostMapping("/create")
    public CommonResult<Long> createTenant(@Valid @RequestBody TenantSaveReqDto createReqVO) {
        TenantSaveReqVO tenantSaveReqVO = BeanUtils.toBean(createReqVO, TenantSaveReqVO.class);
        tenantSaveReqVO.setUsername(tenantSaveReqVO.getName());
        tenantSaveReqVO.setParentId(0L);
        tenantSaveReqVO.setParentId(0L);
        tenantSaveReqVO.setCurrency(List.of(createReqVO.getCurrency()));
        List<String> supportVendors = vendorTenantService.getSupportVendors();
        tenantSaveReqVO.setVendorCodes(supportVendors);

        return success(tenantService.createTenant(tenantSaveReqVO));
    }

    /**
     * 修改商户
     *
     * @param createReqVO
     * @return
     */
    @PostMapping("/update")
    public CommonResult<Boolean> updateTenant(@Valid @RequestBody TenantSaveReqDto createReqVO) {
        TenantSaveReqVO tenantSaveReqVO = BeanUtils.toBean(createReqVO, TenantSaveReqVO.class);
        tenantService.updateTenantByCode(tenantSaveReqVO);
        return success(true);
    }

    /**
     * 重置密码
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/update-password")
    public CommonResult<Boolean> updateUserPassword(@Valid @RequestBody UserUpdatePasswordReqDto reqVO) {
        TenantDO tenantDO = tenantService.getTenantByCode(reqVO.getMerchantCode());
        userService.updateUserPassword(tenantDO.getName(), reqVO.getPassword());
        return success(true);
    }


    /**
     * 更新商户状态
     *
     * @param updateStatusReqVO
     * @return
     */
    @PostMapping("/update/status")
    public CommonResult<Boolean> updateTenantSatus(@Valid @RequestBody TenantUpdateStatusReqDto updateStatusReqVO) {

        tenantService.updateTenantStatus(BeanUtils.toBean(updateStatusReqVO, TenantUpdateStatusReqVO.class));
        return success(true);
    }

    /**
     * 更新运营状态
     *
     * @param updateStatusReqVO
     * @return
     */
    @PostMapping("/update/operate/status")
    public CommonResult<Boolean> updateOperateSatus(@Valid @RequestBody TenantUpdateStatusReqDto updateStatusReqVO) {
        tenantService.updateOperateStatus(BeanUtils.toBean(updateStatusReqVO, TenantUpdateStatusReqVO.class));
        return success(true);
    }


    /**
     * 根据租户ID删除商户
     *
     * @param tids
     * @return
     */
    @DeleteMapping("/delete/tid")
    public CommonResult<Boolean> deleteTenantByTid(@RequestParam List<Long> tids) {
        tenantService.deleteTenantListByTid(tids);
        return success(true);
    }

    /**
     * 根据商户编码删除
     * @param codes
     * @return
     */
    @DeleteMapping("/delete/code")
    public CommonResult<Boolean> deleteTenantByCodes(@RequestParam List<String> codes) {
        tenantService.deleteTenantListByCode(codes);
        return success(true);
    }


    /**
     * 游戏数据概览-商户数据分页
     *
     * @param pageReqVO
     * @return
     */
    @PostMapping("/merchant/page")
    public CommonResult<PageResult<TenantStaticRespDto>> getMerchantPage(@Valid @RequestBody MerchantPageReqDto pageReqVO) {
        return success(tenantDayStaticService.getSupportMerchantStaticPage(pageReqVO));
    }

    /**
     * 游戏信息管理-商户信息分页
     *
     * @param pageReqVO
     * @return
     */
    @PostMapping("/merchant/info/page")
    public CommonResult<PageResult<MerchantRespDto>> getMerchantInfoPage(@Valid @RequestBody MerchantPageReqDto pageReqVO) {
        return success(tenantDayStaticService.getSupportMerchantInfoPage(pageReqVO));
    }

    /**
     * 游戏数据概览-方块统计
     *
     * @return
     */
    @GetMapping("/merchant/total")
    public CommonResult<MerchantTotalRespDto> getMerchantTotal(@RequestParam(required = false) Integer tid) {
        return success(tenantDayStaticService.getSupportMerchantTotal(tid));
    }

    /**
     * 游戏数据概览-列表查看详情
     *
     * @param pageReqVO
     * @return
     */
    @PostMapping("/vendor/page")
    public CommonResult<PageResult<VendorStaticRespDto>> getVendorPage(@Valid @RequestBody VendorPageReqDto pageReqVO) {
        return success(tenantDayStaticService.getSupportVendorStaticPage(pageReqVO));
    }

    /**
     * 租户信息管理-数据概览
     * @return
     */
    @PostMapping("/vendor/total")
    public CommonResult<List<VendorTotalRespDto>> getVendorTotal(@Valid @RequestBody VendorTotalReqDto pageReqVO) {
        return success(tenantDayStaticService.getSupportVendorTotal(pageReqVO));
    }

    @Resource
    private NoticeService noticeService;

    /**
     * 创建系统通知
     * @param createReqVO
     * @return
     */
    @PostMapping("/notice/create")
    public CommonResult<Long> createNotice(@Valid @RequestBody NoticeSaveReqVO createReqVO) {
        TenantUtils.executeIgnore(()->{
            List<TenantDO> tenantDOS = tenantMapper.selectList(Wrappers.<TenantDO>lambdaQuery().
                    in(createReqVO.getCodes() != null, TenantDO::getCode, createReqVO.getCodes())
                    .eq(createReqVO.getTid() != null, TenantDO::getTid, createReqVO.getTid())
            );
            tenantDOS.forEach(item->{
                TenantUtils.execute(item.getId(),()-> {
                    NoticeSaveReqVO bean = BeanUtils.toBean(createReqVO, NoticeSaveReqVO.class);
                    bean.setStatus(CommonStatusEnum.ENABLE.getStatus());
                    bean.setType(NoticeTypeEnum.NOTICE.getType());
                    noticeService.createNotice(createReqVO);
                });
            });

        });

        return success(1L);
    }

    /**
     * 租户系统管理- 租户账单
     * @param pageReqVO
     * @return
     */
    @PostMapping("/vendor/merchant/total")
    public CommonResult<List<VendorTotalRespDto>> getVendorMerchantTotal( @Valid @RequestBody VendorTotalReqDto pageReqVO) {
        return success(tenantDayStaticService.getVendorMerchantTotal(pageReqVO));
    }

}