package com.shared;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GIFView extends View implements Runnable {
	public GIFView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GIFView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private Bitmap bitmap = null;
	private GIFDecode decode = null;
	private int gifCount;

	public GIFView(android.content.Context context) {
		super(context);
	}

	public void setGifImage(int rsid) {
		decode = new GIFDecode();
		decode.read(this.getResources().openRawResource(rsid));
		gifCount = decode.getFrameCount();
		bitmap = decode.getFrame(0);
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (bitmap != null) {
			canvas.drawBitmap(bitmap, 0, 0, new Paint());
			bitmap = decode.next();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.postInvalidate();
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}

	}

}
