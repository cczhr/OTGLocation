package com.cczhr.otglocation.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.google.android.material.textfield.TextInputLayout

/**
 * @author cczhr
 * @description
 * @since 2021/2/25
 */
class TextInputLayout2 :TextInputLayout   {
    private var onGestureListener: OnGestureListener?=null
    private var gestureDetector: GestureDetector?=null
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    fun swipeRight(swipeRight: () -> Unit){
        onGestureListener = OnGestureListener(context,swipeRight)
        gestureDetector = GestureDetector(context, onGestureListener)

    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        gestureDetector?.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    class OnGestureListener(context: Context, var swipeRight: () -> Unit) : GestureDetector.SimpleOnGestureListener() {
        private var maxVelocity = 0F
        private var slipFactor = 0.125F//触发的速度阈值
        private val MIN_DISTANCE = 100//最小距离

        init {
            val viewConfiguration = ViewConfiguration.get(context);
            maxVelocity = viewConfiguration.scaledMaximumFlingVelocity.toFloat()
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e2.x - e1.x > MIN_DISTANCE && velocityX >= (maxVelocity * slipFactor)) {
                swipeRight.invoke()
            }
            return true
        }
    }











}