package com.emmt.plus.process;

import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

public class ProcessEmmtStatus implements RespondenceProcessInterface {

	@Override
	public DataPackageEvent executeProcess(byte[] rcsp) {
		int temperature = rcsp[2];
		int battery = rcsp[4];
		DataPackageEvent dataPackageEvent = new DataPackageEvent();
		dataPackageEvent.setStatus(true);
		dataPackageEvent.setCommandType(MPR1910CmdUtil.EMMT_COMMAND);
		dataPackageEvent.setCommand(MPR1910CmdUtil.EMMT_READER_STATUS);
		dataPackageEvent.setBattery(battery);
		dataPackageEvent.setTemperature(temperature);
		
		return dataPackageEvent;
	}

}
