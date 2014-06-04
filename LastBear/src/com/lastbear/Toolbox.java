package com.lastbear;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.view.MotionEvent;

public class Toolbox {

	private static Toolbox toolbox = null;

	public static Toolbox getInstance() {
		return toolbox;
	}

	public static Toolbox getInstance(Context context) {
		if (toolbox == null) {
			toolbox = new Toolbox(context);
		}
		return toolbox;
	}

	public static final int[] COLORS = new int[] { Color.rgb(41, 117, 222),
			Color.rgb(213, 161, 49), Color.rgb(41, 186, 205),
			Color.rgb(156, 117, 222), Color.rgb(16, 190, 123),
			Color.rgb(238, 101, 148), Color.rgb(0x00, 0x88, 0x00),
			Color.rgb(0xCC, 0x77, 0x4), Color.rgb(0x22, 0x77, 0xCC),
			Color.rgb(0x22, 0x77, 0x44), Color.rgb(0x55, 0x44, 0x88),
			Color.rgb(0x51, 0xC4, 0xC8), Color.rgb(247, 150, 107) };

	private Context context = null;
	private Typeface typeface = null;
	private MediaPlayer backsound = null;
	private MediaPlayer attachSoundEffect = null;

	private HashMap<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>();

	private Toolbox(Context context) {
		this.context = context;
	}

	public Typeface getTypeface() {
		String filename = "SHOWG.TTF";
		if (typeface == null) {
			typeface = Typeface.createFromAsset(context.getAssets(), filename);
		}
		return typeface;
	}

	public MediaPlayer getBacksound() {
		if (backsound == null) {
			backsound = MediaPlayer.create(context, R.raw.musik);
			backsound.setLooping(true);
		}
		return backsound;
	}

	public MediaPlayer getAttachSoundEffect() {
		if (attachSoundEffect == null) {
			attachSoundEffect = MediaPlayer.create(context,
					R.raw.sound_effect_attach);
		}
		return attachSoundEffect;
	}

	public Bitmap getBitmap(int rsid) {
		Integer t = new Integer(rsid);
		if (!bitmaps.containsKey(t)) {
			Bitmap b = BitmapFactory.decodeStream(context.getResources()
					.openRawResource(rsid));
			bitmaps.put(t, b);
		}
		return bitmaps.get(t);
	}

	public static String toString(MotionEvent event) {
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
}
