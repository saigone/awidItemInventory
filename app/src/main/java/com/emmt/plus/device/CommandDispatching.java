package com.emmt.plus.device;

import java.io.IOException;

public class CommandDispatching extends Thread {
	private final CommandChannel mChannel;
	private volatile boolean isRunnungLoop = true;

	public CommandDispatching(CommandChannel channel) {
		mChannel = channel;
	}

	@Override
	public void run() {
		while (isRunnungLoop) {
			try {
				CommandRequest request = mChannel.takeCommand();
				request.execute();
			} catch (IOException e) {
				isRunnungLoop = false;
			} catch (InterruptedException e) {
				isRunnungLoop = false;
			}
		}
	}
	
	public void stopCommandDispatching() {
		isRunnungLoop = false;
		this.interrupt();
	}

}
