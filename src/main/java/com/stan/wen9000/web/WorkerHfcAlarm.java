package com.stan.wen9000.web;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

import redis.clients.jedis.Jedis;

import com.stan.wen9000.action.jedis.util.RedisUtil;


public class WorkerHfcAlarm{
	private static Logger log = Logger.getLogger(ServiceController.class);
	
	private static RedisUtil redisUtil;

	  
	public static void setRedisUtil(RedisUtil redisUtil) {
		WorkerHfcAlarm.redisUtil = redisUtil;
	}
	
	public void start() {
		log.info("[#2] ..... Worker HfcAlarm starting");	
		
		final int PORT = 5000;
	    DatagramSocket dataSocket;
	    DatagramPacket dataPacket;
	    byte receiveByte[];
	    String receiveStr;
	    
	    try {
            dataSocket = new DatagramSocket(PORT);
            receiveStr = "";
            while (true)// 无数据，则循环
            {
            	receiveByte = new byte[256];
                dataPacket = new DatagramPacket(receiveByte, receiveByte.length);
                dataSocket.receive(dataPacket);
                int i = dataPacket.getLength();
                // 接收数据

                if (i > 0) {
                    // 指定接收到数据的长度,可使接收数据正常显示,开始时很容易忽略这一点

                    receiveStr = new String(dataPacket.getData(), "utf-8");
                    int flag = receiveStr.indexOf("|");
                    //System.out.println("====flag==>>"+flag);
                    String mac = receiveStr.substring(0,flag);
                    String desc = receiveStr.substring(flag + 1);
                    mac = macencode(mac);
                    System.out.println("====mac==>>"+mac+"-------desc-->>"+desc);
                    
                    Map alarmhash = new LinkedHashMap();
                    long alarmtime = System.currentTimeMillis();
       			 	Date date = new Date();
       			 	DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
       			 	String alarmtimes = format.format(date);
       			 	alarmhash.put("lalarmtime", Long.toString(alarmtime));
       			 	alarmhash.put("salarmtime", alarmtimes);
       			 	alarmhash.put("alarmlevel", "3");
       			 	alarmhash.put("alarmcode", "200940");
       			 	alarmhash.put("cbatmac", mac);
       			 	alarmhash.put("cnalarminfo", desc);
					alarmhash.put("enalarminfo", desc);
                    Jedis jedis=null;
            		try {
            			jedis = redisUtil.getConnection();
            			jedis.publish("servicealarm.new", JSONValue.toJSONString(alarmhash));
            			redisUtil.getJedisPool().returnResource(jedis);
            		}catch(Exception e){
            			e.printStackTrace();
            			redisUtil.getJedisPool().returnBrokenResource(jedis);
            			
            		}

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

		
	}
	

	private String macencode(String mac){
		String enmac = "";
		for(int i=0;i<10;i+=2){
			enmac += mac.substring(i, i+2) + ":";
		}
		enmac += mac.substring(10);
		return enmac;
	}
}