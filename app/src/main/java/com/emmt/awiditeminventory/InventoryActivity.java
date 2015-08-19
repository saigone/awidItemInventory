package com.emmt.awiditeminventory;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.emmt.Json.JSONParser;
import com.emmt.Utility.SoundTool;
import com.emmt.Utility.ToolUtil;
import com.emmt.database.NewListDataSQL;
import com.emmt.plus.device.HandyDeviceHB;
import com.emmt.plus.device.ReaderErrorRespondException;
import com.emmt.plus.device.ReaderSleepingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class InventoryActivity extends Activity {
    private TableLayout mInventoryTablelayout; // 盤點時記錄用的layout
    private TextView mCountView; // 顯示讀取到的標籤總數
    private TextView mCurrentPower;
    private SQLiteDatabase db; // SQLiteDatabase對象
    private String db_name = "warehouse.db"; // 資料庫名
    private NewListDataSQL helper = new NewListDataSQL(InventoryActivity.this, db_name); // 輔助類名
    private Map<String, String> mEPCandMappingTable = new HashMap<String, String>();
    private final Handler handler = new Handler();
    private SoundTool mSoundTool; // 音效控制
    private HandleTags mHandleTags;
    private Timer mUpdateTimer; // 計時器
    private CheckBox mSoundCheckBox;
    private final static String TAG = InventoryActivity.class.getSimpleName();
    private Button mUpdateJsonData;

    private HandyDeviceHB mHBDevice;
    private long mLastClickTime = 0;
    private Button mBtnInventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.inventory);
        GlobalVariable globalVariable = (GlobalVariable) getApplicationContext();
        mHBDevice = globalVariable.getHandDeviceHB();
        mHBDevice.switchToMultiTagMode(true);
        mHBDevice.enableHandyTrigger(false);
        setComponent();
        initialDatabase();
        initialMappingTable();
        initialSound();
        initialHB1000();
    }

    private void initialHB1000() {
        mHandleTags = new HandleTags();
        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(mHandleTags, 1000, 150);
    }

    private void initialSound() {
        mSoundTool = new SoundTool(this, R.raw.song);
    }

    private void beep() {
        mSoundTool.play();
    }

    private void initialMappingTable() {
        mEPCandMappingTable = this.queryEPCandCarname(); // 取得EPC與對應的財產名稱
    }

    private Map<String, String> queryEPCandCarname() {
        Map<String, String> map = new HashMap<String, String>(); //第一個參數是EPC，第2個參數是車號
        Cursor c = db.rawQuery(
                "select epc, propertyname from WarehouseInventoryTable ORDER BY _ID ASC ", null); // 搜尋指定的標籤與車號

        if (c == null) // 沒找到，返回
            return null;

        if (c.getCount() == 0) { // 沒有筆數，返回
            return null;
        } else {
            c.moveToFirst(); // 移到第一行
            do {
                map.put(c.getString(0), c.getString(1)); // 將EPC與對應的財產名稱放到MAP裡
            } while (c.moveToNext()); // 移到下一行
        }

        return map;
    }

    private void initialDatabase() {
        // 以輔助類獲得資料庫對象
        db = helper.getReadableDatabase();
    }

    private void setComponent() {
        mBtnInventory = (Button) findViewById(R.id.btn_inventory);
        mInventoryTablelayout = (TableLayout) findViewById(R.id.TableLayout_inventory);
        mCountView = (TextView) findViewById(R.id.editTextCount);
        mSoundCheckBox = (CheckBox) findViewById(R.id.check_sound);
        mUpdateJsonData = (Button) findViewById(R.id.btn_json_upload);
        mCurrentPower = (TextView) findViewById(R.id.txt_current_power);

        mProgressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.upload_failed), Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.upload_success), Toast.LENGTH_LONG).show();
                        clearTagTable();
                        mCountView.setText("");
                        mHandleTags.clearTagList();
                        mUpdateJsonData.setEnabled(true);
                        break;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            int power = mHBDevice.getPowerLevel();
            mCurrentPower.setText(getString(R.string.current_power, "" + power));
        } catch (ReaderErrorRespondException e) {
            mCurrentPower.setText(getString(R.string.current_power, "0"));
            e.printStackTrace();
        } catch (ReaderSleepingException e) {
            mCurrentPower.setText(getString(R.string.current_power, "0"));
            e.printStackTrace();
        }
    }

    public void onclick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.btn_inventory:
                processInventory();
                break;
            case R.id.btn_clear:
                if (mHBDevice.isRunningInventory()) {
                    Toast.makeText(InventoryActivity.this,
                            getStringFromResource(R.string.stop_inventory), Toast.LENGTH_LONG).show();
                } else {
                    if (mInventoryTablelayout.getChildCount() == 1)
                        return;

                    clearTagTable();
                    mCountView.setText("");
                    mHandleTags.clearTagList();
                }

                break;
            case R.id.btn_record:

                break;
            case R.id.btn_arrange:
                if (mHBDevice.isRunningInventory()) {
                    Toast.makeText(InventoryActivity.this,
                            getStringFromResource(R.string.stop_inventory), Toast.LENGTH_LONG).show();
                } else {
                    mHandleTags.arrangeTagList();
                    clearTagTable();
                    List<String> list = mHandleTags.getTagList();
                    for (String tag : list) {
                        mHandleTags.addNewRow(tag);
                    }
                }
                break;
            case R.id.btn_json_upload:
                if (mHBDevice.isRunningInventory()) {
                    Toast.makeText(InventoryActivity.this,
                            getStringFromResource(R.string.stop_inventory), Toast.LENGTH_LONG).show();
                } else {
                    int count = mHandleTags.getTagCount();
                    if (count > 0) {
                        List<String> tags = mHandleTags.getTagList();
                        Iterator<String> it = tags.iterator();
                        StringBuilder sb = new StringBuilder();
                        while (it.hasNext()) {
                            sb.append(it.next());
                            sb.append(",");
                        }

                        String uploadTags = sb.toString().substring(0, sb.toString().length() - 1);
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("epc", uploadTags));
                        params.add(new BasicNameValuePair("product", "coffee"));
                        params.add(new BasicNameValuePair("holder", "hank"));
                        params.add(new BasicNameValuePair("udate", ToolUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss")));

                        UploadCoffeeTask upload = new UploadCoffeeTask(params);
                        upload.execute();

                        mUpdateJsonData.setEnabled(false);
                    }
                }
                break;
        }
    }

    private void processInventory() {
        if (!mHBDevice.isRunningInventory()) {
            mHBDevice.startInventory();
            mBtnInventory.setText(getString(R.string.stop));
        }
        else {
            mHBDevice.stopAllAction();
            mBtnInventory.setText(getString(R.string.inventory));
        }
    }

    private void clearTagTable() {
        int count = mInventoryTablelayout.getChildCount();
        for (int i = 1; i < count; i++) {
            //每砍一行就向上縮減一行，所以只須砍第一行即可
            mInventoryTablelayout.removeViewAt(1);
        }
    }

    public class HandleTags extends TimerTask {
        private List<String> arraylist = new ArrayList<String>();
        private int btnID = 0; // 每個新創的BUTTON都要給一個ID

        @Override
        public void run() {
            try {
                String[] tags = mHBDevice.getInventoryResult();
                for (String tag : tags) {
                    Log.v(TAG, "TAG: " + tag);
                    updateRowTable(tag); //更新至tablelayout上
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private void updateRowTable(String tag) {
            if (tag == HandyDeviceHB.ERROR_TAG) {
                return;
            }

            if (mSoundCheckBox.isChecked())
                //beep();

                if (!arraylist.contains(tag)) {
                    arraylist.add(tag);
                    addNewRow(tag);
                    updateCounter(arraylist.size());
                } else {
                    updateTagCount(tag);
                }
        }

        private void updateTagCount(String tag) {
            int index = arraylist.indexOf(tag);
            TableRow row = (TableRow) mInventoryTablelayout.getChildAt(index + 1);
            if(row != null) {
                final TextView countView = (TextView) row.getChildAt(1);
                final int count = Integer.parseInt(countView.getText().toString()) + 1;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        countView.setText("" + count);
                    }
                });
            }
        }

        private void updateCounter(int size) {
            final int count = size;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mCountView.setText("" + count);
                }
            });
        }

        public List<String> getTagList() {
            return arraylist;
        }

        public void arrangeTagList() {
            Collections.sort(arraylist);
        }

        public int getTagCount() {
            return arraylist.size();
        }

        public void clearTagList() {
            arraylist.clear();
        }

        public void addNewRow(String tag) {
            final TableRow row = new TableRow(InventoryActivity.this);
            if (btnID % 2 == 0)
                row.setBackgroundColor(Color.GRAY);

            TextView tagView = buildTextiew();
            tagView.setText(tag);

            TextView countView = buildTextiew();
            countView.setText("1");


            TextView propertyView = buildTextiew();
            if (mEPCandMappingTable != null && mEPCandMappingTable.containsKey(tag)) // 若mappingTable有值，並檢查是否有相對應的資料
                propertyView.setText(mEPCandMappingTable.get(tag)); // 取出對應的值
            else
                propertyView.setText(""); // 無該筆對應資料，秀空值

            Button show = new Button(InventoryActivity.this); // 顯示資料按鍵
            show.setPadding(5, 0, 0, 0);
            show.setId(btnID++); // button需要給予ID
            show.setTag(tag); // 每顆按鍵儲存著標籤的資料
            show.setText("Show");
            show.setTextColor(Color.RED);
            show.setTextSize(15);
            show.setBackgroundDrawable(InventoryActivity.this.getResources().getDrawable(
                    R.drawable.bg_9patchbutton));
            show.setOnClickListener(showBtnClick);


            row.addView(tagView);
            row.addView(countView);
            row.addView(show);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    mInventoryTablelayout.addView(row, new TableLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                }
            });
        }

        private TextView buildTextiew() {
            TextView view = new TextView(InventoryActivity.this);
            view.setPadding(10, 0, 0, 0);
            view.setTextSize(15);
            view.setTextColor(InventoryActivity.this.getResources().getColor(R.color.red));

            return view;
        }

        private View.OnClickListener showBtnClick = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button btn = (Button) findViewById(v.getId());
                String epc = btn.getTag().toString();
                showInfo(epc);
            }

            private void showInfo(String epc) {
                Cursor c = db.rawQuery("select * from WarehouseInventoryTable where epc = \"" + epc
                        + "\" ORDER BY _ID ASC", null);
                if (c == null || c.getCount() == 0) {
                    showNullMessage();
                } else {
                    c.moveToFirst();
                    Bitmap bitmap = getImage(c.getString(1));

                    final Drawable drawable = new BitmapDrawable(bitmap);
                    String tid = c.getString(3);
                    String manager = c.getString(4);
                    String propertyName = c.getString(6);
                    String storageLocation = c.getString(10);
                    String manageUnit = c.getString(13);
                    String usageState = c.getString(19);

                    LayoutInflater factory = LayoutInflater.from(InventoryActivity.this);
                    final View textEntryView = factory.inflate(R.layout.tag_detail_info, null);
                    ((TextView) textEntryView.findViewById(R.id.inventory_tag))
                            .setText(getStringFromResource(R.string.epc) + ": " + epc);
                    ((TextView) textEntryView.findViewById(R.id.inventory_tid))
                            .setText(getStringFromResource(R.string.tid) + ": " + tid);
                    ((TextView) textEntryView.findViewById(R.id.inventory_manager))
                            .setText(getStringFromResource(R.string.manager) + ": " + manager);
                    ((TextView) textEntryView.findViewById(R.id.inventory_propertyName))
                            .setText(getStringFromResource(R.string.property_name) + ": "
                                    + propertyName);
                    ((TextView) textEntryView.findViewById(R.id.inventory_storageLocation))
                            .setText(getStringFromResource(R.string.storage_location) + ": "
                                    + storageLocation);
                    ((TextView) textEntryView.findViewById(R.id.inventory_manageUnit))
                            .setText(getStringFromResource(R.string.manager_unit) + ": "
                                    + manageUnit);
                    ((TextView) textEntryView.findViewById(R.id.inventory_usageState))
                            .setText(getStringFromResource(R.string.usage_states) + ": "
                                    + usageState);
                    ((ImageView) textEntryView.findViewById(R.id.inventory_tagPicture))
                            .setBackground(drawable);

                    new AlertDialog.Builder(InventoryActivity.this).setView(textEntryView)
                            .setPositiveButton(getStringFromResource(R.string.confirm), null)
                            .setTitle(getStringFromResource(R.string.tag_info)).show();
                }
                c.close();
            }

            private void showNullMessage() {
                new AlertDialog.Builder(InventoryActivity.this).setIcon(R.drawable.tag)
                        .setTitle(getStringFromResource(R.string.database))
                        .setMessage(getStringFromResource(R.string.no_data))
                        .setNegativeButton(getStringFromResource(R.string.confirm), null).show();

            }

            private Bitmap getImage(String imageName) {
                byte[] bytes = Base64.decode(imageName, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                return bmp;
            }

            private Bitmap rotateBitmap(Bitmap bmp, int rotate) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
                        matrix, true);

                return rotated;
            }
        };
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.v("InventoryActivity", "onPause");
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.v("InventoryActivity", "onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSoundTool != null)
            mSoundTool.release();

        mUpdateTimer.cancel();
        mHBDevice.stopAllAction();
        //mHBDevice.stopInventory();
        mHBDevice.enableHandyTrigger(false);

        db.close();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mHBDevice.isRunningInventory() == true) {
                Toast.makeText(this, getString(R.string.close_inventory_status), Toast.LENGTH_SHORT).show();
                return true;
            }
            else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private String getStringFromResource(int id) {
        return getResources().getString(id);
    }

    //upload to server
    private ProgressDialog mProgressDialog;
    private Handler mProgressHandler;
    private final static int MAX_PROGRESS = 100;

    private class UploadCoffeeTask extends AsyncTask<Void, Integer, Boolean> {
        private final static String URL_TO_PHP = "http://192.168.1.110/PhpProjectJsonSample/uploadCoffee.php";
        private final static String TAG_SUCCESS = "success";
        private List<NameValuePair> params;

        public UploadCoffeeTask(List<NameValuePair> params) {
            this.params = params;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoginDialog();
            mProgressDialog.setProgress(0);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            publishProgress(0);
            /* getting JSON string from URL */
            JSONParser jParser = new JSONParser();
            JSONObject json = jParser.makeHttpRequest(URL_TO_PHP, "POST", this.params);

            publishProgress(30);
            try {
                boolean result = json.getBoolean(TAG_SUCCESS);
                if (result) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            publishProgress(100);

            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            boolean isSuccess = result;
            if (isSuccess) {
                mProgressHandler.sendEmptyMessage(1);
            } else {
                mProgressHandler.sendEmptyMessage(0);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressDialog.setProgress(0);
        }

        private void showLoginDialog() {
            mProgressDialog = new ProgressDialog(InventoryActivity.this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIconAttribute(android.R.attr.alertDialogIcon);
            mProgressDialog.setTitle(R.string.upload);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(MAX_PROGRESS);
//            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
//                    getText(R.string.cancel), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//
//
//                        }
//                    });
            mProgressDialog.show();
        }
    }

}
