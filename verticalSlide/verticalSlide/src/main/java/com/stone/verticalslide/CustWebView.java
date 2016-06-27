package com.stone.verticalslide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.ZoomButtonsController;

import java.lang.reflect.Method;

public class CustWebView extends WebView {
    private static final int MODE_IDLE = 0;
    private static final int MODE_HORIZONTAL = 1;
    private static final int MODE_VERTICAL = 2;

    private int scrollMode;
    private float downX, downY;

    boolean isAtTop = true; // 如果是true，则允许拖动至底部的下一页
    private int mTouchSlop = 4; // 判定为滑动的阈值，单位是像素

    public CustWebView(Context arg0) {
        this(arg0, null);
    }

    public CustWebView(Context arg0, AttributeSet arg1) {
        this(arg0, arg1, 0);
    }

    public CustWebView(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        disableZoomController();

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    // 使得控制按钮不可用
    @SuppressLint("NewApi")
    private void disableZoomController() {
        // API version 大于11的时候，SDK提供了屏蔽缩放按钮的方法
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            this.getSettings().setBuiltInZoomControls(true);
            this.getSettings().setDisplayZoomControls(false);
        } else {
            // 如果是11- 的版本使用JAVA中的映射的办法
            getControlls();
        }
    }

    private void getControlls() {
        try {
            Class<?> webview = Class.forName("android.webkit.WebView");
            Method method = webview.getMethod("getZoomButtonsController");
            ZoomButtonsController zoomController = (ZoomButtonsController) method.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = ev.getRawX();
            downY = ev.getRawY();
            isAtTop = isAtTop();
            scrollMode = MODE_IDLE;
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (scrollMode == MODE_IDLE) {
                float xDistance = Math.abs(downX - ev.getRawX());
                float yDistance = Math.abs(downY - ev.getRawY());
                if (xDistance > yDistance && xDistance > mTouchSlop) {
                    scrollMode = MODE_HORIZONTAL;
                } else if (downY > xDistance && yDistance > mTouchSlop) {
                    if (downY < ev.getRawY() && isAtTop) {
                        scrollMode = MODE_VERTICAL;
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                }
            }
        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 判断listView是否在顶部
     *
     * @return 是否在顶部
     */
    public boolean isAtTop() {
        return getScrollY() == 0;
    }
}
