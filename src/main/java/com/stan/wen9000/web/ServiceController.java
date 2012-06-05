package com.stan.wen9000.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;

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
		
		
		System.out.println("dowork pat="+pat + "    msg=" + message);
		if(pat.equalsIgnoreCase("servicecontroller.treeinit")){
			doNodeTreeInit();
		}else if(pat.equalsIgnoreCase("servicecontroller.index.init")){
			doNodeIndexInit(message);
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
		}else if(pat.equalsIgnoreCase("servicecontroller.profile_isedit")){
			doProfileIsEdit(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.profile_detail")){
			doProfileDetail(message);
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
		}else if(pat.equalsIgnoreCase("servicecontroller.discovery.search")){
			doDisSearch(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.discovery.searchtotal")){
			doDisSearchTotal(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.global_opt")){
			doOptGlobalopt(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.save_global")){
			doOptGlobalSave(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.saveredis")){
			doOptSaveRedis(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.onlinecbats")){
			doOptOnlineCbats(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.ftpconnet")){
			doOptFtpConnect(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.checkedcbats")){
			doOptCheckedCbats(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.updatedcbats")){
			doOptUpdatedcbats(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.updateproc")){
			doOptUpdatedProc(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.ftpupdate")){
			doOptFtpupdate(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.lastalarms")){
			doOptLastAlarms(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.preconfig_one")){
			doOptPreconfig_one(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.preconfig_batch")){
			doOptPreconfig_batch(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.preconfig_all")){
			doOptPreconfig_all(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.pre_del")){
			doOptPre_Del(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.gethistoryalarm")){
			doGetHistoryAlarm(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.historypage")){
			doGetHistoryPage(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.historynext")){
			doGetHistoryNext(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.historypre")){
			doGetHistoryPre(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.cbatreset")){
			doCbatReset(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.opt.ftpinfo")){
			doFtpInfo(message);
		}else if(pat.equalsIgnoreCase("servicecontroller.delnode")){
			doDelNode(message);
		}
		


	}
	
	private static void doDelNode(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//获取设备id		
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String mac = jsondata.get("mac").toString();
		String type = jsondata.get("type").toString();
		String id = jedis.get("mac:"+mac+":deviceid");
		jedis.del("mac:"+mac+":deviceid");
		if(type.equalsIgnoreCase("cbat")){
			//删除头端下的所有终端
			Set<String> cnus = jedis.smembers("cbatid:"+id+":cnus");
			for(Iterator it = cnus.iterator();it.hasNext();){
				String cnuid = it.next().toString();
				jedis.del("mac:"+jedis.hget("cnuid:"+cnuid+":entity", "mac")+":deviceid");
				jedis.del("cnuid:"+cnuid+":entity");
				
				//jedis.decr("global:deviceid");
			}
			//删除头端
			jedis.del("cbatid:"+id+":entity");
			jedis.del("cbatid:"+id+":cnus");
			jedis.del("cbatid:"+id+":cbatinfo");
		}else if(type.equalsIgnoreCase("cnu")){
			String cbatid = jedis.hget("cnuid:"+id+":entity", "cbatid");
			jedis.srem("cbatid:"+cbatid+":cnus", id);
			jedis.del("cnuid:"+id+":entity");
		}else{
			//hfc
			//TODO
			
			
			
			
		}
		
		jedis.save();
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doFtpInfo(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		String ip = jedis.get("global:ftpip");
		String port = jedis.get("global:ftpport");
		String username = jedis.get("global:ftpusername");
		String password = jedis.get("global:ftppassword");
		JSONObject json = new JSONObject();
		json.put("ftpip", ip);
		json.put("ftpport", port);
		json.put("username", username);
		json.put("password", password);
		jedis.publish("node.opt.ftpinfo", json.toJSONString());
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doCbatReset(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		String cbatid = jedis.get("mac:"+message+":deviceid");
		String cbatip = jedis.hget("cbatid:"+cbatid+":entity", "ip");
		
		try {
			String tmp = util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
			if(tmp == ""){
				jedis.publish("node.opt.cbatreset", "");
				redisUtil.getJedisPool().returnResource(jedis);
				return;
			}
			//恢复出厂设置
			util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,6,3,0}), 
				new Integer32(1)
			);
			jedis.publish("node.opt.cbatreset", "resetok");

		}catch(Exception e){
			jedis.publish("node.opt.cbatreset", "");
		}
		
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doNodeIndexInit(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		if(!jedis.exists("profileid:1:entity")){
			//插入默认模板
			//获取profileid
	    	String proid = String.valueOf(jedis.incr("global:profileid"));
	    	String prokey = "profileid:"+proid + ":entity";
	    	//组合存储字符串
	    	Map<String , String >  proentity = new HashMap<String, String>();
	    	proentity.put("profilename", "出厂模板");
	    	proentity.put("vlanen", "2");
	    	proentity.put("vlan0id", "1");
	    	proentity.put("vlan1id", "1");
	    	proentity.put("vlan2id", "1");
	    	proentity.put("vlan3id", "1");
	    	
	    	proentity.put("rxlimitsts", "2");
	    	proentity.put("cpuportrxrate", "0");
	    	proentity.put("port0txrate", "0");
	    	proentity.put("port1txrate", "0");
	    	proentity.put("port2txrate", "0");
	    	proentity.put("port3txrate", "0");
	    	
	    	proentity.put("txlimitsts", "2");
	    	proentity.put("cpuporttxrate", "0");
	    	proentity.put("port0rxrate", "0");
	    	proentity.put("port1rxrate", "0");
	    	proentity.put("port2rxrate", "0");
	    	proentity.put("port3rxrate", "0");
	    	//save
	    	jedis.hmset(prokey, proentity);
	    	
	    	proid = String.valueOf(jedis.incr("global:profileid"));
	    	prokey = "profileid:"+proid + ":entity";
	    	proentity = new HashMap<String, String>();
	    	proentity.put("profilename", "标准2M");
	    	proentity.put("vlanen", "2");
	    	proentity.put("vlan0id", "1");
	    	proentity.put("vlan1id", "1");
	    	proentity.put("vlan2id", "1");
	    	proentity.put("vlan3id", "1");
	    	
	    	proentity.put("rxlimitsts", "1");
	    	proentity.put("cpuportrxrate", "2048");
	    	proentity.put("port0txrate", "0");
	    	proentity.put("port1txrate", "0");
	    	proentity.put("port2txrate", "0");
	    	proentity.put("port3txrate", "0");
	    	
	    	proentity.put("txlimitsts", "1");
	    	proentity.put("cpuporttxrate", "1024");
	    	proentity.put("port0rxrate", "0");
	    	proentity.put("port1rxrate", "0");
	    	proentity.put("port2rxrate", "0");
	    	proentity.put("port3rxrate", "0");
	    	//save
	    	jedis.hmset(prokey, proentity);
	    	
	    	proid = String.valueOf(jedis.incr("global:profileid"));
	    	prokey = "profileid:"+proid + ":entity";
	    	proentity = new HashMap<String, String>();
	    	proentity.put("profilename", "标准4M");
	    	proentity.put("vlanen", "2");
	    	proentity.put("vlan0id", "1");
	    	proentity.put("vlan1id", "1");
	    	proentity.put("vlan2id", "1");
	    	proentity.put("vlan3id", "1");
	    	
	    	proentity.put("rxlimitsts", "1");
	    	proentity.put("cpuportrxrate", "4096");
	    	proentity.put("port0txrate", "0");
	    	proentity.put("port1txrate", "0");
	    	proentity.put("port2txrate", "0");
	    	proentity.put("port3txrate", "0");
	    	
	    	proentity.put("txlimitsts", "1");
	    	proentity.put("cpuporttxrate", "2048");
	    	proentity.put("port0rxrate", "0");
	    	proentity.put("port1rxrate", "0");
	    	proentity.put("port2rxrate", "0");
	    	proentity.put("port3rxrate", "0");
	    	//save
	    	jedis.hmset(prokey, proentity);
	    	//保存数据到硬盘
	    	jedis.save();
	    	
	    	redisUtil.getJedisPool().returnResource(jedis);
		}
		
	}
	
	private static void doOptPre_Del(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//获取预开户设备模板id
		String proid = jedis.get("preconfig:"+message+":entity");
		//删除预开户键
		jedis.del("preconfig:"+message+":entity");
		//删除集合中的值
		jedis.srem("profileid:"+proid+":cnus", message);
		jedis.bgsave();
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptPreconfig_all(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		Set<String> preconfigs = jedis.keys("preconfig:*:entity");
		JSONArray array = new JSONArray();
		for(Iterator it=preconfigs.iterator();it.hasNext();){
			JSONObject json = new JSONObject();
			String key = it.next().toString();
			String mac = key.substring(10, 27);
			json.put("mac", mac);
			json.put("proname", jedis.hget("profileid:"+jedis.get(key)+":entity", "profilename"));
			//没有实际意义，用于创建删除列
			json.put("tmp", "1");
			array.add(json);
		}
		jedis.publish("node.opt.preconfig_all", array.toJSONString());
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptPreconfig_batch(String message) throws ParseException{
		Jedis jedis=null;
		Boolean iserror = false;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String smac = jsondata.get("smac").toString();
		String emac = jsondata.get("emac").toString();
		String proid = jsondata.get("proid").toString();
		JSONObject resjson = new JSONObject();
		Long tmp1 = mactolong(smac.toUpperCase());
		Long tmp2 = mactolong(emac.toUpperCase());
		if(tmp1>tmp2){
			resjson.put("code", "2");
			jedis.publish("node.opt.preconfig_batch", resjson.toJSONString());
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}
		if(tmp2 - tmp1 >255)
		{
			resjson.put("code", "3");
			jedis.publish("node.opt.preconfig_batch", resjson.toJSONString());
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}
		JSONArray array = new JSONArray();
		while(tmp1<=tmp2)
		{
			String tmp_mac = longtomac(tmp1);
			tmp1++;
			//判断设备是否已被发现和预开户表中是否有此设备
			if((jedis.exists("mac:"+tmp_mac+":deviceid"))||(jedis.exists("preconfig:"+tmp_mac+":entity"))){
				//已存在
				iserror = true;//有错误标志位
				JSONObject json =new JSONObject();
				json.put("mac", tmp_mac);
				array.add(json);
				continue;
			}else{
				jedis.set("preconfig:"+tmp_mac+":entity", proid);
				//profile集合中加入此CNU
				jedis.sadd("profileid:"+proid+":cnus", tmp_mac);

			}
		}
		if(iserror){
			jedis.publish("node.opt.preconfig_batch", array.toJSONString());
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}else{
			resjson.put("code", "0");
		}
		jedis.publish("node.opt.preconfig_batch", resjson.toJSONString());
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptPreconfig_one(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String mac = jsondata.get("mac").toString();
		String proid = jsondata.get("proid").toString();
		//判断设备是否已被发现和预开户表中是否有此设备
		if((jedis.exists("mac:"+mac+":deviceid"))||(jedis.exists("preconfig:"+mac+":entity"))){
			//已存在
			jedis.publish("node.opt.preconfig_one", "");
		}else{
			jedis.set("preconfig:"+mac+":entity", proid);
			//profile集合中加入此CNU
			jedis.sadd("profileid:"+proid+":cnus", mac);
			JSONObject json = new JSONObject();
			json.put("mac", mac);
			json.put("profile", jedis.hget("profileid:"+proid+":entity", "profilename"));
			json.put("html", "<button id=pre_del>删除</button>");
			jedis.publish("node.opt.preconfig_one", json.toJSONString());
		}
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptLastAlarms(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		String alarmid = jedis.get("global:alarmid");
		if(alarmid == null){
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}
		//获取最后几条告警信息，发往前端
		long id = Long.parseLong(alarmid);
		String key = "";

		for(int i=14;i>-1;i--){
			JSONObject json = new JSONObject();
			key = "alarmid:"+(id - i)+":entity";
			if(jedis.hget(key, "alarmlevel") == null){
				continue;
			}
			json.put("alarmlevel", jedis.hget(key, "alarmlevel"));
			json.put("salarmtime", jedis.hget(key, "salarmtime"));
			json.put("cbatmac", jedis.hget(key, "cbatmac"));
			json.put("alarmcode", jedis.hget(key, "alarmcode"));
			json.put("cnalarminfo", jedis.hget(key, "cnalarminfo"));
			jedis.publish("node.alarm.newalarm", json.toJSONString());

		}
		
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptFtpupdate(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String ftpip = jsondata.get("ftpip").toString();
		String ftpport = jsondata.get("ftpport").toString();
		String username = jsondata.get("username").toString();
		String password = jsondata.get("password").toString();
		String filename = jsondata.get("filename").toString();
		//获取所有要升级的头端
		Set<String> cbats = jedis.smembers("global:updatedcbats");
		//记录升级头端数，用户前端进度跟踪
		jedis.set("global:updatedtotal", String.valueOf(cbats.size()));
		//记录已升级头端数，用户前端进度跟踪
		jedis.set("global:updated", "0");

		JSONObject jsonproc = new JSONObject();
		jsonproc.put("total", String.valueOf(cbats.size()));
		jsonproc.put("proc", "0");
		
		jedis.publish("node.opt.updateinfo", jsonproc.toJSONString());
		
		for(Iterator cbat=cbats.iterator();cbat.hasNext();){
			String cbatid = cbat.next().toString();
			jedis.srem("global:updatedcbats", cbatid);
			JSONObject json = new JSONObject();
			json.put("ftpip", ftpip);
			json.put("ftpport", ftpport);
			json.put("username", username);
			json.put("password", password);
			json.put("cbatid", cbatid);
			json.put("filename", filename);
			jedis.publish("ServiceUpdateProcess.update", json.toJSONString());
		}
		
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptUpdatedProc(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		String num =jedis.get("global:updated");
		String total = jedis.get("global:updatedtotal");
		JSONObject json = new JSONObject();
		if(num == null)
			num = "0";
		json.put("proc", num);
		json.put("total", total);
		
		jedis.publish("node.opt.updateproc", json.toJSONString());
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptUpdatedcbats(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String mac = jsondata.get("mac").toString();
		String value = jsondata.get("value").toString();
		
		//获取CBAT ID 
		String cnuid = jedis.get("mac:"+mac+":deviceid");
    	if(value.equalsIgnoreCase("true")){
    		//保存选择的cbat到集合
    		jedis.sadd("global:updatedcbats", cnuid);
    	}else{
    		//删除选择的cbat
    		jedis.srem("global:updatedcbats", cnuid);
    	}

    	redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptCheckedCbats(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//获取所有要升级的头端
		Set<String> cbats = jedis.smembers("global:updatedcbats");
		if(cbats.size()>0){
			jedis.publish("node.opt.checkedcbats", "xxxxxxxxx");
		}else{
			jedis.publish("node.opt.checkedcbats", "");
		}
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptFtpConnect(String message) throws ParseException{
		FTPClient ftpClient;
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String ip = jsondata.get("ftpip").toString();
		String port = jsondata.get("ftpport").toString();
		String username = jsondata.get("username").toString();
		String password = jsondata.get("password").toString();
		//保存到数据库
		jedis.set("global:ftpip", ip);
		jedis.set("global:ftpport", port);
		jedis.set("global:ftpusername", username);
		jedis.set("global:ftppassword", password);
		
		//连接ftp
		try
		  {
		      ftpClient= new FTPClient();
		      ftpClient.connect(ip,Integer.parseInt(port));
		      if(!ftpClient.login(username, password)){
		    	  ftpClient.disconnect();
			      jedis.publish("node.opt.ftpconnect", "");
			      redisUtil.getJedisPool().returnResource(jedis);
			      return;
		      }
		      JSONArray jsonarray = new JSONArray();
		      
		      if(ftpClient.isConnected()){
		    	  FTPFile[] remoteFiles = ftpClient.listFiles("/");
		    	  if(remoteFiles != null) {   
	                for(int i=0;i<remoteFiles.length;i++){
	                	JSONObject filejson = new JSONObject();
	                    String name = remoteFiles[i].getName(); 
	                    if(name.equalsIgnoreCase(".")||name.equalsIgnoreCase("..")){
	                    	continue;
	                    }
	                    filejson.put("filename", name);
	                    jsonarray.add(filejson);
	                }   
		          }    
		      }
		      ftpClient.logout();
		      ftpClient.disconnect();
		      jedis.publish("node.opt.ftpconnect", jsonarray.toJSONString());
		      redisUtil.getJedisPool().returnResource(jedis);
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
			  ftpClient=null;
			  jedis.publish("node.opt.ftpconnect", "");
			  redisUtil.getJedisPool().returnResource(jedis);
		  }
		
	}
	
	private static void doOptOnlineCbats(String message){
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		Set<String> cbats = jedis.keys("cbatid:*:entity");
		JSONArray jsonarray = new JSONArray();
		//清除所有要升级的头端
		Set<String> members = jedis.smembers("global:updatedcbats");
		if(!members.isEmpty()){
			for(Iterator it=members.iterator();it.hasNext();){
				jedis.srem("global:updatedcbats", it.next().toString());
			}
		}		
		//获取在线头端信息
		for(Iterator it=cbats.iterator();it.hasNext();){
			String key = it.next().toString();		
			if(jedis.hget(key, "active").equalsIgnoreCase("1")){
				JSONObject cbatjson = new JSONObject();
				cbatjson.put("check", "<input type=checkbox class=chk />");
				cbatjson.put("mac", jedis.hget(key, "mac"));
				cbatjson.put("ip", jedis.hget(key, "ip"));
				cbatjson.put("upgrade", jedis.hget(key, "upgrade"));
				switch(Integer.parseInt(jedis.hget(key, "devicetype")))
				{
		        	case 1:
		        		cbatjson.put("devicetype", "WEC-3501I X7");
		        		break;
		        	case 2:
		        		cbatjson.put("devicetype", "WEC-3501I E31");
		        		break;
		        	case 3:
		        		cbatjson.put("devicetype", "WEC-3501I Q31");
		        		break;
		        	case 4:
		        		cbatjson.put("devicetype", "WEC-3501I C22");
		        		break;
		        	case 5:
		        		cbatjson.put("devicetype", "WEC-3501I S220");
		        		break;
		        	case 6:
		        		cbatjson.put("devicetype", "WEC-3501I S60");
		        		break;
		        	default:
		        		cbatjson.put("devicetype", "Unknown");
		        		break;
				}
				int index = key.lastIndexOf(":");
				key = key.substring(0, index)+":cbatinfo";
				cbatjson.put("appver", jedis.hget(key, "appver"));
				jsonarray.add(cbatjson);
			}		
		}
		
		jedis.publish("node.opt.onlinecbats", jsonarray.toJSONString());
		redisUtil.getJedisPool().returnResource(jedis);
	}

	private static void doOptSaveRedis(String message){
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		jedis.save();
		jedis.publish("node.opt.saveredis", "");
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptGlobalSave(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//保存全局配置信息
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String ip = jsondata.get("ip").toString();
		String port = jsondata.get("port").toString();
		jedis.set("global:trapserver:ip", ip);
		jedis.set("global:trapserver:port", port);
		jedis.publish("node.opt.globalsave", "");
		//遍历头端,配置trapserverip，port
//		Set<String> cbats = jedis.keys("cbatid:*:entity");
//		for(Iterator it = cbats.iterator();it.hasNext();){
//			String cbatkey = it.next().toString();			
//			try {
//				//set trap server ip
//				util.setV2StrPDU(jedis.hget(cbatkey, "ip"),
//						"161",
//						new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}), 
//						jedis.get("global:trapserver:ip")
//						);
//				//save
//				util.setV2PDU(jedis.hget(cbatkey, "ip"),
//						"161",
//						new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), 
//						new Integer32(1)
//						);
//			}catch(Exception e){
//				
//			}
//			
//		}
		
		
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doOptGlobalopt(String message) throws ParseException{
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//获取全局配置trap server端口号和IP地址
		String ip = jedis.get("global:trapserver:ip");
		String port = jedis.get("global:trapserver:port");
		JSONObject json = new JSONObject();
		json.put("ip", ip);
		json.put("port", port);
		jedis.publish("node.opt.globalopt", json.toJSONString());
		
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doDisSearchTotal(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		String total = jedis.get("global:searchtotal");
		String proc = jedis.get("global:searched");
		JSONObject json = new JSONObject();
		json.put("total", total);
		json.put("proc", proc);
		jedis.publish("node.dis.searchtotal", json.toJSONString());
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doDisSearch(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String startip = jsondata.get("startip").toString();
		String stopip = jsondata.get("stopip").toString();
		String currentip = "";
		long longstartIp = IP2Long.ipToLong(startip);		
		long longstopIp = IP2Long.ipToLong(stopip);
		long total = longstopIp - longstartIp + 1;
		if((total >256)|| (longstartIp>longstopIp))
		{
			log.info("search ip out of range!");
			jedis.publish("node.dis.validate", "");
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}
		jedis.publish("node.dis.validate", "validateok");
		
		//将搜索总ip数写入redis，方便前端显示搜索进度
		jedis.set("global:searchtotal", String.valueOf(total));
		
		//将已搜索设备清0，此键是为防止用户刷新页面导致显示搜索进度不正确
		jedis.set("global:searched", "0");
		
		
		while (longstartIp <= longstopIp) {
			currentip = IP2Long.longToIP(longstartIp);
			jedis.publish("workdiscovery.new", currentip);

			longstartIp++;

		}
		
		redisUtil.getJedisPool().returnResource(jedis);
		
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
			String cnuindex = jedis.hget(cnukey, "devcnuid");
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
			
			//log.info("-----------oldproid---"+old_proid+"---------"+jedis.hget("profileid:"+old_proid+":entity","profilename"));
			if(jedis.exists("profileid:"+old_proid+":entity")){
				//如果原模板是配置信息，则删除
				if(jedis.hget("profileid:"+old_proid+":entity","profilename").equalsIgnoreCase("配置信息")){
					jedis.del("profileid:"+old_proid+":entity");
				}
			}
			
			//删除原profile集合中此CNU
			jedis.srem("profileid:"+old_proid+":cnus", cnuid);
			//更改CNU模板号
			jedis.hset(cnukey, "profileid", proid);
			//添加cnu到新profile集合中
			jedis.sadd("profileid:"+proid+":cnus", jedis.hget(cnukey, "mac"));		
			
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
		//json.put("vlanid", jedis.hget(prokey, "vlanid"));
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
		//获取所有已选定的CNU
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
            	case 10:
            		result = "3702I-C4";            		
            		break;
            	case 7:
            		result = "3702I-L2";             		
            		break;
            	case 9:
            		result = "3702I-C2";
            		break;
            	default:
            		result = "Unknown";
            		break;
    		}
    		json.put("cbatip", jedis.hget("cbatid:"+jedis.hget(cnukey, "cbatid")+":entity", "ip"));
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
	
	//获取所有在线头端下的CNU
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
        	if(jedis.hget("cbatid:"+jedis.hget(prokey, "cbatid")+":entity", "active").equalsIgnoreCase("0")){
        		continue;
        	}
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
    		String cbatid = jedis.hget(prokey, "cbatid");
    		cnujson.put("cbatip", jedis.hget("cbatid:"+cbatid+":entity", "ip"));
    		cnujson.put("mac", jedis.hget(prokey, "mac"));
    		cnujson.put("active", jedis.hget(prokey, "active"));
    		cnujson.put("label", jedis.hget(prokey, "label"));

    		switch(Integer.parseInt(jedis.hget(prokey, "devicetype")))
    		{
	    		case 10:
	        		result = "3702I-C4";            		
	        		break;
	        	case 7:
	        		result = "3702I-L2";             		
	        		break;
	        	case 9:
	        		result = "3702I-C2";
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
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		//获取CNU ID 
		String cnuid = jedis.get("mac:"+jsondata.get("mac").toString()+":deviceid");
    	String key = "cnuid:"+cnuid+":entity";
    	String devid = jedis.hget(key, "devcnuid");
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
    	//配置CNU
		if(Cnuconfig(jsondata,cbatip,Integer.parseInt(devid),jedis)){
			
		}else{
			jedis.publish("node.tree.cnu_sub", "");
			redisUtil.getJedisPool().returnResource(jedis);
			return;
		}
		
		//获取cnu原profileid号
		String old_proid = jedis.hget(key, "profileid");
		String proid = old_proid;
		//判断CNU原模板是否是配置信息
		if(jsondata.get("proname").toString().equalsIgnoreCase("配置信息")){
			editcustomprofile(jsondata,jedis,"profileid:"+old_proid+":entity");
		}else{			
			//删除原profile集合中此CNU
			jedis.srem("profileid:"+old_proid+":cnus", jedis.hget(key, "mac"));
			
			//新建自定义模板
			proid = newcustomprofile(jsondata,jedis);
			//更改CNU模板号
			jedis.hset(key, "profileid", proid);
		}
		
		JSONObject json = new JSONObject();
		json.put("profilename", jedis.hget("profileid:"+proid+":entity", "profilename"));
		jedis.publish("node.tree.cnu_sub", json.toJSONString());		
		
		//保存数据到硬盘
		jedis.bgsave();
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
    	String devid = jedis.hget(key, "devcnuid");
    	//获取所属头端信息
    	String cbatid = jedis.hget(key,"cbatid");
    	String cbatip = jedis.hget("cbatid:"+cbatid+":entity", "ip");
    	
    	//判断头端是否在线
    	String tmp = util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
		if(tmp == ""){
			log.info("--------------------------------->>>>>>>>>>>>tmp -1,头端不在线?");
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			jedis.publish("node.tree.cnusync", "");
			return;
		}
    	//获取终端信息
		try{
			int vlanenable = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,36,Integer.parseInt(devid)}));
			int port0vid = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,37,Integer.parseInt(devid)}));
			int port1vid = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,38,Integer.parseInt(devid)}));
			int port2vid = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,39,Integer.parseInt(devid)}));
			int port3vid = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,40,Integer.parseInt(devid)}));
			
			int txlimitsts = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,52,Integer.parseInt(devid)}));
			int cpuporttxrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,53,Integer.parseInt(devid)}));
			int cpuportrxrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,47,Integer.parseInt(devid)}));
			int port0txrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,48,Integer.parseInt(devid)}));
			int port1txrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,49,Integer.parseInt(devid)}));
			int port2txrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,50,Integer.parseInt(devid)}));
			int port3txrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,51,Integer.parseInt(devid)}));
			
			int rxlimitsts = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,46,Integer.parseInt(devid)}));
			int port0rxrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,54,Integer.parseInt(devid)}));
			int port1rxrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,55,Integer.parseInt(devid)}));
			int port2rxrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,56,Integer.parseInt(devid)}));
			int port3rxrate = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,57,Integer.parseInt(devid)}));

			JSONObject json = new JSONObject();
			json.put("active", jedis.hget(key, "active"));
			String proid = jedis.hget(key, "profileid");
			json.put("profilename", jedis.hget("profileid:"+proid+":entity", "profilename"));
			json.put("vlanen", String.valueOf(vlanenable));
			json.put("vlan0id", String.valueOf(port0vid));
			json.put("vlan1id", String.valueOf(port1vid));
			json.put("vlan2id", String.valueOf(port2vid));
			json.put("vlan3id", String.valueOf(port3vid));
			json.put("txlimitsts", String.valueOf(txlimitsts));
			json.put("cpuporttxrate", String.valueOf(cpuporttxrate));
			json.put("cpuportrxrate", String.valueOf(cpuportrxrate));
			json.put("port0txrate", String.valueOf(port0txrate));
			json.put("port1txrate", String.valueOf(port1txrate));
			json.put("port2txrate", String.valueOf(port2txrate));
			json.put("port3txrate", String.valueOf(port3txrate));
			json.put("rxlimitsts", String.valueOf(rxlimitsts));
			json.put("port0rxrate", String.valueOf(port0rxrate));
			json.put("port1rxrate", String.valueOf(port1rxrate));
			json.put("port2rxrate", String.valueOf(port2rxrate));
			json.put("port3rxrate", String.valueOf(port3rxrate));
			
			jedis.publish("node.tree.cnusync", json.toJSONString());
		}catch(Exception e){
			jedis.publish("node.tree.cnusync", "");
		}
		
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
    	//String vlanid = jsondata.get("vlanid").toString();
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
    	//proentity.put("vlanid", vlanid);
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
		//json.put("vlanid", jedis.hget(prokey, "vlanid"));
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
	
	private static void doProfileDetail(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();
		 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		String prokey = "profileid:"+message+":entity";
		JSONObject json = new JSONObject();
		json.put("proname", jedis.hget(prokey, "profilename"));		
		json.put("vlanen", jedis.hget(prokey, "vlanen"));
		//json.put("vlanid", jedis.hget(prokey, "vlanid"));
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
	    jedis.publish("node.pro.detail", jsonString);
	}
	
	private static void doProfileIsEdit(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		//log.info("--------->>>profileIsEdit----");
		//判断是否有CNU指向此模板
		if(jedis.smembers("profileid:"+message+":cnus").isEmpty()){
			String prokey = "profileid:"+message+":entity";
			JSONObject json = new JSONObject();
			json.put("proname", jedis.hget(prokey, "profilename"));		
			json.put("vlanen", jedis.hget(prokey, "vlanen"));
			//json.put("vlanid", jedis.hget(prokey, "vlanid"));
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
			jedis.publish("node.pro.isedit", jsonString);
		}else{
			jedis.publish("node.pro.isedit", "");
		}
		redisUtil.getJedisPool().returnResource(jedis);
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
    	//String vlanid = jsondata.get("vlanid").toString();
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
    	//proentity.put("vlanid", vlanid);
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
        	if(jedis.hget(prokey, "profilename").equalsIgnoreCase("配置信息")){
        		continue;
        	}
        	int index1 = prokey.indexOf(':') +1;
    		int index2 = prokey.lastIndexOf(':');
    		String cid = prokey.substring(index1, index2);
    		projson.put("id", cid);
    		projson.put("proname", jedis.hget(prokey, "profilename"));
    		projson.put("vlanen", jedis.hget(prokey, "vlanen"));
    		//projson.put("vlanid", jedis.hget(prokey, "vlanid"));
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
		    
		    jedis.hset(cbatinfokey, "netmask",netmask);
		    jedis.hset(cbatinfokey, "gateway",gateway);    
		    
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
    	//log.info("gateway::::::::::"+ gateway);
    	//获取CBAT ID 
		String cbatid = jedis.get("mac:"+mac+":deviceid");
		String cbatkey = "cbatid:"+cbatid+":entity";
		String cbatinfokey = "cbatid:"+cbatid+":cbatinfo";
		
		//发往设备修改设备相关参数(ip/mvlanenable/mvlanid)
    	try{    		
    		String oldip = jedis.hget(cbatkey, "ip");
    		//判断是否要跟设备交互
    		if((oldip.equalsIgnoreCase(ip))&&(mvlanenable.equalsIgnoreCase(jedis.hget(cbatinfokey, "mvlanenable")))&&(mvlanid.equalsIgnoreCase(jedis.hget(cbatinfokey, "mvlanid")))
    				&&(trapserver.equalsIgnoreCase(jedis.hget(cbatinfokey, "trapserverip")))&&(trap_port.equalsIgnoreCase(jedis.hget(cbatinfokey, "agentport")))
    				&&(netmask.equalsIgnoreCase(jedis.hget(cbatinfokey, "netmask")))&&(gateway.equalsIgnoreCase(jedis.hget(cbatinfokey, "gateway")))){
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
    		//判断Ip地址是否和其它头端冲突
    		Set<String> cbats = jedis.keys("cbatid:*:entity");
    		for(Iterator it=cbats.iterator();it.hasNext();){
    			String key = it.next().toString();
    			if((jedis.hget(key, "ip").equalsIgnoreCase(ip))&&(!cbatkey.equalsIgnoreCase(key))){
    				redisUtil.getJedisPool().returnBrokenResource(jedis);
        			jedis.publish("node.tree.cbatmodify", "ipconflict");
        			return;
    			}
    		}
    		util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0}), new Integer32(Integer.valueOf(mvlanenable)));
    		util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,5,0}), new Integer32(Integer.valueOf(mvlanid)));    		
    		util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}), trapserver);
    		util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,7,0}), new Integer32(Integer.valueOf(trap_port)));
    		
    		if((!oldip.equalsIgnoreCase(ip))||(!netmask.equalsIgnoreCase(jedis.hget(cbatinfokey, "netmask")))||(!gateway.equalsIgnoreCase(jedis.hget(cbatinfokey, "gateway")))){
    			util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,1,0}), ip);
    			util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0}), netmask);
        		util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,3,0}), gateway);
    			util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), new Integer32(1));
    			util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,6,1,0}), new Integer32(1));
    		}else{
    			//save
    			util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), new Integer32(1));
    		}
    		
    		//保存
        	jedis.hset(cbatkey, "ip", ip);
        	jedis.hset(cbatkey, "label", label);
        	jedis.hset(cbatinfokey, "netmask", netmask);
        	jedis.hset(cbatinfokey, "gateway", gateway);
        	
        	jedis.hset(cbatinfokey, "address", address);
        	jedis.hset(cbatinfokey, "mvlanenable", mvlanenable);
        	jedis.hset(cbatinfokey, "mvlanid", mvlanid);
        	jedis.hset(cbatinfokey, "trapserverip", trapserver);
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
    	switch(Integer.parseInt(jedis.hget(cnukey, "devicetype")))
		{
    		case 10:
    			cnujson.put("devicetype", "3702I-C4");         		
        		break;
        	case 7:
        		cnujson.put("devicetype", "3702I-L2");           		
        		break;
        	case 9:
        		cnujson.put("devicetype", "3702I-C2");
        		break;
        	default:
        		cnujson.put("devicetype", "Unknown");
        		break;
		}        		
		cnujson.put("label", jedis.hget(cnukey,"label"));
		cnujson.put("address", jedis.hget(cnukey,"address"));
		cnujson.put("contact", jedis.hget(cnukey,"contact"));	
		cnujson.put("phone", jedis.hget(cnukey,"phone"));
    	if(jedis.hget(cnukey, "active").equalsIgnoreCase("1")){
    		//设备在线,获取实时设备信息
    		cnujson.put("active", "在线");
    	}else{
    		//设备离线，获取redis信息
    		cnujson.put("active", "离线");
    	}
    	
    	//添加模板信息
    	//获取对应的profileid
    	String proid = jedis.hget("cnuid:"+id+":entity", "profileid");
    	String prokey = "profileid:"+proid+":entity";
    	cnujson.put("profilename", jedis.hget(prokey,"profilename"));
    	cnujson.put("vlanen", jedis.hget(prokey,"vlanen"));
    	//cnujson.put("vlanid", jedis.hget(prokey,"vlanid"));
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
					
		switch(Integer.parseInt(jedis.hget(cbatkey, "devicetype")))
		{
        	case 1:
        		result = "WEC-3501I X7";
        		break;
        	case 2:
        		result = "WEC-3501I E31";
        		break;
        	case 3:
        		result ="WEC-3501I Q31";
        		break;
        	case 4:
        		result ="WEC-3501I C22";
        		break;
        	case 5:
        		result ="WEC-3501I S220";
        		break;
        	case 6:
        		result ="WEC-3501I S60";
        		break;
        	case 7:
        		//result ="WEC-3501I C22";
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
		cbatjson.put("netmask", jedis.hget(cbatinfokey,"netmask"));
		cbatjson.put("gateway", jedis.hget(cbatinfokey,"gateway"));
		cbatjson.put("trapserver", jedis.hget(cbatinfokey, "trapserverip"));	
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
    	eocjson.put("icon", "home.png");

    	//"children"
		
		JSONArray cbatarray = new JSONArray();
    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		
    		JSONObject cbatjson = new JSONObject();

    		String key = it.next().toString();
   
    		//add head;
    		cbatjson.put("title", jedis.hget(key, "label"));
    		cbatjson.put("key", jedis.hget(key, "mac"));
    		cbatjson.put("online", jedis.hget(key, "active"));
    		//添加头端信息    		
    		if(jedis.hget(key, "active").equalsIgnoreCase("1")){
    			cbatjson.put("icon", "cbaton.png");
    			//"children"+'"'+":";
    		}else{
    			cbatjson.put("icon", "cbatoff.png");
    			//+"children"+'"'+":";
    		}
    		//添加tips
    		cbatjson.put("tooltip",jedis.hget(key, "ip"));
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
        		cnujson.put("title", jedis.hget(key_cnu, "label"));
        		cnujson.put("key", jedis.hget(key_cnu, "mac"));
        		cnujson.put("online", jedis.hget(key_cnu, "active"));
        		
        		cnujson.put("tooltip", jedis.hget(key_cnu, "mac"));
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
	
	private static String newcustomprofile(JSONObject jsondata, Jedis jedis){
		//获取传递参数
    	String vlanen = jsondata.get("vlanen").toString();
    	//String vlanid = jsondata.get("vlanid").toString();
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
    	proentity.put("profilename", "配置信息");
    	proentity.put("vlanen", vlanen);
    	//proentity.put("vlanid", vlanid);
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
    	
    	return proid;
    	//logger.info("prokeys::::::proname"+ proname + "---vlanen::::"+vlanen );
	}
	
	private static void editcustomprofile(JSONObject jsondata, Jedis jedis,String key){
		//获取传递参数
    	String vlanen = jsondata.get("vlanen").toString();
    	//String vlanid = jsondata.get("vlanid").toString();
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
		
    	//组合存储字符串
    	Map<String , String >  proentity = new HashMap<String, String>();
    	proentity.put("profilename", "配置信息");
    	proentity.put("vlanen", vlanen);
    	//proentity.put("vlanid", vlanid);
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
    	jedis.hmset(key, proentity);
    	//logger.info("prokeys::::::proname"+ proname + "---vlanen::::"+vlanen );
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

	private static Boolean Cnuconfig(JSONObject jsondata,String cbatip, int cnuindex,Jedis jedis ){
		try{			
			//vlansts
		util.setV2PDU(cbatip,
			"161",
			new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,36,cnuindex}), 
			new Integer32(Integer.valueOf(jsondata.get("vlanen").toString()))
		);
		//p0vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,37,cnuindex}), 
				new Integer32(Integer.valueOf(jsondata.get("vlan0id").toString())==0?1:Integer.valueOf(jsondata.get("vlan0id").toString()))
		);
		
		//p1vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,38,cnuindex}), 
				new Integer32(Integer.valueOf(jsondata.get("vlan1id").toString())==0?1:Integer.valueOf(jsondata.get("vlan1id").toString()))
				);
		
		//p2vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,39,cnuindex}), 
				new Integer32(Integer.valueOf(jsondata.get("vlan2id").toString())==0?1:Integer.valueOf(jsondata.get("vlan2id").toString()))
				);
		
		//p3vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,40,cnuindex}), 
				new Integer32(Integer.valueOf(jsondata.get("vlan3id").toString())==0?1:Integer.valueOf(jsondata.get("vlan3id").toString()))
				);
		
		//if(pro.getTxlimitsts() != 0 ){			
			//cpuport tx sts
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,52,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("txlimitsts").toString()))
					);
			
			//cpuport tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,53,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("cpuporttxrate").toString()))
					);
			//eth1 tx     
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,54,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("port0rxrate").toString()))
					);
			//eth2 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,55,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("port1rxrate").toString()))
					);
			//eth3 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,56,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("port2rxrate").toString()))
					);
			//eth4 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,57,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("port3rxrate").toString()))
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
					new Integer32(Integer.valueOf(jsondata.get("rxlimitsts").toString()))
					);
			//cpuport rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,47,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("cpuportrxrate").toString()))
					);
			//eth1 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,48,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("port0txrate").toString()))
					);
			//eth2 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,49,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("port1txrate").toString()))
					);
			//eth3 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,50,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("port2txrate").toString()))
					);
			//eth4 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,51,cnuindex}), 
					new Integer32(Integer.valueOf(jsondata.get("port3txrate").toString()))
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
		
		
		}catch(Exception e)
		{
			System.out.println("=============================>Cnuconfig error");
			//e.printStackTrace();
			return false;
		
		}
		return true;
	}
	
	private static long mactolong(String macstring)
	{
		int index1 = 0;
		int index2 = 0;
		String tmp_mac = "";
		try {
			index1 = macstring.indexOf(":");
			tmp_mac=macstring.substring(0, index1);

			index2 = macstring.indexOf(":", index1 + 1);
			tmp_mac += macstring.substring(index1 + 1,index2);

			index1 = index2;
			index2 = macstring.indexOf(":", index1 + 1);
			tmp_mac += macstring.substring(index1 + 1,index2);

			index1 = index2;
			index2 = macstring.indexOf(":", index1 + 1);
			tmp_mac += macstring.substring(index1 + 1,index2);

			index1 = index2;
			index2 = macstring.indexOf(":", index1 + 1);
			tmp_mac += macstring.substring(index1 + 1,index2);
			
			index1 = index2;
			tmp_mac +=macstring.substring(index1 + 1);

		} catch (Exception e) {
			e.printStackTrace();
			
		}
		System.out.println("mac_tmp="+tmp_mac);
		
		return Long.parseLong(tmp_mac, 16);
	}
	
	private static String longtomac(Long Lmac)
	{
		String tmp_mac = Long.toHexString(Lmac);
		String mac = "";
		for(int i=0;i<=10;i+=2)
		{
			mac += tmp_mac.substring(i,i+2); 
			if(i!=10)
			{
				mac += ":";
			}
		}
		System.out.println("======>>>mac = "+ mac);
		return mac;
	}

	private static void doGetHistoryAlarm(String message) {
		Jedis jedis = null;
		System.out.println("now doGet HistoryAlarm Spring get msg=" + message);
		try {
			jedis = redisUtil.getConnection();

		} catch (Exception e) {
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}		
		String historypage;
		//历史告警导航记录
		if(jedis.exists("global:historypage")){
			historypage = jedis.get("global:historypage");
		}else{
			jedis.set("global:historypage", "1");
			historypage = "1";
		}
		try {
			JSONArray jsonResponseArray = new JSONArray();
			String alarmid = jedis.get("global:alarmid");
			if(alarmid == null){
				redisUtil.getJedisPool().returnResource(jedis);
				return;
			}
			
			//获取最后几条告警信息，发往前端
			long id = Long.parseLong(alarmid);
			String alarmkey = "";
			if(id>=1000){
				for(int i=1000*Integer.parseInt(historypage);i>-1;i--){
					JSONObject alarmjson = new JSONObject();
					alarmkey = "alarmid:"+(id - i)+":entity";
					if(jedis.hget(alarmkey, "alarmlevel") == null){
						continue;
					}
					alarmjson.put("salarmtime", jedis.hget(alarmkey, "salarmtime"));
					alarmjson.put("alarmcode", jedis.hget(alarmkey, "alarmcode"));
					alarmjson.put("cnalarminfo", jedis.hget(alarmkey, "cnalarminfo"));
					alarmjson.put("alarmlevel", jedis.hget(alarmkey, "alarmlevel"));
					alarmjson.put("runingtime", jedis.hget(alarmkey, "runingtime"));
					alarmjson.put("cbatmac", jedis.hget(alarmkey, "cbatmac"));
					jsonResponseArray.add(alarmjson);

				}
			}else{
				Set<String> alarms = jedis.keys("alarmid:*:entity");
				for(Iterator it=alarms.iterator();it.hasNext();){
					alarmkey = it.next().toString();
					JSONObject alarmjson = new JSONObject();
					alarmjson.put("salarmtime", jedis.hget(alarmkey, "salarmtime"));
					alarmjson.put("alarmcode", jedis.hget(alarmkey, "alarmcode"));
					alarmjson.put("cnalarminfo", jedis.hget(alarmkey, "cnalarminfo"));
					alarmjson.put("alarmlevel", jedis.hget(alarmkey, "alarmlevel"));
					alarmjson.put("runingtime", jedis.hget(alarmkey, "runingtime"));
					alarmjson.put("cbatmac", jedis.hget(alarmkey, "cbatmac"));
					jsonResponseArray.add(alarmjson);
				}
			}
						
			
			String jsonString = jsonResponseArray.toJSONString();
			// publish to notify node.js a new alarm
			jedis.publish("node.historyalarm.getall", jsonString);

			redisUtil.getJedisPool().returnResource(jedis);

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private static void doGetHistoryPage(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		int historypage = Integer.parseInt(jedis.get("global:historypage"));

		long count = jedis.zcard("alarm_history_queue");
		JSONObject json = new JSONObject();
		if((count/(historypage*1000) != 0)&&(count!=(historypage*1000))){
			//有下一页
			json.put("from", (historypage-1)*1000);
			json.put("end", historypage*1000);
			json.put("hasnext", "1");
			json.put("total", count);
			if(historypage>1){
				json.put("haspre", "1");
			}else{
				json.put("haspre", "0");
			}
		}else{
			//已到最后一页
			json.put("from", (historypage-1)*1000);
			json.put("end", count);
			json.put("hasnext", "0");
			
			json.put("total", count);
			if(historypage>1){
				json.put("haspre", "1");
			}else{
				json.put("haspre", "0");
			}
		}
		
		jedis.publish("node.historyalarm.gethistorypage", json.toJSONString());
		redisUtil.getJedisPool().returnResource(jedis);
	}

	private static void doGetHistoryNext(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		jedis.incr("global:historypage");
		jedis.publish("node.historyalarm.gethistorynp", "");
		redisUtil.getJedisPool().returnResource(jedis);
	}
	
	private static void doGetHistoryPre(String message) throws ParseException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		jedis.decr("global:historypage");
		jedis.publish("node.historyalarm.gethistorynp", "");
		redisUtil.getJedisPool().returnResource(jedis);
	}
}
