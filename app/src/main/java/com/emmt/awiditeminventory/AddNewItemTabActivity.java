package com.emmt.awiditeminventory;

import java.io.ByteArrayOutputStream;

import com.emmt.Utility.SoundTool;
import com.emmt.database.NewListDataSQL;
import com.emmt.plus.device.HandyDeviceHB;
import com.emmt.plus.device.ReaderErrorRespondException;
import com.emmt.plus.device.ReaderSleepingException;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class AddNewItemTabActivity extends Activity {
    private final static String TAG = AddNewItemTabActivity.class.getSimpleName();
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

    private volatile boolean mActivityFlag = true;
    private Thread mHandleTagThread;
    private final Handler handler = new Handler();

    private AddNewItemTabFragment1 NecessaryColumn;
    private AddNewItemTabFragment2 OptionalColumn;
    private AddNewItemTabFragment3 ImageColumn;
    private MyTabInfoContainner tabInfo = new MyTabInfoContainner();

    private HandyDeviceHB mHBDevice = null;
    private long mLastClickTime = 0;

    private Button mBtnReadTags;
    private int mCurrentPower = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.add_new_item_tab_layout);
        setupViewComponent();
        // 檢查連線模式
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        isConnection = bundle.getBoolean("isConnection");

        // 獲得BluetoothAdapter對象
        GlobalVariable globalVariable = (GlobalVariable) getApplicationContext();
        if (isConnection) {
            mHBDevice = globalVariable.getHandDeviceHB();
            mHBDevice.switchToMultiTagMode(false);
            mHBDevice.enableHandyTrigger(false);
        }
        else {
            mBtnReadTags.setEnabled(false);
            setTitle(getStringFromResource(R.string.offline));
        }
        // 以輔助類獲得資料庫對象
        db = helper.getReadableDatabase();
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
                        Log.v(TAG, "position: " + position);
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        mBtnReadTags = (Button) findViewById(R.id.btn_read_tag);
        NecessaryColumn = new AddNewItemTabFragment1();
        NecessaryColumn.setTabInfoContainner(tabInfo);
        OptionalColumn = new AddNewItemTabFragment2();
        OptionalColumn.setTabInfoContainner(tabInfo);
        ImageColumn = new AddNewItemTabFragment3();
        ImageColumn.setTabInfoContainner(tabInfo);

        final ActionBar actBar = getActionBar();
        actBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                Log.v(TAG, "position: " + tab.getPosition());
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
                .setTabListener(new MyTabListener(NecessaryColumn)));

        actBar.addTab(actBar.newTab().setText(getStringFromResource(R.string.extended_column))
                .setTabListener(new MyTabListener(OptionalColumn)));

        actBar.addTab(actBar.newTab().setText(getStringFromResource(R.string.photo_column))
                .setTabListener(new MyTabListener(ImageColumn)));


        actBar.getTabAt(0).setTabListener(new TabListener() {

            @Override
            public void onTabSelected(Tab tab, FragmentTransaction ft) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                // TODO Auto-generated method stub
                tabInfo.setTxtEPC(NecessaryColumn.mTxtEPC.getText().toString().toUpperCase());
                tabInfo.setTxtTID(NecessaryColumn.mTxtTID.getText().toString().toUpperCase());
            }

            @Override
            public void onTabReselected(Tab tab, FragmentTransaction ft) {
                // TODO Auto-generated method stub

            }
        });
        */


    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mHBDevice != null) {
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
                }
                else {
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

        mHandleTagThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mActivityFlag) {
                    try {
                        final String epc = mHBDevice.getInventoryResult()[0];
                        if (HandyDeviceHB.ERROR_TAG.equals(epc))
                            break;
                        showEPC(epc);

                        final String tid = mHBDevice.getTID2();
                        showTID(tid);

                    } catch (ReaderErrorRespondException e) {
                        e.printStackTrace();
                    } catch (ReaderSleepingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Toast.makeText(AddNewItemTabActivity.this, "Reader is sleeping, wait for 5 second", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        mHandleTagThread.start();
    }


    public void showEPC(final String epc) {
        if (epc.equals("TIME_OUT")) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    NecessaryColumn.mTxtEPC.setText("Reading EPC Time out");
                }
            });
        }
        else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    NecessaryColumn.mTxtEPC.setText(epc);
                }
            });
        }
    }

    public void showTID(final String tid) {
        if(tid.endsWith("TIME_OUT")) {
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
                    NecessaryColumn.mTxtTID.setText(tid);
                }
            });
        }
    }

    public void onclick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.btn_read_tag:
                processReadTag();
                break;
            case R.id.btn_store_tab:
                // 檢查EPC、TID與財產名稱是否為空值
                if (hasNullEditText()) {
                    showToastMessage(getStringFromResource(R.string.lost_column));
                    return;
                }
                if (isUniqueEPC(NecessaryColumn.mTxtEPC.getText().toString())) {
                    // insert to database
                    importToDatabase();
                } else {
                    showToastMessage(getStringFromResource(R.string.duplicated_epc));
                }
                finish();
                break;
        }
    }

    private void processReadTag() {
        //mHBDevice.startReadSingleTag();
        try {
            final String epc = mHBDevice.getEPC();
            showEPC(epc);
             final String tid = mHBDevice.getTID2();
            showTID(tid);
        } catch (ReaderErrorRespondException e) {
            e.printStackTrace();
            Toast.makeText(AddNewItemTabActivity.this, "Reader connection is failed, please reconnect reader", Toast.LENGTH_LONG).show();
        } catch (ReaderSleepingException e) {
            e.printStackTrace();
            Toast.makeText(AddNewItemTabActivity.this, "Reader is sleeping, wait for 5 second", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isUniqueEPC(String epc) {
        // 檢查該EPC是否已經存在於資料庫中
        Cursor c = db.rawQuery("select * from WarehouseInventoryTable where epc = \"" + epc + "\"",
                null); // 搜尋指定的標籤
        if (c == null) // 不存在，該EPC尚未建立，是唯一的EPC，傳回true
            return true;

        if (c.getCount() == 0) { // 不存在，該EPC尚未建立，是唯一的EPC，傳回true
            return true;
        } else {
            return false; // 該EPC已存在，傳回false
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

    private void importToDatabase() {
        String epc = NecessaryColumn.mTxtEPC.getText().toString().toUpperCase();
        String tid = NecessaryColumn.mTxtTID.getText().toString().toUpperCase();
        String txtManager = NecessaryColumn.mTxtManager.getText().toString();
        String txtPropertyIndex = NecessaryColumn.mTxtPropertyIndex.getText().toString();
        String txtPropertyName = NecessaryColumn.mTxtPropertyName.getText().toString();
        // 第2頁以後的頁籤，若沒被點選到，無法初始化，所以內含物件會是null，要先判斷
        String txtMfcName = (OptionalColumn.mTxtMfcName == null) ? "" : OptionalColumn.mTxtMfcName
                .getText().toString();
        String txtModelType = (OptionalColumn.mTxtModelType == null) ? ""
                : OptionalColumn.mTxtModelType.getText().toString();
        String txtSerialNumber = (OptionalColumn.mTxtSerialNumber == null) ? ""
                : OptionalColumn.mTxtSerialNumber.getText().toString();
        String txtStorageLocation = (OptionalColumn.mTxtStorageLocation == null) ? ""
                : OptionalColumn.mTxtStorageLocation.getText().toString();
        String txtInventoryCount = (OptionalColumn.mTxtInventoryCount == null) ? "0"
                : OptionalColumn.mTxtInventoryCount.getText().toString();
        String txtMoney = (OptionalColumn.mTxtMoney == null) ? "0" : OptionalColumn.mTxtMoney
                .getText().toString();
        int flag = (OptionalColumn.mChbManageCheck == null) ? 1 : (OptionalColumn.mChbManageCheck
                .isChecked()) ? 1 : 0;
        String txtAccounts = (OptionalColumn.mTxtAccounts == null) ? ""
                : OptionalColumn.mTxtAccounts.getText().toString();
        String txtSection = (OptionalColumn.mTxtSection == null) ? "" : OptionalColumn.mTxtSection
                .getText().toString();
        String txtDate = (OptionalColumn.mTxtDate == null) ? "" : OptionalColumn.mTxtDate.getText()
                .toString();
        String managerUnit = (NecessaryColumn.mSpnManagerUnit == null) ? ""
                : NecessaryColumn.mSpnManagerUnit.getSelectedItem().toString();
        String propertyType = (NecessaryColumn.mSpnPropertyType == null) ? ""
                : NecessaryColumn.mSpnPropertyType.getSelectedItem().toString();
        String usageStates = (OptionalColumn.mSpnUsageStates == null) ? getStringFromResource(R.string.using)
                : OptionalColumn.mSpnUsageStates.getSelectedItem().toString();

        Bitmap bmp = null;
        if (ImageColumn.mImageView1 == null) {
            Drawable canmera = this.getResources().getDrawable(R.drawable.picture);
            bmp = ((BitmapDrawable) canmera).getBitmap();
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
        // 添加方法
        long long1 = db.insert(table_name, "", cv);
        // 添加成功後返回行號，失敗後返回-1
        if (long1 == -1) {
            showToastMessage(getStringFromResource(R.string.add_failed));
        } else {
            showToastMessage(getStringFromResource(R.string.add_successfully));
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
                mHBDevice.stopAllAction();
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
