package com.stan.wen9000.web;


import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;
import com.stan.wen9000.reference.EocDeviceType;

public class ServiceDiscoveryProcessor  {





	EocDeviceType devicetype;

	
	  private static RedisUtil redisUtil;
	  
	  public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceDiscoveryProcessor.redisUtil = redisUtil;
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

      	System.out.println("servicediscovery Subscribing....pmessage....now receive on msgarge1 [" + arg1 + "] arg2=["+msg +"]");
      	try {
  			//arg2 is mssage now is currenti p
  			
  			
  			
  			servicestart(msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();			
  		}
  		
      }

  };

	public void execute() {

		System.out.println("[#3] ..... service discovery starting");
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 jedis.psubscribe(jedissubSub, "servicediscovery.*");
		redisUtil.getJedisPool().returnResource(jedis);
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
		

		

	}

	public static void servicestart(String msg) throws Exception {

			 
		doWork(msg);
			
		
		
	}

	private static void doWork(String message) throws Exception {

		JSONObject json=(JSONObject) JSONValue.parse(message);
		
		
		String msgtype =(String) json.get("msgcode");

		if (msgtype.equalsIgnoreCase("001")) {
			doCbat(message);
		} else if (msgtype.equalsIgnoreCase("002")) {
			// new cnu
			// doCnu(message);
		} else if (msgtype.equalsIgnoreCase("003")) {
			 doHfc(message);
		} else {
			System.out.println("unknow msg to service");
		}

	}

	private static void doCbat(String message) throws ParseException {

		
		
		JSONParser parser = new JSONParser();
		
		ContainerFactory containerFactory = new ContainerFactory(){
		    public List creatArrayContainer() {
		      return new LinkedList();
		    }

		    public Map createObjectContainer() {
		      return new LinkedHashMap();
		    }
		                        
		  };
		                
		  
		Map jsonobj = (Map)parser.parse(message, containerFactory);
		    
		
		
		String cbatip =(String) jsonobj.get("ip");
		String cbatmac =(String) jsonobj.get("cbatmac");
		String cbatdevicetype =(String) jsonobj.get("cbatdevicetype");
		
		
		String agentport =(String) jsonobj.get("cbatinfo:agentport");
		String appver =(String) jsonobj.get("cbatinfo:appver");
		String mvlanid =(String) jsonobj.get("cbatinfo:mvlanid");
		String mvlanenable =(String) jsonobj.get("cbatinfo:mvlanenable");
		

		long start = System.currentTimeMillis();  
		
		
		
		String cbatmackey = "mac:" +  cbatmac.toLowerCase().trim() + ":deviceid";
		
		
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		
		
		}catch(Exception e){
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
		//发现新头端，通知前端		
		JSONObject json = new JSONObject();
		json.put("mac", cbatmac);
		json.put("active", "1");
		json.put("ip", cbatip);
		switch(Integer.parseInt(cbatdevicetype))
		{
        	case 1:
        		//break;
        	case 2:
        		
        		//break;
        	case 3:
        		//break;
        	case 4:
        		
        		//break;
        	case 5:
        		//break;
        	case 6:
        		
        		//break;
        	case 7:
        		//break;
        	case 8:
        		json.put("devtype", "中文测试");
        		break;
        	default:
        		json.put("devtype", "Unknown");
        		break;
		}
		jedis.publish("node.dis.findcbat", json.toJSONString());
		
		//get cbatmac if exist in redis server
		String scbatid = jedis.get(cbatmackey);		
		
				
		long icbatid ;
		
		if(scbatid == null) {
			icbatid = jedis.incr("global:deviceid");		
			jedis.set(cbatmackey, Long.toString(icbatid) );
		}else {
			icbatid = Long.parseLong(scbatid);
					
		}
		
	      
	    
		String scbatentitykey = "cbatid:" + icbatid + ":entity";
		Map<String , String >  cbatentity = new HashMap<String, String>();
		
		cbatentity.put("deviceclass", "cbat");
		cbatentity.put("mac", cbatmac.toLowerCase().trim());
		cbatentity.put("active", "1");
		cbatentity.put("ip", cbatip.toLowerCase().trim());
		cbatentity.put("label", cbatmac.toLowerCase().trim());
		cbatentity.put("devicetype", cbatdevicetype.toLowerCase().trim());
		//20 not have upgradestatus
		cbatentity.put("upgradestatus", "20");
		
		jedis.hmset(scbatentitykey, cbatentity);
	    
		//更新头端时间戳
		Date date = new Date();
		long time = date.getTime();
		jedis.hset(scbatentitykey, "timeticks", String.valueOf(time));
/////////////////////////////save cbatinfo
	    
	    
		
		Map<String , String >  hash = new HashMap<String, String>();
		 
		String scbatinfokey = "cbatid:" + icbatid + ":cbatinfo";
		hash.put("address", "na");
		hash.put("phone", "13988777");
		hash.put("bootver", "cml-boot-v1.1.0_for_linux_sdk");
		hash.put("contact", "na");
		hash.put("agentport", agentport);
		hash.put("appver", appver);
		hash.put("mvlanid", mvlanid);
		hash.put("mvlanenable", mvlanenable);
		
		jedis.hmset(scbatinfokey, hash);
		// hmset cnuid:1 cnuid 1 mac 30:71:b2:88:88:01 label 
		 		
				
		long end = System.currentTimeMillis();  
		System.out.println("one cbat and cbat info SET: " + ((end - start)) + " milliseconds");  
		
		redisUtil.getJedisPool().returnResource(jedis);

	}
	
	@SuppressWarnings("rawtypes")
	private static void doHfc(String message) throws ParseException {

		JSONParser parser = new JSONParser();
		
		ContainerFactory containerFactory = new ContainerFactory(){
		    public List creatArrayContainer() {
		      return new LinkedList();
		    }

		    public Map createObjectContainer() {
		      return new LinkedHashMap();
		    }
		                        
		  };
		                
		  
		Map jsonobj = (Map)parser.parse(message, containerFactory);
		    
		
		String ip =(String) jsonobj.get("ip");
		String oid =(String) jsonobj.get("oid");
		String hfcmac =(String) jsonobj.get("hfcmac");
		String hfctype =(String) jsonobj.get("hfctype");
		String version =(String) jsonobj.get("version");
		String logicalid =(String) jsonobj.get("logicalid");
		String modelnumber =(String) jsonobj.get("modelnumber");
		String serialnumber =(String) jsonobj.get("serialnumber");
		
		String hfckey = "mac:" +  hfcmac.toLowerCase().trim() + ":deviceid";
		
		Jedis jedis=null;
		try {
			 jedis = redisUtil.getConnection();
			
			
			}catch(Exception e){
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				
			}
		
		//get hfcmac if exist in redis server
		String shfcid = jedis.get(hfckey);
		
		long hfcid ;
		
		if(shfcid == null) {
			hfcid = jedis.incr("global:deviceid");
			jedis.set(hfckey, Long.toString(hfcid) );
		}else {
			hfcid = Long.parseLong(shfcid);			
		}
		
		String shfcentitykey = "hfcid:" + hfcid + ":entity";
		Map<String , String >  hfcentity = new HashMap<String, String>();
		 
		hfcentity.put("mac", hfcmac.toLowerCase().trim());
		hfcentity.put("oid", oid);
		hfcentity.put("ip", ip.toLowerCase().trim());
		hfcentity.put("hfctype", hfctype.toLowerCase().trim());
		hfcentity.put("version", version.toLowerCase().trim());
		hfcentity.put("logicalid", logicalid.toLowerCase().trim());
		hfcentity.put("modelnumber", modelnumber.toLowerCase().trim());
		hfcentity.put("serialnumber", serialnumber.toLowerCase().trim());
		
		jedis.hmset(shfcentitykey, hfcentity);
		
		jedis.save();
		
		
		redisUtil.getJedisPool().returnResource(jedis);
	}

}
