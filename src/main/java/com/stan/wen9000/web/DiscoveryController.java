package com.stan.wen9000.web;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@RequestMapping("/discovery/**")
@Controller
public class DiscoveryController {
	private static Logger logger = Logger.getLogger(DiscoveryController.class);
	private String currentip;	
	private static final String DISCOVERY_QUEUE_NAME = "discovery_queue";
	
	private static final String PROCESS_CBAT_QUEUE_NAME = "process_queue";
	
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
	 
//	  private static RedisUtil ru;
	
	
//    @Autowired
//    CnuController cnuctl;
    

    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }

    @RequestMapping
    public String index() {
        return "discovery/index";
    }
    
    @RequestMapping(value = "searchresult")
    @ResponseBody
    public void searchResultList(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	String result = "";
    	String jsonstring = "";
    	Set<String> list = jedis.keys("cbatid:*:entity");
    	int i=0;
    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		i++;
    		if(jsonstring == ""){
    			jsonstring += "{"+'"'+ "cbat"+i + '"'+":{";
    		}else{
    			jsonstring += ","+'"'+ "cbat"+i + '"'+":{";
    		}
    		String key = it.next().toString();
    		int index1 = key.indexOf(':') +1;
    		int index2 = key.lastIndexOf(':');
    		String cid = key.substring(index1, index2);
    		   	
    		jsonstring += '"'+ "id" + '"'+":"+ '"' + cid+ '"' + ",";
    		jsonstring += '"'+ "active" + '"'+":"+ '"' + jedis.hget(key, "active")+ '"' + ",";
    		jsonstring += '"'+ "mac"+ '"'+":" + '"' + jedis.hget(key, "mac")+ '"' + ",";
    		switch(Integer.parseInt(jedis.hget(key, "devicetype")))
    		{
            	case 1:
            		//break;
            	case 2:
            		
            		//break;
            	case 3:
            		//break;
            	case 4:
            		
            		//break;
            	case 5:
            		//break;
            	case 6:
            		
            		//break;
            	case 7:
            		//break;
            	case 8:
            		result = "中文测试";
            		break;
            	default:
            		result = "Unknown";
            		break;
    		}
    		jsonstring += '"'+ "devicetype"+ '"'+":" + '"' + result + '"'+ ",";
    		jsonstring += '"'+ "label"+ '"'+":"+ '"'  + jedis.hget(key, "label")+ '"' + ",";
    		jsonstring += '"'+ "ip"+ '"'+":"+ '"'  + jedis.hget(key, "ip")+ '"' + ",";
    		jsonstring += '"'+ "cbatinfo"+ '"'+":" + '"' + "h_b"+ '"' + "}";
    	}
    	jsonstring += "}";
    	logger.info("keys::::::"+ jsonstring);
       // JSONObject json = JSONObject.fromObject(jsonstring);
        //logger.info("searchresult:::::"+ json.toString());
        pool.returnResource(jedis);
        PrintWriter out = response.getWriter();
        //logger.info("keys::::::"+ json);
        out.println(jsonstring);  
        out.flush();  
        out.close();
    	//return jsonstring;
    }
    
    @RequestMapping(value = "discovertotal")
    public @ResponseBody String getdiscoverTotal() {
    	Jedis jedis = pool.getResource();
    	Long count = jedis.llen(DISCOVERY_QUEUE_NAME);
    	pool.returnResource(jedis);
    	
    	Long total = Long.parseLong(jedis.get("global:discovertotal"));
    	float val = (total - count);
    	String msg = String.valueOf((val/Float.valueOf(String.valueOf(total)))*100);
    	logger.info("discovermsg:::::"+ msg);
    	if(msg.equalsIgnoreCase("100.0")){
    		logger.info("------------------------------------------------- discover Done");
    		jedis.set("searchrun", "false");
    	}
    		
    	return msg;
    }
    
    @RequestMapping(value = "search",  method = RequestMethod.POST)
    public String searchProduct(@RequestParam(value = "startip", required = false) String st, @RequestParam(value = "stopip", required = false) String end) throws Exception {
        System.out.println("start:"+st + ",end:"+end);
        //quartzRun();
        Jedis jedis = pool.getResource();
        if(jedis.get("searchrun")==null){
        	jedis.set("searchrun", "false");
        }
        if(jedis.get("searchrun").equalsIgnoreCase("true"))
        {
        	logger.info("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::return");
        	return "discovery/result";
        }        
        jedis.set("searchrun", "true");
        long longstartIp = IP2Long.ipToLong(st);		
		long longstopIp = IP2Long.ipToLong(end);
		long total = longstopIp - longstartIp + 1;
		if(total >256)
		{
			logger.info("search ip out of range!");
			return "discovery/search";
		}
//		Jedis jedis = ru.getConnection();
		
		
		String msg = "message:"+String.valueOf(total);
		jedis.lpush(PROCESS_CBAT_QUEUE_NAME, msg);

		while (longstartIp <= longstopIp) {
			currentip = IP2Long.longToIP(longstartIp);

			
			jedis.lpush(DISCOVERY_QUEUE_NAME, currentip);
			 System.out.println("DiscoveryAction [x] Sent '" + currentip +
			 "'");

			longstartIp++;

		}
		pool.returnResource(jedis);
		
//		ru.closeConnection(jedis);
		

		return "discovery/result";
	}

}
