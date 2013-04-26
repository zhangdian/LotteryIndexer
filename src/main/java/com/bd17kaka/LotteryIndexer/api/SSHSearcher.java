package com.bd17kaka.LotteryIndexer.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bd17kaka.LotteryIndexer.constat.SSH;
import com.bd17kaka.LotteryIndexer.dao.RedisDao;

public class SSHSearcher {

	private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/spring.xml");
	private static RedisDao redisDao = (RedisDao) context.getBean("redisDao");
	private static final Log log = LogFactory.getLog(SSHSearcher.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String line = "";
		
		String[] tokens = line.split(",");
		double fm = 0.0;
		
		// 首先获取第一个元素出现的概率
		int value = redisDao.hget(SSH.RED.getRedisKey(), tokens[0]);
		int totalValue = 0;
		for (int i = 0; i < 10; i++) {
			totalValue += redisDao.hget(SSH.RED.getRedisKey(), "0"+i);
		}
		for (int i = 10; i < SSH.RED.getMAX(); i++) {
			totalValue += redisDao.hget(SSH.RED.getRedisKey(), i+"");
		}
		fm = (double)value / (double)totalValue;
		
		// 循环计算后面的概率
		for (int i = 0; i < tokens.length - 2; i++) {
			value = redisDao.hget(SSH.RED.getRedisKey(), tokens[i] + ":" + tokens[i + 1]);
			totalValue = redisDao.hget(SSH.RED.getRedisKey(), tokens[i]);
			System.out.println(fm);
			System.out.println(value);
			System.out.println(totalValue);
			System.out.println();
			fm *= (double)value / (double)totalValue; 
		}
		
		// 结果
		System.out.println(fm);
	}

}
