package com.stan.wen9000.web;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;



public class ServiceController {

	
	private static Logger log = Logger.getLogger(ServiceController.class);



	
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

		log.info("[#3] ..... service alarm");

		
		System.out.println("[#3] ..... service discovery starting");
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
		}


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
    	
    	
    	
    	String jsonstring = "";
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