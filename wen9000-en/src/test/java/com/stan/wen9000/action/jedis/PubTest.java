package com.stan.wen9000.action.jedis;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.stan.wen9000.action.jedis.util.RedisUtil;
import com.stan.wen9000.action.jedis.util.SingletonContext;



import redis.clients.jedis.Jedis;



public class PubTest {
	public static void main(String[] args) {
		ApplicationContext ac = SingletonContext.getInstance();
		//ApplicationContext ac = new ClassPathXmlApplicationContext("beans-config.xml");
		RedisUtil ru = (RedisUtil) ac.getBean("redisUtil");	
		Jedis jedis = ru.getConnection();
		jedis.publish("whty_foo", "bar123");
		jedis.publish("whty_test", "hello watson");
	}
}
