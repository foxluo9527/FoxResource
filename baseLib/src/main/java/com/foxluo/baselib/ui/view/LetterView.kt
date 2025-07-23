package com.foxluo.baselib.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import com.blankj.utilcode.util.SizeUtils

class LetterView(context: Context?, attrs: AttributeSet? = null) : View(context, attrs) {
    //当前手指滑动到的位置
    private var choosePosition = -1

    //画文字的画笔
    private val paint by lazy {
        Paint().apply {
            setAntiAlias(true)
            setTextSize(letterSize)
            setTypeface(Typeface.DEFAULT_BOLD)
        }
    }

    //右边的所有文字
    private var letters: MutableList<String>? = null

    //让信息滑到指定位置
    private var updateListView: UpdateListView? = null

    //单个字母的高度
    private val perTextHeight = SizeUtils.sp2px(10.0f).toFloat()

    //字母的字体大小
    private val letterSize = SizeUtils.dp2px(12.0f).toFloat()

    //测量view的大小，让其有多大显示多大
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec) //获取宽的模式
        val heightMode = MeasureSpec.getMode(heightMeasureSpec) //获取高的模式
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) //获取宽的尺寸
        val heightSize = MeasureSpec.getSize(heightMeasureSpec) //获取高的尺寸
        var width = 0
        val height: Int
        if (widthMode == MeasureSpec.EXACTLY) {
            //如果match_parent或者具体的值，直接赋值
            width = widthSize
        }
        //高度跟宽度处理方式一样
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            val textHeight = perTextHeight
            height =
                (getPaddingTop() + textHeight * ((letters?.size
                    ?: 0) + 1) + getPaddingBottom()).toInt()
        }
        //保存测量宽度和测量高度
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val letters = letters ?: return
        for (i in letters.indices) {
            if (i == choosePosition) {
                paint.setColor("#AD8748".toColorInt())
            } else {
                paint.setColor("#adadad".toColorInt())
            }
            val letter = letters.getOrNull(i) ?: return
            canvas.drawText(
                letter,
                (getWidth() - paint.measureText(letters.get(i))) / 2 + SizeUtils.dp2px(3.0f),
                (i + 1) * perTextHeight,
                paint
            )
        }
    }

    //根据点击的y值（y的值是根据你自己定义的view的原点为起点的）判断点的是第几项
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val y = event.getY()
        var currentPosition = (y / perTextHeight).toInt()
        if (currentPosition < 0) {
            currentPosition = 0
        }
        if (currentPosition >= (letters?.size ?: 0)) {
            return true
        }
        val letter = letters?.getOrNull(currentPosition)
        when (event.getAction()) {
            MotionEvent.ACTION_UP -> {}
            else -> {
                updateListView?.updateListView(letter)
                choosePosition = currentPosition
            }
        }
        invalidate()
        return true
    }

    //主ListView调用重回letterView
    fun updateLetterIndexView(currentChar: Int) {
        if (currentChar >= 0 && currentChar < (letters?.size ?: 0)) {
            choosePosition = currentChar
            invalidate()
        }
    }

    fun setUpdateListView(mUpdateListView: UpdateListView?) {
        this.updateListView = mUpdateListView
    }

    fun setData(letters: MutableList<String>) {
        this.letters = letters
        invalidate()
    }

    //控制主ListView数据滑动到指定的位置
    interface UpdateListView {
        fun updateListView(letter: String?)
    }
}
