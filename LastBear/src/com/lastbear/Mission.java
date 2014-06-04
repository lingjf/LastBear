package com.lastbear;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class Mission {

	private static boolean debug_completed = true;
	private static boolean debug_axis = false;

	private String name;
	private int solve = 0;
	private float unit = 1.0f;
	private float rotate = 0.0f;
	private float xscale = 0.0f;
	private float yscale = 0.0f;
	private float width = 480;
	private float height = 800;

	public Polygon shape = null;
	public ArrayList<Polygon> pieces = new ArrayList<Polygon>();
	public ArrayList<Polygon> attaches = new ArrayList<Polygon>();
	public ArrayList<String> console = new ArrayList<String>();

	private Paint mainPaint = null;

	public Mission(String name, float unit, float rotate, float xscale,
			float yscale) {
		this.name = name;
		this.unit = unit;
		this.rotate = rotate;
		this.xscale = xscale;
		this.yscale = yscale;
	}

	public String getName() {
		return name;
	}

	public void incrSolve() {
		solve++;
	}

	public void setShape(ArrayList<Coordinate> points) {
		shape = new Polygon(points);
		attaches.add(shape);
	}

	public void addPiece(String type, ArrayList<Coordinate> points) {
		Polygon p = new Polygon(points);
		p.setType(type);
		pieces.add(p);
	}

	public void relocate(float w, float h) {
		this.width = w;
		this.height = h;
		Matrix matrix = new Matrix();
		matrix.setScale(1 / unit, 1 / unit, 0.0f, 0.0f);
		matrix.postRotate(rotate, 0.0f, 0.0f);
		shape.relocate(matrix);
		RectF rect = shape.getOriginRect();
		matrix.postTranslate(width / 2 - rect.centerX(),
				height / 2 - rect.centerY());
		float scale = 1.0f;
		if (xscale != 0.0f) {
			scale = (width * xscale) / rect.width();
		} else if (yscale != 0.0f) {
			scale = (height * yscale) / rect.height();
		} else {
			scale = (width * 0.8f) / rect.width();
			if (scale * rect.height() >= height) {
				scale = (height * 0.9f) / rect.height();
			}
		}
		matrix.postScale(scale, scale, width / 2, height / 2);
		shape.relocate(matrix);
		for (Polygon p : pieces) {
			p.relocate(matrix);
		}
	}

	public void deploy() {
		boolean vertical = false;
		if (width - shape.getRect().width() > height - shape.getRect().height()) {
			vertical = true;
		}
		Matrix matrix = new Matrix();
		int count = pieces.size();
		ArrayList<Polygon> copied = new ArrayList<Polygon>();
		for (int i = 0; i < count; i++) {
			Polygon p = pieces.get(i);
			matrix.reset();
			matrix.setRotate((float) Math.random() * 360,
					p.getRect().centerX(), p.getRect().centerY());
			p.transform(matrix);
			if (((int) (Math.random() * 1000)) % 2 == 0)
				copied.add(p);
			else
				copied.add(0, p);
		}

		float tx, ty;
		if (!vertical) {
			float h1 = 0.0f;
			float h2 = 0.0f;
			float x1 = 100.0f;
			int c1 = 1;
			float h3 = height;
			float h4 = height;
			float x3 = width;
			int c3 = 0;
			for (int i = 0; i < count; i++) {
				Polygon p = copied.get(i);
				RectF r = p.getRect();
				if (i % 2 == 0) {
					float d = width - x1;
					if (c1 >= 2 ? d < r.width() : d < r.width() * 0.7) {
						h1 = h2;
						if (h1 > height)
							h1 = 0.0f;
						x1 = 0.0f;
						c1 = 0;
					}
					float t = x1 + r.width();
					h2 = Math.max(h2, h1 + r.height());
					tx = (x1 + t) / 2;
					ty = (h1 + h2) / 2;
					x1 = t;
					c1++;
				} else {
					if (c3 >= 2 ? x3 < r.width() : x3 < r.width() * 0.7) {
						h3 = h4;
						if (h1 < 0.0f)
							h1 = height;
						x3 = width;
						c3 = 0;
					}
					float t = x3 - r.width();
					h4 = Math.min(h4, h3 - r.height());
					tx = (x3 + t) / 2;
					ty = (h3 + h4) / 2;
					x3 = t;
					c3++;
				}
				matrix.reset();
				matrix.setTranslate(tx - r.centerX(), ty - r.centerY());
				p.transform(matrix);
			}
		} else {
			float w1 = 0.0f;
			float w2 = 0.0f;
			float y1 = 100.0f;
			int c1 = 1;
			float w3 = width;
			float w4 = width;
			float y3 = height;
			int c3 = 0;
			for (int i = 0; i < count; i++) {
				Polygon p = copied.get(i);
				RectF r = p.getRect();
				if (i % 2 == 0) {
					float d = height - y1;
					if (c1 >= 3 ? d < r.height() : d < r.height() * 0.7) {
						w1 = w2;
						if (w1 > width)
							w1 = 0.0f;
						y1 = 0.0f;
						c1 = 0;
					}
					float t = y1 + r.height();
					w2 = Math.max(w2, w1 + r.width());
					tx = (w1 + w2) / 2;
					ty = (y1 + t) / 2;
					y1 = t;
					c1++;
				} else {
					if (c3 >= 3 ? y3 < r.height() : y3 < r.height() * 0.7) {
						w3 = w4;
						if (w3 < 0.0f)
							w1 = width;
						y3 = height;
						c3 = 0;
					}
					float t = y3 - r.height();
					w4 = Math.min(w4, w3 - r.width());
					tx = (w3 + w4) / 2;
					ty = (y3 + t) / 2;
					y3 = t;
					c3++;
				}
				matrix.reset();
				matrix.setTranslate(tx - r.centerX(), ty - r.centerY());
				p.transform(matrix);
			}
		}
	}

	public boolean attach(Piece piece) {
		if (attaches.contains(piece.shape)) {
			attaches.remove(piece.shape);
		}
		if (shape.relation(piece.shape) == Polygon.INSIDE) {
			attaches.add(piece.shape);
			return true;
		} else {
			System.out.println("[" + shape.tuString(0));
			System.out.println("]" + piece.shape.tuString(0));
			if (debug_completed) {
				console.add("[" + shape.tuString(0));
				console.add("]" + piece.shape.tuString(0));
			}
		}
		return false;
	}

	public boolean dettach(Piece piece) {
		if (attaches.contains(piece.shape)) {
			attaches.remove(piece.shape);
		}
		return false;
	}

	public void suggest(Piece piece) {
		piece.suggestMatrix.reset();
		piece.attachable = false;
		piece.suggestNode[0] = null;
		piece.suggestNode[1] = null;
		piece.suggestEdge[0] = null;
		piece.suggestEdge[1] = null;
		ArrayList<Coordinate> u = new ArrayList<Coordinate>();

		/* 计算Piece和attaches集合中最近的二个拐点 */
		float ds = 100000.0f;
		for (int i = 0; i < piece.shape.getSize(); i++) {
			Coordinate c = piece.shape.getNode(i);
			Coordinate d = null;
			float ts = 1000000.0f;
			for (int j = 0; j < attaches.size(); j++) {
				Polygon p = attaches.get(j);
				for (int k = 0; k < p.getSize(); k++) {
					Coordinate z = p.getNode(k);
					float t = c.distance(z);
					if (t < ts) {
						ts = t;
						d = z;
					}
				}
			}
			if (ts < 20.0f && d != null) {
				if (Math.abs(ts - ds) < 1.0f) {

				} else if (ts > ds) {
					continue;
				} else if (ts < ds) {
					u.clear();
					ds = ts;
				}
				u.add(c);
				u.add(d);
				u.add(piece.shape.getNode(i + 1));
				u.add(piece.shape.getNode(i - 1));
			}
		}

		if (u.size() > 0) { /* 如果这二点足够近, 接下来计算需要自动对齐的边 */
			float ts = 360.0f;
			Coordinate cz = null;
			Coordinate dz = null;
			Line lz = null;
			Line rz = null;
			Line[] cl = new Line[] { new Line(), new Line() };
			Line[] dl = new Line[] { new Line(), new Line() };
			for (int i = 0; i < u.size() / 4; i++) {
				Coordinate c = u.get(i * 4);
				Coordinate d = u.get(i * 4 + 1);
				cl[0] = new Line(c, u.get(i * 4 + 2));
				cl[1] = new Line(c, u.get(i * 4 + 3));
				for (int j = 0; j < attaches.size(); j++) {
					Polygon p = attaches.get(j);
					for (int k = 0; k < p.getSize(); k++) {
						if (p.getEdge(k).online(d) > 0) {
							dl[0] = new Line(d, p.getNode(k + 1));
							dl[1] = new Line(d, p.getNode(k));
							for (int m = 0; m < 2; m++) {
								for (int n = 0; n < 2; n++) {
									if (!dl[n].s.equals(dl[n].e)) {
										float t = cl[m].angle(dl[n]);
										if (t < ts) {
											ts = t;
											cz = c;
											dz = d;
											lz = cl[m];
											rz = dl[n];
										}
									}
								}
							}
						}
					}
				}
			}
			if (cz != null && dz != null) {
				piece.suggestMatrix.setTranslate(dz.x - cz.x, dz.y - cz.y);
				piece.suggestNode[0] = cz;
				piece.suggestNode[1] = dz;
				if (lz != null && rz != null) {
					float t = lz.anglev(rz);
					if (Math.abs(t) < 10.0f) { /* 边吸附 */
						piece.suggestMatrix.postRotate(t, dz.x, dz.y);
						piece.attachable = true;
						piece.suggestEdge[0] = lz;
						piece.suggestEdge[1] = rz;
					}
				}
			}
		} else { /* 计算需要平移对齐的边 */
			float ts = 10000.0f;
			Line lz = null;
			Line rz = null;
			for (int i = 0; i < piece.shape.getSize(); i++) {
				Line l = piece.shape.getEdge(i);
				for (int j = 0; j < attaches.size(); j++) {
					Polygon p = attaches.get(j);
					for (int k = 0; k < p.getSize(); k++) {
						Line r = p.getEdge(k);
						float t = (l.distance(r.s) + l.distance(r.e)) / 2;
						if (t < ts) {
							Coordinate c = r.perpend(l.s);
							Coordinate d = r.perpend(l.e);
							if (r.online(c) > 0 || r.online(d) > 0) {
								ts = t;
								lz = l;
								rz = r;
							}
						}
					}
				}
			}
			if (ts < 20.0f && lz != null && rz != null) {
				float as = lz.angle(rz);
				float fs = lz.direct(rz);
				if (as > 90.0f) {
					as = 180.0f - as;
					fs = fs * -1.0f;
				}
				if (as < 10.0f) {
					Coordinate p = null;
					Coordinate q = null;
					Coordinate c = rz.perpend(lz.s);
					Coordinate d = rz.perpend(lz.e);
					float rc = rz.online(c);
					float rd = rz.online(d);
					if (rc > 0 && rd > 0) {
						if (rz.distance(c) <= rz.distance(d)) {
							p = lz.s;
							q = c;
							if (rc == 0.5) {
								piece.attachable = true;
							}
						} else {
							p = lz.e;
							q = d;
							if (rd == 0.5) {
								piece.attachable = true;
							}
						}
					} else {
						if (rc > 0) {
							p = lz.s;
							q = c;
							if (rc == 0.5) {
								piece.attachable = true;
							}
						} else if (rd > 0) {
							p = lz.e;
							q = d;
							if (rd == 0.5) {
								piece.attachable = true;
							}
						}
					}
					if (p != null && q != null) {
						piece.suggestMatrix.setRotate(as * fs, p.x, p.y);
						piece.suggestMatrix.postTranslate(q.x - p.x, q.y - p.y);
						piece.suggestEdge[0] = lz;
						piece.suggestEdge[1] = rz;
					}
				}
			}
		}
	}

	public boolean completed() {
		if (attaches.size() != pieces.size() + 1) {
			return false;
		}
		for (int i = 0; i < pieces.size(); i++) {
			Polygon pi = pieces.get(i);
			for (int j = i + 1; j < pieces.size(); j++) {
				Polygon pj = pieces.get(j);
				if (pi.relation(pj) != Polygon.OUTSIDE) {
					System.out.println("<" + pi.tuString(0));
					System.out.println(">" + pj.tuString(0));
					if (debug_completed) {
						console.add("<" + pi.tuString(0));
						console.add(">" + pj.tuString(0));
					}
					return false;
				}
			}
		}
		return true;
	}

	public void draw(Canvas canvas) {
		if (mainPaint == null) {
			mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mainPaint.setAntiAlias(true);
			mainPaint.setColor(Color.WHITE);
			mainPaint.setStyle(Paint.Style.STROKE);
		}

		canvas.drawPath(shape.getRecentPath(), mainPaint);

		for (int i = 0; i < pieces.size(); i++) {
			if (i < solve) {
				canvas.drawPath(pieces.get(i).getOriginPath(), mainPaint);
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
		if (debug_completed) {
			Paint debug_paint = new Paint();
			debug_paint.setTextSize(12);
			debug_paint.setColor(Color.RED);

			for (int i = 0; i < console.size(); i++) {
				canvas.drawText(console.get(i), 0, (15 * (i + 1)) % 800,
						debug_paint);
			}
			console.clear();
		}
	}

	public Bitmap thumbnail(boolean admissible) {
		RectF rect = shape.getRect();
		float length;
		if (rect.width() >= rect.height()) {
			length = rect.width() * 1.2f;
			length = Math.max(length, width);
		} else {
			length = rect.height() * 1.2f;
			length = Math.min(length, height);
		}
		float left = (rect.left + rect.right) / 2 - length / 2;
		float top = (rect.top + rect.bottom) / 2 - length / 2;

		Matrix matrix = new Matrix();
		matrix.setTranslate(-left, -top);
		matrix.postScale(100 / length, 100 / length, 0, 0);
		Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);

		Path path = new Path();
		if (admissible) {
			for (int i = 0; i < pieces.size(); i++) {
				paint.setColor(Toolbox.COLORS[i]);
				path.reset();
				pieces.get(i).getOriginPath().transform(matrix, path);
				canvas.drawPath(path, paint);
			}
		} else {
			path.reset();
			shape.getOriginPath().transform(matrix, path);
			paint.setColor(Color.argb(0xFF, 0x22, 0x77, 0xCC));
			canvas.drawPath(path, paint);
		}
		return bitmap;
	}

	public String tuString(int level) {
		return "";
	}
}
