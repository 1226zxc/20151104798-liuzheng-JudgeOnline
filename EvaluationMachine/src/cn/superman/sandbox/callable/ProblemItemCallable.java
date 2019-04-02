package cn.superman.sandbox.callable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import cn.superman.sandbox.core.systemInStream.ThreadInputStream;
import cn.superman.sandbox.core.systemOutStream.CacheOutputStream;
import cn.superman.sandbox.dto.ProblemResultItem;

/**
 * 这个类里面的call()方法会运行用户提交的代码。
 * 这个类实现了Callable线程类，此线程的每一个实例对象
 * 将会运行用户提交的Java代码，并会返回封装代码测评结果的
 * 实例对象ProblemResultItem。
 * 代码运行的入口是main方法。如果代码运行中出现
 * 错误或异常将会封装到ProblemResultItem中，并返回
 * 给调用者。提交的代码可能包含键盘输入流，数据来源用户，因此
 * 需要重定向数据输入流，变更为文件输入流。
 */
public class ProblemItemCallable implements Callable<ProblemResultItem> {
	/**
	 * 代码的主函数入口
	 */
	private Method mainMethod;
	private CacheOutputStream resultBuffer;
	/**
	 * 测试用例文件输入流
	 */
	private FileInputStream fileInputStream;
	private ThreadInputStream threadSystemIn;

	/**
	 * ProblemItemCallable构造方法
	 * @param mainMethod 主方法
	 * @param inputFilePath 文件输入路径
	 * @param resultBuffer 结果输出的缓存流
	 * @param threadSystemIn 线程读取流
     */
	public ProblemItemCallable(Method mainMethod, String inputFilePath,
							   CacheOutputStream resultBuffer, ThreadInputStream threadSystemIn) {
		this.mainMethod = mainMethod;
		this.resultBuffer = resultBuffer;
		this.threadSystemIn = threadSystemIn;
		init(inputFilePath);
	}

	private void init(String inputFilePath) {
		File file = new File(inputFilePath);
		if (!file.exists()) {
			throw new RuntimeException("测试数据有问题");
		}
		try {
			fileInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 针对一个测试用例，测试代码的正确性
	 *
	 * @return 针对此测试用例的测试结果
	 * @throws Exception
     */
	@Override
	public ProblemResultItem call() throws Exception {
		ProblemResultItem item = new ProblemResultItem();
		try {
			// 此测试文件输入流绑定到当前线程里，前面已经做了重定向，这里只需绑定一个测试
			// 用例文件到当前线程中即可
			threadSystemIn.setThreadIn(fileInputStream);
			// 通过发射机制方式调用main方法。每个方法都是Method的实例对象
			// 因为主方法是static所以第一个参数是null就可以了，怎么说都会
			// 忽视这个参数。运行主程序时，一旦出现输入数据的操作，就将会从
			// 本线程中的标准输入流获取输入数据，此时标准输入流已经更换为测试
			// 用例文件。程序运行结果将会保存到标准输出流，此时标准输出流已经
			// 重定向为内存，所以程序的运行结果在内存中。
			mainMethod.invoke(null, new Object[] { new String[0] });
			// 从内存中获取运行结果
			item.setResult(new String(resultBuffer.removeBytes(Thread.currentThread().getId())));
			item.setNormal(true);
		} catch (InvocationTargetException e) {
			//代码在调用主方法过程中出错了
			Throwable throwable = e.getTargetException();
			if (throwable instanceof OutOfMemoryError) {
				item.setMessage("内存溢出");
			} else {
				item.setMessage(throwable.getMessage());
			}
			item.setNormal(false);
		} catch (RuntimeException runtimeException) {
			item.setMessage(runtimeException.getMessage());
			item.setNormal(false);
		} finally {
			threadSystemIn.removeAndCloseThreadIn();
		}
		return item;
	}

	public void colseResource() {
		threadSystemIn.removeAndCloseThreadIn();
	}
}
