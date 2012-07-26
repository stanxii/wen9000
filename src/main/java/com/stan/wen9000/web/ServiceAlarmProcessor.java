package com.stan.wen9000.web;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;



public class ServiceAlarmProcessor {

	
	private static Logger log = Logger.getLogger(ServiceAlarmProcessor.class);

 
	private static final String  ALARM_EXPIRE_SECONDS =  "alarm:expire:seconds" ;
	private static final String  ALARM_REALTIME_MAX_NUM =  "alarm:realtime:max" ;
	private static final String ALARM_REALTIME_QUEUE_NAME = "alarm_realtime_queue";
	private static final String ALARM_HISTORY_QUEUE_NAME = "alarm_history_queue";
	

	private static SnmpUtil util = new SnmpUtil();
	private static RedisUtil redisUtil;

	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceAlarmProcessor.redisUtil = redisUtil;
	}
	
	
	private  static JedisPubSub jedissubSub = new JedisPubSub() {
		public void onUnsubscribe(String arg0, int arg1) {

        }
		public void onSubscribe(String arg0, int arg1) {

        }
		 public void onMessage(String arg0, String arg1) {
	       
	     }
		 public void onPUnsubscribe(String arg0, int arg1) {

	        }
		 public void onPSubscribe(String arg0, int arg1) {

	        } 
  	 
       /*

       * 正则模式：收到匹配key值的消息时触发

       * arg0订阅的key正则表达式

       * arg1匹配上该正则key值

       * arg2收到的消息值

       */

      public void onPMessage(String arg0, String arg1, String msg) {

      	System.out.println("[x]ServiceAlarmProcesser  Subscribing....pmessage....now receive on msgarge1 [" + arg1 + "] arg2=["+msg +"]");
      	try {
  			//arg2 is mssage now is currenti p
  			
  			
  			
  			servicestart(msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();			
  		}
  		
      }

  };
  


	public void start() {

		log.info("[#3] ..... service alarm");

		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		//删除3个月以前的告警信息
		 log.info("Del alarms which 3 months ago.............................START");
		 Double now = (double) System.currentTimeMillis();
		 long lseconds= 3*30*24*60*60*1000000;
		 Set<String> alarms = jedis.zrangeByScore(ALARM_HISTORY_QUEUE_NAME, 0, (now - lseconds));
		 //Set<String> alarms = jedis.zrangeByScore(ALARM_HISTORY_QUEUE_NAME, 0, now );

		 for(Iterator it = alarms.iterator();it.hasNext(); ){
			 String alarmid = it.next().toString();
			 jedis.del("alarmid:"+alarmid+":entity");
			 jedis.zrem(ALARM_HISTORY_QUEUE_NAME, alarmid);
		 }
		 log.info("Del alarms which 3 months ago.............................END");
		 
		 jedis.psubscribe(jedissubSub, "servicealarm.*");
		 
		redisUtil.getJedisPool().returnResource(jedis);
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
		
		
		
		
	}

	
	public static void servicestart(String message) throws Exception {

		//	log.info(" [x] ServiceAlarmProcessor Received '" + message
		//			+ "'");
			
		//	long start = System.currentTimeMillis();  			
			dowork(message);					
		//	long end = System.currentTimeMillis();  
		//	log.info("one ServiceAlarmProcessor dowork spend: " + ((end - start)) + " milliseconds");  
			
		
	}
	
	

	@SuppressWarnings("unchecked")
	public static void dowork(String message) throws ParseException {
		
		
		
		JSONParser parser = new JSONParser();
		
		ContainerFactory containerFactory = new ContainerFactory(){
		    public List<?> creatArrayContainer() {
		      return new LinkedList<Object>();
		    }

		    public Map<?, ?> createObjectContainer() {
		      return new LinkedHashMap<Object, Object>();
		    }
		                        
		  };
		                
		  
		Map<String, String> alarm = (Map<String, String>)parser.parse(message, containerFactory);
		    
		
		
		String alarmcode =(String) alarm.get("alarmcode");
		
		
		
		int code = Integer.parseInt(alarmcode);
		if (code == 200002) {
//			HeartBean heart = new HeartBean();
//			int index1 = 0;
//			int index2 = 0;
//			try {
//				index1 = message.indexOf(";");
//				heart.setCode(Integer.parseInt(message.substring(0, index1)));
//
//				index2 = message.indexOf(";", index1 + 1);
//				heart.setCbatsys(message.substring(index1 + 1, index2));
//
//				index1 = index2;
//				index2 = message.indexOf(";", index1 + 1);
//				heart.setCnusys(message.substring(index1 + 1, index2));
//
//				index1 = index2;
//				heart.setInfo(message.substring(index1 + 1));
//
//				doheart(heart);
//				return;
//			} catch (Exception e) {
//				e.printStackTrace();
//				return;
//			}
//		
		} else {
			doalarm(alarm);			
			savelarm(message, alarm);
	
		}

		// System.out.println("save end msg===========================");

	}


	public static void savelarm(String message, Map<String, String> alarm) {
		//presist alarm		
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		
		
		}catch(Exception e){
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			log.info("------>>>>>>>savelarm ex1<<<<<<<<<----------");
		}
		
		
		//save alarm entity
		Long alarmid = jedis.incr("global:alarmid");
		String salarmid = String.valueOf(alarmid);
		
		String alarmkey = "alarmid:" + salarmid + ":entity";
		jedis.hmset(alarmkey, alarm);
		
		//history alarm sorted sets score is timestamp
		Double score = (double) System.currentTimeMillis();
		jedis.zadd(ALARM_HISTORY_QUEUE_NAME, score, salarmid);
		

		//publish to notify node.js a new alarm
		jedis.publish("node.alarm.newalarm", message);
		
		
		redisUtil.getJedisPool().returnResource(jedis);
		
	}

	public static void doalarm(Map<String, String> alarm) {
		
		String alarmcode =(String) alarm.get("alarmcode");		
		int code = Integer.parseInt(alarmcode);
		
		switch (code) {
		case 200909: {
			doupgrade(alarm);
			
			break;
		}
		default:
		
			break;
		}
	}


	public static void doupgrade(Map<String, String> alarm) {
		try {		

			Jedis jedis=null;
			try {
				jedis = redisUtil.getConnection();
			}catch(Exception e){
				redisUtil.getJedisPool().returnBrokenResource(jedis);
			}
			
			String result = (String)alarm.get("alarmvalue");			
			String cbatmac = (String)alarm.get("cbatmac");			
			String cbatid = jedis.get("mac:" +  cbatmac + ":deviceid");						
			String cbatkey = "cbatid:" + cbatid + ":entity";			
			jedis.hset(cbatkey, "upgrade", result);	
			if(result.equalsIgnoreCase("0")){
				//升级成功
				//清空版本信息
				jedis.hset("cbatid:"+cbatid+":cbatinfo", "appver", "");
				
			}
			if(!result.equalsIgnoreCase("1")){
				//已升级头端加1
				long num_t =jedis.incr("global:updated");
				String num = String.valueOf(num_t);
				//String total = jedis.get("global:updatedtotal");
				//通知前端此头端完成升级
				String total = jedis.get("global:updatedtotal");
				JSONObject json = new JSONObject();
				json.put("proc", num);
				json.put("total", total);
				jedis.publish("node.opt.updateproc", json.toJSONString());
			}			
			
			redisUtil.getJedisPool().returnResource(jedis);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}