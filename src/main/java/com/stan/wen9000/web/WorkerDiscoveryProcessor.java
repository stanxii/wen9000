package com.stan.wen9000.web;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.snmp4j.smi.OID;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.stan.wen9000.action.jedis.foo.MyListener;
import com.stan.wen9000.action.jedis.util.RedisUtil;
import com.stan.wen9000.action.jedis.util.SingletonContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class WorkerDiscoveryProcessor{
	
	private static Logger logger = Logger.getLogger(WorkerDiscoveryProcessor.class);

	private static final String DISCOVERY_QUEUE_NAME = "discovery_queue";

	private static final String PERSIST_CBAT_QUEUE_NAME = "service_discovery_queue";

	private static SnmpUtil util = new SnmpUtil();


	
	
    private static RedisUtil redisUtil;
   
 


	public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	public static void setRedisUtil(RedisUtil redisUtil) {
		WorkerDiscoveryProcessor.redisUtil = redisUtil;
	}

	
    
    
  
	
//	private static JedisPool pool;
//	 static {
//	        JedisPoolConfig config = new JedisPoolConfig();
//	        config.setMaxActive(1000);
//	        config.setMaxIdle(20);
//	        config.setMaxWait(1000);	        
//	        pool = new JedisPool(config, "192.168.1.249", 6379, 10*1000);
//	    }
//	 

	public void execute() {
//		System.out.println(" [x2] WorkerDiscoveryProcessor Start......");
		logger.info(" [x2] WorkerDiscoveryProcessor Start......");

		try {
			servicestart();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void servicestart() throws Exception {

		
//		ApplicationContext ac = new ClassPathXmlApplicationContext("beans-config.xml");
//		//BeanFactory factory  = new XmlBeanFactory((Resource) new ClassPath("beans-config.xml"));
//		RedisUtil ru = (RedisUtil) ac.getBean("redisUtil", RedisUtil.class);
//		
		
		
//		jedis = pool.getResource();
		
		
		while (true) {
			String message = null;
			
			Jedis jedis = redisUtil.getConnection();
			
			message = jedis.rpop(DISCOVERY_QUEUE_NAME);
			
			 redisUtil.closeConnection(jedis);
			
//			System.out.println(" [x] WorkerDiscoveryProcessor what message and len[" + message + "]  " );
			
			if(message == null ) {
//				System.out.println(" [x] WorkerDiscoveryProcessor null and will continue ");
				
				Thread.sleep(1000);
				continue;
			}
			
			
//			System.out.println(" [x] WorkerDiscoveryProcessor Received '" + message
//					+ "'");
			
			
			
			long start = System.currentTimeMillis();  
			
			doWork(message);
			
			
			long end = System.currentTimeMillis();  
			System.out.println("one WorkerDiscoveryProcessor dowork spend: " + ((end - start)) + " milliseconds");  
			
			
		}
		
		

	}

	@SuppressWarnings("unchecked")
	private static void doWork(String currentip) {
		
		long devicetype ;
		int  mvlanenable; 
		int mvlanid ;
		String appver;
		int agetnport;
		
		currentip.trim().toUpperCase();
		
		Pattern pattern = Pattern
				.compile("(((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))");
		Matcher m = pattern.matcher(currentip);
		boolean b1 = m.matches();
		if (!b1) {
			System.out.println("not a good ip for work");
			return;
		}
		
//		if (hfcping(currentip, "161")) {
//			//tong
//			System.out.println("hfc tong ");
//			return;
//		}
		
		
		//eoc
//		tong = ping(currentip);
		

		
		devicetype = eocping(currentip, "161");

			
		
			// ///////////////////////////////////////////////////
			if (devicetype != -1) {
				String cbatmac = "";

				
				try {
					cbatmac = util.getStrPDU(currentip, "161", new OID(
							new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 6, 0 }));
					cbatmac = cbatmac.trim().toUpperCase();
				

//				System.out.println("WorkDiscoveryProcessing discoveryed Mac = "
//						+ cbatmac + "    ip=  " + currentip);
				
				
				/////////获取cbatinfo
				


				 agetnport = util.getINT32PDU(currentip, "161", new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 2, 7, 0 }));					
				 appver = util.getStrPDU(currentip, "161", new OID(new int[] {1, 3, 6, 1, 4, 1, 36186, 8, 4, 4, 0 }));
				 mvlanid =  util.getINT32PDU(currentip, "161", new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 5, 0 }));				    				   
			     mvlanenable = util.getINT32PDU(currentip, "161", new OID(	new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 4, 0 }));
		
			     
		

				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("get cbatmac error");
					return;
				}
				// write trap server address
				// //----------

				String msgservice="";
				// //////////////////////////////get cbat ok now send msg to
				// service
//				 msgservice = "001" + "|" + currentip + "|" +
//				 cbatmac + "|"
//				 + Long.toString(devicetype);
//				 
				 
				
				Map paramhash=new LinkedHashMap();
				
				 
				 
				 paramhash.put("msgcode", "001");
				 paramhash.put("ip", currentip.toLowerCase().trim());
				 paramhash.put("cbatmac", cbatmac.toLowerCase().trim());
				 paramhash.put("cbatdevicetype", Long.toString(devicetype));
				 paramhash.put("cbatinfo:agentport", Integer.toString(agetnport));
				 paramhash.put("cbatinfo:appver", appver.trim());				 
				 paramhash.put("cbatinfo:mvlanid", Integer.toString(mvlanid));
				 paramhash.put("cbatinfo:mvlanenable", mvlanenable == 1 ? "1" :"0");
				 
				 msgservice = JSONValue.toJSONString(paramhash);
				
				sendToPersist(msgservice);
				msgservice = "";

			
		} else {
			// log.info(
			// "#0 ping ping. ip #1........Bu Bu Tong ,Bu tong, Bu tong !",
			// currentip);

			
			return;
		}


	}

	static Boolean ping(String ip) {
		int timeOut = 3000; // I recommend 3 seconds at least

		try {
			if (ip.length() <= 0) {
				System.out.println("ip address is error ping fun...");
				return false;
			}
			InetAddress address = InetAddress.getByName(ip);
			Boolean status = address.isReachable(timeOut);
			// System.out.println("ping " + ip + " ........>result is,    "
			// + status);

			return status;
		} catch (UnknownHostException e) {
			// e.printStackTrace();
			// System.out
			// .println("ping [#0] ..... UnknownHostException ......result is false"
			// + ip);
			return false;
		} catch (IOException e) {
			// e.printStackTrace();
			// System.out
			// .println("ping [#0] .. IOException .........result is false"
			// + ip);
			return false;
		}

	}

	static long eocping(String host, String port) {

		return util.eocping(host, port);
	}

	static Boolean hfcping(String host, String port) {
		// System.out.println("ping hfc~~~~");
		String oid = null;
		try {

			oid = util.gethfcStrPDU(host, port, new OID(new int[] { 1, 3, 6, 1,
					2, 1, 1, 2, 0 }));
			if ((oid != null) && (oid != "")) {
				// service
				
				String msgservice="";
				Map paramhash=new LinkedHashMap();
				
				 
				 
				 paramhash.put("msgcode", "003");
				 paramhash.put("ip", host);
				 paramhash.put("oid", oid);
				 
				 
				 msgservice = JSONValue.toJSONString(paramhash);
				
				sendToPersist(msgservice);
				msgservice = "";
				
//				String msgservice = "003" + "|" + host + "|" + oid;
//				sendToPersist(msgservice);

				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void sendToPersist(String msg) {
				
		Jedis jedis = redisUtil.getConnection();
		jedis.lpush(PERSIST_CBAT_QUEUE_NAME, msg);
		 redisUtil.closeConnection(jedis);

	
	}

}
