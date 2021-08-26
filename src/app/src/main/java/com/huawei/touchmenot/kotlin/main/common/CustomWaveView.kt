package com.huawei.touchmenot.kotlin.main.common

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class CustomWaveView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var mPaint: Paint? = null
    private var mPath: Path? = null
    private var mWidth = 0
    private var mHeight = 0
    private val BYTES_POINT_4X3 = 4 * 3
    private val BYTES_PER_POINT = 4 * 1

    //The current moving distance
    var tranlateWidth = 0f
    var mValueAnimator: ValueAnimator? = null
    private fun init() {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.style = Paint.Style.FILL
        mPaint!!.color = Color.BLUE
        mPaint!!.strokeWidth = Constants.INIT_TWO.toFloat()
        mPath = Path()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mValueAnimator = ValueAnimator.ofInt(Constants.INIT_ZERO, Constants.INIT_ONE)
        mValueAnimator?.addUpdateListener(AnimatorUpdateListener { valueAnimator ->
            tranlateWidth = valueAnimator.animatedFraction * mWidth
            invalidate()
            println(Constants.STR_UPDATE)
        })
        mValueAnimator?.setDuration((Constants.INIT_ONE shl Constants.INIT_TEN.toLong().toInt()).toLong())
        mValueAnimator?.setInterpolator(LinearInterpolator())
        mValueAnimator?.setRepeatMode(ValueAnimator.RESTART)
        mValueAnimator?.setRepeatCount(ValueAnimator.INFINITE)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mValueAnimator!!.cancel()
        mValueAnimator = null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = width
        mHeight = height
        val waveHeight = (mWidth / Constants.INIT_FIVE).toFloat()
        mPath!!.moveTo(-mWidth.toFloat(), Constants.INIT_ZERO.toFloat())
        mPath!!.rCubicTo(mWidth / BYTES_PER_POINT.toFloat(), -waveHeight, mWidth / BYTES_POINT_4X3.toFloat(), waveHeight, mWidth.toFloat(), Constants.INIT_ZERO.toFloat())
        mPath!!.rCubicTo(mWidth / BYTES_PER_POINT.toFloat(), -waveHeight, mWidth / BYTES_POINT_4X3.toFloat(), waveHeight, mWidth.toFloat(), Constants.INIT_ZERO.toFloat())
        mPath!!.rLineTo(Constants.INIT_ZERO.toFloat(), (mHeight shr Constants.INIT_ONE.toFloat().toInt()).toFloat())
        mPath!!.rLineTo((-mWidth shl Constants.INIT_ONE.toFloat().toInt()).toFloat(), Constants.INIT_ZERO.toFloat())
        mPath!!.close()
    }

    fun start() {
        mValueAnimator!!.start()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(tranlateWidth, (height shr Constants.INIT_ONE.toFloat().toInt()).toFloat())
        canvas.drawPath(mPath!!, mPaint!!)
    }

    init {
        init()
    }
}