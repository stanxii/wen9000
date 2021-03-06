package com.stan.eoc.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.stan.eoc.action.jedis.util.RedisUtil;

public class ServiceUpdateProcess{
	
	private static Logger log = Logger.getLogger(ServiceUpdateProcess.class);

	private static SnmpUtil util = new SnmpUtil();
	private static JsonPost jpost = new JsonPost();
	
	private static RedisUtil redisUtil;

	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceUpdateProcess.redisUtil = redisUtil;
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
	
	      	System.out.println("[x]ServiceUpdateProcess  Subscribing....pmessage....now receive on msgarge1 [" + arg1 + "] arg2=["+msg +"]");
	      	try {
	  			//arg2 is mssage now is currenti p
	  			
	  			
	  			
	  			servicestart(arg1, msg);
	  			
	  		}catch(Exception e){
	  			e.printStackTrace();			
	  		}
	  		
	      }

	};
  
	  public void start() {
	
			log.info("[#3] ..... service Update starting");
	
			Jedis jedis=null;
			try {
			 jedis = redisUtil.getConnection();			
			
			 jedis.psubscribe(jedissubSub, "ServiceUpdateProcess.*");
			redisUtil.getJedisPool().returnResource(jedis);
			}catch(Exception e){
				e.printStackTrace();
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				
			}	
		}
	  
	  public static void servicestart(String pat, String message) throws Exception {
	
			System.out.println(" [x] Service Update Received '" + message
					+ "'");
			
			//long start = System.currentTimeMillis();  			
			dowork(pat, message);					
			//long end = System.currentTimeMillis();  
			//System.out.println("one ServiceAlarmProcessor dowork spend: " + ((end - start)) + " milliseconds");  
			
		
	}
	  
	  public static void dowork(String pat, String message) throws ParseException, IOException {
		  
		  if(pat.equalsIgnoreCase("ServiceUpdateProcess.update")){
				doNodeUpdate(message);
			}else if(pat.equalsIgnoreCase("ServiceUpdateProcess.updateinfo")){
				doNodeUpdateInfo(message);
			}
		  
	}
	  
	  private static void doNodeUpdateInfo(String message) throws ParseException{
		  Jedis jedis=null;
			try {
				jedis = redisUtil.getConnection();	 
			
			}catch(Exception e){
				e.printStackTrace();
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				return;
			}
			String num = jedis.get("global:updated");
			String total = jedis.get("global:updatedtotal");
			JSONObject json = new JSONObject();
			json.put("total", total);
			json.put("proc", num);
			
			jedis.publish("node.opt.updateinfo", json.toJSONString());
			redisUtil.getJedisPool().returnResource(jedis);
	  }
	  
	  private static void doNodeUpdate(String message) throws ParseException{
		  Jedis jedis=null;
			try {
			 jedis = redisUtil.getConnection();	 
			
			}catch(Exception e){
				e.printStackTrace();
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				return;
			}
			//正在升级中
			jedis.set("global:isupdating", "true");
			
			JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
			String ftpip = jsondata.get("ftpip").toString();
			String ftpport = jsondata.get("ftpport").toString();
			String username = jsondata.get("username").toString();
			String password = jsondata.get("password").toString();
			String filename = jsondata.get("filename").toString();
			String cbatid = jsondata.get("cbatid").toString();
			String cbatkey = "cbatid:"+cbatid+":entity";
			String cbatip = jedis.hget(cbatkey, "ip");
			if(jedis.hget(cbatkey, "protocal") == null){
				try{				
					String isnull = util.getStrPDU(cbatip, "161", new OID(new int[] {
							1, 3, 6, 1, 4, 1, 36186, 8, 4, 4, 0 }));
					
					util.setV2StrPDU(cbatip,
							"161",
							new OID(new int[] {1,3,6,1,4,1,36186,8,7,2,0}), 
							ftpip
							);

					util.setV2PDU(cbatip,
							"161",
							new OID(new int[] {1,3,6,1,4,1,36186,8,7,3,0}), 
							new Integer32(Integer.parseInt(ftpport))
							);
					
					util.setV2StrPDU(cbatip,
							"161",
							new OID(new int[] {1,3,6,1,4,1,36186,8,7,4,0}), 
							username
							);
					
					util.setV2StrPDU(cbatip,
							"161",
							new OID(new int[] {1,3,6,1,4,1,36186,8,7,5,0}), 
							password
							);
					
					util.setV2StrPDU(cbatip,
							"161",
							new OID(new int[] {1,3,6,1,4,1,36186,8,7,6,0}), 
							filename
							);
					
					//save
					util.setV2PDU(cbatip,
							"161",
							new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), 
							new Integer32(1)
							);
					//proceed
					util.setV2PDU(cbatip,
							"161",
							new OID(new int[] {1,3,6,1,4,1,36186,8,7,7,0}), 
							new Integer32(1)
							);
					
					redisUtil.getJedisPool().returnResource(jedis);
					
				}catch(Exception e){
					e.printStackTrace();
					jedis.hset(cbatkey, "upgrade", "4");
					//已升级头端加1
					long num_t = jedis.incr("global:updated");
					String num = String.valueOf(num_t);
					//删除集合中此头端
					//jedis.srem("global:updatedcbats", cbatid);
					//通知前端此头端完成升级
					String total = jedis.get("global:updatedtotal");
					JSONObject json = new JSONObject();
					json.put("proc", num);
					json.put("total", total);
					jedis.publish("node.opt.updateproc", json.toJSONString());
					redisUtil.getJedisPool().returnResource(jedis);
					return;
				}
			}else{
				JSONObject sjson = new JSONObject();
				sjson.put("ftpserverip", ftpip);
				sjson.put("ftpserverport", Integer.parseInt(ftpport));
				sjson.put("ftpuser", username);
				sjson.put("ftpkey", password);
				sjson.put("ftppath", filename);
				JSONObject resultjson = jpost.post("http://" + cbatip + "/setCbatFtpAndUpdate.json",
						sjson);
				if(resultjson.get("status").toString().equalsIgnoreCase("1")){
					jedis.hset(cbatkey, "upgrade", "4");
					//已升级头端加1
					long num_t = jedis.incr("global:updated");
					String num = String.valueOf(num_t);
					//删除集合中此头端
					//jedis.srem("global:updatedcbats", cbatid);
					//通知前端此头端完成升级
					String total = jedis.get("global:updatedtotal");
					JSONObject json = new JSONObject();
					json.put("proc", num);
					json.put("total", total);
					jedis.publish("node.opt.updateproc", json.toJSONString());
					redisUtil.getJedisPool().returnResource(jedis);
					return;
				}
			}			
			
			//更新头端时间戳
			Date date = new Date();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
			String logtimes = format.format(date);
					
			
			/////////////////////////////save cbatinfo
		
			String scbatinfokey = "cbatid:" + cbatid + ":cbatinfo";
			jedis.hset(scbatinfokey, "upsoftdate", String.valueOf(logtimes));				
	  }
}