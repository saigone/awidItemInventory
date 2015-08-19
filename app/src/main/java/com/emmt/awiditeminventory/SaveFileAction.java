package com.emmt.awiditeminventory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.system.data.DataColumn;
import com.system.data.DataRow;
import com.system.data.DataTable;

public class SaveFileAction implements Runnable {
	private Context context;
	private DataTable recordLog;
	private Handler handler = new Handler();
	private String dirName;
	private boolean isFirstWrite = false; // 是否第一次寫入該檔
	private String fileName;

	public SaveFileAction(Context context, DataTable recordLog, String dirName, String fileName) {
		this.context = context;
		this.recordLog = recordLog;
		this.dirName = dirName;
		this.fileName = fileName + ".txt";
	}

	@Override
	public void run() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) { // 檢查SD卡是否已掛載於平板上
			File sdCardDir = Environment.getExternalStorageDirectory();// 獲取SDCard目錄
			File dir = new File(sdCardDir, dirName); // 建立目錄物件
			if (!dir.isDirectory()) { // 若無此資料夾
				if (!dir.mkdir()) { // 創立一個資料夾
					System.out.println("創立資料夾失敗");
					return; // 失敗便返回
				}
			}

			System.out.println("fileName: " + fileName);
			File saveFile = new File(dir.getPath(), fileName); // 建立檔案物件

			// 新寫法
			if (!saveFile.exists()) { // 檢查檔案是否存在
				try {
					System.out.println("檔案不存在，創立新檔");
					System.out.println(saveFile.createNewFile()); // 不存在則創立新檔
					isFirstWrite = true; // 設定為第一次寫入
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}

			try {
				OutputStreamWriter out = new OutputStreamWriter(
						new FileOutputStream(saveFile, true), "UTF-8");
				// PrintWriter pw = new PrintWriter(new FileWriter(saveFile,
				// true)); // 不覆蓋檔案
				// 檢查是否第一次寫檔
				if (isFirstWrite) {
					for (int i = 0; i < recordLog.Columns.size(); i++) {
						DataColumn col = recordLog.Columns.get(i);
						out.append(col.ColumnName);
						// 最後一位不附加逗號
						if (i < (recordLog.Columns.size() - 1)) {
							out.append(", ");
						}
					}
					out.append("\n");
				}

				for (DataRow row : recordLog.Rows) { // 寫入個欄位的資料
					for (int i = 0; i < row.size(); i++) {
						out.append(row.getValue(i).toString()); // 寫入index
						// 最後一位不附加逗號
						if (i < (row.size() - 1)) {
							out.append(","); // 以逗號分隔
						}
					}
					out.append("\n"); // 換行
				}
				out.flush();
				out.close();
				System.out.println("檔案寫完");
				MediaScannerConnection.scanFile(context, new String[] { Environment
						.getExternalStorageDirectory().getPath() + "/CarDatabase/" + fileName },
						null, null);

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			System.out.println("沒掛載SD卡");
		}
	}

	public String getCurrenDate() {
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy_MM_dd"); // 制定輸出格式
		Date date = new Date();
		String strDate = sdFormat.format(date);

		return strDate;
	}

}
