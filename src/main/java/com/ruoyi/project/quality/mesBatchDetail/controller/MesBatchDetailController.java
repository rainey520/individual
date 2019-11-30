package com.ruoyi.project.quality.mesBatchDetail.controller;

import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.quality.mesBatchDetail.domain.MesBatchDetail;
import com.ruoyi.project.quality.mesBatchDetail.service.IMesBatchDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MES个体扫码记录 信息操作处理
 *
 * @author sj
 * @date 2019-11-28
 */
@Controller
@RequestMapping("/mes/mesBatchDetail")
public class MesBatchDetailController extends BaseController {
    private String prefix = "quality/mesBatchDetail";

    @Autowired
    private IMesBatchDetailService mesBatchDetailService;

    @GetMapping()
    public String mesBatchDetail() {
        return prefix + "/mesBatchDetail";
    }

    /**
     * 查询MES个体扫码记录列表
     */
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(MesBatchDetail mesBatchDetail) {
        startPage();
        List<MesBatchDetail> list = mesBatchDetailService.selectMesBatchDetailList(mesBatchDetail);
        return getDataTable(list);
    }


    /**
     * 导出MES个体扫码记录列表
     */
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(MesBatchDetail mesBatchDetail) {
        List<MesBatchDetail> list = mesBatchDetailService.selectMesBatchDetailList(mesBatchDetail);
        ExcelUtil<MesBatchDetail> util = new ExcelUtil<MesBatchDetail>(MesBatchDetail.class);
        return util.exportExcel(list, "mesBatchDetail");
    }

    /**
     * 新增MES个体扫码记录
     */
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存MES个体扫码记录
     */
    @Log(title = "MES个体扫码记录", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(MesBatchDetail mesBatchDetail) {
        return toAjax(mesBatchDetailService.insertMesBatchDetail(mesBatchDetail));
    }

    /**
     * 修改MES个体扫码记录
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, ModelMap mmap) {
        MesBatchDetail mesBatchDetail = mesBatchDetailService.selectMesBatchDetailById(id);
        mmap.put("mesBatchDetail", mesBatchDetail);
        return prefix + "/edit";
    }

    /**
     * 修改保存MES个体扫码记录
     */
    @Log(title = "MES个体扫码记录", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(MesBatchDetail mesBatchDetail) {
        return toAjax(mesBatchDetailService.updateMesBatchDetail(mesBatchDetail));
    }

    /**
     * 删除MES个体扫码记录
     */
    @Log(title = "MES个体扫码记录", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(mesBatchDetailService.deleteMesBatchDetailByIds(ids));
    }

    /**
     * 查看个体扫码明细信息
     */
    @GetMapping("/scanDetail")
    public String scanDetail(Integer bId, ModelMap map){
        map.put("bId",bId);
        return "quality/mesBatch/oneScanDetail";
    }

}
