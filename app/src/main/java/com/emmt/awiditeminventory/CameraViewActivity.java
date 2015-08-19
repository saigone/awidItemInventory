package com.emmt.awiditeminventory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.emmt.Utility.SoundTool;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class CameraViewActivity extends Activity implements
		SurfaceHolder.Callback {
	private Camera mCamera;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Button mCameraButton;
	private Button mFoucsButton;
	private SeekBar mSeekbar;
	private TextView mZoomLabel;
	private SoundTool mSoundTool; // 音效控制
	private String mPhotoName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持螢幕亮著
		setContentView(R.layout.cameraview);
		
		mPhotoName = getIntent().getExtras().getString("photoName");
		mCameraButton = (Button) this.findViewById(R.id.btn_camera);
		mFoucsButton = (Button) this.findViewById(R.id.btn_foucs);
		mSeekbar = (SeekBar) this.findViewById(R.id.zoomRate);
		mSeekbar.setOnSeekBarChangeListener(seekBarChg);
		mZoomLabel = (TextView) this.findViewById(R.id.zoomLabel);

		surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView1);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		initialSound();
	}

	private void initialSound() {
		mSoundTool = new SoundTool(CameraViewActivity.this, R.raw.camera4); // 設定音效
	}

	@Override
	protected void onStop() {
		super.onStop();
		System.out.println("camera on stop");
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;

		if (mSoundTool != null)
			mSoundTool.release();
	}

	public void btnClick(View view) {
		switch (view.getId()) {
		case R.id.btn_camera:
			// 按下直接照相，直接擷取畫面
			mCamera.takePicture(myShutterCallback, camRawDataCallback,
					myJpegCallback);
			break;
		case R.id.btn_foucs:
			// 按下對焦，執行對焦成功後的回應函式
			mCamera.autoFocus(onCamAutoFocus);
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("photoName", mPhotoName);
			intent.putExtras(bundle);
			setResult(2, intent);
			finish();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		System.out.println("surfaceChanged");

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println("surfaceCreated");
		/*
		 * To take pictures with this class, use the following steps:
		 * 
		 * 1. Obtain an instance of Camera from open(int). 2. Get existing
		 * (default) settings with getParameters(). 3. If necessary, modify the
		 * returned Camera.Parameters object and call
		 * setParameters(Camera.Parameters). 4. If desired, call
		 * setDisplayOrientation(int). 5. Important: Pass a fully initialized
		 * SurfaceHolder to setPreviewDisplay(SurfaceHolder). Without a surface,
		 * the camera will be unable to start the preview. 6. Important: Call
		 * startPreview() to start updating the preview surface. Preview must be
		 * started before you can take a picture.
		 */

		// 1. open camera
		try {
			mCamera = Camera.open(0);
		} catch (RuntimeException e) {
			Toast.makeText(getApplication(), "照相機啟始錯誤！", Toast.LENGTH_LONG)
					.show();
			return;
		}

		// 4. set PreviewDisplay(SurfaceHolder)
		try {
			mCamera.setPreviewDisplay(surfaceHolder);
		} catch (IOException e) {
			Toast.makeText(getApplication(), "照相機啟始錯誤！", Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
			return;
		}

		// 2. set param
		Camera.Parameters param = mCamera.getParameters();
		if (param.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO)
				|| param.getFocusMode().equals(
						Camera.Parameters.FOCUS_MODE_MACRO)) {
			//mCamera.autoFocus(onCamAutoFocus);
		}
		else {
			Toast.makeText(getApplication(), "照相機不支援自動對焦！", Toast.LENGTH_SHORT)
					.show();
		}
		mCamera.setParameters(param);

		// 3. set orientation
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(0, cameraInfo);

		int rotation = this.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degree = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 0;
			break;
		case Surface.ROTATION_90:
			degree = 90;
			break;
		case Surface.ROTATION_180:
			degree = 180;
			break;
		case Surface.ROTATION_270:
			degree = 270;
			break;
		}

		int result;
		if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (cameraInfo.orientation + degree) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (cameraInfo.orientation - degree + 360) % 360;
		}

		mCamera.setDisplayOrientation(result);

		// 5. Call startPreview()
		mCamera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("surfaceDestroyed");

	}

	Camera.AutoFocusCallback onCamAutoFocus = new Camera.AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if (success) {
				// 如果對焦成功就將照片儲存起來
				camera.takePicture(myShutterCallback, null, myJpegCallback);
				Toast.makeText(getApplication(), "自動對焦！", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getApplication(), "不支援自動對焦！", Toast.LENGTH_SHORT)
						.show();
			}

		}

	};

	SeekBar.OnSeekBarChangeListener seekBarChg = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// 設置最高變焦倍率為SeekBar的上限
			mSeekbar.setMax(mCamera.getParameters().getMaxZoom());
			// 設定每次的移動刻度為1
			mSeekbar.setKeyProgressIncrement(1);

			// 如果倍率是正常的倍率
			if (progress > 1) {
				mZoomLabel.setText("倍率 " + progress);

				// 支援平順變焦，就用平順變焦
				if (mCamera.getParameters().isSmoothZoomSupported()) {
					mCamera.startSmoothZoom(progress);
				} else if (mCamera.getParameters().isZoomSupported()) {
					// 如果只支援普通變集，就直接變焦
					Camera.Parameters param = mCamera.getParameters(); // 取得照相機變數
					param.setZoom(progress); // 設定變焦
					mCamera.setParameters(param); // 設定參數
				}
			}
		}
	};

	/**
	 * 當影像擷取時呼叫
	 */
	Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {

		@Override
		public void onShutter() {
			// 顯示一個Toast, 告訴使用者正在處理影像
			System.out.println("myShutterCallback");
			mSoundTool.play();
			Toast.makeText(CameraViewActivity.this, "Shutter started",
					Toast.LENGTH_LONG).show();
		}
	};

	Camera.PictureCallback camRawDataCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// 用來接收原始的影像資料
			System.out.println("camRawDataCallback");
		}
	};

	/**
	 * 當JPEG被壓縮好之後呼叫
	 */
	Camera.PictureCallback myJpegCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			System.out.println("myJpegCallback");
			FileOutputStream outputStream = null;
			try {
				// 檢查是否有SD卡
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {

					// 放入這個程式SD卡外部的空間
					// 如果使用API Level 8(Android 2.2)或以上,
					// 使用getExternalFilesDir()來打開一個文件
					// 如果你使用API Level 7或以下,
					// 使用getExternalStorageDirectory()打開一個文件表示外部存儲的根目錄
					String fileSeparator = System.getProperty("file.separator");
					File file = new File(Environment
							.getExternalStorageDirectory().getPath()
							+ fileSeparator + Environment.DIRECTORY_PICTURES,
							mPhotoName);
					outputStream = new FileOutputStream(file);
					outputStream.write(data);
					outputStream.close();
					Toast.makeText(CameraViewActivity.this,
							"相片大小:" + data.length, Toast.LENGTH_LONG).show();
				}

			} catch (FileNotFoundException e) {
				Toast.makeText(CameraViewActivity.this, "FileNotFound, 檔案無法寫入",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(CameraViewActivity.this, "IO Error, 檔案無法寫入",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} finally {
				// 繼續拍攝
//				mCamera.startPreview();
				mSoundTool.stop();
				returnValue();
			}
		}
	};
	
	private void returnValue() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("photoName", mPhotoName);
		intent.putExtras(bundle);
		setResult(2, intent);
		finish();
	}

	public String getCurrenTime() {
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss"); // 制定輸出格式
		Date date = new Date();
		String strDate = sdFormat.format(date);

		return strDate;
	}

}
