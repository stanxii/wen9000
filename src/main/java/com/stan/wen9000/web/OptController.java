package com.stan.wen9000.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.stan.wen9000.action.jedis.util.RedisUtil;


@RequestMapping("/opts/**")
@Controller
public class OptController {
	private static Logger logger = Logger.getLogger(OptController.class);
	private static JedisPool pool;
	private static RedisUtil redisUtil;
	private static SnmpUtil util = new SnmpUtil();
	
	 public static RedisUtil getRedisUtil() {
			return redisUtil;
		}

		public static void setRedisUtil(RedisUtil redisUtil) {
			OptController.redisUtil = redisUtil;
		}
	
	static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(100);
        config.setMaxIdle(20);
        config.setMaxWait(1000);
        config.setTestOnBorrow(true);
        pool = new JedisPool(config, "127.0.0.1");
    }
	
	@RequestMapping(value="/allcnus")
	@ResponseBody
	public void Getallcnus(HttpServletRequest request, HttpServletResponse response){
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
    	int i =0;
	    String jsonstring = "";
	    String result = "";
        try {                        
            Set<String> list = jedis.keys("cnuid:*:entity");
            for(Iterator it = list.iterator(); it.hasNext(); ) {
            	i++;
        		if(jsonstring == ""){
        			jsonstring += "{"+'"'+ "cnu"+i + '"'+":{";
        		}else{
        			jsonstring += ","+'"'+ "cnu"+i + '"'+":{";
        		}
            	String prokey = (String) it.next();
            	int index1 = prokey.indexOf(':') +1;
        		int index2 = prokey.lastIndexOf(':');
        		String cid = prokey.substring(index1, index2);
        		//判断key是否存在
        		if(jedis.exists("global:checkedcnus")){
        			//判断是否checked
                	if(jedis.sismember("global:checkedcnus", cid)){
                		jsonstring += '"'+ "check" + '"'+":"+ '"' + "<input type=checkbox class=chk checked />"+ '"' + ",";
                	}else{
                		jsonstring += '"'+ "check" + '"'+":"+ '"' + "<input type=checkbox class=chk />"+ '"' + ",";
                	}
        		}else{
        			jsonstring += '"'+ "check" + '"'+":"+ '"' + "<input type=checkbox class=chk />"+ '"' + ",";
        		}
            	
            	
        		jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(prokey, "mac")+ '"' + ",";
        		jsonstring += '"'+ "active"+ '"'+":" + '"' + jedis.hget(prokey, "active")+ '"' + ",";
        		jsonstring += '"'+ "label" + '"'+":"+ '"' + jedis.hget(prokey, "label")+ '"' + ",";
        		switch(Integer.parseInt(jedis.hget(prokey, "devicetype")))
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
        		jsonstring += '"'+ "proname"+ '"'+":" + '"' + jedis.hget("profileid:"+jedis.hget(prokey, "profileid")+":entity", "profilename")+ '"' + ",";
        		jsonstring += '"'+ "contact" + '"'+":"+ '"' + jedis.hget(prokey, "contact")+ '"' + ",";
        		jsonstring += '"'+ "phone"+ '"'+":" + '"' + jedis.hget(prokey, "phone")+ '"' + "}";
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
	
	@RequestMapping(value="/allcheckedcnus")
	@ResponseBody
	public void Getcheckedcnus(HttpServletRequest request, HttpServletResponse response){
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Jedis jedis = pool.getResource();
	    String jsonstring = "";
	    String result = "";
        try {                        
            Set<String> list = jedis.smembers("global:checkedcnus");
            for(Iterator it = list.iterator(); it.hasNext(); ) {
            	String cnuid = (String) it.next();
        		if(jsonstring == ""){
        			jsonstring += "{"+'"'+ "cnu"+cnuid + '"'+":{";
        		}else{
        			jsonstring += ","+'"'+ "cnu"+cnuid + '"'+":{";
        		}
            	
        		String cnukey = "cnuid:"+cnuid+":entity";
        		jsonstring += '"'+ "mac" + '"'+":"+ '"' + jedis.hget(cnukey, "mac")+ '"' + ",";
        		jsonstring += '"'+ "active"+ '"'+":" + '"' + jedis.hget(cnukey, "active")+ '"' + ",";
        		jsonstring += '"'+ "label" + '"'+":"+ '"' + jedis.hget(cnukey, "label")+ '"' + ",";
        		switch(Integer.parseInt(jedis.hget(cnukey, "devicetype")))
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
        		jsonstring += '"'+ "contact" + '"'+":"+ '"' + jedis.hget(cnukey, "contact")+ '"' + ",";
        		jsonstring += '"'+ "phone"+ '"'+":" + '"' + jedis.hget(cnukey, "phone")+ '"' + "}";
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

	@RequestMapping(value="/send_config", method=RequestMethod.POST)
	@ResponseBody
	public void senConfig(HttpServletRequest request, HttpServletResponse response){
		Jedis jedis = pool.getResource();
		String proid = request.getParameter("proid");		

		//获取要配置的CNU
		Set<String> list = jedis.smembers("global:checkedcnus");
		String cnukey="";		
		for(Iterator it = list.iterator(); it.hasNext();){
			String cnuid = it.next().toString();
			cnukey = "cnuid:"+cnuid+":entity";

			//获取cnu在头端上的索引
			String cnuindex = jedis.hget(cnukey, "cnuindex");
			//获取所属头端信息
			String cid = jedis.hget(cnukey, "cbatid");
			String cip = jedis.hget("cbatid:"+cid+":entity", "ip");
			
			//下面是具体节点配置过程或发往其它进程进行异步配置
			//判断设备是否在线
			try {
				String tmp = util.getStrPDU(cip, "161", new OID(new int[] {1,3,6,1,4,1,36186,8,2,6,0}));
				if(tmp == ""){
					//将配置失败的设备id发往队列
					jedis.lpush("Config_failed", cnuid);
					continue;
				}else{
					//发送配置
					if(!sendconfig(Integer.valueOf(proid),cip,Integer.valueOf(cnuindex),jedis)){
						//发送失败
						//将配置失败的设备id发往队列
						jedis.lpush("Config_failed", cnuid);
						continue;
					}
				}
			} catch (IOException e) {

				e.printStackTrace();
				continue;
			}
			
			//将配置成功的设备id发往队列
			jedis.lpush("Config_success", cnuid);
			
			//获取cnu原profileid号
			String old_proid = jedis.hget(cnukey, "profileid");
			//删除原profile集合中此CNU
			jedis.srem("profileid:"+old_proid+":cnus", cnuid);
			//更改CNU模板号
			jedis.hset(cnukey, "profileid", proid);
			//添加cnu到新profile集合中
			jedis.sadd("profileid:"+proid+":cnus", cnuid);
			
			//删除global:checkedcnus集合中此cnu
			jedis.srem("global:checkedcnus", cnuid);
			
			//保存数据到硬盘
			jedis.save();
		}
		pool.returnResource(jedis);
	}
	
	private Boolean sendconfig(int proid,String cbatip, int cnuindex,Jedis jedis ){
		String prokey = "profileid:"+proid+":entity";
		try{
			
			//vlansts
		util.setV2PDU(cbatip,
			"161",
			new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,36,cnuindex}), 
			new Integer32(Integer.valueOf(jedis.hget(prokey, "vlanen")))
		);
		//p0vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,37,cnuindex}), 
				new Integer32(Integer.valueOf(jedis.hget(prokey, "vlan0id"))==0?1:Integer.valueOf(jedis.hget(prokey, "vlan0id")))
		);
		
		//p1vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,38,cnuindex}), 
				new Integer32(Integer.valueOf(jedis.hget(prokey, "vlan1id"))==0?1:Integer.valueOf(jedis.hget(prokey, "vlan1id")))
				);
		
		//p2vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,39,cnuindex}), 
				new Integer32(Integer.valueOf(jedis.hget(prokey, "vlan2id"))==0?1:Integer.valueOf(jedis.hget(prokey, "vlan2id")))
				);
		
		//p3vid
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,40,cnuindex}), 
				new Integer32(Integer.valueOf(jedis.hget(prokey, "vlan3id"))==0?1:Integer.valueOf(jedis.hget(prokey, "vlan3id")))
				);
		
		//if(pro.getTxlimitsts() != 0 ){			
			//cpuport tx sts
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,52,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "txlimitsts")))
					);
			
			//cpuport tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,53,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "cpuporttxrate")))
					);
			//eth1 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,54,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port0rxrate")))
					);
			//eth2 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,55,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port1rxrate")))
					);
			//eth3 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,56,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port2rxrate")))
					);
			//eth4 tx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,57,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port3rxrate")))
					);
		//}
		/*else{
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,52,cnuindex}), 
					new Integer32(0)
					);
		}*/
		
		//if(pro.getRxlimitsts() != 0){
			//rx sts
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,46,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "rxlimitsts")))
					);
			//cpuport rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,47,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "cpuportrxrate")))
					);
			//eth1 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,48,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port0txrate")))
					);
			//eth2 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,49,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port1txrate")))
					);
			//eth3 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,50,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port2txrate")))
					);
			//eth4 rx
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,51,cnuindex}), 
					new Integer32(Integer.valueOf(jedis.hget(prokey, "port3txrate")))
					);
		/*}else{
			//rx sts
			util.setV2PDU(cbatip,
					"161",
					new OID(new int[] {1,3,6,1,4,1,36186,8,8,1,46,cnuindex}), 
					new Integer32(0)
					);
		}*/
		//reload profile
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,1,1,13,cnuindex}), 
				new Integer32(2)
				);	
		
		util.setV2PDU(cbatip,
				"161",
				new OID(new int[] {1,3,6,1,4,1,36186,8,1,1,13,cnuindex}), 
				new Integer32(3)
				);
		if(proid == 1)
		{
			//销户
			util.setV2PDU(cbatip,
			"161",
			new OID(new int[] {1,3,6,1,4,1,36186,8,1,1,13,cnuindex}), 
			new Integer32(4)
			);
			//return true;
		}
			return true;
		
		
		}catch(Exception e)
		{
			System.out.println("=============================>sendconfig error");
			//e.printStackTrace();
			return false;
		
		}
	}
	
	@RequestMapping(value="/get_globalinfo")
	@ResponseBody
	public void getGlobalinfo(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
		Jedis jedis = pool.getResource();
		String jsonstring = "";
		String trapserver_ip = jedis.get("global:trapserver:ip");
		String trapserver_port = jedis.get("global:trapserver:port");
		
		jsonstring = "{" + '"' + "trapserver_ip" + '"' + ":" + '"' + trapserver_ip + '"' + "," + '"' +
		"trapserver_port" + '"' + ":" + '"' + trapserver_port + '"' + "}";
		
		pool.returnResource(jedis);
		PrintWriter out = response.getWriter();
        //logger.info("keys::::::"+ jsonstring);
        out.println(jsonstring);  
        out.flush();  
        out.close();
	}
	
	@RequestMapping(value="/save_globalinfo")
	@ResponseBody
	public void saveGlobalinfo(HttpServletRequest request, HttpServletResponse response) throws IOException{
		Jedis jedis = pool.getResource();
		
		String trapserver_ip = request.getParameter("ip");
		String trapserver_port = request.getParameter("port");
		
		jedis.set("global:trapserver:ip", trapserver_ip);
		jedis.set("global:trapserver:port", trapserver_port);

		jedis.save();
		pool.returnResource(jedis);

	}
	
	@RequestMapping(value="/get_queueinfo")
	@ResponseBody
	public void getQueueinfo(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
		Jedis jedis = pool.getResource();
		String jsonstring = "";
		long heartnum = jedis.llen("heart_queue");
		long alarmnum = jedis.llen("alarm_queue");
		long cbatnum = jedis.llen("cbatsts_queue");
		
		jsonstring = "{" + '"' + "heartnum" + '"' + ":" + '"' + String.valueOf(heartnum) + '"' + "," + '"' +
		"alarmnum" + '"' + ":" + '"' + String.valueOf(alarmnum) + '"'+ "," + '"' +
		"cbatnum" + '"' + ":" + '"' + String.valueOf(cbatnum) + '"' + "}";
		
		pool.returnResource(jedis);
		PrintWriter out = response.getWriter();
        //logger.info("keys::::::"+ jsonstring);
        out.println(jsonstring);  
        out.flush();  
        out.close();
	}
	
	@RequestMapping(value="/getsuccess")
	@ResponseBody
	public void getsuccess(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
		Jedis jedis = pool.getResource();
		String jsonstring = "";
		String cnuid="";
		long num = jedis.llen("Config_success");
		String s_num = String.valueOf(num);
		int i_num = Integer.valueOf(s_num);
		int index = 0;
		for(int i=0;i<i_num;i++)
		{
			cnuid = jedis.rpop("Config_success");
			String key = "cnuid:"+cnuid+":entity";
    		index++;
    		if(jsonstring == ""){
    			jsonstring += "{"+'"'+ "cnu"+index + '"'+":{";
    		}else{
    			jsonstring += ","+'"'+ "cnu"+index + '"'+":{";
    		}

    		jsonstring += '"'+ "active" + '"'+":"+ '"' + jedis.hget(key, "active")+ '"' + ",";
    		jsonstring += '"'+ "mac"+ '"'+":" + '"' + jedis.hget(key, "mac")+ '"' + ",";

    		jsonstring += '"'+ "label"+ '"'+":"+ '"'  + jedis.hget(key, "label")+ '"' + "}";
    	
		}
		jsonstring += "}";
		if(jsonstring.length()<4){
			jsonstring = "";
		}
		pool.returnResource(jedis);
		PrintWriter out = response.getWriter();
        //logger.info("keys::::::"+ jsonstring);
        out.println(jsonstring);  
        out.flush();  
        out.close();
	}
	
	@RequestMapping(value="/getfailed")
	@ResponseBody
	public void getfailed(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
		Jedis jedis = pool.getResource();
		String jsonstring = "";
		String cnuid="";
		long num = jedis.llen("Config_failed");
		String s_num = String.valueOf(num);
		int i_num = Integer.valueOf(s_num);
		int index = 0;
		for(int i=0;i<i_num;i++)
		{
			cnuid = jedis.rpop("Config_failed");
			String key = "cnuid:"+cnuid+":entity";
    		index++;
    		if(jsonstring == ""){
    			jsonstring += "{"+'"'+ "cnu"+index + '"'+":{";
    		}else{
    			jsonstring += ","+'"'+ "cnu"+index + '"'+":{";
    		}

    		jsonstring += '"'+ "active" + '"'+":"+ '"' + jedis.hget(key, "active")+ '"' + ",";
    		jsonstring += '"'+ "mac"+ '"'+":" + '"' + jedis.hget(key, "mac")+ '"' + ",";

    		jsonstring += '"'+ "label"+ '"'+":"+ '"'  + jedis.hget(key, "label")+ '"' + "}";
    	
		}
		jsonstring += "}";
		if(jsonstring.length()<4){
			jsonstring = "";
		}
		pool.returnResource(jedis);
		PrintWriter out = response.getWriter();
        //logger.info("keys::::::"+ jsonstring);
        out.println(jsonstring);  
        out.flush();  
        out.close();
	}
	
	@RequestMapping(value="/clr_heartqueue")
	@ResponseBody
	public void clrheart(HttpServletRequest request, HttpServletResponse response) throws IOException{
		Jedis jedis = pool.getResource();
		
		long num = jedis.llen("heart_queue");
		jedis.ltrim("heart_queue", num, num);
		//jedis.save();
		pool.returnResource(jedis);

	}
	
	@RequestMapping(value="/clr_alarmqueue")
	@ResponseBody
	public void clralarm(HttpServletRequest request, HttpServletResponse response) throws IOException{
		Jedis jedis = pool.getResource();
		
		long num = jedis.llen("alarm_queue");
		jedis.ltrim("alarm_queue", num, num);
		//jedis.save();
		pool.returnResource(jedis);

	}
	
	@RequestMapping(value="/clr_cbatqueue")
	@ResponseBody
	public void clrcbat(HttpServletRequest request, HttpServletResponse response) throws IOException{
		Jedis jedis = pool.getResource();
		
		long num = jedis.llen("cbatsts_queue");
		jedis.ltrim("cbatsts_queue", num, num);
		//jedis.save();
		pool.returnResource(jedis);

	}
}