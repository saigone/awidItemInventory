package com.emmt.awiditeminventory;

import java.io.File;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class UpdateItemTabFragment3 extends Fragment {
	Button mBtnTakePicture1, mBtnTakePicture2, mBtnTakePicture3;
	ImageView mImageView1, mImageView2, mImageView3;
	public static final int PHOTO_RESULT_CODE = 1;
	private MyTabInfoContainner tabInfo;
	private String TAG = "UpdateItemTabFragment3";
	
	public UpdateItemTabFragment3() {
		
	}
	
	public void setTabInfoContainner(MyTabInfoContainner tab) {
		tabInfo = tab;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.add_new_item_tab_sublayout3,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setupViewComponent();
		initialViewComponent();
	}

	private void setupViewComponent() {
		mBtnTakePicture1 = (Button) getView().findViewById(
				R.id.btn_picture_tab_1);
		mBtnTakePicture1.setOnClickListener(btnClick);
		mBtnTakePicture2 = (Button) getView().findViewById(
				R.id.btn_picture_tab_2);
		mBtnTakePicture2.setOnClickListener(btnClick);
		mBtnTakePicture3 = (Button) getView().findViewById(
				R.id.btn_picture_tab_3);
		mBtnTakePicture3.setOnClickListener(btnClick);

		mImageView1 = (ImageView) getView().findViewById(R.id.imgView_tab_1);
		mImageView1.setOnClickListener(btnClick);
		mImageView2 = (ImageView) getView().findViewById(R.id.imgView_tab_2);
		mImageView2.setOnClickListener(btnClick);
		mImageView3 = (ImageView) getView().findViewById(R.id.imgView_tab_3);
		mImageView3.setOnClickListener(btnClick);
	}

	private void showToastMessage(String msg) {
		Toast t = Toast.makeText(this.getActivity(), msg, Toast.LENGTH_LONG);
		t.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
		t.show();
	}
	
	private void initialViewComponent() {
		mImageView1.setScaleType(ImageView.ScaleType.FIT_XY);
		Bitmap b = getPhoto(tabInfo.getTxtTID() + "_1.jpg");
		if (b != null)
			mImageView1.setImageBitmap(b);
		else
			mImageView1.setImageDrawable(getView().getResources()
					.getDrawable(R.drawable.picture));
		
		mImageView2.setScaleType(ImageView.ScaleType.FIT_XY);
		b = getPhoto(tabInfo.getTxtTID() + "_2.jpg");
		if (b != null)
			mImageView2.setImageBitmap(b);
		else
			mImageView2.setImageDrawable(getView().getResources()
					.getDrawable(R.drawable.picture));
		
		mImageView3.setScaleType(ImageView.ScaleType.FIT_XY);
		b = getPhoto(tabInfo.getTxtTID() + "_3.jpg");
		if (b != null)
			mImageView3.setImageBitmap(b);
		else
			mImageView3.setImageDrawable(getView().getResources()
					.getDrawable(R.drawable.picture));
	}
	
	private Bitmap getPhoto(String photoName) {
		String fileSeparator = System.getProperty("file.separator");
		File file = new File(Environment.getExternalStorageDirectory()
				.getPath() + fileSeparator + Environment.DIRECTORY_PICTURES,
				photoName);
		if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
			return bitmap;
		} else {
			return null;
		}
	}
	
	private OnClickListener btnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.btn_picture_tab_1:
			case R.id.btn_picture_tab_2:
			case R.id.btn_picture_tab_3:

				String tid = tabInfo.getTxtTID();
				if (tid == null || "".equals(tid)) {
					showToastMessage("拍照前請先填入EPC與TID資料");
					return;
				}
				Intent intent = new Intent(getActivity(),
						CameraViewActivity.class);
				Bundle bundle = new Bundle();
				String tag = (String) getView().findViewById(id).getTag();
				String photoName = tid + "_" + tag + ".jpg";
				Log.v(TAG, "PhotoName: " + photoName);
				bundle.putString("photoName", photoName);
				intent.putExtras(bundle);
				startActivityForResult(intent, PHOTO_RESULT_CODE);
				break;
			case R.id.imgView_tab_1:
			case R.id.imgView_tab_2:
			case R.id.imgView_tab_3:
				String t = (String) getView().findViewById(id).getTag();
				String p = ""; // ���ɦW
				if ("1".equals(t)) {
					Bitmap b = getPhoto(tabInfo.getTxtTID() + "_1.jpg");
					if(b != null)
						p = tabInfo.getTxtTID() + "_" + t + ".jpg";
					else
						return;
				} else if ("2".equals(t)) {
					Bitmap b = getPhoto(tabInfo.getTxtTID() + "_2.jpg");
					if(b != null)
						p = tabInfo.getTxtTID() + "_" + t + ".jpg";
					else
						return;
				} else if ("3".equals(t)) {
					Bitmap b = getPhoto(tabInfo.getTxtTID() + "_3.jpg");
					if(b != null)
						p = tabInfo.getTxtTID() + "_" + t + ".jpg";
					else
						return;
				} else {
					showToastMessage("該tag未定義");
					return;
				}

				Intent in = new Intent(
						UpdateItemTabFragment3.this.getActivity(),
						ImageViewScaleActivity.class);
				Bundle b = new Bundle();
				b.putString("photoName", p);
				in.putExtras(b);
				startActivity(in);

				break;
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		// requestCode對應到startActivityForResult(arg1, arg2)第2個參數
				// resultCode是指對方回傳setResult(RESULT_OK,i)第一個值
				// data指對方回傳setResult(RESULT_OK,i)第二個值

		if (data == null) {
			System.out.println("data is null");
		}
		if (requestCode == PHOTO_RESULT_CODE) {
			Bundle bundle = null;
			if (data != null && (bundle = data.getExtras()) != null) {
				// 檔案名稱
				String photoName = bundle.getString("photoName");
				Log.v(TAG, "photoName: " + photoName);
				String fileSeparator = System.getProperty("file.separator");
				File file = new File(Environment.getExternalStorageDirectory()
						.getPath()
						+ fileSeparator
						+ Environment.DIRECTORY_PICTURES, photoName);
				// 若檔案存在
				if (file.exists()) {
					// decodeFile將文件轉化為Bitmap物件
					Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
					// 圖片的顯示方式，xy適當位置
					int num = Integer.parseInt(""
							+ photoName.charAt(photoName.indexOf(".") - 1));
					switch (num) { // 以photoName尾端數字來判斷哪個按鍵觸發
					case 1:
						mImageView1.setScaleType(ImageView.ScaleType.FIT_XY);
						mImageView1.setImageBitmap(bitmap);
						break;
					case 2:
						mImageView2.setScaleType(ImageView.ScaleType.FIT_XY);
						mImageView2.setImageBitmap(bitmap);
						break;
					case 3:
						mImageView3.setScaleType(ImageView.ScaleType.FIT_XY);
						mImageView3.setImageBitmap(bitmap);
						break;
					}
				} else {
					Toast.makeText(this.getActivity(), "�ɮפ��s�b",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}
