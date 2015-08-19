package com.emmt.plus.device;

import java.util.ArrayList;
import java.util.List;

import com.emmt.Utility.MPR1910CmdUtil;

public class DataPackageEvent {
	private boolean isSuccess = false;
	private String mMessage = "";
	private byte mCommand = MPR1910CmdUtil.UNDEFINE;
	private byte mType = MPR1910CmdUtil.UNDEFINE;
	private String mEPC = "";
	private String mTID = "";
	private String mVersion = "";
	private int mBattery = 0;
	private int mTemperature = 0;
	private int power = 0;

	public DataPackageEvent() {

	}

	public void setMessage(String message) {
		mMessage = message;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setStatus(boolean status) {
		isSuccess = status;
	}

	public boolean getStatus() {
		return isSuccess;
	}
	
	public void setCommandType(byte type) {
		mType = type;
	}
	
	public byte getCommandType() {
		return mType;
	}

	public void setCommand(byte command) {
		mCommand = command;
	}

	public byte getCommand() {
		return mCommand;
	}

	public String getEPC() {
		return mEPC.toUpperCase();
	}

	public void setEPC(String epc) {
		this.mEPC = epc;
	}

	public String getTID() {
		return mTID.toUpperCase();
	}

	public void setTID(String tid) {
		this.mTID = tid;
	}

	public String getVersion() {
		return mVersion.toUpperCase();
	}

	public void setVersion(String version) {
		this.mVersion = version;
	}

	public int getBattery() {
		return mBattery;
	}

	public void setBattery(int battery) {
		this.mBattery = battery;
	}

	public int getTemperature() {
		return mTemperature;
	}

	public void setTemperature(int temprature) {
		this.mTemperature = temprature;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}
}
