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

    private Paint mScanMaskPaint;
    private Paint mScanRecPaint;
    private Rect mScanRec;
    private int cornerWidth = 6;
    private int cornetHeight = 60;

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
        mScanRecPaint.setColor(getResources().getColor(R.color.scanview_rect));
        mScanMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScanMaskPaint.setColor(getResources().getColor(R.color.scanview_mask));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画背景
        drawMask(canvas);
        //画扫描框边界
        drawScanRectBorders(canvas);
        //画扫描框四个角
        drawScanRectCorners(canvas);
    }

    private void drawMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        // Draw the exterior (i.e. outside the framing rect) darkened
        canvas.drawRect(0, 0, width, mScanRec.top, mScanMaskPaint);
        canvas.drawRect(0, mScanRec.top, mScanRec.left, mScanRec.bottom + 1, mScanMaskPaint);
        canvas.drawRect(mScanRec.right + 1, mScanRec.top, width, mScanRec.bottom + 1, mScanMaskPaint);
        canvas.drawRect(0, mScanRec.bottom + 1, width, height, mScanMaskPaint);
    }

    /**
     * 画扫描框四个角
     *
     * @param canvas
     */
    private void drawScanRectCorners(Canvas canvas) {
        //upper left corner
        canvas.drawRect(mScanRec.left - 1, mScanRec.top - 1, mScanRec.left + cornerWidth, mScanRec.top + cornetHeight, mScanRecPaint);
        canvas.drawRect(mScanRec.left - 1, mScanRec.top - 1, mScanRec.left + cornetHeight, mScanRec.top + cornerWidth, mScanRecPaint);

        //upper right corner
        canvas.drawRect(mScanRec.right - cornerWidth, mScanRec.top - 1, mScanRec.right + 1, mScanRec.top + cornetHeight, mScanRecPaint);
        canvas.drawRect(mScanRec.right - cornetHeight, mScanRec.top - 1, mScanRec.right + 1, mScanRec.top + cornerWidth, mScanRecPaint);

        //lower left corner
        canvas.drawRect(mScanRec.left - 1, mScanRec.bottom - cornetHeight, mScanRec.left + cornerWidth, mScanRec.bottom + 1, mScanRecPaint);
        canvas.drawRect(mScanRec.left - 1, mScanRec.bottom - cornerWidth, mScanRec.left + cornetHeight, mScanRec.bottom + 1, mScanRecPaint);

        //lower right corner
        canvas.drawRect(mScanRec.right - cornetHeight, mScanRec.bottom - cornerWidth, mScanRec.right + 1, mScanRec.bottom + 1, mScanRecPaint);
        canvas.drawRect(mScanRec.right - cornerWidth, mScanRec.bottom - cornetHeight, mScanRec.right + 1, mScanRec.bottom + 1, mScanRecPaint);
    }

    /**
     * 画扫描框边界
     *
     * @param canvas
     */
    private void drawScanRectBorders(Canvas canvas) {
        //画左边界
        canvas.drawLine(mScanRec.left, mScanRec.top, mScanRec.left, mScanRec.bottom, mScanRecPaint);
        //画上边界
        canvas.drawLine(mScanRec.left, mScanRec.top, mScanRec.right, mScanRec.top, mScanRecPaint);
        //画右边界
        canvas.drawLine(mScanRec.right, mScanRec.top, mScanRec.right, mScanRec.bottom, mScanRecPaint);
        //画下边界
        canvas.drawLine(mScanRec.left, mScanRec.bottom, mScanRec.right, mScanRec.bottom, mScanRecPaint);
    }

    public void setScanRec(Rect scanRec) {
        this.mScanRec = scanRec;
    }
}
