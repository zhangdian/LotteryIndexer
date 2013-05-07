#!/bin/bash

DEPLOY_HOME=`echo $(dirname $(pwd))`

CONFIG_DIR=$DEPLOY_HOME/conf/list_topn_probability_combination
LIB_DIR=$DEPLOY_HOME/lib
LIB_JARS=`ls  $LIB_DIR | grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`

#MEM=`free -m | grep Mem |awk '{print $2}'`
#deployMem=`echo "$MEM*1/10" |bc`m
#deployMem=128m
#JAVA_MEM_OPTS="-server -Xms$deployMem -Xmx$deployMem -XX:PermSize=128m -XX:SurvivorRatio=2 -XX:+UseParallelGC"

#echo $JAVA_MEM_OPTS
#echo $LIB_JAR
echo -e "Starting ListTopNCombination process...\c"

nohup java $JAVA_MEM_OPTS -classpath $CONFIG_DIR:$LIB_JARS com.bd17kaka.LotterySearcher.api.ListTopNCombination $1 $2 &
#nohup java -classpath $CONFIG_DIR:$LIB_JARS -jar LotterySearcher-0.0.1-SNAPSHOT.jar &

PID=`ps aux|grep ListTopNCombination |grep java|awk '{print $2}'`
echo -e "\nOK! ListTopNCombination pid=$PID ."