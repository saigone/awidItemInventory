package com.emmt.awiditeminventory;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class UpdateItemTabFragment1 extends Fragment {
	EditText mTxtEPC;
	EditText mTxtTID;
	Spinner mSpnManagerUnit;
	EditText mTxtManager;
	EditText mTxtPropertyIndex;
	Spinner mSpnPropertyType;
	EditText mTxtPropertyName;
	TextView mTxtCurrentPower;
	private MyTabInfoContainner tabInfo;
	
	public UpdateItemTabFragment1() {
	}
	
	public void setTabInfoContainner(MyTabInfoContainner tab) {
		tabInfo = tab;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.add_new_item_tab_sublayout1,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setupViewComponent();
		initialViewComponent();
	}

	private void setupViewComponent() {
		mTxtCurrentPower = (TextView) getView().findViewById(R.id.txt_current_power);
		mTxtEPC = (EditText) getView().findViewById(R.id.txt_epc_tab);
		mTxtEPC.setEnabled(false);
		mTxtTID = (EditText) getView().findViewById(R.id.txt_tid_tab);
		mTxtManager = (EditText) getView().findViewById(R.id.txt_manager_tab);
		mTxtTID.setEnabled(false);
		mTxtPropertyIndex = (EditText) getView().findViewById(
				R.id.txt_property_index_tab);
		mTxtPropertyName = (EditText) getView().findViewById(
				R.id.txt_property_name_tab);
//		mTxtPropertyName.setEnabled(false);
		mSpnManagerUnit = (Spinner) getView().findViewById(
				R.id.spn_manager_unit_tab);
		ArrayAdapter<CharSequence> adapterList = ArrayAdapter
				.createFromResource(this.getActivity(),
						R.array.managerUnitList, R.layout.spinner_layout);
		mSpnManagerUnit.setAdapter(adapterList);

		mSpnPropertyType = (Spinner) getView().findViewById(
				R.id.spn_property_type_tab);
		adapterList = ArrayAdapter.createFromResource(this.getActivity(),
				R.array.propertyTypeList, R.layout.spinner_layout);
		mSpnPropertyType.setAdapter(adapterList);
	}
	
	private void initialViewComponent() {
		mTxtEPC.setText(tabInfo.getTxtEPC());
		mTxtTID.setText(tabInfo.getTxtTID());
		mTxtManager.setText(tabInfo.getTxtManager());
		mTxtPropertyIndex.setText(tabInfo.getTxtPropertyIndex());
		mTxtPropertyName.setText(tabInfo.getTxtPropertyName());
		mSpnManagerUnit.setSelection(tabInfo.getSpnManagerUnit());
		mSpnPropertyType.setSelection(tabInfo.getSpnPropertyType());
	}
}
