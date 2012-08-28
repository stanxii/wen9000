package com.stan.wen9000.web;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import redis.clients.jedis.Jedis;
import com.stan.wen9000.action.jedis.util.RedisUtil;


public class TrapReceiverBean {

	
	
	private static final String Upgrade_QUEUE_NAME = "upgrade_result_queue";
	public static String TRAP_ADDRESS = "udp:0.0.0.0/";	
	private static final String TRAP_SERVER_PORT_KEY = "global:trapserver:port";

	private static Snmp snmp = null;
	private Address listenAddress;
	private static Snmp snmp_send = null;
	
	
	private static Logger logger = Logger.getLogger(TrapReceiverBean.class);
	
	private static RedisUtil redisUtil;

	public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	public static void setRedisUtil(RedisUtil redisUtil) {
		TrapReceiverBean.redisUtil = redisUtil;
	}

	//hfc_client_udp  	    
	private Address targetAddress = null;

	public void start() {
		logger.info("trapreceiver.start() action called, start trap receivering..........");

		targetAddress = GenericAddress.parse("udp:127.0.0.1/2250");		
		doWork();
		
	}

	private void doWork() {

		try {
			//get trap port from db
			Jedis jedis = redisUtil.getConnection();
			String trapport = jedis.get(TRAP_SERVER_PORT_KEY);
			if(trapport == null)trapport="162";
			redisUtil.closeConnection(jedis);
			TRAP_ADDRESS = TRAP_ADDRESS + trapport;
			System.out.println("+++++++++TRAP_ADDRESS=" + TRAP_ADDRESS);
			
			listenAddress = GenericAddress.parse(System.getProperty(
					"snmp4j.listenAddress", TRAP_ADDRESS));
			TransportMapping transport;

			transport = new DefaultUdpTransportMapping(
					(UdpAddress) listenAddress);

			snmp = new Snmp(transport);

			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
			snmp.listen();

		} catch (Exception e) {
			e.printStackTrace();
		}

		CommandResponder pduHandler = new CommandResponder() {
			public synchronized void processPdu(CommandResponderEvent e) {			
				
				//doWork				
				doReceive(e);
			}

		};

		snmp.addCommandResponder(pduHandler);

	}

	

	@SuppressWarnings("unchecked")
	public void doReceive(CommandResponderEvent event) {
		// /process response
		if (event != null && event.getPDU() != null) {
			Vector<VariableBinding> recVBs = (Vector<VariableBinding>) event.getPDU()
					.getVariableBindings();

			// size=9 is cbat alarm
			//logger.info("alarm receive------>>>"+recVBs.toString()+"================size>>>"+recVBs.size());
			if (recVBs.size() == 10) {
				
				//logger.info("------>>>>>>>alarm receive<<<<<<<<<----------");
				Map<String, String> alarmhash=new LinkedHashMap();
			   
				for (int i = 0; i < recVBs.size(); i++) {
					VariableBinding recVB = recVBs.elementAt(i);
					String content = recVB.getVariable().toString();
					 //System.out.println("SNMP4j traper: content=" + content);

					// populate the alarm
					switch (i) {
					case 0:						
						alarmhash.put("runingtime", content);
						break;
					case 1:						
						alarmhash.put("oid", content);
						break;
					case 2:						
						alarmhash.put("alarmcode", content);
						break;
					case 3:						
						alarmhash.put("trapinfo", content);
						break;
					case 4:
						//alarm.setSerialflow(Long.parseLong(content));						
						break;
					case 5:						
						alarmhash.put("cbatmac", content.toLowerCase());
						break;
					case 6:						
						alarmhash.put("cltindex", content);
						break;
					case 7:
						alarmhash.put("cnuindex", content);						
						break;
					case 8:						
						alarmhash.put("alarmtype", content);
						break;
					case 9:
						alarmhash.put("alarmvalue", content);						
						break;
					default:
						System.out.println("not correct");
						break;
					}
				}
				
				
				
				String msgservice = JSONValue.toJSONString(alarmhash);
				
				
				 parseAlarmMsg(alarmhash );
				return;
			}else if(recVBs.size() == 6){
				//heart alarm
				Map hearthash=new LinkedHashMap();
				for (int i = 0; i < recVBs.size(); i++) {
					VariableBinding recVB = recVBs.elementAt(i);
					String content = recVB.getVariable().toString();

					// populate the alarm
					switch (i) {
					case 0:
						
						break;
					case 1:
						
						break;
					case 2:
						hearthash.put("code", content);
						break;
					case 3:
						hearthash.put("info", content);
						break;
					case 4:
						hearthash.put("cbatsys", content);
						break;
					case 5:
						hearthash.put("cnusys", content);
						break;				
					default:
						System.out.println("heart read not correct");
						break;
					}
				}
				//logger.info("heart receive------>>>"+hearthash.get("cbatsys").toString());
				//String msgservice = JSONValue.toJSONString(hearthash);
				parseHeartMsg(hearthash);
			}else if((recVBs.size() == 3)||(recVBs.size() == 4)){
				//hfc alarm
				Jedis jedis=null;
				try {
					jedis = redisUtil.getConnection();
				}catch(Exception e){
					e.printStackTrace();
					redisUtil.getJedisPool().returnBrokenResource(jedis);
					return;
				}
				//W9000显示模式判断
				if((jedis.get("global:displaymode")) != null){
					if(!jedis.get("global:displaymode").equalsIgnoreCase("1")){
						//不显示HFC设备
						redisUtil.getJedisPool().returnResource(jedis);
						return;
					}
				}
				redisUtil.getJedisPool().returnResource(jedis);
				// 设置 target    
		        CommunityTarget target = new CommunityTarget();
		        target.setCommunity(new OctetString("public"));

		        target.setAddress(targetAddress);    
		    
		        // 通信不成功时的重试次数    
		        target.setRetries(2);    
		        // 超时时间    
		        target.setTimeout(500);    
		        // snmp版本    
		        target.setVersion(SnmpConstants.version1);  		        

		     // 向Agent发送PDU，并接收Response    
		        try {
		        	snmp_send = new Snmp(new DefaultUdpTransportMapping());
					ResponseEvent respEvnt = snmp_send.send(event.getPDU(), target);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}    
				
				System.out.println("-------------------------->>>>>>>>alrmlen====="+recVBs.size());
				
			}

		}

	}
	
	@SuppressWarnings("unchecked")
	private void parseHeartMsg(Map<String, String> hearthash){
		//System.out.println("============>>do heart 11111");
		Map<String, String> msgheart = new LinkedHashMap();
		int index1 = 0;
		int index2 = 0;
		int flag = 0;
		String cbatip = "";
		String cbatmac = "";
		String cbattype = "";
		msgheart.put("code", hearthash.get("code"));
		try {
			index1 = ((String) hearthash.get("cbatsys")).indexOf("|");
			cbatmac = ((String) hearthash.get("cbatsys")).substring(1, index1).trim().toLowerCase();
			msgheart.put("cbatmac", cbatmac);
			index2 = ((String) hearthash.get("cbatsys")).indexOf("|", index1 + 1);
			cbatip = ((String) hearthash.get("cbatsys")).substring(index1 + 1, index2);
			msgheart.put("cbatip", cbatip);
			index1 = index2;
			index2 = ((String) hearthash.get("cbatsys")).indexOf("]");
			cbattype = ((String) hearthash.get("cbatsys")).substring(
					index1 + 1, index2);
			msgheart.put("cbattype", cbattype);
			
			index1 = 0;
			index2 = 0;
			String cnumac = "";
			String cnutype = "";
			String cltindex = "";
			String cnuindex = "";
			String active = "";
			int count = 0;
			count = ((String) hearthash.get("cnusys")).length()
					- ((String) hearthash.get("cnusys")).replace("[", "").length();
			//System.out.println("============>>count="+count);
			msgheart.put("cnucount", String.valueOf(count));
			for (int i = 0; i < count; i++) {
				String message = ((String) hearthash.get("cnusys")).substring(flag,
						((String) hearthash.get("cnusys")).indexOf("]", flag + 1));

				try {
					index1 = message.indexOf("|");
					cnumac = message.substring(1, index1).trim().toLowerCase();
					msgheart.put("cnumac"+i, cnumac);
					
					index2 = message.indexOf("|", index1 + 1);
					cnutype = message.substring(index1 + 1, index2);
					msgheart.put("cnutype"+i, cnutype);
					
					index1 = index2;
					index2 = message.indexOf("|", index1 + 1);
					cltindex = message.substring(index1 + 1,index2);
					msgheart.put("cltindex"+i, cltindex);

					index1 = index2;
					index2 = message.indexOf("|", index1 + 1);
					cnuindex = message.substring(index1 + 1,index2);
					msgheart.put("cnuindex"+i, cnuindex);

					index1 = index2;
					active = message.substring(index1 + 1);
					msgheart.put("active"+i, active);

					flag += (index1 + 3);

					//doheartcnu(cbatmac, cnumac, cnutype, cltindex, cnuindex, active);
				} catch (Exception e) {
					System.out.println("parse cnusys error!");
					return;
				}
			}
			doheart(msgheart);
		} catch (Exception e) {
			System.out.println("parse cbatsys error!");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseAlarmMsg(Map alarmhash){		
		try {			 
			 long alarmtime = System.currentTimeMillis();
			 Date date = new Date();
			 DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
			 String alarmtimes = format.format(date);
			 alarmhash.put("lalarmtime", Long.toString(alarmtime));
			 alarmhash.put("salarmtime", alarmtimes);
			 int isOnline = -1;
				String cnumac = "";
				String cnutype = "0";
				int index1 = 0;
				int index2 = 0;
			 
			// /////////////////////case alarm code
			switch (Integer.parseInt((String) alarmhash.get("alarmcode"))) {
			case 200902:
				String trapinfo = (String) alarmhash.get("trapinfo");
				index1 = trapinfo.indexOf("[");
				index2 = trapinfo.indexOf("]");
				cnumac = trapinfo.substring(0, index1).toLowerCase()
						.trim();
				cnutype = trapinfo.substring(
						index1 + 1, index2);
				
				isOnline = Integer.parseInt((String)alarmhash.get("alarmvalue"));
				alarmhash.put("alarmlevel", "7");
				alarmhash.put("cnumac", cnumac);
				alarmhash.put("cnutype", cnutype);
				
				if( 1 == isOnline){
					alarmhash.put("cnalarminfo", "头端:" + (String)alarmhash.get("cbatmac") +"下的 CNU[" + cnumac+"]"+"上线");
					alarmhash.put("enalarminfo", "cbatmac:" + (String)alarmhash.get("cbatmac") +"'s slave[" + cnumac+"]"+" online");
				}else{
					alarmhash.put("cnalarminfo", "头端:" + (String)alarmhash.get("cbatmac") +"下的 CNU[" + cnumac+"]"+"下线");
					alarmhash.put("enalarminfo", "cbatmac:" + (String)alarmhash.get("cbatmac") +"'s slave[" + cnumac+"]"+" offline");
				}
				
				
				break;
			case 200901:	
				isOnline = Integer.parseInt((String)alarmhash.get("alarmvalue"));
				alarmhash.put("alarmlevel", "4");
				if( 1 == isOnline){
					alarmhash.put("cnalarminfo", "头端:" + (String)alarmhash.get("cbatmac") +"发现线卡   " + alarmhash.get("cltindex") );
					alarmhash.put("enalarminfo", "cbatmac:" + (String)alarmhash.get("cbatmac") +"discovery  clt index " + alarmhash.get("cltindex") );
					
				}else{
					alarmhash.put("cnalarminfo", "头端:" + (String)alarmhash.get("cbatmac") +" 丢失线 卡 " + alarmhash.get("cltindex") );
					alarmhash.put("enalarminfo", "cbatmac:" + (String)alarmhash.get("cbatmac") +"loss clt index " + alarmhash.get("cltindex") );
					
				}
					
				
				break;
			case 200909:
				// 事件	upgrade			
				
				String status = (String)alarmhash.get("alarmvalue");
				int istatus = Integer.parseInt(status);
				
				if(istatus >1)
				{				
					alarmhash.put("alarmlevel", "1");
					alarmhash.put("cnalarminfo", "Mac为"+ (String)alarmhash.get("cbatmac") +"的头端升级失败");
					alarmhash.put("enalarminfo", "Mac:"+ (String)alarmhash.get("cbatmac") +" Upgrade Failed!");
					
				}
				else if(istatus==1)
				{
					alarmhash.put("alarmlevel", "7");
					alarmhash.put("cnalarminfo", "Mac为"+ (String)alarmhash.get("cbatmac") +"的头端升级告警信息");
					alarmhash.put("enalarminfo", "Mac:"+ (String)alarmhash.get("cbatmac") +" Upgrade  Alarm information!");
									
				}
				else if(istatus == 0)
				{
					alarmhash.put("alarmlevel", "6");
					alarmhash.put("cnalarminfo", "Mac为"+ (String)alarmhash.get("cbatmac") +"的头端升级成功");
					alarmhash.put("enalarminfo", "Mac:"+ (String)alarmhash.get("cbatmac") +" Upgrade Successful!");
									
				}
				
				break;
			case 200903:
				alarmhash.put("alarmlevel", "1");
				alarmhash.put("cnalarminfo", "环境温度告警");
				alarmhash.put("enalarminfo", "Environment temperature alarm");				
				break;
			case 200904:
				alarmhash.put("alarmlevel", "1");
				alarmhash.put("cnalarminfo", "CBAT管理CPU负载过高告警以及恢复");
				alarmhash.put("enalarminfo", "CBAT Management CPU load warning and high recovery");				
				break;
			case 200905:
				alarmhash.put("alarmlevel", "1");
				alarmhash.put("cnalarminfo", "CBAT内存利用率过高告警");
				alarmhash.put("enalarminfo", "CBAT memory utilization warning too high");				
				break;
			case 200910:
			case 200911:
				alarmhash.put("alarmlevel", "1");
				break;
			case 200906:
				alarmhash.put("alarmlevel", "2");
				alarmhash.put("cnalarminfo", "噪声过高告警");
				alarmhash.put("enalarminfo", "High noise alarm");
				
				break;
			case 200907:
				alarmhash.put("alarmlevel", "2");
				alarmhash.put("cnalarminfo", "链路层速率告警");
				alarmhash.put("enalarminfo", "The link layer rate alarm");				
				break;
			case 200908:
				alarmhash.put("alarmlevel", "2");
				alarmhash.put("cnalarminfo", "物理层速率告警");
				alarmhash.put("enalarminfo", "The physical layer rate alarm");
				
				break;
			case 200913:
				// 警告
				alarmhash.put("alarmlevel", "2");
				alarmhash.put("cnalarminfo", "用户数量超限");
				alarmhash.put("enalarminfo", "The number of users, overrun");				
				break;
			case 200914:
				if(Integer.parseInt((String)alarmhash.get("alarmvalue"))==1)
				{
					alarmhash.put("alarmlevel", "6");
					alarmhash.put("cnalarminfo", "阻止 CNU注册 成功");
					alarmhash.put("enalarminfo", alarmhash.get("trapinfo"));					
				}
				else
				{
					alarmhash.put("alarmlevel", "3");
					alarmhash.put("cnalarminfo", "阻止 CNU注册 失败");
					alarmhash.put("enalarminfo", alarmhash.get("trapinfo"));					
				}
				break;
			case 200915:
			case 200916:
				if(Integer.parseInt((String)alarmhash.get("alarmvalue"))==1){
					alarmhash.put("alarmlevel", "5");
					alarmhash.put("cnalarminfo", "头端["+alarmhash.get("cbatmac")+"]下发MOD["+alarmhash.get("cnuindex")+"]成功");
					alarmhash.put("enalarminfo", alarmhash.get("trapinfo"));
				}else{
					alarmhash.put("alarmlevel", "2");
					alarmhash.put("cnalarminfo", "头端["+alarmhash.get("cbatmac")+"]下发MOD["+alarmhash.get("cnuindex")+"]失败");
					alarmhash.put("enalarminfo", alarmhash.get("trapinfo"));
				}
				
				break;
			case 200918:
				if( Integer.parseInt((String)alarmhash.get("alarmvalue"))==1)
				{
					alarmhash.put("alarmlevel", "6");
					alarmhash.put("cnalarminfo", "KICK OFF CNU 成功");
					alarmhash.put("enalarminfo", alarmhash.get("trapinfo"));					
				}
				else
				{
					alarmhash.put("alarmlevel", "3");
					alarmhash.put("cnalarminfo", "KICK OFF CNU 失败");
					alarmhash.put("enalarminfo", alarmhash.get("trapinfo"));					
				}
				break;
			case 200919:
				alarmhash.put("alarmlevel", "6");
				alarmhash.put("cnalarminfo", "CNU强制重新注册");
				alarmhash.put("enalarminfo", alarmhash.get("trapinfo"));				
				break;
			case 200920:			
				// 告警				
				alarmhash.put("alarmlevel", "6");
				alarmhash.put("cnalarminfo", "Mac为"+ (String)alarmhash.get("cbatmac") +"的头端上线");
				alarmhash.put("enalarminfo", "Mac:"+ (String)alarmhash.get("cbatmac") +"  Master online!");
				break;
			case 200921:				
				alarmhash.put("alarmlevel", "2");
				alarmhash.put("cnalarminfo", "Mac为"+ (String)alarmhash.get("cbatmac") +"的头端下线");
				alarmhash.put("enalarminfo", "Mac:"+ (String)alarmhash.get("cbatmac") +"  Master offline!");
				break;
			case 200912:				
				alarmhash.put("alarmlevel", "5");
				alarmhash.put("cnalarminfo", "非法用户试图注册");
				alarmhash.put("enalarminfo", "llegal users trying to register");				
				break;
			case 200922:
				alarmhash.put("alarmlevel", "5");
				alarmhash.put("cnalarminfo", "线卡丢失");
				alarmhash.put("enalarminfo", "Lose CLT");
			default:
				alarmhash.put("alarmlevel", "7");
				alarmhash.put("cnalarminfo", "未知告警");
				alarmhash.put("enalarminfo", "Unkonwn");
			}


		} catch (Exception e) {
			//System.out.println("trap save db error alarm save error");
			e.printStackTrace();
		}
		
		
		//add by stan alarm filter
		alarmFilter(alarmhash);
		
		//logger.info("------>>>>>>>alarm send<<<<<<<<<----------");
		doAlarm(alarmhash);
		
		
	}
	private boolean alarmFilter(Map alarm) {
		 
		 boolean result = false;
		 
		 
		 return result;
		 

	}

	private void doAlarm(Map alarm) {
		 String msgservice = JSONValue.toJSONString(alarm);
		//int i = 0;
		//while(i<150000){
		//	i++;
			sendToAlarmQueue(msgservice);
		//}
 
	}

	private void doheart(Map heart) {
		// TODO Auto-generated method stub
		String msgservice = JSONValue.toJSONString(heart);
		sendToHeartQueue(msgservice);
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
	
	private void sendToHeartQueue(String msg) {
		//System.out.println("============>>do heart 222222   msg="+msg);
		try {			
			Jedis jedis = redisUtil.getConnection();
			jedis.publish("servicehearbert.new",  msg);
			redisUtil.closeConnection(jedis);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("TrapReceiverBean:sendToQueue error");

		}
	}


}
