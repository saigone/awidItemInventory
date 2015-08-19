package com.emmt.plus.process;

import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

public class ProcessSystemStatus implements RespondenceProcessInterface {

	@Override
	public DataPackageEvent executeProcess(byte[] rcsp) {
		int power = rcsp[rcsp.length - 7]; // power的值排行倒數第7位
		if (power < 0) {
			power += 256; // 超出ASCii的值是負的，需還原成原來的值
		}
		DataPackageEvent dataPackageEvent = new DataPackageEvent();
		dataPackageEvent.setStatus(true);
		dataPackageEvent.setCommandType(MPR1910CmdUtil.SYSTEM_COMMAND);
		dataPackageEvent.setCommand(MPR1910CmdUtil.SYSTEM_READER_STATUS);
		dataPackageEvent.setPower(power);
		
		return dataPackageEvent;
	}

}
