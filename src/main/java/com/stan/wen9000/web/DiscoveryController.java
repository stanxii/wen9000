package com.stan.wen9000.web;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
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

import com.stan.wen9000.domain.Cbat;
import com.stan.wen9000.domain.Cnu;
import com.stan.wen9000.service.CbatService;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

@RequestMapping("/discovery/**")
@Controller
public class DiscoveryController {

	

	private String currentip;
	private static final String TASK_QUEUE_NAME = "discovery_queue";
	
    @Autowired
    CnuController cnuctl;
    
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }

    @RequestMapping
    public String index() {
        return "discovery/index";
    }
    
    @RequestMapping(value = "searchresult",  headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> searchListAll() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
//        List<Cbat> result = cbatctl.cbatService.findAllCbats();
        List<Cnu>  result = cnuctl.cnuService.findAllCnus();
        return new ResponseEntity<String>(Cnu.toJsonArray(result), headers, HttpStatus.OK);
    }
    
    
    @RequestMapping(value = "search",  method = RequestMethod.POST)
    public String searchProduct(@RequestParam(value = "startip", required = false) String st, @RequestParam(value = "stopip", required = false) String end) throws Exception {
        System.out.println("start:"+st + ",end:"+end);
        //quartzRun();
        long longstartIp = IP2Long.ipToLong(st);		
		long longstopIp = IP2Long.ipToLong(end);
		
        ConnectionFactory factory = null;
		Connection connection = null;
		Channel channel = null;
		//This example in Java creates a queue which expires after it has been unused for 30 minutes.
		// Map<String, Object> args = new HashMap<String, Object>();
		//args.put("x-expires", 1800000);
		try {
			factory = new ConnectionFactory();
			factory.setHost("localhost");
			connection = factory.newConnection();			
			//create channel			
			channel = connection.createChannel();

			channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block										
			e.printStackTrace();			
		}
		
		while(longstartIp <= longstopIp ){		
			currentip = IP2Long.longToIP(longstartIp);
			try {

				channel.basicPublish("", TASK_QUEUE_NAME,
						MessageProperties.PERSISTENT_TEXT_PLAIN,
						currentip.getBytes());
				//System.out.println("DiscoveryAction [x] Sent '" + currentip + "'");

				longstartIp++;

			} catch (IOException e) {
				// TODO Auto-generated catch block							
				longstartIp++;	
				e.printStackTrace();
				continue;
			}
			
			
		}
		//////////////////////after loop
		try {	
			channel.close();
			connection.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block										
			e.printStackTrace();
			
		}	
		
        return "discovery/result";
    }
    
    private void quartzRun() throws Exception {

    		// First we must get a reference to a scheduler
            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler sched = sf.getScheduler();
            
         // get a "nice round" time a few seconds in the future...
            long ts = TriggerUtils.getNextGivenSecondDate(null, 15).getTime();
            
         // job3 will run 11 times (run once and repeat 50 more times)
            // job3 will repeat every 10 seconds (50 ms)
            JobDetail job = new JobDetail("job1", "group1", WorkerDiscoveryProcessor.class);
            SimpleTrigger trigger = new SimpleTrigger("trigger1", "group1", "job1", "group1",
                    new Date(ts), null, 50, 50L);
            sched.scheduleJob(job, trigger);
            
            job = new JobDetail("job2", "group1", ServiceDiscoveryProcessor.class);
            trigger = new SimpleTrigger("trigger2", "group1", "job2", "group1",
                    new Date(ts), null, 50, 50L);
            sched.scheduleJob(job, trigger);
            
            sched.start();
            
            sched.shutdown();

    }
    	
   
}
