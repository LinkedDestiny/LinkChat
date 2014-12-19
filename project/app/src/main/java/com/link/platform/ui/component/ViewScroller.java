package com.link.platform.ui.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class ViewScroller extends FrameLayout{//ViewGroup
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	/** 滚动对象Scroller **/
	private Scroller mScroller = null;
	/** 当前屏幕索引 **/
	private int currentScreenIndex = 0;
	/** 设置一个标志位，防止底层的onTouch事件重复处理UP事件 **/
	private ScrollToScreenCallback scrollToScreenCallback;
//	private LayoutChangeCallback layoutChangeCallBack;
	private int mTouchSlop = 0;
	private float mLastMotionX;
	private int mTouchState = TOUCH_STATE_REST;
	private int mMaximumFlingVelocity;
	private int mMinimumFlingVelocity;
	private boolean isLock ;
	/**
	 * Determines speed during touch scrolling
	 */
	private VelocityTracker mVelocityTracker;

	public ViewScroller(Context context) {
		super(context);
		init(context);
	}
	
	public void setLock(boolean isLock){
		this.isLock = isLock;
	}

	public ViewScroller(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ViewScroller(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public int getCurrentScreenIndex() {
		return currentScreenIndex;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// AUTO_TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
//		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//		if (widthMode != MeasureSpec.EXACTLY) {
//			throw new IllegalStateException(
//					"ScrollLayout only canmCurScreen run at EXACTLY mode!");
//		}
//
//		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//		if (heightMode != MeasureSpec.EXACTLY) {
//			throw new IllegalStateException(
//					"ScrollLayout only can run at EXACTLY mode!");
//		}
//
//		// The children are given the same width and height as the scrollLayout
//		final int count = getChildCount();
//		for (int i = 0; i < count; i++) {
//			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
//		}
		scrollTo(currentScreenIndex * width, 0);
	}

	private void enableDrawFromCache() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			ViewGroup viewgroup = (ViewGroup) getChildAt(i);
			viewgroup.setAlwaysDrawnWithCacheEnabled(true);
			viewgroup.setDrawingCacheEnabled(true);
		}
	}

	private void disableDrawFromCache() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			ViewGroup viewgroup = (ViewGroup) getChildAt(i);
			viewgroup.setAlwaysDrawnWithCacheEnabled(false);
			viewgroup.setDrawingCacheEnabled(false);
		}

	}

	private void init(Context context) {
		mScroller = new Scroller(context);
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		// 获得可以认为是滚动的距离
		mTouchSlop = configuration.getScaledTouchSlop();

		mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
		mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// AUTO_TODO Auto-generated method stub
		int childLeft = 0;
		final int childCount = getChildCount();

		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft + childWidth,
						childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
//		if(layoutChangeCallBack != null && changed){
//			layoutChangeCallBack.callback();
//		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// AUTO_TODO Auto-generated method stub
		if(isLock){
			return true;
		}
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		float x = event.getX();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			float distanceX = mLastMotionX - x;
			int count = getChildCount();
			if (count > 1
					&& (distanceX > 0 && getScrollX() < (count - 1)
							* getWidth())// 防止移动过最后一页
					|| (distanceX < 0 && getScrollX() > 0)) {// 防止向第一页之前移动
				float t = distanceX * 1.25f;
				scrollBy((int) t, 0);
				mLastMotionX = x;
			}

			break;
		case MotionEvent.ACTION_UP:

			mVelocityTracker
					.computeCurrentVelocity(1000, mMaximumFlingVelocity);
			final float velocityX = mVelocityTracker.getXVelocity();

			if (velocityX > mMinimumFlingVelocity && currentScreenIndex > 0) {
				scrollToScreen(currentScreenIndex - 1);
			} else if (velocityX < -mMinimumFlingVelocity
					&& currentScreenIndex < getChildCount() - 1) {
				scrollToScreen(currentScreenIndex + 1);
			} else {
				snapToDestination();
			}
			mVelocityTracker.recycle();
			mVelocityTracker = null;
			break;
		case MotionEvent.ACTION_CANCEL:
			mVelocityTracker.recycle();
			mVelocityTracker = null;
			break;
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// AUTO_TODO Auto-generated method stub
		if(isLock){
			return false;
		}
		final float x = ev.getX();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			boolean xMoved = xDiff > (mTouchSlop*4);
			View view = getChildAt(currentScreenIndex);
			boolean isLock = false;
			if(view instanceof OnLockListener){
				isLock = ((OnLockListener)view).isLock();
			}
			// 判断是否是移动
			if (xMoved && !isLock) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			disableDrawFromCache();
			break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	/** 根据当前x坐标位置确定切换到第几屏 **/
	private void snapToDestination() {
		scrollToScreen((getScrollX() + (getWidth() / 2)) / getWidth());
	}

	@Override
	public void computeScroll() {
		// 当滚动没有完成
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), 0);
			postInvalidate();
		}
	}

	public void scrollToScreen(int whichScreen) {
		if (whichScreen != currentScreenIndex && getFocusedChild() != null
				&& getFocusedChild() == getChildAt(currentScreenIndex)) {
			getFocusedChild().clearFocus(); // 清除焦点
		}
		enableDrawFromCache();

		final int delta = whichScreen * getWidth() - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta)/2 );
		invalidate();

		currentScreenIndex = whichScreen; // 设置当前屏幕索引
		if (scrollToScreenCallback != null) { // 刷新圆圈
			scrollToScreenCallback.callback(currentScreenIndex);
		}
	}

	public void setScrollToScreenCallback(
			ScrollToScreenCallback scrollToScreenCallback) {
		this.scrollToScreenCallback = scrollToScreenCallback;
	}
	
//	public void setLayoutChangeCallback(LayoutChangeCallback callback){
//		this.layoutChangeCallBack = callback;
//	}

	/** 底部圆圈显示回调接口 **/
	public interface ScrollToScreenCallback {
		public void callback(int currentIndex);
	}
	
	/**
	 * 重绘监听回调
	 * @author wb-jiangxiang
	 *
	 */
	public interface LayoutChangeCallback {
		public void callback();
	}

	public void setCurrentScreenIndex(int currentScreenIndex) {
		this.currentScreenIndex = currentScreenIndex;
	}

    public interface OnLockListener {
        public boolean isLock();
    }
}
