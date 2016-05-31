package com.mhuiq.nio;

public class TimeServer {
	
	public static void main(String[] args) {
		MutiplexerTimeServer mts = new MutiplexerTimeServer(8080);
		new Thread(mts, "NIO-TimeServer").start();
	}
}
