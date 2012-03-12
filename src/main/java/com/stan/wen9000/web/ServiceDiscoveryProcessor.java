package com.stan.wen9000.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.stan.wen9000.domain.Cbat;
import com.stan.wen9000.domain.Cbatinfo;
import com.stan.wen9000.reference.EocDeviceType;
import com.stan.wen9000.service.CbatService;
import com.stan.wen9000.service.CbatinfoService;


public class ServiceDiscoveryProcessor{

	@Autowired
	CbatService cbatsv;
	
	@Autowired
	CbatinfoService cbatinfosv;
	
	EocDeviceType devicetype;
	
	private static final String PERSIST_CBAT_QUEUE_NAME = "service_discovery_queue";
	private static SnmpUtil util = new SnmpUtil();

	private long count =0;
	public void execute(){

		System.out.println("[#3] ..... service discovery starting");

		servicestart();

	}

	public void servicestart() {

		QueueingConsumer consumer = null;
		Channel channel = null;
		Connection connection = null;

		try {

			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			connection = factory.newConnection();
			channel = connection.createChannel();

			channel.queueDeclare(PERSIST_CBAT_QUEUE_NAME, true, false, false,
					null);
			System.out
					.println(" [*] Waiting for messages. To exit press CTRL+C");

			channel.basicQos(1);

			consumer = new QueueingConsumer(channel);
			channel.basicConsume(PERSIST_CBAT_QUEUE_NAME, false, consumer);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				channel.close();
				connection.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
			}

		}

		while (true) {

			String message = null;
			QueueingConsumer.Delivery delivery;
			try {

				delivery = consumer.nextDelivery();

				message = new String(delivery.getBody());

			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}

			// System.out.println(" [x]ServiceDiscoveryProcessor  Received '" +
			// message + "'");
			count++;
			doWork(message);
			if(count%1000==0){
				DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date now = new Date();
				try {
					System.out.println(format1.parse(now.toLocaleString()));
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("have done:["+count+"]");
			}
			// System.out.println(" [x] ServiceDiscoveryProcessor  Done");

			try {
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}

	}

	private void doWork(String message) {

		String msgtype = message.substring(0, message.indexOf("|"));

		if (msgtype.equalsIgnoreCase("001")) {
			doCbat(message);
		} else if (msgtype.equalsIgnoreCase("002")) {
			// new cnu
			//doCnu(message);
		} else if(msgtype.equalsIgnoreCase("003")){
			//doHfc(message);
		} else {
			System.out.println("unknow msg to service");
		}

	}

	private void doCbat(String message) {

		int index1 = 0;
		int index2 = 0;
		// savedb
		index1 = message.indexOf("|");
		index2 = message.indexOf("|", index1 + 1);
		//System.out.println("Cbatindex1 ==" + index1 + "  cbatindex2=" + index2);
		String cbatip = message.substring(index1 + 1, index2);
		//System.out.println("Cbatip ==" + cbatip);

		index1 = index2;
		index2 = message.indexOf("|", index1 + 1);
		//System.out.println("Cbatindex1 ==" + index1 + "  cbatindex2=" + index2);
		String cbatmac = message.substring(index1 + 1, index2).toUpperCase();
		//System.out.println("cbatmac ==" + cbatmac);

		index1 = index2;

		//System.out.println("Cbatindex1 ==" + index1);
		String cbatdevicetype = message.substring(index1 + 1);
		//System.out.println("cbatdevicetype ==" + cbatdevicetype);
		Cbat cbat1 = cbatsv.findCbat(1L);
//		if (cbat!=null) {
//			// cbat already exist; will delete discovery table ip
//			System.out
//					.println("[ServiceDiscoveryProcessor....]This Cbat have already discovery ! ip="
//							+ cbatip + "  mac=" + cbatmac);
//
//			return;
//		}
		Cbat cbat = new Cbat();
		cbat.setActive(true);
		cbat.setIp(cbatip);
		cbat.setMac(cbatmac.toUpperCase());
		cbat.setLabel(cbatmac.toUpperCase());
		cbat.setDeviceType(devicetype.WEC_3501I);
//		switch ((int) Long.parseLong(cbatdevicetype)) {
//		case 1:
//			// WEC-3501I X7
//			cbat.setDeviceType(devicetype.WEC_3501I);
//			break;
//		case 2:
//			// WEC-3501I E31
//			cbat.setDeviceType(devicetype.WEC_3501I_E31);
//			break;
//		case 3:
//			// WEC-3501I Q31
//			cbat.setDeviceType(devicetype.WEC_3501I);
//			break;
//		case 4:
//			// WEC-3501I C22
//			cbat.setDeviceType(devicetype.WEC_3501I);
//			break;
//		case 5:
//			// WEC-3501I S220
//			cbat.setDeviceType(devicetype.WEC_3501I);
//			break;
//		case 6:
//			// WEC-3501I S60
//			cbat.setDeviceType(devicetype.WEC_3501I);
//			break;
//		default:
//			cbat.setDeviceType(devicetype.WEC_3501I_E31);
//			break;
//		}
		
		// add cbatinfo
		Cbatinfo cbatinfo = new Cbatinfo();
		cbatinfo.setBootVer("cml-boot-v1.1.0 for linux sdk");		
		cbatinfo.setContact("11");
		cbatinfo.setLabel("33");
		cbatinfo.setMvId(2L);
		cbatinfo.setMvStatus(false);
		cbatinfo.setPhone("55");
		cbatinfo.setAddress("333");
//		try {
//			cbatinfo.setAgentPort(util.getINT32PDU(cbatip, "161", new OID(new int[] {
//					1,3,6,1,4,1,36186,8,2,7,0})));
//			cbatinfo.setAppVer(util.getStrPDU(cbatip, "161", new OID(new int[] {
//					1, 3, 6, 1, 4, 1, 36186, 8, 4, 4, 0 })));			
//			cbatinfo.setMvId((long) util.getINT32PDU(cbatip, "161", new OID(
//					new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 5, 0 })));
//			cbatinfo.setMvStatus(util.getINT32PDU(cbatip, "161", new OID(
//					new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 4, 0 }))==1?true:false);
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		cbatinfosv.saveCbatinfo(cbatinfo);
		cbat.setCbatinfo(cbatinfo);
		
		cbatsv.saveCbat(cbat);
		//System.out.println(" [x] Cbat Save Done:"+count);
		
	}

	
}
