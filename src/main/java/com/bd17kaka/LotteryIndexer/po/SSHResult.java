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
	 * 记录类型，标记新旧结果
	 * 对于旧结果，字段为0，不记录新组合到ssh_new_combination中
	 * 对于新结果，字段为1，记录新组合到ssh_new_combination中
	 */
	private int type;
	
	public SSHResult(String id, Date time, String result, int type) {
		super();
		this.id = id;
		this.time = time;
		this.result = result;
		this.type = type;
	}

	public SSHResult() {
		super();
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

	@Override
	public String toString() {
		return "SSHResult [id=" + id + ", time=" + time + ", result=" + result
				+ ", type=" + type + "]";
	}
}
