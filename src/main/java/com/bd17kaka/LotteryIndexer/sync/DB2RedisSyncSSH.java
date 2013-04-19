package com.bd17kaka.LotteryIndexer.sync;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bd17kaka.LotteryIndexer.constat.SSH;
import com.bd17kaka.LotteryIndexer.dao.RedisDao;
import com.bd17kaka.LotteryIndexer.dao.SSHNewCombinationDao;

/**
 * @author bd17kaka
 * 将双色球的数据从DB同步到Redis
 */
/**
 * @author bd17kaka
 * combination_num_by_length_firstnum:
 * 					同步指定长度，指定第一个球号的组合的出现次数
 * 					key -- ssh:red:combination:($length)
 * 					field -- firstnum
 * 					value -- 出现次数
 * 					comment: 以某一个球号开头的组合，随着组合长度的增加，其组合总出现数值会减少，这是因为：
 * 								假设观察以03号球开始的组合，如果长度为4的出现数为294，长度为5的出现数为289，
 * 								那么说明以01:02:03开头出现的长度为6的组合数目为(294-289),
 * 								当然01号球不会出现这种情况
 */
public class DB2RedisSyncSSH {
	
	private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/spring.xml");
	private static SSHNewCombinationDao sshNewCombinationDao = (SSHNewCombinationDao) context.getBean("sshNewCombinationDao");
	private static RedisDao redisDao = (RedisDao) context.getBean("redisDao");
	private static final Log log = LogFactory.getLog(DB2RedisSyncSSH.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length == 0) {
			log.info("请输入参数");
			return;
		}
		
		// 
		String type = args[0];
		
		if ("combination_num_by_length_firstnum".equals(type)) {
			
			for (int i = 1; i <= SSH.RED.getTOTAL(); i++) {
				
				for (int j = 1; j <= SSH.RED.getMAX(); j++) {
					
					String firstNum = String.format("%02d", j);
					
					String key = SSH.RED.getRedisKeyForCombination(i);
					String field = firstNum;
					int value = sshNewCombinationDao.getNumByLengthAndFirstNum(i, firstNum);
					
					log.info(key + " " + field + " " + value);
					
					redisDao.hset(key, field, value+"");
					
				}
				
			}
			
		}
		
	}

}
