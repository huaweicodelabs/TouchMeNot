package com.huawei.touchmenot.java.main.common;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class CustomWaveView  extends View
{

    private Paint mPaint;
    private Path mPath;
    private int mWidth;
    private int mHeight;
    private int BYTES_POINT_4X3 = 4*3, BYTES_PER_POINT = 4*1;

    public CustomWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    //The current moving distance
    float tranlateWidth;

    ValueAnimator mValueAnimator;


    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(Constants.INIT_TWO);

        mPath = new Path();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mValueAnimator = ValueAnimator.ofInt(Constants.INIT_ZERO,Constants.INIT_ONE);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tranlateWidth = valueAnimator.getAnimatedFraction() * mWidth;
                invalidate();
                System.out.println(Constants.STR_UPDATE);
            }
        });
        mValueAnimator.setDuration(Constants.INIT_ONE << Constants.INIT_TEN);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mValueAnimator.cancel();
        mValueAnimator = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = getWidth();
        mHeight = getHeight();
        float waveHeight = (float) (mWidth / Constants.INIT_FIVE);


        mPath.moveTo(-mWidth, Constants.INIT_ZERO);

        mPath.rCubicTo(mWidth / BYTES_PER_POINT, -waveHeight, mWidth / BYTES_POINT_4X3, waveHeight, mWidth, Constants.INIT_ZERO);
        mPath.rCubicTo(mWidth /BYTES_PER_POINT, -waveHeight, mWidth / BYTES_POINT_4X3, waveHeight, mWidth, Constants.INIT_ZERO);

        mPath.rLineTo(Constants.INIT_ZERO, mHeight >> Constants.INIT_ONE);
        mPath.rLineTo(-mWidth << Constants.INIT_ONE, Constants.INIT_ZERO);
        mPath.close();
    }

    public void start() {
        mValueAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.translate(tranlateWidth, getHeight() >> Constants.INIT_ONE);

        canvas.drawPath(mPath, mPaint);
    }
}


