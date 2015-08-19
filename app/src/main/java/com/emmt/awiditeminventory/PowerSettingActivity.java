package com.emmt.awiditeminventory;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.emmt.plus.device.HandyDeviceHB;
import com.emmt.plus.device.ReaderErrorRespondException;
import com.emmt.plus.device.ReaderSleepingException;
import com.triggertrap.seekarc.SeekArc;

/**
 * Created by Owner on 2015/6/10.
 */
public class PowerSettingActivity extends Activity {
    private final static String TAG = PowerSettingActivity.class.getSimpleName();
    private SeekArc mSeekArc;
    private TextView mTxtSeekArcProgress;
    private Button mBtnConfirm;
    private HandyDeviceHB mHBDevice;
    private TextView mTxtVersion;
    private TextView mTxtMainboardVersion;
    private long mLastClickTime = 0;
    private int mCurrentProgress = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keepScreenLight();
        setContentView(R.layout.activity_power_setting);
        initUI();
        initReader();
    }

    private void keepScreenLight() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持螢幕亮著
    }

    private void initUI() {
        mSeekArc = (SeekArc) findViewById(R.id.seekArc);
        mTxtSeekArcProgress = (TextView) findViewById(R.id.seekArcProgress);
        mBtnConfirm = (Button) findViewById(R.id.Btn_powerlevelSubmit);
        mTxtVersion = (TextView) findViewById(R.id.core_version);
        mTxtMainboardVersion = (TextView) findViewById(R.id.mainboard_version);

        mSeekArc.setMax(220);
        mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onProgressChanged(SeekArc seekArc, int progress,
                                          boolean fromUser) {
                mCurrentProgress = progress;
                mTxtSeekArcProgress.setText(getString(R.string.current_power, String.valueOf(progress)));
            }
        });

        mBtnConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                try {
                    boolean s = mHBDevice.setPowerLevel(mCurrentProgress);
                    if(s == true) {
                        Toast.makeText(PowerSettingActivity.this, "功率設定成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else
                        Toast.makeText(PowerSettingActivity.this, "功率設定失敗", Toast.LENGTH_SHORT).show();
                } catch (ReaderErrorRespondException e) {
                    e.printStackTrace();
                } catch (ReaderSleepingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initReader() {
        GlobalVariable globalVariable = (GlobalVariable) getApplicationContext();
        mHBDevice = globalVariable.getHandDeviceHB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String mVersion = mHBDevice.getVersion();
            String mMainboardVersion = mHBDevice.getMainboardVersion();
            int mPower = mHBDevice.getPowerLevel();

            mTxtVersion.setText(getString(R.string.core_version, mVersion));
            mTxtMainboardVersion.setText(getString(R.string.mainboard_version, mMainboardVersion));
            mTxtSeekArcProgress.setText("" + mPower);
            mSeekArc.setProgress(mPower);
        } catch (ReaderErrorRespondException e) {
            Log.v(TAG, e.getMessage());
        } catch (ReaderSleepingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
