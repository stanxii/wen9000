package com.stan.wen9000.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.stan.wen9000.action.jedis.util.RedisUtil;

public class ServiceSendconfig {

	
	private static Logger log = Logger.getLogger(ServiceSendconfig.class);

	private static SnmpUtil util = new SnmpUtil();

	
	private static RedisUtil redisUtil;

	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceSendconfig.redisUtil = redisUtil;
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

      	System.out.println("[x]ServiceSendconfig  Subscribing....pmessage....now receive on msgarge1 [" + arg1 + "] arg2=["+msg +"]");
      	try {
  			//arg2 is mssage now is currenti p
  			
  			
  			
  			servicestart(arg1, msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();			
  		}
  		
      }

  };
  
  public void start() {

		log.info("[#3] ..... service sendconfig starting");

		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		 jedis.psubscribe(jedissubSub, "servicesendconfig.*");
		redisUtil.getJedisPool().returnResource(jedis);
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}

	
	public static void servicestart(String pat, String message) throws Exception {

			System.out.println(" [x] Service sendconfig Received '" + message
					+ "'");		
			dowork(pat, message);					  
			
		
	}
	
	public static void dowork(String pat, String message) throws ParseException, IOException {
		if(pat.equalsIgnoreCase("servicesendconfig.preconfig")){
			doPreConfig(message);
		}else if(pat.equalsIgnoreCase("servicesendconfig.config")){
			//doNodeCbatdetail(message);
		}
	}
	
	private static void doPreConfig(String message) throws ParseException{
		Boolean iserror = false;
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return;
		}
		
		JSONObject jsondata = (JSONObject)new JSONParser().parse(message);
		String cbatip = jsondata.get("cbatip").toString();
		String devcnuid = jsondata.get("devcnuid").toString();
		String proid = jsondata.get("proid").toString();
		String cbatmac = jsondata.get("cbatmac").toString();
		String cnumac = jsondata.get("cnumac").toString();
		//判断设备是否在线
		try {
			String tmp = util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
			if(tmp == ""){
				iserror = true;
			}else{
				//发送配置
				if(!sendconfig(Integer.valueOf(proid),cbatip,Integer.valueOf(devcnuid),jedis)){
					//发送失败
					iserror = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			iserror = true;
		}
		//预开户告警信息
		Map<String, String> alarmhash=new LinkedHashMap();
		alarmhash.put("runingtime", "N/A");
		alarmhash.put("oid", "N/A");
		alarmhash.put("alarmcode", "200932");		
		alarmhash.put("cbatmac", "N/A"); 		
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
		String alarmtimes = format.format(date);
		alarmhash.put("salarmtime", alarmtimes);
		
		if(iserror){
			alarmhash.put("alarmlevel", "7");
			alarmhash.put("trapinfo", "头端标识为"+jedis.hget("cbatid:"+jedis.get("mac:"+cbatmac+":deviceid")+":entity", "label")+"下的CNU["+cnumac+"]预开户失败");
			alarmhash.put("enalarminfo", "CNU["+cnumac+"] Under Cbat["+cbatmac+ "] PreConfig!" );
		}else{
			alarmhash.put("alarmlevel", "5");
			alarmhash.put("trapinfo", "头端标识为"+jedis.hget("cbatid:"+jedis.get("mac:"+cbatmac+":deviceid")+":entity", "label")+"下的CNU["+cnumac+"]预开户成功");
			alarmhash.put("enalarminfo", "CNU["+cnumac+"] Under Cbat["+cbatmac+ "] PreConfig!" );
		}
		
		String msgservice = JSONValue.toJSONString(alarmhash);
		jedis.publish("servicealarm.new", msgservice);
		
		redisUtil.getJedisPool().returnResource(jedis);
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