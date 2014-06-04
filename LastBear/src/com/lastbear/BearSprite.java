package com.lastbear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

public class BearSprite extends View {
	private AnimationDrawable frameAnimation = null;
	public AnimationSet animationSet = null;

	public BearSprite(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public BearSprite(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BearSprite(Context context) {
		super(context);
	}

	public void initailze() {
		int width = 256;
		int w = 32;
		int h = 64;

		frameAnimation = new AnimationDrawable();
		Bitmap salto = Toolbox.getInstance().getBitmap(R.drawable.salto);
		for (int i = 0; i < width / w; i++) {
			Bitmap bmp = Bitmap.createBitmap(salto, w * i, 0, w, h);
			BitmapDrawable bd = new BitmapDrawable(bmp);
			frameAnimation.addFrame(bd, 100);
		}
		frameAnimation.setOneShot(false);

		setBackgroundDrawable(frameAnimation);

		animationSet = new AnimationSet(true);

		TranslateAnimation ta = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT,
				0.7f, Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_PARENT, 0.7f);

		RotateAnimation ra = new RotateAnimation(0, 180,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		ScaleAnimation sa = new ScaleAnimation(1, 5, 1, 5,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);

		FloatAnimation fa = new FloatAnimation(0, 400);
		// animationSet.addAnimation(ta);
		// animationSet.addAnimation(ra);
		// animationSet.addAnimation(sa);
		animationSet.addAnimation(fa);

		animationSet.setInterpolator(new AccelerateInterpolator());
		animationSet.setDuration(5000);
		animationSet.setFillAfter(false);
		animationSet.setRepeatMode(Animation.RESTART);
		animationSet.setRepeatMode(Animation.REVERSE);
		animationSet.setRepeatCount(9);
		ta.setRepeatCount(Animation.INFINITE);
		ra.setRepeatCount(Animation.INFINITE);
		sa.setRepeatCount(Animation.INFINITE);
		fa.setRepeatCount(Animation.INFINITE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Paint paint = new Paint();
		// paint.setStyle(Paint.Style.STROKE);
		// paint.setColor(Color.BLACK);
		// canvas.drawRect(0, 0, this.getWidth(),this.getHeight(), paint);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		frameAnimation.start();
		this.startAnimation(animationSet);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(32, 64);
	}

	private class FloatAnimation extends Animation {
		private final float fromDepthZ;
		private final float toDepthZ;
		private Camera mCamera;

		public FloatAnimation(float fromDepthZ, float toDepthZ) {
			this.fromDepthZ = fromDepthZ;
			this.toDepthZ = toDepthZ;
		}

		@Override
		public void initialize(int width, int height, int parentWidth,
				int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			mCamera = new Camera();
			// mCamera.setLocation(0.0f, 0.0f, 0.0f);
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			float depth = fromDepthZ
					+ ((toDepthZ - fromDepthZ) * interpolatedTime);

			final Camera camera = mCamera;
			final Matrix matrix = t.getMatrix();
			camera.save();
			camera.translate(0.0f, 0.0f, depth);
			camera.getMatrix(matrix);
			camera.restore();
			matrix.preTranslate(200, 200);
			// matrix.postTranslate(centerX, centerY);
		}
	}
}
