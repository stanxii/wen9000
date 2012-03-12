package com.stan.wen9000.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import com.stan.wen9000.reference.EocDeviceType;
import com.stan.wen9000.service.CbatService;
import com.stan.wen9000.service.CbatinfoService;

public class ServiceDiscoveryProcessor  {

	@Autowired
	CbatService cbatsv;

	@Autowired
	CbatinfoService cbatinfosv;

	EocDeviceType devicetype;

	private static final String PERSIST_CBAT_QUEUE_NAME = "service_discovery_queue";
	private static SnmpUtil util = new SnmpUtil();
	
	private static JedisPool pool;
	 
	
	 
	 static {
	        JedisPoolConfig config = new JedisPoolConfig();
	        config.setMaxActive(1000);
	        config.setMaxIdle(20);
	        config.setMaxWait(1000);	        
	        pool = new JedisPool(config, "192.168.1.249", 6379, 10*1000);
	    }
	 
	 
	 private static Jedis jedis ;

	public void execute() {

		System.out.println("[#3] ..... service discovery starting");

		try {
			servicestart();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void servicestart() throws Exception {

		
		jedis = pool.getResource();
		
		while (true) {
		String message = null;


		
		
		
		message = jedis.rpop(PERSIST_CBAT_QUEUE_NAME);
		
		if(message == null) {
			
			
			Thread.sleep(1000);
			continue;
		}
		else if(message.equalsIgnoreCase("ok")) {
			
			System.out.println("Why ServiceDiscoveryProcessor receive == ok?? i don't know");
			Thread.sleep(1000);
			continue;
		}else if(message.length() < 3) {
			
			System.out.println("Why ServiceDiscoveryProcessor receive len < 3 i don't know");
			Thread.sleep(1000);
			continue;
		}
		
		 System.out.println(" [x]ServiceDiscoveryProcessor  Received '" +
		 message + "'");
		 
		 
		doWork(message);
		// System.out.println(" [x] ServiceDiscoveryProcessor  Done");
		
		}
		
	
		
		
		
	}

	private void doWork(String message) {

		String msgtype = message.substring(0, message.indexOf("|"));

		if (msgtype.equalsIgnoreCase("001")) {
			doCbat(message);
		} else if (msgtype.equalsIgnoreCase("002")) {
			// new cnu
			// doCnu(message);
		} else if (msgtype.equalsIgnoreCase("003")) {
			// doHfc(message);
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
//		System.out.println("Cbatindex1 ==" + index1 + "  cbatindex2=" + index2);
		String cbatip = message.substring(index1 + 1, index2);
//		System.out.println("Cbatip ==" + cbatip);

		index1 = index2;
		index2 = message.indexOf("|", index1 + 1);
//		System.out.println("Cbatindex1 ==" + index1 + "  cbatindex2=" + index2);
		String cbatmac = message.substring(index1 + 1, index2).toUpperCase();
//		System.out.println("cbatmac ==" + cbatmac);

		index1 = index2;

//		System.out.println("Cbatindex1 ==" + index1);
		String cbatdevicetype = message.substring(index1 + 1);
//		System.out.println("cbatdevicetype ==" + cbatdevicetype);

	
		
	   		// add cbatinfo
//		Cbatinfo cbatinfo = new Cbatinfo();
//		cbatinfo.setBootVer("cml-boot-v1.1.0 for linux sdk");
//
//		try {
//			cbatinfo.setAgentPort(util.getINT32PDU(cbatip, "161", new OID(
//					new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 2, 7, 0 })));
//			cbatinfo.setAppVer(util.getStrPDU(cbatip, "161", new OID(new int[] {
//					1, 3, 6, 1, 4, 1, 36186, 8, 4, 4, 0 })));
//			cbatinfo.setMvId((long) util.getINT32PDU(cbatip, "161", new OID(
//					new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 5, 0 })));
//			cbatinfo.setMvStatus(util.getINT32PDU(cbatip, "161", new OID(
//					new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 4, 0 })) == 1 ? true
//					: false);
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		

	
		
		
		
		
		//////////////////////////////////
		
		
			 
		/////////////////////////////////save cbat
//		System.out.println("xxxxxxinsert cbat into redis====" );
		
		
		long start = System.currentTimeMillis();  
		
	    long cbatid = jedis.incr("global:cbatid");
	    
	   
	    
	    //set cbat mac
	    String scbatkey = "cbatid:" + cbatid + ":mac";
//	    System.out.println("cbatmacid=" + scbatkey);
//	    System.out.println("cbatmamac=" + cbatmac);
	    jedis.set(scbatkey, cbatmac.toLowerCase().trim());
	    		
        //set cbat active
	    scbatkey = "cbatid:" + cbatid + ":active";
	    jedis.set(scbatkey, "1");
	    
	    
	  //set cbat ip
	    scbatkey = "cbatid:" + cbatid + ":ip";
	    jedis.set(scbatkey, cbatip.toLowerCase().trim());
	    
	    //set cbat label
	     scbatkey = "cbatid:" + cbatid + ":label";
	     jedis.set(scbatkey, cbatmac.toLowerCase().trim());
	    
	    //set devicetype	   
	    scbatkey = "cbatid:" + cbatid + ":devtype";
	    jedis.set(scbatkey, "2");
	
	    //set cbatmac 's cbatid
	    scbatkey = "cbatmac:" +  cbatmac.toLowerCase().trim() + ":cbatid";
	    jedis.set(scbatkey, Long.toString(cbatid) );
	    
	    
	    
/////////////////////////////save cbatinfo
	    
	    long cbatinfoid = jedis.incr("global:cbatinfoid");
		
		Map<String , String >  hash = new HashMap<String, String>();
		 
		String scbatinfokey = "cbatid:" + cbatid + ":cbatinfo";
		hash.put("address", "na");
		hash.put("phone", "13988777");
		jedis.hmset(scbatinfokey, hash);
		// hmset cnuid:1 cnuid 1 mac 30:71:b2:88:88:01 label fuckyou
		 		
				
		long end = System.currentTimeMillis();  
		System.out.println("one cbat and cbat info SET: " + ((end - start)) + " milliseconds");  

	}

}
