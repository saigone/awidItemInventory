package com.emmt.plus.process;

import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

public class ProcessVersion implements RespondenceProcessInterface {

	@Override
	public DataPackageEvent executeProcess(byte[] rcsp) {
		DataPackageEvent dataPackageEvent = new DataPackageEvent();
		String version = new String(rcsp).trim();
		version = version.substring(0, version.length() - 1);

		dataPackageEvent.setStatus(true);
		dataPackageEvent.setCommandType(MPR1910CmdUtil.SYSTEM_COMMAND);
		dataPackageEvent.setCommand(MPR1910CmdUtil.SYSTEM_VERSION);
		dataPackageEvent.setVersion(version);
		
		return dataPackageEvent;
	}

}
