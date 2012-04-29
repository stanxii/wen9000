package com.stan.wen9000.web;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import redis.clients.jedis.Jedis;

import com.stan.wen9000.action.jedis.util.RedisUtil;



public class ServiceAlarmProcessor {

	
	private static Logger log = Logger.getLogger(ServiceAlarmProcessor.class);

	private static final String PERSIST_ALARM_QUEUE_NAME = "alarm_queue";
	private static final String  ALARM_EXPIRE_SECONDS =  "alarm:expire:seconds" ;
	private static final String  ALARM_REALTIME_MAX_NUM =  "alarm:realtime:max" ;
	private static final String ALARM_REALTIME_QUEUE_NAME = "alarm_realtime_queue";
	private static final String ALARM_HISTORY_QUEUE_NAME = "alarm_history_queue";
	

	private static String message = null;
	
	private static RedisUtil redisUtil;
	private static Jedis jedis;
	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceAlarmProcessor.redisUtil = redisUtil;
	}


	public void start() {

		log.info("[#3] ..... service alarm");

		
		try {
			servicestart();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 redisUtil.closeConnection(jedis);
		}

	}

	
	public static void servicestart() throws Exception {

		jedis = redisUtil.getConnection();
		
		while (true) {
			
						
			message = jedis.rpop(PERSIST_ALARM_QUEUE_NAME);
			
			
			
			
			if(message == null ) {
//				System.out.println(" [x] ServiceAlarmProcessor null and will continue ");
				
				Thread.sleep(1000);
				continue;
			}
			
			
			System.out.println(" [x] ServiceAlarmProcessor Received '" + message
					+ "'");
			
			long start = System.currentTimeMillis();  			
			dowork(message);					
			long end = System.currentTimeMillis();  
			System.out.println("one ServiceAlarmProcessor dowork spend: " + ((end - start)) + " milliseconds");  
			
			
		}
		
		

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
			savelarm(alarm);

			
			
			

			
		}

		// System.out.println("save end msg===========================");

	}


	public static void savelarm(Map<String, String> alarm) {
		//presist alarm		
		
		
		//save alarm entity
		Long alarmid = jedis.incr("global:alarmid");
		String salarmid = String.valueOf(alarmid);
		
		String alarmkey = "alarmid:" + salarmid + ":entity";
		jedis.hmset(alarmkey, alarm);
		
		//expire alarm key three months
		int lseconds= 3*30*24*60*60;
		String seconds = jedis.get(ALARM_EXPIRE_SECONDS);
		if(seconds != null)
			 lseconds = Integer.parseInt(seconds);								
		jedis.expire(alarmkey, lseconds);
		
		String smax = jedis.get(ALARM_REALTIME_MAX_NUM);
		int imax = 100;
		if(smax != null)
			imax = Integer.parseInt(smax);
		else{
			jedis.set(ALARM_REALTIME_MAX_NUM, Integer.toString(imax));
		}
		
		//set realtime alarm list queue
		jedis.lpush(ALARM_REALTIME_QUEUE_NAME, salarmid);
		jedis.ltrim(ALARM_REALTIME_QUEUE_NAME, 0, imax -1);
		
		
		//history alarm sorted sets score is timestamp
		Double score = (double) System.currentTimeMillis();
		jedis.zadd(ALARM_HISTORY_QUEUE_NAME, score, salarmid);
		
		
		//publish to notify node.js a new alarm
		jedis.publish("alarm.newalarm", message);
		
		
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
			
			String result = (String)alarm.get("alarmvalue");			
			String cbatmac = (String)alarm.get("cbatmac");			
			String cbatmackey = "cbatmac:" +  cbatmac + ":cbatid";						
			String scbatid = (String)jedis.get(cbatmackey);
			String scbatentitykey = "cbatid:" + scbatid + ":entity";			
			jedis.hset(scbatentitykey, "upgradestatus", result);			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	

	
	
	


	
}