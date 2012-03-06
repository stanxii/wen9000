package com.stan.quartz;
 
public class RunMeJob 
{
	public void printMe() {
		System.out.println("Run Me~~~ Yeah!~~~~~");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Run Me Again~~~Oh~ Yeah!~~~~~");		
	}
	
	
}