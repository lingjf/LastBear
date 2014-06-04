package com.lastbear;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class LastBearView extends View {

	public interface OnListener {
		void onLibrary(String groupId);
	}

	private ArrayList<LibraryGroup> libraryGroups = null;
	private OnListener onListener = null;

	// private RectF contentRect = new RectF();
	private RectF insightRect = new RectF();

	private Paint mainPaint = null;
	private Bitmap background = null;

	public LastBearView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LastBearView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LastBearView(Context context) {
		super(context);
	}

	public void initailze(ArrayList<LibraryGroup> groups, OnListener listener) {
		// System.out.println("LastBearView: initailze");
		this.libraryGroups = groups;
		this.onListener = listener;

		background = Toolbox.getInstance().getBitmap(
				R.drawable.background_welcome);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// System.out.println("LastBearView: onSizeChanged: " + w + ", " + h);

		RectF rect = new RectF(0, 0, 260, 260);
		insightRect.set(0, -(h - rect.height() / 2), w, rect.height() / 2);

		rect.offset(w / 2 - rect.centerX(), 0);
		for (int i = 0; i < libraryGroups.size(); i++) {
			LibraryGroup group = libraryGroups.get(i);
			group.setRect(rect);
			rect.offset(0, h / 2);
		}

		redrawHandler.hooke(-1);
	}

	private GestureDetector gestureDetector = new GestureDetector(
			new GestureDetector.OnGestureListener() {

				public boolean onFling(MotionEvent e1, MotionEvent e2,
						float velocityX, float velocityY) {
					// System.out.println("F: " + velocityX + "," + velocityY);
					if (Math.abs(velocityY) > 300.0f) {
						int target;
						if (velocityY > 0) {
							int i;
							for (i = 0; i < libraryGroups.size(); i++) {
								if (libraryGroups.get(i).getRect().centerY() >= insightRect
										.centerY()) {
									break;
								}
							}
							target = i - 1;
						} else {
							int i;
							for (i = libraryGroups.size() - 1; i >= 0; i--) {
								if (libraryGroups.get(i).getRect().centerY() <= insightRect
										.centerY()) {
									break;
								}
							}
							target = i + 1;
						}
						redrawHandler.hooke(target);
						return true;
					}
					return false;
				}

				public boolean onScroll(MotionEvent e1, MotionEvent e2,
						float distanceX, float distanceY) {
					// System.out.println("S: " + distanceX + "," + distanceY);
					insightRect.offset(0, distanceY);
					redrawHandler.update();
					return false;
				}

				public boolean onSingleTapUp(MotionEvent event) {
					float x1 = event.getX() + insightRect.left;
					float y1 = event.getY() + insightRect.top;
					for (int i = 0; i < libraryGroups.size(); i++) {
						LibraryGroup libraryGroup = libraryGroups.get(i);
						if (libraryGroup.getRect().contains(x1, y1)) {
							if (onListener != null) {
								onListener.onLibrary(libraryGroup.getId());
							}
							return false;
						}
					}
					return false;
				}

				public boolean onDown(MotionEvent event) {
					return false;
				}

				public void onLongPress(MotionEvent event) {
				}

				public void onShowPress(MotionEvent event) {
				}
			});

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		} else {
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_CANCEL:
				break;
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				redrawHandler.cancel();
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				redrawHandler.hooke(-1);
				break;
			default:
				break;
			}
		}
		return true;
	}

	private RefreshHandler redrawHandler = new RefreshHandler();

	private class RefreshHandler extends Handler {
		private static final long INTERVAL = 10;
		private static final int WHAT_UPDATE = 0;
		private static final int WHAT_HOOKE = 2;

		private float velocity = 0;
		private float accelerate = 0;
		private float distance = 0;
		private RectF targetRect = new RectF();
		private long timestamp = 0;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_UPDATE:
				break;
			case WHAT_HOOKE:
				float seconds = (System.currentTimeMillis() - timestamp) / 1000.0f;
				timestamp = System.currentTimeMillis();
				insightRect.offset(0, velocity * seconds);
				distance -= Math.abs(velocity * seconds);
				velocity *= accelerate * seconds + 1;
				if (Math.abs(velocity) > 10 && distance > 0) {
					sendEmptyMessageDelayed(WHAT_HOOKE, INTERVAL);
				} else {
					insightRect.set(targetRect);
				}
				break;
			}
			LastBearView.this.invalidate();
		}

		public void update() {
			if (hasMessages(WHAT_UPDATE) || hasMessages(WHAT_HOOKE)) {
				return;
			} else {
				sendEmptyMessageDelayed(WHAT_UPDATE, INTERVAL);
			}
		}

		public void hooke(int target) {
			removeMessages(WHAT_HOOKE);
			if (target < 0 || libraryGroups.size() <= target) {
				float d = 1000000.0f;
				for (int i = 0; i < libraryGroups.size(); i++) {
					LibraryGroup group = libraryGroups.get(i);
					float t = group.getRect().centerY() - insightRect.centerY();
					if (Math.abs(t) < Math.abs(d)) {
						d = t;
						target = i;
					}
				}
			}

			targetRect.set(insightRect);
			targetRect.offset(0, libraryGroups.get(target).getRect().centerY()
					- insightRect.centerY());

			velocity = targetRect.centerY() - insightRect.centerY();
			distance = Math.abs(velocity);

			accelerate = -0.5f;
			timestamp = System.currentTimeMillis();
			sendEmptyMessageDelayed(WHAT_HOOKE, INTERVAL);
		}

		public void cancel() {
			removeMessages(WHAT_HOOKE);
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// System.out.println("LastBearView: onDraw");

		if (mainPaint == null) {
			mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mainPaint.setTextSize(32);
			mainPaint.setTypeface(Toolbox.getInstance().getTypeface());
			mainPaint.setAntiAlias(true);
			mainPaint.setTextAlign(Paint.Align.CENTER);
			mainPaint.setColor(Color.WHITE);
		}

		canvas.drawBitmap(background, 0, 0, null);

		canvas.save();
		canvas.translate(0, -insightRect.top);
		for (LibraryGroup libraryGroup : libraryGroups) {
			libraryGroup.draw(canvas, mainPaint);
		}
		canvas.restore();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// System.out.println("LastBearView: onMeasure");

		// int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		// int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// System.out.println("LastBearView: onLayout");
	}

}
