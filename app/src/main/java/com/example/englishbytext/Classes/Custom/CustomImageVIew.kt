package com.example.englishbytext.Classes.Custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener


class CustomImageVIew(context: Context, attrs: AttributeSet?, defStyle: Int) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyle), OnTouchListener {
    private val theMatrix: Matrix = Matrix()
    private val savedMatrix: Matrix = Matrix()
    private var mode = NONE
    private val mStartPoint = PointF()
    private val mMiddlePoint = PointF()
    private val mBitmapMiddlePoint = PointF()
    private var oldDist = 1f
    private val matrixValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var scale = 0f
    private var oldEventX = 0f
    private var oldEventY = 0f
    private var oldStartPointX = 0f
    private var oldStartPointY = 0f
    private var mViewWidth = -1
    private var mViewHeight = -1
    private var mBitmapWidth = -1
    private var mBitmapHeight = -1
    private var mDraggable = false

    constructor(context: Context) : this(context, null, 0) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
    }

    fun setBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            setImageBitmap(bitmap)
            mBitmapWidth = bitmap.width
            mBitmapHeight = bitmap.height
            mBitmapMiddlePoint.x = mViewWidth / 2f - mBitmapWidth / 2f
            mBitmapMiddlePoint.y = mViewHeight / 2f - mBitmapHeight / 2f
            theMatrix.postTranslate(mBitmapMiddlePoint.x, mBitmapMiddlePoint.y)
            this.imageMatrix = theMatrix
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(theMatrix)
                mStartPoint[event.x] = event.y
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(theMatrix)
                    midPoint(mMiddlePoint, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                drag(event)
            } else if (mode == ZOOM) {
                zoom(event)
            }
        }
        return true
    }

    fun drag(event: MotionEvent) {
        theMatrix.getValues(matrixValues)
        val left = matrixValues[2]
        val top = matrixValues[5]
        val bottom = top + matrixValues[0] * mBitmapHeight - mViewHeight
        val right = left + matrixValues[0] * mBitmapWidth - mViewWidth
        var eventX = event.x
        var eventY = event.y
        val spacingX = eventX - mStartPoint.x
        val spacingY = eventY - mStartPoint.y
        val newPositionLeft = (if (left < 0) spacingX else spacingX * -1) + left
        val newPositionRight = spacingX + right
        val newPositionTop = (if (top < 0) spacingY else spacingY * -1) + top
        val newPositionBottom = spacingY + bottom
        var x = true
        var y = true
        if (newPositionRight < 0.0f || newPositionLeft > 0.0f) {
            if (newPositionRight < 0.0f && newPositionLeft > 0.0f) {
                x = false
            } else {
                eventX = oldEventX
                mStartPoint.x = oldStartPointX
            }
        }
        if (newPositionBottom < 0.0f || newPositionTop > 0.0f) {
            if (newPositionBottom < 0.0f && newPositionTop > 0.0f) {
                y = false
            } else {
                eventY = oldEventY
                mStartPoint.y = oldStartPointY
            }
        }
        if (mDraggable) {
            theMatrix.set(savedMatrix)
            theMatrix.postTranslate((if (x) eventX - mStartPoint.x else 0).toFloat(), (if (y) eventY - mStartPoint.y else 0).toFloat())
            this.imageMatrix = theMatrix
            if (x) oldEventX = eventX
            if (y) oldEventY = eventY
            if (x) oldStartPointX = mStartPoint.x
            if (y) oldStartPointY = mStartPoint.y
        }
    }

    fun zoom(event: MotionEvent) {
        theMatrix.getValues(matrixValues)
        val newDist = spacing(event)
        val bitmapWidth = matrixValues[0] * mBitmapWidth
        val bimtapHeight = matrixValues[0] * mBitmapHeight
        val `in` = newDist > oldDist
        if (!`in` && matrixValues[0] < 1) {
            return
        }
        mDraggable = bitmapWidth > mViewWidth || bimtapHeight > mViewHeight
        val midX = (mViewWidth / 2).toFloat()
        val midY = (mViewHeight / 2).toFloat()
        theMatrix.set(savedMatrix)
        scale = newDist / oldDist
        theMatrix.postScale(scale, scale, if (bitmapWidth > mViewWidth) mMiddlePoint.x else midX, if (bimtapHeight > mViewHeight) mMiddlePoint.y else midY)
        this.imageMatrix = theMatrix
    }

    /** Determine the space between the first two fingers  */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /** Calculate the mid point of the first two fingers  */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    companion object {
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
    }

    init {
        this.setOnTouchListener(this)
    }
}