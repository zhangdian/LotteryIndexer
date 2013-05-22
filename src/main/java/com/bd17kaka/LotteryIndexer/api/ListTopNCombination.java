package com.bd17kaka.LotteryIndexer.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bd17kaka.LotteryIndexer.constat.SSH;
import com.bd17kaka.LotteryIndexer.constat.SSH.RedDistributedV3;
import com.bd17kaka.LotteryIndexer.constat.SSH.SSHRedAlgorithm;
import com.bd17kaka.LotteryIndexer.constat.SSH.SingleRedDistributedV11;
import com.bd17kaka.LotteryIndexer.dao.RedisDao;

/**
 * @author bd17kaka
 * 根据现有的数据，计算出TopN概率的组合，将计算的结果存储在Redis中
 */
public class ListTopNCombination {

	private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/spring.xml");
	private static RedisDao redisDao = (RedisDao) context.getBean("redisDao");
	private static final Log log = LogFactory.getLog(ListTopNCombination.class);
	
	/**
	 * @param args
	 * 		agrs[0]: 算法类型，比如simple_span2等
	 * 		agrs[1]: n的大小，保留排名前n的记录
	 * 		args[2]: 供SimpleSpan3V5算法使用，保留每个Distribute的前多少个记录
	 * 
	 * simple_span2:
	 * 	分六维进行计算
	 * 	第一维计算单个球出现的概率
	 * 	第二维以第一维的结果为x维，以所有红球为y维，计算所有二维组的概率，x维的概率从第一维的结果集中获取
	 * 	同理，第3-5维都以上一维的结果为x维，以所有红球为y维，计算当前维度球组合的概率，x维的概率从上一维的结果集中获取
	 * 	第6维和2-5维一样，只是要注意如果组合出现过，那么其概率就直接置为0
	 * 
	 * 	在计算过程中要注意以下几点：
	 * 		球的排列顺序要严格递增的顺序
	 * 		如果顺序不对，那么该组合的概率为0
	 * 		如果在一个组合中出现有相同的球号，概率为0
	 * 考虑长度范围是2-3的组合
	 * 
	 * 		key: 	"topn_combination:simple_span2"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 * 
	 * ************************************************************************
	 * 
	 * simple_span3:
	 *	分六维进行计算
	 * 	第一维计算单个球出现的概率
	 * 	第二维以第一维的结果为x维，以所有红球为y维，计算所有二维组的概率，x维的概率从第一维的结果集中获取
	 * 	同理，第3-5维都以上一维的结果为x维，以所有红球为y维，计算当前维度球组合的概率，x维的概率从上一维的结果集中获取
	 * 	第6维和2-5维一样，只是要注意如果组合出现过，那么其概率就直接置为0
	 * 
	 * 	在计算过程中要注意以下几点：
	 * 		球的排列顺序要严格递增的顺序
	 * 		如果顺序不对，那么该组合的概率为0
	 * 		如果在一个组合中出现有相同的球号，概率为0
	 * 
	 * 	考虑长度范围是2-3的组合
	 * 
	 * 		key: 	"topn_combination:simple_span3"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 * 
	 * ************************************************************************
	 * 
	 * simple_span3v2:
	 * 
	 *	首先遍历出所有组合
	 * 	计算所有组合的概率，将概率值放大10000倍，也就是万级的概率
	 * 
	 * 	只考虑长度为3的组合
	 * 
	 * 	计算公式：
	 * 		分子 - (#ABC * #BCD * #CDE * #DEF * #EF) * 10000
	 * 		分母 - (#B * #C * #D * #E) 
	 * 
	 * 		key: 	"topn_combination:simple_span3v2"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 * 
	 * ************************************************************************
	 * 
	 * simple_span3v3:
	 * 
	 *	首先遍历出所有组合
	 * 	计算所有组合的概率，将概率值放大10000倍，也就是万级的概率
	 * 
	 * 	只考虑长度为3的组合
	 * 
	 * 	计算公式：
	 * 		分子 - (#ABC * #BCD * #CDE * #DEF)^2 * 10000
	 * 		分母 - (#A * #B * #C * #D) * (#BC * #CD * #DE) 
	 * 
	 * 		key: 	"topn_combination:simple_span3v3"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 * 
	 * ************************************************************************
	 * 
	 * simple_span3v5:
	 * 		key: 	"topn_combination:simple_span3v5:${distribution}"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 * 
	 * ************************************************************************
	 * 
	 * simple_span3v6:
	 * 	和simple_span3v5相比，单个组合计算概率的方式还是一样，
	 * 	但是求topN的组合的算法改变了，
	 * 	将33个球分成11个部分
	 * 		key: 	"topn_combination:simple_span3v5:${distribution}"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 */
	public static void main(String[] args) {

		int DEFAULT_TOTAL = 10;
		
		// 获取输入参数
		String type = "";
		int total = DEFAULT_TOTAL;
		int globalTotal = 0;
		
		if (args.length == 0) {
			log.error("请输入正确的参数：[type, total, [globalTotal]]");
			return;
		} 
		type = args[0];
		try {
			total = Integer.parseInt(args[1]);
			if (total < SSHRedAlgorithm.getMaxTotal()) {
				total = SSHRedAlgorithm.getMaxTotal();
			}
		} catch (Exception e) {
			total = DEFAULT_TOTAL;
		}
		try {
			globalTotal = Integer.parseInt(args[2]);
			if (globalTotal < SSHRedAlgorithm.getMaxTotal()) {
				globalTotal = SSHRedAlgorithm.getMaxTotal();
			}
		} catch (Exception e) {
			globalTotal = SSHRedAlgorithm.getMaxTotal();
		}
		
		if ("simple_span3v5".equals(type)) {

			/**
			 * 获取所有分布情况（共28种），然后获取每种分布的TopN的组合
			 * 将这些组合存储到Redis中
			 * key: 	"topn_combination:simple_span3v5:${distribution}"
			 * field: 	${combination}
			 * value:	${probabolity}
			 */
			
			log.info("*******************simple_span3v5*********************");
			
			Map<Integer, RedDistributedV3> mapRedDistributed = SSH.RedDistributedV3.getReddistributedMap();
			if (null == mapRedDistributed) {
				return;
			}
			
			// 保存长度为1-3的所有组合的出现个数
			Map<String, Integer> combinationMap = new HashMap<String, Integer>();
			int q = 1, w = 1, r = 1;
			int max = SSH.RED.getMAX();
			for (; q <= max; q++) {

				String field = "";
				int value = 0;

				field =  String.format("%02d", q); 
				value = redisDao.hget(SSH.RED.getRedisKey(), field);
				combinationMap.put(field, value);
				
				for (w = q + 1; w <=max; w++) {

					field =  String.format("%02d", q) 
							+ ":" + String.format("%02d", w);
					value = redisDao.hget(SSH.RED.getRedisKey(), field);
					combinationMap.put(field, value);

					for (r = w + 1; r <=max; r++) {
						
						field =  String.format("%02d", q) 
								+ ":" + String.format("%02d", w) 
								+ ":" + String.format("%02d", r);
						value = redisDao.hget(SSH.RED.getRedisKey(), field);
						combinationMap.put(field, value);
					}
					
				}
				
			}
			
			Map<String, Double> rsTotalMap	= new HashMap<String, Double>();
			Set<Integer> keys = mapRedDistributed.keySet();
			for (Integer key : keys) {

				log.info("Key: " + key);
				
				// 结果集
				Map<String, Double> rsMap = new HashMap<String, Double>();
				
				// 找到分解线
				Integer[] ballsStart = new Integer[6]; // 值为0代表将start赋值为上一维的start+1
				Integer[] ballsEnd = new Integer[6];
				int leftNum 	= 0,
					middleNum 	= 0,
					rightNum	= 0;
				
				leftNum = key / 100;
				for (int i = 0; i < leftNum; i++) {
					if (i == 0) {
						ballsStart[i] = SSH.SingleRedDistributedV3.LEFT.getMIN();
					} else {
						ballsStart[i] = 0;
					}
					ballsEnd[i] = SSH.SingleRedDistributedV3.LEFT.getMAX();
				}
				
				middleNum = (key % 100) / 10;
				for (int i = 0; i < middleNum; i++) {
					if (i == 0) {
						ballsStart[leftNum + i] = SSH.SingleRedDistributedV3.MIDDLE.getMIN();
					} else {
						ballsStart[leftNum + i] = 0;
					}
					ballsEnd[leftNum + i] = SSH.SingleRedDistributedV3.MIDDLE.getMAX();
				}
				
				rightNum = ((key % 100) % 10) / 1;
				for (int i = 0; i < rightNum; i++) {
					if (i == 0) {
						ballsStart[leftNum + middleNum + i] = SSH.SingleRedDistributedV3.RIGHT.getMIN();
					} else {
						ballsStart[leftNum + middleNum + i] = 0;
					}
					ballsEnd[leftNum + middleNum + i] = SSH.SingleRedDistributedV3.RIGHT.getMAX();
				}

				// 应用SimpleSpan3V3算法, 下面代表六个球
				int a = ballsStart[0], 
					b = 1, 
					c = 1,
					d = 1,
					e = 1,
					f = 1;
				for (; a <= ballsEnd[0]; a++) {
					b = (ballsStart[1] == 0) ? (a + 1) : ballsStart[1];
					for (;b <= ballsEnd[1]; b++) {
						c = (ballsStart[2] == 0) ? (b + 1) : ballsStart[2];
						for (; c <= ballsEnd[2]; c++) {
							d = (ballsStart[3] == 0) ? (c + 1) : ballsStart[3];
							for (; d <= ballsEnd[3]; d++) {
								e = (ballsStart[4] == 0) ? (d + 1) : ballsStart[4];
								for (; e <= ballsEnd[4]; e++) {
									f = (ballsStart[5] == 0) ? (e + 1) : ballsStart[5];
									for (; f <= ballsEnd[5]; f++) {
										
										// 分子 分母
										long molecular = 10000; 
										long denominator = 1;
										String field = "";
										
										// 计算分子
										field =  String.format("%02d", a) 
												+ ":" + String.format("%02d", b) 
												+ ":" + String.format("%02d", c);
										molecular *= combinationMap.get(field);
										field =  String.format("%02d", b) 
												+ ":" + String.format("%02d", c) 
												+ ":" + String.format("%02d", d);
										molecular *= combinationMap.get(field);
										field =  String.format("%02d", c) 
												+ ":" + String.format("%02d", d) 
												+ ":" + String.format("%02d", e);
										molecular *= combinationMap.get(field);
										field =  String.format("%02d", d) 
												+ ":" + String.format("%02d", e) 
												+ ":" + String.format("%02d", f);
										molecular *= combinationMap.get(field);
										molecular *= molecular;
										
										// 计算分母
										field =  String.format("%02d", a);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", b);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", c);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", d);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", b) 
												+ ":" + String.format("%02d", c);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", c) 
												+ ":" + String.format("%02d", d);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", d) 
												+ ":" + String.format("%02d", e);
										denominator *= combinationMap.get(field);
										
										// 保存到结果集
										field = String.format("%02d", a) 
												+ ":" + String.format("%02d", b) 
												+ ":" + String.format("%02d", c) 
												+ ":" + String.format("%02d", d)
												+ ":" + String.format("%02d", e)
												+ ":" + String.format("%02d", f);
										double rs = 0.0;
										if (denominator != 0) {
											rs = (double)molecular / (double)denominator;
										}
										rsMap.put(field, rs);
										rsTotalMap.put(field, rs);
										log.debug("组合[" + field + "]: " + rs);
										
										// 如果结果集超过了size个，进行排序，剔除最后的一个
										// 分别计算每个分布以及全局
										if (rsMap.size() > total) {
											ArrayList<Entry<String, Double>> tmp = new ArrayList<Entry<String, Double>>(rsMap.entrySet());
											Collections.sort(tmp, new SSHRedProbabilityComparator());
											Entry<String, Double> which2Del = tmp.get(tmp.size() - 1);
											rsMap.remove(which2Del.getKey());
											log.debug("组合[" + which2Del.getKey() + "]被删除, 其概率为" + which2Del.getValue());
											
										}
										if (rsTotalMap.size() > globalTotal) {
											ArrayList<Entry<String, Double>> tmp = new ArrayList<Entry<String, Double>>(rsTotalMap.entrySet());
											Collections.sort(tmp, new SSHRedProbabilityComparator());
											Entry<String, Double> which2Del = tmp.get(tmp.size() - 1);
											rsTotalMap.remove(which2Del.getKey());
											log.debug("组合[" + which2Del.getKey() + "]被删除, 其概率为" + which2Del.getValue());
										}
									}
									
								}
								
							}
							
						}
						
					}
					
				}
				
				/**
				 *  此处已经得到了结果集，首先删除Redis中这个key，然后将结果集存储到Redis中，清空rsMap，进行下一轮
				 *	key: 	"topn_combination:simple_span3v5:${distribution}"
				 * 	field: 	${combination}
				 * 	value:	${probabolity}
				 */
				
				Set<String> rsKeys = rsMap.keySet();
				if (rsKeys == null) {
					return;
				}
				String redisKey = SSHRedAlgorithm.getRedisKeyOfTopNCombinationByDistribution(SSHRedAlgorithm.SIMPLE_SPAN3V5, key);
				redisDao.del(redisKey);
				for (String rsKey : rsKeys) {
					String redisField	= rsKey;
					String redisValue	= String.valueOf(rsMap.get(rsKey));
					redisDao.hset(redisKey, redisField, redisValue);
					log.info("\t<" + redisField + ", " + redisValue + ">");
				}
				rsMap.clear();
				
			}

			Set<String> rsKeys = rsTotalMap.keySet();
			if (rsKeys == null) {
				return;
			}
			String redisKey = SSHRedAlgorithm.getRedisKeyOfTotalTopNCombination(SSHRedAlgorithm.SIMPLE_SPAN3V5);
			redisDao.del(redisKey);
			for (String rsKey : rsKeys) {
				String redisField	= rsKey;
				String redisValue	= String.valueOf(rsTotalMap.get(rsKey));
				redisDao.hset(redisKey, redisField, redisValue);
				log.info("\t<" + redisField + ", " + redisValue + ">");
			}
			rsTotalMap.clear();
			
		}
		else if ("simple_span2".equals(type)) {
			
			/**
			 * 每个维度的所有球号的出现概率存在一个Map中，所有维度组成一个List，结构如下：
			 * 	List<Map<String, double>>
			 * 每单个球出现的个数存储在一个Map中：
			 * 	Map<String, Integer>
			 */
			List<Map<String, Double>> list = new ArrayList<Map<String, Double>>();
			Map<String, Integer> map = new HashMap<String, Integer>();

			// 计算每单个球出现的概率，同时算出总数
			int totalValue = 0;
			for (int j = 1; j <= SSH.RED.getMAX(); j++) {
				int curValue = 0;
				String field =  String.format("%02d", j);
				curValue = redisDao.hget(SSH.RED.getRedisKey(), field);
				map.put(field, curValue);
				totalValue += curValue;
			}
			
			for (int i = 0; i < SSH.RED.getTOTAL(); i++) {
				
				// 存储该维度每个组合出现的概率
				Map<String, Double> curMap = new HashMap<String, Double>();
				
				if (i == 0) {
				
					/**
					 *	第一维
					 *  算出每单个球的概率
					 */
					int curValue = 0;
					for (int j = 1; j <= SSH.RED.getMAX(); j++) {

						// 求出每个球的概率
						String field =  String.format("%02d", j);
						curValue = map.get(field);
						curMap.put(field, ((double)curValue / (double)totalValue));
						
					}
					
				} else {

					/**
					 *	遍历前一维的每个组合
					 *	取出组合中最后的那个球
					 *	和y维的每个球进行组合，算出概率
					 */
					Map<String, Double> preMap = list.get(i - 1);
					if (null == preMap) {
						continue;
					}
					Set<String> set = preMap.keySet();
					for (String item : set) {
						
						// 当前组合的概率
						double curPM = preMap.get(item);
						
						// 找到最后的那个球
						String[] tokens = item.split(":");
						if (tokens == null || tokens.length == 0) {
							continue;
						}
						String lastBall = tokens[tokens.length - 1];
						
						// 算出以这个球开始的每个组合的概率，和pre组合的概率组合，形成完整组合的概率
						totalValue = map.get(lastBall);
						int curValue = 0;
						for (int j = (1 + Integer.parseInt(lastBall)); j <= SSH.RED.getMAX(); j++) {

							// 求出每个组合的概率
							String key = lastBall + ":" + String.format("%02d", j);
							curValue = redisDao.hget(SSH.RED.getRedisKey(), key);
							String field = item + ":" + String.format("%02d", j); 
							curMap.put(field, curPM * ((double)(curValue) / (double)totalValue));
							
						}
						
					}
					
				}
				
				// debug info
				log.info("------------------------------------------------------");
				log.info("第" + (i + 1) + "维的数据:");
				Set<String> set = curMap.keySet();
				for (String item : set) {
					log.info("\t" + item + ":" + curMap.get(item)); 
				}
				list.add(curMap);
				
			}
			
			// 排序获取概率前total个组合
			String redisKey = SSHRedAlgorithm.getRedisKeyOfTotalTopNCombination(SSHRedAlgorithm.SIMPLE_SPAN2);
			if (list.size() != 0) {
				
				// 清空Redis中现有的数据
				redisDao.del(redisKey);
				
				Map<String, Double> preMap = list.get(SSH.RED.getTOTAL() - 1);
				ArrayList<Entry<String, Double>> tmp = new ArrayList<Entry<String, Double>>(preMap.entrySet());
				Collections.sort(tmp, new SSHRedProbabilityComparator());
				int n = 0;
				for(Entry<String, Double> e : tmp) {
					String field = e.getKey();
					String value = String.valueOf(e.getValue());
					redisDao.hset(redisKey, field, value);
					log.info(e.getKey() + "的概率是" + e.getValue());
		            if (n++ >= total) {
		            	break;
		            }
		        }  
			}
		}
		else if ("simple_span3".equals(type)) {
			
			/**
			 * 每个维度的所有球号的出现概率存在一个Map中，所有维度组成一个List，结构如下：
			 * 	List<Map<String, double>>
			 * 
			 * 每单个球或者组合的出现次数直接去Redis中query
			 * 将来比较一下存储在Redis中和内存中的性能差别
			 */
			List<Map<String, Double>> list = new ArrayList<Map<String, Double>>();
			
			
			for (int i = 0; i < SSH.RED.getTOTAL(); i++) {
				
				// 存储该维度每个组合出现的概率
				Map<String, Double> curMap = new HashMap<String, Double>();
				
				if (i == 0) {
					
					// 单个球出现总数
					int totalValue = 0;
					int curValue = 0;
					for (int j = 1; j <= SSH.RED.getMAX(); j++) {

						String field =  String.format("%02d", j);
						curValue = redisDao.hget(SSH.RED.getRedisKey(), field);
						totalValue += curValue;
					}

					// 单个球每个球的概率
					for (int j = 1; j <= SSH.RED.getMAX(); j++) {

						// 求出每个球的概率
						String field =  String.format("%02d", j);
						curValue = redisDao.hget(SSH.RED.getRedisKey(), field);
						curMap.put(field, ((double)curValue / (double)totalValue));
						
					}
					
				} else {

					/**
					 *	遍历前一维的每个组合
					 *	取出组合中最后的那个球, 和y维的每个球进行组合, 得出一个概率
					 *	如果原组合的长度>=2的话，再取出最后的两个球，和y维当前球再计算一个概率
					 *	两个概率相乘
					 */
					Map<String, Double> preMap = list.get(i - 1);
					if (null == preMap) {
						continue;
					}
					Set<String> set = preMap.keySet();
					for (String item : set) {
						
						// 当前组合的概率
						double curPM = preMap.get(item);
						
						// 找到最后的那个球,以及最后两个球的组合
						String[] tokens = item.split(":");
						int length = tokens.length;
						if (tokens == null || length == 0) {
							continue;
						}
						String lastBall = tokens[length - 1];
						String lastTwoBall = null;
						if (length >= 2) {
							lastTwoBall = tokens[length - 2] + ":" + tokens[length - 1];
						}
						
						// 算出以这个球开始的每个组合的概率，和pre组合的概率组合，形成完整组合的概率
						// 如果pre组合的长度>=2，那么还要计算pre组合的后两个球和当前球组合起来的概率
						int totalValue = redisDao.hget(SSH.RED.getRedisKey(), lastBall);
						int curValue = 0;
						for (int j = (1 + Integer.parseInt(lastBall)); j <= SSH.RED.getMAX(); j++) {

							// 求出每个组合的概率
							String field = lastBall + ":" + String.format("%02d", j);
							curValue = redisDao.hget(SSH.RED.getRedisKey(), field);
							curPM = curPM * ((double)(curValue) / (double)totalValue);
							if (lastTwoBall != null) {
								field = lastTwoBall + ":" + String.format("%02d", j);
								curValue = redisDao.hget(SSH.RED.getRedisKey(), field);
								int tmpTotalBall = redisDao.hget(SSH.RED.getRedisKey(), lastTwoBall);
								curPM = curPM * ((double)(curValue) / (double)tmpTotalBall);
							}
							
							field = item + ":" + String.format("%02d", j); 
							curMap.put(field, curPM);
							
						}
						
					}
					
				}

				// debug info
				log.info("------------------------------------------------------");
				log.info("第" + (i + 1) + "维的数据:");
				Set<String> set = curMap.keySet();
				for (String item : set) {
					log.info("\t" + item + ":" + curMap.get(item)); 
				}
				list.add(curMap);
				
			}
			
			// 排序获取概率最大的size个组合
			String redisKey = SSHRedAlgorithm.getRedisKeyOfTotalTopNCombination(SSHRedAlgorithm.SIMPLE_SPAN3);
			if (list.size() != 0) {
				
				// 清空Redis中现有的数据
				redisDao.del(redisKey);
				
				Map<String, Double> preMap = list.get(SSH.RED.getTOTAL() - 1);
				ArrayList<Entry<String, Double>> tmp = new ArrayList<Entry<String, Double>>(preMap.entrySet());
				Collections.sort(tmp, new SSHRedProbabilityComparator());
				int n = 0;
				for(Entry<String, Double> e : tmp) {  
					String field = e.getKey();
					String value = String.valueOf(e.getValue());
					redisDao.hset(redisKey, field, value);
					log.info(e.getKey() + "的概率是" + e.getValue());
		            if (n++ >= total) {
		            	break;
		            }
		        }  
			}
		}
		else if ("simple_span3V2".equals(type)) {
			
			// 结果集
			Map<String, Double> rsMap = new HashMap<String, Double>();
			
			// 保存长度为1-3的所有组合的出现个数
			Map<String, Integer> combinationMap = new HashMap<String, Integer>();
			int a = 1, b = 1, c = 1;
			int max = SSH.RED.getMAX();
			for (; a <=max; a++) {

				String field = "";
				int value = 0;

				field =  String.format("%02d", a); 
				value = redisDao.hget(SSH.RED.getRedisKey(), field);
				combinationMap.put(field, value);
				
				for (b = a + 1; b <=max; b++) {

					field =  String.format("%02d", a) 
							+ ":" + String.format("%02d", b);
					value = redisDao.hget(SSH.RED.getRedisKey(), field);
					combinationMap.put(field, value);

					for (c = b + 1; c <=max; c++) {
						
						field =  String.format("%02d", a) 
								+ ":" + String.format("%02d", b) 
								+ ":" + String.format("%02d", c);
						value = redisDao.hget(SSH.RED.getRedisKey(), field);
						combinationMap.put(field, value);
					}
					
				}
				
			}

			// 代表六个球号
			int i = 1, j = 1, k = 1, m = 1, n = 1, l = 1;
			for (; i <= max; i++) {
				
				for (j = i + 1; j <= max; j++) {
					
					for (k = j + 1; k <= max; k++) {
						
						for (m = k + 1; m <= max; m++) {
							
							for (n = m + 1; n <= max; n++) {
								
								for (l = n + 1; l <= max; l++) {
									
									// 分子 分母
									long molecular = 10000; 
									long denominator = 1;
									String field = "";
									
									// 计算分子
									field =  String.format("%02d", i) 
											+ ":" + String.format("%02d", j) 
											+ ":" + String.format("%02d", k);
									molecular *= combinationMap.get(field);
									field =  String.format("%02d", j) 
											+ ":" + String.format("%02d", k) 
											+ ":" + String.format("%02d", m);
									molecular *= combinationMap.get(field);
									field =  String.format("%02d", k) 
											+ ":" + String.format("%02d", m) 
											+ ":" + String.format("%02d", n);
									molecular *= combinationMap.get(field);
									field =  String.format("%02d", m) 
											+ ":" + String.format("%02d", n) 
											+ ":" + String.format("%02d", l);
									molecular *= combinationMap.get(field);
									field =  String.format("%02d", n) 
											+ ":" + String.format("%02d", l);
									molecular *= combinationMap.get(field);
									
									// 计算分母
									field =  String.format("%02d", j);
									denominator *= combinationMap.get(field);
									field =  String.format("%02d", k);
									denominator *= combinationMap.get(field);
									field =  String.format("%02d", m);
									denominator *= combinationMap.get(field);
									field =  String.format("%02d", n);
									denominator *= combinationMap.get(field);
									
									// 保存到结果集
									field = String.format("%02d", i) 
											+ ":" + String.format("%02d", j) 
											+ ":" + String.format("%02d", k) 
											+ ":" + String.format("%02d", m)
											+ ":" + String.format("%02d", n)
											+ ":" + String.format("%02d", l);
									double rs = (double)molecular / (double)denominator;
									rsMap.put(field, rs);
									log.info("组合[" + field + "]: " + rs);
									
									// 如果结果集超过了size个，进行排序，剔除最后的一个
									if (rsMap.size() > total) {
										
										ArrayList<Entry<String, Double>> tmp = new ArrayList<Entry<String, Double>>(rsMap.entrySet());
										Collections.sort(tmp, new SSHRedProbabilityComparator());
										Entry<String, Double> which2Del = tmp.get(tmp.size() - 1);
										rsMap.remove(which2Del.getKey());
										log.info("组合[" + which2Del.getKey() + "]被删除, 其概率为" + which2Del.getValue());
										
									}
								}
								
							}
							
						}
						
					}
					
				}
				
			}
			
			// 排序获取概率最大的total个组合
			Set<String> rsKeys = rsMap.keySet();
			if (rsKeys == null) {
				return;
			}
			String redisKey = SSHRedAlgorithm.getRedisKeyOfTotalTopNCombination(SSHRedAlgorithm.SIMPLE_SPAN3V2);
			redisDao.del(redisKey);
			for (String rsKey : rsKeys) {
				String redisField	= rsKey;
				String redisValue	= String.valueOf(rsMap.get(rsKey));
				redisDao.hset(redisKey, redisField, redisValue);
				log.info("\t<" + redisField + ", " + redisValue + ">");
			}
			rsMap.clear();
			
		}
		else if ("simple_span3V3".equals(type)) {
			
			// 结果集
			Map<String, Double> rsMap = new HashMap<String, Double>();
			
			// 保存长度为1-3的所有组合的出现个数
			Map<String, Integer> combinationMap = new HashMap<String, Integer>();
			int q = 1, w = 1, r = 1;
			int max = SSH.RED.getMAX();
			for (; q <=max; q++) {

				String field = "";
				int value = 0;

				field =  String.format("%02d", q); 
				value = redisDao.hget(SSH.RED.getRedisKey(), field);
				combinationMap.put(field, value);
				
				for (w = q + 1; w <=max; w++) {

					field =  String.format("%02d", q) 
							+ ":" + String.format("%02d", w);
					value = redisDao.hget(SSH.RED.getRedisKey(), field);
					combinationMap.put(field, value);

					for (r = w + 1; r <=max; r++) {
						
						field =  String.format("%02d", q) 
								+ ":" + String.format("%02d", w) 
								+ ":" + String.format("%02d", r);
						value = redisDao.hget(SSH.RED.getRedisKey(), field);
						combinationMap.put(field, value);
					}
					
				}
				
			}

			// 代表六个球号
			int a = 1, 
				b = 1, 
				c = 1, 
				d = 1, 
				e = 1, 
				f = 1;
			for (; a <= max; a++) {
				
				for (b = a + 1; b <= max; b++) {
					
					for (c = b + 1; c <= max; c++) {
						
						for (d = c + 1; d <= max; d++) {
							
							for (e = d + 1; e <= max; e++) {
								
								for (f = e + 1; f <= max; f++) {
									
									// 分子 分母
									long molecular = 10000; 
									long denominator = 1;
									String field = "";
									
									// 计算分子
									field =  String.format("%02d", a) 
											+ ":" + String.format("%02d", b) 
											+ ":" + String.format("%02d", c);
									molecular *= combinationMap.get(field);
									field =  String.format("%02d", b) 
											+ ":" + String.format("%02d", c) 
											+ ":" + String.format("%02d", d);
									molecular *= combinationMap.get(field);
									field =  String.format("%02d", c) 
											+ ":" + String.format("%02d", d) 
											+ ":" + String.format("%02d", e);
									molecular *= combinationMap.get(field);
									field =  String.format("%02d", d) 
											+ ":" + String.format("%02d", e) 
											+ ":" + String.format("%02d", f);
									molecular *= combinationMap.get(field);
									molecular *= molecular;
									
									// 计算分母
									field =  String.format("%02d", a);
									denominator *= combinationMap.get(field);
									field =  String.format("%02d", b);
									denominator *= combinationMap.get(field);
									field =  String.format("%02d", c);
									denominator *= combinationMap.get(field);
									field =  String.format("%02d", d);
									denominator *= combinationMap.get(field);
									field =  String.format("%02d", b) 
											+ ":" + String.format("%02d", c);
									denominator *= combinationMap.get(field);
									field =  String.format("%02d", c) 
											+ ":" + String.format("%02d", d);
									denominator *= combinationMap.get(field);
									field =  String.format("%02d", d) 
											+ ":" + String.format("%02d", e);
									denominator *= combinationMap.get(field);
									
									// 保存到结果集
									field = String.format("%02d", a) 
											+ ":" + String.format("%02d", b) 
											+ ":" + String.format("%02d", c) 
											+ ":" + String.format("%02d", d)
											+ ":" + String.format("%02d", e)
											+ ":" + String.format("%02d", f);
									double rs = 0.0;
									if (denominator != 0) {
										rs = (double)molecular / (double)denominator;
									}
									rsMap.put(field, rs);
									log.info("组合[" + field + "]: " + rs);
									
									// 如果结果集超过了total个，进行排序，剔除最后的一个
									if (rsMap.size() > total) {
										
										ArrayList<Entry<String, Double>> tmp = new ArrayList<Entry<String, Double>>(rsMap.entrySet());
										Collections.sort(tmp, new SSHRedProbabilityComparator());
										Entry<String, Double> which2Del = tmp.get(tmp.size() - 1);
										rsMap.remove(which2Del.getKey());
										log.info("组合[" + which2Del.getKey() + "]被删除, 其概率为" + which2Del.getValue());
										
									}
								}
								
							}
							
						}
						
					}
					
				}
				
			}
			
			// 排序获取概率最大的size个组合
			Set<String> rsKeys = rsMap.keySet();
			if (rsKeys == null) {
				return;
			}
			String redisKey = SSHRedAlgorithm.getRedisKeyOfTotalTopNCombination(SSHRedAlgorithm.SIMPLE_SPAN3V3);
			redisDao.del(redisKey);
			for (String rsKey : rsKeys) {
				String redisField	= rsKey;
				String redisValue	= String.valueOf(rsMap.get(rsKey));
				redisDao.hset(redisKey, redisField, redisValue);
				log.info("\t<" + redisField + ", " + redisValue + ">");
			}
			rsMap.clear();
			
		} else if ("simple_span3v6".equals(type)) {
			
			/**
			 * 获取所有分布情况，然后获取每种分布的TopN的组合
			 * 将这些组合存储到Redis中
			 * key: 	"topn_combination:simple_span3v6:${distribution}"
			 * field: 	${combination}
			 * value:	${probabolity}
			 */
			
			log.info("*******************simple_span3v6*********************");
			
			List<String> listDistribute = SingleRedDistributedV11.listDistribute();
			if (null == listDistribute || 0 == listDistribute.size()) {
				return;
			}
			
			// 保存长度为1-3的所有组合的出现个数
			Map<String, Integer> combinationMap = new HashMap<String, Integer>();
			int q = 1, w = 1, r = 1;
			int max = SSH.RED.getMAX();
			for (; q <= max; q++) {

				String field = "";
				int value = 0;

				field =  String.format("%02d", q); 
				value = redisDao.hget(SSH.RED.getRedisKey(), field);
				combinationMap.put(field, value);
				
				for (w = q + 1; w <=max; w++) {

					field =  String.format("%02d", q) 
							+ ":" + String.format("%02d", w);
					value = redisDao.hget(SSH.RED.getRedisKey(), field);
					combinationMap.put(field, value);

					for (r = w + 1; r <=max; r++) {
						
						field =  String.format("%02d", q) 
								+ ":" + String.format("%02d", w) 
								+ ":" + String.format("%02d", r);
						value = redisDao.hget(SSH.RED.getRedisKey(), field);
						combinationMap.put(field, value);
					}
					
				}
				
			}
			
			Map<String, Double> rsTotalMap	= new HashMap<String, Double>();
			for (String key : listDistribute) {

				log.info("Key: " + key);
				
				// 结果集
				Map<String, Double> rsMap = new HashMap<String, Double>();
				
				// 找到分解线
				Integer[] ballsStart = new Integer[6]; // 值为0代表将start赋值为上一维的start+1
				Integer[] ballsEnd = new Integer[6];
				
				char[] tokens = key.toCharArray();
				int curTotal = 0;
				for (int i = 0; i < SingleRedDistributedV11.getTOTAL(); i++) {
					
					int curNum = Integer.parseInt(String.valueOf(tokens[i]));
					for (int j = 0; j < curNum; j++) {
						if (j == 0) {
							ballsStart[j + curTotal] = SSH.SingleRedDistributedV11.getSingleRedDistributed(i + 1).getMIN();
						} else {
							ballsStart[j + curTotal] = 0;
						}
						ballsEnd[j + curTotal] = SSH.SingleRedDistributedV11.getSingleRedDistributed(i + 1).getMAX();
					}
					curTotal += curNum;
				}

				// 应用SimpleSpan3V6算法(算法和SimpleSpan3V5一样), 下面代表六个球
				int a = ballsStart[0], 
					b = 1, 
					c = 1,
					d = 1,
					e = 1,
					f = 1;
				for (; a <= ballsEnd[0]; a++) {
					b = (ballsStart[1] == 0) ? (a + 1) : ballsStart[1];
					for (;b <= ballsEnd[1]; b++) {
						c = (ballsStart[2] == 0) ? (b + 1) : ballsStart[2];
						for (; c <= ballsEnd[2]; c++) {
							d = (ballsStart[3] == 0) ? (c + 1) : ballsStart[3];
							for (; d <= ballsEnd[3]; d++) {
								e = (ballsStart[4] == 0) ? (d + 1) : ballsStart[4];
								for (; e <= ballsEnd[4]; e++) {
									f = (ballsStart[5] == 0) ? (e + 1) : ballsStart[5];
									for (; f <= ballsEnd[5]; f++) {
										
										// 分子 分母
										long molecular = 10000; 
										long denominator = 1;
										String field = "";
										
										// 计算分子
										field =  String.format("%02d", a) 
												+ ":" + String.format("%02d", b) 
												+ ":" + String.format("%02d", c);
										molecular *= combinationMap.get(field);
										field =  String.format("%02d", b) 
												+ ":" + String.format("%02d", c) 
												+ ":" + String.format("%02d", d);
										molecular *= combinationMap.get(field);
										field =  String.format("%02d", c) 
												+ ":" + String.format("%02d", d) 
												+ ":" + String.format("%02d", e);
										molecular *= combinationMap.get(field);
										field =  String.format("%02d", d) 
												+ ":" + String.format("%02d", e) 
												+ ":" + String.format("%02d", f);
										molecular *= combinationMap.get(field);
										molecular *= molecular;
										
										// 计算分母
										field =  String.format("%02d", a);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", b);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", c);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", d);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", b) 
												+ ":" + String.format("%02d", c);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", c) 
												+ ":" + String.format("%02d", d);
										denominator *= combinationMap.get(field);
										field =  String.format("%02d", d) 
												+ ":" + String.format("%02d", e);
										denominator *= combinationMap.get(field);
										
										// 保存到结果集
										field = String.format("%02d", a) 
												+ ":" + String.format("%02d", b) 
												+ ":" + String.format("%02d", c) 
												+ ":" + String.format("%02d", d)
												+ ":" + String.format("%02d", e)
												+ ":" + String.format("%02d", f);
										double rs = 0.0;
										if (denominator != 0) {
											rs = (double)molecular / (double)denominator;
										}
										rsMap.put(field, rs);
										rsTotalMap.put(field, rs);
										log.debug("组合[" + field + "]: " + rs);
										
										// 如果结果集超过了size个，进行排序，剔除最后的一个
										// 分别计算每个分布以及全局
										if (rsMap.size() > total) {
											ArrayList<Entry<String, Double>> tmp = new ArrayList<Entry<String, Double>>(rsMap.entrySet());
											Collections.sort(tmp, new SSHRedProbabilityComparator());
											Entry<String, Double> which2Del = tmp.get(tmp.size() - 1);
											rsMap.remove(which2Del.getKey());
											log.debug("组合[" + which2Del.getKey() + "]被删除, 其概率为" + which2Del.getValue());
											
										}
										if (rsTotalMap.size() > globalTotal) {
											ArrayList<Entry<String, Double>> tmp = new ArrayList<Entry<String, Double>>(rsTotalMap.entrySet());
											Collections.sort(tmp, new SSHRedProbabilityComparator());
											Entry<String, Double> which2Del = tmp.get(tmp.size() - 1);
											rsTotalMap.remove(which2Del.getKey());
											log.debug("组合[" + which2Del.getKey() + "]被删除, 其概率为" + which2Del.getValue());
										}
									}
									
								}
								
							}
							
						}
						
					}
					
				}
				
				/**
				 *  此处已经得到了结果集，首先删除Redis中这个key，然后将结果集存储到Redis中，清空rsMap，进行下一轮
				 *	key: 	"topn_combination:simple_span3v5:${distribution}"
				 * 	field: 	${combination}
				 * 	value:	${probabolity}
				 */
				
				Set<String> rsKeys = rsMap.keySet();
				if (rsKeys == null) {
					return;
				}
				String redisKey = SSHRedAlgorithm.getRedisKeyOfTopNCombinationByDistribution(SSHRedAlgorithm.SIMPLE_SPAN3V6, key);
				redisDao.del(redisKey);
				for (String rsKey : rsKeys) {
					String redisField	= rsKey;
					String redisValue	= String.valueOf(rsMap.get(rsKey));
					redisDao.hset(redisKey, redisField, redisValue);
					log.info("\t<" + redisField + ", " + redisValue + ">");
				}
				rsMap.clear();
				
			}

			Set<String> rsKeys = rsTotalMap.keySet();
			if (rsKeys == null) {
				return;
			}
			String redisKey = SSHRedAlgorithm.getRedisKeyOfTotalTopNCombination(SSHRedAlgorithm.SIMPLE_SPAN3V6);
			redisDao.del(redisKey);
			for (String rsKey : rsKeys) {
				String redisField	= rsKey;
				String redisValue	= String.valueOf(rsTotalMap.get(rsKey));
				redisDao.hset(redisKey, redisField, redisValue);
				log.info("\t<" + redisField + ", " + redisValue + ">");
			}
			rsTotalMap.clear();
			
		}
	}
	
}

