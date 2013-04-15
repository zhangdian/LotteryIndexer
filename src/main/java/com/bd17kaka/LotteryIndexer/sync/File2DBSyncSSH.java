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

import com.bd17kaka.LotteryIndexer.api.SSHIndexer;
import com.bd17kaka.LotteryIndexer.constat.SSH;

/**
 * @author bd17kaka
 * 从文件到DB，同步双色球开奖记录
 */
public class File2DBSyncSSH {

	private static String IN_FILE_PATH = "E:\\Dropbox\\proj\\lottery\\双色球历史记录.txt";
	private static String OUT_FILE_PATH = "E:\\1.txt";
	private static final Log log = LogFactory.getLog(SSHIndexer.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FileReader fr = null;
		try {
			fr = new FileReader(IN_FILE_PATH);
		} catch (FileNotFoundException e) {
			log.error("输入文件不存在: " + IN_FILE_PATH);
			return;
		}
		BufferedReader br = new BufferedReader(fr);
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(OUT_FILE_PATH);
		} catch (IOException e) {
			log.error("输出文件不存在: " + OUT_FILE_PATH);
			return;
		}
		BufferedWriter bw = new BufferedWriter(fw);
		
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
			
		}
		try {
			br.close();
			bw.close();
			fr.close();
			fw.close();
		} catch (IOException e) {}
		
	}

}
