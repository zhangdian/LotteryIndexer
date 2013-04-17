package com.bd17kaka.LotteryIndexer.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	 * 			args[0]: 索引来源
	 * 						0 -- 参数args[1]是号码
	 * 						1 -- 参数args[1]是文件路径
	 * 			args[1]: 具体来源见args[0], 内容是7个数字，以‘,’分隔。前六个是红球，最后一个是篮球
	 */
	public static void main(String[] args) {

		// 记录时间
		long startTime=System.currentTimeMillis();
		
		// 参数检查
		if (args.length < 2) {
			log.error("使用说明: SSHIndexer.java [0|1] [01,02,03,04,05,06,07|'c:\1.txt']");
			log.error("请输入参数");
			return;
		}
		
		if ("0".equals(args[0])) {

			String line = args[1];
			List<String> redList = SSH.RED.getNumsFromInuput(line);
			List<String> blueList = SSH.BLUE.getNumsFromInuput(line);
			
			List<String> keyList = null;
			keyList = SSH.RED.indexer(redList);
			redisDao.insert(keyList, SSH.RED.getRedisKey());
			keyList = SSH.BLUE.indexer(blueList);
			redisDao.insert(keyList, SSH.BLUE.getRedisKey());
			
		} else if ("1".equals(args[0])) {
			
			String filePath = args[1];
			
			FileReader fr = null;
			try {
				fr = new FileReader(filePath);
			} catch (FileNotFoundException e) {
				log.error("输入文件不存在: " + filePath);
				return;
			}
			BufferedReader br = new BufferedReader(fr);
			
			String line = null;
			List<String> redKeyList = new ArrayList<String>();
			List<String> blueKeyList = new ArrayList<String>();
			while (true) {
				
				// 读取一行数据
				try {
					line = br.readLine();
					if (line == null) {
						break;
					}
					log.info("处理数据: " + line);
					
				} catch (IOException e) {
					continue;
				}
				
				// 获取开奖号码，以逗号隔开
				List<String> redList = SSH.RED.getNumsFromInuput(line);
				List<String> blueList = SSH.BLUE.getNumsFromInuput(line);
				redKeyList.addAll(SSH.RED.indexer(redList));
				blueKeyList.addAll(SSH.BLUE.indexer(blueList));
			}
			
			redisDao.insert(redKeyList, SSH.RED.getRedisKey());
			redisDao.insert(blueKeyList, SSH.BLUE.getRedisKey());

			try {
				br.close();
				fr.close();
			} catch (IOException e) {}
			
		} else {
			log.error("无效参数: " + args[0]);
			log.error("使用说明: SSHIndexer.java [0|1] [01,02,03,04,05,06,07|'c:\1.txt']");
			return;
		}
		
		long endTime = System.currentTimeMillis(); 
		log.info("程序一共运行了: " + (endTime - startTime) + "ms");
	}
}
