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

public class MPRCmdCRCUtil {
	// LEN + CMD
	private static int PACKAGE_TITLE_LENGTH = 3;

	// CRC1 + CRC2
	private static int CRC_LENGTH = 2;

	public static byte[] buildFullCommand(byte type, byte cmd, byte data[],
			int dataLength) {
		byte fullCommandLength = (byte) (dataLength + PACKAGE_TITLE_LENGTH + CRC_LENGTH);
		byte command[] = new byte[fullCommandLength];
		command[0] = fullCommandLength;
		command[1] = type;
		command[2] = cmd;

		for (int i = 0; i < dataLength; i++)
			command[i + PACKAGE_TITLE_LENGTH] = data[i];

		int crc = checkCRCFromHostToReader(command, fullCommandLength - 2);
		command[fullCommandLength - 2] = (byte) (crc >> 8);
		command[fullCommandLength - 1] = (byte) (crc & 0xff);
		return command;
	}

	private static int calculateCRCAlgorithm(byte ary[], int offset, int count) {
		int endIndex = offset + count;
		int crc = 65535;
		for (int i = offset; i < endIndex; i++) {
			crc = (ary[i] << 8) ^ crc;
			for (int j = 0; j < 8; j++)
				if ((crc & 0x8000) != 0)
					crc = crc << 1 ^ 0x1021;
				else
					crc <<= 1;
		}

		return crc & 0xffff;
	}

	private static int checkCRCFromHostToReader(byte ary[], int len) {
		return calculateCRCAlgorithm(ary, 0, len) ^ 0xffff;
	}
	
	public static int checkCRCFromReaderToHost(byte ary[], int len) {
		return calculateCRCAlgorithm(ary, 0, len);
	}

}
