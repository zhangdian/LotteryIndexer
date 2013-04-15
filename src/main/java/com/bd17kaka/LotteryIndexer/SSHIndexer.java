package com.bd17kaka.LotteryIndexer;

import java.util.ArrayList;
import java.util.List;

public class SSHIndexer {

	private static final int RED_MAX = 33;
	private static final int RED_MIN = 1;
	
	private static final int BLUE_MAX = 15;
	private static final int BLUE_MIN = 1;
	
	private static final int RED_TOTAL = 6;
	private static final int BLUE_TOTAL = 1;
	private static final int TOTAL = RED_TOTAL + BLUE_TOTAL;
	
	/**
	 * 双色球索引
	 * @param args 7个数字，以‘,’分隔
	 */
	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("请输入参数");
			return;
		}
		
		String[] input = args[0].split(",");
		List<String> redList = new ArrayList<String>();
		List<String> blueList = new ArrayList<String>();
		
		for (int i = 0; i < RED_TOTAL; i++) {
			
			int num = 0;
			try {
				num = Integer.parseInt(input[i]);
			} catch (Exception e) {
				return;
			}
			
			if (num > RED_MAX || num < RED_MIN) {
				return;
			}
			redList.add(input[i]);
			
		}
		
		
		for (int i = RED_TOTAL; i < TOTAL; i++) {
			
			int num = 0;
			try {
				num = Integer.parseInt(input[i]);
			} catch (Exception e) {
				return;
			}
			
			if (num > BLUE_MAX || num < BLUE_MIN) {
				return;
			}
			blueList.add(input[i]);
		}
	
		
		for (String s : redList) {
			System.out.println(s);
		}
		for (String s : blueList) {
			System.out.println(s);
		}
	}

}
