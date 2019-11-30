package com.ruoyi.project.app.domain;

import java.io.Serializable;

/**
 * mes 相关数据
 * @Author: Rainey
 * @Date: 2019/11/29 15:20
 * @Version: 1.0
 **/
public class MesInfo implements Serializable {
    private static final long serialVersionUID = 5342381678889075820L;
    /** 产线id */
    private int lineId;
    /** 产线名称 */
    private String lineName;
    /** 扫码排序 */
    private int orderNum;
    /** 扫码建档信息 */
    private String mainInfo;
    /** 扫码装配信息 */
    private String scanInfo;

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public String getMainInfo() {
        return mainInfo;
    }

    public void setMainInfo(String mainInfo) {
        this.mainInfo = mainInfo;
    }

    public String getScanInfo() {
        return scanInfo;
    }

    public void setScanInfo(String scanInfo) {
        this.scanInfo = scanInfo;
    }

    @Override
    public String toString() {
        return "MesInfo{" +
                "lineId=" + lineId +
                ", lineName='" + lineName + '\'' +
                ", orderNum=" + orderNum +
                ", mainInfo='" + mainInfo + '\'' +
                ", scanInfo='" + scanInfo + '\'' +
                '}';
    }
}
