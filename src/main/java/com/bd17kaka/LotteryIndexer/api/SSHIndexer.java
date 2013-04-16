package com.bd17kaka.LotteryIndexer.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
			
			String[] input = args[1].split(",");
			List<String> redList = new ArrayList<String>();
			List<String> blueList = new ArrayList<String>();
			
			for (int i = 0; i < SSH.RED.getTOTAL(); i++) {
				
				int num = 0;
				try {
					num = Integer.parseInt(input[i]);
				} catch (Exception e) {
					log.error("序列 " + args[1] + " 中，" + input[i] + " 不是合法数字");
					return;
				}
				
				if (!SSH.RED.isValidNum(num)) {
					log.error("序列 " + args[1] + " 中，" + input[i] + " 不是有效数字，必须在"
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
					log.error("序列 " + args[1] + " 中，" + input[i] + " 不是合法数字");
					return;
				}
				
				if (!SSH.BLUE.isValidNum(num)) {
					log.error("序列 " + args[1] + " 中，" + input[i] + " 不是有效数字，必须在"
							+ SSH.BLUE.getMIN() + "和" + SSH.BLUE.getMAX() + "之间");
					return;
				}
				blueList.add(input[i]);
			}
			
			List<String> keyList = null;
			keyList = redIndexer(redList);
			redisDao.insert(keyList, SSH.RED.getRedisKey());
			keyList = blueIndexer(blueList);
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
				List<String> redList = new ArrayList<String>();
				List<String> blueList = new ArrayList<String>();
				String[] input = line.split(",");
				
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
			
				redKeyList.addAll(redIndexer(redList));
				blueKeyList.addAll(blueIndexer(blueList));
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

	
	/**
	 * 对红球进行索引
	 * 只考虑前面和后面一位数字对当前位数字的影响
	 * @return
	 */
	private static List<String> redIndexer(List<String> list)  {
		
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
	
	/**
	 * 对篮球进行索引
	 * @param list
	 * @param depth
	 * @return
	 */
	private static List<String> blueIndexer(List<String> list) {
		return list;
	}
}
