package com.stan.wen9000.action.jedis.foo;

import redis.clients.jedis.Jedis;

public class Foo {
	public static void main(String[] args) {
		Jedis jedis = new Jedis("10.8.9.237");
		jedis.set("foo", "bar");
		String value = jedis.get("foo");
		
		System.out.println(value);
	}
}
