package com.emmt.awiditeminventory;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;

public class MyTabListener2 implements ActionBar.TabListener {

	private Fragment mFragment;

	public MyTabListener2(Fragment fragment) {
		mFragment = fragment;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		ft.add(R.id.frameLayout2, mFragment, null);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		ft.remove(mFragment);
	}

}
