package com.emmt.awiditeminventory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileManager extends ListActivity {
	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = "/";
	private String curPath = "/";
	private TextView mPath;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fileselect);
		mPath = (TextView) findViewById(R.id.mPath);
		Button buttonConfirm = (Button) findViewById(R.id.buttonConfirm);
		buttonConfirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(FileManager.this,
						DataBuilderActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("file", curPath);
				intent.putExtras(bundle);
				setResult(2, intent);
				finish();
			}
		});
		Button buttonCancle = (Button) findViewById(R.id.buttonCancle);
		buttonCancle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		getFileDir(rootPath);
	}

	private void getFileDir(String filePath) {
		mPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles(); // 回傳包含在這資料夾裡所有的檔案
		if (files == null) {
			Toast.makeText(this, "資料夾是空的", Toast.LENGTH_LONG).show();
			filePath = f.getParent(); // 取得上一層的路徑
			f = new File(filePath);
			files = f.listFiles(); // 回傳上一層包含在這資料夾裡所有的檔案
		}
		// 第一次進畫面，預設是根目錄，不會執行
		if (!filePath.equals(rootPath)) {
			items.add("roots");
			paths.add(rootPath); // 根目錄
			items.add("back");
			paths.add(f.getParent()); // 上一頁
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			items.add(file.getName()); // 儲存檔案名稱
			paths.add(file.getPath()); // 儲存檔案路徑
		}
		setListAdapter(new FileAdapter(this, items, paths)); // 顯示list
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		System.out.println("position: " + position);
		// 若前一次選到的資料夾為空，則paths不會輸入任何值，導致選擇其他資料夾時，paths.get(position)會出現異常
		File file = new File(paths.get(position));
		if (file.isDirectory()) {
			curPath = paths.get(position);
			getFileDir(paths.get(position));
		} else {
			// 回傳文件路徑
			curPath = file.getPath().toString();
			mPath.setText(curPath);

			// 可以打開文件
			// openFile(file);
		}
	}

	private void openFile(File f) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		String type = getMIMEType(f);
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}

	private String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}

}
