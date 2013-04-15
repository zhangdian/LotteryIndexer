package com.bd17kaka.LotteryIndexer.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.bd17kaka.LotteryIndexer.api.SSHIndexer;

import redis.clients.jedis.ShardedJedis;

/**
 * 将数据存储在Redis中
 * @author bd17kaka
 */
@Repository(value="redisDao")
public class RedisDaoImpl extends RedisUtils implements RedisDao {

	private static final Log log = LogFactory.getLog(SSHIndexer.class);
	
	@Override
	public void insert(List<String> keys, String redisKey) {
		
		if (null != keys) {
			
			ShardedJedis redis = getConnection();
			
			for (String key : keys) {
				
				redis.hincrBy(redisKey, key, 1);
				log.info("为" + key + "的出现次数加1");
				
			}
			
		}
		
	}

}
