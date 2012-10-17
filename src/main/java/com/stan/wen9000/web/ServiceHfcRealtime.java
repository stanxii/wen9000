package com.stan.wen9000.web;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;

public class ServiceHfcRealtime {

	
	private static Logger log = Logger.getLogger(ServiceHfcRealtime.class);

	private static SnmpUtil util = new SnmpUtil();

	
	private static RedisUtil redisUtil;
	private static String AlarmSatOidStr = ".1.3.6.1.4.1.17409.1.1.1.1.3";
	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceHfcRealtime.redisUtil = redisUtil;
	}
  


	public void start() {
		log.info("[#3] ..... service ServiceHfcRealtime starting"); 			
		dowork();		
	}
	
	public static void dowork(){
		Jedis jedis=null;		
		JSONObject json = new JSONObject();		
		while(true){	
			try {
				jedis = redisUtil.getConnection();	 
			
			}catch(Exception e){
				e.printStackTrace();
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				return;
			}
			if(!jedis.exists("global:displaymode")){
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				redisUtil.getJedisPool().returnResource(jedis);
				continue;
			}
			if(jedis.get("global:displaymode").equalsIgnoreCase("1")){
				String key = jedis.get("global:hfcrealtime");
				if(key != ""){
					if(!jedis.exists(key)){
						redisUtil.getJedisPool().returnResource(jedis);
						continue;
					}
					if(!jedis.hget(key, "active").equalsIgnoreCase("1")){
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						redisUtil.getJedisPool().returnResource(jedis);
						continue;
					}
					String ip = jedis.hget(key, "ip");

					json.put("hfctype", jedis.hget(key, "hfctype"));
					try{
						String oid = util.gethfcStrPDU(ip, "161", new OID(new int[] { 1, 3, 6, 1,
								2, 1, 1, 2, 0 }));
						if ((oid == null) || (oid == "")) {
							redisUtil.getJedisPool().returnResource(jedis);
							continue;
						}
						if(jedis.hget(key, "hfctype").equalsIgnoreCase("掺铒光纤放大器")){
							Realtime_EDFA(ip,jedis,json,key);	
						}else if(jedis.hget(key, "hfctype").equalsIgnoreCase("1310nm光发射机")){
							Realtime_1310(ip,jedis,json,key);							
						}else if(jedis.hget(key, "hfctype").equalsIgnoreCase("光平台")){
							
						}else if(jedis.hget(key, "hfctype").equalsIgnoreCase("万隆8槽WOS2000")){
							
						}else if(jedis.hget(key, "hfctype").equalsIgnoreCase("万隆增强光开关")){
							
						}else if(jedis.hget(key, "hfctype").equalsIgnoreCase("光工作站")){
							
						}else if(jedis.hget(key, "hfctype").equalsIgnoreCase("光接收机")){
							Realtime_Receiver(ip,jedis,json,key);
						}else if(jedis.hget(key, "hfctype").equalsIgnoreCase("1550光发射机")){
							
						}
						
					}catch(Exception e){
						//jedis.publish("node.opt.hfcrealtime", json.toJSONString());
						e.printStackTrace();
					}
					redisUtil.getJedisPool().returnResource(jedis);
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else{
				//未开HFC设备显示模式，延时30s
//				try {
//					Thread.sleep(30000);
//					//log.info("------------------>>>>sleep 30s");
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				log.info("------------------>>>>ServiceHfcRealtime Done!");
				redisUtil.getJedisPool().returnResource(jedis);
				return;
			}
		}		
	}
	
	private static void Realtime_EDFA(String ip,Jedis jedis,JSONObject json,String key){
		String extraoid = "";
		String ParamMibOID = "";
		OID AlarmSatOid = null;
		try{
			int val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,2,0}) );
			json.put("inpower",  val/10 + "."+Math.abs(val%10) + "dBm");
			jedis.hset(key, "inpower",  val/10 + "."+Math.abs(val%10) + "dBm");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,3,0}) );
			json.put("outpower",  val/10 + "."+Math.abs(val%10) + "dBm");
			jedis.hset(key, "outpower",  val/10 + "."+Math.abs(val%10) + "dBm");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,2,1}) );
			json.put("power_v1",  val/10 + "."+Math.abs(val%10) + "V");
			jedis.hset(key, "power_v1",  val/10 + "."+Math.abs(val%10) + "V");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,2,2}) );
			json.put("power_v2",  val/10 + "."+Math.abs(val%10) + "V");
			jedis.hset(key, "power_v2",  val/10 + "."+Math.abs(val%10) + "V");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,2,1}) );
			json.put("bias_c1",  val/10 + "."+Math.abs(val%10) + "mA");
			jedis.hset(key, "bias_c1",  val/10 + "."+Math.abs(val%10) + "mA");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,2,2}) );
			json.put("bias_c2",  val/10 + "."+Math.abs(val%10) + "mA");
			jedis.hset(key, "bias_c2",  val/10 + "."+Math.abs(val%10) + "mA");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,3,1}) );
			json.put("ref_c1",  val/10 + "."+Math.abs(val%10) + "A");
			jedis.hset(key, "ref_c1",  val/10 + "."+Math.abs(val%10) + "A");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,3,2}) );
			json.put("ref_c2",  val/10 + "."+Math.abs(val%10) + "A");
			jedis.hset(key, "ref_c2",  val/10 + "."+Math.abs(val%10) + "A");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,4,1}) );
			json.put("pump_t1", val/10 + "."+Math.abs(val%10) + "℃");
			jedis.hset(key, "pump_t1", val/10 + "."+Math.abs(val%10) + "℃");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,4,2}) );							
			json.put("pump_t2", val/10 + "."+Math.abs(val%10) + "℃");
			jedis.hset(key, "pump_t2", val/10 + "."+Math.abs(val%10) + "℃");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0}) );
			json.put("innertemp", val + "℃");
			jedis.hset(key, "innertemp", val + "℃");
			//hfc_powerv1
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.7.1.2.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_powerv1_sat",  val);
			//hfc_powerv2
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.7.1.2.2";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_powerv2_sat",  val);
			//hfc_ingonglv
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.3.0";
			extraoid = ".11" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_ingonglv_sat",  val);
			//hfc_gonglv
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.2.0";
			extraoid = ".11" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_gonglv_sat",  val);
			//hfc_bias_c1
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.4.1.2.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_bias_c1_sat",  val);
			//hfc_ref_c1
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.4.1.3.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_ref_c1_sat",  val);
			//hfc_pump_t1
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.4.1.4.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_pump_t1_sat",  val);
			//hfc_bias_c2
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.4.1.2.2";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_bias_c2_sat",  val);
			//hfc_ref_c2
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.4.1.3.2";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_ref_c2_sat",  val);
			//hfc_pump_t2
			ParamMibOID = ".1.3.6.1.4.1.17409.1.11.4.1.4.2";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_pump_t2_sat",  val);
		}catch(Exception e){
			
		}
		jedis.publish("node.opt.hfcrealtime", json.toJSONString());
	}
	
	private static void Realtime_1310(String ip,Jedis jedis,JSONObject json,String key){
		String extraoid = "";
		String ParamMibOID = "";
		OID AlarmSatOid = null;
		try{
			int val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,2,1}) );
			json.put("power_v1",  val/10 + "."+Math.abs(val%10) + "V");
			jedis.hset(key, "power_v1", val/10 + "."+Math.abs(val%10) + "V");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,2,2}) );
			json.put("power_v2",  val/10 + "."+Math.abs(val%10) + "V");
			jedis.hset(key, "power_v2", val/10 + "."+Math.abs(val%10) + "V");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,8,1,2,3}) );
			json.put("power_v3",  val/10 + "."+Math.abs(val%10) + "V");
			jedis.hset(key, "power_v3", val/10 + "."+Math.abs(val%10) + "V");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,5,1}) );
			json.put("drivelevel",  val + "dBuV/ch");
			jedis.hset(key, "drivelevel", val + "dBuV/ch");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,6,1}) );
			json.put("rfattrange", val/10 + "."+Math.abs(val%10) + "db");
			jedis.hset(key, "rfattrange", val/10 + "."+Math.abs(val%10) + "db");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,10,1}) );
			json.put("outputpower", val/10 + "."+Math.abs(val%10) + "mW");
			jedis.hset(key, "outputpower", val/10 + "."+Math.abs(val%10) + "mW");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,9,1}) );
			json.put("lasercurrent", val/10 + "."+Math.abs(val%10) + "mA");
			jedis.hset(key, "lasercurrent", val/10 + "."+Math.abs(val%10) + "mA");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,8,1}) );
			json.put("temp", val/10 + "."+Math.abs(val%10) + "℃");
			jedis.hset(key, "temp", val/10 + "."+Math.abs(val%10) + "℃");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,11,1}) );
			json.put("teccurrent", val*0.01 + "A");
			jedis.hset(key, "teccurrent", val*0.01 + "A");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,14,1}) );
        	json.put("mgc", val/10 + "."+Math.abs(val%10) + "db");
        	jedis.hset(key, "mgc", val/10 + "."+Math.abs(val%10) + "db");
        	val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,13,1}) );
        	json.put("agc", val/10 + "."+Math.abs(val%10) + "db");
        	jedis.hset(key, "agc", val/10 + "."+Math.abs(val%10) + "db");
        	String sval = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0}) ) + "℃";
			json.put("innertemp",sval );
        	jedis.hset(key, "innertemp",sval);
        	sval = util.gethfcStrPDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,12,1}) );
			json.put("agccontrol", sval);
			jedis.hset(key, "agccontrol",sval);
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,6,3,1,15,1}) );
			json.put("channelnum", val);
			jedis.hset(key, "channelnum",String.valueOf(val));
			
			//power_v1
			ParamMibOID = ".1.3.6.1.4.1.17409.1.6.8.1.2.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("power_v1_sat",  val);
			//power_v2
			ParamMibOID = ".1.3.6.1.4.1.17409.1.6.8.1.2.2";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("power_v2_sat",  val);
			//power_v3
			ParamMibOID = ".1.3.6.1.4.1.17409.1.6.8.1.2.3";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("power_v3_sat",  val);
			//hfc_drivelevel
			ParamMibOID = ".1.3.6.1.4.1.17409.1.6.3.1.5.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_drivelevel_sat",  val);
			//hfc_rfattrange
			ParamMibOID = ".1.3.6.1.4.1.17409.1.6.3.1.6.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_rfattrange_sat",  val);			
			//hfc_outputpower
			ParamMibOID = ".1.3.6.1.4.1.17409.1.6.3.1.10.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_outputpower_sat",  val);			
			//hfc_lasercurrent
			ParamMibOID = ".1.3.6.1.4.1.17409.1.6.3.1.9.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_lasercurrent_sat",  val);
			
			//hfc_temp
			ParamMibOID = ".1.3.6.1.4.1.17409.1.6.3.1.8.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_temp_sat",  val);
			//hfc_teccurrent
			ParamMibOID = ".1.3.6.1.4.1.17409.1.6.3.1.11.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_teccurrent_sat",  val);
		}catch(Exception e){
			
		}
		jedis.publish("node.opt.hfcrealtime", json.toJSONString());
	}
	
	private static void Realtime_Receiver(String ip,Jedis jedis,JSONObject json,String key){
		String extraoid = "";
		String ParamMibOID = "";
		OID AlarmSatOid = null;
		try{
			int val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,2,1}) );
			json.put("power_v1",  val/10 + "."+Math.abs(val%10) + "V");
			jedis.hset(key, "power_v1", val/10 + "."+Math.abs(val%10) + "V");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,19,1,2,2}) );
			json.put("power_v2",  val/10 + "."+Math.abs(val%10) + "V");
			jedis.hset(key, "power_v2", val/10 + "."+Math.abs(val%10) + "V");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,20,0}) );
			json.put("channelnum", val);
			jedis.hset(key, "channelnum", String.valueOf(val));
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,5,1,2,1}) );
			json.put("inputpower",  val/10 + "."+Math.abs(val%10) + "dBm");
			jedis.hset(key, "inputpower", val/10 + "."+Math.abs(val%10) + "dBm");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,3,1,7,1}) );
			json.put("r_transpower",  val/10 + "."+Math.abs(val%10) + "dBm");
			jedis.hset(key, "r_transpower", val/10 + "."+Math.abs(val%10) + "dBm");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,9,1}) );
			json.put("att",  val/10 + "."+Math.abs(val%10) + "dB");
			jedis.hset(key, "att", val/10 + "."+Math.abs(val%10) + "dB");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,10,1}) );
			json.put("eqv",  val/10 + "."+Math.abs(val%10) + "dB");
			jedis.hset(key, "eqv", val/10 + "."+Math.abs(val%10) + "dB");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,11,1,4,1}) );
			json.put("out_level",  val/10 + "."+Math.abs(val%10) + "dBuV");
			jedis.hset(key, "out_level", val/10 + "."+Math.abs(val%10) + "dBuV");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,3,1,2,1}) );
			json.put("r_biascurrent",  val/10 + "."+Math.abs(val%10) + "mA");
			jedis.hset(key, "r_biascurrent", val/10 + "."+Math.abs(val%10) + "mA");
			val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,10,28,0}) );
			json.put("agc",  val/10 + "."+Math.abs(val%10) + "dBm");
			jedis.hset(key, "agc", val/10 + "."+Math.abs(val%10) + "dBm");
			String innertemp = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0}) ) + "℃";
			json.put("innertemp", innertemp);
			jedis.hset(key, "innertemp",innertemp);
			
			//power_v1
			ParamMibOID = ".1.3.6.1.4.1.17409.1.10.19.1.2.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("power_v1_sat",  val);
			//power_v2
			ParamMibOID = ".1.3.6.1.4.1.17409.1.10.19.1.2.2";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("power_v2_sat",  val);
			//hfc_ingonglv
			ParamMibOID = ".1.3.6.1.4.1.17409.1.10.5.1.2.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_ingonglv_sat",  val);
			//hfc_r_transpower
			ParamMibOID = ".1.3.6.1.4.1.17409.1.10.3.1.7.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_r_transpower_sat",  val);			
			//hfc_r_biascurrent
			ParamMibOID = ".1.3.6.1.4.1.17409.1.10.3.1.2.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_r_biascurrent_sat",  val);			
			//hfc_out_level
			ParamMibOID = ".1.3.6.1.4.1.17409.1.10.11.1.4.1";
			extraoid = ".13" +	ParamMibOID;
			AlarmSatOid = new OID(AlarmSatOidStr + extraoid);
			val = util.gethfcINT32PDU(ip, "161", AlarmSatOid);
			json.put("hfc_out_level_sat",  val);
		}catch(Exception e){
			
		}
		jedis.publish("node.opt.hfcrealtime", json.toJSONString());
	}
}