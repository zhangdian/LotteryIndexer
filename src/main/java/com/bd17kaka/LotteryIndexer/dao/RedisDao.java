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
	
	/**
	 * 获取某个key出现的次数
	 * @param key
	 * @return
	 */
	int getNum(String redisKey, String field);
	
	/**
	 * 设置值
	 * @param redisKey
	 * @param field
	 * @param value
	 */
	void hset(String redisKey, String field, String value);
	
}
