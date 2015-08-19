package com.emmt.awiditeminventory;


import java.text.SimpleDateFormat;
import java.util.*;

import com.emmt.plus.device.HandyDeviceHB;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class ExpandAvtivity extends ExpandableListActivity {
    public static final String GROUP_NAME = "group Name";
    public static final String CHILD_NAME = "child Name";
    private String[] groupTitle = new String[2];
    private String[][] childTitle = new String[3][3];
    private boolean isConnection = false; // 判斷是離線模式或連線模式
    private String mDeviceType = "";
    private final static String TAG = "ExpandAvtivity";
    private HandyDeviceHB mHBDevice;

    private enum GROUP {
        READER_INFO, SYSTEM_SETTING, AWID
    }

    ;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持螢幕亮著
        setContentView(R.layout.expandactivity);
        // 檢查連線模式
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        isConnection = bundle.getBoolean("isConnection");

        // 獲得BluetoothAdapter對象
        GlobalVariable globalVariable = (GlobalVariable) getApplicationContext();
        if (isConnection) {
            mHBDevice = globalVariable.getHandDeviceHB();
            mHBDevice.startCommandDispatching();
            mHBDevice.startRespondenceAnalyse();
        }

        globalVariable.setCurrentTime(getCurrenDate());
        mDeviceType = this.getIntent().getExtras().getString("device");
        Log.v(TAG, "型號: " + mDeviceType);
        initParams();
        setupViewComponent();
        initTitle();
    }

    private void initParams() {
        groupTitle[0] = getStringFromResource(R.string.reader_info);
        groupTitle[1] = getStringFromResource(R.string.system_setting);
        //groupTitle[2] = getStringFromResource(R.string.awid);

        childTitle[0][0] = getStringFromResource(R.string.battery);
        childTitle[0][1] = getStringFromResource(R.string.power_setting);
        childTitle[1][0] = getStringFromResource(R.string.create_item_data);
        childTitle[1][1] = getStringFromResource(R.string.inventory);
        childTitle[1][2] = getStringFromResource(R.string.search_item);
        //childTitle[2][0] = getStringFromResource(R.string.official_website);
    }

    public String getCurrenDate() {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss"); // 制定輸出格式
        Date date = new Date();
        String strDate = sdFormat.format(date);

        return strDate;
    }

    private void setupViewComponent() {
        List<Map<String, String>> groupList = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> childList2D = new ArrayList<List<Map<String, String>>>();
        /*
		 * groupList包含物件的數量等於childList2D包含物件的數量
		 */

        for (int i = 0; i < groupTitle.length; i++) {
            Map<String, String> group = new HashMap<String, String>();
            group.put(GROUP_NAME, groupTitle[i]);
            groupList.add(group);
        }
        buildChildFrame(groupTitle.length, childList2D);

        MyExAdapter myAdapter = new MyExAdapter(this, groupList, childList2D);
        setListAdapter(myAdapter);
        ExpandableListView exListView = this.getExpandableListView();
		/*
		 * 展開所有的Group
		 */
        int size = myAdapter.getGroupCount();
        for (int i = 0; i < size; i++) {
            exListView.expandGroup(i);
        }

		/*
		 * 不能點擊收縮
		 */
        exListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
                                        long id) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        exListView.setOnChildClickListener(childClickListener);
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

    private void buildChildFrame(int groupLength, List<List<Map<String, String>>> childList2D) {
		/*
		 * childTitle裡個別的數量
		 */
        int[] childNum = {2, 3}; // 設定的數量要配合adapter裡是否有相對應的位置
        for (int i = 0; i < groupLength; i++) {
            List<Map<String, String>> childList = new ArrayList<Map<String, String>>();
            for (int j = 0; j < childNum[i]; j++) {
                Map<String, String> child = new HashMap<String, String>();
                child.put(CHILD_NAME, childTitle[i][j]);
                childList.add(child);
            }
            childList2D.add(childList);
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (!isConnection) {
            this.setTitle(getStringFromResource(R.string.offline));
            return;
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.v("ExpandAvtivity", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregisterReceiver(bluetoothConnectionState); //
        // 先註銷廣播，在執行斷線，否則正常狀況下斷線會啟動service

        if (mHBDevice != null) { // 讀取器斷線
            mHBDevice.disconnect();
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

    private void closeProgram() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("確認視窗");
        dialog.setMessage("確定要結束藍芽連線");
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setCancelable(true);
        dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mHBDevice != null) { // 讀取器斷線
                    mHBDevice.disconnect();
                }
                finish();
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    private BroadcastReceiver bluetoothConnectionState = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Toast.makeText(context, getStringFromResource(R.string.disconnection) + action, Toast.LENGTH_SHORT).show();
            Intent disconnIntent = new Intent("RECONNECT");
            ExpandAvtivity.this.sendBroadcast(disconnIntent);
            ExpandAvtivity.this.finish();
        }
    };

    private OnChildClickListener childClickListener = new OnChildClickListener() {

        @Override
        public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
                                    int childPosition, long id) {

            System.out.println("groupPosition: " + groupPosition);
            System.out.println("childPosition: " + childPosition);


            TextView txt = (TextView) view.findViewById(R.id.MyAdapter_TextView_title); // 取得子集合的title
            TextView txtInfo = (TextView) view.findViewById(R.id.MyAdapter_TextView_info); // 取得子集合的info
            String title = (String) txt.getText();

            if (title.equals(childTitle[0][0]) && isConnection) {
                if (mDeviceType.equals("HB-1000")) {
                    int b = 0;
                    try {
                        b = mHBDevice.getBatteryFromHB1000(); // 得到電量
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        b = 0;
                    }
                    if (b <= 20) // 電量小於20%，以紅色警示
                        txtInfo.setTextColor(Color.RED);
                    else
                        txtInfo.setTextColor(Color.BLACK);
                    txtInfo.setText("" + b + " %");
                } else if (mDeviceType.equals("HB-2000")) {
                    txtInfo.setTextColor(Color.RED);
                    txtInfo.setText(getStringFromResource(R.string.unsupport));
                } else {

                }
            } else if (title.equals(childTitle[0][1]) && isConnection) {
                goNextPage(PowerSettingActivity.class);
            } else if (title.equals(childTitle[1][0])) {
                goNextPage(DataBuilderActivity.class);
            } else if (title.equals(childTitle[1][1]) && isConnection) {
                goNextPage(InventoryActivity.class);
            } else if (title.equals(childTitle[1][2]) && isConnection) {
                //goNextPage(PreSelectionActivity.class);
                goNextPage(InventoryExistedTagActivity.class);
            } else if (title.equals(childTitle[2][0])) {
                goNextPage(OfficeWebActivity.class);
            } else {
                Toast.makeText(ExpandAvtivity.this, getStringFromResource(R.string.offline), Toast.LENGTH_LONG).show();
            }

            return true;
        }

        public void goNextPage(Class<?> cls) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putBoolean("isConnection", isConnection);
            intent.putExtras(bundle);
            intent.setClass(ExpandAvtivity.this, cls);
            startActivity(intent);
        }

    };

    private String getStringFromResource(int id) {
        return getResources().getString(id);
    }

}