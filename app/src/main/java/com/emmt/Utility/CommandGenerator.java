/*
 * Description
 *
 *   A brief description of the class/interface.
 *
 * History
 *
 *   yyyy-mm-dd Author        
 *              What has been changed.
 *
 * Copyright notice
 */
package com.emmt.Utility;


public class CommandGenerator {
	
	public static byte[] buildMainboardVersionCmd() {
		byte[] command = MPRCmdCRCUtil.buildFullCommand(
				MPR1910CmdUtil.EMMT_COMMAND, MPR1910CmdUtil.EMMT_MAINBOARD_VERSION,
				null, 0);
		return command;
	}
	
	public static byte[] buildVersionCmd() {
		byte[] command = MPRCmdCRCUtil.buildFullCommand(
				MPR1910CmdUtil.SYSTEM_COMMAND, MPR1910CmdUtil.SYSTEM_VERSION,
				null, 0);
		return command;
	}
	
	public static byte[] buildInventoryCmd() {
		byte[] data = { 0x00, 0x01 };
		byte[] command = MPRCmdCRCUtil.buildFullCommand(
				MPR1910CmdUtil.C1G2_COMMAND, MPR1910CmdUtil.C1G2_PORTAL_ID, data,
				data.length);
		
		return command;
	}
	
	public static byte[] buildStopCmd() {
		byte[] stop = { 0x00 };
		return stop;
	}
	
	public static byte[] buildRFPowerLevelCmd(int power) {
		if(power < 0 || power > 255) {
			return null;
		}
		byte b = (byte)( (power & 0x000000ff) );
		byte[] data = {b};
		byte[] command = MPRCmdCRCUtil.buildFullCommand(
				MPR1910CmdUtil.SYSTEM_COMMAND, MPR1910CmdUtil.C1G2_RF_POWER_LEVEL_CONTROL,
				data, data.length);
		return command;
	}
	
	public static byte[] buildReaderStatusCmd() {
		byte[] command = MPRCmdCRCUtil.buildFullCommand(
				MPR1910CmdUtil.SYSTEM_COMMAND, MPR1910CmdUtil.SYSTEM_READER_STATUS,
				null, 0);
		return command;
	}
	
	public static byte[] buildSingleEPCCmd() {
		byte[] tryTime = {0x06}; 
		byte[] command = MPRCmdCRCUtil.buildFullCommand(
				MPR1910CmdUtil.C1G2_COMMAND, MPR1910CmdUtil.C1G2_READ_SINGLE_TAG_ID_WITH_TIMEOUT,
				tryTime, tryTime.length);
		
		return command;
	}
	
	public static byte[] buildTIDCmd() {
		byte[] bank = {0x02};
		byte[] command = MPRCmdCRCUtil.buildFullCommand(
				MPR1910CmdUtil.C1G2_COMMAND, MPR1910CmdUtil.C1G2_READ_BLOCK_DATA,
				bank, bank.length);
		
		return command;
	}

	public static byte[] buildTID2Cmd() {
		byte[] bank = {0x02, 0x00, 0x00, 0x0C, 0x05};
		byte[] command = MPRCmdCRCUtil.buildFullCommand(
				MPR1910CmdUtil.C1G2_COMMAND, MPR1910CmdUtil.C1G2_READ_HIGH_CAPACITY_MEMORY,
				bank, bank.length);

		return command;
	}
	
	public static byte[] buildRFIDStatusCmd() {
		byte[] command = MPRCmdCRCUtil.buildFullCommand(
				MPR1910CmdUtil.EMMT_COMMAND, MPR1910CmdUtil.EMMT_READER_STATUS,
				null, 0);
		System.out.println(HexConverseUtil.bytesToHexString(command));
		return command;
	}
}
