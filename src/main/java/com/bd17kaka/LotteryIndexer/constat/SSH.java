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
					return null;
				}
				
				if (!SSH.RED.isValidNum(num)) {
					log.error("序列 " + line + " 中，" + input[i] + " 不是有效数字，必须在"
							+ SSH.RED.getMIN() + "和" + SSH.RED.getMAX() + "之间");
					return null;
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
	
	/**
	 * @author bd17kaka
	 * 红球的分布，共28种分布
	 * 006的意思是：
	 * 		01-11有0个球
	 * 		12-22有0个球
	 * 		23-33有6个球
	 */
	public enum RedDistributedV3 {
		_006(6, "006"), 
		_015(15, "015"),
		_024(24, "024"),
		_033(33, "033"),
		_042(42, "042"),
		_051(51, "051"),
		_060(60, "060"),
		_105(105, "105"),
		_114(114, "114"),
		_123(123, "123"),
		_132(132, "132"),
		_141(141, "141"),
		_150(150, "150"),
		_204(204, "204"),
		_213(213, "213"),
		_222(222, "222"),
		_231(231, "231"),
		_240(240, "240"),
		_303(303, "303"),
		_312(312, "312"),
		_321(321, "321"),
		_330(330, "330"),
		_402(402, "402"),
		_411(411, "411"),
		_420(420, "420"),
		_501(501, "501"),
		_510(510, "510"),
		_600(600, "600");
		
		private int type;
		private String des;

		private RedDistributedV3(int type, String des) {

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

		public static Map<Integer, RedDistributedV3> getReddistributedMap() {
			return RedDistributed_MAP;
		}

		/**
		 * get object from key
		 */
		private static final Map<Integer, RedDistributedV3> RedDistributed_MAP;
		static {
			RedDistributed_MAP = new HashMap<Integer, RedDistributedV3>();
			for (RedDistributedV3 l : values()) {
				RedDistributed_MAP.put(l.getType(), l);
			}
		}
		public static RedDistributedV3 getRedDistributed(int type) {
			return RedDistributed_MAP.get(type);
		} 
		
		/**
		 * 获取开奖结果的球号分布
		 * @param input 经过SSH.getNumsFromInuput()处理之后的所有球号列表
		 * @return
		 */
		public static RedDistributedV3 getRedBallDistributed(List<String> input) {
			
			if (null == input) {
				return null;
			}
			
		
			/**
			 * distributedList[0]: Left个数
			 * distributedList[1]: Middle个数
			 * distributedList[2]: Right个数
			 */
			Integer[] distributedList = new Integer[SingleRedDistributedV3.getTOTAL()];
			for (int i = 0; i < SingleRedDistributedV3.getTOTAL(); i++) {
				distributedList[i] = new Integer(0);
			}
			
			for (String item : input) {
				
				SingleRedDistributedV3 rs = SingleRedDistributedV3.testRedDistributed(item);
				if (rs == null) {
					return null;
				}
				
				int curVal = distributedList[rs.getType()];
				distributedList[rs.getType()] = curVal + 1;
			
			}
			
			/**
			 * Left个数*100 + Middle个数*10 + Right个数 * 1
			 */
			int type = distributedList[0] * 100 + distributedList[1] * 10 + distributedList[2] * 1;
			return RedDistributedV3.getRedDistributed(type);
		}
	}
	
	/**
	 * @author bd17kaka
	 * 单个红球的分布
	 * 
	 * 01-11是left
	 * 12-22是middle
	 * 23-33是right
	 */
	public enum SingleRedDistributedV3 {
		
		LEFT(0, "LEFT") {
			@Override
			public int getMAX() {
				return 11;
			}

			@Override
			public int getMIN() {
				return 1;
			}
		}, 
		MIDDLE(1, "MIDDLE") {
			@Override
			public int getMAX() {
				return 22;
			}

			@Override
			public int getMIN() {
				return 12;
			}
		},
		RIGHT(2, "RIGHT") {
			@Override
			public int getMAX() {
				return 33;
			}

			@Override
			public int getMIN() {
				return 23;
			}
		};
	
		public static final int TOTAL = 3;
		
		public static int getTOTAL() {
			return TOTAL;
		}
		
		private int type;
		private String des;

		private SingleRedDistributedV3(int type, String des) {

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
		private static final Map<Integer, SingleRedDistributedV3> SingleRedDistributed_MAP;
		static {
			SingleRedDistributed_MAP = new HashMap<Integer, SingleRedDistributedV3>();
			for (SingleRedDistributedV3 l : values()) {
				SingleRedDistributed_MAP.put(l.getType(), l);
			}
		}
		public static SingleRedDistributedV3 getSingleRedDistributed(int type) {
			return SingleRedDistributed_MAP.get(type);
		} 
		
		/**
		 * 判断一个球属于哪种分布
		 * @param ball
		 * @return
		 */
		public static SingleRedDistributedV3 testRedDistributed (String ball) {

			int type = 0;
			try {
				type = Integer.parseInt(ball);
			} catch (Exception e) {
				return null;
			}
			
			return getSingleRedDistributed((type - 1) / 11);
			
		}
		
		/**
		 * 返回某区间的最大值
		 * @return
		 */
		public abstract int getMAX();
		/**
		 * 返回某区间的最小值
		 * @return
		 */
		public abstract int getMIN();
	}
	
	
	/**
	 * @author bd17kaka
	 * 单个红球的分布
	 * 
	 * 01-11是left
	 * 12-22是middle
	 * 23-33是right
	 */
	public enum SingleRedDistributedV11 {
		
		ONE(1, "ONE") {
			@Override
			public int getMAX() {
				return 3;
			}

			@Override
			public int getMIN() {
				return 1;
			}
		}, 
		TWO(2, "TWO") {
			@Override
			public int getMAX() {
				return 6;
			}

			@Override
			public int getMIN() {
				return 4;
			}
		},
		THREE(3, "THREE") {
			@Override
			public int getMAX() {
				return 9;
			}

			@Override
			public int getMIN() {
				return 7;
			}
		},
		FOUR(4, "FOUR") {
			@Override
			public int getMAX() {
				return 12;
			}

			@Override
			public int getMIN() {
				return 10;
			}
		},
		FIVE(5, "FIVE") {
			@Override
			public int getMAX() {
				return 15;
			}

			@Override
			public int getMIN() {
				return 13;
			}
		},
		SIX(6, "SIX") {
			@Override
			public int getMAX() {
				return 18;
			}

			@Override
			public int getMIN() {
				return 16;
			}
		},
		SEVEN(7, "SEVEN") {
			@Override
			public int getMAX() {
				return 21;
			}

			@Override
			public int getMIN() {
				return 19;
			}
		},
		EIGHT(8, "EIGHT") {
			@Override
			public int getMAX() {
				return 24;
			}

			@Override
			public int getMIN() {
				return 22;
			}
		},
		NINE(9, "NINE") {
			@Override
			public int getMAX() {
				return 27;
			}

			@Override
			public int getMIN() {
				return 25;
			}
		},
		TEN(10, "TEN") {
			@Override
			public int getMAX() {
				return 30;
			}

			@Override
			public int getMIN() {
				return 28;
			}
		},
		ELEVEN(11, "ELEVEN") {
			@Override
			public int getMAX() {
				return 33;
			}

			@Override
			public int getMIN() {
				return 31;
			}
		};

		public static final int TOTAL = 11;
		
		public static int getTOTAL() {
			return TOTAL;
		}
		
		private int type;
		private String des;

		private SingleRedDistributedV11(int type, String des) {

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
		private static final Map<Integer, SingleRedDistributedV11> SingleRedDistributed_MAP;
		static {
			SingleRedDistributed_MAP = new HashMap<Integer, SingleRedDistributedV11>();
			for (SingleRedDistributedV11 l : values()) {
				SingleRedDistributed_MAP.put(l.getType(), l);
			}
		}
		public static SingleRedDistributedV11 getSingleRedDistributed(int type) {
			return SingleRedDistributed_MAP.get(type);
		} 
		
		/**
		 * 判断一个球属于哪种分布
		 * @param ball
		 * @return
		 */
		public static SingleRedDistributedV11 testRedDistributed (String ball) {

			int type = 0;
			try {
				type = Integer.parseInt(ball);
			} catch (Exception e) {
				return null;
			}
			
			return getSingleRedDistributed((type - 1) / 3 + 1);
			
		}
		
		/**
		 * 返回某区间的最大值
		 * @return
		 */
		public abstract int getMAX();
		/**
		 * 返回某区间的最小值
		 * @return
		 */
		public abstract int getMIN();
		
		
		/**
		 * 获取11个区间，6个球，所有的排列方式
		 * 每个组合从的每个数字用空格分隔，如：1 1 1 1 1 1 0 0 0 0 0
		 * @return
		 */
		public static List<String> listDistribute() {
			
			List<String> list = new ArrayList<String>();
			listDistributeMain(11, 6, "", list);
			return list;
		}
		public static int listDistributeMain (int holes, int balls, String curDistribute, List<String> listDistribute) {
			
			if (holes == 1) {
				if (balls <= 3 && balls >= 0) {
					curDistribute += balls;
					listDistribute.add(curDistribute);
					return 1;
				}
				else {
					return 0;
				}
			}
			if (balls == 0) {
				for (int i = 0; i < holes; i++) {
					curDistribute += "0 ";
				}
				listDistribute.add(curDistribute);
				return 1;
				
			} else if (balls < 0) {
			
				return 0;
			}
			
			int curHoles = holes - 1;
			return listDistributeMain(curHoles, balls, curDistribute+"0 ", listDistribute) 
					+ listDistributeMain(curHoles, balls - 1, curDistribute+"1 ", listDistribute) 
					+ listDistributeMain(curHoles, balls - 2, curDistribute+"2 ", listDistribute) 
					+ listDistributeMain(curHoles, balls - 3, curDistribute+"3 ", listDistribute);  
		}
	}
	
	/**
	 * @author bd17kaka
	 * 算法种类
	 */
	public enum SSHRedAlgorithm{
		
		SIMPLE_SPAN2(0, "simple_span2"),
		SIMPLE_SPAN3(1, "simple_span2"),
		SIMPLE_SPAN3V2(2, "simple_span3v2"),
		SIMPLE_SPAN3V3(3, "simple_span3v3"),
		SIMPLE_SPAN3V5(4, "simple_span3v5"),
		SIMPLE_SPAN3V6(5, "simple_span3v6");
		
		private int type;
		private String des;

		private SSHRedAlgorithm(int type, String des) {

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

		public static Map<Integer, SSHRedAlgorithm> getReddistributedMap() {
			return SSHRedAlgorithmMAP;
		}

		/**
		 * get object from key
		 */
		private static final Map<Integer, SSHRedAlgorithm> SSHRedAlgorithmMAP;
		static {
			SSHRedAlgorithmMAP = new HashMap<Integer, SSHRedAlgorithm>();
			for (SSHRedAlgorithm l : values()) {
				SSHRedAlgorithmMAP.put(l.getType(), l);
			}
		}
		public static SSHRedAlgorithm getSSHRedAlgorithm(int type) {
			return SSHRedAlgorithmMAP.get(type);
		} 
		
		/**
		 * 只供SimpleSpan3V5和SimpleSpan3V5使用
		 * 获取使用算法SimpleSpan3V5时，每个组合的TopN记录的key
		 * @param distribution
		 * @return
		 */
		public static String getRedisKeyOfTopNCombinationByDistribution(SSHRedAlgorithm algorithm, int distribution) {
			
			return "topn_combination:" + algorithm.getDes() + ":" + String.format("%03d", distribution);
		}
		/**
		 * 只供SimpleSpan3V5和SimpleSpan3V5使用
		 * 获取使用算法SimpleSpan3V5时，每个组合的TopN记录的key
		 * @param distribution
		 * @return
		 */
		public static String getRedisKeyOfTopNCombinationByDistribution(SSHRedAlgorithm algorithm, String distribution) {
			
			return "topn_combination:" + algorithm.getDes() + ":" + distribution;
		}
		
		
		/**
		 * 获取全局的TopN组合的key
		 * @param distribution
		 * @return
		 */
		public static String getRedisKeyOfTotalTopNCombination(SSHRedAlgorithm algorithm) {
			
			return "topn_combination:" + algorithm.getDes();
			
		}
		
		/**
		 * 获取全局TopN组合的最大数量
		 * @return
		 */
		public static int getMaxTotal() {
			return 50;
		}
	}
	
	
	public static void main(String[] args) {
		String input = "02,04,11,15,23,33";
		List<String> list = SSH.RED.getNumsFromInuput(input);
		for (String string : list) {
			System.out.println(string);
		}
		
		System.out.println(RedDistributedV3.getRedBallDistributed(list));
	}
}
