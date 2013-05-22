package com.bd17kaka.LotteryIndexer.po;

import java.util.Date;

/**
 * 双色球开奖结果信息
 */
public class SSHResult implements java.io.Serializable {
	private static final long serialVersionUID = -1744081377872974319L;
	/**
	 * 开奖期数
	 */
	private String id;
	/**
	 * 开奖日期
	 */
	private Date time;
	/**
	 * 开奖结果
	 */
	private String result;
	/**
	 * 记录类型，标记新旧结果 字段目前没有使用
	 */
	private int type;

	/**
	 * SSH结果中，球号的分布情况，v3
	 */
	private String distributionV3;

	/**
	 * SSH结果中，球号的分布情况，v11
	 */
	private String distributionV11;

	public SSHResult() {
		super();
	}

	public SSHResult(String id, Date time, String result, int type,
			String distributionV3, String distributionV11) {
		super();
		this.id = id;
		this.time = time;
		this.result = result;
		this.type = type;
		this.distributionV3 = distributionV3;
		this.distributionV11 = distributionV11;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDistributionV3() {
		return distributionV3;
	}

	public void setDistributionV3(String distributionV3) {
		this.distributionV3 = distributionV3;
	}

	public String getDistributionV11() {
		return distributionV11;
	}

	public void setDistributionV11(String distributionV11) {
		this.distributionV11 = distributionV11;
	}

	@Override
	public String toString() {
		return "SSHResult [id=" + id + ", time=" + time + ", result=" + result
				+ ", type=" + type + ", distributionV3=" + distributionV3
				+ ", distributionV11=" + distributionV11 + "]";
	}

}
