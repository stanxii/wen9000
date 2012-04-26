package com.stan.wen9000.web;

import java.util.Date;
import java.util.Set;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.stan.wen9000.web.SnmpUtil;
import com.stan.wen9000.action.jedis.util.RedisUtil;

public class ServiceCbatStatus{	
	private static Logger log = Logger.getLogger(ServiceAlarmProcessor.class);
	private static JedisPool pool;
	private static RedisUtil redisUtil;
	private static SnmpUtil util = new SnmpUtil();
	private static final String STSCHANGE_QUEUE_NAME = "stschange_queue";
	private static final String CBATSTS_QUEUE_NAME = "cbatsts_queue";
	
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceCbatStatus.redisUtil = redisUtil;
	}
	
	static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(100);
        config.setMaxIdle(20);
        config.setMaxWait(1000);
        config.setTestOnBorrow(true);
        pool = new JedisPool(config, "127.0.0.1");
    }
	
	//此进程用于判断头端是否下线
	
	private void start(){
		log.info("[#3] ..... ServiceCbatStatus start");
		
		try{
			servicestart();
		}catch(Exception e){
			
		}
		
	}
	
	private void servicestart(){
		while(true){
			String message = "";
			Jedis jedis = redisUtil.getConnection();
			message = jedis.rpop(CBATSTS_QUEUE_NAME);
			redisUtil.closeConnection(jedis);
			
			if(message == null ) {	
				try{
					Thread.sleep(1000);
					continue;
				}catch(Exception e){
					
				}
			}
			
			//System.out.println(" [x] ServiceCbatStatus Received '" + message
			//		+ "'");
						
			dowork(message);					
			
			//log.info("[###] ..... ServiceCbatStatus Done");
		}
	}
	
	private void dowork(String message){
		Jedis jedis = pool.getResource();
		//获取头端时间戳
		long timeticks = Long.valueOf(jedis.hget(message, "timeticks"));
		Date date = new Date();
		long now = date.getTime();
		String id = jedis.get("mac:"+jedis.hget(message, "mac")+":deviceid");
		if(now - timeticks > 75000){
			//确认设备不在线
			jedis.hset(message, "active", "0");

			//cbat状态有变迁,发往STSCHANGE_QUEUE_NAME
			jedis.lpush(STSCHANGE_QUEUE_NAME, id);
			//置所属CNU下线
			Set<String> cnus = jedis.smembers("cbatid:"+id + ":cnus");

			for(Iterator it=cnus.iterator();it.hasNext();){
				String cnuid = it.next().toString();
				String cnukey = "cnuid:"+cnuid+":entity";
				jedis.hset(cnukey, "active", "0");

				//cnu状态有变迁,发往STSCHANGE_QUEUE_NAME
				jedis.lpush(STSCHANGE_QUEUE_NAME, cnuid);
			}
			return;
		}
		
		//判断并修改设备trapserver ip/port
		String devtrapserverip = null;
		Integer trap_port = 0;
		String cbatip = jedis.hget(message, "ip");
		String cbatinfokey = "cbatid:"+id+":cbatinfo";
		try {
			devtrapserverip = util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
			trap_port = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,7,0}));
		}
		catch(Exception e){
			return;
		}

		if(devtrapserverip==""){
			return;
		}
		//if systemconfig db trap ip = device trap ip not need set trap server ip
		if( !jedis.get("global:trapserver:ip").equalsIgnoreCase(devtrapserverip)){
			try {
				//set trap server ip
			util.setV2StrPDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}), 
					jedis.get("global:trapserver:ip")
					);
			//save
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), 
					new Integer32(1)
					);
			
			jedis.hset(cbatinfokey, "trapserver", devtrapserverip);
			//reset
			/*
			util.setV2PDU(currentip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,6,1,0}), 
					new Integer32(1)
					);
			 */
			 		
			}catch(Exception e){
				//e.printStackTrace();
			}
		}
		//if trap port != systemconfig db trap port
		if(trap_port != Integer.valueOf(jedis.get("global:trapserver:port")))
		{
			try {
				//set trap server ip
				util.setV2PDU(cbatip,
						"161",
						new OID(new int[] {1,3,6,1,4,1,36186,8,2,7,0}), 
						new Integer32(Integer.valueOf(jedis.get("global:trapserver:port")))
						);
				//save
				util.setV2PDU(cbatip,
						"161",
						new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), 
						new Integer32(1)
						);
				
				jedis.hset(cbatinfokey, "agentport", String.valueOf(trap_port));
			}catch(Exception e){
				
			}
		}
		pool.returnResource(jedis);	
	}
}