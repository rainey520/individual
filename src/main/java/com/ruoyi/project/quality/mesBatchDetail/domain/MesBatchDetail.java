package com.ruoyi.project.quality.mesBatchDetail.domain;

import com.ruoyi.framework.web.domain.BaseEntity;

import java.util.Date;

/**
 * MES个体扫码记录表 tab_mes_batch_detail
 * 
 * @author sj
 * @date 2019-11-28
 */
public class MesBatchDetail extends BaseEntity
{
	private static final long serialVersionUID = 1L;
	
	/** 主键，自增长 */
	private Integer id;
	/** 建档追溯主id */
	private Integer bId;
	/** 工单号 */
	private String workCode;
	/** 产品编码信息 */
	private String pnCode;
	/** 产品建档信息 */
	private String pnMainInfo;
	/** 扫码排序信息(1-24) */
	private Integer orderNum;
	/** 扫码信息 */
	private String smInfo;
	/** 扫码时间 */
	private Date cTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getbId() {
		return bId;
	}

	public void setbId(Integer bId) {
		this.bId = bId;
	}

	public String getWorkCode() {
		return workCode;
	}

	public void setWorkCode(String workCode) {
		this.workCode = workCode;
	}

	public String getPnCode() {
		return pnCode;
	}

	public void setPnCode(String pnCode) {
		this.pnCode = pnCode;
	}

	public String getPnMainInfo() {
		return pnMainInfo;
	}

	public void setPnMainInfo(String pnMainInfo) {
		this.pnMainInfo = pnMainInfo;
	}

	public Integer getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	public String getSmInfo() {
		return smInfo;
	}

	public void setSmInfo(String smInfo) {
		this.smInfo = smInfo;
	}

	public Date getcTime() {
		return cTime;
	}

	public void setcTime(Date cTime) {
		this.cTime = cTime;
	}

	@Override
	public String toString() {
		return "MesBatchDetail{" +
				"id=" + id +
				", bId=" + bId +
				", workCode='" + workCode + '\'' +
				", pnCode='" + pnCode + '\'' +
				", pnMainInfo='" + pnMainInfo + '\'' +
				", orderNum=" + orderNum +
				", smInfo='" + smInfo + '\'' +
				", cTime=" + cTime +
				'}';
	}
}
