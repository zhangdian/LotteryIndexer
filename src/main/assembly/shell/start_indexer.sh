#!/bin/bash

DEPLOY_HOME=`echo $(dirname $(pwd))`

CONFIG_DIR=$DEPLOY_HOME/conf/conf_ssh_indexer
LIB_DIR=$DEPLOY_HOME/lib
LIB_JARS=`ls  $LIB_DIR | grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`

MEM=`free -m | grep Mem |awk '{print $2}'`
deployMem=`echo "$MEM*1/10" |bc`m
#deployMem=128m
JAVA_MEM_OPTS="-server -Xms$deployMem -Xmx$deployMem -XX:PermSize=128m -XX:SurvivorRatio=2 -XX:+UseParallelGC"

#echo $JAVA_MEM_OPTS
#echo $LIB_JAR
echo -e "Starting SSHIndexer process...\c"

#nohup java $JAVA_MEM_OPTS -classpath $CONFIG_DIR:$LIB_JARS com.bd17kaka.LotteryIndexer.api.SSHIndexer $1 $2 &
nohup java -classpath $CONFIG_DIR:$LIB_JARS com.bd17kaka.LotteryIndexer.api.SSHIndexer $1 $2 &

PID=`ps aux|grep SSHIndexer|grep java|awk '{print $2}'`
echo -e "\nOK! SSHIndexer pid=$PID ."