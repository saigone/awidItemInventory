package com.emmt.plus.device;

public class CommandChannel {
	private static final int MAX_REQUEST = 100;
	private int mTail;
	private int mHead;
	private int mCount;
	private final CommandDispatching mCommandDispatching;
	private CommandRequest[] mCommandRequest;

	public CommandChannel() {
		mTail = 0;
		mHead = 0;
		mCount = 0;
		mCommandRequest = new CommandRequest[MAX_REQUEST];
		mCommandDispatching = new CommandDispatching(this);
	}
	
	public void startWork() {
		mCommandDispatching.start();
	}
	
	public void stopWork() {
		mCommandDispatching.stopCommandDispatching();
	}
	
	public synchronized void putCommand(CommandRequest request) {
		while(mCount >= mCommandRequest.length) {
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		mCommandRequest[mTail] = request;
		mTail = (mTail + 1) % mCommandRequest.length;
		mCount++;
		notifyAll();
	}
	
	public synchronized CommandRequest takeCommand() throws InterruptedException {
		while(mCount <= 0) {
			try {
				wait();
			} catch(InterruptedException e) {
				throw new InterruptedException(e.getMessage());
			}
		}
		
		CommandRequest request = mCommandRequest[mHead];
		mHead = (mHead + 1) % mCommandRequest.length;
		mCount--;
		notifyAll();
		return request;
	}

}
