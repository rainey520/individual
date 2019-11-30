package com.ruoyi.project.app.service.impl;

import com.ruoyi.common.constant.CompanyConstants;
import com.ruoyi.common.constant.MesConstants;
import com.ruoyi.common.constant.WorkConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.app.domain.MesInfo;
import com.ruoyi.project.app.service.IMesService;
import com.ruoyi.project.production.devWorkOrder.domain.DevWorkOrder;
import com.ruoyi.project.production.devWorkOrder.mapper.DevWorkOrderMapper;
import com.ruoyi.project.production.productionLine.domain.ProductionLine;
import com.ruoyi.project.production.productionLine.mapper.ProductionLineMapper;
import com.ruoyi.project.quality.mesBatch.domain.MesBatch;
import com.ruoyi.project.quality.mesBatch.mapper.MesBatchMapper;
import com.ruoyi.project.quality.mesBatchDetail.domain.MesBatchDetail;
import com.ruoyi.project.quality.mesBatchDetail.mapper.MesBatchDetailMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * app MES个体扫码交互逻辑层实现
 *
 * @Author: Rainey
 * @Date: 2019/11/29 11:18
 * @Version: 1.0
 **/
@Service
public class MesServiceImpl implements IMesService {

    /**
     * 日志记录
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MesServiceImpl.class);

    @Autowired
    private MesBatchMapper mesBatchMapper;

    @Autowired
    private ProductionLineMapper lineMapper;

    @Autowired
    private DevWorkOrderMapper workOrderMapper;

    @Autowired
    private MesBatchDetailMapper mesBatchDetailMapper;

    /**
     * 扫码录入
     *
     * @param mesInfo 录入信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult scanInfo(MesInfo mesInfo) {
        try {
            if (mesInfo == null || StringUtils.isEmpty(mesInfo.getMainInfo()) || mesInfo.getLineId() <= 0 || mesInfo.getOrderNum() <= 0 || mesInfo.getOrderNum() > 24) {
                return AjaxResult.api(1, "扫描参数错误", null);
            }
            // 产品建档信息
            String onePnMain = mesInfo.getMainInfo();
            // 产线id信息
            int lineId = mesInfo.getLineId();
            // 产品排序信息
            int orderNum = mesInfo.getOrderNum();

            Map<String, Object> map = new HashMap<>(16);
            /**
             * 首次扫描建档信息进行扫码确认
             */
            if (StringUtils.isNotEmpty(onePnMain) && StringUtils.isEmpty(mesInfo.getScanInfo())) {
                // 查询产线信息
                ProductionLine line = lineMapper.selectProductionLineById(lineId);
                if (line == null) {
                    return AjaxResult.api(1, "产线不存在或被删除", null);
                }
                // 查询建档信息
                MesBatch mesBatch = mesBatchMapper.selectMesBatchByMainInfo(line.getCompanyId(), onePnMain);
                if (mesBatch == null) {
                    return AjaxResult.api(1, "未找到建档记录", null);
                }
                if (mesBatch.getLineId() != lineId) {
                    return AjaxResult.api(1, "请扫码关联产线的产品信息", null);
                }
                // 查询扫码的工单是否已经完成
                DevWorkOrder workOrder = workOrderMapper.selectWorkOrderByWorkCode(mesBatch.getWorkCode());
                if (workOrder == null) {
                    return AjaxResult.api(1, "产品关联工单不存在或删除", null);
                }
                if (!WorkConstants.WORK_STATUS_STARTING.equals(workOrder.getWorkorderStatus())) {
                    return AjaxResult.api(1, "工单未进行或已经结束", null);
                }
                // 获取对应位置的扫码状态标识(0、未扫，1、已扫)
                String myScan = mesBatch.getSign().substring(orderNum - 1, orderNum);
                // 判断该工序是否已经扫过
                if (MesConstants.SCAN_SIGN_YES == Integer.parseInt(myScan)) {
                    return AjaxResult.api(1, "该工序已完成扫码", null);
                }
                // 一号位扫码
                if (orderNum == 1) {
                    // 设置标记需要扫码
                    map.put("tag", MesConstants.TAG_SCAN_YES);
                    return AjaxResult.api(0, "需要扫码", map);
                }
                // 非一号位,获取上一位置的扫码状态
                String upSign = mesBatch.getSign().substring(orderNum - 2, orderNum - 1);
                if (MesConstants.SCAN_SIGN_NO == Integer.parseInt(upSign) && CompanyConstants.LINE_ORDER_TAG_VALID.equals(line.getOrderTag())) {
                    return AjaxResult.api(1, "上段工序未完成", null);
                } else {
                    // 设置标记需要扫码
                    map.put("TAG_SCAN", MesConstants.TAG_SCAN_YES);
                    return AjaxResult.api(0, "需要扫码", map);
                }
            }

            /**
             * 第二次扫码录入对应工位关联信息
             */
            if (StringUtils.isNotEmpty(mesInfo.getScanInfo()) && StringUtils.isNotEmpty(onePnMain)) {
                // 查询产线信息
                ProductionLine line = lineMapper.selectProductionLineById(lineId);
                if (line == null) {
                    return AjaxResult.api(1, "产线不存在或被删除", null);
                }
                // 查询建档信息
                MesBatch mesBatch = mesBatchMapper.selectMesBatchByMainInfo(line.getCompanyId(), onePnMain);
                if (mesBatch == null) {
                    return AjaxResult.api(1, "未找到建档记录", null);
                }
                if (mesBatch.getLineId() != lineId) {
                    return AjaxResult.api(1, "请扫码关联产线的产品信息", null);
                }
                // 查询扫码的工单是否已经完成
                DevWorkOrder workOrder = workOrderMapper.selectWorkOrderByWorkCode(mesBatch.getWorkCode());
                if (workOrder == null) {
                    return AjaxResult.api(1, "产品关联工单不存在或删除", null);
                }
                if (!WorkConstants.WORK_STATUS_STARTING.equals(workOrder.getWorkorderStatus())) {
                    return AjaxResult.api(1, "工单未进行或已经结束", null);
                }
                // 获取对应位置的扫码状态标识(0、未扫，1、已扫)
                String myScan = mesBatch.getSign().substring(orderNum - 1, orderNum);
                // 判断该工序是否已经扫过
                if (MesConstants.SCAN_SIGN_YES == Integer.parseInt(myScan)) {
                    return AjaxResult.api(1, "该工序已完成扫码", null);
                }
                // 非一号位
                if (orderNum > 1) {
                    String upSign = mesBatch.getSign().substring(orderNum - 2, orderNum - 1);
                    if (MesConstants.SCAN_SIGN_NO == Integer.parseInt(upSign) && CompanyConstants.LINE_ORDER_TAG_VALID.equals(line.getOrderTag())) {
                        return AjaxResult.api(1, "上段工序未完成", null);
                    }
                }
                // 修改建档扫码记录
                String sign = mesBatch.getSign();
                StringBuilder sb = new StringBuilder(sign);
                sign = sb.replace(orderNum - 1, orderNum, MesConstants.SCAN_SIGN_YES.toString()).toString();
                mesBatch.setSign(sign);
                mesBatchMapper.updateMesBatch(mesBatch);

                // 新增扫码记录
                MesBatchDetail mesBatchDetail = new MesBatchDetail();
                mesBatchDetail.setbId(mesBatch.getId());
                mesBatchDetail.setcTime(new Date());
                mesBatchDetail.setOrderNum(orderNum);
                mesBatchDetail.setPnMainInfo(mesBatch.getPnMainInfo());
                mesBatchDetail.setPnCode(mesBatch.getPnCode());
                mesBatchDetail.setWorkCode(mesBatch.getWorkCode());
                mesBatchDetail.setSmInfo(mesInfo.getScanInfo());
                mesBatchDetailMapper.insertMesBatchDetail(mesBatchDetail);
                // 标记清除数据
                map.put("TAG_CLEAN", MesConstants.TAG_CLEAN_YES);
                return AjaxResult.api(0, "扫码成功", map);

            }
            return AjaxResult.api(1, "扫码失败", null);
        } catch (Exception e) {
            LOGGER.error("APP扫码交互接口出现异常：" + e.getMessage());
            return AjaxResult.api(500, "系统异常", null);
        }
    }
}

