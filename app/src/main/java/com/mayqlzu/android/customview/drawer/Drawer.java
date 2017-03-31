package com.mayqlzu.android.customview.drawer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mayq on 2017/3/30.
 * <p>
 * this is a drawer ViewGroup, you can add two children inside,
 * the first works as handle, the other is content.
 * the drawer can be pulled up from bottom.
 * <p>
 * todo: what if multi-touch?
 */
public class Drawer extends ViewGroup {
    private View handle;
    private View content;

    private int gap = 0; // how many content can be seen
    private int gapDown = 0;
    private float rawYDown = 0;// relative to screen

    public Drawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        handle = getChildAt(0);
        content = getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // handle
        int spec;
        if (handle.getLayoutParams().height > 0) {
            spec = MeasureSpec.makeMeasureSpec(
                    handle.getLayoutParams().height, MeasureSpec.EXACTLY);
        } else {
            spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        handle.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
                , spec);

        // contentView
        if (content.getLayoutParams().height > 0) {
            spec = MeasureSpec.makeMeasureSpec(
                    content.getLayoutParams().height, MeasureSpec.EXACTLY);
        } else {
            spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        content.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), spec);

        setMeasuredDimension(widthSize, handle.getMeasuredHeight() + gap);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // layout as vertical LinearLayout
        handle.layout(0, 0, handle.getMeasuredWidth(), handle.getMeasuredHeight());
        content.layout(0, handle.getMeasuredHeight(),
                handle.getMeasuredWidth(),
                handle.getMeasuredHeight() + content.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ev.getY() < handle.getHeight()) {
                    // press on handle
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rawYDown = event.getRawY();
                gapDown = gap;
                return true;
            case MotionEvent.ACTION_MOVE:
                float y = event.getRawY();
                float diff = rawYDown - y;
                gap = (int) (gapDown + diff);
                gap = Math.max(gap, 0);
                gap = Math.min(gap, content.getHeight());
                requestLayout();
                return true;
            case MotionEvent.ACTION_UP:
                onFingerReleased();
                break;
            default:
                break;
        }

        return false;
    }

    private void onFingerReleased() {
        int endVal;
        if (gap > content.getHeight() / 3) {
            endVal = content.getHeight();
        } else {
            endVal = 0;
        }

        long duration = 1000 * (long) gap / content.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(gap, endVal);
        animator.setDuration(duration);
        animator.start();

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                gap = (int) animation.getAnimatedValue();
                requestLayout();
            }
        });
    }

}
