package com.lz.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lz on 2018/4/19.
 */

public class ScanView extends View {

    private Paint mScanMask;
    private Paint mScanRecPaint;
    private Rect mScanRec;

    public ScanView(Context context) {
        this(context, null);
    }

    public ScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScanRecPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScanRecPaint.setColor(getResources().getColor(R.color.scanview_mask));
        mScanMask = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(mScanRec, mScanRecPaint);
    }

    public void setScanRec(Rect scanRec) {
        this.mScanRec = scanRec;
    }
}
