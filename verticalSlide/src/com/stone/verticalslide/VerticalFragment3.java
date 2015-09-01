package com.stone.verticalslide;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class VerticalFragment3 extends Fragment {

	private View progressBar;
	private CustWebView webview;
	private boolean hasInited = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.vertical_fragment3, null);
		webview = (CustWebView) rootView.findViewById(R.id.fragment3_webview);
		progressBar = rootView.findViewById(R.id.progressbar);
		return rootView;
	}

	public void initView() {
		if (null != webview && !hasInited) {
			hasInited = true;
			progressBar.setVisibility(View.GONE);
			webview.loadUrl("http://m.zol.com/tuan/");
		}
	}
}
