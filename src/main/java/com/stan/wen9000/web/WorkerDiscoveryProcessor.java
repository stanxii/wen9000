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
			jedis.publish("node.dis.proc", "");
			jedis.incr("global:searched");
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}


	}

	static Boolean hfcdis(String currentip, Jedis jedis){
		if (hfcping(currentip, "161")) {
			String oid = "";			
			try{
				oid = util.gethfcStrPDU(currentip, "161", new OID(new int[] { 1, 3, 6, 1,
					2, 1, 1, 2, 0 }),"public");
				
				if (oid.toString().equals("1.3.6.1.4.1.2000.1.3000"))
		        {
					//hfctype = "光平台";
					disOpticalPlatform(currentip, jedis);
		        }
		        else if (oid.toString().equals("1.3.6.1.4.1.17409.8888.1"))
		        {
		        	//hfctype = "万隆8槽WOS2000";
		        	disWOS2000(currentip, jedis);
		        }
		        else if (oid.toString().equals( "1.3.6.1.4.1.17409.1.8686"))
		        {
		        	//hfctype = "万隆增强光开关";
		        	disSwitch(currentip, jedis);
		        }
		        else if (oid.toString().equals("1.3.6.1.4.1.17409.1.11"))
		        {
		        	disEDFA(currentip, jedis);
		        }
		        else if (oid.toString().equals("1.3.6.1.4.1.17409.1.6"))
		        {
		        	dis1310mn(currentip, jedis);
		        }
		        else if (oid.toString().equals("1.3.6.1.4.1.17409.1.10"))
		        {
		        	//hfctype = "光工作站";
		        	disWorkStation(currentip, jedis);
		        }
		        else if (oid.toString().equals( "1.3.6.1.4.1.17409.1.9"))
		        {
		        	//hfctype = "光接收机";
		        	disReceiver(currentip, jedis);
		        }
		        else if (oid.toString().equals("1.3.6.1.4.1.17409.1.7"))
		        {
		        	//hfctype = "1550光发射机";
		        	dis1550mn(currentip, jedis);
		        }
		        else
		        {
		        	//hfctype = "未知设备类型";
		        }
			}
			catch(Exception e){
				logger.info("read hfc info error!");
			}
			
			
			
			 
			 
			
			
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
					2, 1, 1, 2, 0 }),"public");
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
	
	public static void disWorkStation(String currentip, Jedis jedis){
		String deviceid = new String();
		String devicetype = new String();
		
		try{
			
			deviceid = util.gethfcStrPDU(currentip, "161", new OID(new int[] { 1, 3, 6, 1,
					4, 1, 17409, 1, 3 , 3,2,2,1,2,1 }),"public");
			devicetype = util.gethfcStrPDU(currentip, "161", new OID(new int[] { 1, 3, 6, 1,
					4, 1, 17409, 1, 3 , 3,2,2,1,4,1 }),"public");
			if (deviceid.equalsIgnoreCase("ScanID                         ") || deviceid.equalsIgnoreCase("WL00OR220000"))
            {
                if (devicetype.equalsIgnoreCase("WR8602JL") || devicetype.equalsIgnoreCase("WR8604JL") || devicetype.equalsIgnoreCase("WR8602RJ") || devicetype.equalsIgnoreCase("WR8602RJL") ||
                 devicetype.equalsIgnoreCase("WR8604RJL") || devicetype.equalsIgnoreCase("WR8604DJ") || devicetype.equalsIgnoreCase("WR8602JL-CM") || devicetype.equalsIgnoreCase("WR8600")
                   || devicetype.equalsIgnoreCase("WR1004DJ") || devicetype.equalsIgnoreCase("WR1002RJ") || devicetype.equalsIgnoreCase("SCN-1000-2") || devicetype.equalsIgnoreCase("SCN-870-2") || devicetype.equalsIgnoreCase("WR8602ML") || devicetype.equalsIgnoreCase("WR8602JLE")
              || devicetype.equalsIgnoreCase("WR8602ME") || devicetype.equalsIgnoreCase("FMAU1121") || devicetype.equalsIgnoreCase("WR8602MF-B") || devicetype.equalsIgnoreCase("WR8602M-B") || devicetype.equalsIgnoreCase("WR8604DJ-1G") || devicetype.equalsIgnoreCase("OPS2600")
                || devicetype.equalsIgnoreCase("WR8602MFH-B")){
                	disReceiver(currentip,jedis);
                }else if (devicetype.equalsIgnoreCase("WR8602JDS")){
                	disSwitch_Receiver(currentip,jedis);
                	//return "带切换开关光接收机";
                }
                else if (devicetype.equalsIgnoreCase("WR8604HA") || devicetype.equalsIgnoreCase("WR8604G-S") || devicetype.equalsIgnoreCase("WR8602G-S") || devicetype.equalsIgnoreCase("WR8604HC") || deviceid.equalsIgnoreCase("HC-860")){
                	//return "光工作站";
                }                    
                else if (devicetype == "WR8604HJ" || devicetype.equalsIgnoreCase("SCN-1000-4") || devicetype.equalsIgnoreCase("SCN-870-4") || devicetype.equalsIgnoreCase("WR8604HJ-1G")
                || devicetype.equalsIgnoreCase("OPS2500") || devicetype.equalsIgnoreCase("WR8604HJ-1G-2P") || devicetype.equalsIgnoreCase("OPS2500-D2R") || devicetype.equalsIgnoreCase("WR8604")){
                	//return "光AGC工作站";
                }                    
                else{
                	// return "光接收机";
                }
                   
            }

            else if (deviceid.equalsIgnoreCase("JLE-86-2") || deviceid.equalsIgnoreCase("CEAM-1G-2") || deviceid.equalsIgnoreCase("JL-86-2") || deviceid.equalsIgnoreCase("DJ-1G-4") || deviceid.equalsIgnoreCase("DJ-1G-4-R")
                || deviceid.equalsIgnoreCase("DJ-86-4") || deviceid.equalsIgnoreCase("JL-86-4") || deviceid.equalsIgnoreCase("RJL-86-4") || deviceid.equalsIgnoreCase("RJL-86-2") || deviceid.equalsIgnoreCase("RJ-86-2") 
                || deviceid.equalsIgnoreCase("JDS-86-2") || deviceid.equalsIgnoreCase("RJ-1G-2") || deviceid.equalsIgnoreCase("JDS-86-2") 
               || deviceid.equalsIgnoreCase("DM-86-2") || deviceid.equalsIgnoreCase("ME-86-2") || deviceid.equalsIgnoreCase("JL-CM-2") || deviceid.equalsIgnoreCase("J-1G-2") || deviceid.equalsIgnoreCase("JL-1G-2")
               || deviceid.equalsIgnoreCase("JL-1G-4") || deviceid.equalsIgnoreCase("J-B-1G-2") || deviceid.equalsIgnoreCase("M-B-86-2") || deviceid.equalsIgnoreCase("CEAM-1G-2")){
            	disReceiver(currentip,jedis);
            }                
            else if (deviceid.equalsIgnoreCase("HJ-1G")|| deviceid.equalsIgnoreCase("HJ-860")){
            	//return "光AGC工作站";
            }                
             else if (deviceid.equalsIgnoreCase("JS-1G-2") || deviceid.equalsIgnoreCase("SJL-1G-4") || deviceid.equalsIgnoreCase("JS-1G-2S")){
            	 disSwitch_Receiver(currentip,jedis);
            	 //return "带切换开关光接收机";
             }                
            else{
            	//return "光工作站";
            }
                
		}catch(Exception e){
			
		}
		
	}
	
	public static void disReceiver(String currentip, Jedis jedis){
		int val = 0;
		String hfc_mac = "";
		String hfc_version = "";
		String hfc_LogicalID = "";
		String hfc_ModelNumber = "";
		String hfc_SerialNumber = "";
		String hfctype = "";
		String trapip1="";
		String trapip2="";
		String trapip3="";
		String innertemp = "";
		String inputpower = "";
		String power1 = "";
		String power_v1 = "";
		String power2 = "";
		String power_v2 = "";
		String community = "public";
		int channelnum = 0;
    	String r_transpower = new String();
    	String r_biascurrent = new String();
    	String out_port = new String();
    	String att = new String();
    	String eqv = new String();
    	String out_level = new String();
    	String agc = new String();
    	hfctype = "光接收机";
    	try{
    		hfc_version = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,18,0}),community );

    		hfc_LogicalID = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,1,0}),community );
    		if(hfc_LogicalID == ""){
    			return;
    		}
    		hfc_ModelNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,3,0}),community );
    		hfc_SerialNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,4,0}),community );
    		trapip1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,1}),community);
        	trapip2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,2}),community);
        	trapip3 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,3}),community);
        	hfc_mac = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,2,1,1,1,0}),community );
        	//logger.info("======mac====="+hfc_mac);
        	innertemp = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0}),community ) + "℃";
        	hfc_mac = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,2,2,1,10,1}),community );
        	power1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,4,1}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,2,1}),community );
        	power_v1 = val/10 + "."+Math.abs(val%10) + "V";
        	power2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,4,2}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,2,2}),community );
        	power_v2 = val/10 + "."+Math.abs(val%10) + "V";
        	channelnum = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,20,0}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,5,1,2,1}),community );
        	inputpower = val/10 + "."+Math.abs(val%10) + "dBm";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,3,1,7,1}),community );
        	r_transpower = val/10 + "."+Math.abs(val%10) + "dBm";
        	out_port = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,6,1}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,9,1}),community );
        	att = val/10 + "."+Math.abs(val%10) + "dB";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,10,1}),community );
        	eqv = val/10 + "."+Math.abs(val%10) + "dB";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,4,1}),community );
        	out_level = val/10 + "."+Math.abs(val%10) + "dBuV";
        	//反向偏置电流不确定oid
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,3,1,2,1}),community );
        	r_biascurrent = val + "mA";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,28,0}),community );
        	agc = val/10 + "."+Math.abs(val%10) + "dBm";
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	
    	String msgservice="";
    	Map paramhash=new LinkedHashMap();				 
   		 paramhash.put("msgcode", "003");
   		 paramhash.put("ip", currentip);
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
   		 paramhash.put("channelnum", channelnum);
   		 paramhash.put("innertemp", innertemp);    	   		 
   		paramhash.put("r_transpower", r_transpower);
  		 	paramhash.put("r_biascurrent", r_biascurrent);   	   		 	
  		 	paramhash.put("out_port", out_port);
		 	paramhash.put("att", att);
		 	paramhash.put("eqv", eqv);
		 	paramhash.put("out_level", out_level);
		 	paramhash.put("inputpower", inputpower);
		 	paramhash.put("agc", agc);
   		 msgservice = JSONValue.toJSONString(paramhash);
   		 sendToPersist(msgservice,jedis);
	}
	
	public static void disSwitch_Receiver(String currentip, Jedis jedis){
		int val = 0;
		String hfc_mac = "";
		String hfc_version = "";
		String hfc_LogicalID = "";
		String hfc_ModelNumber = "";
		String hfc_SerialNumber = "";
		String hfctype = "带切换开关光接收机";
		String power1 = "";
		String power_v1 = "";
		String power2 = "";
		String power_v2 = "";
		String trapip1="";
		String trapip2="";
		String trapip3="";
		String Ainputpower = "";
		String Binputpower = "";
		int channelnum = 0;
		String switchval = "";
		String workchannel = "";
		String workmode = "";
		String innertemp = "";
		String agc = "";
		String out_port = "";
    	String att = "";
    	String eqv = "";
    	String out_level = "";
    	String community = "public";
    	
    	try{
    		hfc_version = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,18,0}),community );

			hfc_LogicalID = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,1,0}),community );
			if(hfc_LogicalID == ""){
				return;
			}
			hfc_ModelNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,3,0}),community );
			hfc_SerialNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,4,0}),community );
			trapip1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,1}),community);
        	trapip2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,2}),community);
        	trapip3 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,3}),community);
    		hfc_mac = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,2,2,1,10,1}),community );
        	power1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,4,1}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,2,1}),community );
        	power_v1 = val/10 + "."+Math.abs(val%10) + "V";
        	power2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,4,2}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,2,2}),community );
        	power_v2 = val/10 + "."+Math.abs(val%10) + "V";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,5,1,2,1}),community );
        	Ainputpower = val/10 + "."+Math.abs(val%10) + "dBm";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,5,1,2,2}),community );
        	Binputpower = val/10 + "."+Math.abs(val%10) + "dBm";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,13,1,4,1}),community );
        	switchval = val/10 + "."+Math.abs(val%10) + "dBm";
        	channelnum = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,20,0}),community );
        	out_port = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,6,1}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,9,1}),community );
        	att = val/10 + "."+Math.abs(val%10) + "dB";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,10,1}),community );
        	eqv = val/10 + "."+Math.abs(val%10) + "dB";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,4,1}),community );
        	out_level = val/10 + "."+Math.abs(val%10) + "dBuV";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,28,0}),community );
        	agc = val/10 + "."+Math.abs(val%10) + "dBm";
        	workchannel = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,13,1,2,1}),community ));
        	workmode = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,13,1,3,1}),community ));
        	innertemp = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0}),community ) + "℃";
        	String msgservice="";
        	Map paramhash=new LinkedHashMap();				 
	   		 paramhash.put("msgcode", "003");
	   		 paramhash.put("ip", currentip);
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
	   		 paramhash.put("channelnum", channelnum);
	   		 paramhash.put("innertemp", innertemp);    	   		 
	   		paramhash.put("Ainputpower", Ainputpower);
	   		 paramhash.put("Binputpower", Binputpower);   	   		 	
	   		 paramhash.put("out_port", out_port);
   		 	paramhash.put("att", att);
   		 	paramhash.put("eqv", eqv);
   		 	paramhash.put("out_level", out_level);
   		 	paramhash.put("workchannel", workchannel);
   		    paramhash.put("workmode", workmode);
   		 	paramhash.put("agc", agc);
   		    paramhash.put("switchval", switchval);
	   		 msgservice = JSONValue.toJSONString(paramhash);
	   		 sendToPersist(msgservice,jedis);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
	
	public static void dis1550mn(String currentip, Jedis jedis){
		
	}
	
	public static void disSwitch(String currentip, Jedis jedis){
		
	}
	
	public static void disWOS2000(String currentip, Jedis jedis){
		
	}
	
	public static void disOpticalPlatform(String currentip, Jedis jedis){
		
	}
	
	public static void dis1310mn(String currentip, Jedis jedis){
		int val = 0;
		String hfc_mac = "";
		String hfc_version = "";
		String hfc_LogicalID = "";
		String hfc_ModelNumber = "";
		String hfc_SerialNumber = "";
		String hfctype = "1310nm光发射机";
		String trapip1="";
		String trapip2="";
		String trapip3="";
		String power1 = "";
		String power_v1 = "";
		String power2 = "";
		String power_v2 = "";
		String power3 = "";
		String power_v3 = "";
		int channelnum = 0;
		String wavelength = "";
		String rfattrange = "";
		String lasertype = "";
		String outputpower = "";
		String agccontrol = "";
		String lasercurrent = "";
		String temp = "";
		String teccurrent = "";
		String drivelevel = "";
		String mgc = "";
		String agc = "";
		String innertemp = "";
		String community = "public";
		try{
			hfc_version = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,18,0}),community );

			hfc_LogicalID = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,1,0}),community );
			if(hfc_LogicalID == ""){
				return;
			}
			hfc_ModelNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,3,0}),community );
			hfc_SerialNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,4,0}),community );
			trapip1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,1}),community);
        	trapip2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,2}),community);
        	trapip3 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,3}),community);
        	hfc_mac = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,2,1,1,1,0}),community );
        	power1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,4,1}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,2,1}),community );
        	power_v1 = val/10 + "."+Math.abs(val%10) + "V";
        	power2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,4,2}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,2,2}),community );
        	power_v2 = val/10 + "."+Math.abs(val%10) + "V";
        	power3 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,4,3}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,2,3}),community );
        	power_v3 = val/10 + "."+Math.abs(val%10) + "V";
        	channelnum = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,15,1}),community );
        	wavelength = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,2,1}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,6,1}),community );
        	rfattrange = val/10 + "."+Math.abs(val%10) + "db";
        	lasertype = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,3,1}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,10,1}),community );
        	outputpower = val/10 + "."+Math.abs(val%10) + "mW";
        	agccontrol = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,12,1}),community );
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,9,1}),community );
        	lasercurrent = val/10 + "."+Math.abs(val%10) + "mA";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,8,1}),community );
        	temp = val/10 + "."+Math.abs(val%10) + "℃";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,11,1}),community );
        	teccurrent = val * 0.01 + "A";//val/100 + "."+Math.abs(val%100)+ Math.abs(val%10) + "A";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,5,1}),community );
        	drivelevel = val + "dBuV/ch";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,14,1}),community );
        	mgc = val/10 + "."+Math.abs(val%10) + "db";
        	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,13,1}),community );
        	agc = val/10 + "."+Math.abs(val%10) + "db";
        	innertemp = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0}),community ) + "℃";
        	String msgservice="";
        	Map paramhash=new LinkedHashMap();				 
	   		 paramhash.put("msgcode", "003");
	   		 paramhash.put("ip", currentip);
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
	   		 paramhash.put("power3", power3);
	   		 paramhash.put("power_v3", power_v3);
	   		 paramhash.put("channelnum", channelnum);
	   		 paramhash.put("wavelength", wavelength);
	   		 paramhash.put("rfattrange", rfattrange);
	   		 paramhash.put("lasertype", lasertype);
	   		 paramhash.put("outputpower", outputpower);
	   		 paramhash.put("agccontrol", agccontrol);	   		 
	   		 paramhash.put("lasercurrent", lasercurrent);
	   		 paramhash.put("temp", temp);
	   		 paramhash.put("teccurrent", teccurrent);
	   		 paramhash.put("drivelevel", drivelevel);
	   		 paramhash.put("mgc", mgc);
	   		 paramhash.put("agc", agc);
	   		 paramhash.put("innertemp", innertemp);
	   		 msgservice = JSONValue.toJSONString(paramhash);
	   		 
	   		 sendToPersist(msgservice,jedis);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void disEDFA(String currentip, Jedis jedis){
		int val = 0;
		String hfc_mac = "";
		String hfc_version = "";
		String hfc_LogicalID = "";
		String hfc_ModelNumber = "";
		String hfc_SerialNumber = "";
		String hfctype = "掺铒光纤放大器";
		String inpower="";
		String outpower="";
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
		String innertemp="";
		String msgservice="";
		String community = "public";
    	//String temp = String.valueOf(util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0})))+ "℃";
		try{
			hfc_version = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,18,0}),community );

			hfc_LogicalID = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,1,0}),community );
			if(hfc_LogicalID == ""){
				return;
			}
			hfc_ModelNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,3,0}),community );
			hfc_SerialNumber = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,4,0}),community );
			
			trapip1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,1}),community);
	    	trapip2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,2}),community);
	    	trapip3 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,3,1,7,1,2,3}),community);
	    	hfc_mac = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,2,1,1,1,0}),community );
	    	power1 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,4,1}),community );
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,2,1}),community );
	    	power_v1 = val/10 + "."+Math.abs(val%10) + "V";
	    	power2 = util.gethfcStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,4,2}),community );
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,2,2}),community );
	    	power_v2 = val/10 + "."+Math.abs(val%10) + "V";
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,2,1}),community );
	    	bias_c1 = val/10 + "."+Math.abs(val%10) + "mA";
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,2,2}),community );
	    	bias_c2 = val/10 + "."+Math.abs(val%10) + "mA";
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,3,1}),community );
	    	ref_c1 = val/10 + "."+Math.abs(val%10) + "A";
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,3,2}),community );
	    	ref_c2 = val/10 + "."+Math.abs(val%10) + "A";
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,4,1}),community );
	    	pump_t1 = val/10 + "."+Math.abs(val%10) + "℃";
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,4,2}),community );
	    	pump_t2 = val/10 + "."+Math.abs(val%10) + "℃";	
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,2,0}),community );
	    	inpower = val/10 + "."+Math.abs(val%10) + "dBm";
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,3,0}),community );
	    	outpower = val/10 + "."+Math.abs(val%10) + "dBm";
	    	val = util.gethfcINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0}),community );
	    	innertemp = val + "℃";
		}catch(Exception e){
			e.printStackTrace();
		}    		     
    	
    	 Map paramhash=new LinkedHashMap();				 
		 paramhash.put("msgcode", "003");
		 paramhash.put("ip", currentip);
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
		 paramhash.put("inpower", inpower);
		 paramhash.put("outpower", outpower);
		 paramhash.put("innertemp", innertemp);
		 msgservice = JSONValue.toJSONString(paramhash);
		 
		 sendToPersist(msgservice,jedis);
	}

}
