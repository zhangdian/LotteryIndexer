package com.bd17kaka.LotteryIndexer.dao;

import java.util.List;

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
	
	/**
	 * 获取所有以firstNum开头的，length长度的组合的出现次数
	 * @return
	 */
	int getNumByLengthAndFirstNum(int length, String firstNum);
	
	
	/**
	 * 获取指定长度的组合所有可能的出现个数
	 * select num from ssh_new_combination where length=2 group by num;
	 * @param length
	 * @return
	 */
	List<SSHNewCombination> listNumByLength(int length);
	
	/**
	 * 获取指定长度、指定出现个数num的组合的个数
	 * @param num
	 * @param length
	 * @return
	 */
	int getSizeByNumAndLength(int num, int length);

}
