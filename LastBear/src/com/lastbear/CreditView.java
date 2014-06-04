package com.lastbear;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CreditView extends SurfaceView implements SurfaceHolder.Callback {

	private String fps = "";
	private Movie movie;
	private long movieStart;

	private SurfaceHolder surfaceHolder = null;
	private RefreshThread refreshThread = null;

	public CreditView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initailze(context);
	}

	public CreditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initailze(context);
	}

	public CreditView(Context context) {
		super(context);
		initailze(context);
	}

	private void initailze(Context context) {
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		refreshThread = new RefreshThread();

		InputStream is;
		is = context.getResources().openRawResource(R.drawable.river);
		// is = context.getResources().openRawResource(R.drawable.salto);
		movie = Movie.decodeStream(is);
	}

	public RefreshThread getRefreshThread() {
		return refreshThread;
	}

	protected void doDraw(Canvas canvas) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTypeface(Toolbox.getInstance().getTypeface());
		paint.setAntiAlias(true);
		paint.setTextSize(20);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		// mainPaint.setStrokeWidth(1);
		// mainPaint.setAlpha(0);

		canvas.drawBitmap(
				Toolbox.getInstance().getBitmap(R.drawable.background_tangram),
				0, 0, null);

		canvas.drawText("lingjf@gmail.com", 100, 50, paint);

		canvas.drawText(fps, 100, 600, paint);

		canvas.drawBitmap(Toolbox.getInstance().getBitmap(R.drawable.ball),
				100, 150, paint);

		canvas.drawBitmap(Toolbox.getInstance().getBitmap(R.drawable.salto),
				100, 250, paint);

		// movie.draw(canvas, 200, 420);

		RectF rect = new RectF(400, 10, 430, 40);
		for (int i = 0; i < Toolbox.COLORS.length; i++) {
			paint.setColor(Toolbox.COLORS[i]);
			canvas.drawRect(rect, paint);
			canvas.drawText("" + (i + 1), rect.right + 8, rect.bottom, paint);
			rect.offset(0, 35);
		}
	}

	public class RefreshThread extends Thread {
		public static final int STATE_RUNING = 0;
		public static final int STATE_PAUSED = 1;

		private long count = 0;
		private long start = 0;
		private long span = 50;
		private boolean running = true;
		private int state = STATE_RUNING;

		public void setRunning(boolean b) {
			running = b;
		}

		public void setState(int state) {
			synchronized (surfaceHolder) {
				this.state = state;
			}
		}

		@Override
		public void run() {
			Canvas canvas = null;
			while (running) {
				try {
					canvas = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						CreditView.this.doDraw(canvas);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (canvas != null) {
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}

				count++;
				if (count == 20) {
					count = 0;
					long tempStamp = System.nanoTime();
					long spank = tempStamp - start;
					start = tempStamp;
					double fps = Math.round(100000000000.0 / spank * 20) / 100.0;
					CreditView.this.fps = "FPS:" + fps;
				}

				try {
					Thread.sleep(span);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (!hasWindowFocus)
			refreshThread.setState(RefreshThread.STATE_PAUSED);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created
		refreshThread.setRunning(true);
		if (!refreshThread.isAlive())
			refreshThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		boolean retry = true;
		refreshThread.setRunning(false);
		while (retry) {
			try {
				refreshThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}
}
