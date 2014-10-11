package com.stan.eoc.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import com.stan.eoc.web.SnmpUtil;
import com.stan.eoc.action.jedis.util.RedisUtil;

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
	        	
	        }

	    };

	    
	
	//此进程用于判断头端是否下线
	
	private void start(){
		log.info("[#3] ..... ServiceCbatStatus start");
		
		try {
			
		 while(true){
				try{
					servicestart();
					Thread.currentThread().sleep(2000);
					
					
					//tongji publis to web
					doCount();
					
				}catch(Exception e){
					
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}
	
	
	private static void doCount(){		
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		/////do
		
		jedis.getbit("cbat:alives", 0);
		
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	
	private static void servicestart(){

			
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
					dowork(jedis, key);	
										
				}
				
				
				redisUtil.getJedisPool().returnResource(jedis);
							

	}
	
	private static void dowork(Jedis jedis, String message){			
		
		//获取头端时间戳
		long timeticks = Long.parseLong(jedis.hget(message, "timeticks"));
		Date date = new Date();
		long now = date.getTime();
		String cbatmac = jedis.hget(message, "mac");
		String id = jedis.get("mac:"+ cbatmac +":deviceid");
		if(now - timeticks > 375000){
			//log.info("发现设备下线:time====="+date.toString()+"======now==="+now+"======timeticks===="+timeticks+"====timedate==="+new Date(timeticks).toString());
			//确认设备不在线
			jedis.hset(message, "active", "0");
			
			//sum tongji online cbats
			jedis.setbit("cbat:alives", Long.parseLong(id), false);

			//cbat状态有变迁,发往STSCHANGE_QUEUE_NAME
			System.out.println("===now cbatstatus check cbat offline! cbatmac="+ cbatmac);
			Sendstschange("cbat",id,jedis);
			//jedis.lpush(STSCHANGE_QUEUE_NAME, id);
			//置所属CNU下线
			Set<String> cnus = jedis.smembers("cbatid:"+id + ":cnus");

			for(Iterator it=cnus.iterator();it.hasNext();){
				String cnuid = it.next().toString();
				String cnukey = "cnuid:"+cnuid+":entity";
				jedis.hset(cnukey, "active", "0");

				//sum tongji online cbats
				jedis.setbit("cnu:alives", Long.parseLong(cnuid), false);
				
				//cnu状态有变迁,发往STSCHANGE_QUEUE_NAME
				Sendstschange("cnu",cnuid,jedis);
				//jedis.lpush(STSCHANGE_QUEUE_NAME, cnuid);
			}
			//产生下线告警信息
			Map<String, String> alarmhash = new LinkedHashMap();
			alarmhash.put("runingtime", "N/A");
			alarmhash.put("oid", "N/A");
			alarmhash.put("alarmcode", "200921");		
			alarmhash.put("cbatmac", jedis.hget(message, "mac")); 		
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
			String alarmtimes = format.format(date);
			alarmhash.put("salarmtime", alarmtimes);
			alarmhash.put("alarmlevel", "1");
			alarmhash.put("cnalarminfo", "Mac为"+ jedis.hget(message, "mac") +"的头端下线");
			alarmhash.put("enalarminfo", "Mac:"+ jedis.hget(message, "mac") +"  Master offline!");
			
			String msgservice = JSONValue.toJSONString(alarmhash);
			jedis.publish("servicealarm.new", msgservice);			
		}
		
		
		
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
			json.put("cbatmac", jedis.hget("cbatid:"+cbatid+":entity","mac"));
			json.put("srcmac", "");
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