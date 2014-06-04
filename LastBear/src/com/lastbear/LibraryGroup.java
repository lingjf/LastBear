package com.lastbear;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;

public class LibraryGroup {
	private static boolean debug_identify = true;

	public static final int RAWS = 4;
	private static final int THUMBNAIL_W = 180;
	private static final int THUMBNAIL_H = 180;

	private Context context;
	private String name = null;
	private String id = null;
	private RectF rect = new RectF();
	private Path path = new Path();

	private Bitmap background = null;
	private Bitmap thumbnail = null;
	private ArrayList<LibraryMission> missions = null;

	public LibraryGroup(Context context, String id, String name) {
		super();
		this.context = context;
		this.id = id;
		this.name = name;

		background = Toolbox.getInstance().getBitmap(
				R.drawable.background_group_tile);
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public void setRect(RectF rect) {
		this.rect.set(rect);
		path.reset();
		path.addRoundRect(rect, 17, 17, Path.Direction.CCW);
	}

	public RectF getRect() {
		return rect;
	}

	public void setThumbnail(ArrayList<Polygon> polygons) {
		RectF surround = new RectF();
		for (Polygon p : polygons) {
			surround.union(p.getRect());
		}
		float scale;
		if (surround.width() / surround.height() > THUMBNAIL_W / THUMBNAIL_H) {
			scale = THUMBNAIL_W / surround.width();
		} else {
			scale = THUMBNAIL_H / surround.height();
		}

		Matrix matrix = new Matrix();
		matrix.setTranslate(THUMBNAIL_W / 2 - surround.centerX(), THUMBNAIL_H
				/ 2 - surround.centerY());
		matrix.postScale(scale, scale, THUMBNAIL_W / 2, THUMBNAIL_H / 2);
		thumbnail = Bitmap.createBitmap(THUMBNAIL_W, THUMBNAIL_H,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(thumbnail);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);

		for (int i = 0; i < polygons.size(); i++) {
			paint.setColor(Toolbox.COLORS[i]);
			Polygon p = polygons.get(i);
			p.transform(matrix);
			canvas.drawPath(p.getRecentPath(), paint);
		}
	}

	public void setThumbnail4(ArrayList<Polygon> polygons) {
		Matrix matrix = new Matrix();
		RectF surround = new RectF();

		for (Polygon p : polygons) {
			surround.union(p.getRect());
		}

		for (Polygon p : polygons) {
			RectF rect = p.getRect();
			matrix.reset();
			matrix.setTranslate((rect.centerX() - surround.centerX()) * 0.5f,
					(rect.centerY() - surround.centerY()) * 0.5f);
			p.transform(matrix);
		}

		for (Polygon p : polygons) {
			surround.union(p.getRect());
		}
		float scale;
		if (surround.width() / surround.height() > THUMBNAIL_W / THUMBNAIL_H) {
			scale = THUMBNAIL_W / surround.width();
		} else {
			scale = THUMBNAIL_H / surround.height();
		}

		matrix.reset();
		matrix.setTranslate(THUMBNAIL_W / 2 - surround.centerX(), THUMBNAIL_H
				/ 2 - surround.centerX());
		matrix.postScale(scale, scale, THUMBNAIL_W / 2, THUMBNAIL_H / 2);
		thumbnail = Bitmap.createBitmap(THUMBNAIL_W, THUMBNAIL_H,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(thumbnail);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);

		for (int i = 0; i < polygons.size(); i++) {
			paint.setColor(Toolbox.COLORS[i]);
			Polygon p = polygons.get(i);
			p.transform(matrix);
			canvas.drawPath(p.getRecentPath(), paint);
		}
	}

	public void resetMissions() {
		if (missions != null) {
			for (LibraryMission libraryMission : missions) {
				libraryMission.reset();
			}
		}
	}

	public ArrayList<LibraryMission> getMissions() {
		if (missions == null) {
			missions = new ArrayList<LibraryMission>();
			try {
				String fileName = "lastbear_g" + id + ".xml";
				InputStream is = context.getAssets().open(fileName);
				MissionBuilder missionBuilder = new MissionBuilder(is);
				for (int i = 0; i < missionBuilder.getMissions().size(); i++) {
					Mission mission = missionBuilder.getMissions().get(i);
					mission.relocate(480, 800);
					missions.add(new LibraryMission(i, this, mission));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return missions;
	}

	public LibraryMission getMission(int missionIndex) {
		if (missionIndex < 0 || getMissions().size() <= missionIndex) {
			return null;
		} else {
			return getMissions().get(missionIndex);
		}
	}

	public void draw(Canvas canvas, Paint paint) {
		canvas.save();
		canvas.clipPath(path);
		canvas.drawBitmap(background, rect.left, rect.top, paint);
		canvas.restore();
		paint.setColor(Color.rgb(0x22, 0x77, 0xCC));
		canvas.drawText(name, rect.centerX(), rect.top + 40, paint);

		if (thumbnail != null) {
			canvas.drawBitmap(thumbnail, rect.centerX() - THUMBNAIL_W / 2,
					rect.centerY() + 26 - THUMBNAIL_H / 2, paint);
		}
		if (debug_identify) {
			Paint debug_paint = new Paint();
			debug_paint.setTextAlign(Paint.Align.CENTER);
			debug_paint.setTextSize(48);
			debug_paint.setColor(Color.RED);
			canvas.drawText(id, rect.centerX(), rect.top + 48, debug_paint);
		}
	}
}
