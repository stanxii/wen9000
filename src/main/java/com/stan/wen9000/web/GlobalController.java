package com.stan.wen9000.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@RequestMapping("/global/**")
@Controller
public class GlobalController {
	private static Logger logger = Logger.getLogger(DiscoveryController.class);
	private static JedisPool pool;
	
	static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(100);
        config.setMaxIdle(20);
        config.setMaxWait(1000);
        config.setTestOnBorrow(true);
        pool = new JedisPool(config, "127.0.0.1");
    }
	
	@RequestMapping(value="/cbatinfo/{id}", method=RequestMethod.GET)
	public String prepare(Model model,@PathVariable Long id) {		
		Jedis jedis = pool.getResource();
		String tmpdata = jedis.hget("cbatid:"+id+":cbatinfo","address");
		model.addAttribute("address", tmpdata);
		tmpdata = jedis.hget("cbatid:"+id+":cbatinfo","phone");
		model.addAttribute("phone", tmpdata);
		tmpdata = jedis.hget("cbatid:"+id+":cbatinfo","bootver");
		model.addAttribute("bootver", tmpdata);
		tmpdata = jedis.hget("cbatid:"+id+":cbatinfo","contact");
		model.addAttribute("contact", tmpdata);
		tmpdata = jedis.hget("cbatid:"+id+":cbatinfo","agentport");
		model.addAttribute("agentport", tmpdata);
		tmpdata = jedis.hget("cbatid:"+id+":cbatinfo","appver");
		model.addAttribute("appver", tmpdata);
		tmpdata = jedis.hget("cbatid:"+id+":cbatinfo","mvlanid");
		model.addAttribute("mvlanid", tmpdata);
		tmpdata = jedis.hget("cbatid:"+id+":cbatinfo","mvlanenable");
		model.addAttribute("mvlanenable", tmpdata);
		
		pool.returnResource(jedis);
		return "cbatinfoes/show";
	}
	
	@RequestMapping(value="/cbats/{mac}", method=RequestMethod.GET)
	public void getcbat(HttpServletRequest request, HttpServletResponse response,@PathVariable String mac) throws IOException {
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	String jsonstring = "{";
		Jedis jedis = pool.getResource();
		String id = jedis.get("cbatmac:"+mac+":cbatid");
		String cbatkey = "cbatid:"+id+":entity";
		
		jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(cbatkey,"mac")+ '"' + ",";
		jsonstring += '"'+ "active" + '"'+":"+ '"' + jedis.hget(cbatkey, "active")+ '"' + ",";
		jsonstring += '"'+ "ip"+ '"'+":" + '"' + jedis.hget(cbatkey, "ip")+ '"' + ",";
		jsonstring += '"'+ "label"+ '"'+":" + '"' + jedis.hget(cbatkey, "label")+ '"' + ",";
		jsonstring += '"'+ "devicetype"+ '"'+":" + '"' + jedis.hget(cbatkey, "devicetype")+ '"' + "}";
		logger.info("getcbat keys::::::"+ jsonstring);
		pool.returnResource(jedis);
		//return "cbats/show";
		PrintWriter out = response.getWriter();
		out.println(jsonstring);
		out.flush();
		out.close();
	}
	
	@RequestMapping(value="/cnus/{mac}", method=RequestMethod.GET)
	public void getcnu(HttpServletRequest request, HttpServletResponse response,@PathVariable String mac) throws IOException {		
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	String jsonstring = "{";
		Jedis jedis = pool.getResource();
		String id = jedis.get("cnumac:"+mac+":cnuid");
		String cnukey = jedis.keys("cnuid:"+id+"*:entity").toString().replace("[", "").replace("]", "").trim();
		
		jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(cnukey,"mac")+ '"' + ",";
		jsonstring += '"'+ "active" + '"'+":"+ '"' + jedis.hget(cnukey, "active")+ '"' + ",";
		jsonstring += '"'+ "address"+ '"'+":" + '"' + jedis.hget(cnukey, "address")+ '"' + ",";
		jsonstring += '"'+ "contact"+ '"'+":" + '"' + jedis.hget(cnukey, "contact")+ '"' + ",";
		jsonstring += '"'+ "phone"+ '"'+":" + '"' + jedis.hget(cnukey, "phone")+ '"' + "}";

		
		pool.returnResource(jedis);
		//return "cnus/show";
		PrintWriter out = response.getWriter();
        //logger.info("keys::::::"+ json);
        out.println(jsonstring);  
        out.flush();  
        out.close();
	}
	
	@RequestMapping(value="/hfcs/{mac}", method=RequestMethod.GET)
	public void gethfc(HttpServletRequest request, HttpServletResponse response,@PathVariable String mac) throws IOException {		
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	String jsonstring = "{";
		Jedis jedis = pool.getResource();
		String id = jedis.get("hfcmac:"+mac+":hfcid");
		String hfckey = jedis.keys("hfcid:"+id+"*:entity").toString().replace("[", "").replace("]", "").trim();
		
		jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(hfckey,"hfcmac")+ '"' + ",";
		jsonstring += '"'+ "ip" + '"'+":"+ '"' + jedis.hget(hfckey, "ip")+ '"' + ",";
		jsonstring += '"'+ "oid"+ '"'+":" + '"' + jedis.hget(hfckey, "oid")+ '"' + ",";
		jsonstring += '"'+ "hfctype"+ '"'+":" + '"' + jedis.hget(hfckey, "hfctype")+ '"' + ",";		
		jsonstring += '"'+ "logicalid" + '"'+":"+ '"' + jedis.hget(hfckey, "logicalid")+ '"' + ",";
		jsonstring += '"'+ "modelnumber"+ '"'+":" + '"' + jedis.hget(hfckey, "modelnumber")+ '"' + ",";
		jsonstring += '"'+ "serialnumber"+ '"'+":" + '"' + jedis.hget(hfckey, "serialnumber")+ '"' + ",";
		jsonstring += '"'+ "version"+ '"'+":" + '"' + jedis.hget(hfckey, "version")+ '"' + "}";

		
		pool.returnResource(jedis);
		//return "cnus/show";
		PrintWriter out = response.getWriter();
        //logger.info("keys::::::"+ json);
        out.println(jsonstring);  
        out.flush();  
        out.close();
	}
	
	
	@RequestMapping(value = "cbats")
    @ResponseBody
    public void getcbats(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
    	//logger.info("keys::::::"+ jsonstring);
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
	
	@RequestMapping(value = "eocs")
    @ResponseBody
    public void getAllEoc(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	String jsonstring = "";
    	Set<String> list = jedis.keys("cbatid:*:entity");
    	int i=0;
    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		i++;
    		if(jsonstring == ""){
    			jsonstring += "[{"+'"'+ "title" + '"'+":"+'"'+"EOC_NODE"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true,"+'"'+"children"
    			+'"'+":[{";
    		}else{
    			jsonstring += ",{";
    		}
    		String key = it.next().toString();    		   	
    		jsonstring += '"'+ "title" + '"'+":"+ '"' + jedis.hget(key, "ip")+ '"' + ","+'"'+"mac"+'"'+":"+'"'
    		+jedis.hget(key, "mac")+'"'+ ","+'"'+"type"+'"'+":"+'"'+"cbat"+'"'+","+'"'+"children"+'"'+":";
    		//取得所有属于cbatmac为mac的cnu key
        	Set<String> list_cnu = jedis.keys("cnuid:*:cbatid:"+jedis.get("cbatmac:"+jedis.hget(key, "mac")+":cbatid")+":*:entity");
        	int j=0;
        	String cnustring ="";
        	for(Iterator jt = list_cnu.iterator(); jt.hasNext(); ) 
        	{ 
        		String key_cnu = jt.next().toString();  
        		j++;
        		if(cnustring == ""){
        			cnustring += "[{";
        		}else{
        			cnustring += ",{";
        		}    		
        		cnustring += '"'+ "title" + '"'+":"+'"'+jedis.hget(key_cnu, "mac")+'"'+ ","+'"'+"mac"+'"'+":"+'"'
        		+jedis.hget(key_cnu, "mac")+'"'+ ","+'"'+"type"+'"'+":"+'"'+"cnu"+'"'+ "}";
        	}
        	cnustring += "]";
        	if(cnustring.length()<3)
        	{
        		cnustring = "[{"+'"'+ "title" + '"'+":"+'"'+"NOData"+'"'+ "}]";
        	}
        	jsonstring += cnustring;
    		jsonstring += "}";
    	}
    	jsonstring += "]}";
    	if(jsonstring.length()<3)
    	{
    		jsonstring = "{"+'"'+ "title" + '"'+":"+'"'+"NOData"+'"'+ "}";
    	}
    	
    	String hfcstring = "";
    	
    	list = jedis.keys("hfcid:*:entity");
    	for(Iterator it=list.iterator();it.hasNext();){
    		if(hfcstring == ""){
        		hfcstring += ",{"+'"'+ "title" + '"'+":"+'"'+"HFC_NODE"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true,"+'"'+"children"
    			+'"'+":[{";
    		}else{
    			hfcstring += ",{";
    		}
    		String key = it.next().toString();   
    		hfcstring += '"'+ "title" + '"'+":"+ '"' + jedis.hget(key, "ip")+ '"' + ","+'"'+"mac"+'"'+":"+'"'
    		+jedis.hget(key, "hfcmac")+'"'+ ","+'"'+"type"+'"'+":"+'"'+"hfc"+'"'+","+'"'+"children"+'"'+":"+"[{"+
    		'"'+ "title" + '"'+":"+'"'+jedis.hget(key, "logicalid")+'"'+ "},{"+ '"'+ "title" + '"'+":"+'"'+jedis.hget(key, "modelnumber")+'"'+ "},{"+
    		'"'+ "title" + '"'+":"+'"'+jedis.hget(key, "hfctype")+'"'+ "}]}";
    	}
    	hfcstring += "]}]";
    	jsonstring += hfcstring;
    	//logger.info("keys::::::"+ jsonstring);
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
	
	@RequestMapping(value = "tree_read")
    @ResponseBody
    public void tree_read(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	String jsonstring = "";
    	Set<String> list = jedis.keys("cbatid:*:entity");
    	int i=0;
    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		i++;
    		if(jsonstring == ""){
    			jsonstring += "{"+'"'+ "title" + '"'+":"+'"'+"EOC_NODE"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true,"+'"'+"children"
    			+'"'+":[{";
    		}else{
    			jsonstring += ",{";
    		}
    		String key = it.next().toString();    		   	
    		jsonstring += '"'+ "title" + '"'+":"+ '"' + jedis.hget(key, "ip")+ '"' + ","+'"'+"isLazy"+'"'+":true,"+'"'+"mac"+'"'+":"+'"'+jedis.hget(key, "mac")+'"'+ "}";

    	}
    	jsonstring += "]}";
    	if(jsonstring.length()<3)
    	{
    		jsonstring = "{"+'"'+ "title" + '"'+":"+'"'+"NOData"+'"'+ "}";
    	}
    	//logger.info("keys::::::"+ jsonstring);
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
	
	@RequestMapping(value = "cnu_read/{mac}")
	@ResponseBody
	private void getcnus(@PathVariable String mac,HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	String jsonstring = "";
    	//String cbatmac = mac.substring(mac.indexOf(":")+3, mac.length()-1);
    	logger.info("cbatmac keys::::::"+ mac);
    	//取得所有属于cbatmac为mac的cnu key
    	Set<String> list = jedis.keys("cnuid:*:cbatid:"+jedis.get("cbatmac:"+mac+":cbatid")+":*:entity");
    	int i=0;
    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		String key = it.next().toString();  
    		i++;
    		if(jsonstring == ""){
    			jsonstring += "[{";
    		}else{
    			jsonstring += ",{";
    		}    		
    		jsonstring += '"'+ "title" + '"'+":"+'"'+jedis.hget(key, "mac")+'"'+ ","+'"'+"mac"+'"'+":"+'"'+jedis.hget(key, "mac")+'"'+ "}";
    	}
    	jsonstring += "]";
    	if(jsonstring.length()<3)
    	{
    		jsonstring = "[{"+'"'+ "title" + '"'+":"+'"'+"NOData"+'"'+ "}]";
    	}
    	//logger.info("cnukeys::::::"+ jsonstring);
    	pool.returnResource(jedis);
        PrintWriter out = response.getWriter();
        out.println(jsonstring);  
        out.flush();  
        out.close();
	}
	
}