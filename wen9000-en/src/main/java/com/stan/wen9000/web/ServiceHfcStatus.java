package com.stan.wen9000.web;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;

public class ServiceHfcStatus{	
	private static Logger log = Logger.getLogger(ServiceHfcStatus.class);

	private static RedisUtil redisUtil;
	private static Jedis jedis = null;
	
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceHfcStatus.redisUtil = redisUtil;
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
	        	try {
	      			//arg2 is mssage now is currenti p
	      			
	      			
	      			
	      			servicestart(arg1, arg2);
	      			
	      		}catch(Exception e){
	      			e.printStackTrace();			
	      		}
	        }

	    };
	    
	  //此进程用于判断HFC设备上下线
		
		private void start(){
			log.info("[#3] ..... ServiceHfcStatus start");
			Jedis jedis=null;
			try {
			 jedis = redisUtil.getConnection();
			 
			 jedis.psubscribe(jedissubSub, "ServiceHfcStatus.*");
			redisUtil.getJedisPool().returnResource(jedis);
			}catch(Exception e){
				e.printStackTrace();
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				
			}
			
		}		
		
		
		private static void servicestart(String pat, String message) throws ParseException{

				
				//System.out.println(" [x] ServiceHfcStatus Received '" + message
				//		+ "'");
							
				dowork(message);					

		}
		
		
		private static void dowork(String message) throws ParseException{	
			Jedis jedis=null;
			try {
			 jedis = redisUtil.getConnection();	 
			
			}catch(Exception e){
				e.printStackTrace();
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				return;
			}
			
			JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
			//log.info("---------------json------"+jsondata);
			String mac = jsondata.get("mac").toString();
			String flag = jsondata.get("flag").toString();
			String devid = jedis.get("mac:"+mac.trim()+":deviceid");
			String key = "hfcid:"+ devid + ":entity";
			try{
				if(jedis.hget(key, "active").equalsIgnoreCase(flag)){
					redisUtil.getJedisPool().returnBrokenResource(jedis);
					return;
				}
			}catch(Exception e){
				//System.out.println("---------key-----"+key+"----mac----"+mac);
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				return;
			}
			
			jedis.hset(key, "active", flag);
			
			Sendstschange("hfc", devid, jedis);
			
			redisUtil.getJedisPool().returnResource(jedis);
		}
		
		private static void Sendstschange(String type,String devid,Jedis jedis){ 
			JSONObject json = new JSONObject();
			if(type == "cbat"){
				String cbatkey = "cbatid:"+devid+":entity";
				json.put("mac", jedis.hget(cbatkey,"mac"));
				json.put("online", jedis.hget(cbatkey,"active"));
				json.put("ip", jedis.hget(cbatkey,"ip"));
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
				json.put("online", jedis.hget(hfckey,"active"));
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