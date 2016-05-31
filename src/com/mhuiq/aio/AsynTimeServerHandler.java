package com.mhuiq.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;


public class AsynTimeServerHandler implements Runnable {
	private int port;
	
	CountDownLatch latch ;
	
	AsynchronousServerSocketChannel asynchronousSocketChannel ;

	public AsynTimeServerHandler(int port) {
		this.port = port;
		try {
			asynchronousSocketChannel = AsynchronousServerSocketChannel.open();
			asynchronousSocketChannel.bind(new InetSocketAddress(port));
			System.out.println("server is listening in :" + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		latch = new CountDownLatch(1);
		doAccept();
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private void doAccept() {
		asynchronousSocketChannel.accept(this, new AcceptCompletionHandle());
	}
	
	
}

class AcceptCompletionHandle implements CompletionHandler<AsynchronousSocketChannel, AsynTimeServerHandler> {

	@Override
	public void failed(Throwable exc, AsynTimeServerHandler attachment) {
		exc.printStackTrace();
		attachment.latch.countDown();
	}

	@Override
	public void completed(AsynchronousSocketChannel result, AsynTimeServerHandler attachment) {
		attachment.asynchronousSocketChannel.accept(attachment, this);
		ByteBuffer bufffer = ByteBuffer.allocate(1024);
		result.read(bufffer, bufffer, new ReadCompletionHandle(result));
	}
	
}