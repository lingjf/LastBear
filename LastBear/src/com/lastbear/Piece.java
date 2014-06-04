package com.lastbear;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

public class Piece {
	private static boolean debug_type = true;
	private static boolean debug_axis = false;
	private static boolean debug_circle = false;
	private static boolean debug_attach = true;

	private static boolean ATTACHED_TRUN_ENABLED = false;
	private static boolean CORNER_TRUN_ENABLED = false;

	public Polygon shape = null;
	private Mission mission = null;
	public int color;

	public static final int NONE = 0;
	public static final int HEAD = 1;
	public static final int DRAG = 2;
	public static final int TRUN = 3;
	public static final int NING = 4;
	public int state = NONE;
	private Coordinate dragPoint = new Coordinate(0, 0);
	private Coordinate trunPoint = new Coordinate(0, 0);
	private Coordinate origPoint = new Coordinate(0, 0);
	private Coordinate ningPoint = new Coordinate(0, 0);
	private boolean attached = false;

	public Coordinate[] suggestNode = new Coordinate[] { null, null };
	public Line[] suggestEdge = new Line[] { null, null };
	public Matrix suggestMatrix = new Matrix();
	public boolean attachable = false;

	private Paint mainPaint = null;
	private Paint suggestPaint = null;

	public Piece(int color, Mission mission, Polygon polygon) {
		super();
		this.color = color;
		this.mission = mission;
		this.shape = polygon;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isAttached() {
		return attached;
	}

	public boolean onDragStart(float x, float y) {
		if (!shape.isInside(x, y)) {
			return false;
		}
		state = Piece.DRAG;
		dragPoint.set(x, y);
		attached = mission.dettach(this);
		return true;
	}

	public boolean onTrunStart(float x, float y) {
		if (!shape.inShadow(x, y)) {
			return false;
		}
		if (!ATTACHED_TRUN_ENABLED) {
			if (attached)
				return false;
		}
		state = Piece.TRUN;
		trunPoint.set(x, y);
		origPoint.set(shape.getGravityCenter());

		if (CORNER_TRUN_ENABLED) {
			Coordinate c = shape.inCorner(x, y);
			if (c != null) {
				Coordinate a = shape.getAgainst(c);
				if (a != null) {
					origPoint.set(a);
				}
			}
		}
		attached = mission.dettach(this);
		return true;
	}

	public boolean onNingStart(float x, float y) {
		if (state != DRAG) {
			return false;
		}
		state = Piece.NING;
		ningPoint.set(x, y);
		attached = mission.dettach(this);
		return true;
	}

	public boolean onDrag(float x, float y) {
		float dx = x - dragPoint.x;
		float dy = y - dragPoint.y;
		Matrix matrix = new Matrix();
		matrix.setTranslate(dx, dy);
		shape.transform(matrix);
		dragPoint.set(x, y);

		mission.suggest(this);
		return true;
	}

	public boolean onTrun(float x, float y) {
		Line AB = new Line(origPoint, trunPoint);
		Line AC = new Line(origPoint, new Coordinate(x, y));
		float d = AB.anglev(AC);
		Matrix matrix = new Matrix();
		matrix.setRotate(d, origPoint.x, origPoint.y);
		shape.transform(matrix);
		trunPoint.set(x, y);

		mission.suggest(this);
		return true;
	}

	public boolean onNing(int pid, float x, float y) {
		Coordinate A;
		Line AB, AC;
		if (pid == 0) {
			A = new Coordinate(ningPoint);
			AB = new Line(A, dragPoint);
			AC = new Line(A, new Coordinate(x, y));
			dragPoint.set(x, y);
		} else {
			A = new Coordinate(dragPoint);
			AB = new Line(A, ningPoint);
			AC = new Line(A, new Coordinate(x, y));
			ningPoint.set(x, y);
		}
		float d = AB.anglev(AC);
		Matrix matrix = new Matrix();
		matrix.setRotate(d, A.x, A.y);
		shape.transform(matrix);

		mission.suggest(this);
		return true;
	}

	public boolean onStop(int pid, float x, float y) {
		switch (state) {
		case NONE:
			break;
		case DRAG:
			state = Piece.NONE;
			break;
		case TRUN:
			state = Piece.NONE;
			break;
		case NING:
			if (pid == 1) {
				state = Piece.DRAG;
			}
			if (pid == 0) {
				state = Piece.NONE;
			}
			break;
		}
		if (!suggestMatrix.isIdentity()) {
			shape.transform(suggestMatrix);
			suggestMatrix.reset();
			if (attachable) {
				Toolbox.getInstance().getAttachSoundEffect().start();
				attached = mission.attach(this);
			}
			// System.out.println("SUGGEST: " + polygon.tuString());
		}
		return true;
	}

	private Path getSuggest() {
		if (!suggestMatrix.isIdentity()) {
			Path suggest_path = new Path(shape.getRecentPath());
			suggest_path.transform(suggestMatrix);
			return suggest_path;
		}
		return null;
	}

	public void draw(Canvas canvas) {
		if (mainPaint == null) {
			mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mainPaint.setAntiAlias(true);
			mainPaint.setTextSize(33);
			mainPaint.setColor(color);
		}
		canvas.drawPath(shape.getRecentPath(), mainPaint);

		if (suggestPaint == null) {
			suggestPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			suggestPaint.setAntiAlias(true);
			suggestPaint.setTextSize(33);
			suggestPaint.setColor(Color.WHITE);
			suggestPaint.setStyle(Paint.Style.STROKE);
		}

		Path p = getSuggest();
		if (p != null) {
			canvas.drawPath(p, suggestPaint);
			if (debug_attach) {
				Paint debug_paint = new Paint();
				debug_paint.setStrokeWidth(6);
				debug_paint.setTextSize(24);

				debug_paint.setColor(Color.BLUE);
				for (int i = 0; i < shape.getSize(); i++) {
					Coordinate c = shape.getNode(i);
					canvas.drawText("" + i, c.x, c.y, debug_paint);
				}
				debug_paint.setColor(Color.RED);
				if (suggestNode[0] != null) {
					canvas.drawCircle(suggestNode[0].x, suggestNode[0].y, 10,
							debug_paint);
				}
				if (suggestEdge[0] != null) {
					canvas.drawLine(suggestEdge[0].s.x, suggestEdge[0].s.y,
							suggestEdge[0].e.x, suggestEdge[0].e.y, debug_paint);
				}
				debug_paint.setColor(Color.GREEN);
				if (suggestNode[1] != null) {
					canvas.drawCircle(suggestNode[1].x, suggestNode[1].y, 10,
							debug_paint);
				}
				if (suggestEdge[1] != null) {
					canvas.drawLine(suggestEdge[1].s.x, suggestEdge[1].s.y,
							suggestEdge[1].e.x, suggestEdge[1].e.y, debug_paint);
				}
			}
		}

		if (debug_type) {
			String t = shape.getType();
			if (t != null) {
				Paint debug_paint = new Paint();
				debug_paint.setTextSize(30);
				if (attached) {
					debug_paint.setColor(Color.WHITE);
				} else {
					debug_paint.setColor(Color.BLACK);
				}
				canvas.drawText(t, shape.getGravityCenter().x,
						shape.getGravityCenter().y, debug_paint);
			}
		}

		if (debug_axis) {
			Paint debug_paint = new Paint();
			debug_paint.setTextSize(12);
			debug_paint.setColor(Color.WHITE);
			for (int i = 0; i < shape.getSize(); i++) {
				Coordinate c = shape.getNode(i);
				canvas.drawText("" + i + ":" + c.tuString(0), c.x, c.y,
						debug_paint);
			}
		}

		if (debug_circle) {
			Paint debug_paint = new Paint();
			debug_paint.setTextSize(30);
			debug_paint.setStyle(Paint.Style.STROKE);
			debug_paint.setColor(Color.GREEN);

			for (int i = 0; i < shape.getSize(); i++) {
				Coordinate c = shape.getNode(i);
				canvas.drawText("" + i, c.x, c.y, debug_paint);
				canvas.drawCircle(c.x, c.y, Polygon.CORNER_PIXEL, debug_paint);
			}
		}
	}

	public String tuString(int level) {
		String sss = "";
		if (level >= 0) {
			sss += (attached ? "attached;" : "dettached;");
		}
		if (level >= 1) {
			sss += (attachable ? "attachable;" : "unattachable;");
		}
		if (level >= 2) {
			if (suggestNode[0] != null & suggestNode[1] != null)
				sss += "a-b-c:" + suggestNode[0].tuString(0) + "@"
						+ suggestNode[1].tuString(0);
			if (suggestEdge[0] != null && suggestEdge[1] != null)
				sss += "a-b-l" + suggestEdge[0].tuString(0) + "@"
						+ suggestEdge[1].tuString(0);
		}
		return sss;
	}
}
