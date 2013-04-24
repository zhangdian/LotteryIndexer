服务器上还没有同步DB-REDIS

###############################################################################
注：只考虑当前球和他后面一个球的情况，也就是"马尔科夫假设的二元模型".

1. File2DBSyncSSH：原始数据过滤得到按年份分组的文件，同时要将每个开奖记录保存到DB的ssh_result表

2. SSHIndexer：将按年份的文件中的开奖记录，建立索引到Redis。同时将每个组合出现的次数同步到ssh_new_combination中

3. DB2RedisSyncSSH:combination_num_by_length_firstnum；
	将每个组合出现的次数从DB的ssh_new_combination同步到Redis
	
4. 古德-图灵估计需要计算N(r),N(r+1)，意思分别是出现r次的组合的个数，出现(r+1)次组合的个数
	从DB中将这两个数据计算到Redis
	

