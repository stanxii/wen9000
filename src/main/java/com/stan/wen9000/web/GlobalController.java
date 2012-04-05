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

import com.stan.wen9000.action.jedis.util.RedisUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@RequestMapping("/global/**")
@Controller
public class GlobalController {
	private static Logger logger = Logger.getLogger(DiscoveryController.class);
	private static final String STSCHANGE_QUEUE_NAME = "stschange_queue";
	private static JedisPool pool;
	private static RedisUtil redisUtil;
	 public static RedisUtil getRedisUtil() {
			return redisUtil;
		}

		public static void setRedisUtil(RedisUtil redisUtil) {
			GlobalController.redisUtil = redisUtil;
		}
	
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
		String id = jedis.get("mac:"+mac+":deviceid");
		String cbatkey = "cbatid:"+id+":entity";
		String result = "";
	
		if(jedis.hget(cbatkey, "active").equalsIgnoreCase("1") ){
			//设备在线，实时获得设备信息
			jsonstring += '"'+ "active" + '"'+":"+ '"' + "在线" + '"' + ",";
		}else{
			//设备离线，从redis获取设备信息
			jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(cbatkey,"mac")+ '"' + ",";
			jsonstring += '"'+ "active" + '"'+":"+ '"' + "离线" + '"' + ",";
			jsonstring += '"'+ "ip"+ '"'+":" + '"' + jedis.hget(cbatkey, "ip")+ '"' + ",";
			jsonstring += '"'+ "label"+ '"'+":" + '"' + jedis.hget(cbatkey, "label")+ '"' + ",";

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
			jsonstring += '"'+ "address"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "address")+ '"' + ",";
			jsonstring += '"'+ "phone"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "phone")+ '"' + ",";
			jsonstring += '"'+ "bootver"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "bootver")+ '"' + ",";
			jsonstring += '"'+ "contact"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "contact")+ '"' + ",";
			jsonstring += '"'+ "agentport"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "agentport")+ '"' + ",";
			jsonstring += '"'+ "appver"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "appver")+ '"' + ",";
			jsonstring += '"'+ "mvlanenable"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "mvlanenable")+ '"' + ",";
			jsonstring += '"'+ "mvlanid"+ '"'+":" + '"' + jedis.hget(cbatinfokey, "mvlanid")+ '"' + "}";
			
		}			
		
		logger.info("getcbat keys::::::"+ jsonstring);
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
    		
    	}else{
    		//设备离线，获取redis信息
    		jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(cnukey,"mac")+ '"' + ",";    		
    		jsonstring += '"'+ "active" + '"'+":"+ '"' + "离线" + '"' + ",";    		
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
    	//这里是测试代码，profileid=1
    	String prokey = "profileid:1:entity";
    	//组合json字符串
    	String jsonstring = "{";
    	jsonstring += '"'+ "profilename" + '"'+":"+ '"' + jedis.hget(prokey,"profilename")+ '"' + ",";  
    	jsonstring += '"'+ "vlanenable" + '"'+":"+ '"' + jedis.hget(prokey,"vlanenable")+ '"' + ",";
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
        logger.info("keys::::::"+ jsonstring);
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
	
	//dynatree初始化读取数据函数
	@RequestMapping(value = "eocs")
    @ResponseBody
    public void getAllEoc(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	String jsonstring = "";
    	Set<String> list = jedis.keys("cbatid:*:entity");

    	for(Iterator it = list.iterator(); it.hasNext(); ) 
    	{ 
    		if(jsonstring == ""){
    			jsonstring += "[{"+'"'+ "title" + '"'+":"+'"'+"EOC设备"+'"'+","+'"'+"key"+'"'+":"+ '"'+"eocroot"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true,"+'"'+"children"
    			+'"'+":[{";
    		}else{
    			jsonstring += ",{";
    		}
    		String key = it.next().toString();   
    		//添加头端信息
    		if(jedis.hget(key, "active").equalsIgnoreCase("1")){
    			jsonstring += '"'+ "title" + '"'+":"+ '"' + jedis.hget(key, "ip")+ '"' + ","+'"'+"key"+'"'+":"+'"'
        		+jedis.hget(key, "mac")+'"'+ ","+'"'+"online"+'"'+":"+'"'+jedis.hget(key, "active")+'"'+ ","+'"'+"type"+'"'+":"+'"'+"cbat"+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"doc_with_children.gif"+'"'+","+'"'+"children"+'"'+":";
    		}else{
    			jsonstring += '"'+ "title" + '"'+":"+ '"' + jedis.hget(key, "ip")+ '"' + ","+'"'+"key"+'"'+":"+'"'
        		+jedis.hget(key, "mac")+'"'+ ","+'"'+"online"+'"'+":"+'"'+jedis.hget(key, "active")+'"'+ ","+'"'+"type"+'"'+":"+'"'+"cbat"+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"offline.png"+'"'+","+'"'+"children"+'"'+":";
    		}

    		//获取cbatid
    		String cbatid = jedis.get("mac:" + jedis.hget(key, "mac") + ":deviceid");
    		//logger.info("keys::::::cbatid"+ cbatid);
    		//取得所有属于cbatid的 cnuid
        	Set<String> list_cnu = jedis.smembers("cbatid:" + cbatid + ":cnus");//jedis.keys("cnuid:*:cbatid:"+jedis.get("cbatmac:"+jedis.hget(key, "mac")+":cbatid")+":*:entity");
        	String cnustring ="";
        	for(Iterator jt = list_cnu.iterator(); jt.hasNext(); ) 
        	{ 
        		String key_cnuid = jt.next().toString();  
        		String key_cnu = "cnuid:" + key_cnuid + ":entity";
        		//logger.info("keys::::::key_cnu"+ key_cnu);
        		if(cnustring == ""){
        			cnustring += "[{";
        		}else{
        			cnustring += ",{";
        		}    		
        		if(jedis.hget(key_cnu, "active").equalsIgnoreCase("1")){
        			cnustring += '"'+ "title" + '"'+":"+'"'+jedis.hget(key_cnu, "mac")+'"'+ ","+'"'+"key"+'"'+":"+'"'
            		+jedis.hget(key_cnu, "mac")+'"'+ ","+'"'+"online"+'"'+":"+'"'+jedis.hget(key_cnu, "active")+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"online.gif"+'"'+ ","+'"'+"type"+'"'+":"+'"'+"cnu"+'"'+ "}";
        		}else{
        			cnustring += '"'+ "title" + '"'+":"+'"'+jedis.hget(key_cnu, "mac")+'"'+ ","+'"'+"key"+'"'+":"+'"'
            		+jedis.hget(key_cnu, "mac")+'"'+ ","+'"'+"online"+'"'+":"+'"'+jedis.hget(key_cnu, "active")+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"offline.png"+'"'+ ","+'"'+"type"+'"'+":"+'"'+"cnu"+'"'+ "}";
        		}
        		
        	}
        	cnustring += "]";
        	//头端下没有终端
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
    		jsonstring = "[{"+'"'+ "title" + '"'+":"+'"'+"EOC设备"+'"'+","+'"'+"key"+ '"'+"eocroot"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true,"+'"'+"children"
			+'"'+":[{"+'"'+ "title" + '"'+":"+'"'+"NOData"+'"'+ "}]"+ "}";
    	}
    	
    	String hfcstring = "";    	
    	list = jedis.keys("hfcid:*:entity");
    	for(Iterator it=list.iterator();it.hasNext();){    		
			if(hfcstring == ""){
        		hfcstring += ",{"+'"'+ "title" + '"'+":"+'"'+"HFC设备"+'"'+","+'"'+"key"+ '"'+"hfcroot"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true,"+'"'+"children"
    			+'"'+":[{";
    		}else{
    			hfcstring += ",{";
    		}    		
    		String key = it.next().toString();   
    		hfcstring += '"'+ "title" + '"'+":"+ '"' + jedis.hget(key, "ip")+ '"' + ","+'"'+"key"+'"'+":"+'"'
    		+jedis.hget(key, "hfcmac")+'"'+ ","+'"'+"online"+'"'+":"+'"'+jedis.hget(key, "active")+'"'+ ","+'"'+"icon"+'"'+":"+'"'+"doc_with_children.gif"+'"'+ ","+'"'+"type"+'"'+":"+'"'+"hfc"+'"'+","+'"'+"children"+'"'+":"+"[{"+
    		'"'+ "title" + '"'+":"+'"'+jedis.hget(key, "logicalid")+'"'+ "},{"+ '"'+ "title" + '"'+":"+'"'+jedis.hget(key, "modelnumber")+'"'+ "},{"+
    		'"'+ "title" + '"'+":"+'"'+jedis.hget(key, "hfctype")+'"'+ "}]}";
    	}
    	if(hfcstring.length()>3){
    		hfcstring += "]}";
    	}
    	hfcstring += "]";
    	if(hfcstring.length()<=3)
    	{
    		hfcstring = ",{"+'"'+ "title" + '"'+":"+'"'+"HFC设备"+'"'+","+'"'+"key"+'"'+":"+'"'+"hfcroot"+'"'+","+'"'+"isFolder"+ '"'+":true,"+'"'+"expand"+ '"'+":true,"+'"'+"children"
			+'"'+":[{"+'"'+ "title" + '"'+":"+'"'+"NOData"+'"'+ "}]"+ "}]";
    	}
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
					logger.info("getsts:::message::=================>>>>cbat");
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
					logger.info("getsts:::message::=================>>>>cnu===="+cbatid);
					if(jsonstring == ""){
		    			jsonstring += "[{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("cnuid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("cnuid:"+message+":entity","active")+'"'+","+'"'+"type"+ '"'+":"+'"'+"cnu"+'"'+","+'"'+"cbatmac"+ '"'+":"+'"'+jedis.hget("cbatid:"+cbatid+":entity","mac")+'"'+"}";
		    			
		    		}else{
		    			jsonstring += ",{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("cnuid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("cnuid:"+message+":entity","active")+'"'+","+'"'+"type"+ '"'+":"+'"'+"cnu"+'"'+","+'"'+"cbatmac"+ '"'+":"+'"'+jedis.hget("cbatid:"+cbatid+":entity","mac")+'"'+"}";
		    		}
					logger.info("getsts:::message::=================>>>>cnu====end");
				}else if(jedis.hgetAll("hfcid:"+message+":entity").toString()!="{}"){
					logger.info("getsts:::message::=================>>>>hfc");
					if(jsonstring == ""){
		    			jsonstring += "[{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("hfcid:"+message+":entity","active")+'"'+","+'"'+"ip"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","ip")+'"'+","+'"'+"type"+ '"'+":"+'"'+"hfc"+'"'+"}";
		    			
		    		}else{
		    			jsonstring += ",{"+'"'+ "mac" + '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","mac")+'"'+","+'"'+"online"+ '"'+":"+'"'
		    			+jedis.hget("hfcid:"+message+":entity","active")+'"'+","+'"'+"ip"+ '"'+":"+'"'+jedis.hget("hfcid:"+message+":entity","ip")+'"'+","+'"'+"type"+ '"'+":"+'"'+"hfc"+'"'+"}";
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
}