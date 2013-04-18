package com.bd17kaka.LotteryIndexer.sync;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bd17kaka.LotteryIndexer.constat.SSH;
import com.bd17kaka.LotteryIndexer.dao.SSHResultDao;
import com.bd17kaka.LotteryIndexer.po.SSHResult;

/**
 * @author bd17kaka
 * 从文件到DB，同步双色球开奖记录
 * 当多次执行这个代码的时候，需要将数据库中的数据进行清空
 */
public class File2DBSyncSSH {

	private static String IN_FILE_PATH = "E:\\Dropbox\\proj\\lottery\\ssh.txt";
	private static String OUT_FILE_PATH = "E:\\Dropbox\\proj\\lottery\\";
	
	private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/spring.xml");
	private static SSHResultDao sshResultDao = (SSHResultDao) context.getBean("sshResultDao");
	private static final Log log = LogFactory.getLog(File2DBSyncSSH.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		for (int i =0; i < args.length; i++) {
			System.out.println(args[i]);
		}
		
		if (args.length == 1) {
			IN_FILE_PATH = args[0];
		}
		if (args.length == 2) {
			IN_FILE_PATH = args[0];
			OUT_FILE_PATH = args[1];
		}
		
		System.out.println(IN_FILE_PATH);
		System.out.println(OUT_FILE_PATH);
		
		FileReader fr = null;
		try {
			fr = new FileReader(IN_FILE_PATH);
		} catch (FileNotFoundException e) {
			log.error("输入文件不存在: " + IN_FILE_PATH);
			return;
		}
		BufferedReader br = new BufferedReader(fr);
		
		FileWriter fw = null;
		BufferedWriter bw = null;
		
		do {
			
			int curYear = 0;
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
				
				/*
				 * 处理数据
				 * tokens[0]: 	开奖日期
				 * tokens[1]: 	开奖期数
				 * tokens[2-8]: 开奖号码 
				 * tokens[9]:	当期销售额
				 */
				// 获取日期
				String[] tokens = line.split("([ |\t])+");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = null;
				try {
					date = sdf.parse(tokens[0]);
				} catch (ParseException e) {
					continue;
				}
				
				// 获取开奖年份，决定是否需要新建输入文件
				int year = date.getYear() + 1900;
				if (curYear == 0) {
					
					curYear = year;
					try {
						fw = new FileWriter(OUT_FILE_PATH + year + ".txt");
					} catch (IOException e) {
						log.error("无法创建输出文件: " + OUT_FILE_PATH + year + ".txt");
						return;
					}
					bw = new BufferedWriter(fw);
				} else if (curYear != year) {
					
					try {
						bw.close();
						fw.close();
					} catch (IOException e) {
					}
					
					curYear = year;
					try {
						fw = new FileWriter(OUT_FILE_PATH + year + ".txt");
					} catch (IOException e) {
						log.error("无法创建输出文件: " + OUT_FILE_PATH + year + ".txt");
						return;
					}
					bw = new BufferedWriter(fw);
				}
				
				// 获取开奖号码，以逗号隔开
				String awardStr = "";
				for (int i = 0; i < SSH.TOTAL; i++) {
					awardStr += tokens[i + 2] + ",";
				}
				awardStr = awardStr.substring(0, awardStr.length() - 1);
				try {
					fw.write(awardStr);
					fw.write("\r\n");
				} catch (IOException e) {}
				
				// 将数据写到DB
				SSHResult sshResult = new SSHResult();
				sshResult.setId(tokens[1]);
				sshResult.setResult(awardStr);
				sshResult.setTime(date);
				if (curYear < 2010) {
					sshResult.setType(0);
				} else {
					sshResult.setType(1);
				}
				sshResultDao.insert(sshResult);
			}
			
		} while (false);
		
		try {
			br.close();
			bw.close();
			fr.close();
			fw.close();
		} catch (IOException e) {}
		
	}

}
