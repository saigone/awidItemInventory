package com.emmt.plus.process;

import android.util.Log;

import com.emmt.Utility.HexConverseUtil;
import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.MprUtilityTool;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

public class ProcessTID implements RespondenceProcessInterface {

	@Override
	public DataPackageEvent executeProcess(byte[] rcsp) {
		DataPackageEvent dataPackageEvent = new DataPackageEvent();
		dataPackageEvent.setStatus(true);
		dataPackageEvent.setCommandType(MPR1910CmdUtil.C1G2_COMMAND);
		dataPackageEvent.setCommand(MPR1910CmdUtil.C1G2_READ_BLOCK_DATA);
		
		byte[] afterShifting = MprUtilityTool.dataShiftLeft(rcsp); // shifting演算法
		String tid = HexConverseUtil.bytesToHexString(afterShifting).toUpperCase();

		// 前3位(包含長度)與後7位不要，轉成16進位長度變2倍
		tid = tid.substring(4, tid.length() - 14).toUpperCase();
		if (tid.length() > 24) { // 只取96bits，每個字元4bits
			tid = tid.substring(0, 24);
		}

		dataPackageEvent.setTID(tid);
		
		return dataPackageEvent;
	}

}
