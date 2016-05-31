package com.mhuiq.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;



public class ReadCompletionHandle implements CompletionHandler<Integer, ByteBuffer> {
	private static AtomicInteger ai = new AtomicInteger(0);
	private AsynchronousSocketChannel channel;
	
	public ReadCompletionHandle(AsynchronousSocketChannel channel) {
		if (null == this.channel) {
			this.channel = channel;
		}
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		attachment.flip();
		byte[] body = new byte[attachment.remaining()];
		attachment.get(body);
		try {
			String req = new String(body, "utf-8");
			System.out.println("server receive :" + req);
			String response = "hello client";
			doWrite(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doWrite(String response) {
		byte[] resp = response.getBytes();
		ByteBuffer buff = ByteBuffer.allocate(resp.length);
		buff.put(resp);
		buff.flip();
		channel.write(buff, buff, new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				
				if (attachment.hasRemaining()) {
					channel.write(attachment, attachment, this);
				}
				System.out.println(ai.addAndGet(1));
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	
	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
