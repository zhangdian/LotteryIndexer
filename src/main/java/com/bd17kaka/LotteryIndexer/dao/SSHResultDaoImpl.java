package com.bd17kaka.LotteryIndexer.dao;

import java.sql.Types;

import org.springframework.stereotype.Repository;

import com.bd17kaka.LotteryIndexer.po.SSHResult;

/**
 * 双色球开奖结果DAO
 * @author bd17kaka
 */
@Repository(value="sshResultDao")
public class SSHResultDaoImpl extends SpringJDBCDaoSupport implements SSHResultDao {

	private static final String TABLE = "ssh_result";
	
	public boolean insert(SSHResult sshResult) {
		
		String sql = "insert into " + TABLE + " values(?,?,?,?,?,?)";
		Object[] args = new Object[] { 
				sshResult.getId(), 
				sshResult.getTime(), 
				sshResult.getResult(), 
				sshResult.getType(),
				sshResult.getDistributionV3(),
				sshResult.getDistributionV11()
		};
		int[] argTypes = new int[] { 
				Types.VARCHAR,
				Types.DATE,
				Types.VARCHAR,
				Types.INTEGER,
				Types.VARCHAR,
				Types.VARCHAR
		};
		int n = 0;
		n = this.getJdbcTemplate().update(sql, args, argTypes);
		return n > 0 ? true : false;
	}
}
