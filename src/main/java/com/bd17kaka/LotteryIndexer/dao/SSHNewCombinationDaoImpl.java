package com.bd17kaka.LotteryIndexer.dao;

import java.sql.Types;

import org.springframework.stereotype.Repository;

import com.bd17kaka.LotteryIndexer.po.SSHNewCombination;

/**
 * 双色球新出现组合DAO
 * @author bd17kaka
 */
@Repository(value="sshNewCombinationDao")
public class SSHNewCombinationDaoImpl extends SpringJDBCDaoSupport implements SSHNewCombinationDao {

	private static final String TABLE = "ssh_new_combination";

	public boolean insert(SSHNewCombination sshNewCombination) {
		String sql = "insert into " + TABLE + " values(?,?,?,?) ON DUPLICATE KEY UPDATE num=num+1";
		Object[] args = new Object[] { 
				sshNewCombination.getId(), 
				sshNewCombination.getLength(),
				sshNewCombination.getCombination(),
				sshNewCombination.getNum()
		};
		int[] argTypes = new int[] { 
				Types.VARCHAR,
				Types.INTEGER,
				Types.VARCHAR,
				Types.INTEGER,
		};
		int n = 0;
		n = this.getJdbcTemplate().update(sql, args, argTypes);
		return n > 0 ? true : false;
	}
}
