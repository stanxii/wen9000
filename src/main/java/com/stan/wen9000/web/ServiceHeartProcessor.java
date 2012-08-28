package com.stan.wen9000.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.web.SnmpUtil;
import com.stan.wen9000.action.jedis.util.RedisUtil;

public class ServiceHeartProcessor{	
	private static Logger log = Logger.getLogger(ServiceHeartProcessor.class);
	private static final String HEART_QUEUE_NAME = "heart_queue";
	private static final String STSCHANGE_QUEUE_NAME = "stschange_queue";

	
	private static RedisUtil redisUtil;
	
	
	private static SnmpUtil util = new SnmpUtil();
	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceHeartProcessor.redisUtil = redisUtil;
	}
	
	
	private   JedisPubSub jedissubSub = new JedisPubSub() {
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

      	//System.out.println("[x]ServiceHearbertProcesser  Subscribing....pmessage....now receive on msgarge1 [" + arg1 + "] arg2=["+msg +"]");
      	try {
  			//arg2 is mssage now is currenti p
  			
  			
  			
  			servicestart(msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();			
  		}
  		
      }

  };
  

	
	public void start(){
	
		System.out.println("[#3] ..... service hearbert starting");
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		 jedis.psubscribe(jedissubSub, "servicehearbert.*");
		redisUtil.getJedisPool().returnResource(jedis);
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}
	
	private void servicestart(String message) throws InterruptedException, ParseException, IOException{
	
	
		
		//System.out.println(" [x] ServiceHeartProcessor Received '" + message
		//		+ "'");
		
		//long start = System.currentTimeMillis();  			
		dowork(message);					

		//long end = System.currentTimeMillis();  
		//System.out.println("one ServiceHeartProcessor dowork spend: " + ((end - start)) + " milliseconds");  
	}

		
	
	
	
	private void dowork(String message) throws ParseException, IOException{
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
	
	private void doheart(Map<String,String> heart) throws IOException{
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
	
	private void doheartcbat(String cbatmac, String cbatip, String type) throws IOException {
			Jedis jedis=null;
			try {
			 jedis = redisUtil.getConnection();
			
			
			}catch(Exception e){
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				
			}
			
			
		//判断头端是否已存在
		if(jedis.exists("mac:"+cbatmac+":deviceid")){
			//头端已存在
			String deviceid = jedis.get("mac:"+cbatmac+":deviceid");
			String cbatkey = "cbatid:"+deviceid+":entity";
			if(jedis.hget("cbatid:"+deviceid+":cbatinfo", "appver").equalsIgnoreCase("")){
				String appver = util.getStrPDU(jedis.hget(cbatkey, "ip"), "161", new OID(new int[] {1, 3, 6, 1, 4, 1, 36186, 8, 4, 4, 0 }));
				
				if(appver != ""){
					jedis.hset("cbatid:"+deviceid+":cbatinfo", "appver", appver);
				}
			}
			if(jedis.hget(cbatkey, "active").equalsIgnoreCase("1")==false){				
				//jedis.lpush(STSCHANGE_QUEUE_NAME, deviceid);
				jedis.hset(cbatkey,"active", "1");
				
//				//判断新头端ip是否与已发现头端重复
//				Set<String> cbats = jedis.keys("cbatid:*:entity");
//				for(Iterator it= cbats.iterator();it.hasNext();){
//					String ckey = it.next().toString();
//					if(jedis.hget(ckey, "ip").equalsIgnoreCase(cbatip) && (!jedis.hget(ckey, "mac").equalsIgnoreCase(cbatmac))){
//						//编辑告警信息
//						Map<String, String> alarmhash=new LinkedHashMap();
//						alarmhash.put("runingtime", "N/A");
//						alarmhash.put("oid", "N/A");
//						alarmhash.put("alarmcode", "200934");		
//						alarmhash.put("cbatmac", cbatmac); 		
//						Date date = new Date();
//						DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
//						String alarmtimes = format.format(date);
//						alarmhash.put("salarmtime", alarmtimes);
//						alarmhash.put("alarmlevel", "1");
//						alarmhash.put("cnalarminfo", "新发现头端["+cbatmac+"]IP地址冲突！");
//						alarmhash.put("enalarminfo", "New Cbat["+cbatmac+ "]IP Conflict!");
//						
//						String msgservice = JSONValue.toJSONString(alarmhash);
//						jedis.publish("servicealarm.new", msgservice);
//						redisUtil.getJedisPool().returnResource(jedis);
//						return;
//					}
//				}
				//cbat状态有变迁,发往STSCHANGE_QUEUE_NAME
				Sendstschange("cbat",deviceid,jedis);
			}
			
			//更新头端信息			
			jedis.hset(cbatkey,"ip", cbatip);
			
//			cbat.setAppversion(util.getStrPDU(cbatip, "161",
//					new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8,4, 4, 0 })));
			//更新头端时间戳
			Date date = new Date();
			long time = date.getTime();
			jedis.hset(cbatkey, "timeticks", String.valueOf(time));
			
		}else{
			//新头端
			//判断新头端ip是否与已发现头端重复
			Set<String> cbats = jedis.keys("cbatid:*:entity");
			for(Iterator it= cbats.iterator();it.hasNext();){
				String cbatkey = it.next().toString();
				if(jedis.hget(cbatkey, "ip").equalsIgnoreCase(cbatip)){
					//编辑告警信息
					Map<String, String> alarmhash=new LinkedHashMap();
					alarmhash.put("runingtime", "N/A");
					alarmhash.put("oid", "N/A");
					alarmhash.put("alarmcode", "200934");		
					alarmhash.put("cbatmac", cbatmac); 		
					Date date = new Date();
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
					String alarmtimes = format.format(date);
					alarmhash.put("salarmtime", alarmtimes);
					alarmhash.put("alarmlevel", "1");
					alarmhash.put("cnalarminfo", "新发现头端["+cbatmac+"]IP地址冲突！");
					alarmhash.put("enalarminfo", "New Cbat["+cbatmac+ "]IP Conflict!");
					
					String msgservice = JSONValue.toJSONString(alarmhash);
					jedis.publish("servicealarm.new", msgservice);
					redisUtil.getJedisPool().returnResource(jedis);
					return;
				}
			}
			
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
			cbatentity.put("upgrade", "20");
			//保存头端信息
			jedis.hmset(scbatentitykey, cbatentity);
			
			//更新头端时间戳
			Date date = new Date();
			long time = date.getTime();
			jedis.hset(scbatentitykey, "timeticks", String.valueOf(time));			
			
			/////////////////////////////save cbatinfo
			Map<String , String >  hash = new HashMap<String, String>();
			 
			String scbatinfokey = "cbatid:" + icbatid + ":cbatinfo";
			hash.put("address", "N/A");
			hash.put("phone", "N/A");
			hash.put("bootver", "cml-boot-v1.1.0_for_linux_sdk");
			hash.put("contact", "N/A");
			hash.put("upsoftdate", "2012-08-21 15:22:00");
			//获取设备相关信息
			try{
				 int agentport = util.getINT32PDU(cbatip, "161", new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 2, 7, 0 }));					
				 String appver = util.getStrPDU(cbatip, "161", new OID(new int[] {1, 3, 6, 1, 4, 1, 36186, 8, 4, 4, 0 }));
				 int mvlanid =  util.getINT32PDU(cbatip, "161", new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 5, 0 }));				    				   
			     int mvlanenable = util.getINT32PDU(cbatip, "161", new OID(	new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 4, 0 }));
			     String trapserverip = util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
			     String netmask = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,2,0})));
			     String gateway = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,3,0})));
				 hash.put("agentport", String.valueOf(agentport));
				 hash.put("appver", appver);
				 hash.put("mvlanid",String.valueOf(mvlanid));
				 hash.put("mvlanenable", String.valueOf(mvlanenable));
				 hash.put("trapserverip", trapserverip);
				 hash.put("netmask", netmask);
				 hash.put("gateway", gateway);
				 hash.put("dns", "192.168.223.1");
				 hash.put("telnet", "300");
			}catch(Exception e){
				
			}
			jedis.hmset(scbatinfokey, hash);
			
			jedis.save();
			//发现新cbat,发往STSCHANGE_QUEUE_NAME
			//jedis.lpush(STSCHANGE_QUEUE_NAME, String.valueOf(icbatid));
			Sendstschange("cbat",String.valueOf(icbatid),jedis);
		}
		
		
		redisUtil.getJedisPool().returnResource(jedis);
						
				
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

		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();
		}catch(Exception e){
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			e.printStackTrace();
			return;
		}	
		//判断所属头端是否存在
		if(jedis.get("mac:"+cbatmac+":deviceid")==null){
			return;
		}
		//判断cnu是否已存在
		if(jedis.exists("mac:"+cnumac+":deviceid")){
			String cnuid = jedis.get("mac:"+cnumac+":deviceid");
			//log.info("cnumac-----"+cnumac + "----------cnuid-----"+cnuid);
			//cnu已存在
			//以下判断是否有移机操作
			//获取redis中CNU所属cbatmac
			String tmpcbatid = jedis.hget("cnuid:"+cnuid+":entity", "cbatid");
			if(jedis.hget("cbatid:"+tmpcbatid+":entity", "mac").equalsIgnoreCase(cbatmac)){
				//没有移机
				
			}else{
				//发现移机操作
				String cur_cbatid = jedis.get("mac:"+cbatmac+":deviceid");
				//修改CNU移机先关信息
				jedis.hset("cnuid:"+cnuid+":entity", "cbatid", cur_cbatid);
				jedis.smove("cbatid:"+tmpcbatid+":cnus", "cbatid:"+cur_cbatid+":cnus", cnuid);	
				
				//通知前端移机事件
				jedis.hset("cnuid:"+cnuid+":entity", "devcnuid", cnuindex);
				jedis.hset("cnuid:"+cnuid+":entity", "active", active);
				Sendstschange("cnu",cnuid,jedis);
				//产生移机告警信息
				Map<String, String> alarmhash=new LinkedHashMap();
				alarmhash.put("runingtime", "N/A");
				alarmhash.put("oid", "N/A");
				alarmhash.put("alarmcode", "200933");		
				alarmhash.put("cbatmac", cbatmac); 		
				Date date = new Date();
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
				String alarmtimes = format.format(date);
				alarmhash.put("salarmtime", alarmtimes);
				alarmhash.put("alarmlevel", "3");
				String cbatid = jedis.get("mac:"+cbatmac+":deviceid");
				alarmhash.put("cnalarminfo", "CNU["+cnumac+"]从头端["+jedis.hget("cbatid:"+tmpcbatid+":entity", "label")+"]移动到["+
						jedis.hget("cbatid:"+cbatid+":entity", "label")+"]");
				alarmhash.put("enalarminfo", "CNU["+cnumac+"] Move from Cbat["+jedis.hget("cbatid:"+tmpcbatid+":entity", "label")+ "]To ["
						+jedis.hget("cbatid:"+cbatid+":entity", "label")+"]");
				
				String msgservice = JSONValue.toJSONString(alarmhash);
				jedis.publish("servicealarm.new", msgservice);
				redisUtil.getJedisPool().returnResource(jedis);
				return;
			}
			if(jedis.hget("cnuid:"+cnuid+":entity", "active").equalsIgnoreCase(active)==false){
				//cnu状态有变迁,发往STSCHANGE_QUEUE_NAME
				//jedis.lpush(STSCHANGE_QUEUE_NAME, cnuid);
				jedis.hset("cnuid:"+cnuid+":entity", "active", active);
				Sendstschange("cnu",cnuid,jedis);
				
			}
			//cnu其它信息修改
			
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
			//判断设备是否被预开户
			if(jedis.exists("preconfig:"+cnumac.toLowerCase()+":entity")){
				//预开户
				String proid = jedis.get("preconfig:"+cnumac.toLowerCase()+":entity");
				cnuentity.put("profileid", proid);
				//添加cnu到profile集合中
				jedis.sadd("profileid:"+proid+":cnus", cnumac.toLowerCase());
				//发送配置事件	
				JSONObject configjson = new JSONObject();
				configjson.put("cbatip", jedis.hget("cbatid:"+jedis.get("mac:"+cbatmac+":deviceid")+":entity", "ip"));
				configjson.put("devcnuid",cnuindex.toLowerCase().trim());
				configjson.put("proid", proid);
				configjson.put("cbatmac", cbatmac);
				configjson.put("cnumac", cnumac);
				jedis.publish("servicesendconfig.preconfig", configjson.toJSONString());
				//删除预开户表
				jedis.del("preconfig:"+cnumac.toLowerCase()+":entity");		
				//预开户告警信息
				Map<String, String> alarmhash=new LinkedHashMap();
				alarmhash.put("runingtime", "N/A");
				alarmhash.put("oid", "N/A");
				alarmhash.put("alarmcode", "200932");
				alarmhash.put("cnalarminfo", "头端标识为"+jedis.hget("cbatid:"+jedis.get("mac:"+cbatmac+":deviceid")+":entity", "label")+"下的CNU["+cnumac+"]预开户");
				alarmhash.put("enalarminfo", "CNU["+cnumac+"] Under Cbat["+cbatmac+ "] PreConfig!" );
				alarmhash.put("cbatmac", "N/A"); 
				alarmhash.put("alarmlevel", "3");
				Date date = new Date();
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
				String alarmtimes = format.format(date);
				alarmhash.put("salarmtime", alarmtimes);
				String msgservice = JSONValue.toJSONString(alarmhash);
				jedis.publish("servicealarm.new", msgservice);
				
			}else{
				//未预开户设备
				//发送到配置进程，下发默认初始配置
//				String defaultproid;
//				if((defaultproid = jedis.get("global:defaultprofileid"))==null){
//					jedis.set("global:defaultprofileid", "1");
//					defaultproid = "1";
//				}
//				JSONObject sendjson = new JSONObject();
//				String cbatid = jedis.get("mac:"+cbatmac+":deviceid");
//				sendjson.put("cbatip", jedis.hget("cbatid:"+cbatid+":entity", "ip"));
//				sendjson.put("devcnuid", cnuindex.toLowerCase().trim());
//				sendjson.put("proid", defaultproid);
//				jedis.publish("servicesendconfig.defaultconfig", sendjson.toJSONString());
//				
//				cnuentity.put("profileid", defaultproid);
				//暂不下发配置
				cnuentity.put("profileid", "1");
				//添加cnu到profile集合中
				jedis.sadd("profileid:1:cnus", cnumac.toLowerCase());
			}
			//将cnuid添加到所属头端下的集合中
			jedis.sadd("cbatid:"+jedis.get("mac:"+cbatmac+":deviceid")+":cnus", Long.toString(icnuid));

			//save
			jedis.hmset(scnuentitykey, cnuentity);
			jedis.save();
			//发现新cnu,发往STSCHANGE_QUEUE_NAME
			//jedis.lpush(STSCHANGE_QUEUE_NAME, String.valueOf(icnuid));
			Sendstschange("cnu",String.valueOf(icnuid),jedis);

		}

		redisUtil.getJedisPool().returnResource(jedis);
					
	}
	
	public void doOffline_heart(String cbatmac, String cnuindex, String cnumac,
			String cnutype) {
		
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();
		
		
		}catch(Exception e){
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		//判断所属头端是否存在
		if(jedis.get("mac:"+cbatmac+":deviceid")==null){
			return;
		}
		//判断cnu是否已存在
		//可注释此段代码，离线设备不予发现
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
			//暂将profileid置1
			cnuentity.put("profileid", "1");
			
			//将cnuid添加到所属头端下的集合中
			jedis.sadd("cbatid:"+jedis.get("mac:"+cbatmac+":deviceid")+":cnus", Long.toString(icnuid));
			//save
			jedis.hmset(scnuentitykey, cnuentity);
			
			//添加cnu到profile集合中
			jedis.sadd("profileid:1:cnus", cnumac);
			
			jedis.bgsave();
			//发现新cnu,发往STSCHANGE_QUEUE_NAME
			//jedis.lpush(STSCHANGE_QUEUE_NAME, String.valueOf(icnuid));
			Sendstschange("cnu",String.valueOf(icnuid),jedis);
			redisUtil.getJedisPool().returnResource(jedis);	
			return;
		}
		//以下判断是否是所属头端发出的心跳
		String cnuid = jedis.get("mac:"+cnumac+":deviceid");

		String cur_cbatid = jedis.hget("cnuid:"+cnuid+":entity", "cbatid");

		if(jedis.hget("cbatid:"+cur_cbatid+":entity", "mac").equalsIgnoreCase(cbatmac)){
			//是所属头端发出的心跳
			if(jedis.hget("cnuid:"+cnuid+":entity", "active").equalsIgnoreCase("0")==false){
				//cnu状态有变迁,发往STSCHANGE_QUEUE_NAME
				//jedis.lpush(STSCHANGE_QUEUE_NAME, cnuid);
				//修改CNU相关信息
				jedis.hset("cnuid:"+cnuid+":entity", "active", "0");
				Sendstschange("cnu",cnuid,jedis);
			}
			
		}else{
			//不是所属头端发出的心跳
			redisUtil.getJedisPool().returnResource(jedis);
			return;

		}
		
		redisUtil.getJedisPool().returnResource(jedis);
			
	}
	
	private void Sendstschange(String type,String devid,Jedis jedis){ 
		JSONObject json = new JSONObject();
		if(type == "cbat"){
			String cbatkey = "cbatid:"+devid+":entity";
			json.put("mac", jedis.hget(cbatkey,"mac"));
			json.put("online", jedis.hget(cbatkey,"active"));
			json.put("ip", jedis.hget(cbatkey,"ip"));
			json.put("label", jedis.hget(cbatkey,"label"));
			json.put("type", "cbat");
		}else if(type == "cnu"){
			String cbatid = jedis.hget("cnuid:"+devid+":entity","cbatid");
			String cnukey = "cnuid:"+devid+":entity";
			json.put("mac", jedis.hget(cnukey,"mac"));
			json.put("online", jedis.hget(cnukey,"active"));
			json.put("cbatmac", jedis.hget("cbatid:"+cbatid+":entity","mac"));
			json.put("srcmac", "");
			json.put("type", "cnu");
			
		}else if(type == "hfc"){
			String hfckey = "hfcid:"+devid+":entity";
			json.put("mac", jedis.hget(hfckey,"mac"));
			json.put("active", jedis.hget(hfckey,"active"));
			json.put("ip", jedis.hget(hfckey,"ip"));
			json.put("type", "hfc");
			json.put("sn", jedis.hget(hfckey,"serialnumber"));
			json.put("hp", jedis.hget(hfckey,"hfctype"));
			json.put("id", jedis.hget(hfckey,"logicalid"));
			
		}
		String jsonString = json.toJSONString(); 
	    jedis.publish("node.tree.statuschange", jsonString);
	}
	
}