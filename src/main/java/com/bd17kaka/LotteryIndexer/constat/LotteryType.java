/**
 * 
 */
package com.bd17kaka.LotteryIndexer.constat;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bd17kaka
 * 彩票类型
 */
public enum LotteryType {

	
	SSH(0, "双色球") {
		
		@Override
		boolean isValidNum(int num) {
			return (num <=33) && (num >=1);
		}
	}, 
	DLT(1, "大乐透") {

		@Override
		boolean isValidNum(int num) {
			return false;
		}

	};
	
	private int type;
	private String des;
	
	private LotteryType(int type, String des) {
		
		this.type = type;
		this.des = des;
		
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	
	/**
	 * get object from key
	 */
	private static final Map<Integer, LotteryType> LotteryType_MAP;
	static {
		LotteryType_MAP = new HashMap<Integer, LotteryType>();
		for (LotteryType l : values()) {
			LotteryType_MAP.put(l.getType(), l);
		}
	}
	public static LotteryType getLotteryType(int type) {
		return LotteryType_MAP.get(type);
	}
	
	abstract boolean isValidNum(int num);
}
