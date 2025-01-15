package com.foxluo.baselib.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.foxluo.baselib.R;

/**
 * @author: njb
 * @date: 2023/5/21 22:56
 * @desc:
 */
@Suppress("NAME_SHADOWING")
class RoundImageView : AppCompatImageView {
    private val paint: Paint by lazy { Paint() }
    private var roundWidth = 10
    private var roundHeight = 10
    private val paint2: Paint by lazy { Paint() }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    @SuppressLint("Recycle")
    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val attrs = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView)
            roundWidth = attrs.getDimensionPixelSize(R.styleable.RoundImageView_x_radius, roundWidth)
            roundHeight = attrs.getDimensionPixelSize(R.styleable.RoundImageView_y_radius, roundHeight)
        } else {
            val density = context.resources.displayMetrics.density
            roundWidth = (roundWidth * density).toInt()
            roundHeight = (roundHeight * density).toInt()
        }
        paint.color = Color.WHITE
        paint.isAntiAlias = true
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    override fun draw(canvas: Canvas) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas2 = Canvas(bitmap)
        super.draw(canvas2)
        drawLiftUp(canvas2)
        drawLiftDown(canvas2)
        drawRightUp(canvas2)
        drawRightDown(canvas2)
        canvas.drawBitmap(bitmap, 0f, 0f, paint2)
        bitmap.recycle()
    }

    private fun drawLiftUp(canvas: Canvas) {
        val path = Path()
        path.moveTo(0f, roundHeight.toFloat())
        path.lineTo(0f, 0f)
        path.lineTo(roundWidth.toFloat(), 0f)
        path.arcTo(
            RectF(0f, 0f, (roundWidth * 2).toFloat(), (roundHeight * 2).toFloat()),
            -90f,
            -90f
        )
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawLiftDown(canvas: Canvas) {
        val path = Path()
        path.moveTo(0f, (height - roundHeight).toFloat())
        path.lineTo(0f, height.toFloat())
        path.lineTo(roundWidth.toFloat(), height.toFloat())
        path.arcTo(
            RectF(
                0f,
                (height - roundHeight * 2).toFloat(),
                (roundWidth * 2).toFloat(),
                height.toFloat()
            ), 90f, 90f
        )
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawRightDown(canvas: Canvas) {
        val path = Path()
        path.moveTo((width - roundWidth).toFloat(), height.toFloat())
        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(width.toFloat(), (height - roundHeight).toFloat())
        path.arcTo(
            RectF(
                (width - roundWidth * 2).toFloat(),
                (height - roundHeight * 2).toFloat(),
                width.toFloat(),
                height.toFloat()
            ), -0f, 90f
        )
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawRightUp(canvas: Canvas) {
        val path = Path()
        path.moveTo(width.toFloat(), roundHeight.toFloat())
        path.lineTo(width.toFloat(), 0f)
        path.lineTo((width - roundWidth).toFloat(), 0f)
        path.arcTo(
            RectF(
                (width - roundWidth * 2).toFloat(),
                0f,
                width.toFloat(),
                (roundHeight * 2).toFloat()
            ), -90f, 90f
        )
        path.close()
        canvas.drawPath(path, paint)
    }
}
