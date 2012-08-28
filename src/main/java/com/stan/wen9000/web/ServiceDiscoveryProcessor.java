package com.stan.wen9000.web;


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

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;
import com.stan.wen9000.reference.EocDeviceType;

public class ServiceDiscoveryProcessor  {





	EocDeviceType devicetype;

	private static SnmpUtil util = new SnmpUtil();
	  private static RedisUtil redisUtil;
	  
	  public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceDiscoveryProcessor.redisUtil = redisUtil;
	}

	
	  
	
	private  static JedisPubSub jedissubSub = new JedisPubSub() {
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

      	System.out.println("servicediscovery Subscribing....pmessage....now receive on msgarge1 [" + arg1 + "] arg2=["+msg +"]");
      	try {
  			//arg2 is mssage now is currenti p
  			
  			
  			
  			servicestart(msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();			
  		}
  		
      }

  };

	public void execute() {

		System.out.println("[#3] ..... service discovery starting");
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 jedis.psubscribe(jedissubSub, "servicediscovery.*");
		redisUtil.getJedisPool().returnResource(jedis);
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
		

		

	}

	public static void servicestart(String msg) throws Exception {

			 
		doWork(msg);
			
		
		
	}

	private static void doWork(String message) throws Exception {

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

	private static void doCbat(String message) throws ParseException {

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
		String trapserverip =(String) jsonobj.get("cbatinfo:trapserverip");
		String appver =(String) jsonobj.get("cbatinfo:appver");
		String mvlanid =(String) jsonobj.get("cbatinfo:mvlanid");
		String mvlanenable =(String) jsonobj.get("cbatinfo:mvlanenable");
		String netmask =(String) jsonobj.get("cbatinfo:netmask");
		String gateway =(String) jsonobj.get("cbatinfo:gateway");
		

		long start = System.currentTimeMillis();  

		String cbatmackey = "mac:" +  cbatmac.toLowerCase().trim() + ":deviceid";

		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		}catch(Exception e){
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}		
		
		//get cbatmac if exist in redis server
		String scbatid = jedis.get(cbatmackey);		
	
		long icbatid ;
		
		if(scbatid == null) {
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
			icbatid = jedis.incr("global:deviceid");		
			jedis.set(cbatmackey, Long.toString(icbatid) );
		}else {
			//icbatid = Long.parseLong(scbatid);
			//不是新发现的头端
			return;
		}

		String scbatentitykey = "cbatid:" + String.valueOf(icbatid) + ":entity";
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
	    
		//更新头端时间戳
		Date date = new Date();
		long time = date.getTime();
		jedis.hset(scbatentitykey, "timeticks", String.valueOf(time));
/////////////////////////////save cbatinfo
	    
		//发现新头端，通知前端		
		JSONObject json = new JSONObject();
		json.put("mac", cbatmac);
		json.put("active", "1");
		json.put("ip", cbatip);
		switch(Integer.parseInt(cbatdevicetype))
		{
			case 1:
				json.put("devicetype", "WEC-3501I X7");
	    		break;
	    	case 2:
	    		json.put("devicetype", "WEC-3501I E31");
	    		break;
	    	case 3:
	    		json.put("devicetype", "WEC-3501I Q31");
	    		break;
	    	case 4:
	    		json.put("devicetype", "WEC-3501I C22");
	    		break;
	    	case 5:
	    		json.put("devicetype", "WEC-3501I S220");
	    		break;
	    	case 6:
	    		json.put("devicetype", "WEC-3501I S60");
	    		break;
	    	case 20:
	    		json.put("devicetype", "WEC9720EK C22");
        		break;
        	case 21:
        		json.put("devicetype", "WEC9720EK E31");
        		break;
        	case 22:
        		json.put("devicetype", "WEC9720EK Q31");
        		break;
        	case 23:
        		json.put("devicetype", "WEC9720EK S220");
        		break;
        	case 24:
        		json.put("devicetype", "WEC9720EK SD220");
        		break;
        	case 36:
        		json.put("devicetype", "WEC701 M0");
        		break;
        	case 40:
        		json.put("devicetype", "WEC701 C2");
        		break;
        	case 41:
        		json.put("devicetype", "WEC701 C4");
        		break;
	    	default:
	    		json.put("devicetype", "Unknown");
	    		break;
		}
		jedis.publish("node.dis.findcbat", json.toJSONString());

		Sendstschange("cbat",String.valueOf(icbatid),jedis);
		
		Map<String , String >  hash = new HashMap<String, String>();

		String scbatinfokey = "cbatid:" + icbatid + ":cbatinfo";
		hash.put("address", "N/A");
		hash.put("phone", "13988777");
		hash.put("bootver", "cml-boot-v1.1.0_for_linux_sdk");
		hash.put("contact", "N/A");
		hash.put("agentport", agentport);
		hash.put("trapserverip", trapserverip);
		hash.put("appver", appver);
		hash.put("mvlanid", mvlanid);
		hash.put("mvlanenable", mvlanenable);
		hash.put("netmask", netmask);
		hash.put("gateway", gateway);
		hash.put("dns", "192.168.223.1");
		hash.put("telnet", "300");
		hash.put("upsoftdate", "2012-08-21 15:22:00");
		jedis.hmset(scbatinfokey, hash);
		
		//trapserverip and port
		SaveTrapServer(jedis,String.valueOf(icbatid));
		
		redisUtil.getJedisPool().returnResource(jedis);

	}
	
	@SuppressWarnings("rawtypes")
	private static void doHfc(String message) throws ParseException {

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
		    
		String hfctype =(String) jsonobj.get("hfctype");
		if(hfctype.equalsIgnoreCase("掺铒光纤放大器")){
			saveEDFA(hfctype,jsonobj);			
		}else if(hfctype.equalsIgnoreCase("1310nm光发射机")){
			save1310(hfctype,jsonobj);			
		}
		
		
		
	}
	
	private static void SaveTrapServer(Jedis jedis,String cbatid){
		//判断并修改设备trapserver ip/port
		String devtrapserverip = null;
		Integer trap_port = 0;
		String cbatip = jedis.hget("cbatid:"+cbatid+":entity", "ip");
		String cbatinfokey = "cbatid:"+cbatid+":cbatinfo";
		try {
			devtrapserverip = util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
			trap_port = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,7,0}));
		}
		catch(Exception e){
			return;
		}

		if(devtrapserverip==""){
			return;
		}
		
		//如果global:trapserver:ip键不存在，创建之
		if(jedis.get("global:trapserver:ip") == null){
			jedis.set("global:trapserver:ip", "192.168.223.251");
			jedis.set("global:trapserver:port", "162");
		}

		//if systemconfig db trap ip = device trap ip not need set trap server ip
		if( !jedis.get("global:trapserver:ip").equalsIgnoreCase(devtrapserverip)){
			try {
				//set trap server ip
			util.setV2StrPDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}), 
					jedis.get("global:trapserver:ip")
					);
			//save
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), 
					new Integer32(1)
					);

			jedis.hset(cbatinfokey, "trapserverip", jedis.get("global:trapserver:ip"));
			//reset
			/*
			util.setV2PDU(currentip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,6,1,0}), 
					new Integer32(1)
					);
			 */
			 		
			}catch(Exception e){
				//e.printStackTrace();
			}
		}
		//if trap port != systemconfig db trap port
		if(trap_port != Integer.valueOf(jedis.get("global:trapserver:port")))
		{
			try {
				//set trap server ip
				util.setV2PDU(cbatip,
						"161",
						new OID(new int[] {1,3,6,1,4,1,36186,8,2,7,0}), 
						new Integer32(Integer.valueOf(jedis.get("global:trapserver:port")))
						);
				//save
				util.setV2PDU(cbatip,
						"161",
						new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), 
						new Integer32(1)
						);
				
				jedis.hset(cbatinfokey, "agentport", String.valueOf(trap_port));
			}catch(Exception e){
				
			}
		}
	}
	
	private static void Sendstschange(String type,String devid,Jedis jedis){ 
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
			json.put("cbatmac", jedis.hget(cnukey,jedis.hget("cbatid:"+cbatid+":entity","mac")));
			json.put("type", "cnu");
			
		}else if(type == "hfc"){
			String hfckey = "hfcid:"+devid+":entity";
			json.put("mac", jedis.hget(hfckey,"mac"));
			json.put("lable", jedis.hget(hfckey,"ip"));
			json.put("online", jedis.hget(hfckey,"active"));
			json.put("ip", jedis.hget(hfckey,"ip"));
			json.put("type", "hfc");
			json.put("mn", jedis.hget(hfckey,"modelnumber"));
			json.put("hp", jedis.hget(hfckey,"hfctype"));
			json.put("id", jedis.hget(hfckey,"logicalid"));
			
		}
		String jsonString = json.toJSONString(); 
	    jedis.publish("node.tree.statuschange", jsonString);
	}
	
	private static void save1310(String hfctype,Map jsonobj){
		String ip =(String) jsonobj.get("ip");
		//String gateway =(String) jsonobj.get("gateway");
		//String oid =(String) jsonobj.get("oid");
		String hfcmac =(String) jsonobj.get("hfcmac");			
		String version =(String) jsonobj.get("version");
		String logicalid =(String) jsonobj.get("logicalid");
		String modelnumber =(String) jsonobj.get("modelnumber");
		String serialnumber =(String) jsonobj.get("serialnumber");
		String trapip1 =(String) jsonobj.get("trapip1");
		String trapip2 =(String) jsonobj.get("trapip2");
		String trapip3 =(String) jsonobj.get("trapip3");
		String power1 =(String) jsonobj.get("power1");
		String power_v1 =(String) jsonobj.get("power_v1");
		String power2 =(String) jsonobj.get("power2");
		String power_v2 =(String) jsonobj.get("power_v2");
		String power3 =(String) jsonobj.get("power3");
		String power_v3 =(String) jsonobj.get("power_v3");
		String channelnum = String.valueOf(jsonobj.get("channelnum"));
		String wavelength = (String) jsonobj.get("wavelength");
		String rfattrange = (String) jsonobj.get("rfattrange");
		String lasertype = (String) jsonobj.get("lasertype");
		String outputpower = (String) jsonobj.get("outputpower");
		String agccontrol = (String) jsonobj.get("agccontrol");
		String lasercurrent = (String) jsonobj.get("lasercurrent");
		String temp = (String) jsonobj.get("temp");
		String teccurrent = (String) jsonobj.get("teccurrent");
		String drivelevel = (String) jsonobj.get("drivelevel");
		String mgc = (String) jsonobj.get("mgc");
		String agc = (String) jsonobj.get("agc");
		String innertemp = (String) jsonobj.get("innertemp");
		String hfckey = "mac:" +  hfcmac.toLowerCase().trim() + ":deviceid";
		
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();
		}catch(Exception e){
			redisUtil.getJedisPool().returnBrokenResource(jedis);			
		}
		
		//get hfcmac if exist in redis server
		String shfcid = jedis.get(hfckey);
		
		long hfcid ;
		
		if(shfcid == null) {
			hfcid = jedis.incr("global:deviceid");
			jedis.set(hfckey, Long.toString(hfcid));
		}else {
			hfcid = Long.parseLong(shfcid);			
		}		
		
		String shfcentitykey = "hfcid:" + hfcid + ":entity";
		Map<String , String >  hfcentity = new HashMap<String, String>();
		 
		hfcentity.put("mac", hfcmac.toLowerCase().trim());
		//hfcentity.put("oid", oid);
		hfcentity.put("ip", ip.toLowerCase().trim());
		//hfcentity.put("gateway", gateway.toLowerCase().trim());
		hfcentity.put("active", "1");
		hfcentity.put("lable", ip.toLowerCase().trim());
		hfcentity.put("hfctype", hfctype.toLowerCase().trim());
		hfcentity.put("version", version.toLowerCase().trim());
		hfcentity.put("logicalid", logicalid.toLowerCase().trim());
		hfcentity.put("modelnumber", modelnumber.toLowerCase().trim());
		hfcentity.put("serialnumber", serialnumber.toLowerCase().trim());
		hfcentity.put("trapip1", trapip1.toLowerCase().trim());
		hfcentity.put("trapip2", trapip2.toLowerCase().trim());
		hfcentity.put("trapip3", trapip3.toLowerCase().trim());
		hfcentity.put("power1", power1.trim());
		hfcentity.put("power_v1", power_v1.trim());
		hfcentity.put("power2", power2.trim());
		hfcentity.put("power_v2", power_v2.trim());
		hfcentity.put("power3", power3.trim());
		hfcentity.put("power_v3", power_v3.trim());
		hfcentity.put("channelnum", channelnum);
		hfcentity.put("wavelength", wavelength);
		hfcentity.put("rfattrange", rfattrange);
		hfcentity.put("lasertype", lasertype);
		hfcentity.put("outputpower", outputpower);
		hfcentity.put("agccontrol", agccontrol);	   		 
		hfcentity.put("lasercurrent", lasercurrent);
		hfcentity.put("temp", temp);
		hfcentity.put("teccurrent", teccurrent);
		hfcentity.put("drivelevel", drivelevel);
		hfcentity.put("mgc", mgc);
		hfcentity.put("agc", agc);
		hfcentity.put("innertemp", innertemp);
		jedis.hmset(shfcentitykey, hfcentity);

		jedis.save();
		
		Sendstschange("hfc",String.valueOf(hfcid),jedis);
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void saveEDFA(String hfctype,Map jsonobj){
		String ip =(String) jsonobj.get("ip");
		//String gateway =(String) jsonobj.get("gateway");
		//String oid =(String) jsonobj.get("oid");
		String hfcmac =(String) jsonobj.get("hfcmac");
		
		String version =(String) jsonobj.get("version");
		String logicalid =(String) jsonobj.get("logicalid");
		String modelnumber =(String) jsonobj.get("modelnumber");
		String serialnumber =(String) jsonobj.get("serialnumber");
		String trapip1 =(String) jsonobj.get("trapip1");
		String trapip2 =(String) jsonobj.get("trapip2");
		String trapip3 =(String) jsonobj.get("trapip3");
		String power1 =(String) jsonobj.get("power1");
		String power_v1 =(String) jsonobj.get("power_v1");
		String power2 =(String) jsonobj.get("power2");
		String power_v2 =(String) jsonobj.get("power_v2");
		String bias_c1 =(String) jsonobj.get("bias_c1");
		String bias_c2 =(String) jsonobj.get("bias_c2");
		String ref_c1 =(String) jsonobj.get("ref_c1");
		String ref_c2 =(String) jsonobj.get("ref_c2");
		String pump_t1 =(String) jsonobj.get("pump_t1");
		String pump_t2 =(String) jsonobj.get("pump_t2");

		String hfckey = "mac:" +  hfcmac.toLowerCase().trim() + ":deviceid";
		
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();
		}catch(Exception e){
			redisUtil.getJedisPool().returnBrokenResource(jedis);			
		}
		
		//get hfcmac if exist in redis server
		String shfcid = jedis.get(hfckey);
		
		long hfcid ;
		
		if(shfcid == null) {
			hfcid = jedis.incr("global:deviceid");
			jedis.set(hfckey, Long.toString(hfcid));
		}else {
			hfcid = Long.parseLong(shfcid);			
		}		
		
		String shfcentitykey = "hfcid:" + hfcid + ":entity";
		Map<String , String >  hfcentity = new HashMap<String, String>();
		 
		hfcentity.put("mac", hfcmac.toLowerCase().trim());
		//hfcentity.put("oid", oid);
		hfcentity.put("ip", ip.toLowerCase().trim());
		//hfcentity.put("gateway", gateway.toLowerCase().trim());
		hfcentity.put("active", "1");
		hfcentity.put("lable", ip.toLowerCase().trim());
		hfcentity.put("hfctype", hfctype.toLowerCase().trim());
		hfcentity.put("version", version.toLowerCase().trim());
		hfcentity.put("logicalid", logicalid.toLowerCase().trim());
		hfcentity.put("modelnumber", modelnumber.toLowerCase().trim());
		hfcentity.put("serialnumber", serialnumber.toLowerCase().trim());
		hfcentity.put("trapip1", trapip1.toLowerCase().trim());
		hfcentity.put("trapip2", trapip2.toLowerCase().trim());
		hfcentity.put("trapip3", trapip3.toLowerCase().trim());
		hfcentity.put("power1", power1.trim());
		hfcentity.put("power_v1", power_v1.trim());
		hfcentity.put("power2", power2.trim());
		hfcentity.put("power_v2", power_v2.trim());
		hfcentity.put("bias_c1", bias_c1.trim());
		hfcentity.put("bias_c2", bias_c2.trim());
		hfcentity.put("ref_c1", ref_c1.trim());
		hfcentity.put("ref_c2", ref_c2.trim());
		hfcentity.put("pump_t1", pump_t1.trim());
		hfcentity.put("pump_t2", pump_t2.trim());
		jedis.hmset(shfcentitykey, hfcentity);

		jedis.save();
		
		Sendstschange("hfc",String.valueOf(hfcid),jedis);
		redisUtil.getJedisPool().returnResource(jedis);
	}

}
