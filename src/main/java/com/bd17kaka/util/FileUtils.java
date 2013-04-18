package com.bd17kaka.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtils {

	private static final Log log = LogFactory.getLog(FileUtils.class);
	
	public static void reverseFileLine(String inFilePath, String outFilePath) {
		
		FileReader fr = null;
		try {
			fr = new FileReader(inFilePath);
		} catch (FileNotFoundException e) {
			log.error("输入文件不存在: " + inFilePath);
			return;
		}
		BufferedReader br = new BufferedReader(fr);
		
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(outFilePath);
		} catch (IOException e) {
			log.error("无法创建输出文件: " + outFilePath);
			return;
		}
		bw = new BufferedWriter(fw);
		
		List<String> lines = new ArrayList<String>();
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
			
			lines.add(line);
		}
		
		try {
			br.close();
			fr.close();
		} catch (IOException e) {}
		
		int size = lines.size();
		for (int i = size - 1; i >= 0; i--) {
			try {
				fw.write(lines.get(i));
				fw.write("\r\n");
			} catch (IOException e) {
				continue;
			}
		}
		try {
			bw.close();
			fw.close();
		} catch (IOException e) {}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		reverseFileLine("E:\\Dropbox\\proj\\lottery\\ssh.txt", "E:\\Dropbox\\proj\\lottery\\ssh2.txt");
	}

}
