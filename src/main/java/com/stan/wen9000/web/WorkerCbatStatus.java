package com.stan.wen9000.web;

import java.util.Set;

import java.util.Iterator;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.stan.wen9000.action.jedis.util.RedisUtil;

public class WorkerCbatStatus{	
	private static Logger log = Logger.getLogger(WorkerCbatStatus.class);
	private static JedisPool pool;
	private static RedisUtil redisUtil;
	private static final String CBATSTS_QUEUE_NAME = "cbatsts_queue";
	
	public static void setRedisUtil(RedisUtil redisUtil) {
		WorkerCbatStatus.redisUtil = redisUtil;
	}
	
	static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(100);
        config.setMaxIdle(20);
        config.setMaxWait(1000);
        config.setTestOnBorrow(true);
        pool = new JedisPool(config, "192.168.1.249");
    }
	
	
	private void start(){
		log.info("[#2] ..... WorkerCbatStatus start");
		while(true){
			try{
				servicestart();
				Thread.sleep(20000);
			}catch(Exception e){
				
			}
			
		}
		
	}
	
	private void servicestart(){
		//获取所有cbat设备
		Jedis jedis = pool.getResource();
		String key;
		Set<String> cbats = jedis.keys("cbatid:*:entity");
		for(Iterator it=cbats.iterator();it.hasNext();){
			key = it.next().toString();
			if(jedis.hget(key, "active").equalsIgnoreCase("0")){
				continue;
			}
			//将ip发往server
			jedis.lpush(CBATSTS_QUEUE_NAME, key);
		}
		pool.returnResource(jedis);	
	}
}