package com.bd17kaka.LotteryIndexer.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bd17kaka.LotteryIndexer.constat.SSH;
import com.bd17kaka.LotteryIndexer.dao.RedisDao;
import com.bd17kaka.LotteryIndexer.dao.SSHNewCombinationDao;
import com.bd17kaka.LotteryIndexer.po.SSHNewCombination;

public class SSHIndexer {

	private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/spring.xml");
	private static RedisDao redisDao = (RedisDao) context.getBean("redisDao");
	private static SSHNewCombinationDao sshNewCombinationDao = (SSHNewCombinationDao) context.getBean("sshNewCombinationDao");
	private static final Log log = LogFactory.getLog(SSHIndexer.class);
	
	/**
	 * 双色球索引
	 * 将第一次出现的组合保存到DB: ssh_new_combination
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
		
		// 
		String type = args[0]; 
		
		if ("0".equals(type)) {

			String line = args[1];
			
			String[] tokens = line.split(" ");
			String id = tokens[0];
			line = tokens[1];
			
			List<String> redList = SSH.RED.getNumsFromInuput(line);
			List<String> blueList = SSH.BLUE.getNumsFromInuput(line);
			
			List<String> redKeyList = null;
			List<String> blueKeyList = null;
			redKeyList = SSH.RED.indexer(redList);
			redisDao.insert(redKeyList, SSH.RED.getRedisKey());
			blueKeyList = SSH.BLUE.indexer(blueList);
			redisDao.insert(blueKeyList, SSH.BLUE.getRedisKey());
			
			// 每个组合在list中只会出现一次，那么当从redis中get获得的值是1的时候，说明是新出现的
			// 只需要考虑红球
			for (String s : redKeyList) {
				SSHNewCombination sshNewCombination = new SSHNewCombination();
				sshNewCombination.setId(id);
				sshNewCombination.setLength(s.split(":").length);
				sshNewCombination.setCombination(s);
				sshNewCombination.setNum(1);
				sshNewCombinationDao.insert(sshNewCombination);
			}
			
		} else if ("1".equals(type)) {
			
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
				
				// 
				String[] tokens = line.split(" ");
				String id = tokens[0];
				line = tokens[1];
				
				// 获取开奖号码，以逗号隔开
				List<String> redList = SSH.RED.getNumsFromInuput(line);
				List<String> blueList = SSH.BLUE.getNumsFromInuput(line);
				List<String> redKeyList = null;
				List<String> blueKeyList = null;
				redKeyList = SSH.RED.indexer(redList);
				redisDao.insert(redKeyList, SSH.RED.getRedisKey());
				blueKeyList = SSH.BLUE.indexer(blueList);
				redisDao.insert(blueKeyList, SSH.BLUE.getRedisKey());
				
				// 每个组合在list中只会出现一次，那么当从redis中get获得的值是1的时候，说明是新出现的
				// 只需要考虑红球
				for (String s : redKeyList) {
					SSHNewCombination sshNewCombination = new SSHNewCombination();
					sshNewCombination.setId(id);
					sshNewCombination.setLength(s.split(":").length);
					sshNewCombination.setCombination(s);
					sshNewCombination.setNum(1);
					sshNewCombinationDao.insert(sshNewCombination);
				}
			}
			
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
