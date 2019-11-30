package com.ruoyi.project.quality.mesBatch.controller;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.quality.mesBatch.domain.MesBatch;
import com.ruoyi.project.quality.mesBatch.service.IMesBatchService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * MES个体追溯建档 信息操作处理
 *
 * @author sj
 * @date 2019-11-28
 */
@Controller
@RequestMapping("/mes/mesBatch")
public class MesBatchController extends BaseController {
    private String prefix = "quality/mesBatch";

    @Autowired
    private IMesBatchService mesBatchService;

    @RequiresPermissions("mes:mesBatch:view")
    @GetMapping()
    public String mesBatch() {
        return prefix + "/mesBatch";
    }

    /**
     * 查询MES个体追溯建档列表
     */
    @RequiresPermissions("mes:mesBatch:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(MesBatch mesBatch) {
        startPage();
        List<MesBatch> list = mesBatchService.selectMesBatchList(mesBatch);
        return getDataTable(list);
    }


    /**
     * 导出MES个体追溯建档列表
     */
    @RequiresPermissions("mes:mesBatch:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(MesBatch mesBatch) {
        List<MesBatch> list = mesBatchService.selectMesBatchList(mesBatch);
        ExcelUtil<MesBatch> util = new ExcelUtil<MesBatch>(MesBatch.class);
        return util.exportExcel(list, "mesBatch");
    }

    /**
     * 新增MES个体追溯建档
     */
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存MES个体追溯建档
     */
    @RequiresPermissions("mes:mesBatch:add")
    @Log(title = "MES个体追溯建档", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(MesBatch mesBatch) {
        return toAjax(mesBatchService.insertMesBatch(mesBatch));
    }

    /**
     * 修改MES个体追溯建档
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, ModelMap mmap) {
        MesBatch mesBatch = mesBatchService.selectMesBatchById(id);
        mmap.put("mesBatch", mesBatch);
        return prefix + "/edit";
    }

    /**
     * 修改保存MES个体追溯建档
     */
    @RequiresPermissions("mes:mesBatch:edit")
    @Log(title = "MES个体追溯建档", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(MesBatch mesBatch) {
        return toAjax(mesBatchService.updateMesBatch(mesBatch));
    }

    /**
     * 删除MES个体追溯建档
     */
    @RequiresPermissions("mes:mesBatch:remove")
    @Log(title = "MES个体追溯建档", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(mesBatchService.deleteMesBatchByIds(ids));
    }

    /**
     * 导出对应工单的产品建档信息
     */
    @PostMapping("/exportAllPnInfo")
    @ResponseBody
    public AjaxResult exportAllPnInfo(MesBatch mesBatch) {
        List<MesBatch> list = new ArrayList<>();
        String fileName = "建档信息";
        if (mesBatch != null && StringUtils.isNotEmpty(mesBatch.getWorkCode())) {
            list = mesBatchService.selectMesBatchList(mesBatch);
            fileName = mesBatch.getWorkCode() + "建档信息";
        }
        ExcelUtil<MesBatch> util = new ExcelUtil<MesBatch>(MesBatch.class);
        return util.exportExcel(list, fileName);
    }

    /**
     * 查看工单建档信息
     */
    @GetMapping("/scanInfo")
    public String scanInfo(String workCode, ModelMap map) {
        map.put("workCode", workCode);
        return prefix + "/scanInfo";
    }

}
