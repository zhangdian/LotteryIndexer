package com.bd17kaka.LotteryIndexer.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bd17kaka.LotteryIndexer.constat.SSH;
import com.bd17kaka.LotteryIndexer.dao.RedisDao;

public class SSHIndexer {

	private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/spring.xml");
	private static RedisDao redisDao = (RedisDao) context.getBean("redisDao");
	private static final Log log = LogFactory.getLog(SSHIndexer.class);
	
	/**
	 * 双色球索引
	 * @param args 
	 * 			args[0] : 7个数字，以‘,’分隔。前六个是红球，最后一个是篮球
	 * 			args[1] : 索引的深度
	 */
	public static void main(String[] args) {

		// 参数检查
		if (args.length == 0) {
			log.error("请输入参数");
			return;
		}
		
		String[] input = args[0].split(",");
		List<String> redList = new ArrayList<String>();
		List<String> blueList = new ArrayList<String>();
		
		for (int i = 0; i < SSH.RED.getTOTAL(); i++) {
			
			int num = 0;
			try {
				num = Integer.parseInt(input[i]);
			} catch (Exception e) {
				log.error("序列 " + args[0] + " 中，" + input[i] + " 不是合法数字");
				return;
			}
			
			if (!SSH.RED.isValidNum(num)) {
				log.error("序列 " + args[0] + " 中，" + input[i] + " 不是有效数字，必须在"
						+ SSH.RED.getMIN() + "和" + SSH.RED.getMAX() + "之间");
				return;
			}
				
			redList.add(input[i]);
			
		}
		
		
		for (int i = SSH.RED.getTOTAL(); i < SSH.TOTAL; i++) {
			
			int num = 0;
			try {
				num = Integer.parseInt(input[i]);
			} catch (Exception e) {
				log.error("序列 " + args[0] + " 中，" + input[i] + " 不是合法数字");
				return;
			}
			
			if (!SSH.BLUE.isValidNum(num)) {
				log.error("序列 " + args[0] + " 中，" + input[i] + " 不是有效数字，必须在"
						+ SSH.BLUE.getMIN() + "和" + SSH.BLUE.getMAX() + "之间");
				return;
			}
			blueList.add(input[i]);
		}
	
		// 获取索引深度，默认是2
		int depth = 2;
		if (args.length == 2) {
			try {
				depth = Integer.parseInt(args[1]);
			} catch (Exception e) {
				depth = 2;
			}
		}
		
		for (String s : redList) {
			log.info(s);
		}
		List<String> keyList = redIndexer(redList, 3);
		redisDao.insert(keyList, SSH.RED.getRedisKey());
		
		for (String s : blueList) {
			log.info(s);
		}
	}

	
	/**
	 * 对红球进行索引
	 * 只考虑前面和后面一位数字对当前位数字的影响
	 * @return
	 */
	private static List<String> redIndexer(List<String> list, int depth)  {
		
		List<String> rs = new ArrayList<String>();
		
		String key = "";
		int size = list.size();
		for (int i = 0; i < size; i++) {
			
			key = list.get(i);
			rs.add(key);
			
			int end = i + depth;
			for (int j = i + 1; j < (end > size ? size : end); j++) {
				
				key += ":" + list.get(j);
				rs.add(key);
			}
			key = "";
		}
		
		return rs;
	}
	
	private static List<String> blue(List<String> list, int depth) {
		return null;
	}
}
