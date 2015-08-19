package com.emmt.awiditeminventory;

import java.util.Calendar;


import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

public class AddNewItemTabFragment2 extends Fragment {
	Button mBtnDatePicDlg;
	EditText mTxtMfcName;
	EditText mTxtModelType;
	EditText mTxtSerialNumber;
	EditText mTxtStorageLocation;
	EditText mTxtInventoryCount;
	EditText mTxtDate;
	EditText mTxtMoney;
	CheckBox mChbManageCheck;
	Spinner mSpnUsageStates;
	EditText mTxtAccounts;
	EditText mTxtSection;
	
	private MyTabInfoContainner tabInfo;
	
	public AddNewItemTabFragment2() {
		
	}
	
	public void setTabInfoContainner(MyTabInfoContainner tab) {
		tabInfo = tab;
	}
	
//	public AddNewItemTabFragment2(MyTabInfoContainner tab) {
//		tabInfo = tab;
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.add_new_item_tab_sublayout2,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setupViewComponent();
	}

	private void setupViewComponent() {
		mBtnDatePicDlg = (Button) getView().findViewById(
				R.id.btn_DatePicDlg_tab);
		mBtnDatePicDlg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("Date btn");
				mTxtDate.setText("");
				Calendar now = Calendar.getInstance();

				DatePickerDialog datePicDlg = new DatePickerDialog(
						AddNewItemTabFragment2.this.getActivity(),
						datePicDlgOnDateSelLis, now.get(Calendar.YEAR), now
								.get(Calendar.MONTH), now
								.get(Calendar.DAY_OF_MONTH));
				datePicDlg.setTitle("選擇日期");
				datePicDlg.setMessage("請選擇適合您的日期");
				datePicDlg.setIcon(android.R.drawable.ic_dialog_info);
				datePicDlg.setCancelable(false);
				datePicDlg.show();

			}
		});

		mTxtMfcName = (EditText) getView().findViewById(R.id.txt_mfc_name_tab);
		mTxtModelType = (EditText) getView().findViewById(
				R.id.txt_model_type_tab);
		mTxtSerialNumber = (EditText) getView().findViewById(
				R.id.txt_serial_number_tab);
		mTxtStorageLocation = (EditText) getView().findViewById(
				R.id.txt_storage_location_tab);
		mTxtInventoryCount = (EditText) getView().findViewById(
				R.id.txt_inventory_count_tab);
		mTxtMoney = (EditText) getView().findViewById(R.id.txt_money_tab);
		mChbManageCheck = (CheckBox) getView().findViewById(
				R.id.chb_manage_check_tab);
		mTxtAccounts = (EditText) getView().findViewById(R.id.txt_accounts_tab);
		mTxtSection = (EditText) getView().findViewById(R.id.txt_section_tab);
		mTxtDate = (EditText) getView().findViewById(R.id.txt_date_tab);

		mSpnUsageStates = (Spinner) getView().findViewById(
				R.id.spn_usage_states_tab);
		ArrayAdapter<CharSequence> adapterList = ArrayAdapter
				.createFromResource(this.getActivity(),
						R.array.UsageStatesList, R.layout.spinner_layout);
		mSpnUsageStates.setAdapter(adapterList);
	}

	private DatePickerDialog.OnDateSetListener datePicDlgOnDateSelLis = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mTxtDate.setText(Integer.toString(year) + "年"
					+ Integer.toString(monthOfYear + 1) + "月"
					+ Integer.toString(dayOfMonth) + "日");
		}
	};
}
