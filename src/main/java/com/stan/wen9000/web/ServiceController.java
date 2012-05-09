package com.stan.wen9000.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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
	public static void dowork(String pat, String message) throws ParseException, IOException {
		
		
		if(pat.equalsIgnoreCase("servicecontroller.treeinit")){
			doNodeTreeInit();
		}else if(pat.equalsIgnoreCase("servicecontroller.cbatdetail")){
			doNodeCbatdetail(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.cnudetail")){
			doNodeCnudetail(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.cbat_modify")){
			doCbatModify(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.cbat_sync")){
			doCbatSync(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.profile_all")){
			doProfileAll(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.profile_del")){
			doProfileDel(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.profile_edit")){
			doProfileEdit(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.profile_get")){
			doProfileGet(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.profile_create")){
			doProfileCreate(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.cnu_basesub")){
			doCnuBase(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.cnusync")){
			doCnuSync(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.cnu_sub")){
			doCnuSub(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.cnus")){
			doOptCnus(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.checkedcnus")){
			doOptCheckedCnus(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.allcheckedcnus")){
			doOptAllCheckedCnus(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.selectedpro")){
			doOptSelectedPro(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.send_config")){
			doOptSendConfig(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.con_success")){
			doOptConSuccess(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.con_failed")){
			doOptConFailed(message);
		}


	}
	
	private static void doOptConSuccess(String message){
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//获得配置成功的设备集合
		Set<String> members = jedis.smembers("global:configsuccess");
		JSONArray jsonarray = new JSONArray(); 
		for(Iterator it = members.iterator(); it.hasNext();){
			JSONObject json = new JSONObject();
			String key = "cnuid:"+it.next().toString()+":entity";
			json.put("active", jedis.hget(key, "active"));
			json.put("mac", jedis.hget(key, "mac"));
			json.put("label", jedis.hget(key, "label"));
			jsonarray.add(json);
		}
		String jsonstring = jsonarray.toJSONString();
		
		jedis.publish("node.opt.con_success", jsonstring);
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptConFailed(String message){
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//获得配置失败的设备集合
		Set<String> members = jedis.smembers("global:configfailed");
		JSONArray jsonarray = new JSONArray(); 
		for(Iterator it = members.iterator(); it.hasNext();){
			JSONObject json = new JSONObject();
			String key = "cnuid:"+it.next().toString()+":entity";
			json.put("active", jedis.hget(key, "active"));
			json.put("mac", jedis.hget(key, "mac"));
			json.put("label", jedis.hget(key, "label"));
			jsonarray.add(json);
		}
		String jsonstring = jsonarray.toJSONString();
		
		jedis.publish("node.opt.con_failed", jsonstring);
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptSendConfig(String message){
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//清空配置结果集合
		Set<String> members = jedis.smembers("global:configfailed");
		if(!members.isEmpty()){
			for(Iterator it=members.iterator();it.hasNext();){
				jedis.srem("global:configfailed", it.next().toString());
			}
		}		
		members = jedis.smembers("global:configsuccess");		
		if(!members.isEmpty()){
			for(Iterator it=members.iterator();it.hasNext();){
				jedis.srem("global:configsuccess", it.next().toString());
			}
		}
		
		String proid = jedis.get("global:selectedprofile");
		//获取要配置的CNU
		Set<String> list = jedis.smembers("global:checkedcnus");
		String cnukey="";		
		String proc = "0/"+String.valueOf(list.size());
		int i=0;
		for(Iterator it = list.iterator(); it.hasNext();){
			String cnuid = it.next().toString();
			cnukey = "cnuid:"+cnuid+":entity";
			//删除global:checkedcnus集合中此cnu
			jedis.srem("global:checkedcnus", cnuid);
			//配置进度字符串
			i++;
			proc = i + "/" + String.valueOf(list.size());
			//获取cnu在头端上的索引
			String cnuindex = jedis.hget(cnukey, "cnuindex");
			//获取所属头端信息
			String cid = jedis.hget(cnukey, "cbatid");
			String cip = jedis.hget("cbatid:"+cid+":entity", "ip");
			
			//下面是具体节点配置过程或发往其它进程进行异步配置
			//判断设备是否在线
			try {
				String tmp = util.getStrPDU(cip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
				if(tmp == ""){
					//将配置失败的设备id存储
					jedis.sadd("global:configfailed", cnuid);
					jedis.publish("node.opt.proc", proc);
					continue;
				}else{
					//发送配置
					if(!sendconfig(Integer.valueOf(proid),cip,Integer.valueOf(cnuindex),jedis)){
						//发送失败
						//将配置失败的设备id发往队列
						jedis.sadd("global:configfailed", cnuid);
						jedis.publish("node.opt.proc", proc);
						continue;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				jedis.publish("node.opt.proc", proc);
				continue;
			}
			
			//将配置成功的设备id存储
			jedis.sadd("global:configsuccess", cnuid);
			
			//获取cnu原profileid号
			String old_proid = jedis.hget(cnukey, "profileid");
			//删除原profile集合中此CNU
			jedis.srem("profileid:"+old_proid+":cnus", cnuid);
			//更改CNU模板号
			jedis.hset(cnukey, "profileid", proid);
			//添加cnu到新profile集合中
			jedis.sadd("profileid:"+proid+":cnus", cnuid);		
			
			//保存数据到硬盘
			jedis.bgsave();
			
			jedis.publish("node.opt.proc", proc);
		}
		jedis.publish("node.opt.sendconfig", "");
		redisUtil.getJedisPool().returnResource(jedis);
		
	}
	
	private static void doOptSelectedPro(String message) throws ParseException, IOException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		//获得已选模板id
		String proid = jedis.get("global:selectedprofile");
		String prokey = "profileid:"+ proid + ":entity";
		
		JSONObject json = new JSONObject();
		json.put("proname", jedis.hget(prokey, "profilename"));
		json.put("vlanen", jedis.hget(prokey, "vlanen"));
		json.put("vlanid", jedis.hget(prokey, "vlanid"));
		json.put("vlan0id", jedis.hget(prokey, "vlan0id"));
		json.put("vlan1id", jedis.hget(prokey, "vlan1id"));
		json.put("vlan2id", jedis.hget(prokey, "vlan2id"));
		json.put("vlan3id", jedis.hget(prokey, "vlan3id"));
		json.put("rxlimitsts", jedis.hget(prokey, "rxlimitsts"));
		json.put("cpuportrxrate", jedis.hget(prokey, "cpuportrxrate"));
		json.put("port0txrate", jedis.hget(prokey, "port0txrate"));
		json.put("port1txrate", jedis.hget(prokey, "port1txrate"));
		json.put("port2txrate", jedis.hget(prokey, "port2txrate"));
		json.put("port3txrate", jedis.hget(prokey, "port3txrate"));
		json.put("txlimitsts", jedis.hget(prokey, "txlimitsts"));
		json.put("cpuporttxrate", jedis.hget(prokey, "cpuporttxrate"));
		json.put("port0rxrate", jedis.hget(prokey, "port0rxrate"));
		json.put("port1rxrate", jedis.hget(prokey, "port1rxrate"));
		json.put("port2rxrate", jedis.hget(prokey, "port2rxrate"));
		json.put("port3rxrate", jedis.hget(prokey, "port3rxrate"));
		
		String jsonstring = json.toJSONString();
		
		jedis.publish("node.opt.selectedpro", jsonstring);
        
        redisUtil.getJedisPool().returnResource(jedis);
		
	}
	
	private static void doOptAllCheckedCnus(String message) throws ParseException, IOException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		JSONArray jsonResponseArray = new JSONArray();
		String result="";
		Set<String> list = jedis.smembers("global:checkedcnus");
        for(Iterator it = list.iterator(); it.hasNext(); ) {
        	String cnuid = (String) it.next();
        	JSONObject json = new JSONObject();
    		String cnukey = "cnuid:"+cnuid+":entity";
    		json.put("mac", jedis.hget(cnukey, "mac"));
    		json.put("active", jedis.hget(cnukey, "active"));
    		json.put("label", jedis.hget(cnukey, "label"));
    		switch(Integer.parseInt(jedis.hget(cnukey, "devicetype")))
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
    		json.put("devicetype", result);
    		json.put("contact", jedis.hget(cnukey, "contact"));
    		json.put("phone", jedis.hget(cnukey, "phone"));
    		jsonResponseArray.add(json);
        }
        
        String jsonstring = jsonResponseArray.toJSONString();
        
        jedis.publish("node.opt.allcheckedcnus", jsonstring);
        
        redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptCheckedCnus(String message) throws ParseException, IOException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String cnumac = jsondata.get("cnumac").toString();
		String value = jsondata.get("value").toString();
		//获取CNU ID 
		String cnuid = jedis.get("mac:"+cnumac+":deviceid");
    	if(value.equalsIgnoreCase("true")){
    		//保存选择的cnu到集合
    		jedis.sadd("global:checkedcnus", cnuid);
    	}else{
    		//删除选择的cnu
    		jedis.srem("global:checkedcnus", cnuid);
    	}
    	
    	
    	redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptCnus(String message) throws ParseException, IOException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		String result = "";
		JSONArray jsonResponseArray = new JSONArray();
		Set<String> list = jedis.keys("cnuid:*:entity");
        for(Iterator it = list.iterator(); it.hasNext(); ) {
        	JSONObject cnujson = new JSONObject();
        	String prokey = (String) it.next();
        	int index1 = prokey.indexOf(':') +1;
    		int index2 = prokey.lastIndexOf(':');
    		String cid = prokey.substring(index1, index2);
    		//判断key是否存在
    		if(jedis.exists("global:checkedcnus")){
    			//判断是否checked
            	if(jedis.sismember("global:checkedcnus", cid)){
            		cnujson.put("check", "<input type=checkbox class=chk checked />");
            	}else{
            		cnujson.put("check", "<input type=checkbox class=chk />");
            	}
    		}else{
    			cnujson.put("check", "<input type=checkbox class=chk />");
    		}
        	
    		cnujson.put("mac", jedis.hget(prokey, "mac"));
    		cnujson.put("active", jedis.hget(prokey, "active"));
    		cnujson.put("label", jedis.hget(prokey, "label"));

    		switch(Integer.parseInt(jedis.hget(prokey, "devicetype")))
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
    		cnujson.put("devicetype", result);
    		cnujson.put("proname", jedis.hget("profileid:"+jedis.hget(prokey, "profileid")+":entity", "profilename"));
    		cnujson.put("contact", jedis.hget(prokey, "contact"));
    		cnujson.put("label", jedis.hget(prokey, "label"));
    		
    		jsonResponseArray.add(cnujson);
        }
        
        String jsonstring = jsonResponseArray.toJSONString();
        
        jedis.publish("node.opt.cnus", jsonstring);
        
        redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doCnuSub(String message) throws ParseException, IOException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		//获取CNU ID 
		String cnuid = jedis.get("mac:"+message+":deviceid");
    	String key = "cnuid:"+cnuid+":entity";
    	String devid = jedis.hget(key, "cnuindex");
    	//获取所属头端信息
    	String cbatid = jedis.hget(key,"cbatid");
    	String cbatip = jedis.hget("cbatid:"+cbatid+":entity", "ip");
    	
    	//判断头端是否在线
    	int tmp =(util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0})));
		if(tmp == -1){
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			jedis.publish("node.tree.cnu_sub", "");
			return;
		}
    	
		//TODO
		
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doCnuSync(String message) throws ParseException, IOException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		//获取CNU ID 
		String cnuid = jedis.get("mac:"+message+":deviceid");
    	String key = "cnuid:"+cnuid+":entity";
    	String devid = jedis.hget(key, "cnuindex");
    	//获取所属头端信息
    	String cbatid = jedis.hget(key,"cbatid");
    	String cbatip = jedis.hget("cbatid:"+cbatid+":entity", "ip");
    	
    	//判断头端是否在线
    	int tmp =(util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0})));
		if(tmp == -1){
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			jedis.publish("node.tree.cnusync", "");
			return;
		}
    	//获取终端信息
		//TODO
		
		//JSONObject json = new JSONObject();
		
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doCnuBase(String message) throws ParseException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String address = jsondata.get("address").toString();
    	String contact = jsondata.get("contact").toString();
    	String phone = jsondata.get("phone").toString();
    	String label = jsondata.get("label").toString();
    	String mac = jsondata.get("mac").toString();
    	
    	//获取CNU ID 
		String cnuid = jedis.get("mac:"+mac+":deviceid");
    	String key = "cnuid:"+cnuid+":entity";
		jedis.hset(key, "address", address);
		jedis.hset(key, "contact", contact);
		jedis.hset(key, "phone", phone);
		jedis.hset(key, "label", label);
		jedis.save();
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doProfileCreate(String message) throws ParseException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		//获取传递参数
    	String proname = jsondata.get("proname").toString();
    	String vlanen = jsondata.get("vlanen").toString();
    	String vlanid = jsondata.get("vlanid").toString();
    	String vlan0id = jsondata.get("vlan0id").toString();
    	String vlan1id = jsondata.get("vlan1id").toString();
    	String vlan2id = jsondata.get("vlan2id").toString();
    	String vlan3id = jsondata.get("vlan3id").toString();
    	
    	String rxlimitsts = jsondata.get("rxlimitsts").toString();
    	String cpuportrxrate = jsondata.get("cpuportrxrate").toString();
    	String port0txrate = jsondata.get("port0txrate").toString();
    	String port1txrate = jsondata.get("port1txrate").toString();
    	String port2txrate = jsondata.get("port2txrate").toString();
    	String port3txrate = jsondata.get("port3txrate").toString();
    	
    	String txlimitsts = jsondata.get("txlimitsts").toString();
    	String cpuporttxrate = jsondata.get("cpuporttxrate").toString();
    	String port0rxrate = jsondata.get("port0rxrate").toString();
    	String port1rxrate = jsondata.get("port1rxrate").toString();
    	String port2rxrate = jsondata.get("port2rxrate").toString();
    	String port3rxrate = jsondata.get("port3rxrate").toString();
		
    	//获取profileid
    	String proid = String.valueOf(jedis.incr("global:profileid"));
    	String prokey = "profileid:"+proid + ":entity";
    	//组合存储字符串
    	Map<String , String >  proentity = new HashMap<String, String>();
    	proentity.put("profilename", proname.toLowerCase());
    	proentity.put("vlanen", vlanen);
    	proentity.put("vlanid", vlanid);
    	proentity.put("vlan0id", vlan0id);
    	proentity.put("vlan1id", vlan1id);
    	proentity.put("vlan2id", vlan2id);
    	proentity.put("vlan3id", vlan3id);
    	
    	proentity.put("rxlimitsts", rxlimitsts);
    	proentity.put("cpuportrxrate", cpuportrxrate);
    	proentity.put("port0txrate", port0txrate);
    	proentity.put("port1txrate", port1txrate);
    	proentity.put("port2txrate", port2txrate);
    	proentity.put("port3txrate", port3txrate);
    	
    	proentity.put("txlimitsts", txlimitsts);
    	proentity.put("cpuporttxrate", cpuporttxrate);
    	proentity.put("port0rxrate", port0rxrate);
    	proentity.put("port1rxrate", port1rxrate);
    	proentity.put("port2rxrate", port2rxrate);
    	proentity.put("port3rxrate", port3rxrate);
    	//save
    	jedis.hmset(prokey, proentity);
    	//logger.info("prokeys::::::proname"+ proname + "---vlanen::::"+vlanen );
    	
    	//保存数据到硬盘
    	jedis.save();
    	
    	redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doProfileGet(String message){
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//更新已选模板，批量配置时才会用到
		jedis.set("global:selectedprofile", message);
		
		String prokey = "profileid:"+message+":entity";
		JSONObject json = new JSONObject();
		json.put("proname", jedis.hget(prokey, "profilename"));		
		json.put("vlanen", jedis.hget(prokey, "vlanen"));
		json.put("vlanid", jedis.hget(prokey, "vlanid"));
		json.put("vlan0id", jedis.hget(prokey, "vlan0id"));
		json.put("vlan1id", jedis.hget(prokey, "vlan1id"));
		json.put("vlan2id", jedis.hget(prokey, "vlan2id"));
		json.put("vlan3id", jedis.hget(prokey, "vlan3id"));
		json.put("rxlimitsts", jedis.hget(prokey, "rxlimitsts"));
		json.put("cpuportrxrate", jedis.hget(prokey, "cpuportrxrate"));
		json.put("port0txrate", jedis.hget(prokey, "port0txrate"));
		json.put("port1txrate", jedis.hget(prokey, "port1txrate"));
		json.put("port2txrate", jedis.hget(prokey, "port2txrate"));
		json.put("port3txrate", jedis.hget(prokey, "port3txrate"));    		
		json.put("txlimitsts", jedis.hget(prokey, "txlimitsts"));
		json.put("cpuporttxrate", jedis.hget(prokey, "cpuporttxrate"));
		json.put("port0rxrate", jedis.hget(prokey, "port0rxrate"));
		json.put("port1rxrate", jedis.hget(prokey, "port1rxrate"));
		json.put("port2rxrate", jedis.hget(prokey, "port2rxrate"));
		json.put("port3rxrate", jedis.hget(prokey, "port3rxrate"));
		
		String jsonString = json.toJSONString(); 
	    redisUtil.getJedisPool().returnResource(jedis);
	    jedis.publish("node.pro.get", jsonString);
	}
	
	private static void doProfileEdit(String message) throws ParseException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		//获取传递参数
		String proid = jsondata.get("proid").toString();
    	String proname = jsondata.get("proname").toString();
    	String vlanen = jsondata.get("vlanen").toString();
    	String vlanid = jsondata.get("vlanid").toString();
    	String vlan0id = jsondata.get("vlan0id").toString();
    	String vlan1id = jsondata.get("vlan1id").toString();
    	String vlan2id = jsondata.get("vlan2id").toString();
    	String vlan3id = jsondata.get("vlan3id").toString();
    	
    	String rxlimitsts = jsondata.get("rxlimitsts").toString();
    	String cpuportrxrate = jsondata.get("cpuportrxrate").toString();
    	String port0txrate = jsondata.get("port0txrate").toString();
    	String port1txrate = jsondata.get("port1txrate").toString();
    	String port2txrate = jsondata.get("port2txrate").toString();
    	String port3txrate = jsondata.get("port3txrate").toString();
    	
    	String txlimitsts = jsondata.get("txlimitsts").toString();
    	String cpuporttxrate = jsondata.get("cpuporttxrate").toString();
    	String port0rxrate = jsondata.get("port0rxrate").toString();
    	String port1rxrate = jsondata.get("port1rxrate").toString();
    	String port2rxrate = jsondata.get("port2rxrate").toString();
    	String port3rxrate = jsondata.get("port3rxrate").toString();

    	String prokey = "profileid:"+proid + ":entity";
    	//组合存储字符串
    	Map<String , String >  proentity = new HashMap<String, String>();
    	proentity.put("profilename", proname.toLowerCase());
    	proentity.put("vlanen", vlanen);
    	proentity.put("vlanid", vlanid);
    	proentity.put("vlan0id", vlan0id);
    	proentity.put("vlan1id", vlan1id);
    	proentity.put("vlan2id", vlan2id);
    	proentity.put("vlan3id", vlan3id);
    	
    	proentity.put("rxlimitsts", rxlimitsts);
    	proentity.put("cpuportrxrate", cpuportrxrate);
    	proentity.put("port0txrate", port0txrate);
    	proentity.put("port1txrate", port1txrate);
    	proentity.put("port2txrate", port2txrate);
    	proentity.put("port3txrate", port3txrate);
    	
    	proentity.put("txlimitsts", txlimitsts);
    	proentity.put("cpuporttxrate", cpuporttxrate);
    	proentity.put("port0rxrate", port0rxrate);
    	proentity.put("port1rxrate", port1rxrate);
    	proentity.put("port2rxrate", port2rxrate);
    	proentity.put("port3rxrate", port3rxrate);
    	//save
    	jedis.hmset(prokey, proentity);
    	//logger.info("prokeys::::::proname"+ proname + "---vlanen::::"+vlanen );
    	
    	//保存数据到硬盘
    	jedis.save();

		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doProfileDel(String message){
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		 String prokey = "profileid:"+message+":entity";
		 //判断profile集合中是否有cnu
		 if(jedis.smembers("profileid:"+message+":cnus").isEmpty()){
			 //无CNU
			 //删除此profile
			 jedis.del(prokey);
			 
			 jedis.publish("node.pro.delprofile", "deleteok");
			 redisUtil.getJedisPool().returnResource(jedis);
		 }else{
			 //集合中有CNU，无法删除此profile
			 jedis.publish("node.pro.delprofile", "");
			 redisUtil.getJedisPool().returnResource(jedis);
		 }		 
	}
	
	private static void doProfileAll(String message){
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		JSONArray jsonResponseArray = new JSONArray();
		Set<String> list = jedis.keys("profileid:*:entity");		
        for(Iterator it = list.iterator(); it.hasNext(); ) {
        	JSONObject projson = new JSONObject();
        	String prokey = (String) it.next();
        	int index1 = prokey.indexOf(':') +1;
    		int index2 = prokey.lastIndexOf(':');
    		String cid = prokey.substring(index1, index2);
    		projson.put("id", cid);
    		projson.put("proname", jedis.hget(prokey, "profilename"));
    		projson.put("vlanen", jedis.hget(prokey, "vlanen"));
    		projson.put("vlanid", jedis.hget(prokey, "vlanid"));
    		projson.put("vlan0id", jedis.hget(prokey, "vlan0id"));
    		projson.put("vlan1id", jedis.hget(prokey, "vlan1id"));
    		projson.put("vlan2id", jedis.hget(prokey, "vlan2id"));
    		projson.put("vlan3id", jedis.hget(prokey, "vlan3id"));
    		projson.put("rxlimitsts", jedis.hget(prokey, "rxlimitsts"));
    		projson.put("cpuportrxrate", jedis.hget(prokey, "cpuportrxrate"));
    		projson.put("port0txrate", jedis.hget(prokey, "port0txrate"));
    		projson.put("port1txrate", jedis.hget(prokey, "port1txrate"));
    		projson.put("port2txrate", jedis.hget(prokey, "port2txrate"));
    		projson.put("port3txrate", jedis.hget(prokey, "port3txrate"));    		
    		projson.put("txlimitsts", jedis.hget(prokey, "txlimitsts"));
    		projson.put("cpuporttxrate", jedis.hget(prokey, "cpuporttxrate"));
    		projson.put("port0rxrate", jedis.hget(prokey, "port0rxrate"));
    		projson.put("port1rxrate", jedis.hget(prokey, "port1rxrate"));
    		projson.put("port2rxrate", jedis.hget(prokey, "port2rxrate"));
    		projson.put("port3rxrate", jedis.hget(prokey, "port3rxrate"));
    		jsonResponseArray.add(projson);
        }
        
   	 
   	 	String jsonString = jsonResponseArray.toJSONString();
   	 
   
   		//publish to notify node.js a new alarm
		jedis.publish("node.pro.allprofiles", jsonString);
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doCbatSync(String message) throws IOException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		
		String cbatid = jedis.get("mac:"+message+":deviceid");
    	String cbatkey = "cbatid:"+cbatid+":entity";
    	String cbatip = jedis.hget(cbatkey, "ip");
    	String cbatinfokey = "cbatid:"+cbatid+":cbatinfo";
    	//获得设备相关参数(ip/mvlanenable/mvlanid)
    	
    	int tmp =(util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0})));
		if(tmp == -1){
			redisUtil.getJedisPool().returnBrokenResource(jedis);

			jedis.publish("node.tree.cbatsync", "");
			return;
		}

    	try{
	    	int mvlanenable = (util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0})));
	    	int mvlanid =( util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,5,0})));
	    	String netmask = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,2,0})));
	    	String gateway = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,3,0})));
	    	
	    	String hwversion = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,4,3,0})));
	    	String appver = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,4,4,0})));
	    	String trapserverip = util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
			int trap_port = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,7,0}));
	    	
			//logger.info("mvlanenable::::::"+ mvlanenable);
			
			//更新redis数据
		    jedis.hset(cbatinfokey, "mvlanenable",String.valueOf(mvlanenable));
		    jedis.hset(cbatinfokey, "mvlanid",String.valueOf(mvlanid));
		    jedis.hset(cbatinfokey, "bootver",hwversion);
		    jedis.hset(cbatinfokey, "appver",appver);
		    jedis.hset(cbatinfokey, "agentport",String.valueOf(trap_port));
		    jedis.hset(cbatinfokey, "trapserverip",trapserverip);
		    
		    jedis.hset(cbatkey, "netmask",netmask);
		    jedis.hset(cbatkey, "gateway",gateway);    
		    
		    JSONObject cbatjson = new JSONObject();
		    cbatjson.put("mvlanenable", mvlanenable);
		    cbatjson.put("mvlanid", mvlanid);
		    cbatjson.put("bootver", hwversion);
		    cbatjson.put("hwversion", hwversion);
		    cbatjson.put("appver", appver);
		    cbatjson.put("netmask", netmask);
		    cbatjson.put("gateway", gateway);
		    cbatjson.put("trapserverip", trapserverip);
		    cbatjson.put("trap_port", trap_port);
		    
		    String jsonString = cbatjson.toJSONString(); 
		    redisUtil.getJedisPool().returnResource(jedis);
		    jedis.publish("node.tree.cbatsync", jsonString);
	    }catch(Exception e){
	    	//e.printStackTrace();
	    	redisUtil.getJedisPool().returnBrokenResource(jedis);
	    	jedis.publish("node.tree.cbatsync", "");
	    	return;
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

	private static Boolean sendconfig(int proid,String cbatip, int cnuindex,Jedis jedis ){
		String prokey = "profileid:"+proid+":entity";
		try{
			
			//vlansts
		util.setV2PDU(cbatip,
			"161",
			new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,36,cnuindex}), 
			new Integer32(Integer.valueOf(jedis.hget(prokey, "vlanen")))
		);
		//p0vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,37,cnuindex}), 
				new Integer32(Integer.valueOf(jedis.hget(prokey, "vlan0id"))==0?1:Integer.valueOf(jedis.hget(prokey, "vlan0id")))
		);
		
		//p1vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,38,cnuindex}), 
				new Integer32(Integer.valueOf(jedis.hget(prokey, "vlan1id"))==0?1:Integer.valueOf(jedis.hget(prokey, "vlan1id")))
				);
		
		//p2vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,39,cnuindex}), 
				new Integer32(Integer.valueOf(jedis.hget(prokey, "vlan2id"))==0?1:Integer.valueOf(jedis.hget(prokey, "vlan2id")))
				);
		
		//p3vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,40,cnuindex}), 
				new Integer32(Integer.valueOf(jedis.hget(prokey, "vlan3id"))==0?1:Integer.valueOf(jedis.hget(prokey, "vlan3id")))
				);
		
		//if(pro.getTxlimitsts() != 0 ){			
			//cpuport tx sts
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,52,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "txlimitsts")))
					);
			
			//cpuport tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,53,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "cpuporttxrate")))
					);
			//eth1 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,54,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port0rxrate")))
					);
			//eth2 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,55,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port1rxrate")))
					);
			//eth3 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,56,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port2rxrate")))
					);
			//eth4 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,57,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port3rxrate")))
					);
		//}
		/*else{
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,52,cnuindex}), 
					new Integer32(0)
					);
		}*/
		
		//if(pro.getRxlimitsts() != 0){
			//rx sts
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,46,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "rxlimitsts")))
					);
			//cpuport rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,47,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "cpuportrxrate")))
					);
			//eth1 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,48,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port0txrate")))
					);
			//eth2 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,49,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port1txrate")))
					);
			//eth3 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,50,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port2txrate")))
					);
			//eth4 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,51,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port3txrate")))
					);
		/*}else{
			//rx sts
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,46,cnuindex}), 
					new Integer32(0)
					);
		}*/
		//reload profile
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,1,1,13,cnuindex}), 
				new Integer32(2)
				);	
		
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,1,1,13,cnuindex}), 
				new Integer32(3)
				);
		if(proid == 1)
		{
			//销户
			util.setV2PDU(cbatip,
			"161",
			new OID(new int[] {1,3,6,1,4,1,36186,8,1,1,13,cnuindex}), 
			new Integer32(4)
			);
			//return true;
		}
			return true;
		
		
		}catch(Exception e)
		{
			System.out.println("=============================>sendconfig error");
			//e.printStackTrace();
			return false;
		
		}
	}


	
	
	


	
}