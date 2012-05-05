package com.stan.wen9000.web;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;



public class ServiceController {

	
	private static Logger log = Logger.getLogger(ServiceController.class);

	private static SnmpUtil util = new SnmpUtil();

	
	private static RedisUtil redisUtil;

	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceController.redisUtil = redisUtil;
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

      	System.out.println("[x]ServiceController  Subscribing....pmessage....now receive on msgarge1 [" + arg1 + "] arg2=["+msg +"]");
      	try {
  			//arg2 is mssage now is currenti p
  			
  			
  			
  			servicestart(arg1, msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();			
  		}
  		
      }

  };
  


	public void start() {

		log.info("[#3] ..... service Controller starting");

		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		 jedis.psubscribe(jedissubSub, "servicecontroller.*");
		redisUtil.getJedisPool().returnResource(jedis);
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
		
		
		
		
	}

	
	public static void servicestart(String pat, String message) throws Exception {

			System.out.println(" [x] Service Controller Received '" + message
					+ "'");
			
			long start = System.currentTimeMillis();  			
			dowork(pat, message);					
			long end = System.currentTimeMillis();  
			System.out.println("one ServiceAlarmProcessor dowork spend: " + ((end - start)) + " milliseconds");  
			
		
	}
	
	

	@SuppressWarnings("unchecked")
	public static void dowork(String pat, String message) throws ParseException {
		
		
		if(pat.equalsIgnoreCase("servicecontroller.treeinit")){
			doNodeTreeInit();
		}else if(pat.equalsIgnoreCase("servicecontroller.cbatdetail")){
			doNodeCbatdetail(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.cnudetail")){
			doNodeCnudetail(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.cbat_modify")){
			doCbatModify(message);
		}


	}
	
	private static void doCbatModify(String message) throws ParseException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//log.info("xxxxxxxxxxxxxxxxxx1:::"+message);
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);

		String mac = jsondata.get("mac").toString();
		String ip = jsondata.get("ip").toString();
    	String label = jsondata.get("label").toString();
    	String address = jsondata.get("address").toString();
    	String mvlanenable = jsondata.get("mvlanenable").toString();
    	String mvlanid = jsondata.get("mvlanid").toString();
    	String trapserver = jsondata.get("trapserver").toString();
    	String trap_port = jsondata.get("trap_port").toString();
    	String netmask = jsondata.get("netmask").toString();
    	String gateway = jsondata.get("gateway").toString();
    	//logger.info("cbatmac::::::"+ mac +"::::::::value::::::"+ip);
    	//获取CBAT ID 
		String cbatid = jedis.get("mac:"+mac+":deviceid");
		String cbatkey = "cbatid:"+cbatid+":entity";
		String cbatinfokey = "cbatid:"+cbatid+":cbatinfo";
		
		//发往设备修改设备相关参数(ip/mvlanenable/mvlanid)
    	try{    		
    		String oldip = jedis.hget(cbatkey, "ip");
    		//判断是否要跟设备交互
    		if((oldip.equalsIgnoreCase(ip))&&(mvlanenable.equalsIgnoreCase(jedis.hget(cbatinfokey, "mvlanenable")))&&(mvlanid.equalsIgnoreCase(jedis.hget(cbatinfokey, "mvlanid")))
    				&&(trapserver.equalsIgnoreCase(jedis.hget(cbatinfokey, "trapserver")))&&(trap_port.equalsIgnoreCase(jedis.hget(cbatinfokey, "agentport")))
    				&&(netmask.equalsIgnoreCase(jedis.hget(cbatkey, "netmask")))&&(gateway.equalsIgnoreCase(jedis.hget(cbatkey, "gateway")))){
    			//保存
            	jedis.hset(cbatkey, "label", label);
            	
            	jedis.hset(cbatinfokey, "address", address);
            	jedis.save();
            	
            	redisUtil.getJedisPool().returnResource(jedis);
            	
            	jedis.publish("node.tree.cbatmodify", "modifyok");
            	
            	return;
    		}
    		//需要跟设备交互
    		int tmp =(util.getINT32PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0})));
    		if(tmp == -1){
    			redisUtil.getJedisPool().returnBrokenResource(jedis);
    			jedis.publish("node.tree.cbatmodify", "");
    			return;
    		}
    		util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0}), new Integer32(Integer.valueOf(mvlanenable)));
    		util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,5,0}), new Integer32(Integer.valueOf(mvlanid)));    		
    		util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}), trapserver);
    		util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,7,0}), new Integer32(Integer.valueOf(trap_port)));
    		util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0}), netmask);
    		util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,3,0}), gateway);
    		if(!oldip.equalsIgnoreCase(ip)){
    			util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,1,0}), ip);
    			util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), new Integer32(1));
    			util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,6,1,0}), new Integer32(1));
    		}    		
    		
    		//保存
        	jedis.hset(cbatkey, "ip", ip);
        	jedis.hset(cbatkey, "label", label);
        	jedis.hset(cbatkey, "netmask", netmask);
        	jedis.hset(cbatkey, "gateway", gateway);
        	
        	jedis.hset(cbatinfokey, "address", address);
        	jedis.hset(cbatinfokey, "mvlanenable", mvlanenable);
        	jedis.hset(cbatinfokey, "mvlanid", mvlanid);
        	jedis.hset(cbatinfokey, "trapserver", trapserver);
        	jedis.hset(cbatinfokey, "agentport", trap_port);
        	jedis.save();
    	}catch(Exception e){
    		//e.printStackTrace();
    		redisUtil.getJedisPool().returnBrokenResource(jedis);
    		jedis.publish("node.tree.cbatmodify", "");
    		return;
    	}
    	redisUtil.getJedisPool().returnResource(jedis);
    	jedis.publish("node.tree.cbatmodify", "modifyok");
	}
	
	private static void doNodeCnudetail(String mac){
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		String id = jedis.get("mac:"+mac+":deviceid");
    	String cnukey = "cnuid:"+id+":entity";
    	JSONObject cnujson = new JSONObject();
    	cnujson.put("mac", jedis.hget(cnukey,"mac"));
		
		cnujson.put("label", jedis.hget(cnukey,"label"));
		cnujson.put("address", jedis.hget(cnukey,"address"));
		cnujson.put("contact", jedis.hget(cnukey,"contact"));	
		cnujson.put("phone", jedis.hget(cnukey,"phone"));
    	if(jedis.hget(cnukey, "active").equalsIgnoreCase("1")){
    		//设备在线,获取实时设备信息
    		cnujson.put("active", "离线");
    	}else{
    		//设备离线，获取redis信息
    		cnujson.put("active", "在线");
    	}
    	
    	//添加模板信息
    	//获取对应的profileid
    	String proid = jedis.hget("cnuid:"+id+":entity", "profileid");
    	String prokey = "profileid:"+proid+":entity";
    	cnujson.put("profilename", jedis.hget(prokey,"profilename"));
    	cnujson.put("vlanen", jedis.hget(prokey,"vlanen"));
    	cnujson.put("vlanid", jedis.hget(prokey,"vlanid"));
    	cnujson.put("vlan0id", jedis.hget(prokey,"vlan0id"));
    	cnujson.put("vlan1id", jedis.hget(prokey,"vlan1id"));
    	cnujson.put("vlan2id", jedis.hget(prokey,"vlan2id"));
    	cnujson.put("vlan3id", jedis.hget(prokey,"vlan3id"));
    	cnujson.put("rxlimitsts", jedis.hget(prokey,"rxlimitsts"));    	
    	cnujson.put("cpuportrxrate", jedis.hget(prokey,"cpuportrxrate"));
    	cnujson.put("port0txrate", jedis.hget(prokey,"port0txrate"));
    	cnujson.put("port1txrate", jedis.hget(prokey,"port1txrate"));
    	cnujson.put("port2txrate", jedis.hget(prokey,"port2txrate"));
    	cnujson.put("port3txrate", jedis.hget(prokey,"port3txrate"));
    	cnujson.put("txlimitsts", jedis.hget(prokey,"txlimitsts"));
    	cnujson.put("cpuporttxrate", jedis.hget(prokey,"cpuporttxrate"));
    	cnujson.put("port0rxrate", jedis.hget(prokey,"port0rxrate"));
    	cnujson.put("port1rxrate", jedis.hget(prokey,"port1rxrate"));
    	cnujson.put("port2rxrate", jedis.hget(prokey,"port2rxrate"));
    	cnujson.put("port3rxrate", jedis.hget(prokey,"port3rxrate"));
    	
    	redisUtil.getJedisPool().returnResource(jedis);
    	
    	String jsonString = cnujson.toJSONString();    
		//publish to notify node.js a new alarm
		jedis.publish("node.tree.cnudetail", jsonString);
	}
	
	private static void doNodeCbatdetail(String mac){
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		String id = jedis.get("mac:"+mac+":deviceid");
		String cbatkey = "cbatid:"+id+":entity";
		String result = "";
		JSONObject cbatjson = new JSONObject();
		
		cbatjson.put("mac", jedis.hget(cbatkey,"mac"));
		cbatjson.put("ip", jedis.hget(cbatkey,"ip"));
		cbatjson.put("label", jedis.hget(cbatkey,"label"));
		cbatjson.put("netmask", jedis.hget(cbatkey,"netmask"));
		cbatjson.put("gateway", jedis.hget(cbatkey,"gateway"));			
		switch(Integer.parseInt(jedis.hget(cbatkey, "devicetype")))
		{
        	case 1:
        		//break;
        	case 2:
        		
        		//break;
        	case 3:
        		//break;
        	case 4:
        		
        		//break;
        	case 5:
        		//break;
        	case 6:
        		
        		//break;
        	case 7:
        		//break;
        	case 8:
        		result = "中文测试";
        		break;
        	default:
        		result = "Unknown";
        		break;
		}
		cbatjson.put("devicetype", result);	
		//读取cbatinfo信息
		String cbatinfokey = "cbatid:"+id+":cbatinfo";
		cbatjson.put("trapserver", jedis.hget(cbatinfokey, "trapserver"));	
		cbatjson.put("address", jedis.hget(cbatinfokey, "address"));	
		cbatjson.put("phone", jedis.hget(cbatinfokey, "phone"));
		cbatjson.put("bootver", jedis.hget(cbatinfokey, "bootver"));
		cbatjson.put("contact", jedis.hget(cbatinfokey, "contact"));
		cbatjson.put("agentport", jedis.hget(cbatinfokey, "agentport"));
		cbatjson.put("appver", jedis.hget(cbatinfokey, "appver"));
		cbatjson.put("mvlanenable", jedis.hget(cbatinfokey, "mvlanenable"));
		cbatjson.put("mvlanid", jedis.hget(cbatinfokey, "mvlanid"));
		if(jedis.hget(cbatkey, "active").equalsIgnoreCase("1") ){
			//设备在线，实时获得设备信息
			cbatjson.put("active", "在线");			
		}else{
			//设备离线，从redis获取设备信息
			cbatjson.put("active", "离线");					
		}
		
		redisUtil.getJedisPool().returnResource(jedis);
		String jsonString = cbatjson.toJSONString();
   	 
	    
		//publish to notify node.js a new alarm
		jedis.publish("node.tree.cbatdetail", jsonString);
	}
	
	private static void doNodeTreeInit() {
		
		
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		
	
		
        JSONArray jsonResponseArray = new JSONArray();

    	Set<String> list = jedis.keys("cbatid:*:entity");

    	
    	
    	JSONObject eocjson = new JSONObject();
		
    	eocjson.put((String)"title", (String)"EOC设备");
    	eocjson.put("key", "eocroot");
    	eocjson.put("isFolder", "true");
    	eocjson.put("expand", "true");

    	//"children"
		
		JSONArray cbatarray = new JSONArray();
    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		
    		JSONObject cbatjson = new JSONObject();

            
    
    		String key = it.next().toString();
   
    		//add head;
    		cbatjson.put("title", jedis.hget(key, "ip"));
    		cbatjson.put("key", jedis.hget(key, "mac"));
    		cbatjson.put("online", jedis.hget(key, "active"));
    		//添加头端信息    		
    		if(jedis.hget(key, "active").equalsIgnoreCase("1")){
    			cbatjson.put("icon", "doc_with_children.gif");
    			//"children"+'"'+":";
    		}else{
    			cbatjson.put("icon", "offline.png");
    			//+"children"+'"'+":";
    		}
    		cbatjson.put("type", "cbat");

    		//获取cbatid
    		String cbatid = jedis.get("mac:" + jedis.hget(key, "mac") + ":deviceid");
    		//logger.info("keys::::::cbatid"+ cbatid);
    		//取得所有属于cbatid的 cnuid
        	Set<String> list_cnu = jedis.smembers("cbatid:" + cbatid + ":cnus");//jedis.keys("cnuid:*:cbatid:"+jedis.get("cbatmac:"+jedis.hget(key, "mac")+":cbatid")+":*:entity");
        	String cnustring ="";
        	
        	JSONArray cnujsons = new JSONArray();
        	for(Iterator jt = list_cnu.iterator(); jt.hasNext(); ) 
        	{ 
        		
        		JSONObject cnujson = new JSONObject();
        		
        		String key_cnuid = jt.next().toString();  
        		String key_cnu = "cnuid:" + key_cnuid + ":entity";
        		//logger.info("keys::::::key_cnu"+ key_cnu);
        		
        	
        		cnujson.put("title", jedis.hget(key_cnu, "label"));
        		cnujson.put("key", jedis.hget(key_cnu, "mac"));
        		cnujson.put("online", jedis.hget(key_cnu, "active"));
        		
        	
        		if(jedis.hget(key_cnu, "active").equalsIgnoreCase("1")){
        			cnujson.put("icon",  "online.gif");        			
        		}else{
        			cnujson.put("icon", "offline.png");        			
        		}
        		cnujson.put("type", "cnu");
        		
        		
        		cnujsons.add(cnujson);
        		
        	}
        	
        	cbatjson.put("children", cnujsons);
        	
        	
        	cbatarray.add(cbatjson);
    	}
    	
   
    	eocjson.put("children", cbatarray);
    	    	 
    	redisUtil.getJedisPool().returnResource(jedis);

    	 
    	 jsonResponseArray.add(eocjson);
    	 
    	 String jsonString = jsonResponseArray.toJSONString();
    	 
    
    		//publish to notify node.js a new alarm
 		jedis.publish("node.tree.init", jsonString);
 		
    	 
	}



	
	
	


	
}