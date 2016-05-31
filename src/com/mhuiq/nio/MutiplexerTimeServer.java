package com.mhuiq.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class MutiplexerTimeServer implements Runnable {
	private Selector selector;
	private ServerSocketChannel ssc;
	private volatile boolean stop;
	private Map<Object, String> map = new ConcurrentHashMap<Object, String>();
	private int a = 0;
	
	public MutiplexerTimeServer(int port) {
		try {
			selector = Selector.open();
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.socket().bind(new InetSocketAddress(port), 1024);
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("Server is listening in port :" + port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void stop() {
		this.stop = true;
	}
	
	@Override
	public void run() {
		try {
			while (!stop) {
				selector.select(1000);
				Set<SelectionKey> selectedkeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedkeys.iterator();
				SelectionKey key = null;
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						e.printStackTrace();
						if (null != key) {
							key.cancel();
							if (null != key.channel()) {
								key.channel().close();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != selector) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			if (key.isAcceptable()) {
				ServerSocketChannel ssc1 = (ServerSocketChannel) key.channel();
				SocketChannel sc1 = ssc1.accept();
				sc1.configureBlocking(false);
				sc1.register(selector, SelectionKey.OP_READ);
			}
			if (key.isReadable()) {
				SocketChannel sc1 = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc1.read(readBuffer);
				if (readBytes > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "utf-8");
					
					String str = map.get(sc1) == null ? "" : map.get(sc1);
					str = str + body;
					if (str.length() == 12) {
						map.remove(sc1);
//						System.out.println("Server receive order :" + sc1.hashCode() + " + " + str);
						String response = "hello Client";
						doWrite(sc1, response);
						System.out.println("µÚ" + (++a) + "´Î");
					} else {
						map.put(sc1, str);
					}
					
				} else if (readBytes < 0) {
					key.cancel();
					sc1.close();
				} else {
				}
				
			}
		}
	}
	
	private void doWrite(SocketChannel channel, String response) throws IOException {
		if (null != response && response.trim().length() > 0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer);
		}
	}
}
