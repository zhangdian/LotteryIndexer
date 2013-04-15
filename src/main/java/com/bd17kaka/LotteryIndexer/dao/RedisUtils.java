package com.bd17kaka.LotteryIndexer.dao;

import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

@Repository
public class RedisUtils {

	@Resource(name = "shardedJedisPool")
	protected ShardedJedisPool shardedJedisPool;
	
	public ShardedJedis getConnection() {
		return shardedJedisPool.getResource();
	}

	public void returnConnection(ShardedJedis resource) {
		shardedJedisPool.returnResource(resource);
	}
	
	private Random random = new Random();

	public String getRandomIP() {
		return new StringBuffer().append(random.nextInt(245) + 1).append(".")
				.append(random.nextInt(245) + 1).append(".")
				.append(random.nextInt(245) + 1).append(".")
				.append(random.nextInt(245) + 1).toString();
	}

	public String getRandomPassport() {
		return getRandomStr(random.nextInt(16) + 5);
	}

	public String getRandomStr(int length) {
		StringBuilder code = new StringBuilder();
		String charOrNum; // 输出字母还是数字
		int caseOption; // 取得大写字母还是小写字母 (ASCII码值)
		for (int i = 0; i < length; i++) {
			charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
			if ("char".equalsIgnoreCase(charOrNum)) { // 字符串
				caseOption = random.nextInt(2) % 2 == 0 ? 65 : 97;
				code.append((char) (caseOption + random.nextInt(26)));
			} else if ("num".equalsIgnoreCase(charOrNum)) { // 数字
				code.append((random.nextInt(10)));
			}
		}
		return code.toString().toLowerCase();
	}

	public int getRandomDayOfMonth() {
		return random.nextInt(30) + 1;
	}

}
