package com.emmt.awiditeminventory;

import com.emmt.plus.device.HandyDeviceHB;
import com.emmt.plus.device.ReaderErrorRespondException;
import com.emmt.plus.device.ReaderSleepingException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PowerLevelActivity extends Activity {
	private SeekBar mSeekBar; // 功率拉霸
	private Button mBtnConfirm; // 確認按鍵
	private Button mBtnCancel; // 取消按鍵
	private TextView mTxtPowerValue; // 功率目前的值
	private TextView mTxtVersion;
    private TextView mTxtMainboardVersion;
	private int mPower = 0;
	private String mVersion = "";
    private String mMainboardVersion = "";
	private HandyDeviceHB mHBDevice;
	private final static String TAG = PowerLevelActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持螢幕亮著
		setContentView(R.layout.powerlevel);
		GlobalVariable globalVariable = (GlobalVariable) getApplicationContext();
		//mHBDevice = globalVariable.getHBDevice();
		mHBDevice = globalVariable.getHandDeviceHB();
		try {
			mVersion = mHBDevice.getVersion();
            mMainboardVersion = mHBDevice.getMainboardVersion();
			mPower = mHBDevice.getPowerLevel();
		} catch (ReaderErrorRespondException e) {
			mPower = 0;
			mVersion = "";
			Log.v(TAG, e.getMessage());
		} catch (ReaderSleepingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setupViewComponent();
		setTitle(getStringFromResource(R.string.power_setting));
	}
	private void setupViewComponent() {
		mSeekBar = (SeekBar) this.findViewById(R.id.SeekBar_powerlevel);
		mSeekBar.setOnSeekBarChangeListener(seekBarChange);
		mSeekBar.setProgress(mPower);
		mBtnConfirm = (Button) findViewById(R.id.Btn_powerlevelSubmit);
		mBtnConfirm.setOnClickListener(btnClick);
		mBtnCancel = (Button) findViewById(R.id.Btn_powerlevelCancel);
		mBtnCancel.setOnClickListener(btnClick);
		mTxtPowerValue = (TextView) findViewById(R.id.txt_powerValue);
		mTxtPowerValue.setText(String.valueOf(mPower));
		mTxtVersion = (TextView) findViewById(R.id.version);
		mTxtVersion.setText(mVersion);
        mTxtMainboardVersion = (TextView) findViewById(R.id.mainboardVersion);
        mTxtMainboardVersion.setText(mMainboardVersion);
	}
	
	private SeekBar.OnSeekBarChangeListener seekBarChange = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// 檢驗事件觸發是否來自使用者，否則當程式呼叫mSeekBar.setProgress時，會觸發該事件，但mTxtPowerValue尚未初始化，
						// 會造成mTxtPowerValue.setText這行出現NullPointer的錯誤
			if(fromUser) 
			 mTxtPowerValue.setText(String.valueOf(progress));  // 顯示目前的設定值
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}
	};

	private View.OnClickListener btnClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId(); // 取得ID

			switch (id) {
			case R.id.Btn_powerlevelSubmit:
				int power = Integer.parseInt(mTxtPowerValue.getText().toString());
				boolean bool = false;
				try {
					bool = mHBDevice.setPowerLevel(power);
				} catch (ReaderErrorRespondException e) {
					Log.v(TAG, e.getMessage());
				} catch (ReaderSleepingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (bool) 
					Toast.makeText(PowerLevelActivity.this, "Successfully",
							Toast.LENGTH_LONG).show();
				else
					Toast.makeText(PowerLevelActivity.this, "Failed",
							Toast.LENGTH_LONG).show();
				break;
			case R.id.Btn_powerlevelCancel:
				PowerLevelActivity.this.finish();
				break;
			}
		}
	};
	
	private String getStringFromResource(int id) {
		return getResources().getString(id);
	}
}
