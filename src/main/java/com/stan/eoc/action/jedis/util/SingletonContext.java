package com.stan.eoc.action.jedis.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Spring的ApplicationContext对象
 * 
 * @author 徐旺胜
 * @date 2012-1-8 下午04:31:09
 */
public final class SingletonContext {
	private static ApplicationContext ac = null;
	
	private SingletonContext() {}

	/**
	 * ----懒汉式单例
	 * 1.单例在并发访问并调用其相应的GetInstance方法的时候也会造成创建多个实例对象，加锁是必要的。
	 * 2.使用synchronized是比较好的解决方案，优点是代码简洁，缺点是在抛出异常的时候不能处理维护使
	 *    系统处于良好状态。
	 * 3.显示的lock设定是良好的解决方案。
	 */
	private static Lock lock = new ReentrantLock();
	public static ApplicationContext getInstance() {
		if(ac == null) {
			lock.lock();
			if(ac == null) {
				try{
					ac = new ClassPathXmlApplicationContext("beans-config.xml");
				} finally {
					lock.unlock();
				}
			}
		}
		return ac;
	}
}