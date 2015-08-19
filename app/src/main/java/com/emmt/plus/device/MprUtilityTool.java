package com.emmt.plus.device;

import com.emmt.Utility.MPR1910CmdUtil;

public class MprUtilityTool {

	public MprUtilityTool() {
		// TODO Auto-generated constructor stub
	}
	
	public enum MprStatus {
		ACCEPTING, CORRECT, ERROR, PRESS, RELEASE, EPC, TID, OVERHEAT, READ_HIGH_CAPACITY_MEMORY,
		ELSE, MULTI, SLEEP, VERSION, EMMT_READER_STATUS, SYSTEM_READER_STATUS, MAINBOARD_VERSION
	}
	
	public static byte[] dataShiftLeft(byte[] buf) {
		for (int i = 0; i < buf.length - 2; i++) {
			buf[i] <<= 1;
			if ((buf[i + 1] & 0x80) != 0)
				++buf[i];
		}

		return buf;
	}
	
	public static MprStatus checkStateFromRestRespond(byte[] rcsp) throws Exception {
		MprStatus status = MprStatus.ACCEPTING;
		
		try {
			switch(rcsp[0]) {
			case MPR1910CmdUtil.SYSTEM_COMMAND:
				if (rcsp[1] == MPR1910CmdUtil.SYSTEM_VERSION)
					status = MprStatus.VERSION;
				if (rcsp[1] == MPR1910CmdUtil.SYSTEM_READER_STATUS)
					status = MprStatus.SYSTEM_READER_STATUS;
				break;
			case MPR1910CmdUtil.C1G2_COMMAND:
				if (rcsp[1] == MPR1910CmdUtil.C1G2_READ_SINGLE_TAG_ID_WITH_TIMEOUT)
					status = MprStatus.EPC;
				else if (rcsp[1] == MPR1910CmdUtil.C1G2_READ_BLOCK_DATA)
					status = MprStatus.TID;
				else if (rcsp[1] == MPR1910CmdUtil.C1G2_PORTAL_ID)
					status = MprStatus.MULTI;
				else if(rcsp[1] == MPR1910CmdUtil.C1G2_READ_HIGH_CAPACITY_MEMORY)
					status = MprStatus.READ_HIGH_CAPACITY_MEMORY;
				break;
			case MPR1910CmdUtil.AWID_COMMAND:
				if(rcsp[1] == MPR1910CmdUtil.C1G2_READ_SINGLE_TAG_ID_WITH_TIMEOUT) // EPC TIME OUT RESPOND HAS 0xFF AT BEGIN
					status = MprStatus.EPC;
				else if(rcsp[1] == MPR1910CmdUtil.C1G2_READ_HIGH_CAPACITY_MEMORY)
					status = MprStatus.READ_HIGH_CAPACITY_MEMORY;
				else if (rcsp[2] == MPR1910CmdUtil.AWID_PRESS_BUTTON)
					status = MprStatus.PRESS;
				else if (rcsp[2] == MPR1910CmdUtil.AWID_RELEASE_BUTTON)
					status = MprStatus.RELEASE;
				else if(rcsp[2] == MPR1910CmdUtil.AWID_OVER_HEAT)
					status = MprStatus.OVERHEAT;
				break;
			case MPR1910CmdUtil.EMMT_COMMAND:
				if (rcsp[1] == MPR1910CmdUtil.EMMT_SLEEPING)
					status = MprStatus.SLEEP;
				else if(rcsp[1] == MPR1910CmdUtil.EMMT_READER_STATUS)
					status = MprStatus.EMMT_READER_STATUS;
                else if(rcsp[1] == MPR1910CmdUtil.EMMT_MAINBOARD_VERSION)
                    status = MprStatus.MAINBOARD_VERSION;
				break;
			default:
				status = MprStatus.ELSE;
				break;
			}
		} catch (Exception ex) {
			throw new Exception(ex.toString());
		}
		
		return status;
	}
}
