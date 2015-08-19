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

public class MPR1910CmdUtil {
	// only one ACK respond type
	public final static byte ONLY_ONE_ACK_COMMAND = (byte) 0xAA;
	public final static byte ERROR_ACK_COMMAND = (byte) 0xEE;
	
	//Stop
	public final static byte STOP_COMMAND = (byte)0x00;

	//System 
	public final static byte SYSTEM_COMMAND = (byte)0x00;
	public final static byte SYSTEM_VERSION = (byte)0x00;
	public final static byte SYSTEM_TEMPERATURE = (byte)0x01;
	public final static byte SYSTEM_READER_STATUS = (byte)0x0B;
	
	// C1G2
	public final static byte C1G2_COMMAND = (byte)0x20;
	public final static byte C1G2_PORTAL_ID = (byte)0x1E;
	public final static byte C1G2_READ_SINGLE_TAG_ID = (byte)0x00;
	public final static byte C1G2_READ_SINGLE_TAG_ID_WITH_TIMEOUT = 0x10;
	public final static byte C1G2_RF_POWER_LEVEL_CONTROL = 0x12;
	public final static byte C1G2_READ_BLOCK_DATA = 0x0D;
	public final static byte C1G2_READ_HIGH_CAPACITY_MEMORY = 0x6D;
	
	// EMMT
	public final static byte EMMT_COMMAND = (byte)0x90;
	public final static byte EMMT_READER_STATUS = 0x0B;
	public final static byte EMMT_MAINBOARD_VERSION = 0x0C;
	public final static byte EMMT_SLEEPING = (byte)0x15;
	
	// AWID
	public final static byte AWID_COMMAND = (byte)0xFF;
	public final static byte AWID_PRESS_BUTTON = (byte)0xD1;
	public final static byte AWID_RELEASE_BUTTON = (byte)0xD2;
	public final static byte AWID_OVER_HEAT = (byte)0x00;
	
	// Undefine
	public final static byte EPC_TID = (byte)0x60;
	public final static byte UNDEFINE = (byte) 0xDD;
}
