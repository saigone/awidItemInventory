package com.emmt.awiditeminventory;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageViewScaleActivity extends Activity {
	private ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持螢幕亮著
		setContentView(R.layout.image_view);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		String photoName = bundle.getString("photoName");
		setComponent(photoName);
	}

	private void setComponent(String photoName) {
		mImageView = (ImageView) findViewById(R.id.image_scale);
		String fileSeparator = System.getProperty("file.separator");
		File file = new File(Environment.getExternalStorageDirectory()
				.getPath() + fileSeparator + Environment.DIRECTORY_PICTURES,
				photoName);
		// 若檔案存在
		if (file.exists()) {
			// decodeFile將文件轉化為Bitmap物件
			Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
			// 圖片的顯示方式，xy適當位置
			// mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
			// mImageView.setRotation(-90);
			mImageView.setImageBitmap(bitmap);
		} else {
			Toast.makeText(this, getStringFromResource(R.string.no_file), Toast.LENGTH_SHORT);
		}
		mImageView.setOnTouchListener(new TounchListener());
	}

	private class TounchListener implements OnTouchListener {

		private PointF startPoint = new PointF();
		private Matrix matrix = new Matrix();
		private Matrix currentMaritx = new Matrix();

		private int mode = 0;// 用於標記模式
		private static final int DRAG = 1;// 拖動
		private static final int ZOOM = 2;// 放大
		private float startDis = 0;
		private PointF midPoint;// 中心點

		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				mode = DRAG;
				currentMaritx.set(mImageView.getImageMatrix());// 記錄ImageView當期的移動位置
				startPoint.set(event.getX(), event.getY());// 開始點
				break;

			case MotionEvent.ACTION_MOVE:// 移動事件
				if (mode == DRAG) {// 圖片拖動事件
					float dx = event.getX() - startPoint.x;// x軸移動距離
					float dy = event.getY() - startPoint.y;
					matrix.set(currentMaritx);// 在當前的位置基礎上移動
					matrix.postTranslate(dx, dy);

				} else if (mode == ZOOM) {// 圖片放大事件
					float endDis = distance(event);// 結束距離
					if (endDis > 10f) {
						float scale = endDis / startDis;// 放大倍數
						matrix.set(currentMaritx);
						matrix.postScale(scale, scale, midPoint.x, midPoint.y);
					}

				}

				break;

			case MotionEvent.ACTION_UP:
				mode = 0;
				break;
			// 當屏幕上有多個點被按住，鬆開其中一個點時觸發(即非最後一個點被放開時)
			case MotionEvent.ACTION_POINTER_UP:
				mode = 0;
				break;
			// 當屏幕上已經有觸點（手指）,再有一個手指壓下屏幕
			case MotionEvent.ACTION_POINTER_DOWN:
				mode = ZOOM;
				startDis = distance(event);

				if (startDis > 10f) {// 避免手指上有兩個繭
					midPoint = mid(event);
					currentMaritx.set(mImageView.getImageMatrix());// 記錄當前的縮放倍數
				}

				break;

			}
			mImageView.setImageMatrix(matrix);
			return true;
		}

	}

	/**
	 * 兩點之間的距離
	 * 
	 * @param event
	 * @return
	 */
	private static float distance(MotionEvent event) {
		// 兩根線的距離
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		return FloatMath.sqrt(dx * dx + dy * dy);
	}

	/**
	 * 計算兩點之間中心點的距離
	 * 
	 * @param event
	 * @return
	 */
	private static PointF mid(MotionEvent event) {
		float midx = event.getX(1) + event.getX(0);
		float midy = event.getY(1) + event.getY(0);

		return new PointF(midx / 2, midy / 2);
	}

	private String getStringFromResource(int id) {
		return getResources().getString(id);
	}
}
