package com.stan.wen9000.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.*;
import com.stan.wen9000.action.jedis.util.RedisUtil;


public class ServiceHfcAlarmProcessor {
	private static Logger log = Logger.getLogger(ServiceHfcAlarmProcessor.class);
	private static RedisUtil redisUtil;
	
	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceHfcAlarmProcessor.redisUtil = redisUtil;
	}
	
	public class ParmsTableRow{
		private String _ParamMibLabel;
		private String _ParamMibOid;
		private String _ParamDispText;
		private Boolean _IsFormatEnable;
		private float _FormatCoff;
		private String _FormatText;
		private String _FormatUnit;
		
		public String get_ParamMibLabel() {
			return _ParamMibLabel;
		}

		public String get_ParamMibOid() {
			return _ParamMibOid;
		}

		public String get_ParamDispText() {
			return _ParamDispText;
		}

		public Boolean get_IsFormatEnable() {
			return _IsFormatEnable;
		}

		public float get_FormatCoff() {
			return _FormatCoff;
		}

		public String get_FormatText() {
			return _FormatText;
		}

		public String get_FormatUnit() {
			return _FormatUnit;
		}

		public ParmsTableRow(String _ParamMibLabel, String _ParamMibOid,
				String _ParamDispText, Boolean _IsFormatEnable,
				float _FormatCoff, String _FormatText, String _FormatUnit) {
			super();
			this._ParamMibLabel = _ParamMibLabel;
			this._ParamMibOid = _ParamMibOid;
			this._ParamDispText = _ParamDispText;
			this._IsFormatEnable = _IsFormatEnable;
			this._FormatCoff = _FormatCoff;
			this._FormatText = _FormatText;
			this._FormatUnit = _FormatUnit;
		}

		
	}
	
	private static MibOperations _MibOperObj;

	
	public static MibOperations get_MibOperObj() {
		return _MibOperObj;
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
		
		System.out.println("[#3] ..... service HfcAlarm starting");
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();
		 
		 jedis.psubscribe(jedissubSub, "servicehfcalarm.*");
		redisUtil.getJedisPool().returnResource(jedis);
		  
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}
	
	private void servicestart(String message) throws InterruptedException, ParseException, IOException{
		//System.out.println(" [x] ServiceHeartProcessor Received '" + message
		//		+ "'");			
		dowork(message);					

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
		  
		  Map<String, String> alarm = (Map<String, String>)parser.parse(message, containerFactory);
		  
		  dohfcalarm(alarm);
	}
	
	private void dohfcalarm(Map<String,String> alarm) throws IOException{
		String devmac = alarm.get("mac");
		String logicalid = alarm.get("logicalid");
		String alarminfo = alarm.get("alarminfo");
		String trapstring = "";
		String status = alarm.get("status");
		switch(Integer.valueOf(status)){
			case 0://hfcColdstart
				ParseTrapHfcColdStart(devmac,logicalid);
				break;
			case 1://hfcAlarmevent
				ParseTrapHfcAlarmEvent(devmac,logicalid,alarminfo);
				break;
			case 8686://osSwitchevnet
				ParseTrapHfcOsSwitchEvent(devmac,logicalid,alarminfo);
				break;
			default:
				break;		
		}
	}
	
	public void ParseTrapHfcColdStart(String mac,String logicid){
		String cntrapstring = "";
		String entrapstring = "";
		cntrapstring = "HFC设备冷启动,";
		cntrapstring += "逻辑ID:"+ logicid;
		entrapstring = "HFC Cold Start,";
		entrapstring += "Logical ID:"+ logicid;
		Map<String, String> hash = new LinkedHashMap();
		long alarmtime = System.currentTimeMillis();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
		String alarmtimes = format.format(date);
		hash.put("mac", mac);
		hash.put("code", "200940");
		hash.put("lalarmtime", Long.toString(alarmtime));
		hash.put("salarmtime", alarmtimes);
		hash.put("alarmlevel", "2");
		hash.put("cnalarminfo", cntrapstring);
		hash.put("enalarminfo", entrapstring);
		sendToAlarmQueue(JSONValue.toJSONString(hash));
	}
	
	public void ParseTrapHfcAlarmEvent(String mac,String logicid,String alarminfo) throws IOException{
		String trapstring = "";
		trapstring = "HFC设备冷启动,";
		trapstring += "物理地址:"+ mac;
		trapstring += "逻辑ID:"+ logicid;
		trapstring = "HFC设备参数告警,";
		trapstring += "物理地址:"+ mac;
		trapstring += "逻辑ID:"+ logicid;
		alarminfo.replace(":", "");
		byte[] b_alarminfo = hexStringToBytes(alarminfo);
		if(b_alarminfo.length<6)
			return;
		trapstring += "告警类型:"+ GetAlarmEnumString(b_alarminfo[4]);
		byte[] alarmvb = new byte[alarminfo.length() - 6];
		System.arraycopy(alarminfo, 6, alarmvb, 0, alarmvb.length);
		SnmpOID oid = null;
		int val = 0;
		if(ParseAlarmInform(alarmvb,oid,val)){
			MibNode fnode = this.get_MibOperObj().getNearestNode(oid);
			if(fnode != null){
				//从数据库读取相关信息
				
				int[] nodeoid = fnode.getOID();
				int[] oidarray = oid.toIntArray();
				String exstr = "";
				if(oidarray.length>nodeoid.length){
					for(int i = nodeoid.length;i<oidarray.length;i++){
						exstr += ',' + oidarray[i];
					}
				}
			}
		}
	}
	
	public void ParseTrapHfcOsSwitchEvent(String mac,String logicid,String alarminfo){
		
	}
	
	public Boolean ParseAlarmInform(byte[] data, SnmpOID oid, int val) throws IOException{
		oid = null;
		val = 0;
		if(data.length <2) return false;
		if(data[0] != 0x06) return false;
		int oidindex = 1,oidlen;
		if((data[1] & 0x80) == 0){
			oidlen = data[1] + 1;
		}else{
			int arrayindex =  2 + (data[1]&0x7f);
			oidlen = 0;
			for(int i=2;i<arrayindex;i++){
				oidlen = (oidlen << 8) + data[i];
			}
			oidlen += arrayindex -1;
		}
		byte[] arrayoid = new byte[oidlen];
		System.arraycopy(data, oidindex, arrayoid, 0, oidlen);		
		oid = new SnmpOID(new ASN1Parser(arrayoid).decodeOID());
		oidindex += oidlen;
		if(data[oidindex++] != 0x02) return false;
		byte[] arrayval = new byte[data.length - oidindex];
		System.arraycopy(data, oidindex, arrayval, 0, arrayval.length);
		val = new ASN1Parser(arrayval).decodeInteger();
		return true;
	}
	
	public String GetAlarmEnumString(byte num){
		switch(num){
		case 1:
			return "NORMAL";
		case 2:
			return "HIHI";
		case 3:
			return "HI";
		case 4:
			return "LO";
		case 5:
			return "LOLO";
		case 6:
			return "Discrete Major";
		case 7:
			return "Discrete Minor";
		default:
			return "Unkown Alarm";
		}	
		
	}
	
	
	/** 
	 * Convert hex string to byte[] 
	 * @param hexString the hex string 
	 * @return byte[] 
	 */  
	public byte[] hexStringToBytes(String hexString) {  
	    if (hexString == null || hexString.equals("")) {  
	        return null;  
	    }  
	    hexString = hexString.toUpperCase();  
	    int length = hexString.length() / 2;  
	    char[] hexChars = hexString.toCharArray();  
	    byte[] d = new byte[length];  
	    for (int i = 0; i < length; i++) {  
	        int pos = i * 2;  
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
	    }  
	    return d;  
	}  
	/** 
	 * Convert char to byte 
	 * @param c char 
	 * @return byte 
	 */  
	 private byte charToByte(char c) {  
	    return (byte) "0123456789ABCDEF".indexOf(c);  
	}  
	 
	 private void sendToAlarmQueue(String msg) {
		try {
			Jedis jedis = redisUtil.getConnection();
			jedis.publish("servicealarm.new", msg);
			redisUtil.closeConnection(jedis);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("TrapReceiverBean:sendToQueue error");

		}
	}
}
