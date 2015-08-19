package com.emmt.plus.process;

import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

public class ProcessErrorAck implements RespondenceProcessInterface {

	@Override
	public DataPackageEvent executeProcess(byte[] rcsp) {
		DataPackageEvent dataPackageEvent = new DataPackageEvent();
		dataPackageEvent.setStatus(false);
		dataPackageEvent.setCommandType(MPR1910CmdUtil.ERROR_ACK_COMMAND);
		dataPackageEvent.setMessage("ACK is 0xFF");
		
		return dataPackageEvent;
	}

}
