package cn.superman.sandbox.core.systemOutStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CacheOutputStream extends OutputStream {

	/**
	 * volatile关键字的意义：
	 * 这个关键字是也是为了保证线程同步，不同于sychronized关键字，volatile实质
	 * 并不会给代码块加线程互斥访问锁，而是将数值数据直接写入到内存中，不经过保存在
	 * CPU寄存器这一步骤，所以速度会更快些，依此保证其他线程能立即读到修改后的值，不读
	 * 脏数据，这样就能实现内存数据的可见性。内存的可见性：对于多线程而言，一个线程修改数据
	 * 其他线程能读到正确的数据，这叫做内存数据的可见性。
	 *
	 * 详细内容看Java笔记
	 */
	private volatile ThreadLocal<ByteArrayOutputStream> localBytesCache = new ThreadLocal<ByteArrayOutputStream>() {
		@Override
		protected ByteArrayOutputStream initialValue() {
			return new ByteArrayOutputStream();
		}

	};

	@Override
	public void write(int b) throws IOException {
		ByteArrayOutputStream byteBufferStream = localBytesCache.get();
		byteBufferStream.write(b);
	}

	public byte[] removeBytes(long threadId) {
		ByteArrayOutputStream byteBufferStream = localBytesCache.get();

		if (byteBufferStream == null) {
			return new byte[0];
		}
		byte[] result = byteBufferStream.toByteArray();
		// 因为这个可能以后还可以重用（因为线程时有反复重用的，所以这里只需要将里面的内容清空就可以了）
		// 从内存中取出运行结果后清空输出流，以便下次再利用
		byteBufferStream.reset();
		return result;
	}
}
