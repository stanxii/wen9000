package com.stan.eoc.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import redis.clients.jedis.Jedis;

import com.stan.eoc.action.jedis.util.RedisUtil;

public class WorkerHfcStatus{	
	private Snmp snmp = null;  
	
    private Address targetAddress = null;  
	private static Logger log = Logger.getLogger(WorkerHfcStatus.class);
	private static RedisUtil redisUtil;
	public static void setRedisUtil(RedisUtil redisUtil) {
		WorkerHfcStatus.redisUtil = redisUtil;
	}
	
	public void initComm() throws IOException {          
		DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		snmp.listen();
    }  
	
	private void start() throws IOException{
		log.info("[#2] ..... WorkerHfcStatus start");   
		initComm();
		while(true){
			try{
				if(servicestart()){
					//eoc模式
					log.info("[#2] ..... WorkerHfcStatus Done!");
					return;
				}
				//log.info("--------------sleep start!!");
				Thread.currentThread().sleep(10000);
				//log.info("--------------sleep end 10s!!");
			}catch(Exception e){
				
			}
			
		}
		
	}
	
	private Boolean servicestart(){
		//获取所有hfc设备
		Jedis jedis=null;
		try {
		 jedis = redisUtil.getConnection();	 
		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			return false;
		}
		if(!jedis.exists("global:displaymode")){
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			redisUtil.getJedisPool().returnResource(jedis);
			return false;
		}
		if(!jedis.get("global:displaymode").equalsIgnoreCase("1")){
			redisUtil.getJedisPool().returnResource(jedis);
			return true;
		}
		//log.info("----------------------------->>>>log testing~~~~");
		String key;
		Set<String> hfcs = jedis.keys("hfcid:*:entity");
		for(Iterator it=hfcs.iterator();it.hasNext();){
			key = it.next().toString();
			targetAddress = GenericAddress.parse("udp:"+jedis.hget(key, "ip")+"/161");  
			try {
				getPDU(jedis,jedis.hget(key, "mac"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//snmpAsynGetList(jedis,jedis.hget(key, "ip"),jedis.hget(key, "mac"));
		}
		redisUtil.getJedisPool().returnResource(jedis);
		return false;
	}
	
	public void getPDU(Jedis jedis,String mac) throws IOException {  
        // PDU 对象  
        PDU pdu = new PDU();  
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.17409.1.3.3.2.2.1.10.1")));  //mac
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.17409.1.3.2.1.1.1.0")));  //mac
        // 操作类型  
        pdu.setType(PDU.GET);  
        sendPDU(pdu,mac);  
    }  
	
	 public void sendPDU(final PDU pdu,final String mac) throws IOException {  
	        // 设置 目标  
	        CommunityTarget target = new CommunityTarget();  
	        target.setCommunity(new OctetString("public"));  
	        target.setAddress(targetAddress);  
	        // 通信不成功时的重试次数 N+1次  
	        target.setRetries(2);  
	        // 超时时间  
	        target.setTimeout(2 * 1000);  
	        // SNMP 版本  
	        target.setVersion(SnmpConstants.version1);  	  
	        // 设置监听对象  	        
	        ResponseListener listener = new ResponseListener() {  
	            public void onResponse(ResponseEvent event) {  
	                //System.out.println("---------->开始异步解析<------------");  
	                readResponse(event,mac);
	                snmp.cancel(pdu, this);
	            }  
	        };  	        
	        // 发送报文  
	        snmp.send(pdu, target, null, listener);  
	        //return null;  
	    }  
	
	@SuppressWarnings("unchecked")  
    public void readResponse(ResponseEvent respEvnt,String mac) {  
        // 解析Response  
        //System.out.println("------------>解析Response<----------"); 
		 Jedis jedis1 = null;
         try {
         	jedis1 = redisUtil.getConnection();	 
    		
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis1);
			return;
		}
        if (respEvnt != null && respEvnt.getResponse() != null) {  
            Vector<VariableBinding> recVBs = respEvnt.getResponse()  
                    .getVariableBindings();  
//            for (int i = 0; i < recVBs.size(); i++) {  
//                VariableBinding recVB = recVBs.elementAt(i);  
//                System.out.println(recVB.getOid() + " :::"+i+"::: "  
//                        + recVB.getVariable().toString());  
//            }  
           
            VariableBinding recVB = recVBs.elementAt(0);
            JSONObject json = new JSONObject();
            if((recVB.getVariable().toString().equalsIgnoreCase("Null"))){
            	recVB = recVBs.elementAt(1);
            }
        	json.put("mac", recVB.getVariable().toString());        	
        	json.put("flag", "1");
        	jedis1.publish("ServiceHfcStatus.hfcmac", json.toJSONString());
        	redisUtil.getJedisPool().returnResource(jedis1);
        	
        }else{
        	//log.info("-------->>hfc设备不在线:"+mac);
        	JSONObject json = new JSONObject();
        	json.put("mac", mac);
        	json.put("flag", "0");
        	jedis1.publish("ServiceHfcStatus.hfcmac", json.toJSONString());
        	redisUtil.getJedisPool().returnResource(jedis1);
        }
        
    }  

}