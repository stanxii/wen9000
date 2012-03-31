package com.stan.wen9000.web;


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

import com.stan.wen9000.action.jedis.util.RedisUtil;
import com.stan.wen9000.reference.EocDeviceType;

public class ServiceDiscoveryProcessor  {





	EocDeviceType devicetype;

	private static final String PERSIST_CBAT_QUEUE_NAME = "service_discovery_queue";
//	private static JedisPool pool;
	 
	
	  private static RedisUtil redisUtil;
	  
	  public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceDiscoveryProcessor.redisUtil = redisUtil;
	}

	private static Jedis jedis;
	  
	  
//	 static {
//	        JedisPoolConfig config = new JedisPoolConfig();
//	        config.setMaxActive(1000);
//	        config.setMaxIdle(20);
//	        config.setMaxWait(1000);	        
//	        pool = new JedisPool(config, "192.168.1.249", 6379, 10*1000);
//	    }
//	 
//	 
//	 private static Jedis jedis ;

	public void execute() {

		System.out.println("[#3] ..... service discovery starting");

		try {
			servicestart();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void servicestart() throws Exception {

		
		
		jedis = redisUtil.getConnection();
		
		while (true) {
		String message = null;

	
		message = jedis.rpop(PERSIST_CBAT_QUEUE_NAME);
		
		
		if(message == null) {
			
			
			Thread.sleep(1000);
			continue;
		}
		else if(message.equalsIgnoreCase("ok")) {
			
//			System.out.println("Why ServiceDiscoveryProcessor receive == ok?? i don't know");
			Thread.sleep(1000);
			continue;
		}else if(message.length() < 3) {
			
//			System.out.println("Why ServiceDiscoveryProcessor receive len < 3 i don't know");
			Thread.sleep(1000);
			continue;
		}
		
		 System.out.println(" [x]ServiceDiscoveryProcessor  Received '" +
		 message + "'");
		 
		 
		doWork(message);
		// System.out.println(" [x] ServiceDiscoveryProcessor  Done");
		
		}
		
	
		
		
		
	}

	private void doWork(String message) throws ParseException {

		
		 
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

	private void doCbat(String message) throws ParseException {

		
		
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

	}
	
	@SuppressWarnings("rawtypes")
	private void doHfc(String message) throws ParseException {

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
	}

}
