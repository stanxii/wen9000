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
			if(jedis.get("global:displaymode").equalsIgnoreCase("1")){
				String key = jedis.get("global:hfcrealtime");
				if(key != ""){
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
							int val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,2,0}) );
							json.put("inpower",  val/10 + "."+val%10 + "dBm");
							val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,3,0}) );
							json.put("outpower",  val/10 + "."+val%10 + "dBm");
							val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,7,1,2,1}) );
							json.put("power_v",  val/10 + "."+val%10 + "V");
							val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,2,1}) );
							json.put("bias_c1",  val/10 + "."+val%10 + "mA");
							val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,2,2}) );
							json.put("bias_c2",  val/10 + "."+val%10 + "mA");
							val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,3,1}) );
							json.put("ref_c1",  val/10 + "."+val%10 + "A");
							val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,3,2}) );
							json.put("ref_c2",  val/10 + "."+val%10 + "A");
							val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,4,1}) );
							json.put("pump_t1", val/10 + "."+val%10 + "℃");
							val = util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,11,4,1,4,2}) );							
							json.put("pump_t2", val/10 + "."+val%10 + "℃");
							json.put("temp", util.gethfcINT32PDU(ip, "161", new OID(new int[] {1,3,6,1,4,1,17409,1,3,1,13,0}) ) + "℃");
						}
						jedis.publish("node.opt.hfcrealtime", json.toJSONString());
						
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
				try {
					Thread.sleep(30000);
					log.info("------------------>>>>sleep 30s");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				redisUtil.getJedisPool().returnResource(jedis);
			}
		}
	}
	
}