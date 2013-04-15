package com.bd17kaka.LotteryIndexer.constat;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bd17kaka 双色球
 */
public enum SSH {

	RED(0, "RED") {

		@Override
		public boolean isValidNum(int num) {
			return (num <= MAX) && (num >= MIN);
		}
		public static final int MAX = 33;
		public static final int MIN = 1;
		public static final int TOTAL = 6;
		
		@Override
		public int getMAX() {
			return MAX;
		}
		
		@Override
		public int getMIN() {
			return MIN;
		}

		@Override
		public int getTOTAL() {
			return TOTAL;
		}

		@Override
		public String getRedisKey() {
			return "ssh:red";
		}
		
	},
	BLUE(1, "BLUE") {

		@Override
		public boolean isValidNum(int num) {
			return (num <= MAX) && (num >= MIN);
		}
		public static final int MAX = 15;
		public static final int MIN = 1;
		public static final int TOTAL = 1;
		
		@Override
		public int getMAX() {
			return MAX;
		}
		
		@Override
		public int getMIN() {
			return MIN;
		}
		
		@Override
		public int getTOTAL() {
			return TOTAL;
		}

		@Override
		public String getRedisKey() {
			return "ssh:blue";
		}
		
	};

	public static final int TOTAL = RED.getTOTAL() + BLUE.getTOTAL();
	
	private int type;
	private String des;
	

	private SSH(int type, String des) {

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
	private static final Map<Integer, SSH> SSH_MAP;
	static {
		SSH_MAP = new HashMap<Integer, SSH>();
		for (SSH l : values()) {
			SSH_MAP.put(l.getType(), l);
		}
	}
	public static SSH getSSH(int type) {
		return SSH_MAP.get(type);
	}

	public abstract boolean isValidNum(int num);
	public abstract int getMAX();
	public abstract int getMIN();
	public abstract int getTOTAL();
	public abstract String getRedisKey();
}
