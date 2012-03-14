package com.stan.wen9000.action.jedis;

import org.springframework.context.ApplicationContext;

import com.stan.wen9000.action.jedis.foo.MyListener;
import com.stan.wen9000.action.jedis.util.RedisUtil;
import com.stan.wen9000.action.jedis.util.SingletonContext;



import redis.clients.jedis.Jedis;



public class SubTest {
	public static void main(String[] args) {
		ApplicationContext ac = SingletonContext.getInstance();
		RedisUtil ru = (RedisUtil) ac.getBean("redisUtil");	
		final Jedis jedis = ru.getConnection();
		final MyListener listener = new MyListener();
		new Thread(new Runnable() {
			@Override
			public void run() {
				//jedis.subscribe(listener, "foo", "watson");
				//jedis.subscribe(listener, new String[]{"whty_foo","whty_test"});
				jedis.psubscribe(listener, new String[]{"whty_*"});
			}
		}).start();
			
		System.out.println("==============");
	}
}
