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
  	
  	
	@SuppressWarnings("static-access")
	public void start(){
		
		System.out.println("[#3] ..... service HfcAlarm starting");
		Jedis jedis=null;
		try {
			//加载MIB
			  String nowpath;             //当前tomcat的bin目录的路径 
		      String tempdir;  
		      nowpath=System.getProperty("user.dir");  
		      tempdir=nowpath.replace("bin", "webapps");  //把bin 文件夹变到 webapps文件里面   
		      tempdir=nowpath.replace("\\wen9000", "");
		      tempdir+="\\"+"wen9000"+"\\"+"mibs";    
			  log.info("--------------Path--->>>"+tempdir+"------->>>>"+System.getProperty("user.dir"));
			  _MibOperObj = new MibOperations();
			  this._MibOperObj.loadMibModules(tempdir+"/NSCRTV-FOBETMOC-WOS2000-MIB");
			  this._MibOperObj.loadMibModules(tempdir+"/NSCRTV-HFCEMS-ALARMS-MIB");
			  this._MibOperObj.loadMibModules(tempdir+"/NSCRTV-HFCEMS-COMMON-MIB");
			  this._MibOperObj.loadMibModules(tempdir+"/NSCRTV-HFCEMS-PROPERTY-MIB");
			  this._MibOperObj.loadMibModules(tempdir+"/NSCRTV-HFCEMS-OPTICALSWITCH-MIB");
			  this._MibOperObj.loadMibModules(tempdir+"/NSCRTV-HFCEMS-OPTICALAMPLIFIER-MIB");
			  this._MibOperObj.loadMibModules(tempdir+"/NSCRTV-HFCEMS-OPTICALTRANSMITTERDIRECTLY-MIB");
			  this._MibOperObj.loadMibModules(tempdir+"/NSCRTV-HFCEMS-DOWNSTREAMOPTICALRECEIVER-MIB");
			  this._MibOperObj.loadMibModules(tempdir+"/NSCRTV-HFCEMS-FIBERNODE-MIB");
			  jedis = redisUtil.getConnection();		 
			  jedis.psubscribe(jedissubSub, "servicehfcalarm.*");
			  log.info("------->>>>>3333");
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
		String traptype = alarm.get("traptype");
		String enterprise = alarm.get("enterprise");
		//log.info("------------->>>---traptype---"+traptype+"----enterprise---"+enterprise);
		try{
			if(Integer.valueOf(traptype)!= 6){
				ProcessGenericTraps(Integer.valueOf(traptype),devmac);
			}else if(enterprise.equalsIgnoreCase("1.3.6.1.4.1.17409.1")){
				ProcessHFCTraps(alarm);
			}else if(enterprise.equalsIgnoreCase("1.3.6.1.4.1.17409.8888.1")){
				ProcessWosTraps(alarm);
			}else if(enterprise.equalsIgnoreCase("1.3.6.1.4.1.2000.1.3000")){
				ProcessWos3kTraps(alarm);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	private void ProcessWosTraps(Map<String,String> alarm)
    {
		String status = alarm.get("status");
        switch (Integer.valueOf(status))
        {
            case 1://wosTrapRestart
                ParseTrapWosTrapRestart(alarm);
                return;
            case 2://wosTrapDeviceUp
                ParseTrapWosTrapDeviceUp(alarm);
                return;
            case 3://wosTrapDeviceDown
                ParseTrapWosTrapDeviceDown(alarm);
                return;
        }
    }
    private void ProcessWos3kTraps(Map<String,String> alarm)
    {
    	String status = alarm.get("status");
        switch (Integer.valueOf(status))
        {
            case 1://wosTrapRestart
                ParseTrapWosTrapRestart(alarm);
                return;
            case 2://wosTrapDeviceUp
                ParseTrapWosTrapDeviceUp(alarm);
                return;
            case 3://wosTrapDeviceDown
                ParseTrapWosTrapDeviceDown(alarm);
                return;
            case 4:                             ///////////////  //修改
                //ParseTrapWos3kAlarmEvent(alarm);
               return;
        }
    }
	
	 public void ProcessHFCTraps(Map<String,String> alarm) throws IOException
     {
		 String logicalid = alarm.get("logicalid");
		 String alarminfo = alarm.get("alarminfo");
		 String status = alarm.get("status");
		 String devmac = alarm.get("mac");
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
	
	public void ProcessGenericTraps(int traptype, String mac)
    {
        String cntrapstring = "";
        String entrapstring = "";
        Map<String, String> hash = new LinkedHashMap();
        switch (traptype)
        { 
            case 0:
            	cntrapstring = "标准冷启动";
            	entrapstring = "Standard ColdStart";
            	hash.put("alarmlevel", "2");
                break;
            case 1:
            	cntrapstring = "标准热启动";
            	entrapstring = "Standard WarmStart";
            	hash.put("alarmlevel", "3");
                break;
            case 2:
            	cntrapstring = "标准连接断开";
            	entrapstring = "Standard UnLink";
            	hash.put("alarmlevel", "2");
                break;
            case 3:
            	cntrapstring = "标准连接成功";
            	entrapstring = "Standard Connect";
            	hash.put("alarmlevel", "6");
                break;
            /*
            case 4:
                trapstring = "标准签名错误";
                break;
             */
            case 5:
            	cntrapstring = "标准目标丢失";
            	entrapstring = "Standard Lose";
            	hash.put("alarmlevel", "3");
                break;
            default:
                return;
        }
        
		long alarmtime = System.currentTimeMillis();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
		String alarmtimes = format.format(date);
		hash.put("cbatmac", mac);
		hash.put("alarmcode", "200940");
		hash.put("lalarmtime", Long.toString(alarmtime));
		hash.put("salarmtime", alarmtimes);		
		hash.put("cnalarminfo", cntrapstring);
		hash.put("enalarminfo", entrapstring);
		sendToAlarmQueue(JSONValue.toJSONString(hash));

    }
	
	public void ParseTrapWosTrapRestart(Map<String,String> alarm)
    {
        String cntrapstring = "WOS光平台重启动，";
        String entrapstring = "WOS PlatForm Restart，";
        String devmac = alarm.get("mac");
        String logicalid = alarm.get("logicalid");
        cntrapstring += ",物理地址：" + devmac;
        cntrapstring += " ,软件版本：" + Float.valueOf(logicalid) / 100.0f;
        entrapstring += ",MAC：" + devmac;
        entrapstring += " ,Version：" + Float.valueOf(logicalid) / 100.0f;
        Map<String, String> hash = new LinkedHashMap();
        long alarmtime = System.currentTimeMillis();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
		String alarmtimes = format.format(date);
		hash.put("cbatmac", devmac);
		hash.put("alarmcode", "200940");
		hash.put("alarmlevel", "2");
		hash.put("lalarmtime", Long.toString(alarmtime));
		hash.put("salarmtime", alarmtimes);		
		hash.put("cnalarminfo", cntrapstring);
		hash.put("enalarminfo", entrapstring);
		sendToAlarmQueue(JSONValue.toJSONString(hash));
    }
	
	public void ParseTrapWosTrapDeviceUp(Map<String,String> alarm)
    {
//        String trapstring = "检测到设备上线，" + dev.FullPath + "的子设备";
//
//        int[] oidarray = pdu.GetVariableBinding(0).ObjectID.ToIntArray();
//        int slotnum = oidarray[oidarray.Length - 1];
//        int subdevtype = oidarray[oidarray.Length - 2];
//
//        //trapstring += '第' + slotnum.ToString() + "号插槽的" + GetWosSubDevName(subdevtype);
//        lock (CAppKernel.ViewTrapLog)
//        {
//            CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.WosTrapDeviceUp, pdu.Address.ToString(), trapstring,
//                DateTime.Now);
//        }
    }

    public void ParseTrapWosTrapDeviceDown(Map<String,String> alarm)
    {
//        String trapstring = "检测到设备下线，" + dev.FullPath + "的子设备";
//
//        int[] oidarray = pdu.GetVariableBinding(0).ObjectID.ToIntArray();
//        int slotnum = oidarray[oidarray.Length - 1];
//        int subdevtype = oidarray[oidarray.Length - 2];
//
//       // trapstring += '第' + slotnum.ToString() + "号插槽的" + GetWosSubDevName(subdevtype);
//        lock (CAppKernel.ViewTrapLog)
//        {
//            CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.WosTrapDeviceDown, pdu.Address.ToString(), trapstring,
//                DateTime.Now);
//        }
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
		hash.put("cbatmac", mac);
		hash.put("alarmcode", "200940");
		hash.put("lalarmtime", Long.toString(alarmtime));
		hash.put("salarmtime", alarmtimes);
		hash.put("alarmlevel", "2");
		hash.put("cnalarminfo", cntrapstring);
		hash.put("enalarminfo", entrapstring);
		sendToAlarmQueue(JSONValue.toJSONString(hash));
	}
	
	public void ParseTrapHfcAlarmEvent(String mac,String logicid,String alarminfo) throws IOException{
		Jedis jedis=null;
		try {
			jedis = redisUtil.getConnection();		
		}catch(Exception e){
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			log.info("------>>>>>>>save hfcalarm ex1<<<<<<<<<----------");
		}
		String cntrapstring = "";
		String entrapstring = "";
		cntrapstring = "HFC设备参数告警,";
		entrapstring = "HFC Parameters Alarm,";
		cntrapstring += ",逻辑ID:"+ logicid;
		entrapstring += ",Logic ID:"+ logicid;
		alarminfo = alarminfo.replace(":", "");
		byte[] b_alarminfo = hexStringToBytes(alarminfo);
		if(b_alarminfo.length<6)
			return;
		//log.info("===b_alarminfo=="+b_alarminfo[0]+":"+b_alarminfo[1]+":"+b_alarminfo[2]+":"+b_alarminfo[3]+":"+b_alarminfo[4]+":"+b_alarminfo[5]+":"+b_alarminfo[6]);
		cntrapstring += ",告警类型:"+ GetAlarmEnumString(b_alarminfo[4]);
		entrapstring += ",Alarm Type:"+ GetAlarmEnumString(b_alarminfo[4]);
		byte[] alarmvb = new byte[b_alarminfo.length - 6];
		System.arraycopy(b_alarminfo, 6, alarmvb, 0, alarmvb.length);
		SnmpOID oid = null;
		int val = 0;
		if((oid=ParseAlarmInform(alarmvb,oid,val))!=null){			
			MibNode fnode = this.get_MibOperObj().getNearestNode(oid);
			if(fnode != null){							
				int[] nodeoid = fnode.getOID();
				int[] oidarray = oid.toIntArray();
				String exstr = "";
				if(oidarray.length>nodeoid.length){
					for(int i = nodeoid.length;i<oidarray.length;i++){
						exstr += "," + oidarray[i];
					}
				}
				//从数据库读取相关信息	
				Map<String, String> ptr = jedis.hgetAll(fnode.getLabel());
				if(ptr.get("ParamDispText") != null){
					cntrapstring += ",参数名称:"+ ptr.get("ParamDispText") + exstr;
					entrapstring += ",Param Name:"+ ptr.get("ParamDispText") + exstr;
					log.info("------>>>>>>>3<<<<<<<<<----------"+ptr.get("IsFormatEnable"));
					if(ptr.get("IsFormatEnable") == "true"){
						if(ptr.get("ParamMibLable") == "fnReverseOpticalPower"){
							float tmpf = val * Integer.valueOf(ptr.get("FormatCoff"));
							cntrapstring += ",参数值:"+tmpf + ptr.get("FormatText");
							entrapstring += ",Param Val:"+tmpf + ptr.get("FormatText");
						}else{
							float tmpf = val * Integer.valueOf(ptr.get("FormatCoff"));
							cntrapstring += ",参数值:"+tmpf + ptr.get("FormatText") + ptr.get("FormatUnit");
							entrapstring += ",Param Val:"+tmpf + ptr.get("FormatText") + ptr.get("FormatUnit");
						}
					}
				}else{
					cntrapstring += ",参数名称:"+ fnode.getLabel() + exstr;
					entrapstring += ",Param Name:"+ fnode.getLabel() + exstr;
					cntrapstring += ",参数值:"+ val;
					entrapstring += ",Param Value:"+ val;
				}
			}else{
				cntrapstring += ",参数名称:"+ oid;
				entrapstring += ",Param Name:"+ oid;
				cntrapstring += ",参数值:"+ val;
				entrapstring += ",Param Value:"+ val;
			}
			Map<String, String> hash = new LinkedHashMap();
			long alarmtime = System.currentTimeMillis();
			Date date = new Date();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
			String alarmtimes = format.format(date);
			hash.put("cbatmac", mac);
			hash.put("alarmcode", "200940");
			hash.put("lalarmtime", Long.toString(alarmtime));
			hash.put("salarmtime", alarmtimes);
			switch(b_alarminfo[4]){
			case 1:
				hash.put("alarmlevel", "7");
				break;
			case 2:
				hash.put("alarmlevel", "1");
				break;
			case 3:
				hash.put("alarmlevel", "2");
				break;
			case 4:
				hash.put("alarmlevel", "2");
				break;
			case 5:
				hash.put("alarmlevel", "1");
				break;
			case 6:
				//return "Discrete Major";
				hash.put("alarmlevel", "2");
				break;
			case 7:
				//return "Discrete Minor";
				hash.put("alarmlevel", "2");
				break;
			default:
				//return "Unkown Alarm";
				hash.put("alarmlevel", "3");
				break;
			}	
			hash.put("cnalarminfo", cntrapstring);
			hash.put("enalarminfo", entrapstring);
			sendToAlarmQueue(JSONValue.toJSONString(hash));
		}
		
	}
	
	public void ParseTrapHfcOsSwitchEvent(String mac,String logicid,String alarminfo){
		
	}
	
	public SnmpOID ParseAlarmInform(byte[] data, SnmpOID oid, int val) throws IOException{
		oid = null;
		val = 0;
		if(data.length <2) return null;
		if(data[0] != 0x06) return null;
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
		
		if(data[oidindex++] != 0x02) return null;
		byte[] arrayval = new byte[data.length - oidindex];
		System.arraycopy(data, oidindex, arrayval, 0, arrayval.length);
		val = new ASN1Parser(arrayval).decodeInteger();
		return oid;
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
		
		hexString = hexString.toLowerCase();
		final byte[] byteArray = new byte[hexString.length() / 2];
		int k = 0;
		for (int i = 0; i < byteArray.length; i++) {
                        //因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
			byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
			byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
			byteArray[i] = (byte) (high << 4 | low);
			k += 2;
		}
		return byteArray;
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
