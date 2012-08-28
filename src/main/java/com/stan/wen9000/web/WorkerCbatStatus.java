package com.stan.wen9000.web;

import java.util.Set;

import java.util.Iterator;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;

public class WorkerCbatStatus{	
	private static Logger log = Logger.getLogger(WorkerCbatStatus.class);
	private static RedisUtil redisUtil;
	private static final String CBATSTS_QUEUE_NAME = "cbatsts_queue";
	
	public static void setRedisUtil(RedisUtil redisUtil) {
		WorkerCbatStatus.redisUtil = redisUtil;
	}
	
	private  static JedisPubSub jedissubSub = new JedisPubSub() {
  	  /*

       * 常规模式：关闭订阅时触发

       * arg0 key值

       * arg1 订阅数量

       */

      public void onUnsubscribe(String arg0, int arg1) {

      }

       /*

       * 常规模式：启动订阅时触发

       * arg0 key值

       * arg1 订阅数量

       */

      public void onSubscribe(String arg0, int arg1) {

      }

       /*

       * 常规模式：收到匹配key值的消息时触发

       * arg0 key值

       * arg1 收到的消息值

       */

      public void onMessage(String arg0, String arg1) {

      }

       /*

       * 正则模式：关闭正则类型订阅时触发

       * arg0 key的正则表达式

       * arg1 订阅数量

       */

      public void onPUnsubscribe(String arg0, int arg1) {

      }

       /*

       * 正则模式：启动正则类型订阅时触发

       * arg0 key的正则表达式

       * arg1 订阅数量

       */

      public void onPSubscribe(String arg0, int arg1) {

      }

       /*

       * 正则模式：收到匹配key值的消息时触发

       * arg0订阅的key正则表达式

       * arg1匹配上该正则key值

       * arg2收到的消息值

       */

      public void onPMessage(String arg0, String arg1, String arg2) {

      }

  };
	
	
	private void start(){
		log.info("[#2] ..... WorkerCbatStatus start");
		while(true){
			try{
				servicestart();
				Thread.currentThread().sleep(20000);
			}catch(Exception e){
				
			}
			
		}
		
	}
	
	private void servicestart(){
		//获取所有cbat设备
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//log.info("----------------------------->>>>log testing~~~~");
		String key;
		Set<String> cbats = jedis.keys("cbatid:*:entity");
		for(Iterator it=cbats.iterator();it.hasNext();){
			key = it.next().toString();
			if(jedis.hget(key, "active").equalsIgnoreCase("0")){
				continue;
			}
			//将ip发往server
			jedis.publish("ServiceCbatStatus.cbatkey", key);
			//jedis.lpush(CBATSTS_QUEUE_NAME, key);
		}
		redisUtil.getJedisPool().returnResource(jedis);
	}
}