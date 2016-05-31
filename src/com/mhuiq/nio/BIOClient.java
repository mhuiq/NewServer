package com.mhuiq.nio;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOClient {
	private final static int THREAD_NUMS = 120000;

	private static CountDownLatch latch = new CountDownLatch(THREAD_NUMS);
	
	private static class Task implements Runnable {

		@Override
		public void run() {
			try {
				long startTime = System.currentTimeMillis();
				Socket socket = new Socket("99.0.37.144", 8080);
				OutputStream os = socket.getOutputStream();
				byte[] request = "hello server".getBytes();
				int remains = request.length;
				for (int i=0; i<remains; ++i) {
					os.write(request, i, 1);
//					Thread.sleep(100l);
				}
				os.flush();
				InputStream is = socket.getInputStream();
				byte[] bytes = new byte[1024];
				is.read(bytes);
//				System.out.println(new String(bytes, "utf-8"));
				os.close();
				long wasteTime = System.currentTimeMillis() - startTime;
				if (wasteTime > 100) {
					System.out.println("共消耗时间：" + wasteTime + "ms");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		}
		
	}
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		ExecutorService pools = Executors.newFixedThreadPool(20);
		for (int i=0; i<THREAD_NUMS; ++i) {
//			Thread t = new Thread(task);
//			t.start();
//			t.join();
			pools.execute(new Task());
//			Thread.sleep(100l);
		}
		latch.await();
		System.out.println("耗时：" + (System.currentTimeMillis() - startTime));
	}
	
}
