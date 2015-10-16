package com.stone.verticalslide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 这是一个viewGroup容器，实现上下两个frameLayout拖动切换
 * 
 * @author sistone.Zhang
 */
@SuppressLint("NewApi")
public class DragLayout extends ViewGroup {

	/* 拖拽工具类 */
	private final ViewDragHelper mDragHelper;
	private GestureDetectorCompat gestureDetector;

	/* 上下两个frameLayout，在Activity中注入fragment */
	private View frameView1, frameView2;
	private int viewHeight;
	private static final int VEL_THRESHOLD = 100; // 滑动速度的阈值，超过这个绝对值认为是上下
	private static final int DISTANCE_THRESHOLD = 100; // 单位是像素，当上下滑动速度不够时，通过这个阈值来判定是应该粘到顶部还是底部
	private int downTop1; // 手指按下的时候，frameView1的getTop值
	private ShowNextPageNotifier nextPageListener; // 手指松开是否加载下一页的notifier

	public DragLayout(Context context) {
		this(context, null);
	}

	public DragLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDragHelper = ViewDragHelper
				.create(this, 10f, new DragHelperCallback());
		mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM);
		gestureDetector = new GestureDetectorCompat(context,
				new YScrollDetector());
	}

	@Override
	protected void onFinishInflate() {
		// 跟findviewbyId一样，初始化上下两个view
		frameView1 = getChildAt(0);
		frameView2 = getChildAt(1);
	}

	class YScrollDetector extends SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx,
				float dy) {
			// 垂直滑动时dy>dx，才被认定是上下拖动
			return Math.abs(dy) > Math.abs(dx);
		}
	}

	@Override
	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	/**
	 * 这是拖拽效果的主要逻辑
	 */
	private class DragHelperCallback extends ViewDragHelper.Callback {

		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			int childIndex = 1;
			if (changedView == frameView2) {
				childIndex = 2;
			}

			// 一个view位置改变，另一个view的位置要跟进
			onViewPosChanged(childIndex, top);
		}

		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			// 两个子View都需要跟踪，返回true
			return true;
		}

		@Override
		public int getViewVerticalDragRange(View child) {
			// 这个用来控制拖拽过程中松手后，自动滑行的速度，暂时给一个随意的数值
			return 1;
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			// 滑动松开后，需要向上或者乡下粘到特定的位置
			animTopOrBottom(releasedChild, yvel);
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			int finalTop = top;
			if (child == frameView1) {
				// 拖动的时第一个view
				if (top > 0) {
					// 不让第一个view往下拖，因为顶部会白板
					finalTop = 0;
				}
			} else if (child == frameView2) {
				// 拖动的时第二个view
				if (top < 0) {
					// 不让第二个view网上拖，因为底部会白板
					finalTop = 0;
				}
			}

			// finalTop代表的是理论上应该拖动到的位置。此处计算拖动的距离除以一个参数(3)，是让滑动的速度变慢。数值越大，滑动的越慢
			return child.getTop() + (finalTop - child.getTop()) / 3;
		}
	}

	/**
	 * 滑动时view位置改变协调处理
	 * 
	 * @param viewIndex
	 *            滑动view的index(1或2)
	 * @param posTop
	 *            滑动View的top位置
	 */
	private void onViewPosChanged(int viewIndex, int posTop) {
		if (viewIndex == 1) {
			int offsetTopBottom = viewHeight + frameView1.getTop()
					- frameView2.getTop();
			frameView2.offsetTopAndBottom(offsetTopBottom);
		} else if (viewIndex == 2) {
			int offsetTopBottom = frameView2.getTop() - viewHeight
					- frameView1.getTop();
			frameView1.offsetTopAndBottom(offsetTopBottom);
		}

		// 有的时候会默认白板，这个很恶心。后面有时间再优化
		invalidate();
	}

	private void animTopOrBottom(View releasedChild, float yvel) {
		int finalTop = 0; // 默认是粘到最顶端
		if (releasedChild == frameView1) {
			// 拖动第一个view松手
			if (yvel < -VEL_THRESHOLD
					|| (downTop1 == 0 && frameView1.getTop() < -DISTANCE_THRESHOLD)) {
				// 向上的速度足够大，就滑动到顶端
				// 向上滑动的距离超过某个阈值，就滑动到顶端
				finalTop = -viewHeight;

				// 下一页可以初始化了
				if (null != nextPageListener) {
					nextPageListener.onDragNext();
				}
			}
		} else {
			// 拖动第二个view松手
			if (yvel > VEL_THRESHOLD
					|| (downTop1 == -viewHeight && releasedChild.getTop() > DISTANCE_THRESHOLD)) {
				// 保持原地不动
				finalTop = viewHeight;
			}
		}

		if (mDragHelper.smoothSlideViewTo(releasedChild, 0, finalTop)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	/* touch事件的拦截与处理都交给mDraghelper来处理 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		if (frameView1.getBottom() > 0 && frameView1.getTop() < 0) {
			// view粘到顶部或底部，正在动画中的时候，不处理touch事件
			return false;
		}

		boolean yScroll = gestureDetector.onTouchEvent(ev);
		boolean shouldIntercept = mDragHelper.shouldInterceptTouchEvent(ev);
		int action = ev.getActionMasked();

		if (action == MotionEvent.ACTION_DOWN) {
			// action_down时就让mDragHelper开始工作，否则有时候导致异常 他大爷的
			mDragHelper.processTouchEvent(ev);
			downTop1 = frameView1.getTop();
		}

		return shouldIntercept && yScroll;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// 统一交给mDragHelper处理，由DragHelperCallback实现拖动效果
		mDragHelper.processTouchEvent(e); // 该行代码可能会抛异常，正式发布时请将这行代码加上try catch
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// 只在初始化的时候调用
		// 一些参数作为全局变量保存起来
		
		if (frameView1.getTop() == 0) {
			// 只在初始化的时候调用
			// 一些参数作为全局变量保存起来
			frameView1.layout(l, 0, r, b - t);
			frameView2.layout(l, 0, r, b - t);

			viewHeight = frameView1.getMeasuredHeight();
			frameView2.offsetTopAndBottom(viewHeight);
		} else {
			// 如果已被初始化，这次onLayout只需要将之前的状态存入即可
			frameView1.layout(l, frameView1.getTop(), r, frameView1.getBottom());
			frameView2.layout(l, frameView2.getTop(), r, frameView2.getBottom());
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(
				resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
				resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
	}

	/**
	 * 这是View的方法，该方法不支持android低版本（2.2、2.3）的操作系统，所以手动复制过来以免强制退出
	 */
	public static int resolveSizeAndState(int size, int measureSpec,
			int childMeasuredState) {
		int result = size;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			result = size;
			break;
		case MeasureSpec.AT_MOST:
			if (specSize < size) {
				result = specSize | MEASURED_STATE_TOO_SMALL;
			} else {
				result = size;
			}
			break;
		case MeasureSpec.EXACTLY:
			result = specSize;
			break;
		}
		return result | (childMeasuredState & MEASURED_STATE_MASK);
	}

	public void setNextPageListener(ShowNextPageNotifier nextPageListener) {
		this.nextPageListener = nextPageListener;
	}

	public interface ShowNextPageNotifier {
		public void onDragNext();
	}
}
