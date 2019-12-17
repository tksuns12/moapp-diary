package com.moapp.emotion_diary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatEditText;

public class LinedEditText extends AppCompatEditText {
    private Rect mRect;
    private Paint mPaint;

    // we need this constructor for LayoutInflater
    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor("#6e5e44"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = getHeight();
        int line_height = getLineHeight();

        int count = height / line_height;

        if (getLineCount() > count)
            count = getLineCount();

        Rect r = mRect;
        Paint paint = mPaint;
        int baseline = getLineBounds(0, r);

        for (int i = 0; i < count; i++) {

            canvas.drawLine(r.left, baseline + 2, r.right, baseline + 2, paint);
            baseline += getLineHeight();
        }
        super.onDraw(canvas);

    }

}