package com.stan.eoc.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;



@RequestMapping("/profiles/**")
@Controller
public class ProfileController {
	private static JedisPool pool;
	private static Logger logger = Logger.getLogger(ProfileController.class);
	 static {
	        JedisPoolConfig config = new JedisPoolConfig();
	        config.setMaxActive(100);
	        config.setMaxIdle(20);
	        config.setMaxWait(1000);
	        config.setTestOnBorrow(true);
	        pool = new JedisPool(config, "192.168.1.249");
	    }
	 
	 @RequestMapping(value = "/getprofiles")
		@ResponseBody
		public void listProfiles(HttpServletRequest request, HttpServletResponse response){ 
		 	Jedis jedis = pool.getResource();
		 	response.setContentType("text/html");
	    	response.setCharacterEncoding("UTF-8");
		    int i =0;
		    String jsonstring = "";
	        try {                        
	            Set<String> list = jedis.keys("profileid:*:entity");
	            for(Iterator it = list.iterator(); it.hasNext(); ) {
	            	i++;
	        		if(jsonstring == ""){
	        			jsonstring += "{"+'"'+ "pro"+i + '"'+":{";
	        		}else{
	        			jsonstring += ","+'"'+ "pro"+i + '"'+":{";
	        		}
	            	String prokey = (String) it.next();
	            	int index1 = prokey.indexOf(':') +1;
	        		int index2 = prokey.lastIndexOf(':');
	        		String cid = prokey.substring(index1, index2);
	        		   	
	        		jsonstring += '"'+ "id" + '"'+":"+ '"' + cid+ '"' + ",";
	        		jsonstring += '"'+ "proname" + '"'+":"+ '"' + jedis.hget(prokey, "profilename")+ '"' + ",";
	        		jsonstring += '"'+ "vlanen"+ '"'+":" + '"' + jedis.hget(prokey, "vlanen")+ '"' + ",";
	        		jsonstring += '"'+ "vlanid" + '"'+":"+ '"' + jedis.hget(prokey, "vlanid")+ '"' + ",";
	        		jsonstring += '"'+ "vlan0id"+ '"'+":" + '"' + jedis.hget(prokey, "vlan0id")+ '"' + ",";
	        		jsonstring += '"'+ "vlan1id" + '"'+":"+ '"' + jedis.hget(prokey, "vlan1id")+ '"' + ",";
	        		jsonstring += '"'+ "vlan2id"+ '"'+":" + '"' + jedis.hget(prokey, "vlan2id")+ '"' + ",";
	        		jsonstring += '"'+ "vlan3id" + '"'+":"+ '"' + jedis.hget(prokey, "vlan3id")+ '"' + ",";
	        		jsonstring += '"'+ "rxlimitsts"+ '"'+":" + '"' + jedis.hget(prokey, "rxlimitsts")+ '"' + ",";
	        		jsonstring += '"'+ "cpuportrxrate" + '"'+":"+ '"' + jedis.hget(prokey, "cpuportrxrate")+ '"' + ",";
	        		jsonstring += '"'+ "port0txrate"+ '"'+":" + '"' + jedis.hget(prokey, "port0txrate")+ '"' + ",";
	        		jsonstring += '"'+ "port1txrate" + '"'+":"+ '"' + jedis.hget(prokey, "port1txrate")+ '"' + ",";
	        		jsonstring += '"'+ "port2txrate"+ '"'+":" + '"' + jedis.hget(prokey, "port2txrate")+ '"' + ",";
	        		jsonstring += '"'+ "port3txrate" + '"'+":"+ '"' + jedis.hget(prokey, "port3txrate")+ '"' + ",";
	        		jsonstring += '"'+ "txlimitsts"+ '"'+":" + '"' + jedis.hget(prokey, "txlimitsts")+ '"' + ",";
	        		jsonstring += '"'+ "cpuporttxrate" + '"'+":"+ '"' + jedis.hget(prokey, "cpuporttxrate")+ '"' + ",";
	        		jsonstring += '"'+ "port0rxrate"+ '"'+":" + '"' + jedis.hget(prokey, "port0rxrate")+ '"' + ",";
	        		jsonstring += '"'+ "port1rxrate" + '"'+":"+ '"' + jedis.hget(prokey, "port1rxrate")+ '"' + ",";
	        		jsonstring += '"'+ "port2rxrate"+ '"'+":" + '"' + jedis.hget(prokey, "port2rxrate")+ '"' + ",";
	        		jsonstring += '"'+ "port3rxrate" + '"'+":"+ '"' + jedis.hget(prokey, "port3rxrate")+ '"' + "}";
	            }
	            jsonstring += "}";
	            pool.returnResource(jedis);
	    		PrintWriter out = response.getWriter();
	            //logger.info("keys::::::"+ jsonstring);
	            out.println(jsonstring);  
	            out.flush();  
	            out.close();

	            
	        } catch (Exception e) {
	            // TODO Auto-generated catch block
	        	e.printStackTrace();
	        }

		}
	 
	 @RequestMapping(value = "/{proid}")
		@ResponseBody
		public void listProfiles(@PathVariable String proid ,HttpServletRequest request, HttpServletResponse response) throws IOException{ 
		 	Jedis jedis = pool.getResource();
		 	response.setContentType("text/html");
	    	response.setCharacterEncoding("UTF-8");
	    	
	    	String prokey = "profileid:"+proid+":entity";
		    String jsonstring = "{";
	                           
    		jsonstring += '"'+ "proname" + '"'+":"+ '"' + jedis.hget(prokey, "profilename")+ '"' + ",";
    		jsonstring += '"'+ "vlanen"+ '"'+":" + '"' + jedis.hget(prokey, "vlanen")+ '"' + ",";
    		jsonstring += '"'+ "vlanid" + '"'+":"+ '"' + jedis.hget(prokey, "vlanid")+ '"' + ",";
    		jsonstring += '"'+ "vlan0id"+ '"'+":" + '"' + jedis.hget(prokey, "vlan0id")+ '"' + ",";
    		jsonstring += '"'+ "vlan1id" + '"'+":"+ '"' + jedis.hget(prokey, "vlan1id")+ '"' + ",";
    		jsonstring += '"'+ "vlan2id"+ '"'+":" + '"' + jedis.hget(prokey, "vlan2id")+ '"' + ",";
    		jsonstring += '"'+ "vlan3id" + '"'+":"+ '"' + jedis.hget(prokey, "vlan3id")+ '"' + ",";
    		jsonstring += '"'+ "rxlimitsts"+ '"'+":" + '"' + jedis.hget(prokey, "rxlimitsts")+ '"' + ",";
    		jsonstring += '"'+ "cpuportrxrate" + '"'+":"+ '"' + jedis.hget(prokey, "cpuportrxrate")+ '"' + ",";
    		jsonstring += '"'+ "port0txrate"+ '"'+":" + '"' + jedis.hget(prokey, "port0txrate")+ '"' + ",";
    		jsonstring += '"'+ "port1txrate" + '"'+":"+ '"' + jedis.hget(prokey, "port1txrate")+ '"' + ",";
    		jsonstring += '"'+ "port2txrate"+ '"'+":" + '"' + jedis.hget(prokey, "port2txrate")+ '"' + ",";
    		jsonstring += '"'+ "port3txrate" + '"'+":"+ '"' + jedis.hget(prokey, "port3txrate")+ '"' + ",";
    		jsonstring += '"'+ "txlimitsts"+ '"'+":" + '"' + jedis.hget(prokey, "txlimitsts")+ '"' + ",";
    		jsonstring += '"'+ "cpuporttxrate" + '"'+":"+ '"' + jedis.hget(prokey, "cpuporttxrate")+ '"' + ",";
    		jsonstring += '"'+ "port0rxrate"+ '"'+":" + '"' + jedis.hget(prokey, "port0rxrate")+ '"' + ",";
    		jsonstring += '"'+ "port1rxrate" + '"'+":"+ '"' + jedis.hget(prokey, "port1rxrate")+ '"' + ",";
    		jsonstring += '"'+ "port2rxrate"+ '"'+":" + '"' + jedis.hget(prokey, "port2rxrate")+ '"' + ",";
    		jsonstring += '"'+ "port3rxrate" + '"'+":"+ '"' + jedis.hget(prokey, "port3rxrate")+ '"' + "}";

            pool.returnResource(jedis);
    		PrintWriter out = response.getWriter();
            //logger.info("keys::::::"+ jsonstring);
            out.println(jsonstring);  
            out.flush();  
            out.close();


		}
	 
	 @RequestMapping(value="/delete/{proid}")
	 public void deletepro(@PathVariable String proid,HttpServletRequest request, HttpServletResponse response ) throws IOException{
		 response.setContentType("text/html");
	     response.setCharacterEncoding("UTF-8");
		 Jedis jedis = pool.getResource();
		 String prokey = "profileid:"+proid+":entity";
		 //判断profile集合中是否有cnu
		 if(jedis.smembers("profileid:"+proid+":cnus").isEmpty()){
			 //无CNU
			 //删除此profile
			 jedis.del(prokey);
			 
			 pool.returnResource(jedis);
			 PrintWriter out = response.getWriter();
	         //logger.info("keys::::::"+ jsonstring);
	         out.println("ok");  
	         out.flush();  
	         out.close();
		 }else{
			 //集合中有CNU，无法删除此profile
			 
			 pool.returnResource(jedis);
			 PrintWriter out = response.getWriter();
	         //logger.info("keys::::::"+ jsonstring);
	         out.println();  
	         out.flush();  
	         out.close();
		 }		 
		 
	 }
	 
	 @RequestMapping(value="/savepro", method=RequestMethod.POST)
		public void putcnuprofile(HttpServletRequest request, HttpServletResponse response) throws IOException {		
			response.setContentType("text/html");
	    	response.setCharacterEncoding("UTF-8");
	    	Jedis jedis = pool.getResource();
	    	String prokey = "";
	    	//获取传递参数
	    	String proname = request.getParameter("proname");
	    	String vlanen = request.getParameter("vlanen");
	    	String vlanid = request.getParameter("vlanid");
	    	String vlan0id = request.getParameter("vlan0id");
	    	String vlan1id = request.getParameter("vlan1id");
	    	String vlan2id = request.getParameter("vlan2id");
	    	String vlan3id = request.getParameter("vlan3id");
	    	
	    	String rxlimitsts = request.getParameter("rxlimitsts");
	    	String cpuportrxrate = request.getParameter("cpuportrxrate");
	    	String port0txrate = request.getParameter("port0txrate");
	    	String port1txrate = request.getParameter("port1txrate");
	    	String port2txrate = request.getParameter("port2txrate");
	    	String port3txrate = request.getParameter("port3txrate");
	    	
	    	String txlimitsts = request.getParameter("txlimitsts");
	    	String cpuporttxrate = request.getParameter("cpuporttxrate");
	    	String port0rxrate = request.getParameter("port0rxrate");
	    	String port1rxrate = request.getParameter("port1rxrate");
	    	String port2rxrate = request.getParameter("port2rxrate");
	    	String port3rxrate = request.getParameter("port3rxrate");
	    	
	    	//获取profileid
	    	String proid = String.valueOf(jedis.incr("global:profileid"));
	    	prokey = "profileid:"+proid + ":entity";
	    	//组合存储字符串
	    	Map<String , String >  proentity = new HashMap<String, String>();
	    	proentity.put("profilename", proname.toLowerCase());
	    	proentity.put("vlanen", vlanen);
	    	proentity.put("vlanid", vlanid);
	    	proentity.put("vlan0id", vlan0id);
	    	proentity.put("vlan1id", vlan1id);
	    	proentity.put("vlan2id", vlan2id);
	    	proentity.put("vlan3id", vlan3id);
	    	
	    	proentity.put("rxlimitsts", rxlimitsts);
	    	proentity.put("cpuportrxrate", cpuportrxrate);
	    	proentity.put("port0txrate", port0txrate);
	    	proentity.put("port1txrate", port1txrate);
	    	proentity.put("port2txrate", port2txrate);
	    	proentity.put("port3txrate", port3txrate);
	    	
	    	proentity.put("txlimitsts", txlimitsts);
	    	proentity.put("cpuporttxrate", cpuporttxrate);
	    	proentity.put("port0rxrate", port0rxrate);
	    	proentity.put("port1rxrate", port1rxrate);
	    	proentity.put("port2rxrate", port2rxrate);
	    	proentity.put("port3rxrate", port3rxrate);
	    	//save
	    	jedis.hmset(prokey, proentity);
	    	//logger.info("prokeys::::::proname"+ proname + "---vlanen::::"+vlanen );
	    	
	    	//保存数据到硬盘
	    	jedis.save();
	    	
			pool.returnResource(jedis);

		}
	
	 @RequestMapping(value="/editpro/{proid}", method=RequestMethod.POST)
		public void putcnuprofile(@PathVariable String proid,HttpServletRequest request, HttpServletResponse response) throws IOException {		
			response.setContentType("text/html");
	    	response.setCharacterEncoding("UTF-8");
	    	Jedis jedis = pool.getResource();
	    	String prokey = "";
	    	//获取传递参数
	    	String proname = request.getParameter("proname");
	    	String vlanen = request.getParameter("vlanen");
	    	String vlanid = request.getParameter("vlanid");
	    	String vlan0id = request.getParameter("vlan0id");
	    	String vlan1id = request.getParameter("vlan1id");
	    	String vlan2id = request.getParameter("vlan2id");
	    	String vlan3id = request.getParameter("vlan3id");
	    	
	    	String rxlimitsts = request.getParameter("rxlimitsts");
	    	String cpuportrxrate = request.getParameter("cpuportrxrate");
	    	String port0txrate = request.getParameter("port0txrate");
	    	String port1txrate = request.getParameter("port1txrate");
	    	String port2txrate = request.getParameter("port2txrate");
	    	String port3txrate = request.getParameter("port3txrate");
	    	
	    	String txlimitsts = request.getParameter("txlimitsts");
	    	String cpuporttxrate = request.getParameter("cpuporttxrate");
	    	String port0rxrate = request.getParameter("port0rxrate");
	    	String port1rxrate = request.getParameter("port1rxrate");
	    	String port2rxrate = request.getParameter("port2rxrate");
	    	String port3rxrate = request.getParameter("port3rxrate");

	    	prokey = "profileid:"+proid + ":entity";
	    	//组合存储字符串
	    	Map<String , String >  proentity = new HashMap<String, String>();
	    	proentity.put("profilename", proname.toLowerCase());
	    	proentity.put("vlanen", vlanen);
	    	proentity.put("vlanid", vlanid);
	    	proentity.put("vlan0id", vlan0id);
	    	proentity.put("vlan1id", vlan1id);
	    	proentity.put("vlan2id", vlan2id);
	    	proentity.put("vlan3id", vlan3id);
	    	
	    	proentity.put("rxlimitsts", rxlimitsts);
	    	proentity.put("cpuportrxrate", cpuportrxrate);
	    	proentity.put("port0txrate", port0txrate);
	    	proentity.put("port1txrate", port1txrate);
	    	proentity.put("port2txrate", port2txrate);
	    	proentity.put("port3txrate", port3txrate);
	    	
	    	proentity.put("txlimitsts", txlimitsts);
	    	proentity.put("cpuporttxrate", cpuporttxrate);
	    	proentity.put("port0rxrate", port0rxrate);
	    	proentity.put("port1rxrate", port1rxrate);
	    	proentity.put("port2rxrate", port2rxrate);
	    	proentity.put("port3rxrate", port3rxrate);
	    	//save
	    	jedis.hmset(prokey, proentity);
	    	//logger.info("prokeys::::::proname"+ proname + "---vlanen::::"+vlanen );
	    	
	    	//保存数据到硬盘
	    	jedis.save();
	    	
			pool.returnResource(jedis);

		}
}