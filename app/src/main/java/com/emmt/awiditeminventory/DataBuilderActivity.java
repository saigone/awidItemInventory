package com.emmt.awiditeminventory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import com.emmt.Json.JSONParser;
import com.emmt.Utility.SoundTool;
import com.emmt.database.NewListDataSQL;
import com.system.data.DataRow;
import com.system.data.DataTable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataBuilderActivity extends Activity {
    private final static String TAG = DataBuilderActivity.class.getSimpleName();
    // SQLiteDatabase對象
    SQLiteDatabase db;
    // 資料庫名
    public String db_name = "warehouse.db";
    // 表名
    public String table_name = "WarehouseInventoryTable";
    // 輔助類名
    NewListDataSQL helper = new NewListDataSQL(this, db_name);
    // 保存搜索到的item
    SimpleAdapter mAdapter;
    private static ListView mlistView;
    private List<Map<String, Object>> mList;
    // requestCode對應到startActivityForResult(arg1, arg2)第2個參數
    public static final int FILE_RESULT_CODE = 1;
    //private SoundTool mSoundTool; // 音效控制
    private boolean isConnection = false; // 判斷是離線模式或連線模式
    private String mCurrentTime = "";
    private Button mUpdateJsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持螢幕亮著
        setContentView(R.layout.data_builder);
        // 檢查連線模式
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        isConnection = bundle.getBoolean("isConnection");
        if (!isConnection)
            setTitle(getStringFromResource(R.string.offline));

        GlobalVariable globalVariable = (GlobalVariable) getApplicationContext();
        mCurrentTime = globalVariable.getCurrentTime();

        //initialSound();
        // 以輔助類獲得資料庫對象
        db = helper.getReadableDatabase();
        mlistView = (ListView) findViewById(R.id.item_list);
        mList = new ArrayList<Map<String, Object>>();

        mAdapter = new SimpleAdapter(this, mList, R.layout.list_item_2,
                new String[]{"itemView", "epctitle", "epc", "propertytitle",
                        "property"}, new int[]{R.id.list_item2_view,
                R.id.list_item2_title1, R.id.list_item2_content1,
                R.id.list_item2_title2, R.id.list_item2_content2});
        mlistView.setAdapter(mAdapter);
        mlistView.setTextFilterEnabled(true);
        mlistView.setOnItemClickListener(new itemClickListener(this));

        mUpdateJsonData = (Button) findViewById(R.id.btn_update_data);
    }

    private void initialSound() {
        //mSoundTool = new SoundTool(this, R.raw.song); // 設定音效
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart");
        super.onStart();
        mList.clear(); // 先清空
        Cursor c = db
                .rawQuery(
                        "select epc, propertyname from WarehouseInventoryTable ORDER BY _ID DESC",
                        null);
        if (c == null) {
            System.out.println("null data");
        } else if (c.getCount() == 0) {
            System.out.println("zero data");
        } else {
            c.moveToFirst(); // 游標移到第一行
            do {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("itemView", R.drawable.tag_64);
                item.put("epctitle", "EPC: ");
                item.put("epc", c.getString(0)); // EPC
                item.put("propertytitle", getStringFromResource(R.string.property_name) + ": ");
                item.put("property", c.getString(1)); // propertyname
                mList.add(item);
            } while (c.moveToNext());
        }
        mAdapter.notifyDataSetChanged();
    }

    public void onclick(View view) {
        //beep(); // 呼叫蜂鳴器

        switch (view.getId()) {
            case R.id.btn_add_new_item:
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putBoolean("isConnection", isConnection);
                intent.putExtras(bundle);
                intent.setClass(this, AddNewItemTabActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_import_data:
                Intent i = new Intent(this, FileManager.class);
                startActivityForResult(i, FILE_RESULT_CODE);
                break;
            case R.id.btn_export_data:
                saveProcess(); // 匯出全部的檔案
                long long1 = db.delete(table_name, null, null); // 刪除全部的rows

                // 添加成功後返回行號，失敗後返回-1
                if (long1 != -1) {
                    // showToastMessage("資料已輸出至CarDatabase資料夾");
                    System.out.println("資料已輸出至CarDatabase資料夾");
                }
                // _id歸0
                db.execSQL("Update sqlite_sequence SET seq = 0 Where name = 'WarehouseInventoryTable'");
                clearAllData();
                File sdCardDir = Environment.getExternalStorageDirectory();// 獲取SDCard目錄
                String dir = sdCardDir.getPath();
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(getStringFromResource(R.string.export_msg));
                dialog.setMessage(getStringFromResource(R.string.save_path) + dir + "/CarDatabase");
                dialog.setIcon(android.R.drawable.ic_dialog_info);
                dialog.setCancelable(true);
                dialog.setPositiveButton(getStringFromResource(R.string.confirm),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub

                            }
                        });
                dialog.show();
                break;
            case R.id.btn_update_data:
                if(isNetworkAvailable()) {
                    mUpdateJsonData.setEnabled(false);
                    DownloadCoffeeTask download = new DownloadCoffeeTask();
                    download.execute();
                }
                else {
                    Toast.makeText(this, getString(R.string.internet_disconnect), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void clearAllData() {
        mList.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void saveProcess() {
        Cursor c = db.rawQuery(
                "select * from WarehouseInventoryTable ORDER BY _ID ASC", null); // ASC由小往大輸出
        if (c == null)
            return;

        if (c.getCount() == 0) {
            System.out.println("筆數為0");
            return;
        }

        DataTable recordLog = new DataTable(); // 記錄每張標籤的時間、EPC等資料
        initialRecordLog(recordLog);

        c.moveToFirst(); // 游標移到第一行
        do {
            DataRow dataRow = recordLog.NewRow(); // 新增一行資料表
            dataRow.setValue(0, c.getInt(0)); // 紀錄ID
            Log.v("DataBuilderActivity", "index: " + c.getInt(0));
            // c.getString(1) 為圖檔
            dataRow.setValue(1, c.getString(2)); // 紀錄EPC
            dataRow.setValue(2, c.getString(3)); // 紀錄TID
            dataRow.setValue(3, c.getString(4)); // 保管人
            dataRow.setValue(4, c.getString(5)); // 財產編號
            dataRow.setValue(5, c.getString(6)); // 財產名稱
            dataRow.setValue(6, c.getString(7)); // 製造廠商
            dataRow.setValue(7, c.getString(8)); // 型號
            dataRow.setValue(8, c.getString(9)); // 序號
            dataRow.setValue(9, c.getString(10)); // 存放地點
            dataRow.setValue(10, c.getString(11)); // 初盤數量
            dataRow.setValue(11, c.getString(12)); // 取得金額
            dataRow.setValue(12, c.getString(13)); // 管理單位
            dataRow.setValue(13, c.getString(14)); // 帳列科目
            dataRow.setValue(14, c.getString(15)); // 備註
            dataRow.setValue(15, c.getString(16)); // 取得日期
            String check = (c.getInt(17) == 1) ? "Y" : "N";
            dataRow.setValue(16, check); // 單位列管(Y/N)
            dataRow.setValue(17, c.getString(18)); // 資產分類
            dataRow.setValue(18, c.getString(19)); // 使用狀況
            recordLog.Rows.add(dataRow); // 加入至recordLog裡
        } while (c.moveToNext());

        saveRecordLog(recordLog); // 儲存紀錄
    }

    private void saveRecordLog(DataTable recordLog) {
        // 啟動save Thread
        Thread t = new Thread(
                new SaveFileAction(this, recordLog, "CarDatabase", mCurrentTime));
        t.start();
    }

    private void initialRecordLog(DataTable recordLog) {
        recordLog.Columns.Add("Index");
        recordLog.Columns.Add("EPC");
        recordLog.Columns.Add("TID");
        recordLog.Columns.Add(getStringFromResource(R.string.manager));
        recordLog.Columns.Add(getStringFromResource(R.string.property_index));
        recordLog.Columns.Add(getStringFromResource(R.string.property_name));
        recordLog.Columns.Add(getStringFromResource(R.string.mfc_name));
        recordLog.Columns.Add(getStringFromResource(R.string.model_type));
        recordLog.Columns.Add(getStringFromResource(R.string.serial_number));
        recordLog.Columns.Add(getStringFromResource(R.string.storage_location));
        recordLog.Columns.Add(getStringFromResource(R.string.inventory_count));
        recordLog.Columns.Add(getStringFromResource(R.string.money));
        recordLog.Columns.Add(getStringFromResource(R.string.manager_unit));
        recordLog.Columns.Add(getStringFromResource(R.string.accounts));
        recordLog.Columns.Add(getStringFromResource(R.string.section));
        recordLog.Columns.Add(getStringFromResource(R.string.date));
        recordLog.Columns.Add(getStringFromResource(R.string.manage_check));
        recordLog.Columns.Add(getStringFromResource(R.string.property_type));
        recordLog.Columns.Add(getStringFromResource(R.string.usage_states));
    }

    private void beep() {
     //   mSoundTool.play(); // 發出音效
    }

    public class itemClickListener implements OnItemClickListener {
        private Context _context;

        public itemClickListener(Context context) {
            _context = context;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            TextView epc = (TextView) arg1
                    .findViewById(R.id.list_item2_content1);
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("EPC", epc.getText().toString());
            bundle.putBoolean("isConnection", isConnection);
            intent.putExtras(bundle);
            intent.setClass(DataBuilderActivity.this, UpdateItemTabActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        // requestCode對應到startActivityForResult(arg1, arg2)第2個參數
        // resultCode是指對方回傳setResult(RESULT_OK,i)第一個值
        // data指對方回傳setResult(RESULT_OK,i)第二個值
        if (FILE_RESULT_CODE == requestCode) {
            Bundle bundle = null;
            if (data != null && (bundle = data.getExtras()) != null) {
                String filePath = bundle.getString("file");
                String end = filePath.substring(filePath.lastIndexOf(".") + 1,
                        filePath.length()).toLowerCase();
                if (end.equals("xls")) {
                    String[] exceldata = getExcelData(filePath); // 得到excel資料
                    if (exceldata == null) {
                        showToastMessage(getStringFromResource(R.string.wrong_excel_file));
                        return;
                    }

                    for (int i = 0; i < exceldata.length; i++) {
                        String[] rows = exceldata[i].split(","); // 取得一行資料，以","切割
                        if (rows[0] == null || "".equals(rows[0])) {
                            // 若EPC欄位的值為空字串
                            continue; // 該筆資料則不輸入資料庫
                        }
                        try {
                            importToDatabase(rows);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            showToastMessage(e.getMessage());
                            break;
                        }
                    }
//					showToastMessage("Excel檔案匯入成功");

                } else {
                    showToastMessage(getStringFromResource(R.string.check_excel_file));
                }
            }
        }
    }

    private String[] getExcelData(String filePath) {
        StringBuilder sb = new StringBuilder(); // 建立StringBuilder

        try {
            Workbook workbook = Workbook.getWorkbook(new File(filePath)); // 取得Excel檔案
            Sheet sheet = workbook.getSheet(0); // 取得第1頁 sheet
            int columns = sheet.getColumns(); // 得到欄位數量
            int rows = sheet.getRows(); // 得到行數
            System.out.println("columns: " + columns);
            System.out.println("rows: " + rows);

            // 第0行是標頭，所以從第1行開始
            for (int row = 1; row < rows; row++) {
                // 第0欄是index，所以從第1欄開始
                for (int column = 1; column < columns; column++) {
                    Cell cell = sheet.getCell(column, row); // 取得該欄位資料
                    sb.append(cell.getContents()); // 存入sb
                    sb.append(","); // 以逗號分隔
                }

                sb.append(" ."); // 以空白+句號分隔
            }
        } catch (BiffException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String s = sb.toString(); // 轉成字串
        // 最後1個字元"."刪除，以"."做切割
        String[] array = s.substring(0, s.length() - 1).split("\\.");

        return array;
    }

    private void importToDatabase(String[] rows) throws Exception {
        try {
            if (isUniqueEPC(rows[0])) { // 檢查EPC欄位有無重複
                ContentValues cv = new ContentValues();
                int flag = (rows[15] == "Y") ? 1 : 0;
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.picture);
                // 先把 bitmap 轉成 byte
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte bytes[] = stream.toByteArray();
                // Android 2.2以上才有內建Base64，其他要自已找Libary或是用Blob存入SQLite
                // 把byte變成base64
                String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);

                // 輸入資料庫欄位
                cv.put("imageview", base64);
                cv.put("epc", rows[0]);
                cv.put("tid", rows[1]);
                cv.put("manager", rows[2]);
                cv.put("propertyindex", rows[3]);
                cv.put("propertyname", rows[4]);
                cv.put("mfcname", rows[5]);
                cv.put("modeltype", rows[6]);
                cv.put("serialnumber", rows[7]);
                cv.put("storagelocation", rows[8]);
                cv.put("inventorycount", rows[9]);
                cv.put("money", rows[10]);
                cv.put("unitmanage", rows[11]);
                cv.put("accounts", rows[12]);
                cv.put("section", rows[13]);
                cv.put("date", rows[14]);
                cv.put("managecheck", flag);
                cv.put("propertytype", rows[16]);
                cv.put("usagestates", rows[17]);

                // 添加方法
                long long1 = db.insert(table_name, "", cv);
                // 添加成功後返回行號，失敗後返回-1
                if (long1 == -1)
                    showToastMessage(getStringFromResource(R.string.add_failed));
                // else
                // showToastMessage("新增1筆資料");
            } else {
                showToastMessage(getStringFromResource(R.string.duplicated_epc));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(getStringFromResource(R.string.wrong_excel_file));
        }
    }

    private boolean isUniqueEPC(String epc) {
        // 檢查該EPC是否已經存在於資料庫中
        Cursor c = db.rawQuery(
                "select * from WarehouseInventoryTable where epc = \"" + epc
                        + "\"", null); // 搜尋指定的標籤
        if (c == null) // 不存在，該EPC尚未建立，是唯一的EPC，傳回true
            return true;

        if (c.getCount() == 0) { // 不存在，該EPC尚未建立，是唯一的EPC，傳回true
            return true;
        } else {
            return false; // 該EPC已存在，傳回false
        }
    }

    public void showToastMessage(String msg) {
        Toast t = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        t.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (mSoundTool != null)
//                mSoundTool.release();

            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String getStringFromResource(int id) {
        return getResources().getString(id);
    }

    // download data from server
    private ProgressDialog mProgressDialog;
    private final static int MAX_PROGRESS = 100;

    private class DownloadCoffeeTask extends AsyncTask<Void, Integer, JSONObject> {
        private final static String URL_TO_PHP = "http://192.168.1.110/PhpProjectJsonSample/downloadCoffee.php";
        private final static String TAG_SUCCESS = "success";
        private List<NameValuePair> params = new ArrayList<NameValuePair>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoginDialog();
            mProgressDialog.setProgress(0);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            publishProgress(30);
            JSONParser jParser = new JSONParser();
            JSONObject json = jParser.makeHttpRequest(URL_TO_PHP, "GET", this.params);
            publishProgress(100);

            return json;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mProgressDialog.dismiss();
            try {
                JSONArray dataArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject jobj = dataArray.getJSONObject(i);
                    String id = jobj.getString("id");
                    String epc = jobj.getString("epc");
                    String holder = jobj.getString("holder");
                    String product = jobj.getString("product");
                    String udate = jobj.getString("udate");
                    String[] rows = getTemplateStringArray();
                    rows[0] = epc;
                    rows[1] = id;
                    rows[2] = holder;
                    rows[4] = product;
                    rows[14] = udate;
                    importToDatabase(rows);
                }
                showView();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressDialog.setProgress(0);
        }

        private void showLoginDialog() {
            mProgressDialog = new ProgressDialog(DataBuilderActivity.this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIconAttribute(android.R.attr.alertDialogIcon);
            mProgressDialog.setTitle(R.string.upload);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(MAX_PROGRESS);
            mProgressDialog.show();
        }

        private String[] getTemplateStringArray() {
            String[] rows = new String[18];
            for(int i = 0; i < rows.length; i++) {
                rows[i] = "undefine";
            }

            return rows;
        }

        private void showView() {
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mList.clear(); // 先清空
                    Cursor c = db
                            .rawQuery(
                                    "select epc, propertyname from WarehouseInventoryTable ORDER BY _ID DESC",
                                    null);
                    if (c == null) {
                        System.out.println("null data");
                    } else if (c.getCount() == 0) {
                        System.out.println("zero data");
                    } else {
                        c.moveToFirst(); // 游標移到第一行
                        do {
                            Map<String, Object> item = new HashMap<String, Object>();
                            item.put("itemView", R.drawable.tag_64);
                            item.put("epctitle", "EPC: ");
                            item.put("epc", c.getString(0)); // EPC
                            item.put("propertytitle", getStringFromResource(R.string.property_name) + ": ");
                            item.put("property", c.getString(1)); // propertyname
                            mList.add(item);
                        } while (c.moveToNext());
                    }
                    mAdapter.notifyDataSetChanged();
                    mUpdateJsonData.setEnabled(true);
                }
            });
        }
    }
}
