package com.emmt.awiditeminventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.emmt.plus.device.HandyDeviceHB;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements Runnable {
	private Button mBtnScanBluetooth;
	private BluetoothAdapter mBluetoothAdapter;
	private List<String> lstDevices = new ArrayList<String>();
	private List<Map<String, Object>> mList;
	private static final int REQUEST_DISCOVERABLE_BLUETOOTH = 3;
	private static final String TAG = "MainActivity";
	private String mBluetoothAddress;
	// 顯示搜索到的遠程藍牙設備
	private static ListView mDeviceListView;
	final private Handler mHandler = new Handler();
	//private ProgressDialog mProcessConnectionDialog;
	// 保存搜索到的遠程藍牙設備
	private static SimpleAdapter adtDevices;
	// 藍芽廣播註冊
	private boolean isRegistered = false;
	private String mDeviceType = "HB-1000";
	private WriteOrReaderTextFile mWRFile;
	private boolean isOverUsingTime = false;
	private ProgressDialog mConnectionDialog;
	private HandyDeviceHB mHandyDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持螢幕亮著
		setContentView(R.layout.activity_main);
		//initialTimeLock();
		initViewComponent();

		// 獲得BluetoothAdapter對象
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// mDeviceListView及其數據源配適器
		mDeviceListView = (ListView) findViewById(R.id.bluetooth_list);
		mList = new ArrayList<Map<String, Object>>();
		adtDevices = new SimpleAdapter(this, mList, R.layout.list_item_2, new String[] { "btView",
				"bluetooth", "btAddress", "device", "devicename" }, new int[] {
				R.id.list_item2_view, R.id.list_item2_title1, R.id.list_item2_content1,
				R.id.list_item2_title2, R.id.list_item2_content2 });
		mDeviceListView.setAdapter(adtDevices);
		// 給ListView添加點擊事件
		mDeviceListView.setOnItemClickListener(new itemClickListener(MainActivity.this));

		initTitle();
	}

	private void initViewComponent() {
		mBtnScanBluetooth = (Button) this.findViewById(R.id.btn_scan_bluetooth);
		if (isOverUsingTime) {
			mBtnScanBluetooth.setEnabled(false);
			Toast.makeText(this, "已超出試用時間，程式停止功能", Toast.LENGTH_LONG).show();
		}
	}

	private void initTitle() {
		String version = null;
		try {
			PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pinfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		this.setTitle(getResources().getString(R.string.app_name) + " " + version);

	}

	private void initialTimeLock() {

		try {
			mWRFile = new WriteOrReaderTextFile();
			mWRFile.createDirectory();
			isOverUsingTime = mWRFile.isOverUsingTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class itemClickListener implements OnItemClickListener {
		private Context _context;

		public itemClickListener(Context context) {
			_context = context;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			View view = arg1;
			TextView device = (TextView) view.findViewById(R.id.list_item2_content2);
			TextView textbluetooth = (TextView) view.findViewById(R.id.list_item2_content1);

			// TextView textView = (TextView) arg1;
			if (device.getText().toString().startsWith("HB-1")) {
				mDeviceType = "HB-1000";

			} else if (device.getText().toString().startsWith("HB-2")) {
				mDeviceType = "HB-2000";
			} else {
				Toast.makeText(_context, getStringFromResource(R.string.bluetooth_null),
						Toast.LENGTH_SHORT).show();
				return;
			}

//			mProcessConnectionDialog = ProgressDialog.show(MainActivity.this,
//					getStringFromResource(R.string.bluetooth_state), device.getText().toString()
//							+ " " + getStringFromResource(R.string.bluetooth_connecting), true,
//					false);

			mBluetoothAddress = textbluetooth.getText().toString();
			Thread thread = new Thread(MainActivity.this);
			thread.start();

			showProcessDialog(device.getText().toString());
		}
	}

	private void showProcessDialog(String deviceName) {
		mConnectionDialog = new ProgressDialog(this);
		mConnectionDialog.setTitle(getString(R.string.bluetooth_state));
		mConnectionDialog.setMessage(deviceName + " " + getString(R.string.bluetooth_connecting));
		mConnectionDialog.setCancelable(false);
		mConnectionDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mHandyDevice.disconnect();
				dialog.dismiss();
			}
		});
		mConnectionDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mConnectionDialog.show();
	}

	@Override
	public void run() {
		// HBSerialDevice hbDevice = new HBSerialDevice(mBluetoothAdapter,
		// mBluetoothAddress, MainActivity.this);
		mHandyDevice = new HandyDeviceHB(this, mBluetoothAddress);
		mHandyDevice.connect();

		// if (hbDevice.connect()) {
		if (mHandyDevice.isConnected()) {
			GlobalVariable globalVariable = (GlobalVariable) MainActivity.this
					.getApplicationContext();
			// globalVariable.setHBDevice(hbDevice);
			globalVariable.setHandDeviceHB(mHandyDevice);
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putBoolean("isConnection", true);
			bundle.putString("device", mDeviceType);
			intent.putExtras(bundle);
			intent.setClass(MainActivity.this, ExpandAvtivity.class);
			startActivity(intent);
		} else {
			mHandyDevice.disconnect();
			mConnectionDialog.dismiss();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this,
							getStringFromResource(R.string.bluetooth_connection_failed),
							Toast.LENGTH_LONG).show();
				}
			});
		}

//		mProcessConnectionDialog.dismiss();
	}

	/*
	 * 要設為public，layout否則會找不到該函式
	 */
	public void onclick(View view) {
		switch (view.getId()) {

		case R.id.btn_offline_bluetooth:
			// 離線模式下，讀取器不能使用，只能做基本的查詢
			Intent offline_intent = new Intent();
			Bundle offline_bundle = new Bundle();
			offline_bundle.putBoolean("isConnection", false);
			offline_intent.putExtras(offline_bundle);
			offline_intent.setClass(MainActivity.this, DataBuilderActivity.class);
			startActivity(offline_intent);
			break;
		case R.id.btn_scan_bluetooth:
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, getStringFromResource(R.string.bluetooth_not_found),
						Toast.LENGTH_LONG).show();
				Log.v(TAG, "沒有檢測到藍芽設備");
				return;
			} else {
				Log.v(TAG, "掃描藍芽");
				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {// 如果藍芽還沒開起
					// 也可以打開藍芽，有提示效果
					Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivity(intent);

					/* 確保藍芽被發現 */
					Intent discoverableIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					// 設置可見狀態500秒，但是最多是300秒
					discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500);
					startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BLUETOOTH);
					return;
				}
			}

			// 避免使用者連續按掃描鍵，造成當機的風險
			if (!mBtnScanBluetooth.isEnabled()) // 若按鍵已經被按過了，就不做任何動作
				return;
			mBtnScanBluetooth.setEnabled(false); // 若是按鍵沒被按過，當按下時設定為已按過狀態
			new Thread(new Clock()).start(); // 開啟計時器

			// 注冊Receiver來獲取藍牙設備相關的節果 將action指定為：ACTION_FOUND

			IntentFilter intent = new IntentFilter();
			intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver來取得搜索節果
			intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
			intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

			// 注冊廣播接收器
			registerReceiver(searchDevices, intent);
			// 標示已註冊
			isRegistered = true;
			// 防止重復添加的數據
			lstDevices.clear();
			mList.clear();

			// 掃描藍牙設備，最少要12秒，功耗也非常大
			mBluetoothAdapter.startDiscovery();

			break;
		case R.id.btn_close_program:
			closeProgram();
			break;
		}
	}

	/**
	 * 
	 * 廣播
	 */

	private BroadcastReceiver searchDevices = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();

			if (bundle != null) {
				Object[] lstName = bundle.keySet().toArray();

				// 顯示所有收到的消息及其細節
				for (int i = 0; i < lstName.length; i++) {
					String keyName = lstName[i].toString();
					Log.v(TAG + "|" + keyName, String.valueOf(bundle.get(keyName)));
				}
			}

			// 搜索遠程藍牙設備時，取得設備的MAC地址
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// 代表遠程藍牙適配器的對象取出
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				String str = device.getName() + "|" + device.getAddress();
				if (lstDevices.indexOf(str) == -1) {// 防止重復添加
					lstDevices.add(str); // 獲取設備名稱和mac地址

					Map<String, Object> item = new HashMap<String, Object>();
					item.put("btView", R.drawable.bluetooth);
					item.put("device", getStringFromResource(R.string.device_name));
					item.put("devicename", device.getName());
					item.put("bluetooth", getStringFromResource(R.string.bluetooth_address));
					item.put("btAddress", device.getAddress());
					mList.add(item);
					System.out.println(str);
				}

				// 起到更新的效果
				/*
				 * 你在用adapter.notifyDataSetChanged()
				 * 刷新listview的時候，如果當前listview還未來得及刷新， 你就去觸摸listview，就會導致跳出出錯信息
				 * 為避免出現java.lang.IllegalStateException: The content of the
				 * adapter has changed but ListView did not receive a
				 * notification，在呼叫notifyDataSetChanged 之前，先隱藏listview
				 */
				mDeviceListView.setVisibility(View.GONE);
				adtDevices.notifyDataSetChanged();
				mDeviceListView.setVisibility(View.VISIBLE);
			}
		}

	};

	public class Clock implements Runnable {
		Handler handler = new Handler(); // 啟動計時器

		@Override
		public void run() {
			for (int i = 12; i >= 1; i--) {
				updateClock(i);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			updateScanButton();
		}

		public void updateClock(int sec) {
			final int s = sec;
			handler.post(new Runnable() {
				@Override
				public void run() {
					mBtnScanBluetooth
							.setText(getStringFromResource(R.string.wait) + " (" + s + ")");
					; // 更新clock
				}
			});
		}

		public void updateScanButton() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					mBtnScanBluetooth.setText(getStringFromResource(R.string.scan_bluetooth));
					mBtnScanBluetooth.setEnabled(true);
					mBluetoothAdapter.cancelDiscovery();
				}
			});
		}
	}

	private void closeProgram() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		dialog.setTitle(getStringFromResource(R.string.check_dialog));
		dialog.setMessage(getStringFromResource(R.string.check_exit_programe));
		dialog.setIcon(android.R.drawable.ic_dialog_info);
		dialog.setCancelable(true);
		dialog.setPositiveButton(getStringFromResource(R.string.confirm),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
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
	}

	private String getStringFromResource(int id) {
		return getResources().getString(id);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.v("TAG", "onStart");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.v("TAG", "onStop");
		if(mConnectionDialog != null)
			mConnectionDialog.dismiss();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("TAG", "onDestroy");
		// 銷毀廣播
		if (isRegistered) {
			this.unregisterReceiver(searchDevices);
		}

		try {
			mWRFile.writeFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			closeProgram();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

}
