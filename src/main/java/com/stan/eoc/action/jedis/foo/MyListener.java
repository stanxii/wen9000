package com.stan.eoc.action.jedis.foo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class MyListener extends JedisPubSub {
	public void onMessage(String channel, String message) {
		System.out.println(channel + "=" + message);
	}

	public void onSubscribe(String channel, int subscribedChannels) {
	}

	public void onUnsubscribe(String channel, int subscribedChannels) {
	}

	public void onPSubscribe(String pattern, int subscribedChannels) {
	}

	public void onPUnsubscribe(String pattern, int subscribedChannels) {
	}

	public void onPMessage(String pattern, String channel, String message) {
	}

	public static void main(String[] args) {
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "10.8.9.237");
		Jedis jedis = pool.getResource();
		
		//Jedis jedis = new Jedis("10.8.9.237");
		
		jedis.publish("foo", "bar123");

		MyListener l = new MyListener();
		jedis.subscribe(l, "foo");

	}
}
