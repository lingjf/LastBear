package com.lastbear;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TangramDone extends View {

	private static final int CONTENT_WIDTH = 400;
	private static final int CONTENT_HEIGHT = 300;
	private static final int LIBRARY_WIDTH = 100;
	private static final int LIBRARY_HEIGHT = 100;
	private static final int REPLAY_WIDTH = 100;
	private static final int REPLAY_HEIGHT = 100;
	private static final int FORWARD_WIDTH = 100;
	private static final int FORWARD_HEIGHT = 100;

	public interface OnListener {
		void onListLibrary();

		void onMissionContinue();

		void onMissionForward();

		void onDonate();
	}

	private OnListener onListener = null;
	private LibraryMission libraryMission = null;
	private boolean isopen = false;
	private RectF contentRect = new RectF();
	private RectF libraryRect = new RectF();
	private RectF replayRect = new RectF();
	private RectF forwardRect = new RectF();
	private Path libraryClip = new Path();
	private Path replayClip = new Path();
	private Path forwardClip = new Path();

	public TangramDone(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TangramDone(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TangramDone(Context context) {
		super(context);
	}

	public void initialize(LibraryMission libraryMission, OnListener listener) {
		this.libraryMission = libraryMission;
		this.onListener = listener;
		close();
	}

	public void open() {
		isopen = true;
		setVisibility(VISIBLE);
		bringToFront();
	}

	public void close() {
		isopen = false;
		setVisibility(INVISIBLE);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		contentRect.set(w / 2 - CONTENT_WIDTH / 2, h / 2 - CONTENT_HEIGHT / 2,
				w / 2 + CONTENT_WIDTH / 2, h / 2 + CONTENT_HEIGHT / 2);

		libraryRect.set(contentRect.left + 60, contentRect.bottom,
				contentRect.left + 60, contentRect.bottom);
		libraryRect.inset(-LIBRARY_WIDTH / 2, -LIBRARY_HEIGHT / 2);

		replayRect.set(contentRect.centerX(), contentRect.bottom,
				contentRect.centerX(), contentRect.bottom);
		replayRect.inset(-REPLAY_WIDTH / 2, -REPLAY_HEIGHT / 2);

		forwardRect.set(contentRect.right - 60, contentRect.bottom,
				contentRect.right - 60, contentRect.bottom);
		forwardRect.inset(-FORWARD_WIDTH / 2, -FORWARD_HEIGHT / 2);

		libraryClip.reset();
		libraryClip.addCircle(libraryRect.centerX(), libraryRect.centerY(),
				libraryRect.width() / 2, Path.Direction.CCW);

		replayClip.reset();
		replayClip.addCircle(replayRect.centerX(), replayRect.centerY(),
				replayRect.width() / 2, Path.Direction.CCW);

		forwardClip.reset();
		forwardClip.addCircle(forwardRect.centerX(), forwardRect.centerY(),
				forwardRect.width() / 2, Path.Direction.CCW);
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (!isopen)
			return false;

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_CANCEL:
			break;
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			if (libraryRect.contains(event.getX(), event.getY())) {
				if (onListener != null)
					onListener.onListLibrary();
				return true;
			}
			if (replayRect.contains(event.getX(), event.getY())) {
				if (onListener != null)
					onListener.onMissionContinue();
				return true;
			}
			if (forwardRect.contains(event.getX(), event.getY())) {
				if (onListener != null)
					onListener.onMissionForward();
				return true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			break;
		default:
			break;
		}
		return true;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!isopen)
			return;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(24);
		paint.setTypeface(Toolbox.getInstance().getTypeface());
		paint.setAntiAlias(true);

		paint.setColor(Color.argb(160, 156, 162, 166));
		canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setTextAlign(Paint.Align.CENTER);

		Rect bounds = new Rect();
		paint.getTextBounds("M012", 0, 1, bounds);

		canvas.drawRoundRect(contentRect, 10, 10, paint);
		canvas.drawText("Congratulations", contentRect.centerX(),
				contentRect.top + bounds.height(), paint);
		String tips = "Mission " + libraryMission.getGroup().getName() + "/"
				+ libraryMission.getMission().getName() + " Completed";
		canvas.drawText(tips, contentRect.centerX(), contentRect.centerY()
				+ bounds.height() / 2, paint);

		// draw library button
		canvas.save();
		canvas.clipPath(libraryClip);
		canvas.drawText("Library", libraryRect.centerX(), libraryRect.centerY()
				- paint.getFontMetrics().ascent / 2, paint);
		canvas.drawBitmap(
				Toolbox.getInstance().getBitmap(R.drawable.done_library),
				libraryRect.left, libraryRect.top, paint);
		canvas.restore();

		// draw replay button
		canvas.save();
		canvas.clipPath(replayClip);
		canvas.drawText("Replay", replayRect.centerX(), replayRect.centerY()
				- paint.getFontMetrics().ascent / 2, paint);
		canvas.drawBitmap(
				Toolbox.getInstance().getBitmap(R.drawable.done_replay),
				replayRect.left, replayRect.top, paint);
		canvas.restore();

		// draw forward button
		canvas.save();
		canvas.clipPath(forwardClip);
		canvas.drawText("Forward", forwardRect.centerX(), forwardRect.centerY()
				- paint.getFontMetrics().ascent / 2, paint);
		canvas.drawBitmap(
				Toolbox.getInstance().getBitmap(R.drawable.done_forward),
				forwardRect.left, forwardRect.top, paint);
		canvas.restore();
	}

}
