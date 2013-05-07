package com.bd17kaka.LotteryIndexer.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bd17kaka.LotteryIndexer.constat.SSH;
import com.bd17kaka.LotteryIndexer.constat.SSH.RedDistributed;
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
	 * 		agrs[1]: n的大小
	 * 
	 * simple_span2:
	 * 		key: 	"topn_combination:simple_span2"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 * simple_span3:
	 * 		key: 	"topn_combination:simple_span3"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 * simple_span3v2:
	 * 		key: 	"topn_combination:simple_span3v2"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 * simple_span3v3:
	 * 		key: 	"topn_combination:simple_span3v3"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 * simple_span3v5:
	 * 		key: 	"topn_combination:simple_span3v5:${distribution}"
	 * 		field: 	${combination}
	 * 		value:	${probabolity}
	 */
	public static void main(String[] args) {

		// 获取输入参数
		String type = "";
		int n = 10;
		int total = 20;
		
		if (args.length == 0) {
			log.error("请输入正确的参数：[type, n, total]");
			return;
		} 
		type = args[0];
		try {
			n = Integer.parseInt(args[1]);
		} catch (Exception e) {
			n = 10;
		}
		if (n < 10) {
			n = 10;
		}
		try {
			total = Integer.parseInt(args[2]);
		} catch (Exception e) {
			total = 20;
		}
		if (total < 20) {
			total = 20;
		}
		
		if ("simple_span3v5".equals(type)) {

			/**
			 * 获取所有分布情况（共28种），然后获取每种分布的TopN的组合
			 * 将这些组合存储到Redis中
			 * key: 	"topn_combination:simple_span3v5:${distribution}"
			 * field: 	${combination}
			 * value:	${probabolity}
			 */
			
			log.info("*******************simple_span2v5*********************");
			
			Map<Integer, RedDistributed> mapRedDistributed = SSH.RedDistributed.getReddistributedMap();
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
				Map<String, Double> rsMap 		= new HashMap<String, Double>();
				
				// 找到分解线
				Integer[] ballsStart = new Integer[6]; // 值为0代表将start赋值为上一维的start+1
				Integer[] ballsEnd = new Integer[6];
				int leftNum 	= 0,
					middleNum 	= 0,
					rightNum	= 0;
				
				leftNum = key / 100;
				for (int i = 0; i < leftNum; i++) {
					if (i == 0) {
						ballsStart[i] = SSH.SingleRedDistributed.LEFT.getMIN();
					} else {
						ballsStart[i] = 0;
					}
					ballsEnd[i] = SSH.SingleRedDistributed.LEFT.getMAX();
				}
				
				middleNum = (key % 100) / 10;
				for (int i = 0; i < middleNum; i++) {
					if (i == 0) {
						ballsStart[leftNum + i] = SSH.SingleRedDistributed.MIDDLE.getMIN();
					} else {
						ballsStart[leftNum + i] = 0;
					}
					ballsEnd[leftNum + i] = SSH.SingleRedDistributed.MIDDLE.getMAX();
				}
				
				rightNum = ((key % 100) % 10) / 1;
				for (int i = 0; i < rightNum; i++) {
					if (i == 0) {
						ballsStart[leftNum + middleNum + i] = SSH.SingleRedDistributed.RIGHT.getMIN();
					} else {
						ballsStart[leftNum + middleNum + i] = 0;
					}
					ballsEnd[leftNum + middleNum + i] = SSH.SingleRedDistributed.RIGHT.getMAX();
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
										if (rsMap.size() > n) {
											ArrayList<Entry<String, Double>> tmp = new ArrayList<Entry<String, Double>>(rsMap.entrySet());
											Collections.sort(tmp, new SSHRedProbabilityComparator());
											Entry<String, Double> which2Del = tmp.get(tmp.size() - 1);
											rsMap.remove(which2Del.getKey());
											log.debug("组合[" + which2Del.getKey() + "]被删除, 其概率为" + which2Del.getValue());
											
										}
										if (rsTotalMap.size() > total) {
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
				String redisKey = RedDistributed.getRedisKeyOfSimpleSpan3V5(key);
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
			String redisKey = RedDistributed.getRedisKeyOfSimpleSpan3V5Total();
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

