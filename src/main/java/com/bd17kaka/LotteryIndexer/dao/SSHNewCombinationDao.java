package com.bd17kaka.LotteryIndexer.dao;

import com.bd17kaka.LotteryIndexer.po.SSHNewCombination;


/**
 * 双色球新出现组合DAO
 * @author bd17kaka
 */
public interface SSHNewCombinationDao {
	/**
	 * 插入新组合
	 * @param user
	 * @return
	 */
	boolean insert(SSHNewCombination sshNewCombination);

}
