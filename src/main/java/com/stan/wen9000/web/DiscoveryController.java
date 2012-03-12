package com.stan.wen9000.web;



import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.stan.wen9000.domain.Cnu;

@RequestMapping("/discovery/**")
@Controller
public class DiscoveryController {

	private String currentip;	
	private static final String DISCOVERY_QUEUE_NAME = "discovery_queue";
	
	private static JedisPool pool;
	 
	 static {
	        JedisPoolConfig config = new JedisPoolConfig();
	        config.setMaxActive(100);
	        config.setMaxIdle(20);
	        config.setMaxWait(1000);
	        config.setTestOnBorrow(true);
	        pool = new JedisPool(config, "192.168.1.249");
	    }
	 

	@Autowired
	CnuController cnuctl;

	@RequestMapping(method = RequestMethod.POST, value = "{id}")
	public void post(@PathVariable Long id, ModelMap modelMap,
			HttpServletRequest request, HttpServletResponse response) {
	}

	@RequestMapping
	public String index() {
		return "discovery/index";
	}

	@RequestMapping(value = "searchresult", headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> searchListAll() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json; charset=utf-8");
		// List<Cbat> result = cbatctl.cbatService.findAllCbats();
		List<Cnu> result = cnuctl.cnuService.findAllCnus();
		return new ResponseEntity<String>(Cnu.toJsonArray(result), headers,
				HttpStatus.OK);
	}

	@RequestMapping(value = "search", method = RequestMethod.POST)
	public String searchProduct(
			@RequestParam(value = "startip", required = false) String st,
			@RequestParam(value = "stopip", required = false) String end)
			throws Exception {
		System.out.println("start:" + st + ",end:" + end);

		long longstartIp = IP2Long.ipToLong(st);
		long longstopIp = IP2Long.ipToLong(end);
		
		Jedis jedis = pool.getResource();
		
		while (longstartIp <= longstopIp) {
			currentip = IP2Long.longToIP(longstartIp);

			
			jedis.lpush(DISCOVERY_QUEUE_NAME, currentip);
			// System.out.println("DiscoveryAction [x] Sent '" + currentip +
			// "'");

			longstartIp++;

		}

		

		return "discovery/result";
	}

}
