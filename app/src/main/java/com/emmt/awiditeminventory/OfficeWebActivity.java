package com.emmt.awiditeminventory;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class OfficeWebActivity extends Activity {
	private WebView web;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		web = new WebView(this);
		web.getSettings().setJavaScriptEnabled(true);
		final Activity activity = this;
		web.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				super.onProgressChanged(view, newProgress);
				activity.setProgress(newProgress * 1000);
			}

		});

		web.setWebViewClient(new WebViewClient() {

			@Override
			public void onReceivedError(WebView view, int errorCode, String description,
					String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				Toast.makeText(activity, description, Toast.LENGTH_LONG).show();
			}

		});
		web.loadUrl("http://www.awidasia.com/shop/");
		// ��o WebSettings����
		WebSettings webSettings = web.getSettings();
		// �䴩JavaScript
		webSettings.setJavaScriptEnabled(true);
		// �䴩Zoom
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);

		setContentView(web, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
			web.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
