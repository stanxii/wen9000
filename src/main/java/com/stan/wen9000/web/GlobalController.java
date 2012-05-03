package com.stan.wen9000.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.stan.wen9000.web.SnmpUtil;
import com.stan.wen9000.action.jedis.util.RedisUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@RequestMapping("/global/**")
@Controller
public class GlobalController {
	private static Logger logger = Logger.getLogger(GlobalController.class);
	private static final String STSCHANGE_QUEUE_NAME = "stschange_queue";
	private static SnmpUtil util = new SnmpUtil();

	
	private static JedisPool pool;
	 
	 static {
	        JedisPoolConfig config = new JedisPoolConfig();
	        config.setMaxActive(100);
	        config.setMaxIdle(20);
	        config.setMaxWait(1000);
	        config.setTestOnBorrow(true);
	        pool = new JedisPool(config, "192.168.1.249");
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
		
		String id = jedis.get("mac:"+mac+":deviceid");
		String cbatkey = "cbatid:"+id+":entity";
		String result = "";
	
		if(jedis.hget(cbatkey, "active").equalsIgnoreCase("1") ){
			//设备在线，实时获得设备信息
			jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(cbatkey,"mac")+ '"' + ",";
			jsonstring += '"'+ "active" + '"'+":"+ '"' + "离线" + '"' + ",";
			jsonstring += '"'+ "ip"+ '"'+":" + '"' + jedis.hget(cbatkey, "ip")+ '"' + ",";
			jsonstring += '"'+ "label"+ '"'+":" + '"' + jedis.hget(cbatkey, "label")+ '"' + ",";
			jsonstring += '"'+ "netmask"+ '"'+":" + '"' + jedis.hget(cbatkey, "netmask")+ '"' + ",";
			jsonstring += '"'+ "gateway"+ '"'+":" + '"' + jedis.hget(cbatkey, "gateway")+ '"' + ",";			
			
			switch(Integer.parseInt(jedis.hget(cbatkey, "devicetype")))
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

			jsonstring += '"'+ "devicetype"+ '"'+":" + '"' + result+ '"' + ",";
			//读取cbatinfo信息

			String cbatinfokey = "cbatid:"+id+":cbatinfo";
			jsonstring += '"'+ "trapserver"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "trapserver")+ '"' + ",";
			jsonstring += '"'+ "address"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "address")+ '"' + ",";
			jsonstring += '"'+ "phone"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "phone")+ '"' + ",";
			jsonstring += '"'+ "bootver"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "bootver")+ '"' + ",";
			jsonstring += '"'+ "contact"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "contact")+ '"' + ",";
			jsonstring += '"'+ "agentport"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "agentport")+ '"' + ",";
			jsonstring += '"'+ "appver"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "appver")+ '"' + ",";
			jsonstring += '"'+ "mvlanenable"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "mvlanenable")+ '"' + ",";
			jsonstring += '"'+ "mvlanid"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "mvlanid")+ '"' + "}";
			
			
		}else{
			//设备离线，从redis获取设备信息
			jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(cbatkey,"mac")+ '"' + ",";
			jsonstring += '"'+ "active" + '"'+":"+ '"' + "离线" + '"' + ",";
			jsonstring += '"'+ "ip"+ '"'+":" + '"' + jedis.hget(cbatkey, "ip")+ '"' + ",";
			jsonstring += '"'+ "label"+ '"'+":" + '"' + jedis.hget(cbatkey, "label")+ '"' + ",";
			jsonstring += '"'+ "netmask"+ '"'+":" + '"' + jedis.hget(cbatkey, "netmask")+ '"' + ",";
			jsonstring += '"'+ "gateway"+ '"'+":" + '"' + jedis.hget(cbatkey, "gateway")+ '"' + ",";			
			
			switch(Integer.parseInt(jedis.hget(cbatkey, "devicetype")))
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

			jsonstring += '"'+ "devicetype"+ '"'+":" + '"' + result+ '"' + ",";
			//读取cbatinfo信息

			String cbatinfokey = "cbatid:"+id+":cbatinfo";
			jsonstring += '"'+ "trapserver"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "trapserver")+ '"' + ",";
			jsonstring += '"'+ "address"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "address")+ '"' + ",";
			jsonstring += '"'+ "phone"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "phone")+ '"' + ",";
			jsonstring += '"'+ "bootver"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "bootver")+ '"' + ",";
			jsonstring += '"'+ "contact"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "contact")+ '"' + ",";
			jsonstring += '"'+ "agentport"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "agentport")+ '"' + ",";
			jsonstring += '"'+ "appver"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "appver")+ '"' + ",";
			jsonstring += '"'+ "mvlanenable"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "mvlanenable")+ '"' + ",";
			jsonstring += '"'+ "mvlanid"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "mvlanid")+ '"' + "}";
			
		}			
		
		//logger.info("getcbat keys::::::"+ jsonstring);
		 pool.returnResource(jedis);
		
		PrintWriter out = response.getWriter();
		out.println(jsonstring);
		out.flush();
		out.close();
	}
	
	@RequestMapping(value="/cnus/{mac}", method=RequestMethod.GET)
	public void getcnu(HttpServletRequest request, HttpServletResponse response,@PathVariable String mac) throws IOException {		
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	String id = jedis.get("mac:"+mac+":deviceid");
    	String cnukey = "cnuid:"+id+":entity";
    	String jsonstring = "{";
    	if(jedis.hget(cnukey, "active").equalsIgnoreCase("1")){
    		//设备在线,获取实时设备信息
    		jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(cnukey,"mac")+ '"' + ",";    		
    		jsonstring += '"'+ "active" + '"'+":"+ '"' + "离线" + '"' + ",";  
    		jsonstring += '"'+ "label"+ '"'+":" + '"' + jedis.hget(cnukey, "label")+ '"' + ",";
    		jsonstring += '"'+ "address"+ '"'+":" + '"' + jedis.hget(cnukey, "address")+ '"' + ",";
    		jsonstring += '"'+ "contact"+ '"'+":" + '"' + jedis.hget(cnukey, "contact")+ '"' + ",";
    		jsonstring += '"'+ "phone"+ '"'+":" + '"' + jedis.hget(cnukey, "phone")+ '"' + "}";
    	}else{
    		//设备离线，获取redis信息
    		jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(cnukey,"mac")+ '"' + ",";    		
    		jsonstring += '"'+ "active" + '"'+":"+ '"' + "离线" + '"' + ",";  
    		jsonstring += '"'+ "label"+ '"'+":" + '"' + jedis.hget(cnukey, "label")+ '"' + ",";
    		jsonstring += '"'+ "address"+ '"'+":" + '"' + jedis.hget(cnukey, "address")+ '"' + ",";
    		jsonstring += '"'+ "contact"+ '"'+":" + '"' + jedis.hget(cnukey, "contact")+ '"' + ",";
    		jsonstring += '"'+ "phone"+ '"'+":" + '"' + jedis.hget(cnukey, "phone")+ '"' + "}";
    	}
    	
    	 pool.returnResource(jedis);
		PrintWriter out = response.getWriter();
        //logger.info("keys::::::"+ jsonstring);
        out.println(jsonstring);  
        out.flush();  
        out.close();
	}
	
	@RequestMapping(value="/cnuprofiles/{mac}", method=RequestMethod.GET)
	public void getcnuprofile(HttpServletRequest request, HttpServletResponse response,@PathVariable String mac) throws IOException {		
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	//获取CNUID
    	String id = jedis.get("mac:"+mac+":deviceid");
    	//获取对应的profileid
    	String proid = jedis.hget("cnuid:"+id+":entity", "profileid");
    	String prokey = "profileid:"+proid+":entity";
    	//组合json字符串
    	String jsonstring = "{";
    	jsonstring += '"'+ "profilename" + '"'+":"+ '"' + jedis.hget(prokey,"profilename")+ '"' + ",";  
    	jsonstring += '"'+ "vlanen" + '"'+":"+ '"' + jedis.hget(prokey,"vlanen")+ '"' + ",";
    	jsonstring += '"'+ "vlanid" + '"'+":"+ '"' + jedis.hget(prokey,"vlanid")+ '"' + ",";
    	jsonstring += '"'+ "vlan0id" + '"'+":"+ '"' + jedis.hget(prokey,"vlan0id")+ '"' + ",";
    	jsonstring += '"'+ "vlan1id" + '"'+":"+ '"' + jedis.hget(prokey,"vlan1id")+ '"' + ",";
    	jsonstring += '"'+ "vlan2id" + '"'+":"+ '"' + jedis.hget(prokey,"vlan2id")+ '"' + ",";
    	jsonstring += '"'+ "vlan3id" + '"'+":"+ '"' + jedis.hget(prokey,"vlan3id")+ '"' + ",";

    	jsonstring += '"'+ "rxlimitsts" + '"'+":"+ '"' + jedis.hget(prokey,"rxlimitsts")+ '"' + ",";
    	jsonstring += '"'+ "cpuportrxrate" + '"'+":"+ '"' + jedis.hget(prokey,"cpuportrxrate")+ '"' + ",";
    	jsonstring += '"'+ "port0txrate" + '"'+":"+ '"' + jedis.hget(prokey,"port0txrate")+ '"' + ",";
    	jsonstring += '"'+ "port1txrate" + '"'+":"+ '"' + jedis.hget(prokey,"port1txrate")+ '"' + ",";
    	jsonstring += '"'+ "port2txrate" + '"'+":"+ '"' + jedis.hget(prokey,"port2txrate")+ '"' + ",";
    	jsonstring += '"'+ "port3txrate" + '"'+":"+ '"' + jedis.hget(prokey,"port3txrate")+ '"' + ",";

    	jsonstring += '"'+ "txlimitsts" + '"'+":"+ '"' + jedis.hget(prokey,"txlimitsts")+ '"' + ",";
    	jsonstring += '"'+ "cpuporttxrate" + '"'+":"+ '"' + jedis.hget(prokey,"cpuporttxrate")+ '"' + ",";
    	jsonstring += '"'+ "port0rxrate" + '"'+":"+ '"' + jedis.hget(prokey,"port0rxrate")+ '"' + ",";
    	jsonstring += '"'+ "port1rxrate" + '"'+":"+ '"' + jedis.hget(prokey,"port1rxrate")+ '"' + ",";
    	jsonstring += '"'+ "port2rxrate" + '"'+":"+ '"' + jedis.hget(prokey,"port2rxrate")+ '"' + ",";
    	jsonstring += '"'+ "port3rxrate" + '"'+":"+ '"' + jedis.hget(prokey,"port3rxrate")+ '"' + "}";
    	 pool.returnResource(jedis);
		PrintWriter out = response.getWriter();
        //logger.info("keys:::proname:::"+ jedis.hget(prokey,"profilename"));
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
		String id = jedis.get("mac:"+mac+":deviceid");
		String hfckey = "hfcid:"+id+":entity";
		
		jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(hfckey,"mac")+ '"' + ",";
		jsonstring += '"'+ "ip" + '"'+":"+ '"' + jedis.hget(hfckey, "ip")+ '"' + ",";
		jsonstring += '"'+ "oid"+ '"'+":" + '"' + jedis.hget(hfckey, "oid")+ '"' + ",";
		jsonstring += '"'+ "hfctype"+ '"'+":" + '"' + jedis.hget(hfckey, "hfctype")+ '"' + ",";		
		jsonstring += '"'+ "logicalid" + '"'+":"+ '"' + jedis.hget(hfckey, "logicalid")+ '"' + ",";
		jsonstring += '"'+ "modelnumber"+ '"'+":" + '"' + jedis.hget(hfckey, "modelnumber")+ '"' + ",";
		jsonstring += '"'+ "serialnumber"+ '"'+":" + '"' + jedis.hget(hfckey, "serialnumber")+ '"' + "}";

		
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
	
	//dynatree初始化读取数据函数
	@RequestMapping(value = "eocs", method = RequestMethod.GET,  headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> getAllEoc(@RequestBody String json) {
		
		
		HttpHeaders headers = new HttpHeaders();        
        headers.add("Content-Type", "application/json; charset=utf-8");
        
		
        JSONArray jsonResponseArray = new JSONArray();
    	
    	
    	Jedis jedis = pool.getResource();
    	String jsonstring = "";
    	Set<String> list = jedis.keys("cbatid:*:entity");

    	
    	
    	JSONObject eocjson = new JSONObject();
		
    	eocjson.put((String)"title", (String)"EOC设备");
    	eocjson.put("key", "eocroot");
    	eocjson.put("isFolder", "true");
    	eocjson.put("expand", "true");

    	//"children"
		
		JSONArray cbatarray = new JSONArray();
    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		
    		JSONObject cbatjson = new JSONObject();

            
    
    		String key = it.next().toString();
   
    		//add head;
    		cbatjson.put("title", jedis.hget(key, "ip"));
    		cbatjson.put("key", jedis.hget(key, "mac"));
    		cbatjson.put("online", jedis.hget(key, "active"));
    		//添加头端信息    		
    		if(jedis.hget(key, "active").equalsIgnoreCase("1")){
    			cbatjson.put("icon", "doc_with_children.gif");
    			//"children"+'"'+":";
    		}else{
    			cbatjson.put("icon", "offline.png");
    			//+"children"+'"'+":";
    		}
    		cbatjson.put("type", "cbat");

    		//获取cbatid
    		String cbatid = jedis.get("mac:" + jedis.hget(key, "mac") + ":deviceid");
    		//logger.info("keys::::::cbatid"+ cbatid);
    		//取得所有属于cbatid的 cnuid
        	Set<String> list_cnu = jedis.smembers("cbatid:" + cbatid + ":cnus");//jedis.keys("cnuid:*:cbatid:"+jedis.get("cbatmac:"+jedis.hget(key, "mac")+":cbatid")+":*:entity");
        	String cnustring ="";
        	
        	JSONArray cnujsons = new JSONArray();
        	for(Iterator jt = list_cnu.iterator(); jt.hasNext(); ) 
        	{ 
        		
        		JSONObject cnujson = new JSONObject();
        		
        		String key_cnuid = jt.next().toString();  
        		String key_cnu = "cnuid:" + key_cnuid + ":entity";
        		//logger.info("keys::::::key_cnu"+ key_cnu);
        		
        	
        		cnujson.put("title", jedis.hget(key_cnu, "label"));
        		cnujson.put("key", jedis.hget(key_cnu, "mac"));
        		cnujson.put("online", jedis.hget(key_cnu, "active"));
        		
        	
        		if(jedis.hget(key_cnu, "active").equalsIgnoreCase("1")){
        			cnujson.put("icon",  "online.gif");        			
        		}else{
        			cnujson.put("icon", "offline.png");        			
        		}
        		cnujson.put("type", "cnu");
        		
        		
        		cnujsons.add(cnujson);
        		
        	}
        	
        	cbatjson.put("children", cnujsons);
        	
        	
        	cbatarray.add(cbatjson);
    	}
    	
   
    	eocjson.put("children", cbatarray);
    	
    	///////////////////////////////hfc
    	
    	
    	/*
   
    	String hfcstring = "";    	
    	list = jedis.keys("hfcid:*:entity");
    	
    	
    	
    	
    	for(Iterator it=list.iterator();it.hasNext();){    		
			if(hfcstring == ""){
        		hfcstring += ",{"+'"'+ "title" + '"'+":"+'"'+"HFC设备"+'"'+","+'"'+"key"+ '"'+":"+'"'+"hfcroot"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true,"+'"'+"children"
    			+'"'+":[{";
    		}else{
    			hfcstring += ",{";
    		}    		
    		String key = it.next().toString();  
    		if(jedis.hget(key, "active").equalsIgnoreCase("1")){
    			hfcstring += '"'+ "title" + '"'+":"+ '"' + jedis.hget(key, "ip")+ '"' + ","+'"'+"key"+'"'+":"+'"'
        		+jedis.hget(key, "mac")+'"'+ ","+'"'+"online"+'"'+":"+'"'+"1"+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"doc_with_children.gif"+'"'+ ","+'"'+"type"+'"'+":"+'"'+"hfc"+'"'+","+'"'+"children"+'"'+":"+"[{"+
        		'"'+ "title" + '"'+":"+'"'+jedis.hget(key, "logicalid")+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"tp.png"+'"'+ "},{"+ '"'+ "title" + '"'+":"+'"'+jedis.hget(key, "modelnumber")+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"tp.png"+'"'+ "},{"+
        		'"'+ "title" + '"'+":"+'"'+jedis.hget(key, "hfctype")+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"tp.png"+'"'+ "}]}";
    		}else{
    			hfcstring += '"'+ "title" + '"'+":"+ '"' + jedis.hget(key, "ip")+ '"' + ","+'"'+"key"+'"'+":"+'"'
        		+jedis.hget(key, "mac")+'"'+ ","+'"'+"online"+'"'+":"+'"'+"0"+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"offline.png"+'"'+ ","+'"'+"type"+'"'+":"+'"'+"hfc"+'"'+","+'"'+"children"+'"'+":"+"[{"+
        		'"'+ "title" + '"'+":"+'"'+jedis.hget(key, "logicalid")+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"tp.png"+'"'+ "},{"+ '"'+ "title" + '"'+":"+'"'+jedis.hget(key, "modelnumber")+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"tp.png"+'"'+ "},{"+
        		'"'+ "title" + '"'+":"+'"'+jedis.hget(key, "hfctype")+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"tp.png"+'"'+ "}]}";
    		}
    		
    	}
    	if(hfcstring.length()>3){
    		hfcstring += "]}";
    	}
    	hfcstring += "]";
    	if(hfcstring.length()<=3)
    	{
    		hfcstring = ",{"+'"'+ "title" + '"'+":"+'"'+"HFC设备"+'"'+","+'"'+"key"+'"'+":"+'"'+"hfcroot"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true"+ "}]";
    	}
    	jsonstring += hfcstring;
    	//logger.info("keys::::::"+ jsonstring);
       // JSONObject json = JSONObject.fromObject(jsonstring);
        //logger.info("searchresult:::::"+ json.toString());
         * */
    	 pool.returnResource(jedis);
    	 
    	 
    	 
    	 jsonResponseArray.add(eocjson);
    	 
    	 return new ResponseEntity<String>(jsonResponseArray.toJSONString(), headers, HttpStatus.OK);
    
    	
    	 
    }
	
	@RequestMapping(value = "tree_read")
    @ResponseBody
    public void tree_read(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	String jsonstring = "";
    	Set<String> list = jedis.keys("cbatid:*:entity");
    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		if(jsonstring == ""){
    			jsonstring += "{"+'"'+ "title" + '"'+":"+'"'+"EOC设备"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true,"+'"'+"children"
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
    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		String key = it.next().toString();  
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
	
	@RequestMapping(value = "statuschange")
	@ResponseBody
	private void getsts(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	//Jedis jedis_queue = redisUtil.getConnection();
    	String jsonstring = "";
    	//从状态变更队列获取状态变更的设备
    	while (true) {
			String message = null;		
			message = jedis.rpop(STSCHANGE_QUEUE_NAME);
			//logger.info("getsts:::message:::"+ message);
			if(message == null) {		
				break;
			}else if(message.equalsIgnoreCase("ok")) {
				
				System.out.println("Why ServiceDiscoveryProcessor receive == ok?? i don't know");
				continue;
			}else{
				//logger.info("getsts:::message:::"+ jedis.hgetAll("cbatid:"+message+":entity"));
				if(jedis.hgetAll("cbatid:"+message+":entity").toString()!="{}"){
					//logger.info("getsts:::message::=================>>>>cbat");
					if(jsonstring == ""){
		    			jsonstring += "[{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("cbatid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("cbatid:"+message+":entity","active")+'"'+","+'"'+"ip"+ '"'+":"+'"'+jedis.hget("cbatid:"+message+":entity","ip")+'"'
		    			+","+'"'+"type"+ '"'+":"+'"'+"cbat"+'"' +"}";
		    			
		    		}else{
		    			jsonstring += ",{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("cbatid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("cbatid:"+message+":entity","active")+'"'+","+'"'+"ip"+ '"'+":"+'"'+jedis.hget("cbatid:"+message+":entity","ip")+'"'
		    			+","+'"'+"type"+ '"'+":"+'"'+"cbat"+'"'+"}";
		    		}
				}else if(jedis.hgetAll("cnuid:"+message+":entity").toString()!="{}"){
					String cbatid = jedis.hget("cnuid:"+message+":entity","cbatid");
					//logger.info("getsts:::message::=================>>>>cnu===="+cbatid);
					if(jsonstring == ""){
		    			jsonstring += "[{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("cnuid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("cnuid:"+message+":entity","active")+'"'+","+'"'+"type"+ '"'+":"+'"'+"cnu"+'"'+","+'"'+"cbatmac"+ '"'+":"+'"'+jedis.hget("cbatid:"+cbatid+":entity","mac")+'"'+"}";
		    			
		    		}else{
		    			jsonstring += ",{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("cnuid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("cnuid:"+message+":entity","active")+'"'+","+'"'+"type"+ '"'+":"+'"'+"cnu"+'"'+","+'"'+"cbatmac"+ '"'+":"+'"'+jedis.hget("cbatid:"+cbatid+":entity","mac")+'"'+"}";
		    		}
					//logger.info("getsts:::message::=================>>>>cnu====end");
				}else if(jedis.hgetAll("hfcid:"+message+":entity").toString()!="{}"){
					//logger.info("getsts:::message::=================>>>>hfc");
					if(jsonstring == ""){
		    			jsonstring += "[{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("hfcid:"+message+":entity","active")+'"'+","+'"'+"ip"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","ip")+'"'+","+'"'+"type"+ '"'+":"+'"'+"hfc"+'"'
		    			+","+'"'+"sn"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","serialnumber")+'"'+","+'"'+"hp"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","hfctype")+'"'
		    			+","+'"'+"id"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","logicalid")+'"'+"}";
		    			
		    		}else{
		    			jsonstring += ",{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("hfcid:"+message+":entity","active")+'"'+","+'"'+"ip"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","ip")+'"'+","+'"'+"type"+ '"'+":"+'"'+"hfc"
		    			+","+'"'+"sn"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","serialnumber")+'"'+","+'"'+"hp"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","hfctype")+'"'
		    			+","+'"'+"id"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","logicalid")+'"'+'"'+"}";
		    		}
				}
				
			}
			
			
		}
    	jsonstring += "]";
    	if(jsonstring.length()<3){
    		jsonstring = "";
    	}
    	//redisUtil.closeConnection(jedis_queue);
    	//logger.info("getsts::::::"+ jsonstring);
    	 pool.returnResource(jedis);
        PrintWriter out = response.getWriter();
        out.println(jsonstring);  
        out.flush();  
        out.close();
	}
	
	
	@RequestMapping(value="/checkedcnus", method=RequestMethod.POST)
	public void checkedcnu(HttpServletRequest request, HttpServletResponse response) throws IOException {		
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	String mac = request.getParameter("cnumac");
    	String val = request.getParameter("value");
    	//logger.info("cnumac::::::"+ mac +"::::::::value::::::"+val);
    	//获取CNU ID 
		String cnuid = jedis.get("mac:"+mac+":deviceid");
    	if(val.equalsIgnoreCase("true")){
    		//保存选择的cnu到集合
    		jedis.sadd("global:checkedcnus", cnuid);
    	}else{
    		//删除选择的cnu
    		jedis.srem("global:checkedcnus", cnuid);
    	}
    	
    	
    	 pool.returnResource(jedis);
	}
	
	@RequestMapping(value="/save_cnuinfo", method=RequestMethod.POST)
	public void savecnuinfo(HttpServletRequest request, HttpServletResponse response) throws IOException {		
		Jedis jedis = pool.getResource();
    	String address = request.getParameter("address");
    	String contact = request.getParameter("contact");
    	String phone = request.getParameter("phone");
    	String label = request.getParameter("label");
    	String mac = request.getParameter("mac");
    	
    	//获取CNU ID 
		String cnuid = jedis.get("mac:"+mac+":deviceid");
    	String key = "cnuid:"+cnuid+":entity";
		jedis.hset(key, "address", address);
		jedis.hset(key, "contact", contact);
		jedis.hset(key, "phone", phone);
		jedis.hset(key, "label", label);
		jedis.save();
		 pool.returnResource(jedis);
	}
	
	//头端数据修改
	@RequestMapping(value="/modifycbat", method=RequestMethod.POST)
	public void modifycbat(HttpServletRequest request, HttpServletResponse response) throws IOException {		
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	String datastring = "";
    	Jedis jedis = pool.getResource();
    	String mac = request.getParameter("mac");
    	String ip = request.getParameter("ip");
    	String label = request.getParameter("label");
    	String address = request.getParameter("address");
    	String mvlanenable = request.getParameter("mvlanenable");
    	String mvlanid = request.getParameter("mvlanid");
    	String trapserver = request.getParameter("trapserver");
    	String trap_port = request.getParameter("trap_port");
    	String netmask = request.getParameter("netmask");
    	String gateway = request.getParameter("gateway");
    	//logger.info("cbatmac::::::"+ mac +"::::::::value::::::"+ip);
    	//获取CBAT ID 
		String cbatid = jedis.get("mac:"+mac+":deviceid");
		String cbatkey = "cbatid:"+cbatid+":entity";
		String cbatinfokey = "cbatid:"+cbatid+":cbatinfo";		
    	
    	//发往设备修改设备相关参数(ip/mvlanenable/mvlanid)
    	try{    		
    		String oldip = jedis.hget(cbatkey, "ip");
    		//判断是否要跟设备交互
    		if((oldip.equalsIgnoreCase(ip))&&(mvlanenable.equalsIgnoreCase(jedis.hget(cbatinfokey, "mvlanenable")))&&(mvlanid.equalsIgnoreCase(jedis.hget(cbatinfokey, "mvlanid")))
    				&&(trapserver.equalsIgnoreCase(jedis.hget(cbatinfokey, "trapserver")))&&(trap_port.equalsIgnoreCase(jedis.hget(cbatinfokey, "agentport")))
    				&&(netmask.equalsIgnoreCase(jedis.hget(cbatkey, "netmask")))&&(gateway.equalsIgnoreCase(jedis.hget(cbatkey, "gateway")))){
    			//保存
            	jedis.hset(cbatkey, "label", label);
            	
            	jedis.hset(cbatinfokey, "address", address);
            	jedis.save();
            	
            	 pool.returnResource(jedis);
            	datastring = "{" + '"' + "sts" + '"' +":" + '"' + "ok" + '"' + "}";
            	PrintWriter out = response.getWriter();
                out.println(datastring);  
                out.flush();  
                out.close();
            	return;
    		}
    		int tmp =(util.getINT32PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0})));
    		if(tmp == -1){
    			return;
    		}
    		util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0}), new Integer32(Integer.valueOf(mvlanenable)));
    		util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,5,0}), new Integer32(Integer.valueOf(mvlanid)));    		
    		util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}), trapserver);
    		util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,7,0}), new Integer32(Integer.valueOf(trap_port)));
    		util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0}), netmask);
    		util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,3,0}), gateway);
    		if(!oldip.equalsIgnoreCase(ip)){
    			util.setV2StrPDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,1,0}), ip);
    			util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,6,2,0}), new Integer32(1));
    			util.setV2PDU(oldip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,6,1,0}), new Integer32(1));
    		}    		
    		
    		//保存
        	jedis.hset(cbatkey, "ip", ip);
        	jedis.hset(cbatkey, "label", label);
        	jedis.hset(cbatkey, "netmask", netmask);
        	jedis.hset(cbatkey, "gateway", gateway);
        	
        	jedis.hset(cbatinfokey, "address", address);
        	jedis.hset(cbatinfokey, "mvlanenable", mvlanenable);
        	jedis.hset(cbatinfokey, "mvlanid", mvlanid);
        	jedis.hset(cbatinfokey, "trapserver", trapserver);
        	jedis.hset(cbatinfokey, "agentport", trap_port);
        	jedis.save();
    	}catch(Exception e){
    		//e.printStackTrace();
    		 pool.returnResource(jedis);
    		return;
    	}
    	 pool.returnResource(jedis);
    	datastring = "{" + '"' + "sts" + '"' +":" + '"' + "ok" + '"' + "}";
    	PrintWriter out = response.getWriter();
        out.println(datastring);  
        out.flush();  
        out.close();
	}
	
	//头端数据同步
	@RequestMapping(value="/synccbat", method=RequestMethod.POST)
	public void synccbat(HttpServletRequest request, HttpServletResponse response) throws IOException {		
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	String mac = request.getParameter("mac");
    	String cbatid = jedis.get("mac:"+mac+":deviceid");
    	String cbatkey = "cbatid:"+cbatid+":entity";
    	String cbatip = jedis.hget(cbatkey, "ip");
    	String cbatinfokey = "cbatid:"+cbatid+":cbatinfo";
    	//获得设备相关参数(ip/mvlanenable/mvlanid)
    	try{
	    	int mvlanenable = (util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,4,0})));
	    	int mvlanid =( util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,5,0})));
	    	String netmask = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,2,0})));
	    	String gateway = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,5,3,0})));
	    	
	    	String hwversion = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,4,3,0})));
	    	String appver = (util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,4,4,0})));
	    	String trapserverip = util.getStrPDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
			int trap_port = util.getINT32PDU(cbatip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,7,0}));
	    	
			//logger.info("mvlanenable::::::"+ mvlanenable);
			
			//更新redis数据
		    jedis.hset(cbatinfokey, "mvlanenable",String.valueOf(mvlanenable));
		    jedis.hset(cbatinfokey, "mvlanid",String.valueOf(mvlanid));
		    jedis.hset(cbatinfokey, "bootver",hwversion);
		    jedis.hset(cbatinfokey, "appver",appver);
		    jedis.hset(cbatinfokey, "agentport",String.valueOf(trap_port));
		    jedis.hset(cbatinfokey, "trapserverip",trapserverip);
		    
		    jedis.hset(cbatkey, "netmask",netmask);
		    jedis.hset(cbatkey, "gateway",gateway);    
		    
		    
		    //组合字符串返回前端
		    String datastring = "{" + '"' + "mvlanenable" + '"' +":" + '"' + mvlanenable + '"' + "," + '"' + "mvlanid"+
		    '"'+":"+'"'+mvlanid+'"'+","+ '"' + "bootver" + '"' +":" + '"' + hwversion + '"' +","+ '"' + "appver" + '"' +":" + '"' + appver + '"'
		    +","+ '"' + "netmask" + '"' +":" + '"' + netmask + '"'+","+ '"' + "gateway" + '"' +":" + '"' + gateway + '"'
		    +","+ '"' + "trapserverip" + '"' +":" + '"' + trapserverip + '"'+","+ '"' + "trap_port" + '"' +":" + '"' + trap_port + '"'+"}";
		    
		    pool.returnResource(jedis);
		    
		    PrintWriter out = response.getWriter();
	        out.println(datastring);  
	        out.flush();  
	        out.close();
	    }catch(Exception e){
	    	//e.printStackTrace();
	    	 pool.returnResource(jedis);
	    	return;
	    }
	    
    	
	}
}