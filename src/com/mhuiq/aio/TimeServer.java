package com.mhuiq.aio;

public class TimeServer {

	public static void main(String[] args) {
		AsynTimeServerHandler timeServer = new AsynTimeServerHandler(8080);
		new Thread(timeServer, "AIO SERVER").start();;
	}
}
