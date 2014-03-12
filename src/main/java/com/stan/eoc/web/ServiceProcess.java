package com.stan.eoc.web;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.stan.eoc.action.jedis.util.RedisUtil;




public class ServiceProcess{
	private static final String PROCESS_CBAT_QUEUE_NAME = "process_queue";
	
	private static Logger logger = Logger.getLogger(ServiceProcess.class);
	
	private static RedisUtil redisUtil;
	  
	  public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceProcess.redisUtil = redisUtil;
	}

	private static Jedis jedis;
	
	public void execute(){
		logger.info("[#3]ServiceProcess starting");
		
		try{
			servicestart();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void servicestart() throws InterruptedException{
		
		jedis = redisUtil.getConnection();
		
		while (true) {
			String message = null;
			
		
			message = jedis.rpop(PROCESS_CBAT_QUEUE_NAME);
			
			if(message == null) {			
				Thread.sleep(1000);
				continue;
			}
			else if(message.equalsIgnoreCase("ok")) {
				
				System.out.println("Why ServiceDiscoveryProcessor receive == ok?? i don't know");
				Thread.sleep(1000);
				continue;
			}else if(message.length() < 3) {
				
				System.out.println("Why ServiceDiscoveryProcessor receive len < 3 i don't know");
				Thread.sleep(1000);
				continue;
			}
			logger.info(":::::::::::::::serviceProcess");
			String total = "global:discovertotal";
			jedis.set(total, message.substring(8));
			
		}
	}
}

