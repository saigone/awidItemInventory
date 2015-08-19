package com.emmt.plus.process;

import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

public class ProcessSleep implements RespondenceProcessInterface {

	@Override
	public DataPackageEvent executeProcess(byte[] rcsp) {
		DataPackageEvent dataPackageEvent = new DataPackageEvent();
		dataPackageEvent.setStatus(false);
		dataPackageEvent.setCommandType(MPR1910CmdUtil.EMMT_COMMAND);
		dataPackageEvent.setCommand(MPR1910CmdUtil.EMMT_SLEEPING);
		dataPackageEvent.setMessage("Reader is Sleeping");
		
		return dataPackageEvent;
	}

}
