package com.bd17kaka.LotteryIndexer.dao;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;


@Repository
public class SpringJDBCDaoSupport extends JdbcDaoSupport
{

	private static final Logger Log = LoggerFactory.getLogger(SpringJDBCDaoSupport.class);
	
	@Resource(name="lotteryDataSource")
	private DataSource dataSource;//在spring.xml文件中配置

	private Random random = new Random();

	@PostConstruct
	public void initDataSource()
	{
		Log.debug("initDataSource");
		super.setDataSource(dataSource);
		//executeInitSQL();
	}
	
	private void executeInitSQL()
	{
		URL url = SpringJDBCDaoSupport.class.getResource("/init-sql");
		try {
			File file = new File(url.toURI());
			File[] sqlFiles = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name)
				{
					return name!=null && name.endsWith(".sql");
				}
			});
			for(File sqlFile:sqlFiles)
			{
				String sql = FileUtils.readFileToString(sqlFile,"UTF-8");
				Log.debug(sql);
				if(StringUtils.isEmpty(sql))
					continue;
				getJdbcTemplate().update(sql);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String getRandomIP()
	{
		return new StringBuffer()
		.append(random.nextInt(245)+1).append(".")
		.append(random.nextInt(245)+1).append(".")
		.append(random.nextInt(245)+1).append(".")
		.append(random.nextInt(245)+1)
		.toString();
	}

	public String getRandomPassport()
	{
		return getRandomStr(random.nextInt(16)+5);  
	}
	
	public String getRandomStr(int length)
	{
		StringBuilder code = new StringBuilder();   
	    String charOrNum;    // 输出字母还是数字   
	    int caseOption;      //取得大写字母还是小写字母  (ASCII码值) 
	    for(int i = 0; i < length; i++){   
	        charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; 
	        if("char".equalsIgnoreCase(charOrNum)){                // 字符串
	            caseOption = random.nextInt(2) % 2 == 0 ? 65 : 97; 
	            code.append((char) (caseOption + random.nextInt(26)));   
	        }else if("num".equalsIgnoreCase(charOrNum)){            // 数字
	            code.append((random.nextInt(10)));   
	        }   
	    }   
	    return code.toString().toLowerCase(); 
	}
	
	public int getRandomDayOfMonth()
	{
		return random.nextInt(30)+1;
	}
	
}