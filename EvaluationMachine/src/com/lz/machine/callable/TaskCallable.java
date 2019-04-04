package com.lz.machine.callable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.lz.machine.core.systemInStream.ThreadInputStream;
import com.lz.machine.core.systemOutStream.CacheOutputStream;
import com.lz.machine.dto.Task;
import com.lz.machine.dto.TaskItemResult;

/**
 * 根据多组测试用例测评代码的线程规则定义类
 *
 * @author 刘铮
 */
public class TaskCallable implements Callable<List<TaskItemResult>> {
	/**
	 * 运行代码的主方法
	 */
	private Method mainMethod;

	/**
	 * 包含此项任务的要求信息
	 */
	private Task task;
	/**
	 * 重定向后的标准输出流
	 */
	private CacheOutputStream resultBuffer;

	private Runtime run = null;

	/**
	 * 相当于一个计数器，只有值为0才可以继续执行此线程
	 */
	private CountDownLatch countDownLatch = null;

	/**
	 * 重定向后的标准输入流
	 */
	private ThreadInputStream threadSystemIn;
	private static final ExecutorService ITEM_GET_THREAD_POOL = Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setName("ITEM_GET_THREAD_POOL id "
							+ System.currentTimeMillis());
					return thread;
				}
			});
	/**
	 * 处理一个代码每一个测试用例的线程池，这样所有的测试用例可以并发测试
	 */
	private static final ExecutorService ITEM_EXEC_THREAD_POOL = Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("ITEM_EXEC_THREAD_POOL id " + System.currentTimeMillis());
			return thread;
		}
	});

	/**
	 * 构造一个处理问题的线程
	 *
	 * @param mainMethod     目标代码主方法的类型
	 * @param task        描述这个问题信息的实例
	 * @param resultBuffer   重定向后的标准输出流
	 * @param threadSystemIn 重定向后的标准输入流
	 */
	public TaskCallable(Method mainMethod, Task task,
						CacheOutputStream resultBuffer, ThreadInputStream threadSystemIn) {
		this.mainMethod = mainMethod;
		this.task = task;
		this.resultBuffer = resultBuffer;
		this.threadSystemIn = threadSystemIn;
		run = Runtime.getRuntime();
	}

	/**
	 * 测验代码
	 *
	 * 此方法为多线程测验代码的规则
	 * @return 返回每一个测试用例的测评结果
	 * @throws Exception
	 */
	@Override
	public List<TaskItemResult> call() throws Exception {
		// 获取测试用例文件
		List<String> paths = task.getInputDataFilePathList();
		final List<TaskItemResult> resultItems = new ArrayList<TaskItemResult>();
		//设置计数器，计数值为所有测试用例的个数
		countDownLatch = new CountDownLatch(paths.size());
		// 为了内存使用比较准确，先大概的执行一次回收吧
		run.gc();

		// 遍历此问题的每一个测试用例文件
		for (int i = 0; i < paths.size(); i++) {
			// 获取每一个测试用例文件
			final String path = paths.get(i);
			ITEM_EXEC_THREAD_POOL.execute(new Runnable() {
				@Override
				public void run() {
					resultItems.add(process(path));
				}
			});
		}

		// 阻塞线程，等待所有结果都计算完了，再返回
		countDownLatch.await();
		return resultItems;
	}

	/**
	 * 测评一个问题的代码的一个测试用例，返回这个测试用例的测评结果
	 *
	 * @param inputFilePath 测试数据文件路径
	 * @return 返回封装了测评结果的ProblemResultItem实例
	 */
	private TaskItemResult process(String inputFilePath) {
		TaskItemResult item = null;
		ProblemItemCallable itemCallable = null;
		long beginMemory = 0;
		long beginTime = 0;
		long endTime = 0;
		long endMemory = 0;
		Future<TaskItemResult> submit = null;

		try {
			itemCallable = new ProblemItemCallable(mainMethod, inputFilePath, resultBuffer, threadSystemIn);
			// 像这个内存池中添加线程对象，并返回等待的结果submit，而不是运行完成结果。submit是Future的实例对象
			submit = ITEM_GET_THREAD_POOL.submit(itemCallable);
			// 没有执行代码前，获得当前JVM已使用的内存情况
			beginMemory = run.totalMemory() - run.freeMemory();
			beginTime = System.nanoTime();

			// 最多等待两毫秒获取计算结果，怕没有计算完成就返回结果
			item = submit.get(task.getTimeLimit() + 2, TimeUnit.MILLISECONDS);

			// 针对测试用例，代码运行超时，强行关闭这个测试用例线程
			if (item == null) {
				killThread((FutureTask<TaskItemResult>) submit);
				throw new TimeoutException();
			}

			endTime = System.nanoTime();
			//获得结束时候的已使用的内存
			endMemory = run.totalMemory() - run.freeMemory();
		} catch (Exception e) {
			// 出现了意外，先关闭资源再说（如已经打开的流等）
			itemCallable.colseResource();
			killThread((FutureTask<TaskItemResult>) submit);
			item = new TaskItemResult();
			item.setNormal(false);
			if (e instanceof CancellationException || e instanceof TimeoutException) {
				// 超时了，会进来这里
				item.setMessage("超时");
			} else {
				item.setMessage(e.getMessage());
			}
			endTime = System.nanoTime();
			endMemory = run.totalMemory() - run.freeMemory();
		}
		// 时间为毫微秒，要先转变为微秒再变为毫秒
		item.setUseTime((endTime - beginTime) / 1000 / 1000);
		// 这样就能获取到运行时使用的内存量
		item.setUseMemory(endMemory - beginMemory);
		item.setInputFilePath(inputFilePath);
		//与实际使用的内存和题目规定的内存限制比较
		if (item.getUseMemory() > task.getMemoryLimit()) {
			item.setNormal(false);
			item.setMessage("超出内存限制");
		}
		// 无论怎么样，这里必须最后都要进行减一，不然将会一直阻塞线程，最终无法返回结果
		countDownLatch.countDown();
		return item;
	}

	/**
	 * 关闭指定的线程
	 *
	 * 需要注意的是，这里将会调用线程stop方法，因为只有这样才能强行终止超时的线程，而又因为这里并不需要保证什么原子性以及一致性的业务要求，
	 * 所以用stop方法是没什么大问题的
	 *
	 * @param submit 需要关闭的线程
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("deprecation")
	private void killThread(FutureTask<TaskItemResult> submit) {
		try {
			submit.cancel(true);
			// 利用反射，强行取出正在运行该任务的线程
			Field runner = submit.getClass().getDeclaredField("runner");
			runner.setAccessible(true);
			Thread execThread = (Thread) runner.get(submit);
			execThread.stop();
			submit.cancel(true);
		} catch (Exception e) {
			System.err.println(e);
		}

	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
}
