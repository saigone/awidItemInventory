package com.emmt.plus.device;

import java.io.IOException;
import java.io.OutputStream;

public class CommandRequest {
	private OutputStream mOutStream = null;
	private byte[] mCommand;
	
	public CommandRequest(byte[] cmd, OutputStream stream) {
		mCommand = cmd;
		mOutStream = stream;
	}
	
	public void execute() throws IOException {
		mOutStream.write(mCommand);
		mOutStream.flush();
	}

}
