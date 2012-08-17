package com.stan.wen9000.web;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;


public class WorkerDiscoveryProcessor{
	
	private static Logger logger = Logger.getLogger(WorkerDiscoveryProcessor.class);

	
	private static SnmpUtil util = new SnmpUtil();

	private static RedisUtil redisUtil;
	private static String dismode;

	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		WorkerDiscoveryProcessor.redisUtil = redisUtil;
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

        	System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>Descovery Subscribing................now receive on msg");
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

        	System.out.println(">pMessage>>>>>>>>>>>>>>>>>>>>>>>>Descovery Subscribing................now receive on msgarge1 [" + arg1 + "] arg2=["+arg2 +"]");
        	try {
    			//arg2 is mssage now is currenti p

    			servicestart(arg2);
    			
    		}catch(Exception e){
    			e.printStackTrace();			
    		}
    		
        }

    };

     /*

     * 启动订阅，当该方法启动时，将阻塞等待消息

     * 说明：

     * 1.subscribe(JedisPubSub jedisPubSub, String... channels)

     * 是常规订阅方法，key值基于完全匹配,方法中channels是多个要订阅的key值

 * 2.psubscribe(JedisPubSub jedisPubSub, String... patterns)

     * 是正则订阅方法，key值基于正则匹配,方法中的patterns是多个订阅到正则表达式

     * 不同的订阅将会触发JedisPubSub中不同的方法

     */

     

  
   


	public void execute()  {
//		System.out.println(" [x2] WorkerDiscoveryProcessor Start......");
		logger.info(" [x2] WorkerDiscoveryProcessor Start......");
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();
		 
			jedis.psubscribe(jedissubSub, "workdiscovery.*");
			redisUtil.getJedisPool().returnResource(jedis);
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}	

	}

	
		

	public static  void servicestart(String message) throws Exception {
			
 			
			doWork(message);		
  
			
			
	}
		
	
	@SuppressWarnings("unchecked")
	private static void doWork(String currentip) {		
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		long devicetype ;
		int  mvlanenable; 
		int mvlanid ;
		String appver;
		String trapserverip;
		int agetnport;
		String netmask;
		String gateway;
		
		
		currentip.trim().toUpperCase();
		
		
		System.out.println("Current ip=" + currentip);
		Pattern pattern = Pattern
				.compile("(((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))");
		Matcher m = pattern.matcher(currentip);
		boolean b1 = m.matches();
		if (!b1) {
			logger.info("[x] not a good ip for work");
			jedis.publish("node.dis.proc", "");
			jedis.incr("global:searched");
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}
		
		//W9000显示模式判断
		if((dismode = jedis.get("global:displaymode")) != null){
			if(dismode.equalsIgnoreCase("1")){
				//显示HFC设备
				//logger.info("------------------------------>>>>>>dismode==1");
				if(hfcdis(currentip,jedis)){
					return;
				}
			}
		}		
		
		//eoc	
		devicetype = eocping(currentip, "161");	
		
		// ///////////////////////////////////////////////////
		if (devicetype != -1) {
			String cbatmac = "";
			
			try {
				cbatmac = util.getStrPDU(currentip, "161", new OID(
						new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 6, 0 }));
				cbatmac = cbatmac.trim().toUpperCase();

				/////////获取cbatinfo
				 agetnport = util.getINT32PDU(currentip, "161", new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 2, 7, 0 }));					
				 appver = util.getStrPDU(currentip, "161", new OID(new int[] {1, 3, 6, 1, 4, 1, 36186, 8, 4, 4, 0 }));
				 mvlanid =  util.getINT32PDU(currentip, "161", new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 5, 0 }));				    				   
			     mvlanenable = util.getINT32PDU(currentip, "161", new OID(	new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 4, 0 }));
			     trapserverip = util.getStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
			     netmask = (util.getStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,2,0})));
			     gateway = (util.getStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,3,0})));
			} catch (IOException e) {
				System.out.println("get cbatmac error");
				jedis.publish("node.dis.proc", "");
				jedis.incr("global:searched");
				redisUtil.getJedisPool().returnResource(jedis);
				return;
			}
			// write trap server address
			// //----------

			String msgservice="";

			Map cbathash=new LinkedHashMap();

			 cbathash.put("msgcode", "001");
			 cbathash.put("ip", currentip.toLowerCase().trim());
			 cbathash.put("cbatmac", cbatmac.toLowerCase().trim());
			 cbathash.put("cbatdevicetype", Long.toString(devicetype));
			 cbathash.put("cbatinfo:agentport", Integer.toString(agetnport));
			 cbathash.put("cbatinfo:appver", appver.trim());				 
			 cbathash.put("cbatinfo:mvlanid", Integer.toString(mvlanid));
			 cbathash.put("cbatinfo:mvlanenable", Integer.toString(mvlanenable));
			 cbathash.put("cbatinfo:trapserverip", trapserverip);
			 cbathash.put("cbatinfo:netmask", netmask);
			 cbathash.put("cbatinfo:gateway", gateway);
			 msgservice = JSONValue.toJSONString(cbathash);
			
			sendToPersist(msgservice,jedis);
			msgservice = "";

			
		} else {
			// log.info(
			// "#0 ping ping. ip #1........Bu Bu Tong ,Bu tong, Bu tong !",
			// currentip);

			jedis.publish("node.dis.proc", "");
			jedis.incr("global:searched");
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}


	}

	static Boolean hfcdis(String currentip, Jedis jedis){
		if (hfcping(currentip, "161")) {
			//tong
			String oid = "";
			String gateway = "";
			String hfc_mac = "";
			String hfc_version = "";
			String hfc_LogicalID = "";
			String hfc_ModelNumber = "";
			String hfc_SerialNumber = "";
			String hfctype = "";
			String trapip1="";
			String trapip2="";
			String trapip3="";
			String power1 = "";
			String power_v1 = "";
			String power2 = "";
			String power_v2 = "";
			String bias_c1 = "";
			String bias_c2 = "";
			String ref_c1 = "";
			String ref_c2 = "";
			String pump_t1 = "";
			String pump_t2 = "";
			//相关参数
			//String powername = "";
			//String powervolate = "";

			try{
				oid = util.gethfcStrPDU(currentip, "161", new OID(new int[] { 1, 3, 6, 1,
					2, 1, 1, 2, 0 }));
				hfc_version = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,18,0}) );

				hfc_LogicalID = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,1,0}) );
				if(hfc_LogicalID == ""){
					return false;
				}
				hfc_ModelNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,3,0}) );
				hfc_SerialNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,4,0}) );
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
		        	//String temp = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0})))+ "℃";
		        	trapip1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,1}));
		        	trapip2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,2}));
		        	trapip3 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,3}));
		        	hfc_mac = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,2,1,1,1,0}) );
		        	power1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,4,1}) );
		        	power_v1 = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,2,1}) ) * 0.1) + "V";
		        	power2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,4,2}) );
		        	power_v2 = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,2,2}) ) * 0.1) + "V";
		        	bias_c1 = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,2,1}) )*0.1) + "mA";
		        	bias_c2 = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,2,2}) )*0.1) + "mA";
		        	ref_c1 = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,3,1}) )*0.1) + "A";
		        	ref_c2 = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,3,1}) )*0.1) + "A";
		        	pump_t1 = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,4,1}) )*0.1).substring(0, 5) + "℃";
		        	pump_t2 = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,4,2}) )*0.1).substring(0, 5) + "℃";
		        	//logger.info("------trapip===="+trapip1+"---------->>>>"+trapip2);
		        	//gateway = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,8888,1,5,0}) );
		        	//powername = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,4,0}) );
		        	//powervolate = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,2,0}) );
		        }
		        else if (oid.toString().equals("1.3.6.1.4.1.17409.1.6"))
		        {
		        	hfctype = "1310nm光发射机";
		        	//trapip1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,1}));
		        	//hfc_mac = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,2,1,1,1,0}) );
		        	//gateway = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,8888,1,5,0}) );
		        	//powername = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,4,0}) );
		        	//powervolate = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,2,0}) );
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
			}
			catch(Exception e){
				logger.info("read hfc info error!");
			}
			
			
			
			 String msgservice="";
			 Map paramhash=new LinkedHashMap();				 
			 paramhash.put("msgcode", "003");
			 paramhash.put("ip", currentip);
			 paramhash.put("gateway", gateway);
			 paramhash.put("oid", oid);	
			 paramhash.put("hfcmac", hfc_mac);
			 paramhash.put("hfctype", hfctype);	
			 paramhash.put("version", hfc_version);	
			 paramhash.put("logicalid", hfc_LogicalID);
			 paramhash.put("modelnumber", hfc_ModelNumber);
			 paramhash.put("serialnumber", hfc_SerialNumber);
			 paramhash.put("trapip1", trapip1);
			 paramhash.put("trapip2", trapip2);
			 paramhash.put("trapip3", trapip3);
			 paramhash.put("power1", power1);
			 paramhash.put("power_v1", power_v1);
			 paramhash.put("power2", power2);
			 paramhash.put("power_v2", power_v2);
			 paramhash.put("bias_c1", bias_c1);
			 paramhash.put("bias_c2", bias_c2);
			 paramhash.put("ref_c1", ref_c1);
			 paramhash.put("ref_c2", ref_c2);
			 paramhash.put("pump_t1", pump_t1);
			 paramhash.put("pump_t2", pump_t2);
			 msgservice = JSONValue.toJSONString(paramhash);
			
			sendToPersist(msgservice,jedis);
			//System.out.println("hfc tong ");
			return true;
		}
		return false;
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

	public static  void sendToPersist(String msg,Jedis jedis) {
		
		//jedis.connect();

		//jedis.lpush(PERSIST_CBAT_QUEUE_NAME, msg);
		jedis.publish("servicediscovery.new", msg);
		//jedis.dbSize();
		jedis.publish("node.dis.proc", "");
		jedis.incr("global:searched");
		redisUtil.getJedisPool().returnResource(jedis);
	
	}

}
