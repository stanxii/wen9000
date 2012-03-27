package com.stan.wen9000.web;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;

import com.stan.wen9000.action.jedis.util.RedisUtil;


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
		
		if (hfcping(currentip, "161")) {
			//tong
			String oid = "";
			String hfc_mac = "30:71:b2:00:00:00";
			String hfc_version = "";
			String hfc_LogicalID = "";
			String hfc_ModelNumber = "";
			String hfc_SerialNumber = "";
			try{
				oid = util.gethfcStrPDU(currentip, "161", new OID(new int[] { 1, 3, 6, 1,
					2, 1, 1, 2, 0 }));
				hfc_version = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,18,0}) );
				hfc_LogicalID = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,1,0}) );
				hfc_ModelNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,3,0}) );
				hfc_SerialNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,4,0}) );
			}
			catch(Exception e){
				logger.info("read hfc info error!");
			}
			String hfctype = "";
			if (oid.toString().equals("1.3.6.1.4.1.2000.1.3000"))
	        {
				hfctype = "光平台";
	        }
	        else if (oid.toString().equals("1.3.6.1.4.1.17409.8888.1"))
	        {
	        	hfctype = "万隆8槽WOS2000";
	        }
	        else if (oid.toString().equals( "1.3.6.1.4.1.17409.1.8686"))
	        {
	        	hfctype = "万隆增强光开关";
	        }
	        else if (oid.toString().equals("1.3.6.1.4.1.17409.1.11"))
	        {
	        	hfctype = "掺铒光纤放大器";
	        }
	        else if (oid.toString().equals("1.3.6.1.4.1.17409.1.6"))
	        {
	        	hfctype = "1310nm光发射机";
	        }
	        else if (oid.toString().equals("1.3.6.1.4.1.17409.1.10"))
	        {
	        	hfctype = "光工作站";
	        }
	        else if (oid.toString().equals( "1.3.6.1.4.1.17409.1.9"))
	        {
	        	hfctype = "光接收机";
	        }
	        else if (oid.toString().equals("1.3.6.1.4.1.17409.1.7"))
	        {
	        	hfctype = "1550光发射机";
	        }
	        else
	        {
	        	hfctype = "未知设备类型";
	        }
			
			 String msgservice="";
			 Map paramhash=new LinkedHashMap();				 
			 paramhash.put("msgcode", "003");
			 paramhash.put("ip", currentip);
			 paramhash.put("oid", oid);	
			 paramhash.put("hfcmac", hfc_mac);
			 paramhash.put("hfctype", hfctype);	
			 paramhash.put("version", hfc_version);	
			 paramhash.put("logicalid", hfc_LogicalID);
			 paramhash.put("modelnumber", hfc_ModelNumber);
			 paramhash.put("serialnumber", hfc_SerialNumber);
			 
			 msgservice = JSONValue.toJSONString(paramhash);
			
			sendToPersist(msgservice);
			//System.out.println("hfc tong ");
			return;
		}
		
		
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
