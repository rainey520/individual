package com.ruoyi.project.app.service;

import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.app.domain.MesInfo;

/**
 * app MES个体扫码交互逻辑层接口
 * @Author: Rainey
 * @Date: 2019/11/29 11:17
 * @Version: 1.0
 **/
public interface IMesService {

    /**
     * 扫码录入
     * @param mesInfo 录入信息
     * @return 结果
     */
    AjaxResult scanInfo(MesInfo mesInfo);
}
