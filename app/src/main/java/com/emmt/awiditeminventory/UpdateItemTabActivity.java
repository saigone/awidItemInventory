package com.emmt.awiditeminventory;

import java.io.ByteArrayOutputStream;


import com.emmt.Utility.SoundTool;
import com.emmt.database.NewListDataSQL;
import com.emmt.plus.device.HandyDeviceHB;
import com.emmt.plus.device.ReaderErrorRespondException;
import com.emmt.plus.device.ReaderSleepingException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class UpdateItemTabActivity extends Activity {
	private static final int NUM_PAGES = 3;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally to access previous
	 * and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;

	// SQLiteDatabase對象
	SQLiteDatabase db;
	// 資料庫名
	public String db_name = "warehouse.db";
	// 表名
	public String table_name = "WarehouseInventoryTable";
	// 輔助類名
	NewListDataSQL helper = new NewListDataSQL(this, db_name);
	// requestCode對應到startActivityForResult(arg1, arg2)第2個參數
	public static final int FILE_RESULT_CODE = 1;
	// private HBSerialDevice mHBDevice; // HB讀取器
	private SoundTool mSoundTool; // 音效控制
	private boolean isConnection = false; // 判斷是離線模式或連線模式
	private String mEPC; // dataBuilder所帶過來的資料

	private volatile boolean mActivityFlag = true;
	private Thread mHandleTagThread;

	private UpdateItemTabFragment1 NecessaryColumn;
	private UpdateItemTabFragment2 OptionalColumn;
	private UpdateItemTabFragment3 ImageColumn;
	private MyTabInfoContainner tabInfo = new MyTabInfoContainner();

	private HandyDeviceHB mHBDevice;
	private int mCurrentPower = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.update_item_tab_layout);
		setupViewComponent();
		// 檢查連線模式
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		mEPC = bundle.getString("EPC");
		isConnection = bundle.getBoolean("isConnection");

		// 獲得BluetoothAdapter對象
		GlobalVariable globalVariable = (GlobalVariable) getApplicationContext();
        if (isConnection) {
            mHBDevice = globalVariable.getHandDeviceHB();
            mHBDevice.switchToMultiTagMode(false);
            mHBDevice.enableHandyTrigger(true);
		}
		else {
			setTitle(getStringFromResource(R.string.offline));
		}
		// 以輔助類獲得資料庫對象
		db = helper.getReadableDatabase();
		initialComponent();
		initialSound(); // 初始化音效
	}

	private void setupViewComponent() {
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOnPageChangeListener(
				new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between pages, select the
						// corresponding tab.
						getActionBar().setSelectedNavigationItem(position);
					}
				});

		NecessaryColumn = new UpdateItemTabFragment1();
		NecessaryColumn.setTabInfoContainner(tabInfo);
		OptionalColumn = new UpdateItemTabFragment2();
		OptionalColumn.setTabInfoContainner(tabInfo);
		ImageColumn = new UpdateItemTabFragment3();
		ImageColumn.setTabInfoContainner(tabInfo);

		final ActionBar actBar = getActionBar();
		actBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				mPager.setCurrentItem(tab.getPosition());
			}

			public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
				tabInfo.setTxtEPC(NecessaryColumn.mTxtEPC.getText().toString().toUpperCase());
				tabInfo.setTxtTID(NecessaryColumn.mTxtTID.getText().toString().toUpperCase());
				tabInfo.setCurrentPower(mCurrentPower);
			}

			public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
				// probably ignore this event
			}
		};

		actBar.addTab(actBar.newTab().setText(getStringFromResource(R.string.basic_column))
				.setTabListener(tabListener));

		actBar.addTab(actBar.newTab().setText(getStringFromResource(R.string.extended_column))
				.setTabListener(tabListener));

		actBar.addTab(actBar.newTab().setText(getStringFromResource(R.string.photo_column))
				.setTabListener(tabListener));

		/*
		actBar.addTab(actBar.newTab().setText(getStringFromResource(R.string.basic_column))
				.setTabListener(new MyTabListener2(NecessaryColumn)));

		actBar.addTab(actBar.newTab().setText(getStringFromResource(R.string.extended_column))
				.setTabListener(new MyTabListener2(OptionalColumn)));

		actBar.addTab(actBar.newTab().setText(getStringFromResource(R.string.photo_column))
				.setTabListener(new MyTabListener2(ImageColumn)));
				*/
	}

	@Override
	protected void onResume() {
		super.onResume();
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mHBDevice != null) {
					try {
						mCurrentPower = mHBDevice.getPowerLevel();
						NecessaryColumn.mTxtCurrentPower.setText(getString(R.string.current_power, mCurrentPower));
					} catch (ReaderErrorRespondException e) {
						e.printStackTrace();
						NecessaryColumn.mTxtCurrentPower.setText(getString(R.string.current_power, 0));
					} catch (ReaderSleepingException e) {
						NecessaryColumn.mTxtCurrentPower.setText(getString(R.string.current_power, 0));
						e.printStackTrace();
					}
				} else {
					mCurrentPower = 0;
					NecessaryColumn.mTxtCurrentPower.setText(getString(R.string.current_power, 0));
				}
			}
		}, 350);
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;

			switch (position) {
				case 0:
					fragment = NecessaryColumn;
					break;
				case 1:
					fragment = OptionalColumn;
					break;
				case 2:
					fragment = ImageColumn;
					break;
			}

			return fragment;
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	private void initialSound() {
		mSoundTool = new SoundTool(this, R.raw.song); // 設定音效
	}

	private void beep() {
		mSoundTool.play(); // 發出音效
	}

	private void waitAndReceiveData() {
		// read Tag
		final Handler handler = new Handler();
		mHandleTagThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (mActivityFlag) {
					try {
						final String epc = mHBDevice.getInventoryResult()[0];
						if (epc.equals("TIME_OUT")) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									NecessaryColumn.mTxtEPC.setText("Reading EPC Time out");
								}
							});
							//continue;
						} else if (HandyDeviceHB.ERROR_TAG.equals(epc))
							break;
						else {
							handler.post(new Runnable() {
								@Override
								public void run() {
									NecessaryColumn.mTxtEPC.setText(epc.toUpperCase());
								}
							});
						}

						final String tid = mHBDevice.getTID2();
						if(tid.equals("TIME_OUT")) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									NecessaryColumn.mTxtTID.setText("Reading TID Time out");
								}
							});
						}
						else {
							handler.post(new Runnable() {
								@Override
								public void run() {
									NecessaryColumn.mTxtTID.setText(tid.toUpperCase());
								}
							});
						}
					} catch (ReaderErrorRespondException e) {
						e.printStackTrace();
					} catch (ReaderSleepingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(UpdateItemTabActivity.this, "Reader is sleeping, wait for 5 second", Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		mHandleTagThread.start();
	}

	private void initialComponent() {
		Cursor c = db.rawQuery("select * from WarehouseInventoryTable where epc = \"" + mEPC
				+ "\" ORDER BY _ID ASC", null); // ASC由小往大輸出
		if (c == null) {
			showToastMessage("Null data");
		} else if (c.getCount() == 0) {
			showToastMessage(getStringFromResource(R.string.no_data));
		} else {
			c.moveToFirst();
			do {
				// c.getString(0)為index
				tabInfo.setImageBitmap(getImage(c.getString(1)));
				tabInfo.setTxtEPC(c.getString(2));
				tabInfo.setTxtTID(c.getString(3));
				tabInfo.setTxtManager(c.getString(4));
				tabInfo.setTxtPropertyIndex(c.getString(5));
				tabInfo.setTxtPropertyName(c.getString(6));
				tabInfo.setTxtMfcName(c.getString(7));
				tabInfo.setTxtModelType(c.getString(8));
				tabInfo.setTxtSerialNumber(c.getString(9));
				tabInfo.setTxtStorageLocation(c.getString(10));
				tabInfo.setTxtInventoryCount(c.getString(11));
				tabInfo.setTxtMoney(c.getString(12));
				tabInfo.setSpnManagerUnit(getStringArrayPosition(R.array.managerUnitList,
						c.getString(13)));
				tabInfo.setTxtAccounts(c.getString(14));
				tabInfo.setTxtSection(c.getString(15));
				tabInfo.setTxtDate(c.getString(16));
				tabInfo.setChbManageCheck(getCheckState(c.getInt(17)));
				tabInfo.setSpnPropertyType(getStringArrayPosition(R.array.propertyTypeList,
						c.getString(18)));
				tabInfo.setSpnUsageStates(getStringArrayPosition(R.array.UsageStatesList,
						c.getString(19)));
				// tabInfo.setImageName(c.getString(20));
			} while (c.moveToNext());
		}
		c.close();
	}

	private Bitmap getImage(String imageName) {
		// 把Base64變回bytes
		byte[] bytes = Base64.decode(imageName, Base64.DEFAULT);
		// 用BitmapFactory生成bitmap
		Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		// 轉換Bitmap為Drawable
		// Drawable drawable = new BitmapDrawable(bmp);

		return bmp;
	}

	private int getStringArrayPosition(int array, String matchString) {
		String[] stringArray = getResources().getStringArray(array);
		for (int i = 0; i < stringArray.length; i++) {
			if (matchString.equals(stringArray[i])) {
				return i;
			}
		}

		return 0;
	}

	private String getStringFromArrayPosition(int array, int index) {
		String[] stringArray = getResources().getStringArray(array);
		return (stringArray[index] == null) ? "" : stringArray[index];
	}

	private boolean getCheckState(int flag) {
		if (flag == 0) {
			return false;
		} else {
			return true;
		}
	}

	public void onclick(View view) {
		switch (view.getId()) {
		case R.id.btn_update_tab2:
			// 檢查EPC、TID與財產名稱是否為空值
			if (hasNullEditText()) {
				showToastMessage(getStringFromResource(R.string.lost_column));
				return;
			}
			// insert to database
			updateDatabase();
			break;
		case R.id.btn_delete_tab2:
			// 檢查EPC、TID與財產名稱是否為空值
			if (hasNullEditText()) {
				showToastMessage(getStringFromResource(R.string.lost_column));
				return;
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(getStringFromResource(R.string.warnning_dialog));
			dialog.setMessage(getStringFromResource(R.string.delete_data));
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.setCancelable(true);
			dialog.setPositiveButton(getStringFromResource(R.string.confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							delete();
							finish();
						}
					});
			dialog.setNegativeButton(getStringFromResource(R.string.cancel),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// moveTaskToBack(false);
						}
					});
			dialog.show();
			break;
		}
	}

	private void delete() {

		long long1 = db.delete(table_name, "epc = \""
				+ NecessaryColumn.mTxtEPC.getText().toString() + "\"", null);

		// 添加成功後返回行號，失敗後返回-1
		if (long1 == -1) {
			showToastMessage(getStringFromResource(R.string.delete_failed));
		} else {
			showToastMessage(getStringFromResource(R.string.delete_successfully));
		}
	}

	private boolean hasNullEditText() {
		if ("".equals(NecessaryColumn.mTxtEPC.getText().toString())
				|| "".equals(NecessaryColumn.mTxtTID.getText().toString())
				|| "".equals(NecessaryColumn.mTxtPropertyName.getText().toString())) {
			return true;
		}

		return false;
	}

	private void showToastMessage(String msg) {
		Toast t = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		t.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
		t.show();
	}

	private void updateDatabase() {
		String epc = NecessaryColumn.mTxtEPC.getText().toString().toUpperCase();
		String tid = NecessaryColumn.mTxtTID.getText().toString().toUpperCase();
		String txtManager = NecessaryColumn.mTxtManager.getText().toString();
		String txtPropertyIndex = NecessaryColumn.mTxtPropertyIndex.getText().toString();
		String txtPropertyName = NecessaryColumn.mTxtPropertyName.getText().toString();
		// 第2頁以後的頁籤，若沒被點選到，無法初始化，所以內含物件會是null，要先判斷
		String txtMfcName = (OptionalColumn.mTxtMfcName == null) ? tabInfo.getTxtMfcName()
				: OptionalColumn.mTxtMfcName.getText().toString();
		String txtModelType = (OptionalColumn.mTxtModelType == null) ? tabInfo.getTxtModelType()
				: OptionalColumn.mTxtModelType.getText().toString();
		String txtSerialNumber = (OptionalColumn.mTxtSerialNumber == null) ? tabInfo
				.getTxtSerialNumber() : OptionalColumn.mTxtSerialNumber.getText().toString();
		String txtStorageLocation = (OptionalColumn.mTxtStorageLocation == null) ? tabInfo
				.getTxtStorageLocation() : OptionalColumn.mTxtStorageLocation.getText().toString();
		String txtInventoryCount = (OptionalColumn.mTxtInventoryCount == null) ? tabInfo
				.getTxtInventoryCount() : OptionalColumn.mTxtInventoryCount.getText().toString();
		String txtMoney = (OptionalColumn.mTxtMoney == null) ? tabInfo.getTxtMoney()
				: OptionalColumn.mTxtMoney.getText().toString();
		int flag = (OptionalColumn.mChbManageCheck == null) ? (tabInfo.getChbManageCheck() == true) ? 1
				: 0
				: (OptionalColumn.mChbManageCheck.isChecked()) ? 1 : 0;
		String txtAccounts = (OptionalColumn.mTxtAccounts == null) ? tabInfo.getTxtAccounts()
				: OptionalColumn.mTxtAccounts.getText().toString();
		String txtSection = (OptionalColumn.mTxtSection == null) ? tabInfo.getTxtSection()
				: OptionalColumn.mTxtSection.getText().toString();
		String txtDate = (OptionalColumn.mTxtDate == null) ? tabInfo.getTxtDate()
				: OptionalColumn.mTxtDate.getText().toString();
		String managerUnit = (NecessaryColumn.mSpnManagerUnit == null) ? getStringFromArrayPosition(
				R.array.managerUnitList, tabInfo.getSpnManagerUnit())
				: NecessaryColumn.mSpnManagerUnit.getSelectedItem().toString();
		String propertyType = (NecessaryColumn.mSpnPropertyType == null) ? getStringFromArrayPosition(
				R.array.propertyTypeList, tabInfo.getSpnPropertyType())
				: NecessaryColumn.mSpnPropertyType.getSelectedItem().toString();
		String usageStates = (OptionalColumn.mSpnUsageStates == null) ? getStringFromArrayPosition(
				R.array.UsageStatesList, tabInfo.getSpnUsageStates())
				: OptionalColumn.mSpnUsageStates.getSelectedItem().toString();

		Bitmap bmp = null;
		if (ImageColumn.mImageView1 == null) {
			bmp = tabInfo.getImageBitmap();
		} else {
			// 建立圖片的緩存，圖片的緩存本身就是一個Bitmap
			ImageColumn.mImageView1.buildDrawingCache();
			// 取得緩存圖片的Bitmap檔
			bmp = ImageColumn.mImageView1.getDrawingCache();
		}

		// 先把 bitmap 轉成 byte
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte bytes[] = stream.toByteArray();
		// Android 2.2以上才有內建Base64，其他要自已找Libary或是用Blob存入SQLite
		// 把byte變成base64
		String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);

		ContentValues cv = new ContentValues();
		cv.put("imageview", base64);
		cv.put("epc", epc);
		cv.put("tid", tid);
		cv.put("manager", txtManager);
		cv.put("propertyindex", txtPropertyIndex);
		cv.put("propertyname", txtPropertyName);
		cv.put("mfcname", txtMfcName);
		cv.put("modeltype", txtModelType);
		cv.put("serialnumber", txtSerialNumber);
		cv.put("storagelocation", txtStorageLocation);
		cv.put("inventorycount", txtInventoryCount);
		cv.put("money", txtMoney);
		cv.put("unitmanage", managerUnit);
		cv.put("accounts", txtAccounts);
		cv.put("section", txtSection);
		cv.put("date", txtDate);
		cv.put("managecheck", flag);
		cv.put("propertytype", propertyType);
		cv.put("usagestates", usageStates);
		// cv.put("imagename", tabInfo.getImageName());
		// 添加方法
		long long1 = db.update(table_name, cv, "epc = \"" + epc + "\"", null);

		// 添加成功後返回行號，失敗後返回-1
		if (long1 == -1) {
			showToastMessage("更新失敗");
		} else {
			showToastMessage("更新" + long1 + "資料");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mSoundTool != null)
				mSoundTool.release();

			if (mHBDevice != null) {
				mActivityFlag = false;
				//mHBDevice.stopInventory();
                mHBDevice.enableHandyTrigger(false);
			}

			if (db != null) {
				db.close();
			}

			mSoundTool = null;
			mHBDevice = null;
			db = null;

			finish();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private String getStringFromResource(int id) {
		return getResources().getString(id);
	}
}
