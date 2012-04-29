package com.stan.wen9000.web;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.stan.wen9000.web.SnmpUtil;
import com.stan.wen9000.action.jedis.util.RedisUtil;

public class ServiceHeartProcessor{	
	private static Logger log = Logger.getLogger(ServiceAlarmProcessor.class);
	private static final String HEART_QUEUE_NAME = "heart_queue";
	private static final String STSCHANGE_QUEUE_NAME = "stschange_queue";

	
	private static RedisUtil redisUtil;
	private static Jedis jedis;
	
	private static SnmpUtil util = new SnmpUtil();
	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceHeartProcessor.redisUtil = redisUtil;
	}
	

	
	public void start(){
		log.info("[#3] ..... service heart");
		
		try{
			servicestart();
		}catch(Exception e){
			redisUtil.closeConnection(jedis);
		}		
	}
	
	private void servicestart() throws InterruptedException, ParseException{
	
		jedis = redisUtil.getConnection();
		
		while(true){
			String message = "";
			
			message = jedis.rpop(HEART_QUEUE_NAME);
			
			
			if(message == null ) {				
				Thread.sleep(1000);
				continue;
			}
			
			System.out.println(" [x] ServiceHeartProcessor Received '" + message
					+ "'");
			
			long start = System.currentTimeMillis();  			
			dowork(message);					
			long end = System.currentTimeMillis();  
			System.out.println("one ServiceHeartProcessor dowork spend: " + ((end - start)) + " milliseconds");  
		}
	}
	
	
	
	private void dowork(String message) throws ParseException{
		JSONParser parser = new JSONParser();
		
		ContainerFactory containerFactory = new ContainerFactory(){
		    public List<?> creatArrayContainer() {
		      return new LinkedList<Object>();
		    }

		    public Map<?, ?> createObjectContainer() {
		      return new LinkedHashMap<Object, Object>();
		    }
		                        
		  };
		  
		  Map<String, String> heart = (Map<String, String>)parser.parse(message, containerFactory);
		  
		  doheart(heart);
	}
	
	private void doheart(Map<String,String> heart){
		String cbatip = "";
		String cbatmac = "";
		String cbattype = "";
		//解析cbat 心跳信息
		cbatip = heart.get("cbatip");
		cbatmac = heart.get("cbatmac");

		cbattype = heart.get("cbattype");
		//处理cbat 心跳信息
		doheartcbat(cbatmac, cbatip, cbattype);
		//解析cnu 心跳信息
		String cnumac = "";
		String cnutype = "";
		String cltindex = "";
		String cnuindex = "";
		String active = "";
		int count = 0;
		count = Integer.valueOf(heart.get("cnucount"));
		//遍历所有cnu
		for (int i = 0; i < count; i++) {
			cnumac = heart.get("cnumac"+i);
			cnutype = heart.get("cnutype"+i);
			cltindex = heart.get("cltindex"+i);
			cnuindex = heart.get("cnuindex"+i);
			active = heart.get("active"+i);
			//处理cnu心跳信息
			doheartcnu(cbatmac, cnumac, cnutype, cltindex, cnuindex, active);
		}
	}
	
	private void doheartcbat(String cbatmac, String cbatip, String type) {
		
		//判断头端是否已存在
		if(jedis.exists("mac:"+cbatmac+":deviceid")){
			//头端已存在
			String deviceid = jedis.get("mac:"+cbatmac+":deviceid");
			String cbatkey = "cbatid:"+deviceid+":entity";
			if(jedis.hget(cbatkey, "active").equalsIgnoreCase("1")==false){
				//cbat状态有变迁,发往STSCHANGE_QUEUE_NAME
				jedis.lpush(STSCHANGE_QUEUE_NAME, deviceid);
				
			}
			//更新头端信息
			jedis.hset(cbatkey,"active", "1");
			jedis.hset(cbatkey,"cbatip", cbatip);
//			cbat.setAppversion(util.getStrPDU(cbatip, "161",
//					new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8,4, 4, 0 })));
			//更新头端时间戳
			Date date = new Date();
			long time = date.getTime();
			jedis.hset(cbatkey, "timeticks", String.valueOf(time));
			
		}else{
			//新头端
			String cbatmackey = "mac:" +  cbatmac.toLowerCase().trim() + ":deviceid";
			long icbatid = jedis.incr("global:deviceid");
			jedis.set(cbatmackey, Long.toString(icbatid) );
			
			String scbatentitykey = "cbatid:" + icbatid + ":entity";
			Map<String , String >  cbatentity = new HashMap<String, String>();
			
			cbatentity.put("mac", cbatmac.toLowerCase().trim());
			cbatentity.put("active", "1");
			cbatentity.put("ip", cbatip.toLowerCase().trim());
			cbatentity.put("label", cbatmac.toLowerCase().trim());
			cbatentity.put("devicetype", type.toLowerCase().trim());
			//20 not have upgradestatus
			cbatentity.put("upgradestatus", "20");
			//保存头端信息
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
			//获取设备相关信息
			try{
				 int agentport = util.getINT32PDU(cbatip, "161", new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 2, 7, 0 }));					
				 String appver = util.getStrPDU(cbatip, "161", new OID(new int[] {1, 3, 6, 1, 4, 1, 36186, 8, 4, 4, 0 }));
				 int mvlanid =  util.getINT32PDU(cbatip, "161", new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 5, 0 }));				    				   
			     int mvlanenable = util.getINT32PDU(cbatip, "161", new OID(	new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 4, 0 }));
				 hash.put("agentport", String.valueOf(agentport));
				 hash.put("appver", appver);
				 hash.put("mvlanid",String.valueOf(mvlanid));
				 hash.put("mvlanenable", String.valueOf(mvlanenable));
			}catch(Exception e){
				
			}
			jedis.hmset(scbatinfokey, hash);
			
			jedis.save();
			//发现新cbat,发往STSCHANGE_QUEUE_NAME
			jedis.lpush(STSCHANGE_QUEUE_NAME, String.valueOf(icbatid));
		}
		
						
				
	}
	
	private void doheartcnu(String cbatmac, String cnumac, String type,
			String cltindex, String cnuindex, String active) {
		
		// CNU上线
		if (active.equalsIgnoreCase("1")) {
			doheartOnline(cbatmac, cnumac, type, cnuindex, active);

		} else // CNU offline
		{
			doOffline_heart(cbatmac, cnuindex, cnumac, type);

		}		
		
	}
	
	public void doheartOnline(String cbatmac, String cnumac, String cnutype,
			String cnuindex, String active) {
		
		//判断cnu是否已存在
		if(jedis.exists("mac:"+cnumac+":deviceid")){
			String cnuid = jedis.get("mac:"+cnumac+":deviceid");
			//cnu已存在
			//一下判断是否有移机操作
			//获取redis中CNU所属cbatmac
			String tmpcbatid = jedis.hget("cnuid:"+cnuid+":entity", "cbatid");
			if(jedis.hget("cbatid:"+tmpcbatid+":entity", "cbatmac").equalsIgnoreCase(cbatmac)){
				//没有移机
				
			}else{
				//发现移机操作
				String cur_cbatid = jedis.get("mac:"+cbatmac+":deviceid");
				//修改CNU移机先关信息
				jedis.hset("cnuid:"+cnuid+":entity", "cbatid", cur_cbatid);
				jedis.smove("cbatid:"+tmpcbatid+":cnus", "cbatid:"+cur_cbatid+":cnus", cnuid);				
			}
			if(jedis.hget("cnuid:"+cnuid+":entity", "active").equalsIgnoreCase(active)==false){
				//cnu状态有变迁,发往STSCHANGE_QUEUE_NAME
				jedis.lpush(STSCHANGE_QUEUE_NAME, cnuid);
				
			}
			//cnu其它信息修改
			jedis.hset("cnuid:"+cnuid+":entity", "active", active);
			jedis.hset("cnuid:"+cnuid+":entity", "devcnuid", cnuindex);
			
		}else{
			//发现新cnu
			String cnumackey = "mac:" +  cnumac.toLowerCase().trim() + ":deviceid";
			long icnuid = jedis.incr("global:deviceid");		
			jedis.set(cnumackey, Long.toString(icnuid) );
			//组合cnu信息
			String scnuentitykey = "cnuid:" + icnuid + ":entity";
			Map<String , String >  cnuentity = new HashMap<String, String>();			
			cnuentity.put("mac", cnumac.toLowerCase().trim());
			cnuentity.put("active", active);
			cnuentity.put("devcnuid", cnuindex.toLowerCase().trim());//设备上cnu的索引
			cnuentity.put("label", cnumac.toLowerCase().trim());
			cnuentity.put("devicetype", cnutype.toLowerCase().trim());
			cnuentity.put("cbatid", jedis.get("mac:"+cbatmac+":deviceid"));
			//cnuentity.put("profileid", jedis.get("mac:"+cbatmac+":deviceid"));
			
			//将cnuid添加到所属头端下的集合中
			jedis.sadd("cbatid:"+jedis.get("mac:"+cbatmac+":deviceid")+":cnus", Long.toString(icnuid));
			//save
			jedis.hmset(scnuentitykey, cnuentity);
			jedis.save();
			//发现新cnu,发往STSCHANGE_QUEUE_NAME
			jedis.lpush(STSCHANGE_QUEUE_NAME, String.valueOf(icnuid));
		}
		
					
	}
	
	public void doOffline_heart(String cbatmac, String cnuindex, String cnumac,
			String cnutype) {
		
		//判断cnu是否已存在
		if(!(jedis.exists("mac:"+cnumac+":deviceid"))){
			//发现新cnu
			String cnumackey = "mac:" +  cnumac.toLowerCase().trim() + ":deviceid";
			long icnuid = jedis.incr("global:deviceid");		
			jedis.set(cnumackey, Long.toString(icnuid) );
			//组合cnu信息
			String scnuentitykey = "cnuid:" + icnuid + ":entity";
			Map<String , String >  cnuentity = new HashMap<String, String>();			
			cnuentity.put("mac", cnumac.toLowerCase().trim());
			cnuentity.put("active", "0");
			cnuentity.put("devcnuid", cnuindex.toLowerCase().trim());//设备上cnu的索引
			cnuentity.put("label", cnumac.toLowerCase().trim());
			cnuentity.put("devicetype", cnutype.toLowerCase().trim());
			cnuentity.put("cbatid", jedis.get("mac:"+cbatmac+":deviceid"));
			//cnuentity.put("profileid", jedis.get("mac:"+cbatmac+":deviceid"));
			
			//将cnuid添加到所属头端下的集合中
			jedis.sadd("cbatid:"+jedis.get("mac:"+cbatmac+":deviceid")+":cnus", Long.toString(icnuid));
			//save
			jedis.hmset(scnuentitykey, cnuentity);
			
			//发现新cnu,发往STSCHANGE_QUEUE_NAME
			jedis.lpush(STSCHANGE_QUEUE_NAME, String.valueOf(icnuid));
			
		}
		//一下判断是否是所属头端发出的心跳
		String cnuid = jedis.get("mac:"+cnumac+":deviceid");
		String cur_cbatid = jedis.hget("cnuid:"+cnuid+":entity", "cbatid");
		if(jedis.hget("cbatid:"+cur_cbatid+":entity", "mac").equalsIgnoreCase(cbatmac)){
			//是所属头端发出的心跳
			if(jedis.hget("cnuid:"+cnuid+":entity", "active").equalsIgnoreCase("0")==false){
				//cnu状态有变迁,发往STSCHANGE_QUEUE_NAME
				jedis.lpush(STSCHANGE_QUEUE_NAME, cnuid);
				
			}
			//修改CNU先关信息
			jedis.hset("cnuid:"+cnuid+":entity", "active", "0");
		}else{
			//不是所属头端发出的心跳
			return;
		}
		
			
	}
	
}