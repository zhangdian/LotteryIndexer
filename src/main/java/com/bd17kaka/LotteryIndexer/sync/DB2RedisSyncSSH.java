package com.bd17kaka.LotteryIndexer.sync;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bd17kaka.LotteryIndexer.constat.SSH;
import com.bd17kaka.LotteryIndexer.dao.RedisDao;
import com.bd17kaka.LotteryIndexer.dao.SSHNewCombinationDao;
import com.bd17kaka.LotteryIndexer.po.SSHNewCombination;

/**
 * @author bd17kaka
 * 将双色球的数据从DB同步到Redis
 */
/**
 * @author bd17kaka
 * combination_num_by_length_firstnum:
 * 					同步指定长度，指定第一个球号的组合的出现次数
 * 					key -- ssh:red:combination:num:($length)
 * 					field -- firstnum
 * 					value -- 出现次数
 * 					comment: 以某一个球号开头的组合，随着组合长度的增加，其组合总出现数值会减少，这是因为：
 * 								假设观察以03号球开始的组合，如果长度为4的出现数为294，长度为5的出现数为289，
 * 								那么说明以01:02:03开头出现的长度为6的组合数目为(294-289),
 * 								当然01号球不会出现这种情况
 * 
 * combination_size_by_num
 * 					同步出现指定次数num的组合的个数
 * 					key -- ssh:red:combination:size
 * 					field -- $(num)
 * 					value -- 个数
 * 					comment: 使用“马尔科夫假设”二元模型，所以只同步二元组合的个数。
 * 							 	未来也可以加一个length参数，同步多远模型
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
					
					String key = SSH.RED.getRedisKeyForCombinationNumByLengthAndFirstNum(i);
					String field = firstNum;
					int value = sshNewCombinationDao.getNumByLengthAndFirstNum(i, firstNum);
					
					log.info(key + " " + field + " " + value);
					
					redisDao.hset(key, field, value+"");
					
				}
				
			}
			
		} else if ("combination_size_by_num".equals(type)) {
			
			int length = 2;
			
			// 获取所有组合可能出现的个数, 只取list中每个SSHNewCombination对象的num字段
			List<SSHNewCombination> list = sshNewCombinationDao.listNumByLength(length);
			if (null != list) {
				
				for (SSHNewCombination item : list) {
					
					int num = item.getNum();
					log.info("处理出现" + num + "次的组合");
					int size = sshNewCombinationDao.getSizeByNumAndLength(num, length);
					log.info("出现" + num + "次的组合一共有" + size + "个");
					if (size != 0){
						// 将其保存到Redis
						String key = SSH.RED.getRedisKeyForCombinationSizeByNum();
						String field = num+"";
						String value = size+"";
						redisDao.hset(key, field, value);
					}
				}
			}
		}
		
	}

}
