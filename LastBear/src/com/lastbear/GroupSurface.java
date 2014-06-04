package com.lastbear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GroupSurface extends SurfaceView implements SurfaceHolder.Callback {
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

	private SurfaceHolder surfaceHolder = null;
	private RefreshThread refreshThread = null;

	public GroupSurface(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GroupSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GroupSurface(Context context) {
		super(context);
	}

	public void initailze(LibraryGroup group, OnListener listener) {
		// System.out.println("LibrarySurface: initailze");
		this.libraryGroup = group;
		this.onListener = listener;

		background = Toolbox.getInstance().getBitmap(
				R.drawable.background_library);

		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		refreshThread = new RefreshThread();
	}

	private GestureDetector gestureDetector = new GestureDetector(
			new GestureDetector.OnGestureListener() {

				public boolean onFling(MotionEvent e1, MotionEvent e2,
						float velocityX, float velocityY) {
					// System.out.println("onFling: " + velocityX);
					refreshThread.newton(velocityX);
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
					refreshThread.update();
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
				refreshThread.cancel();
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				refreshThread.hooke();
				break;
			default:
				break;
			}
		}
		return true;
	}

	private void doDraw(Canvas canvas) {
		// System.out.println("LibrarySurface: onDraw");
		if (mainPaint == null) {
			mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mainPaint.setTypeface(Toolbox.getInstance().getTypeface());
			mainPaint.setAntiAlias(true); // ÉèÖÃPaintÎªÎÞ¾â³Ý
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

	private class RefreshThread extends Thread {

		private long count = 0;
		private long start = 0;
		private long span = 30;
		private boolean flag = true;

		private static final int WHAT_UPDATE = 1;
		private static final int WHAT_NEWTON = 2;
		private static final int WHAT_HOOKE = 4;

		private int what;
		private long timestamp = 0;
		private float velocity = 0;
		private float accelerate = 0;
		private float distance = 0;

		@Override
		public void run() {
			Canvas canvas = null;
			while (flag) {

				float percent = (System.currentTimeMillis() - timestamp) / 1000.0f;
				timestamp = System.currentTimeMillis();

				int last_what = what;
				what = 0;

				if ((what & WHAT_NEWTON) != 0) {
					insightRect.offset(-velocity * percent, 0);
					velocity *= accelerate * percent + 1;
					if (insightRect.left < contentRect.left
							|| contentRect.right < insightRect.right) {
						hooke();
					} else if (Math.abs(velocity) > 80) {
						what |= WHAT_NEWTON;
					}
				} else if ((what & WHAT_HOOKE) != 0) {
					insightRect.offset(-velocity * percent, 0);
					distance -= Math.abs(velocity * percent);
					velocity *= accelerate * percent + 1;
					if (Math.abs(velocity) > 10 && distance > 0) {
						what |= WHAT_HOOKE;
					} else {
						float a = contentRect.left - insightRect.left;
						float b = contentRect.right - insightRect.right;
						if (Math.abs(a) < Math.abs(b)) {
							insightRect.offset(a, 0);
						} else {
							insightRect.offset(b, 0);
						}
					}
				}

				if (last_what != 0) {
					try {
						canvas = surfaceHolder.lockCanvas(null);
						synchronized (surfaceHolder) {
							GroupSurface.this.doDraw(canvas);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (canvas != null) {
							surfaceHolder.unlockCanvasAndPost(canvas);
						}
					}
				}

				count++;
				if (count == 20) {
					count = 0;
					long tempStamp = System.nanoTime();
					long spank = tempStamp - start;
					start = tempStamp;
					double fps = Math.round(100000000000.0 / spank * 20) / 100.0;
					String sss = "FPS:" + fps;
				}

				try {
					Thread.sleep(span);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void update() {
			what |= WHAT_UPDATE;
		}

		public void newton(float velocity) {
			if (Math.abs(velocity) > 800) {
				velocity = velocity > 0 ? 800 : -800;
			}
			this.velocity = velocity;
			accelerate = -0.5f;
			timestamp = System.currentTimeMillis();
			what &= ~(WHAT_UPDATE | WHAT_HOOKE);
			what |= WHAT_NEWTON;
		}

		public void hooke() {
			if ((what & WHAT_NEWTON) != 0)
				return;
			what &= ~(WHAT_NEWTON | WHAT_HOOKE);

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
			what |= WHAT_HOOKE;
		}

		public void cancel() {
			what &= ~(WHAT_NEWTON | WHAT_HOOKE);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		System.out.println("LibrarySurface: surfaceChanged: " + width + ", "
				+ height);

		insightRect.set(0, 0, width, height);
		contentRect.set(0, 0, width, height);
		backwardRect.set(10, height - 10 - BACKWARD_HEIGHT,
				10 + BACKWARD_WIDTH, height - 10);
		indicateRect.set(width / 2 - INDICATE_WIDTH / 2, height - 10
				- INDICATE_HEIGHT, width / 2 + INDICATE_WIDTH / 2, height - 10);

		if (libraryGroup == null)
			return;
		int missions = libraryGroup.getMissions().size();
		int columns = (int) Math.floor((missions + LibraryGroup.RAWS - 1)
				/ LibraryGroup.RAWS);
		contentRect.set(0, 0,
				Math.max(LibraryMission.RANGE_WIDTH * columns, width), height);
		insightRect.set(0, 0, width, height);

		for (int i = 0; i < missions; i++) {
			int m = i / LibraryGroup.RAWS;
			int n = i % LibraryGroup.RAWS;
			LibraryMission libraryMission = libraryGroup.getMissions().get(i);
			libraryMission.setRange(LibraryMission.RANGE_WIDTH * m,
					LibraryMission.RANGE_HEIGHT * n + TOPADING_SIZE);
		}
		refreshThread.update();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!refreshThread.isAlive()) {
			refreshThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		refreshThread.flag = false;
		refreshThread = null;
	}
}
