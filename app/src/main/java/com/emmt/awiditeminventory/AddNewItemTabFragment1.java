package com.emmt.awiditeminventory;


import android.app.Fragment;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class AddNewItemTabFragment1 extends Fragment {
	EditText mTxtEPC;
	EditText mTxtTID;
	Spinner mSpnManagerUnit;
	EditText mTxtManager;
	EditText mTxtPropertyIndex;
	Spinner mSpnPropertyType;
	EditText mTxtPropertyName;
	TextView mTxtCurrentPower;
	private MyTabInfoContainner tabInfo;
	
	public AddNewItemTabFragment1() {
		
	}
	
	public void setTabInfoContainner(MyTabInfoContainner tab) {
		tabInfo = tab;
	}
	
//	public AddNewItemTabFragment1(MyTabInfoContainner tab) {
//		tabInfo = tab;
//	}

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
	}

	private void setupViewComponent() {
		Log.v("time", "AddNewItemTabFragment1 setupViewComponent");
		mTxtCurrentPower = (TextView) getView().findViewById(R.id.txt_current_power);
		mTxtEPC = (EditText) getView().findViewById(R.id.txt_epc_tab);
		mTxtTID = (EditText) getView().findViewById(R.id.txt_tid_tab);
		mTxtManager = (EditText) getView().findViewById(R.id.txt_manager_tab);
		mTxtPropertyIndex = (EditText) getView().findViewById(
				R.id.txt_property_index_tab);
		mTxtPropertyName = (EditText) getView().findViewById(
				R.id.txt_property_name_tab);

		mTxtCurrentPower.setText(getString(R.string.current_power, tabInfo.getCurrentPower()));

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
	
	
}
