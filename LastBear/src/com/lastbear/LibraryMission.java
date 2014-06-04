package com.lastbear;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class LibraryMission {
	private static boolean debug_rectangle = false;
	private static boolean debug_name = false;

	public static final int PADING_SIZE = 5;
	public static final int RANGE_WIDTH = 120;
	public static final int RANGE_HEIGHT = 150;
	public static final int UNDER_WIDTH = 104;
	public static final int UNDER_HEIGHT = 116;
	public static final int TILE_WIDTH = 100;
	public static final int TILE_HEIGHT = 100;
	public static final int TEXT_WIDTH = 100;
	public static final int TEXT_HEIGHT = 35;

	private LibraryGroup group = null;
	private int index;
	private Integer score = null;
	private Mission mission = null;
	private Bitmap thumbnail = null;

	private RectF range = new RectF(0, 0, RANGE_WIDTH, RANGE_HEIGHT);

	private RectF underRect = new RectF((RANGE_WIDTH - TILE_WIDTH) / 2,
			RANGE_HEIGHT - PADING_SIZE - UNDER_HEIGHT,
			(RANGE_WIDTH + TILE_WIDTH) / 2, RANGE_HEIGHT - PADING_SIZE);
	private RectF tileRect = new RectF((RANGE_WIDTH - TILE_WIDTH) / 2,
			PADING_SIZE, (RANGE_WIDTH + TILE_WIDTH) / 2, PADING_SIZE
					+ TILE_HEIGHT);
	private RectF tileEdge = new RectF((RANGE_WIDTH - TILE_WIDTH) / 2 - 1,
			PADING_SIZE - 1, (RANGE_WIDTH + TILE_WIDTH) / 2 + 1, PADING_SIZE
					+ TILE_HEIGHT + 1);
	private RectF textRect = new RectF((RANGE_WIDTH - TEXT_WIDTH) / 2,
			RANGE_HEIGHT - PADING_SIZE - TEXT_HEIGHT,
			(RANGE_WIDTH + TEXT_WIDTH) / 2, RANGE_HEIGHT - PADING_SIZE);

	private Path tileClip = new Path();

	public LibraryMission(int index, LibraryGroup group, Mission mission) {
		super();
		this.index = index;
		this.group = group;
		this.mission = mission;
		tileClip.addRoundRect(tileRect, 10, 10, Path.Direction.CCW);
	}

	public void reset() {
		// mission = null;
		// thumbnail = null;
		// score = null;
	}

	public Mission getMission() {
		return mission;
	}

	public int getIndex() {
		return index;
	}

	public LibraryGroup getGroup() {
		return group;
	}

	public int getScore() {
		if (score == null) {
			ScoreBuilder sb = ScoreBuilder.getInstance();
			score = sb.getScore(group.getId(), "" + index);
		}
		return score.intValue();
	}

	public void setScore(int score) {
		this.score = new Integer(score);
		ScoreBuilder sb = ScoreBuilder.getInstance();
		sb.setScore(group.getId(), "" + index, this.score);
		this.thumbnail = null;
	}

	public RectF getRange() {
		return range;
	}

	public void setRange(float left, float top) {
		range.set(left, top, left + RANGE_WIDTH, top + RANGE_HEIGHT);
	}

	public Bitmap getThumbnail() {
		if (thumbnail == null) {
			if (mission != null) {
				thumbnail = mission.thumbnail(getScore() > 0);
			}
		}
		return thumbnail;
	}

	public void draw(Canvas canvas, RectF insight, Paint paint) {
		if (!RectF.intersects(insight, range))
			return;

		paint.setColor(Color.argb(150, 20, 20, 20));
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRoundRect(underRect, 15, 15, paint);

		canvas.save();
		canvas.clipPath(tileClip);
		paint.setColor(Color.argb(160, 204, 255, 255));
		canvas.drawRect(tileRect, paint);
		paint.setColor(Color.WHITE);
		canvas.drawBitmap(getThumbnail(), tileRect.left, tileRect.top, paint);
		canvas.restore();

		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(Color.rgb(90, 90, 90));
		canvas.drawRoundRect(tileRect, 10, 10, paint);
		paint.setColor(Color.rgb(255, 197, 72));
		canvas.drawRoundRect(tileEdge, 12, 12, paint);

		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(0);
		paint.setColor(Color.WHITE);
		canvas.drawText("" + (getIndex() + 1), textRect.centerX(),
				textRect.centerY() - paint.getFontMetrics().ascent / 2, paint);

		if (debug_name) {
			Paint debug_paint = new Paint();
			debug_paint.setTextAlign(Paint.Align.CENTER);
			debug_paint.setTextSize(24);
			debug_paint.setColor(Color.RED);
			canvas.drawText(mission.getName(), textRect.centerX(),
					textRect.centerY(), debug_paint);
		}
		if (debug_rectangle) {
			RectF t = new RectF(0, 0, RANGE_WIDTH, RANGE_HEIGHT);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(t, 5, 5, paint);
			canvas.drawRoundRect(textRect, 10, 10, paint);
		}
	}
}
