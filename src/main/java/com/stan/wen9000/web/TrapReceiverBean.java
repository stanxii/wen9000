package com.stan.wen9000.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.stan.wen9000.domain.Alarm;


public class TrapReceiverBean {

	private static final String HEART_QUEUE_NAME = "heart_queue";
	private static final String ALARM_QUEUE_NAME = "alarm_queue";
	private static final String Upgrade_QUEUE_NAME = "upgrade_result_queue";

	public static String TRAP_ADDRESS = "udp:0.0.0.0/";

	private static Snmp snmp = null;
	private Address listenAddress;
	
	
	private ConnectionFactory factoryAlarm = null;
	private Connection connectionAlarm = null;
	private Channel channelAlarm = null;
	
//	private ConnectionFactory factoryHeart = null;
//	private Connection connectionHeart = null;
//	private Channel channelHeart = null;
	
	public void init(){
		//init alarm rabbitmq
		//This example in Java creates a queue which expires after it has been unused for 30 minutes.
		// Map<String, Object> args = new HashMap<String, Object>();
		//args.put("x-expires", 1800000);
		try {
			factoryAlarm = new ConnectionFactory();
			factoryAlarm.setHost("localhost");
			connectionAlarm = factoryAlarm.newConnection();			
			//create channel			
			channelAlarm = connectionAlarm.createChannel();

			channelAlarm.queueDeclare(ALARM_QUEUE_NAME, true, false, false, null);
		} catch (Exception e1) {
			// TODO Auto-generated catch block										
			e1.printStackTrace();			
		}
		
		//init heart rabbitmq
		
//		try {
//			factoryHeart = new ConnectionFactory();
//			factoryHeart.setHost("localhost");
//			connectionHeart = factoryHeart.newConnection();			
//			//create channel			
//			channelHeart = connectionHeart.createChannel();
//
//			channelHeart.queueDeclare(HEART_QUEUE_NAME, true, false, false, null);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block										
//			e1.printStackTrace();			
//		}
		
		
	}
	
	public void desTroy(){
		//close rabbimq
		try {	
			channelAlarm.close();
			connectionAlarm.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block										
			e1.printStackTrace();
			
		}		
//		try {	
//			channelHeart.close();
//			connectionHeart.close();
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block										
//			e1.printStackTrace();
//			
//		}	
	}

	public void start() {
//		// implement your business logic here
//		//log.info("trapreceiver.start() action called, start trap receivering..........");
//		try {
//			factoryAlarm = new ConnectionFactory();
//			factoryAlarm.setHost("localhost");
//			connectionAlarm = factoryAlarm.newConnection();			
//			//create channel			
//			channelAlarm = connectionAlarm.createChannel();
//
//			channelAlarm.queueDeclare(ALARM_QUEUE_NAME, true, false, false, null);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block										
//			e1.printStackTrace();			
//		}
//		
//		//init heart rabbitmq
//		
////		try {
////			factoryHeart = new ConnectionFactory();
////			factoryHeart.setHost("localhost");
////			connectionHeart = factoryHeart.newConnection();			
////			//create channel			
////			channelHeart = connectionHeart.createChannel();
////
////			channelHeart.queueDeclare(HEART_QUEUE_NAME, true, false, false, null);
////		} catch (Exception e1) {
////			// TODO Auto-generated catch block										
////			e1.printStackTrace();			
////		}
//		doWork();
//		
//		try {	
//			channelAlarm.close();
//			connectionAlarm.close();
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block										
//			e1.printStackTrace();
//			
//		}		
////		try {	
////			channelHeart.close();
////			connectionHeart.close();
////		} catch (Exception e1) {
////			// TODO Auto-generated catch block										
////			e1.printStackTrace();
////			
////		}	

	}

	private void doWork() {

		try {
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

	

	public void doReceive(CommandResponderEvent event) {

		// /process response
		if (event != null && event.getPDU() != null) {
			Vector<VariableBinding> recVBs = event.getPDU()
					.getVariableBindings();

			// size=9 is cbat alarm

			if (recVBs.size() == 10) {
				Alarm alarm = new Alarm();

				for (int i = 0; i < recVBs.size(); i++) {
					VariableBinding recVB = recVBs.elementAt(i);
					String content = recVB.getVariable().toString();
					// System.out.println("SNMP4j traper: content=" + content);

					// populate the alarm
					switch (i) {
					case 0:
						alarm.setTimeticks(content);
						break;
					case 1:
						alarm.setOid(content);
						break;
					case 2:
						alarm.setAlarmcode(Integer.parseInt(content));
						break;
					case 3:
						alarm.setTrapinfo(content);
						break;
					case 4:
						//alarm.setSerialflow(Long.parseLong(content));
						break;
					case 5:
						alarm.setCbatmac(content.toUpperCase());
						break;
					case 6:
						alarm.setCltindex(Integer.parseInt(content));
						break;
					case 7:
						alarm.setCnuindex(Integer.parseInt(content));
						break;
					case 8:
						alarm.setAlarmtype(Integer.parseInt(content));
						break;
					case 9:
						alarm.setAlarmvalue(Integer.parseInt(content));
						break;
					default:
						System.out.println("not correct");
						break;
					}
				}

				try {
					//Calendar c = Calendar.getInstance();
					//Date now = c.getTime();
					Date now = new Date();
					alarm.setRealtime(now);

					// /////////////////////case alarm code
					switch (alarm.getAlarmcode()) {
					case 200000:
					case 200001:
					case 200902:
					case 200903:
					case 200904:
					case 200905:
					case 200906:
					case 200907:
					case 200908:	
					case 200909:
					case 200910:
					case 200911:
					case 200920:
					case 200921:
						//doCbatUpstart(alarm);
						doAlarm(alarm);
						break;					
					default:
						;
					}

				} catch (Exception e) {
					//System.out.println("trap save db error alarm save error");
					e.printStackTrace();
				}
				return;
			}
			
			if(recVBs.size() == 6){
//				HeartBean heart = new HeartBean(); 
//				for (int i = 0; i < recVBs.size(); i++) {
//					VariableBinding recVB = recVBs.elementAt(i);
//					String content = recVB.getVariable().toString();
//
//					// populate the alarm
//					switch (i) {
//					case 0:
//						
//						break;
//					case 1:
//						
//						break;
//					case 2:
//						heart.setCode(Integer.parseInt(content));
//						break;
//					case 3:
//						heart.setInfo(content);
//						break;
//					case 4:
//						heart.setCbatsys(content);
//						break;
//					case 5:
//						heart.setCnusys(content);
//						break;				
//					default:
//						System.out.println("heart read not correct");
//						break;
//					}
//				}
//				doheart(heart);
			}

		}

	}
	
	private void doAlarm(Alarm alarm) {
		
		//System.out.println("================================>>>>>code"+ alarm.getAlarmcode());
		
		// TODO Auto-generated method stub
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String msg = alarm.getAlarmcode() + "|" + alarm.getAlarmtype() + "|"
				+ alarm.getCltindex() + "|" + alarm.getCnuindex() + "|"
				+ alarm.getItemnumber() + "|" + alarm.getCbatmac() + "|"
				+ alarm.getAlarmvalue() + "|" + alarm.getTrapinfo()+ "|" + format1.format(alarm.getRealtime())
				+ "|" + alarm.getTimeticks();
		//System.out.println("===================>>>>>send msg alarm");
		sendToAlarmQueue(msg);
	}

//	private void doheart(HeartBean heart) {
//		// TODO Auto-generated method stub
//		String msg = heart.getCode() + ";" + heart.getCbatsys() + ";"
//				+ heart.getCnusys() + ";" + heart.getInfo();
//		//System.out.println("===================>>>>>send msg alarm");
//		sendToAlarmQueue(msg);
//	}
	
	private void doCnuStatus(Alarm alarm) {
		// TODO Auto-generated method stub
		String msg = alarm.getAlarmcode() + "|" + alarm.getAlarmvalue() + "|"
				+ alarm.getCltindex() + "|" + alarm.getCnuindex() + "|"
				+ alarm.getCnumac() + "|" + alarm.getTrapinfo();
		sendToHeartQueue(msg);
	}

	private void doCbatOnline(Alarm alarm) {
		// TODO Auto-generated method stub
		String msg = alarm.getAlarmcode() + "|" + alarm.getAlarmvalue() + "|"
				+ alarm.getCltindex() + "|" + alarm.getCnuindex() + "|"
				+ alarm.getCbatmac() + "|" + alarm.getTrapinfo();
		sendToHeartQueue(msg);
	}

	private void doCbatUpstart(Alarm alarm) {
		// TODO Auto-generated method stub
		// discovery new Cbat
		String msg = alarm.getAlarmcode() + "|" + alarm.getAlarmvalue() + "|"
				+ alarm.getCltindex() + "|" + alarm.getCnuindex() + "|"
				+ alarm.getCbatmac() + "|" + alarm.getTrapinfo();
		sendToHeartQueue(msg);

	}

	private void sendToAlarmQueue(String msg) {
		try {
			
			channelAlarm.basicPublish("", ALARM_QUEUE_NAME,
					MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
			// System.out.println(" [x0] Alarm Sent '" + msg +
			//"'");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("TrapReceiverBean:sendToQueue error");

		}
	}
	
	private void sendToHeartQueue(String msg) {
		try {
			
//			channelHeart.basicPublish("", HEART_QUEUE_NAME,
//					MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
			// System.out.println(" [x0] Alarm_Heart Sent '" + msg +
			// "'");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("TrapReceiverBean:sendToQueue error");

		}
	}


}
