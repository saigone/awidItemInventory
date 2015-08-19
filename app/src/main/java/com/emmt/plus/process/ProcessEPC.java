package com.emmt.plus.process;

import android.util.Log;

import com.emmt.Utility.HexConverseUtil;
import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

public class ProcessEPC implements RespondenceProcessInterface {
	private final static String READ_EPC_TIME_OUT = "FF1080FEC1";
	
	@Override
	public DataPackageEvent executeProcess(byte[] rcsp) {
		DataPackageEvent dataPackageEvent = new DataPackageEvent();
		String epc = HexConverseUtil.bytesToHexString(rcsp).toUpperCase();
		if (epc.equals(READ_EPC_TIME_OUT)) {
			dataPackageEvent.setEPC("TIME_OUT");
		} else {
			dataPackageEvent.setEPC(epc.substring(8, epc.length() - 8));
		}
		dataPackageEvent.setStatus(true);
		dataPackageEvent.setCommandType(MPR1910CmdUtil.C1G2_COMMAND);
		dataPackageEvent.setCommand(MPR1910CmdUtil.C1G2_READ_SINGLE_TAG_ID_WITH_TIMEOUT);
		
		return dataPackageEvent;
	}

}
