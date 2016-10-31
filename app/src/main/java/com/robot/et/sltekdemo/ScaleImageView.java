package com.robot.et.sltekdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

@SuppressLint("FloatMath")
public class ScaleImageView extends ImageView {

	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	public ScaleImageView(Context context) {
		super(context);
	}

	public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ScaleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
//		return FloatMath.sqrt(x * x + y * y);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
			Log.d("Infor", "������");
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			matrix.set(getImageMatrix());
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			Log.d("Infor", "������...");
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN: // ��㴥��
			oldDist = this.spacing(event);
			if (oldDist > 10f) {
				Log.d("Infor", "oldDist" + oldDist);
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) { // ��ʵ��ͼƬ���϶�����...
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY()
						- start.y);
			} else if (mode == ZOOM) {// ��ʵ��ͼƬ�����Ź���...
				float newDist = spacing(event);
				if (newDist > 10) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}
		setImageMatrix(matrix);
		return true;
	}
}
