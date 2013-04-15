package com.bd17kaka.LotteryIndexer.dao;

import java.util.List;


/**
 * 将数据存储在Redis中
 * @author bd17kaka
 */
public interface RedisDao {

	
	/**
	 * 将指定类型的索引存储到redis
	 * @param map
	 */
	void insert(List<String> indexs, String redisKey);
	
}
