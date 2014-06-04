package com.lastbear;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SampleView extends View {

	int color[] = { Color.RED, Color.GREEN, Color.YELLOW, Color.BLACK,
			Color.CYAN };
	Paint paint = null;
	Bitmap android = null;
	Bitmap nokia = null;

	Rect rect = null;
	Region region = null;
	Path path = null;

	Rect drag_object = null;
	Point drag_root_point = new Point(0, 0);
	Point drag_leaf_point = new Point(0, 0);
	int drag_root_pid = -1;
	int drag_leaf_pid = -1;

	public SampleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initailze();
	}

	public SampleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initailze();
	}

	public SampleView(Context context) {
		super(context);
		initailze();
	}

	private void initailze() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(44);
		paint.setColor(0xFF00F000);
		setBackgroundResource(R.drawable.background_library);

		Drawable d1 = this.getResources().getDrawable(R.drawable.android);
		android = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(android);
		d1.setBounds(0, 0, 100, 100);
		d1.draw(canvas);

		InputStream is = this.getResources().openRawResource(
				R.drawable.button_back);
		nokia = BitmapFactory.decodeStream(is);

		rect = new Rect(350, 10, 450, 110);

		region = new Region(20, 500, 120, 600);
		region.op(70, 550, 170, 650, Region.Op.XOR);

		path = new Path();
		path.moveTo(50, 0);
		path.lineTo(30, 110);
		path.lineTo(50, 100);
		path.lineTo(70, 110);
		path.close();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		System.out.println("SevenView: onFinishInflate");
	}

	private String dumpEvent(MotionEvent event) {
		String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
				"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		StringBuilder sb = new StringBuilder();
		sb.append("ACTION_" + names[event.getActionMasked()]);
		sb.append("{index=" + event.getActionIndex());
		sb.append(",id=" + event.getPointerId(event.getActionIndex()) + "}");
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid " + event.getPointerId(i) + ")");

			for (int j = 0; j < event.getHistorySize(); j++) {
				sb.append((int) event.getHistoricalX(i, j) + ","
						+ (int) event.getHistoricalY(i, j) + "~");
			}

			sb.append((int) event.getX(i) + "," + (int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";");
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		System.out.println("LibraryView: onTouchEvent : " + dumpEvent(event));

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_CANCEL:
			break;
		case MotionEvent.ACTION_DOWN:
			float x = event.getX(event.getActionIndex());
			float y = event.getY(event.getActionIndex());
			if (rect.contains((int) x, (int) y)) {
				drag_object = rect;
				drag_root_pid = event.getPointerId(event.getActionIndex());
				drag_root_point.set((int) x, (int) y);
			} else {
				drag_object = null;
				drag_root_pid = -1;
				drag_root_point.set(0, 0);
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			float x2 = event.getX(event.getActionIndex());
			float y2 = event.getY(event.getActionIndex());
			if (drag_object != null) {
				drag_leaf_pid = event.getPointerId(event.getActionIndex());
				drag_leaf_point.set((int) x2, (int) y2);
			} else {
				drag_leaf_pid = -1;
				drag_leaf_point.set(0, 0);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (drag_object != null) {
				for (int i = 0; i < event.getPointerCount(); i++) {
					if (event.getPointerId(i) == drag_root_pid) {
						int dx = (int) event.getX(i) - drag_root_point.x;
						int dy = (int) event.getY(i) - drag_root_point.y;
						drag_object.offset(dx, dy);
						drag_root_point.offset(dx, dy);
						invalidate();
					} else if (event.getPointerId(i) == drag_leaf_pid) {

					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			drag_object = null;
			break;
		default:
			// System.out.println("SevenView: onTouchEvent : ");
			break;
		}
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		System.out.println("LibraryView: onMeasure");

		// int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		// int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(widthSpecSize, heightSpecSize);

		System.out.println("widht=" + widthSpecSize + "height="
				+ heightSpecSize);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		System.out.println("SevenView: onLayout");
	}

	float deg = 0.0f;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		System.out.println("LibraryView: onDraw");
		Matrix matrix = new Matrix();

		for (int i = 0; i <= canvas.getWidth(); i += 100) {
			canvas.drawLine(i, 0, i, 10, paint);
		}
		for (int j = 0; j <= canvas.getHeight(); j += 100) {
			canvas.drawLine(0, j, 10, j, paint);
		}
		// canvas.drawColor(Color.WHITE);
		canvas.drawText("Ling Jianfa", 100, 100, paint);
		canvas.drawBitmap(nokia, 20, 120, paint);
		/*
		 * matrix.reset(); matrix.setTranslate(350, 120);
		 * matrix.postRotate(deg++, 350, 120);
		 * 
		 * canvas.drawBitmap(android, matrix, paint);
		 */
		canvas.drawRect(rect, paint);

		RegionIterator iter = new RegionIterator(region);
		Rect r = new Rect();
		int x = 0;
		while (iter.next(r)) {
			Paint p = new Paint();
			p.setColor(color[x++]);
			canvas.drawRect(r, p);
		}
		canvas.drawPath(path, paint);
		/*
		 * rect.offset(0, 1); region.translate(1, -1); matrix.reset();
		 * matrix.setTranslate(1, 1); path.transform(matrix); invalidate();
		 */

		float Ax = 350.0f;
		float Ay = 120.0f;
		float Bx = 450.0f;
		float By = 120.0f;
		float Cx = 350.0f;
		float Cy = 220.0f;
		float a2 = (Bx - Cx) * (Bx - Cx) + (By - Cy) * (By - Cy);
		float b2 = (Cx - Ax) * (Cx - Ax) + (Cy - Ay) * (Cy - Ay);
		float c2 = (Ax - Bx) * (Ax - Bx) + (Ay - By) * (Ay - By);
		float a = (float) Math.sqrt((double) a2);
		float b = (float) Math.sqrt((double) b2);
		float c = (float) Math.sqrt((double) c2);
		float cosA = (c2 + b2 - a2) / (2 * b * c);
		// float sinA = (float) Math.sqrt(1 - cosA * cosA);
		float d = (float) Math.toDegrees(Math.acos(cosA));
		System.out.printf("abc = %f,%f,%f, %f, %f\n", a, b, c, cosA, d);
		matrix.reset();
		matrix.setTranslate(350, 120);
		matrix.postRotate(d, 350, 120);

		canvas.drawBitmap(android, matrix, paint);

		// matrix.setSinCos(sinA, cosA, Ax, Ay);
		// matrix.setSinCos(0.087f, 0.966f, Ax, Ay);
		// matrix.setRotate(d, Ax, Ay);

	}

}
