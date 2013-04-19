package com.bd17kaka.LotteryIndexer.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import redis.clients.jedis.ShardedJedis;

import com.bd17kaka.LotteryIndexer.api.SSHIndexer;

/**
 * 将数据存储在Redis中
 * @author bd17kaka
 */
@Repository(value="redisDao")
public class RedisDaoImpl extends RedisUtils implements RedisDao {

	private static final Log log = LogFactory.getLog(SSHIndexer.class);

	public void insert(List<String> indexs, String redisKey) {
		
		if (null != indexs) {
			
			ShardedJedis redis = getConnection();
			
			for (String key : indexs) {
				
				redis.hincrBy(redisKey, key, 1);
				log.info("为" + key + "的出现次数加1");
				
			}
			
			returnConnection(redis);
		}
		
	}

	public int getNum(String key, String field) {
		
		ShardedJedis redis = getConnection();

		int value = 0;
		String strValue = redis.hget(key, field);
		try {
			value = Integer.parseInt(strValue);
		} catch (Exception e) {
			value = 0;
		}
		
		returnConnection(redis);
		return value;
	}

	public void hset(String redisKey, String field, String value) {

		ShardedJedis redis = getConnection();

		redis.hset(redisKey, field, value);
		
		returnConnection(redis);
		
	}
}
