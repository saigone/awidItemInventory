package com.emmt.awiditeminventory;

import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class MyTabInfoContainner {
	private Bitmap imageBitmap = null;
	private String imageName = "";
	private String txtEPC = "";
	private String txtTID = "";
	private int spnManagerUnit = 0;
	private String txtManager = "";
	private String txtPropertyIndex = "";
	private int spnPropertyType = 0;
	private String txtPropertyName = "";
	private String txtMfcName = "";
	private String txtModelType = "";
	private String txtSerialNumber = "";
	private String txtStorageLocation = "";
	private String txtInventoryCount = "0";
	private String txtDate = "";
	private String txtMoney = "0";
	private boolean chbManageCheck = true;
	private int spnUsageStates = 0;
	private String txtAccounts = "";
	private String txtSection = "";
	private boolean hasImage1 = false;
	private boolean hasImage2 = false;
	private boolean hasImage3 = false;
	private int currentPower = 0;

	public int getCurrentPower() {
		return currentPower;
	}

	public void setCurrentPower(int currentPower) {
		this.currentPower = currentPower;
	}
	
	public boolean isHasImage1() {
		return hasImage1;
	}

	public void setHasImage1(boolean hasImage1) {
		this.hasImage1 = hasImage1;
	}

	public boolean isHasImage2() {
		return hasImage2;
	}

	public void setHasImage2(boolean hasImage2) {
		this.hasImage2 = hasImage2;
	}

	public boolean isHasImage3() {
		return hasImage3;
	}

	public void setHasImage3(boolean hasImage3) {
		this.hasImage3 = hasImage3;
	}

//	public String getImageName() {
//		return imageName;
//	}
//
//	public void setImageName(String imageName) {
//		this.imageName = imageName;
//	}

	public Bitmap getImageBitmap() {
		return imageBitmap;
	}

	public void setImageBitmap(Bitmap imageView) {
		this.imageBitmap = imageView;
	}

	public String getTxtEPC() {
		return txtEPC;
	}

	public void setTxtEPC(String txtEPC) {
		this.txtEPC = txtEPC;
	}

	public String getTxtTID() {
		return txtTID;
	}

	public void setTxtTID(String txtTID) {
		this.txtTID = txtTID;
	}

	public int getSpnManagerUnit() {
		return spnManagerUnit;
	}

	public void setSpnManagerUnit(int spnManagerUnit) {
		this.spnManagerUnit = spnManagerUnit;
	}

	public String getTxtManager() {
		return txtManager;
	}

	public void setTxtManager(String txtManager) {
		this.txtManager = txtManager;
	}

	public String getTxtPropertyIndex() {
		return txtPropertyIndex;
	}

	public void setTxtPropertyIndex(String txtPropertyIndex) {
		this.txtPropertyIndex = txtPropertyIndex;
	}

	public int getSpnPropertyType() {
		return spnPropertyType;
	}

	public void setSpnPropertyType(int spnPropertyType) {
		this.spnPropertyType = spnPropertyType;
	}

	public String getTxtPropertyName() {
		return txtPropertyName;
	}

	public void setTxtPropertyName(String txtPropertyName) {
		this.txtPropertyName = txtPropertyName;
	}

	public String getTxtMfcName() {
		return txtMfcName;
	}

	public void setTxtMfcName(String txtMfcName) {
		this.txtMfcName = txtMfcName;
	}

	public String getTxtModelType() {
		return txtModelType;
	}

	public void setTxtModelType(String txtModelType) {
		this.txtModelType = txtModelType;
	}

	public String getTxtSerialNumber() {
		return txtSerialNumber;
	}

	public void setTxtSerialNumber(String txtSerialNumber) {
		this.txtSerialNumber = txtSerialNumber;
	}

	public String getTxtStorageLocation() {
		return txtStorageLocation;
	}

	public void setTxtStorageLocation(String txtStorageLocation) {
		this.txtStorageLocation = txtStorageLocation;
	}

	public String getTxtInventoryCount() {
		return txtInventoryCount;
	}

	public void setTxtInventoryCount(String txtInventoryCount) {
		this.txtInventoryCount = txtInventoryCount;
	}

	public String getTxtDate() {
		return txtDate;
	}

	public void setTxtDate(String txtDate) {
		this.txtDate = txtDate;
	}

	public String getTxtMoney() {
		return txtMoney;
	}

	public void setTxtMoney(String txtMoney) {
		this.txtMoney = txtMoney;
	}

	public boolean getChbManageCheck() {
		return chbManageCheck;
	}

	public void setChbManageCheck(boolean chbManageCheck) {
		this.chbManageCheck = chbManageCheck;
	}

	public int getSpnUsageStates() {
		return spnUsageStates;
	}

	public void setSpnUsageStates(int spnUsageStates) {
		this.spnUsageStates = spnUsageStates;
	}

	public String getTxtAccounts() {
		return txtAccounts;
	}

	public void setTxtAccounts(String txtAccounts) {
		this.txtAccounts = txtAccounts;
	}

	public String getTxtSection() {
		return txtSection;
	}

	public void setTxtSection(String txtSection) {
		this.txtSection = txtSection;
	}
}
