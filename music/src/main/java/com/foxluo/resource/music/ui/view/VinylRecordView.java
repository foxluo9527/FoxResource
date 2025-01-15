package com.foxluo.resource.music.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

import com.foxluo.resource.music.R;


/**
 * @author: njb
 * @date: 2021/5/25 16:35
 * @desc: 描述
 */
public class VinylRecordView extends View {
    private Paint paint;
    // 圆环半径
    private int ringWidth;
    // 渐变色
    private int[] colors;
    private SweepGradient gradient;
    // 圆线距圆环内边的距离
    private int[] ringLinesMarginOut = {
            dp2px(5F),
            dp2px(11F),
            dp2px(17F),
            dp2px(23F),
            dp2px(29F),
            dp2px(35F)
    };
    // 圆线高度
    private int ringLineWidth;

    public VinylRecordView(Context context) {
        this(context, null);
    }

    public VinylRecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VinylRecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        colors = new int[]{getColor(R.color.widget_album_ring_color1), getColor(R.color.widget_album_ring_color2),
                getColor(R.color.widget_album_ring_color1), getColor(R.color.widget_album_ring_color2),
                getColor(R.color.widget_album_ring_color1)};

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VinylRecordView);
        ringWidth = (int) typedArray.getDimension(R.styleable.VinylRecordView_ring_width, getResources().getDimension(R.dimen.widget_album_ring_width));
        ringLineWidth = (int) typedArray.getDimension(R.styleable.VinylRecordView_ring_line_width, getResources().getDimension(R.dimen.widget_album_ring_line_width));
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStrokeWidth(ringWidth);
        paint.setColor(getColor(R.color.widget_album_ring_color1));
        if (gradient == null) {
            gradient = new SweepGradient(getWidth() * 0.5F, getHeight() * 0.5F, colors, new float[]{
                    0F, 0.25F, 0.5F, 0.75F, 1F
            });
        }
        paint.setShader(gradient);
        canvas.drawCircle(getWidth() * 0.5F, getHeight() * 0.5F, (getWidth() - ringWidth) * 0.5F, paint);
        paint.setShader(null);
        paint.setStrokeWidth(ringLineWidth);
        paint.setColor(getColor(R.color.widget_album_ring_line_color));
        for (int marginOut : ringLinesMarginOut) {
            canvas.drawCircle(getWidth() * 0.5F, getHeight() * 0.5F, getWidth() * 0.5F - marginOut - ringLineWidth * 0.5F, paint);
        }
    }

    private int dp2px(float dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5F);
    }

    @ColorInt
    private int getColor(@ColorRes int colorId) {
        return getResources().getColor(colorId, null);
    }
}
