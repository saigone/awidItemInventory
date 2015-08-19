package com.emmt.plus.process;

import android.util.Log;

import com.emmt.Utility.HexConverseUtil;
import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

public class ProcessMultiTag implements RespondenceProcessInterface {

	@Override
	public DataPackageEvent executeProcess(byte[] rcsp) {
		DataPackageEvent dataPackageEvent = new DataPackageEvent();
		String epc = HexConverseUtil.bytesToHexString(rcsp).toUpperCase();
		epc = epc.substring(8, epc.length() - 8);

		dataPackageEvent.setStatus(true);
		dataPackageEvent.setCommandType(MPR1910CmdUtil.C1G2_COMMAND);
		dataPackageEvent.setCommand(MPR1910CmdUtil.C1G2_PORTAL_ID);
		dataPackageEvent.setEPC(epc);
		
		return dataPackageEvent;
	}

}
