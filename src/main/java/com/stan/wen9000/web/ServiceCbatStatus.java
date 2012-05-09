package com.stan.wen9000.web;

import java.util.Date;
import java.util.Set;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.web.SnmpUtil;
import com.stan.wen9000.action.jedis.util.RedisUtil;

public class ServiceCbatStatus{	

	private static Logger log = Logger.getLogger(ServiceCbatStatus.class);

	private static RedisUtil redisUtil;
	private static Jedis jedis = null;
	  
	
	private static SnmpUtil util = new SnmpUtil();
	private static final String STSCHANGE_QUEUE_NAME = "stschange_queue";
	private static  String CBATSTS_QUEUE_NAME = "cbatsts_queue";
	
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceCbatStatus.redisUtil = redisUtil;
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
	        	try {
	      			//arg2 is mssage now is currenti p
	      			
	      			
	      			
	      			servicestart(arg1, arg2);
	      			
	      		}catch(Exception e){
	      			e.printStackTrace();			
	      		}
	        }

	    };

	    
	
	//此进程用于判断头端是否下线
	
	private void start(){
		log.info("[#3] ..... ServiceCbatStatus start");
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		 jedis.psubscribe(jedissubSub, "ServiceCbatStatus.*");
		redisUtil.getJedisPool().returnResource(jedis);
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}
	
	
	
	private static void servicestart(String pat, String message){

			
			//System.out.println(" [x] ServiceCbatStatus Received '" + message
			//		+ "'");
						
			dowork(message);					

	}
	
	private static void dowork(String message){	
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		//获取头端时间戳
		long timeticks = Long.parseLong(jedis.hget(message, "timeticks"));
		Date date = new Date();
		long now = date.getTime();
		String id = jedis.get("mac:"+jedis.hget(message, "mac")+":deviceid");
		if(now - timeticks > 75000){
			//确认设备不在线
			jedis.hset(message, "active", "0");

			//cbat状态有变迁,发往STSCHANGE_QUEUE_NAME
			Sendstschange("cbat",id,jedis);
			//jedis.lpush(STSCHANGE_QUEUE_NAME, id);
			//置所属CNU下线
			Set<String> cnus = jedis.smembers("cbatid:"+id + ":cnus");

			for(Iterator it=cnus.iterator();it.hasNext();){
				String cnuid = it.next().toString();
				String cnukey = "cnuid:"+cnuid+":entity";
				jedis.hset(cnukey, "active", "0");

				//cnu状态有变迁,发往STSCHANGE_QUEUE_NAME
				Sendstschange("cnu",cnuid,jedis);
				//jedis.lpush(STSCHANGE_QUEUE_NAME, cnuid);
			}
			redisUtil.getJedisPool().returnResource(jedis);
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
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}

		if(devtrapserverip==""){
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}
		
		//如果global:trapserver:ip键不存在，创建之
		if(jedis.get("global:trapserver:ip")==null){
			jedis.set("global:trapserver:ip", "192.168.223.253");
			jedis.set("global:trapserver:port", "162");
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
				redisUtil.getJedisPool().returnBrokenResource(jedis);
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
				redisUtil.getJedisPool().returnBrokenResource(jedis);
			}
		}
		redisUtil.getJedisPool().returnResource(jedis);
		
	}
	
	private static void Sendstschange(String type,String devid,Jedis jedis){ 
		JSONObject json = new JSONObject();
		if(type == "cbat"){
			String cbatkey = "cbatid:"+devid+":entity";
			json.put("mac", jedis.hget(cbatkey,"mac"));
			json.put("online", jedis.hget(cbatkey,"active"));
			json.put("ip", jedis.hget(cbatkey,"ip"));
			json.put("type", "cbat");
		}else if(type == "cnu"){
			String cbatid = jedis.hget("cnuid:"+devid+":entity","cbatid");
			String cnukey = "cnuid:"+devid+":entity";
			json.put("mac", jedis.hget(cnukey,"mac"));
			json.put("online", jedis.hget(cnukey,"active"));
			json.put("cbatmac", jedis.hget(cnukey,jedis.hget("cbatid:"+cbatid+":entity","mac")));
			json.put("type", "cnu");
			
		}else if(type == "hfc"){
			String hfckey = "hfcid:"+devid+":entity";
			json.put("mac", jedis.hget(hfckey,"mac"));
			json.put("active", jedis.hget(hfckey,"active"));
			json.put("ip", jedis.hget(hfckey,"ip"));
			json.put("type", "hfc");
			json.put("sn", jedis.hget(hfckey,"serialnumber"));
			json.put("hp", jedis.hget(hfckey,"hfctype"));
			json.put("id", jedis.hget(hfckey,"logicalid"));
			
		}
		String jsonString = json.toJSONString(); 
	    jedis.publish("node.tree.statuschange", jsonString);
	}
}