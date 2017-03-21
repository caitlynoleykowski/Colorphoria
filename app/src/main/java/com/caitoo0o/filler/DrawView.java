package com.caitoo0o.filler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by caitoo0o on 3/15/2017.
 */

public class DrawView extends View {

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFF000000;
    private float paintbrushSize, prevBrushSize;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private boolean eraseMode = false;
    private boolean edited = false;

    public DrawView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){
        paintbrushSize = getResources().getInteger(R.integer.extra_small_size);
        prevBrushSize = paintbrushSize;

        //get drawing area setup for interaction
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true); // for smoothness
        drawPaint.setStrokeWidth(paintbrushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND); // for smoothness
        drawPaint.setStrokeCap(Paint.Cap.ROUND); // for smoothness

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Called when view given size

        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        edited = true;
        return true;
    }

    public void setPaintColor(String newColor){
        //set color
        invalidate();
        int paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void setPaintbrushSize(float newSize){
        //update size
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        paintbrushSize = pixelAmount;
        drawPaint.setStrokeWidth(paintbrushSize);
    }

    public void setPrevBrushSize(float lastSize){
        prevBrushSize =lastSize;
    }
    public float getPrevBrushSize(){
        return prevBrushSize;
    }

    public void setEraseMode(boolean isErase){
        //set eraseMode true or false
        eraseMode = isErase;
        if(eraseMode) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }

    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public boolean isEdited() {
        return edited;
    }
}
