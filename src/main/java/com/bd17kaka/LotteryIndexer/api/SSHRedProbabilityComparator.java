package com.bd17kaka.LotteryIndexer.api;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author bd17kaka
 * 对所有的可能性进行排序
 */
public class SSHRedProbabilityComparator implements Comparator<Map.Entry<String, Double>> {

	public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
		
		return (o1.getValue() < o2.getValue()) ? 1 : -1;
	}
}
