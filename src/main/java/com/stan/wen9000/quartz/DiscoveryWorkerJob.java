package com.stan.wen9000.quartz;



import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DiscoveryWorkerJob {

	private int timeout;
	private int i=0;
	
	
	
    private static Logger _log = LoggerFactory.getLogger(DiscoveryWorkerJob.class);

    
    public DiscoveryWorkerJob() {
    }
    
    

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}




	public void doIt() {

        // Say Hello to the World and display the date/time
        _log.info("Descovery Working doing fucking begin...... World! - " + new Date());
        
        System.out.println("fuckingdongin me.... starting....." + i);
		i++;
		ping("3.4.5.6");
		
		System.out.println("fuckingdongin me.... over" );
        
        _log.info("Descovery Working doing fucking end...... World! - " + new Date());
    }
	
	static Boolean ping(String ip) {
		int timeOut = 3000; // I recommend 3 seconds at least

		try {
			if (ip.length() <= 0) {
				System.out.println("ip address is error ping fun...");
				return false;
			}
			InetAddress address = InetAddress.getByName(ip);
			Boolean status = address.isReachable(timeOut);
			System.out.println("ping " + ip + " ........>result is,    "
					+ status);

			return status;
		} catch (UnknownHostException e) {
			//e.printStackTrace();
			//System.out
			//		.println("ping [#0] ..... UnknownHostException ......result is false"
			//				+ ip);
			return false;
		} catch (IOException e) {
			//e.printStackTrace();
			//System.out
			//		.println("ping [#0] .. IOException .........result is false"
			//				+ ip);
			return false;
		}

	}

	
	
    
}
