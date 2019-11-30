package com.ruoyi.project.app.controller;

import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.app.domain.MesInfo;
import com.ruoyi.project.app.service.IMesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * app MES个体扫码交互控制层
 * @Author: Rainey
 * @Date: 2019/11/29 11:16
 * @Version: 1.0
 **/
@RestController
@RequestMapping("/app")
public class MesController {

    @Autowired
    private IMesService mesService;

    /**
     * 扫码录入
     */
    @RequestMapping("/scanInfo")
    public AjaxResult scanInfo(@RequestBody MesInfo mesInfo){
        return mesService.scanInfo(mesInfo);
    }

}
