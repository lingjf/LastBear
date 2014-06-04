package com.lastbear;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TangramPlay extends View {

	public interface OnListener {
		void onMissionCompleted();
	}

	private OnListener onListener = null;
	private Mission mission = null;
	private ArrayList<Piece> pieces = null;
	private Piece focusPiece = null;

	private Bitmap background = null;

	public TangramPlay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TangramPlay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TangramPlay(Context context) {
		super(context);
	}

	public void initialize(Mission mission, OnListener listener) {
		this.mission = mission;
		this.onListener = listener;

		background = Toolbox.getInstance().getBitmap(
				R.drawable.background_tangram);
	}

	private ArrayList<Piece> getPieces() {
		if (pieces == null) {
			pieces = new ArrayList<Piece>();
			mission.deploy();
			for (int i = 0; i < mission.pieces.size(); i++) {
				Polygon p = mission.pieces.get(i);
				pieces.add(new Piece(Toolbox.COLORS[i], mission, p));
			}
		}
		return pieces;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x, y;
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			x = event.getX(event.getActionIndex());
			y = event.getY(event.getActionIndex());
			for (int i = 0; i < getPieces().size(); i++) {
				Piece piece = getPieces().get(i);
				if (piece.onDragStart(x, y)) {
					getPieces().remove(piece);
					getPieces().add(0, piece);
					focusPiece = piece;
					return true;
				}
			}
			if (focusPiece != null) {
				if (!focusPiece.onTrunStart(x, y)) {
					focusPiece.state = Piece.NONE;
					focusPiece = null;
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			x = event.getX(event.getActionIndex());
			y = event.getY(event.getActionIndex());
			if (focusPiece != null) {
				focusPiece.onNingStart(x, y);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (focusPiece != null) {
				if (focusPiece.state == Piece.DRAG) {
					focusPiece.onDrag(event.getX(), event.getY());
					invalidate();
				} else if (focusPiece.state == Piece.TRUN) {
					focusPiece.onTrun(event.getX(), event.getY());
					invalidate();
				} else if (focusPiece.state == Piece.NING) {
					for (int i = 0; i < event.getPointerCount(); i++) {
						focusPiece.onNing(event.getPointerId(i), event.getX(i),
								event.getY(i));
					}
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			if (focusPiece != null) {
				focusPiece.onStop(event.getPointerId(event.getActionIndex()),
						event.getX(event.getActionIndex()),
						event.getY(event.getActionIndex()));
				invalidate();
				if (mission.completed()) {
					if (onListener != null)
						onListener.onMissionCompleted();
				}
			}
			break;
		default:
			// System.out.println("TangramPlay: onTouchEvent : ");
			break;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawBitmap(background, 0, 0, null);

		for (int i = getPieces().size() - 1; i >= 0; i--) {
			Piece piece = getPieces().get(i);
			piece.draw(canvas);
		}
		if (mission != null) {
			mission.draw(canvas);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// System.out.println("TangramPlay: onFinishInflate");
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// System.out.println("TangramPlay: onLayout");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// System.out.println("TangramPlay: onMeasure");

		// int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		// int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

}
