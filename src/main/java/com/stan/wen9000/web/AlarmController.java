package com.stan.wen9000.web;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;




import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
	        pool = new JedisPool(config, "127.0.0.1");
	    }
	 
	private static final String ALARM_REALTIME_QUEUE_NAME = "alarm_realtime_queue";
	 
	 
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/realtimealarm", method = RequestMethod.POST, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> listRealtimeAlarm(@RequestParam(value="sEcho", required = false) String sEcho,@RequestParam(value="iDisplayStart", required = false) Integer page, @RequestParam(value="size", required = false) Integer size ){ 
	

		
		
		HttpHeaders headers = new HttpHeaders();        
        headers.add("Content-Type", "application/json; charset=utf-8");
      
        int iTotalRecords; // total number of records (unfiltered)
        int iTotalDisplayRecords;//value will be set when code filters companies by keyword
        JSONObject jsonResponse = new JSONObject();
        try {
            
            JSONArray data = new JSONArray();
            Jedis jedis = pool.getResource();
            long len = jedis.llen(ALARM_REALTIME_QUEUE_NAME);
            iTotalRecords = (int)len;
            iTotalDisplayRecords = (int)len;
            System.out.println("get http request secho=" + sEcho);
            jsonResponse.put("sEcho", "2");
            jsonResponse.put("iTotalRecords", iTotalRecords);
            jsonResponse.put("iTotalDisplayRecords", iTotalDisplayRecords);
            
            
            for(long i=len; len >0; len --) {
	            String alarmid = (String)jedis.lindex(ALARM_REALTIME_QUEUE_NAME, len-1);
	            String alarmkey = "alarmid:" + alarmid +":entity";
	        	Map<String, String> alarmmap = jedis.hgetAll(alarmkey);
	        	if(alarmmap != null){
	        		JSONArray row = new JSONArray();
	        		row.addAll(alarmmap.values());
	        		data.add(row);
	        			
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
}
