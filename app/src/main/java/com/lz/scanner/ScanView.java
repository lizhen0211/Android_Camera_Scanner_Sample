package com.lz.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lz on 2018/4/19.
 */

public class ScanView extends View {

    private static final String TAG = ScanView.class.getSimpleName();

    private Paint mScanMaskPaint;
    private Paint mScanRecPaint;
    private Rect mScanRec;
    private int cornerWidth = 6;
    private int cornetHeight = 60;
    private Paint mTipPaint;
    private int textPaddingTop;

    private int laserTopOffset = 0;
    private static final int SPEEN_DISTANCE = 5;
    private Rect laserLineRect;
    private Drawable laserDrawable;
    private static final int MIDDLE_LINE_PADDING = 0;
    private int MIDDLE_LINE_WIDTH = 6;

    private Rect laserGridRect;
    private int laserGridTopOffset = 0;
    private Paint mlaserGridPaint;
    private static final int LASER_PADDING_WIDTH = 10;

    public ScanView(Context context) {
        this(context, null);
    }

    public ScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        float density = context.getResources().getDisplayMetrics().density;

        mScanRecPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScanRecPaint.setColor(getResources().getColor(R.color.scanview_rect));
        mScanMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScanMaskPaint.setColor(getResources().getColor(R.color.scanview_mask));
        mTipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTipPaint.setColor(Color.WHITE);
        mTipPaint.setTextSize((int) (13 * density));
        mTipPaint.setAlpha(0x40);
        mTipPaint.setTypeface(Typeface.create("System", Typeface.BOLD));

        textPaddingTop = (int) (40 * density);

        laserLineRect = new Rect();
        laserDrawable = context.getResources().getDrawable(R.drawable.scan_pointer);

        laserGridRect = new Rect();
        mlaserGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mlaserGridPaint.setColor(getResources().getColor(R.color.laser_grid_color));
        mlaserGridPaint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画背景
        drawMask(canvas);
        //画激光线
        //drawLaserLine(canvas);
        //画激光网格
        drawLaserGrid(canvas);
        //画扫描框边界
        drawScanRectBorders(canvas);
        //画扫描框四个角
        drawScanRectCorners(canvas);
        //画提示文字
        drawDescText(canvas);
    }

    private void drawLaserGrid(Canvas canvas) {
        int width = mScanRec.width();
        int height = mScanRec.height();
        int vertLinenum = width / LASER_PADDING_WIDTH;
        if ((laserGridTopOffset += (LASER_PADDING_WIDTH / 2)) < mScanRec.bottom - mScanRec.top) {
            //画竖线，从左向右画
            for (int i = 1; i < vertLinenum; i++) {
                int startX = mScanRec.left + LASER_PADDING_WIDTH * i;
                int stopX = startX;
                int startY = mScanRec.top;
                int stopY = startY + LASER_PADDING_WIDTH;
                canvas.drawLine(startX, startY, stopX, stopY + laserGridTopOffset, mlaserGridPaint);
            }

            //横线线条，从上向下画
            int gridHeight = laserGridTopOffset;
            //竖直方向有多少条横线
            int horiLinenum = gridHeight / LASER_PADDING_WIDTH;
            for (int i = 1; i < horiLinenum + 1; i++) {
                int startX = mScanRec.left;
                int stopX = startX + width;
                int startY = mScanRec.top + LASER_PADDING_WIDTH * i;
                int stopY = startY;
                //设置渐变色,Y值越大，颜色越深
                int alpha = (int) ((i * (float) LASER_PADDING_WIDTH / (float) gridHeight) * 255);
                //Log.e(TAG, alpha + "");
                mlaserGridPaint.setAlpha(alpha);
                canvas.drawLine(startX, startY, stopX, stopY, mlaserGridPaint);
            }
        } else {
            laserGridTopOffset = 0;
        }
        invalidate();
    }

    /**
     * 画激光线
     */
    private void drawLaserLine(Canvas canvas) {
        if ((laserTopOffset += SPEEN_DISTANCE) < mScanRec.bottom - mScanRec.top) {
            laserLineRect.set(mScanRec.left + MIDDLE_LINE_PADDING, mScanRec.top + laserTopOffset - MIDDLE_LINE_WIDTH / 2, mScanRec.right - MIDDLE_LINE_PADDING, mScanRec.top + laserTopOffset + MIDDLE_LINE_WIDTH / 2);
            laserDrawable.setBounds(laserLineRect);
            laserDrawable.draw(canvas);
        } else {
            laserTopOffset = 0;
        }
        invalidate();
    }

    /**
     * 画提示文字
     *
     * @param canvas
     */
    private void drawDescText(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Rect bounds = new Rect();
        mTipPaint.getTextBounds(getResources().getString(R.string.scan_prompt), 0, getResources().getString(R.string.scan_prompt).length(), bounds);
        int textWidth = bounds.width();
        canvas.drawText(getResources().getString(R.string.scan_prompt), width / 2 - textWidth / 2, mScanRec.bottom + (float) textPaddingTop, mTipPaint);
    }

    /**
     * 画背景
     *
     * @param canvas
     */
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
