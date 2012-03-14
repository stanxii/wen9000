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
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.stan.wen9000.action.jedis.util.RedisUtil;
import com.stan.wen9000.reference.EocDeviceType;
import com.stan.wen9000.service.CbatService;
import com.stan.wen9000.service.CbatinfoService;

public class ServiceDiscoveryProcessor  {



	@Autowired
	CbatService cbatsv;

	@Autowired
	CbatinfoService cbatinfosv;

	EocDeviceType devicetype;

	private static final String PERSIST_CBAT_QUEUE_NAME = "service_discovery_queue";
	private static SnmpUtil util = new SnmpUtil();
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
		
//		 System.out.println(" [x]ServiceDiscoveryProcessor  Received '" +
//		 message + "'");
		 
		 
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
			// doHfc(message);
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
		
		
		
		String cbatmackey = "cbatmac:" +  cbatmac.toLowerCase().trim() + ":cbatid";
		
		//get cbatmac if exist in redis server
		String scbatid = jedis.get(cbatmackey);		
		
				
		long cbatid ;
		long cbatinfoid ;
		
		if(scbatid == null) {
			cbatid = jedis.incr("global:cbatid");
			jedis.set("global:cbatinfoid", Long.toString(cbatid));
			cbatinfoid =  cbatid;
			jedis.set(cbatmackey, Long.toString(cbatid) );
		}else {
			cbatid = Long.parseLong(scbatid);
			cbatinfoid = cbatid ;
			
			
		}
		
	      
	    
		String scbatentitykey = "cbatid:" + cbatid + ":entity";
		Map<String , String >  cbatentity = new HashMap<String, String>();
		 
		cbatentity.put("mac", cbatmac.toLowerCase().trim());
		cbatentity.put("active", "1");
		cbatentity.put("ip", cbatip.toLowerCase().trim());
		cbatentity.put("label", cbatmac.toLowerCase().trim());
		cbatentity.put("devicetype", cbatdevicetype.toLowerCase().trim());
		
		jedis.hmset(scbatentitykey, cbatentity);
	    
/////////////////////////////save cbatinfo
	    
	    
		
		Map<String , String >  hash = new HashMap<String, String>();
		 
		String scbatinfokey = "cbatid:" + cbatid + ":cbatinfo";
		hash.put("address", "na");
		hash.put("phone", "13988777");
		hash.put("bootver", "cml-boot-v1.1.0 for linux sdk");
		hash.put("contact", "na");
		hash.put("agentport", agentport);
		hash.put("appver", appver);
		hash.put("mvlanid", mvlanid);
		hash.put("mvlanenable", mvlanenable);
		
		jedis.hmset(scbatinfokey, hash);
		// hmset cnuid:1 cnuid 1 mac 30:71:b2:88:88:01 label fuckyou
		 		
				
		long end = System.currentTimeMillis();  
		System.out.println("one cbat and cbat info SET: " + ((end - start)) + " milliseconds");  

	}

}
