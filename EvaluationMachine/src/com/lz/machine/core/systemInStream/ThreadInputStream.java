package com.lz.machine.core.systemInStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * 创建线程输入流类，该类继承了InputStream类
 * 线程输入流类使用的是ThreadLocal类，这样使得为每一个线程
 * 保存独立的数据，每个线程之间都是独立的。由于被设置的数据
 * 是和ThreadLocal绑定在一起的，所以在任何地点都可以之间通过
 * ThreadLocal类中的get方法获取到绑定到线程的数据。
 * 实质是每个线程都有一份自己线程独立共享的变量Map，ThreadLocal
 * 只不过是一个入口，使其能够访问到线程中绑定的线程本地变量。
 */
public class ThreadInputStream extends InputStream {
	/**
	 * 创建一个线程本地变量实例对象localIn用于保存输入流引用。这个个引用将会
	 * 存在这个Thread从生到死，一直都绑定在这个线程中，在任何地方都可以获取这个输入流
	 */
	private volatile ThreadLocal<InputStream> localIn = new ThreadLocal<InputStream>();


	/**
	 * 读取当前线程中保存的InputStream中的值
	 * 该值是下一个输入流中下一个数据的字节数形式
	 * 例如：下一个数据是字符'a'那么对应的56就会被返回
	 * @return 返回下一个数据的字节数形式
	 * @throws IOException
     */
	@Override
	public int read() throws IOException {
		return localIn.get().read();
	}

	/**
	 * 将输入流引用绑定到当前线程中，通过使用ThreadLocal的Set方法。
	 * 实质是每个线程都有一份自己线程独立共享的变量Map，ThreadLocal
	 * 只不过是一个入口，能够访问到线程中绑定的线程本地变量。
	 * @param in
     */
	public void setThreadIn(InputStream in) {
		localIn.set(in);
	}

	/**
	 * 从这个线程中移除绑定的输入流引用，并关闭流
	 */
	public void removeAndCloseThreadIn() {
		InputStream stream = localIn.get();
		localIn.remove();
		try {
			if (stream != null) {
				stream.close();
				stream = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
