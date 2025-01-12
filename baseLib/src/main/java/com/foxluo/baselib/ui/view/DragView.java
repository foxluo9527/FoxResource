package com.foxluo.baselib.ui.view;

import static com.blankj.utilcode.util.SizeUtils.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

/**
 * 自定义的view，能够随意拖动。
 */

public class DragView extends CardView {
    public DragView(Context context) {
        super(context);
    }

    public DragView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // ----------------------------- 拖拽 -----------------------------

    private boolean mIsDrug = true;
    private boolean mCustomIsAttach = true;//是否需要自动吸
    private boolean mCustomIsDrag = true;//是否可拖曳

    /**
     * 是否可自动吸边
     */
    public void setAutoAttach(boolean autoAttach) {
        this.mCustomIsAttach = autoAttach;
    }

    /**
     * 是否可拖动
     */
    public void setDraggable(boolean draggable) {
        this.mCustomIsDrag = draggable;
    }

    private int leftPadding = 0;
    private int topPadding = 0;
    private int rightPadding = 0;
    private int bottomPadding = 0;

    /**
     * 设置最多拖动边界
     *
     * @param left   左侧
     * @param top    顶部
     * @param right  右侧
     * @param bottom 底部
     */
    public void setDragPadding(int left, int top, int right, int bottom) {
        this.leftPadding = dp2px(left);
        this.topPadding = dp2px(top);
        this.rightPadding = dp2px(right);
        this.bottomPadding = dp2px(bottom);
    }

    private float mLastRawX;
    private float mLastRawY;
    private int mRootMeasuredWidth = 0;
    private int mRootMeasuredHeight = 0;
    private int mRootTopY = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //判断是否需要滑动
        if (mCustomIsDrag) {
            //当前手指的坐标
            float mRawX = ev.getRawX();
            float mRawY = ev.getRawY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN://手指按下
                    mIsDrug = false;
                    //记录按下的位置
                    mLastRawX = mRawX;
                    mLastRawY = mRawY;
                    ViewGroup mViewGroup = (ViewGroup) getParent();
                    if (mViewGroup != null) {
                        int[] location = new int[2];
                        mViewGroup.getLocationInWindow(location);
                        //获取父布局的高度
                        mRootMeasuredHeight = mViewGroup.getMeasuredHeight();
                        mRootMeasuredWidth = mViewGroup.getMeasuredWidth();
                        //获取父布局顶点的坐标
                        mRootTopY = location[1];
                    }
                    break;
                case MotionEvent.ACTION_MOVE://手指滑动
                    if (mRawX >= 0 && mRawX <= mRootMeasuredWidth && mRawY >= mRootTopY && mRawY <= (mRootMeasuredHeight + mRootTopY)) {
                        //手指X轴滑动距离
                        float differenceValueX = mRawX - mLastRawX;
                        //手指Y轴滑动距离
                        float differenceValueY = mRawY - mLastRawY;
                        //判断是否为拖动操作
                        if (!mIsDrug) {
                            mIsDrug = !(Math.sqrt(differenceValueX * differenceValueX + differenceValueY * differenceValueY) < 2);
                        }
                        //获取手指按下的距离与控件本身X轴的距离
                        float ownX = getX();
                        //获取手指按下的距离与控件本身Y轴的距离
                        float ownY = getY();
                        //理论中X轴拖动的距离
                        float endX = ownX + differenceValueX;
                        //理论中Y轴拖动的距离
                        float endY = ownY + differenceValueY;
                        //X轴可以拖动的最大距离
                        float maxX = mRootMeasuredWidth - getWidth();
                        //Y轴可以拖动的最大距离
                        float maxY = mRootMeasuredHeight - getHeight();
                        //X轴边界限制
                        endX = endX < leftPadding ? leftPadding : Math.min(endX, maxX - rightPadding);
                        //Y轴边界限制
                        endY = endY < topPadding ? topPadding : Math.min(endY, maxY - bottomPadding);
                        //开始移动
                        setX(endX);
                        setY(endY);
                        //记录位置
                        mLastRawX = mRawX;
                        mLastRawY = mRawY;
                    }

                    break;
                case MotionEvent.ACTION_UP://手指离开
                    //根据自定义属性判断是否需要贴边
                    if (mCustomIsAttach) {
                        //判断是否为点击事件
                        if (mIsDrug) {
                            float center = (mRootMeasuredWidth >> 1);
                            //自动贴边
                            if (mLastRawX <= center) {
                                mLastRawX = leftPadding;
                                //向左贴边
                                this.animate()
                                        .setDuration(300)
                                        .x(mLastRawX)
                                        .start();
                            } else {
                                mLastRawX = mRootMeasuredWidth - getWidth() - rightPadding;
                                //向右贴边
                                this.animate()
                                        .setDuration(300)
                                        .x(mLastRawX)
                                        .start();
                            }
                        }
                    }

                    // 如果要保存最后坐标点
                    /*if (mIsDrug) {
                        mDefaultPreference.putFloat(IPreferencesConsts.REMOVABLE_VIEW_COORDINATE_X, mLastRawX);
                        mDefaultPreference.putFloat(IPreferencesConsts.REMOVABLE_VIEW_COORDINATE_Y, mLastRawY - ev.getY());
                        mDefaultPreference.commit();
                    }*/

                    break;
            }
        }
        //是否拦截事件
        return mIsDrug ? mIsDrug : super.onTouchEvent(ev);
    }
}