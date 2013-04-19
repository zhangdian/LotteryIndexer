package com.bd17kaka.LotteryIndexer.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
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

	public int getNumByLengthAndFirstNum(int length, String firstNum) {
		String sql = "select sum(num) from "+TABLE+" where length=? and combination like ?";
		Object[] args = new Object[] { length, firstNum+"%" };
		int[] argTypes = new int[] { Types.INTEGER, Types.VARCHAR };

		int n = 0;
		
		try {
			n = this.getJdbcTemplate().queryForInt(sql, args, argTypes);
		} catch (Exception e) {
			n = 0;
		}
		return n;
	}
	
	private SSHNewCombination rsToAPIKey(ResultSet rs) throws SQLException {
		SSHNewCombination po = null;
		if (rs != null) {
			po = new SSHNewCombination();
			po.setCombination(rs.getString("combination"));
			po.setId(rs.getString("id"));
			po.setLength(rs.getInt("length"));
			po.setNum(rs.getInt("num"));
		}
		return po;
	}
	
}
