package com.bd17kaka.LotteryIndexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest {

	public static void main(String[] args) {
		
		List<String> list = new ArrayList<String>();
		System.out.println(test(11, 6, "", list));
		System.out.println(list.size());
		for (String string : list) {
			System.out.println(string);
		}
	}
	
	
	public static int test (int holes, int balls, String curDistribute, List<String> listDistribute) {
		
		if (holes == 1) {
			if (balls <= 3 && balls >= 0) {
				curDistribute += balls;
				listDistribute.add(curDistribute);
				return 1;
			}
			else {
				return 0;
			}
		}
		if (balls == 0) {
			for (int i = 0; i < holes; i++) {
				curDistribute += "0 ";
			}
			listDistribute.add(curDistribute);
			return 1;
			
		} else if (balls < 0) {
		
			return 0;
		}
		
		int curHoles = holes - 1;
		return test(curHoles, balls, curDistribute+"0 ", listDistribute) 
				+ test(curHoles, balls - 1, curDistribute+"1 ", listDistribute) 
				+ test(curHoles, balls - 2, curDistribute+"2 ", listDistribute) 
				+ test(curHoles, balls - 3, curDistribute+"3 ", listDistribute);  
	}
	
}
