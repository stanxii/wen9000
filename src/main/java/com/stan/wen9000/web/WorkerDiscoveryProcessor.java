package com.stan.wen9000.web;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.snmp4j.smi.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class WorkerDiscoveryProcessor {

	private static final String DISCOVERY_QUEUE_NAME = "discovery_queue";

	private static final String PERSIST_CBAT_QUEUE_NAME = "service_discovery_queue";

	private static SnmpUtil util = new SnmpUtil();

    private static JedisPool pool;
	 private  static Jedis jedis;
	 static {
	        JedisPoolConfig config = new JedisPoolConfig();
	        config.setMaxActive(1000);
	        config.setMaxIdle(20);
	        config.setMaxWait(1000);	        
	        pool = new JedisPool(config, "192.168.1.249", 6379, 10*1000);
	    }
	 

	public void execute() {
		System.out.println(" [x2] WorkerDiscoveryProcessor Start......");

		try {
			servicestart();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void servicestart() throws Exception {

		
		jedis = pool.getResource();
		
		
		while (true) {
			String message = null;
			
		
			
			message = jedis.rpop(DISCOVERY_QUEUE_NAME);
			
			System.out.println(" [x] WorkerDiscoveryProcessor Received '"
					+ message + "'");
			
			if(message == null) {
				
				
				Thread.sleep(1000);
				continue;
			}
			
			DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date now = new Date();
			try {
				System.out.println(format1.parse(now.toLocaleString()));
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			
			for(long i =0; i< 10000000; i++) {
			doWork(message);
			}
			
			
			
			
			DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date now2 = new Date();
			try {
				System.out.println(format2.parse(now2.toLocaleString()));
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			System.out.println(" [x] WorkerDiscoveryProcessor done ");
			// System.out.println(" [x] WorkerDiscoveryProcessor Done");
			
			
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
			// System.out.println("not a good ip for work");
			return;
		}
		
		
		// test tong will true
					if (true) {
						String cbatmac = "30:71:b2:00:00:ff" ;
						
						// service
						msgservice="";
						 msgservice = "001" + "|" + currentip + "|" + cbatmac +"|" + "2";
						// cbatmac.toUpperCase() + "|"
						// + devicetype.toString();

						
						
						sendToPersist(msgservice);
						msgservice = "";
						
						return;
					}

					
					

		if (hfcping(currentip, "161")) {
			return;
		}
		tong = ping(currentip);
		if (tong) {
			tong = false;

			tong = snmpping(currentip, "161");

			
			// ///////////////////////////////////////////////////
			if (tong) {

				// ////////////////////////////

				// log.info(
				// "Snmping.........ip......#0........successful.... now save to db  Tong tong tong !",
				// currentip);

				String cbatmac = "";

				try {
					cbatmac = util.getStrPDU(currentip, "161", new OID(
							new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 6, 0 }));
					cbatmac = cbatmac.toUpperCase();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out
							.println("WorkDiscoveryProcessing XXXX]]]]]]Cbat get table mac addrress error");
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
					devicetype = (long) util.getINT32PDU(currentip, "161",
							new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 4,
									8, 0 }));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("get devicetype error");
					return;
				}
				// write trap server address
				// //----------

				// //////////////////////////////get cbat ok now send msg to
				// service
				// msgservice = "001" + "|" + currentip + "|" +
				// cbatmac.toUpperCase() + "|"
				// + devicetype.toString();

				sendToPersist(msgservice);
				msgservice = "";

			} else {
				// log.info(
				// "Snmping...ip #0...................  discovery #1  Bu Tong ,Bu tong, Bu tong !",
				// currentip);
				return;
			}
		} else {
			// log.info(
			// "#0 ping ping. ip #1........Bu Bu Tong ,Bu tong, Bu tong !",
			// currentip);

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
			// System.out.println("ping " + ip + " ........>result is,    "
			// + status);

			return status;
		} catch (UnknownHostException e) {
			// e.printStackTrace();
			// System.out
			// .println("ping [#0] ..... UnknownHostException ......result is false"
			// + ip);
			return false;
		} catch (IOException e) {
			// e.printStackTrace();
			// System.out
			// .println("ping [#0] .. IOException .........result is false"
			// + ip);
			return false;
		}

	}

	static Boolean snmpping(String host, String port) {

		return util.snmpping(host, port);
	}

	static Boolean hfcping(String host, String port) {
		// System.out.println("ping hfc~~~~");
		String oid = null;
		try {

			oid = util.gethfcStrPDU(host, port, new OID(new int[] { 1, 3, 6, 1,
					2, 1, 1, 2, 0 }));
			if ((oid != null) && (oid != "")) {
				// service
				String msgservice = "003" + "|" + host + "|" + oid;
				sendToPersist(msgservice);

				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void sendToPersist(String msg) {

		jedis.lpush(PERSIST_CBAT_QUEUE_NAME, msg.toLowerCase().trim());
	
	}

}
