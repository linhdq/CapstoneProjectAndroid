package com.ldz.fpt.businesscardscannerandroid.activity.crop_image_activity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.ldz.fpt.businesscardscannerandroid.R;

public class CropHighlightView implements HighlightView {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = CropHighlightView.class.getSimpleName();
    private View mContext; // The View displaying the image.

    /* used during onDraw */
    private final Rect viewDrawingRect = new Rect();
    private final Rect leftRect = new Rect();
    private final Rect rightRect = new Rect();
    private final Rect topRect = new Rect();
    private final Rect bottomRect = new Rect();
    private final RectF pathBounds = new RectF();
    private final Rect pathBoundsRounded = new Rect();
    private final Rect canvasCLipRect = new Rect();

    private final CropWindow trapzoid;
    boolean isFocused;
    boolean hidden = false;

    private Rect mDrawRect; // in screen space
    private Matrix mMatrix;

    private final Paint mFocusPaint = new Paint();
    private final Paint mOutlinePaint = new Paint();
    private final int mCropCornerHandleRadius;
    private final int mCropEdgeHandleRadius;
    private final float mHysteresis;


    public CropHighlightView(ImageView ctx, Rect imageRect, RectF cropRect, float[] listPoint) {
        mContext = ctx;
        final int progressColor = mContext.getResources().getColor(R.color.progress_color);
        mCropCornerHandleRadius = mContext.getResources().getDimensionPixelSize(R.dimen.crop_handle_corner_radius);
        mCropEdgeHandleRadius = mContext.getResources().getDimensionPixelSize(R.dimen.crop_handle_edge_radius);
        mHysteresis = mContext.getResources().getDimensionPixelSize(R.dimen.crop_hit_hysteresis);
        final int edgeWidth = mContext.getResources().getDimensionPixelSize(R.dimen.crop_edge_width);
        mMatrix = new Matrix(ctx.getImageMatrix());
        Log.i(LOG_TAG, "image = " + imageRect.toString() + " crop = " + cropRect.toString());
        trapzoid = new CropWindow(cropRect, imageRect, listPoint);

        mDrawRect = computeLayout();

        mFocusPaint.setARGB(125, 50, 50, 50);
        mFocusPaint.setStyle(Paint.Style.FILL);

        mOutlinePaint.setARGB(0xFF, Color.red(progressColor), Color.green(progressColor), Color.blue(progressColor));
        mOutlinePaint.setStrokeWidth(edgeWidth);
        mOutlinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mOutlinePaint.setAntiAlias(true);
    }


    public void setFocus(boolean f) {
        isFocused = f;
    }

    @Override
    public void draw(Canvas canvas) {
        if (hidden) {
            return;
        }
        mDrawRect = computeLayout();
        drawEdges(canvas);

    }

    private void drawEdges(Canvas canvas) {
        final float[] p = trapzoid.getScreenPoints(getMatrix());
        Path path = new Path();
        path.moveTo((int) p[0], (int) p[1]);
        path.lineTo((int) p[2], (int) p[3]);
        path.lineTo((int) p[4], (int) p[5]);
        path.lineTo((int) p[6], (int) p[7]);
        path.close();
        path.computeBounds(pathBounds, false);
        pathBounds.round(pathBoundsRounded);
        canvas.getClipBounds(canvasCLipRect);

        mContext.getDrawingRect(viewDrawingRect);
        topRect.set(0, 0, viewDrawingRect.right, getDrawRect().top);
        rightRect.set(0, getDrawRect().top, getDrawRect().left, getDrawRect().bottom);
        leftRect.set(getDrawRect().right, getDrawRect().top, viewDrawingRect.right, getDrawRect().bottom);
        bottomRect.set(0, getDrawRect().bottom, viewDrawingRect.right, viewDrawingRect.bottom);

        canvas.drawRect(topRect, mFocusPaint);
        canvas.drawRect(rightRect, mFocusPaint);
        canvas.drawRect(leftRect, mFocusPaint);
        canvas.drawRect(bottomRect, mFocusPaint);
        if (canvasCLipRect.contains(pathBoundsRounded)) {
            canvas.save();
            canvas.clipRect(getDrawRect());
            path.setFillType(Path.FillType.INVERSE_EVEN_ODD);
            canvas.drawPath(path, mFocusPaint);
            canvas.restore();
        }
        canvas.drawLine(p[0], p[1], p[2], p[3], mOutlinePaint);
        canvas.drawLine(p[2], p[3], p[4], p[5], mOutlinePaint);
        canvas.drawLine(p[4], p[5], p[6], p[7], mOutlinePaint);
        canvas.drawLine(p[0], p[1], p[6], p[7], mOutlinePaint);

        canvas.drawCircle(p[0], p[1], mCropCornerHandleRadius, mOutlinePaint);
        canvas.drawCircle(p[2], p[3], mCropCornerHandleRadius, mOutlinePaint);
        canvas.drawCircle(p[4], p[5], mCropCornerHandleRadius, mOutlinePaint);
        canvas.drawCircle(p[6], p[7], mCropCornerHandleRadius, mOutlinePaint);

        float x = (p[0] + p[2]) / 2;
        float y = (p[1] + p[3]) / 2;
        canvas.drawCircle(x, y, mCropEdgeHandleRadius, mOutlinePaint);
        x = (p[2] + p[4]) / 2;
        y = (p[3] + p[5]) / 2;
        canvas.drawCircle(x, y, mCropEdgeHandleRadius, mOutlinePaint);
        x = (p[4] + p[6]) / 2;
        y = (p[5] + p[7]) / 2;
        canvas.drawCircle(x, y, mCropEdgeHandleRadius, mOutlinePaint);
        x = (p[0] + p[6]) / 2;
        y = (p[1] + p[7]) / 2;
        canvas.drawCircle(x, y, mCropEdgeHandleRadius, mOutlinePaint);


    }


    // Determines which edges are hit by touching at (x, y).
    public int getHit(float x, float y, float scale) {
        // convert hysteresis to imagespace
        final float hysteresis = mHysteresis / scale;
        return trapzoid.getHit(x, y, hysteresis);
    }


    // Handles motion (dx, dy) in screen space.
    // The "edge" parameter specifies which edges the user is dragging.
    @Override
    public void handleMotion(int edge, float dx, float dy) {
        if (edge == GROW_NONE) {
            return;
        } else if (edge == MOVE) {
            trapzoid.moveBy(dx, dy);
        } else {
            trapzoid.growBy(edge, dx, dy);
        }
        mDrawRect = computeLayout();
    }

    /**
     * @return cropping rectangle in image space.
     */
    public Rect getCropRect() {
        return trapzoid.getBoundingRect();
    }

    public float[] getTrapezoid() {
        return trapzoid.getPoints();
    }

    public Rect getPerspectiveCorrectedBoundingRect() {
        return trapzoid.getPerspectiveCorrectedBoundingRect();
    }

    // Maps the cropping rectangle from image space to screen space.
    private Rect computeLayout() {
        return trapzoid.getBoundingRect(getMatrix());
    }

    @Override
    public Matrix getMatrix() {
        return mMatrix;
    }

    @Override
    public Rect getDrawRect() {
        return mDrawRect;
    }

    @Override
    public float centerY() {
        return getCropRect().centerY();
    }

    @Override
    public float centerX() {
        return getCropRect().centerX();
    }
}
