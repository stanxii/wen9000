package com.stan.wen9000.web;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.snmp4j.smi.OID;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;

public class WorkerDiscoveryProcessor implements Job{

	private static final String TASK_QUEUE_NAME = "discovery_queue";

	private static final String PERSIST_CBAT_QUEUE_NAME = "service_discovery_queue";

	private static SnmpUtil util = new SnmpUtil();

	public void execute(JobExecutionContext context) throws JobExecutionException{
		System.out.println(" [x2] WorkerDiscoveryProcessor Start......");
		servicestart();

	}

	public static void servicestart() {

		QueueingConsumer consumer = null;
		
		ConnectionFactory factory = null;
		Connection connection = null;
		Channel channel = null;
		try {
			factory = new ConnectionFactory();
			factory.setHost("localhost");
			connection = factory.newConnection();			
			//create channel			
			channel = connection.createChannel();

			channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
			//now waiting for message
			
			
			channel.basicQos(1);	
			consumer = new QueueingConsumer(channel);			
			channel.basicConsume(TASK_QUEUE_NAME, false, consumer);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block										
			e.printStackTrace();			
		}
		
		
		while (true) {
			String message = null;
			QueueingConsumer.Delivery delivery = null;

			try {
				delivery = consumer.nextDelivery();

				message = new String(delivery.getBody());
			} catch (Exception e) {
				// TODO: handle exception
				continue;
			}

			System.out.println(" [x] WorkerDiscoveryProcessor Received '" + message
					+ "'");
			doWork(message);
			//System.out.println(" [x] WorkerDiscoveryProcessor Done");

			try {
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

		}

	}

	private static void doWork(String currentip) {

		String msgservice = "";

		// do work
		Boolean tong;
		Pattern pattern = Pattern
				.compile("(((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))");
		Matcher m = pattern.matcher(currentip);
		boolean b1 = m.matches();
		if (!b1) {
			//System.out.println("not a good ip for work");
			return;
		}

		if(hfcping(currentip,"161"))
		{
			return;
		}
		tong = ping(currentip);
		if (tong) {
			tong = false;
			
			
			tong = snmpping(currentip, "161");
			if (tong) {
				
				//log.info(
				//		"Snmping.........ip......#0........successful.... now save to db  Tong tong tong !",
				//		currentip);

				String cbatmac = "";
				
				try {
				     cbatmac = util.getStrPDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,6,0}) );
				     cbatmac = cbatmac.toUpperCase();
				} catch (IOException e) {
						// TODO Auto-generated catch block													
					System.out.println("WorkDiscoveryProcessing XXXX]]]]]]Cbat get table mac addrress error");
					return;
				}
					
				

				System.out.println("WorkDiscoveryProcessing discoveryed Mac = "
						+ cbatmac.toUpperCase() + "    ip=  " + currentip);

				if (cbatmac.length() <= 0) {
					System.out
							.println("get cbat mac error, can't discovery this cbat please check this CBAT IP"
									+ currentip);
					return;
				}
				
				Long devicetype = 0L;
				
				try {
					devicetype = (long) util.getINT32PDU(currentip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,4,8,0}) );
				} catch (IOException e) {
						// TODO Auto-generated catch block													
					System.out.println("get devicetype error");
					return;
				}
				// write trap server address
				// //----------

				// //////////////////////////////get cbat ok now send msg to
				// service
				msgservice = "001" + "|" + currentip + "|" + cbatmac.toUpperCase() + "|"
						+ devicetype.toString();

				sendToPersist(msgservice);
				msgservice = "";


			} else {
				//log.info(
				//		"Snmping...ip #0...................  discovery #1  Bu Tong ,Bu tong, Bu tong !",
				//		currentip);
				return;
			}
		} else {
			//log.info(
			//		"#0 ping ping. ip #1........Bu Bu Tong ,Bu tong, Bu tong !",
			//		currentip);

			return;
		}

	}	

	static Boolean ping(String ip) {
		int timeOut = 3000; // I recommend 3 seconds at least

		try {
			if (ip.length() <= 0) {
				System.out.println("ip address is error ping fun...");
				return false;
			}
			InetAddress address = InetAddress.getByName(ip);
			Boolean status = address.isReachable(timeOut);
			//System.out.println("ping " + ip + " ........>result is,    "
			//		+ status);

			return status;
		} catch (UnknownHostException e) {
			//e.printStackTrace();
			//System.out
			//		.println("ping [#0] ..... UnknownHostException ......result is false"
			//				+ ip);
			return false;
		} catch (IOException e) {
			//e.printStackTrace();
			//System.out
			//		.println("ping [#0] .. IOException .........result is false"
			//				+ ip);
			return false;
		}

	}

	static Boolean snmpping(String host, String port) {

		return util.snmpping(host, port);
	}

	static Boolean hfcping(String host, String port) {
		//System.out.println("ping hfc~~~~");
		String oid = null;
		try
		{
			
			oid = util.gethfcStrPDU(host, port, new OID(new int[] {1,3,6,1,2,1,1,2,0}) );
			if((oid != null)&&(oid != ""))
			{			           
				// service
				String msgservice = "003" + "|" + host + "|" + oid;
				sendToPersist(msgservice);
				
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	public static void sendToPersist(String msg) {
			

		ConnectionFactory factory = null;
		Connection connection = null;
		Channel channel = null;
		//This example in Java creates a queue which expires after it has been unused for 30 minutes.
		// Map<String, Object> args = new HashMap<String, Object>();
		//args.put("x-expires", 1800000);
		try {
			factory = new ConnectionFactory();
			factory.setHost("localhost");
			connection = factory.newConnection();			
			//create channel			
			channel = connection.createChannel();

			channel.queueDeclare(PERSIST_CBAT_QUEUE_NAME, true, false, false, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block										
			e.printStackTrace();			
		}
		
		
		try {
			
			
			channel.basicPublish("", PERSIST_CBAT_QUEUE_NAME,
					MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
			//System.out.println(" [x] Sent '" + msg + "'");

			channel.close();
			connection.close();
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

}
