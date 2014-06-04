package com.lastbear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class GroupView extends View {
	private static boolean debug_rectangle = false;

	public static final int TOPADING_SIZE = 8;
	public static final int BACKWARD_WIDTH = 89;
	public static final int BACKWARD_HEIGHT = 80;
	public static final int INDICATE_WIDTH = 200;
	public static final int INDICATE_HEIGHT = 80;

	public interface OnListener {
		void onMission(String groupId, int missionIndex);

		void onBackward();
	}

	private Paint mainPaint = null;
	private Paint contentPaint = null;

	private LibraryGroup libraryGroup = null;
	private OnListener onListener = null;
	private RectF contentRect = new RectF();
	private RectF insightRect = new RectF();
	private RectF backwardRect = new RectF();
	private RectF indicateRect = new RectF();

	private Bitmap background = null;

	public GroupView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GroupView(Context context) {
		super(context);
	}

	public void initailze(LibraryGroup group, OnListener listener) {
		// System.out.println("LibraryView: initailze");
		this.libraryGroup = group;
		this.onListener = listener;

		background = Toolbox.getInstance().getBitmap(
				R.drawable.background_library);
	}

	private GestureDetector gestureDetector = new GestureDetector(
			new GestureDetector.OnGestureListener() {

				public boolean onFling(MotionEvent e1, MotionEvent e2,
						float velocityX, float velocityY) {
					// System.out.println("onFling: " + velocityX);
					redrawHandler.newton(velocityX);
					return false;
				}

				public boolean onScroll(MotionEvent e1, MotionEvent e2,
						float distanceX, float distanceY) {
					// System.out.println("onScroll: " + distanceX);
					if (contentRect.left <= insightRect.left
							&& insightRect.right <= contentRect.right) {
						insightRect.offset(distanceX, 0);
					} else {
						if ((insightRect.left < contentRect.left && distanceX > 0)
								|| (contentRect.right < insightRect.right && distanceX < 0)) {
							insightRect.offset(distanceX, 0);
						} else {
							insightRect.offset(distanceX / 2, 0);
						}
					}
					redrawHandler.update();
					return false;
				}

				public boolean onSingleTapUp(MotionEvent event) {
					float x1 = event.getX() + insightRect.left;
					float y1 = event.getY();
					for (int i = 0; i < libraryGroup.getMissions().size(); i++) {
						LibraryMission libraryMission = libraryGroup
								.getMissions().get(i);
						if (libraryMission.getRange().contains(x1, y1)) {
							if (onListener != null) {
								onListener.onMission(libraryGroup.getId(),
										libraryMission.getIndex());
							}
							return false;
						}
					}
					if (backwardRect.contains((int) event.getX(),
							(int) event.getY())) {
						if (onListener != null) {
							onListener.onBackward();
						}
						return false;
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
				redrawHandler.hooke();
				break;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// System.out.println("LibraryView: onSizeChanged: " + w + ", " + h);

		insightRect.set(0, 0, w, h);
		contentRect.set(0, 0, w, h);
		backwardRect.set(10, h - 10 - BACKWARD_HEIGHT, 10 + BACKWARD_WIDTH,
				h - 10);
		indicateRect.set(w / 2 - INDICATE_WIDTH / 2, h - 10 - INDICATE_HEIGHT,
				w / 2 + INDICATE_WIDTH / 2, h - 10);

		if (libraryGroup == null)
			return;
		int missions = libraryGroup.getMissions().size();
		int columns = (int) Math.floor((missions + LibraryGroup.RAWS - 1)
				/ LibraryGroup.RAWS);
		contentRect.set(0, 0,
				Math.max(LibraryMission.RANGE_WIDTH * columns, w), h);
		insightRect.set(0, 0, w, h);

		for (int i = 0; i < missions; i++) {
			int m = i / LibraryGroup.RAWS;
			int n = i % LibraryGroup.RAWS;
			LibraryMission libraryMission = libraryGroup.getMissions().get(i);
			libraryMission.setRange(LibraryMission.RANGE_WIDTH * m,
					LibraryMission.RANGE_HEIGHT * n + TOPADING_SIZE);
		}
	}

	/**
	 * Create a simple handler that we can use to cause animation to happen.
	 */
	private RefreshHandler redrawHandler = new RefreshHandler();

	private class RefreshHandler extends Handler {
		private static final long INTERVAL = 10;
		private static final int WHAT_UPDATE = 0;
		private static final int WHAT_NEWTON = 1;
		private static final int WHAT_HOOKE = 2;

		private float velocity = 0;
		private float accelerate = 0;
		private float distance = 0;
		private long timestamp = 0;

		@Override
		public void handleMessage(Message msg) {
			float seconds = (System.currentTimeMillis() - timestamp) / 1000.0f;
			timestamp = System.currentTimeMillis();

			switch (msg.what) {
			case WHAT_UPDATE:
				break;
			case WHAT_NEWTON:
				insightRect.offset(-velocity * seconds, 0);
				velocity *= accelerate * seconds + 1;
				if (insightRect.left < contentRect.left
						|| contentRect.right < insightRect.right) {
					hooke();
				} else if (Math.abs(velocity) > 80) {
					sendEmptyMessageDelayed(WHAT_NEWTON, INTERVAL);
				}
				break;
			case WHAT_HOOKE:
				insightRect.offset(-velocity * seconds, 0);
				distance -= Math.abs(velocity * seconds);
				velocity *= accelerate * seconds + 1;
				if (Math.abs(velocity) > 10 && distance > 0) {
					sendEmptyMessageDelayed(WHAT_HOOKE, INTERVAL);
				} else {
					float a = contentRect.left - insightRect.left;
					float b = contentRect.right - insightRect.right;
					if (Math.abs(a) < Math.abs(b)) {
						insightRect.offset(a, 0);
					} else {
						insightRect.offset(b, 0);
					}
				}
				break;
			}
			GroupView.this.invalidate();
		}

		public void update() {
			if (hasMessages(WHAT_UPDATE) || hasMessages(WHAT_NEWTON)
					|| hasMessages(WHAT_HOOKE)) {
				return;
			} else {
				sendEmptyMessageDelayed(WHAT_UPDATE, INTERVAL);
			}
		}

		public void newton(float velocity) {
			if (Math.abs(velocity) > 800) {
				velocity = velocity > 0 ? 800 : -800;
			}
			this.velocity = velocity;
			accelerate = -0.5f;
			timestamp = System.currentTimeMillis();
			removeMessages(WHAT_UPDATE);
			removeMessages(WHAT_NEWTON);
			removeMessages(WHAT_HOOKE);
			sendEmptyMessageDelayed(WHAT_NEWTON, INTERVAL);
		}

		public void hooke() {
			if (hasMessages(WHAT_NEWTON))
				return;
			removeMessages(WHAT_NEWTON);
			removeMessages(WHAT_HOOKE);

			float a = insightRect.left - contentRect.left;
			float b = insightRect.right - contentRect.right;
			if (a < 0) {
				velocity = a;
				distance = Math.abs(a);
			} else if (b > 0) {
				velocity = b;
				distance = Math.abs(b);
			} else {
				return;
			}
			accelerate = -0.5f;
			timestamp = System.currentTimeMillis();
			sendEmptyMessageDelayed(WHAT_HOOKE, INTERVAL);
		}

		public void cancel() {
			removeMessages(WHAT_NEWTON);
			removeMessages(WHAT_HOOKE);
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// System.out.println("LibraryView: onDraw");
		if (mainPaint == null) {
			mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mainPaint.setTypeface(Toolbox.getInstance().getTypeface());
			mainPaint.setAntiAlias(true); // …Ë÷√PaintŒ™Œﬁæ‚≥›
			mainPaint.setTextSize(30);
			mainPaint.setTextAlign(Paint.Align.CENTER);
			mainPaint.setStyle(Paint.Style.STROKE);// Paint.Style.FILL_AND_STROKE;
			// mainPaint.setStrokeWidth(1);
			mainPaint.setColor(Color.WHITE);
			// mainPaint.setAlpha(0);
		}
		if (contentPaint == null) {
			contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			contentPaint.setTypeface(Toolbox.getInstance().getTypeface());
			contentPaint.setAntiAlias(true);
			contentPaint.setTextSize(30);
			contentPaint.setTextAlign(Paint.Align.CENTER);
			contentPaint.setStyle(Paint.Style.STROKE);
			contentPaint.setColor(Color.WHITE);
		}

		canvas.drawBitmap(background, 0, 0, null);

		for (int i = 0; i < libraryGroup.getMissions().size(); i++) {
			LibraryMission libraryMission = libraryGroup.getMissions().get(i);
			canvas.save();
			canvas.translate(libraryMission.getRange().left - insightRect.left,
					libraryMission.getRange().top - insightRect.top);
			libraryMission.draw(canvas, insightRect, contentPaint);
			canvas.restore();
		}

		// draw indicator
		float a = insightRect.left / contentRect.width();
		float b = insightRect.right / contentRect.width();

		float insight_start = indicateRect.left + INDICATE_WIDTH * a;
		float insight_end = indicateRect.left + INDICATE_WIDTH * b;
		if (insight_start < indicateRect.left)
			insight_start = indicateRect.left;
		if (insight_start > indicateRect.right)
			insight_start = indicateRect.right;
		if (insight_end > indicateRect.right)
			insight_end = indicateRect.right;
		if (insight_end < indicateRect.left)
			insight_end = indicateRect.left;

		canvas.drawLine(insight_start, indicateRect.centerY() - 4, insight_end,
				indicateRect.centerY() - 4, mainPaint);
		canvas.drawLine(indicateRect.left, indicateRect.centerY(),
				indicateRect.right, indicateRect.centerY(), mainPaint);
		mainPaint.setStrokeWidth(1);
		canvas.drawText(libraryGroup.getName() + "-" + libraryGroup.getId(),
				indicateRect.centerX(),
				indicateRect.centerY() - mainPaint.getFontMetrics().ascent + 4,
				mainPaint);

		canvas.drawRoundRect(indicateRect, 10, 10, mainPaint);

		// draw backward
		canvas.save();
		Path path = new Path();
		path.addRoundRect(backwardRect, 10, 10, Path.Direction.CCW);
		canvas.clipPath(path);
		canvas.drawBitmap(
				Toolbox.getInstance().getBitmap(R.drawable.button_back),
				backwardRect.left, backwardRect.top, mainPaint);
		canvas.restore();

		if (debug_rectangle) {
			Paint debug_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			debug_paint.setStyle(Paint.Style.STROKE);
			debug_paint.setColor(Color.RED);
			canvas.drawRect(insightRect, debug_paint);
			debug_paint.setColor(Color.GREEN);
			canvas.drawRect(contentRect, debug_paint);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// System.out.println("LibraryView: onFinishInflate");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// System.out.println("LibraryView: onMeasure");

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
		// System.out.println("LibraryView: onLayout");
	}
}
