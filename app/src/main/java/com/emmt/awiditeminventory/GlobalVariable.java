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
package com.emmt.awiditeminventory;


import com.emmt.plus.device.HandyDeviceHB;
import android.app.Application;

public class GlobalVariable extends Application {
	private HandyDeviceHB handDeviceHB = null;
	private String TIDType = "標準";
	private String currentTime = "";

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public String getTIDType() {
		return TIDType;
	}

	public void setTIDType(String tIDType) {
		TIDType = tIDType;
	}

	
	public HandyDeviceHB getHandDeviceHB() {
		return handDeviceHB;
	}
	
	public void setHandDeviceHB(HandyDeviceHB device) {
		handDeviceHB = device;
	}
}
