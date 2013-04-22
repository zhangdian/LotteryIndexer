package com.bd17kaka.LotteryIndexer.constat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author bd17kaka 双色球
 */
public enum SSH {

	RED(0, "RED") {
		
		private Log log = LogFactory.getLog("SSH:RED");
		
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

		@Override
		public List<String> getNumsFromInuput(String line) {

			String[] input = line.split(",");
			
			List<String> redList = new ArrayList<String>();
			for (int i = 0; i < SSH.RED.getTOTAL(); i++) {
				
				int num = 0;
				try {
					num = Integer.parseInt(input[i]);
				} catch (Exception e) {
					log.error("序列 " + line + " 中，" + input[i] + " 不是合法数字");
					continue;
				}
				
				if (!SSH.RED.isValidNum(num)) {
					log.error("序列 " + line + " 中，" + input[i] + " 不是有效数字，必须在"
							+ SSH.RED.getMIN() + "和" + SSH.RED.getMAX() + "之间");
					continue;
				}
				redList.add(input[i]);
			}
			
			return redList;
		}

		@Override
		public List<String> indexer(List<String> list) {
			
			Collections.sort(list);
			
			List<String> rs = new ArrayList<String>();
			String key = "";
			int size = list.size();
			for (int i = 0; i < size; i++) {
				
				key = list.get(i);
				rs.add(key);
				
				for (int j = i + 1; j < size; j++) {
					
					key += ":" + list.get(j);
					rs.add(key);
				}
				key = "";
			}
			
			return rs;
		}

		@Override
		public String getRedisKeyForCombinationNumByLengthAndFirstNum(int length) {
			return "ssh:red:combination:num:" + length;
		}

		@Override
		public String getRedisKeyForCombinationSizeByNum() {
			return "ssh:red:combination:size";
		}
		
	},
	BLUE(1, "BLUE") {

		private Log log = LogFactory.getLog("SSH:BLUE");
		
		@Override
		public boolean isValidNum(int num) {
			return (num <= MAX) && (num >= MIN);
		}
		public static final int MAX = 16;
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

		@Override
		public List<String> getNumsFromInuput(String line) {

			String[] input = line.split(",");
			
			List<String> blueList = new ArrayList<String>();
			for (int i = SSH.RED.getTOTAL(); i < SSH.TOTAL; i++) {
				
				int num = 0;
				try {
					num = Integer.parseInt(input[i]);
				} catch (Exception e) {
					log.error("序列 " + line + " 中，" + input[i] + " 不是合法数字");
					continue;
				}
				
				if (!SSH.BLUE.isValidNum(num)) {
					log.error("序列 " + line + " 中，" + input[i] + " 不是有效数字，必须在"
							+ SSH.BLUE.getMIN() + "和" + SSH.BLUE.getMAX() + "之间");
					continue;
				}
				blueList.add(input[i]);
			}
		
			return blueList;
		}

		/* (non-Javadoc)
		 * @see com.bd17kaka.LotteryIndexer.constat.SSH#indexer(java.util.List)
		 * SSH的篮球只有一个，所以不需要做过多的操作 
		 */
		@Override
		public List<String> indexer(List<String> list) {
			
			return list;
			
		}

		@Override
		public String getRedisKeyForCombinationNumByLengthAndFirstNum(int length) {
			return "ssh:blue:combination:num:" + length;
		}

		@Override
		public String getRedisKeyForCombinationSizeByNum() {
			return "ssh:blue:combination:size";
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

	/**
	 * 是否是合法数字
	 * @param num
	 * @return
	 */
	public abstract boolean isValidNum(int num);
	/**
	 * 获取最大值
	 * @return
	 */
	public abstract int getMAX();
	/**
	 * 获取最小值
	 * @return
	 */
	public abstract int getMIN();
	/**
	 * 获取某种球的总数
	 * @return
	 */
	public abstract int getTOTAL();
	/**
	 * 获取某种球在Redis中的key
	 * @return
	 */
	public abstract String getRedisKey();
	/**
	 * 指定长度，以某个球号开始的组合出现的次数  
	 * key -- ssh:red:combination:num:($length)
	 * field -- ($firstNum)
	 * value -- ($num)
	 * @return
	 */
	public abstract String getRedisKeyForCombinationNumByLengthAndFirstNum(int length);
	/**
	 * 指定出现次数的组合个数
	 * 在马尔科夫二元模型中使用，只存储二元组合
	 * key -- ssh:red:combination:size
	 * field -- ($num)
	 * value -- size
	 * @param num 组合出现的个数
	 * @return
	 */
	public abstract String getRedisKeyForCombinationSizeByNum();
	/**
	 * 从输入中获取所有球号
	 * @param line
	 * @return
	 */
	public abstract List<String> getNumsFromInuput(String line);
	/**
	 * 对输入的球号进行索引
	 * @param list
	 * @return
	 */
	public abstract List<String> indexer(List<String> list);
}
