package com.stan.wen9000.web;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;










import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@RequestMapping("/alarm/**")
@Controller
public class AlarmController {

	
	private static JedisPool pool;
//	 
	 static {
	        JedisPoolConfig config = new JedisPoolConfig();
	        config.setMaxActive(100);
	        config.setMaxIdle(20);
	        config.setMaxWait(1000);
	        config.setTestOnBorrow(true);
	        pool = new JedisPool(config, "192.168.1.249");
	    }
	 
	private static final String ALARM_REALTIME_QUEUE_NAME = "alarm_realtime_queue";
	private static final String ALARM_HISTORY_QUEUE_NAME = "alarm_history_queue";
	 
	 
	
	@RequestMapping(value = "/historyalarm", method = RequestMethod.POST, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> listHistoryAlarm( @RequestBody String json){ 
	
		JSONObject jsonResponse = new JSONObject();
		

		//System.out.println("history 1"); 
		HttpHeaders headers = new HttpHeaders();        
        headers.add("Content-Type", "application/json; charset=utf-8");
	    long iDisplayStart=0;
	    long iDisplayLength=0;
	        
	    //System.out.println("history 2");
		
		//System.out.println("RequesBody string=["+ json);
		Object job = JSONValue.parse(json);
				
		//System.out.println("history 3");
		JSONArray array = (JSONArray)job;
		
		int iTotalRecords; // total number of records (unfiltered)
	    int iTotalDisplayRecords;//value will be set when code filters companies by keyword
		
		for(int i=0; i< array.size(); i++ ) {			
			JSONObject item = (JSONObject)array.get(i);		
			if(((String)item.get("name")).equals("sEcho")){
				//System.out.println("historyalarm sEcho=" + item.get("value"));
				jsonResponse.put("sEcho", item.get("value"));
			}else if(((String)item.get("name")).equals("iDisplayStart")){
				iDisplayStart =  (Long)item.get("value");
				//System.out.println("historyalarm iDisplayStart=" + iDisplayStart);
			
			}else if(((String)item.get("name")).equals("iDisplayLength")){				
				iDisplayLength = (Long) item.get("value");
				//System.out.println("iDisplayLength=" + iDisplayLength);
			}		
			
			if(i==array.size() -1) break;
		}
		//System.out.println("historyalarm JSONArray 2size="+ array.size());
			
				
        try {
                    	
            JSONArray data = new JSONArray();
            Jedis jedis = pool.getResource();
            
            
            
            Set<String> results = jedis.zrange(ALARM_HISTORY_QUEUE_NAME, (int)iDisplayStart , (int)(iDisplayStart + iDisplayLength));
            
            
            
            
            iTotalDisplayRecords = iTotalRecords = (int)(long)jedis.zcard(ALARM_HISTORY_QUEUE_NAME);
            
            //iTotalDisplayRecords = iTotalRecords;
            //System.out.println("get http request secho=" + param.get("sEcho")); 
            jsonResponse.put("iTotalRecords", iTotalRecords);
            jsonResponse.put("iTotalDisplayRecords", iTotalDisplayRecords);
            
           
            for(String alarmid: results) {
	            
	            String alarmkey = "alarmid:" + alarmid +":entity";
	        	Map<String, String> alarmmap = jedis.hgetAll(alarmkey);
	        	if(alarmmap != null){
	        		JSONArray row = new JSONArray();
	        		row.addAll(alarmmap.values());	        		
	        		data.add(row);	        		
	        		 System.out.println("historyalarm row=" + row.toJSONString());
	        	}
            }
            pool.returnResource(jedis);
            
            jsonResponse.put("aaData", data);
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
        	e.printStackTrace();
            jsonResponse =null;
        }
        
        
        return new ResponseEntity<String>(jsonResponse.toJSONString(), headers, HttpStatus.OK);
	}
	
	
	

	@RequestMapping(value = "/initRealTimeAlarm", method = RequestMethod.POST, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> initRealTimeAlarm( @RequestBody String json){ 
	
		JSONObject jsonResponse = new JSONObject();
		

		HttpHeaders headers = new HttpHeaders();        
        headers.add("Content-Type", "application/json; charset=utf-8");
	   
				
        try {
                    	
            JSONArray data = new JSONArray();
            Jedis jedis = pool.getResource();
            
            
            int iTotalRecords = 10;            
            List<String> results = jedis.lrange(ALARM_REALTIME_QUEUE_NAME, 0, iTotalRecords -1);
            jsonResponse.put("iTotalRecords", iTotalRecords);
           
            for(int i=0; i< results.size() ; i++) {
	            String alarmid = results.get(i);
	            String alarmkey = "alarmid:" + alarmid +":entity";
	        	Map<String, String> alarmmap = jedis.hgetAll(alarmkey);
	        	if(alarmmap != null){
	        		JSONArray row = new JSONArray();
	        		row.addAll(alarmmap.values());	        		
	        		data.add(row);	        		
	        		 System.out.println("realtimealarm row=" + row.toJSONString());
	        	}
            }
            pool.returnResource(jedis);
            
            jsonResponse.put("aaData", data);
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
        	e.printStackTrace();
            jsonResponse =null;
        }
        
        
        return new ResponseEntity<String>(jsonResponse.toJSONString(), headers, HttpStatus.OK);
	}
	

	
	@RequestMapping(value = "/flushrealtimealarm", method = RequestMethod.POST, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> listRealtimeAlarm( @RequestBody String json){ 
	
		

	JSONObject jsonResponse = new JSONObject();
		

		 
		HttpHeaders headers = new HttpHeaders();        
        headers.add("Content-Type", "application/json; charset=utf-8");
	    
        
        long iRealStartid=0;
	    long iDisplayLength=0;
	        
	  
		//System.out.println("RequesBody string=["+ json);
		Object job = JSONValue.parse(json);
				
		
		JSONArray array = (JSONArray)job;
		
		int iTotalRecords; // total number of records (unfiltered)
	    int iTotalDisplayRecords;//value will be set when code filters companies by keyword
		System.out.println("JSONArray size="+ array.size());
		
		for(int i=0; i< array.size(); i++ ) {
			System.out.println("i=" + i);
			JSONObject item = (JSONObject)array.get(i);		
			 if(((String)item.get("name")).equals("iRealStartid")){
				 iRealStartid =  (Long)item.get("value");
				System.out.println("realtime iRealStartid=" + iRealStartid);
			
			}else if(((String)item.get("name")).equals("iDisplayLength")){				
				iDisplayLength = (Long) item.get("value");
				System.out.println("iDisplayLength=" + iDisplayLength);
			}		
			
			if(i==array.size() -1) break;
		}
		System.out.println("historyalarm JSONArray 2size="+ array.size());
		
		
		
		
		
/*
		HttpHeaders headers = new HttpHeaders();        
        headers.add("Content-Type", "application/json; charset=utf-8");
	   
				
        try {
            
        	System.out.println("now over 2");
            JSONArray data = new JSONArray();
            Jedis jedis = pool.getResource();
            
            
            int iTotalRecords = (int)(long)jedis.llen(ALARM_REALTIME_QUEUE_NAME);            
            List<String> results = jedis.lrange(ALARM_REALTIME_QUEUE_NAME, 0, iTotalRecords -1);
            jsonResponse.put("iTotalRecords", iTotalRecords);
           
            for(int i=0; i< results.size() ; i++) {
	            String alarmid = results.get(i);
	            String alarmkey = "alarmid:" + alarmid +":entity";
	        	Map<String, String> alarmmap = jedis.hgetAll(alarmkey);
	        	if(alarmmap != null){
	        		JSONArray row = new JSONArray();
	        		row.addAll(alarmmap.values());	        		
	        		data.add(row);	        		
	        		 System.out.println("realtimealarm row=" + row.toJSONString());
	        	}
            }
            pool.returnResource(jedis);
            
            jsonResponse.put("aaData", data);
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
        	e.printStackTrace();
            jsonResponse =null;
        }
*/
		
        
        return new ResponseEntity<String>(jsonResponse.toJSONString(), headers, HttpStatus.OK);
	}
	
	
}
