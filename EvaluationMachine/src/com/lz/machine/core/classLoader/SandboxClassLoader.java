package com.lz.machine.core.classLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SandboxClassLoader extends ClassLoader {
	private String classPath = null;

	public SandboxClassLoader(String classPath) {
		super();
		this.classPath = classPath;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return loadSandboxClass(name);
	}

	/**
	 * 加载沙箱类。
	 * 加载的实质就是通过输入流的操作把class文件的二进制数据读入内存中。
	 * 该类返回的是对指定加载类的实例类Class类
	 * @param name 需要加载的类名
	 * @return 返回加载完成的类的实例Class
	 * @throws ClassNotFoundException
     */
	public Class<?> loadSandboxClass(String name) throws ClassNotFoundException {
		String classFilePath = classPath + File.separator + name + ".class";
		FileInputStream inputStream = null;
		try {
			File file = new File(classFilePath);
			inputStream = new FileInputStream(file);
			byte[] classByte = new byte[(int) file.length()];
			inputStream.read(classByte);

			//这个方法是继承下来的，通过这个方法就能定义一个类，类名取决于name，类结构就是classByte字节数组的内容
			return defineClass(name, classByte, 0, classByte.length);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}

		return null;
	}
}
