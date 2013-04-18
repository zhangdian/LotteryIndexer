package com.bd17kaka.LotteryIndexer.po;

/**
 * 双色球新出现组合对象
 */
public class SSHNewCombination implements java.io.Serializable {
	private static final long serialVersionUID = -1744081377872974319L;
	/**
	 * 开奖期数
	 */
	private String id;
	/**
	 * 新组合长度
	 */
	private int length;
	/**
	 * 新组合
	 */
	private String combination;
	/**
	 * 出现次数
	 */
	private int num;

	public SSHNewCombination() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SSHNewCombination(String id, int length, String combination, int num) {
		super();
		this.id = id;
		this.length = length;
		this.combination = combination;
		this.num = num;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getCombination() {
		return combination;
	}

	public void setCombination(String combination) {
		this.combination = combination;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	@Override
	public String toString() {
		return "SSHNewCombination [id=" + id + ", length=" + length
				+ ", combination=" + combination + "]";
	}
}
