package com.lastbear;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Assistant extends View {

	private static final int MENU_PADING = 10;
	private static final int MENU_WIDTH = 80;
	private static final int MENU_HEIGHT = 80;

	private static final int ACTION_PADING = 3;
	private static final int ACTION_WIDTH = 70;
	private static final int ACTION_HEIGHT = 70;

	public static final int ACTION_CLEAR = 1;
	public static final int ACTION_SOUND = 2;
	public static final int ACTION_CREDITS = 3;
	public static final int ACTION_SOLVE = 4;
	public static final int ACTION_BACKWARD = 5;
	public static final int ACTION_COACH = 6;
	public static final int ACTION_FINISH = 7;

	public interface OnListener {
		void onAction(int action);
	}

	private class Action {
		public int id;
		public String name;
		public RectF rect = new RectF();
		public Bitmap bitmap = null;
		public float angle;

		public Action(int id, String name, Bitmap bitmap) {
			this.id = id;
			this.name = name;
			this.bitmap = bitmap;
		}

		public boolean contains(float x, float y) {
			if (!rect.contains(x, y))
				return false;
			if (Coordinate.distance(x, y, rect.centerX(), rect.centerY()) < ACTION_WIDTH)
				return true;
			else
				return false;
		}
	}

	private Paint mainPaint = null;
	private OnListener onListener = null;

	private String position;
	private float startAngle = 0;
	private float sweepAngle = 0;
	private float averageAngle = 0;
	private float deltaAngle = 0;
	private boolean isopen = false;
	private RectF menuRect = new RectF();
	private RectF fanRect = new RectF();

	private ArrayList<Action> actions = new ArrayList<Action>();
	private float distance;
	private float fanRadius;

	private Path path = new Path();
	private Matrix matrix = new Matrix();

	public Assistant(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Assistant(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Assistant(Context context) {
		super(context);
	}

	public void initialize(String position, float startAngle, float sweepAngle,
			OnListener listener) {
		// System.out.println("Assistant: initialize");
		this.position = position;
		this.startAngle = startAngle;
		this.sweepAngle = sweepAngle;
		this.onListener = listener;
		this.averageAngle = sweepAngle;
	}

	public void addAction(int id, String name, Bitmap bitmap) {
		actions.add(new Action(id, name, bitmap));
		averageAngle = sweepAngle / actions.size();
		for (int i = 0; i < actions.size(); i++) {
			actions.get(i).angle = startAngle + averageAngle * (i + 0.5f);
		}
		distance = (ACTION_WIDTH / 2 + ACTION_PADING)
				/ (float) Math.abs(Math.sin(Math.toRadians(averageAngle / 2)));
		fanRadius = distance + ACTION_WIDTH / 2 + ACTION_PADING * 2;
	}

	public void setAction(int id, String name, Bitmap bitmap) {
		for (int i = 0; i < actions.size(); i++) {
			if (actions.get(i).id == id) {
				if (name != null) {
					actions.get(i).name = name;
				}
				if (bitmap != null) {
					actions.get(i).bitmap = bitmap;
					redrawHandler.update();
				}
				return;
			}
		}
	}

	public boolean isOpen() {
		return isopen;
	}

	public void open(boolean quick) {
		if (!isopen) {
			isopen = true;
			if (quick) {
				deltaAngle = sweepAngle;
			}
			redrawHandler.update();
		}
	}

	public void close(boolean quick) {
		if (isopen) {
			isopen = false;
			if (quick) {
				deltaAngle = 0;
			}
			redrawHandler.update();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_CANCEL:
			break;
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			if (menuRect.contains(event.getX(), event.getY())) {
				if (isOpen()) {
					close(false);
				} else {
					open(false);
				}
				return true;
			}
			if (isopen) {
				for (int i = 0; i < actions.size(); i++) {
					Action action = actions.get(i);
					if (action.contains(event.getX(), event.getY())) {
						if (onListener != null) {
							onListener.onAction(action.id);
						}
						redrawHandler.update();
						return true;
					}
				}
			}
			close(false);
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// System.out.println("Assistant: onSizeChanged: " + w + ", " + h);

		if (position.equalsIgnoreCase("left top")) {
			menuRect.set(MENU_PADING, MENU_PADING, MENU_PADING + MENU_WIDTH,
					MENU_PADING + MENU_HEIGHT);
		} else if (position.equalsIgnoreCase("left bottom")) {
			menuRect.set(MENU_PADING, h - MENU_PADING - MENU_HEIGHT,
					MENU_PADING + MENU_WIDTH, h - MENU_PADING);
		} else if (position.equalsIgnoreCase("right top")) {
			menuRect.set(w - MENU_PADING - MENU_WIDTH, MENU_PADING, w
					- MENU_PADING, MENU_PADING + MENU_HEIGHT);
		} else if (position.equalsIgnoreCase("right bottom")) {
			menuRect.set(w - MENU_PADING - MENU_WIDTH, h - MENU_PADING
					- MENU_HEIGHT, w - MENU_PADING, h - MENU_PADING);
		} else {
			menuRect.set(w / 2 - MENU_WIDTH / 2, h / 2 - MENU_HEIGHT / 2, w / 2
					+ MENU_WIDTH / 2, h / 2 + MENU_HEIGHT / 2);
		}

		fanRect.set(menuRect.centerX() - fanRadius, menuRect.centerY()
				- fanRadius, menuRect.centerX() + fanRadius, menuRect.centerY()
				+ fanRadius);
		for (Action action : actions) {
			action.rect.set(menuRect.centerX() - ACTION_WIDTH / 2,
					menuRect.centerY() - ACTION_HEIGHT / 2, menuRect.centerX()
							+ ACTION_WIDTH / 2, menuRect.centerY()
							+ ACTION_HEIGHT / 2);
			action.rect.offset((float) Math.cos(Math.toRadians(action.angle))
					* distance, (float) Math.sin(Math.toRadians(action.angle))
					* distance);
		}
	}

	/**
	 * Create a simple handler that we can use to cause animation to happen.
	 */
	private RefreshHandler redrawHandler = new RefreshHandler();

	private class RefreshHandler extends Handler {
		private static final long INTERVAL = 10;
		private static final long ACCELERATE = 3;
		private static final int WHAT_UPDATE = 0;

		private float velocity = 0;
		private long timestamp = 0;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_UPDATE:
				float seconds = (System.currentTimeMillis() - timestamp) / 1000.0f;
				timestamp = System.currentTimeMillis();
				if (isopen) {
					deltaAngle += velocity * seconds;
					velocity *= ACCELERATE * seconds + 1;
					if (sweepAngle < 0) {
						if (deltaAngle > 0) {
							deltaAngle = 0;
						} else if (deltaAngle < sweepAngle) {
							deltaAngle = sweepAngle;
						}
					} else {
						if (deltaAngle > sweepAngle) {
							deltaAngle = sweepAngle;
						} else if (deltaAngle < 0) {
							deltaAngle = 0;
						}
					}

					if (deltaAngle != sweepAngle) {
						sendEmptyMessageDelayed(WHAT_UPDATE, INTERVAL);
					}
				} else {
					deltaAngle -= velocity * seconds;
					velocity *= ACCELERATE * seconds + 1;
					if (sweepAngle < 0) {
						if (deltaAngle > 0) {
							deltaAngle = 0;
						} else if (deltaAngle < sweepAngle) {
							deltaAngle = sweepAngle;
						}
					} else {
						if (deltaAngle > sweepAngle) {
							deltaAngle = sweepAngle;
						} else if (deltaAngle < 0) {
							deltaAngle = 0;
						}
					}

					if (deltaAngle != 0) {
						sendEmptyMessageDelayed(WHAT_UPDATE, INTERVAL);
					}
				}
				break;
			}
			Assistant.this.invalidate();
		}

		public void update() {
			if (hasMessages(WHAT_UPDATE)) {
				return;
			} else {
				velocity = 88 * (sweepAngle < 0 ? -1 : 1);
				timestamp = System.currentTimeMillis();
				// sendMessageDelayed(obtainMessage(WHAT_UPDATE), INTERVAL);
				sendEmptyMessageDelayed(WHAT_UPDATE, INTERVAL);
			}
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// System.out.println("Assistant: onDraw");

		if (mainPaint == null) {
			mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mainPaint.setTypeface(Toolbox.getInstance().getTypeface());
			mainPaint.setAntiAlias(true);
			mainPaint.setTextSize(20);
			mainPaint.setTextAlign(Paint.Align.CENTER);
			// mainPaint.setStrokeWidth(1);
			// mainPaint.setColor(Color.WHITE);
			// mainPaint.setAlpha(0);
		}

		path.reset();
		path.moveTo(fanRect.centerX(), fanRect.centerY());
		path.arcTo(fanRect, startAngle, deltaAngle);
		path.close();

		mainPaint.setStyle(Paint.Style.FILL);
		mainPaint.setColor(Color.argb(160, 156, 162, 166));
		canvas.drawPath(path, mainPaint);

		canvas.save();
		canvas.clipPath(path);
		mainPaint.setStyle(Paint.Style.STROKE);
		mainPaint.setColor(Color.WHITE);
		for (Action action : actions) {
			if (action.bitmap != null) {
				canvas.drawBitmap(action.bitmap, action.rect.left,
						action.rect.top, mainPaint);
				canvas.drawCircle(action.rect.centerX(), action.rect.centerY(),
						action.rect.width() / 2, mainPaint);
			} else {
				canvas.drawCircle(action.rect.centerX(), action.rect.centerY(),
						action.rect.width() / 2, mainPaint);
				canvas.drawText(action.name, action.rect.centerX(),
						action.rect.centerY()
								- mainPaint.getFontMetrics().ascent / 2,
						mainPaint);
			}
		}
		canvas.restore();

		// draw menu button
		canvas.drawText("menu", menuRect.centerX(), menuRect.centerY()
				- mainPaint.getFontMetrics().ascent / 2, mainPaint);
		Bitmap bitmap = Toolbox.getInstance().getBitmap(R.drawable.menu_root);
		matrix.reset();
		matrix.setTranslate(menuRect.centerX() - bitmap.getWidth() / 2,
				menuRect.centerY() - bitmap.getHeight() / 2);
		matrix.postRotate(deltaAngle, menuRect.centerX(), menuRect.centerY());
		canvas.drawBitmap(bitmap, matrix, mainPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// System.out.println("Assistant: onMeasure");
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
				MeasureSpec.getSize(heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// System.out.println("Assistant: onLayout");
	}
}
