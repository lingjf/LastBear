package com.lastbear;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

public class Polygon {

	public final static int SHADOW_PIXEL = 80;
	public final static int CORNER_PIXEL = 50;

	public final static int INSIDE = 1;
	public final static int ONEDGE = 2;
	public final static int ONNODE = 3;
	public final static int OUTSIDE = 0;
	public final static int CROSS = -1;
	public final static int ENCLOSE = -2;

	private String type = null;
	private int size = 0;
	private float[] configs = null;
	private float[] origins = null;
	private float[] recents = null;

	private Matrix matrixs = new Matrix(); /* identity matrix */

	private Coordinate[] nodes = null;
	private Line[] edges = null;

	private Coordinate gravityCenter = null;
	private Float radius = null;
	private RectF rect = null;

	private Path originPath = null;
	private Path recentPath = null;

	public Polygon(ArrayList<Coordinate> points) {
		super();
		size = points.size();
		configs = new float[size * 2];
		origins = new float[size * 2];
		recents = new float[size * 2];

		for (int i = 0; i < size; i++) {
			configs[i * 2] = points.get(i).x;
			configs[i * 2 + 1] = points.get(i).y;
			origins[i * 2] = points.get(i).x;
			origins[i * 2 + 1] = points.get(i).y;
			recents[i * 2] = points.get(i).x;
			recents[i * 2 + 1] = points.get(i).y;
		}
	}

	public void relocate(Matrix matrix) {
		matrix.mapPoints(origins, configs);

		matrixs.reset();
		matrixs.mapPoints(recents, origins);
		gravityCenter = null;
		radius = null;
		rect = null;
		nodes = null;
		edges = null;
		originPath = null;
		recentPath = null;
	}

	public void transform(Matrix matrix) {
		matrixs.postConcat(matrix);

		matrixs.mapPoints(recents, origins);
		gravityCenter = null;
		rect = null;
		nodes = null;
		edges = null;
		recentPath = null;
	}

	public int getSize() {
		return size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public RectF getConfigRect() {
		RectF rect = new RectF(100000.0f, 100000.0f, -1000000.0f, -1000000.0f);
		for (int i = 0; i < size; i++) {
			float x = configs[i * 2];
			float y = configs[i * 2 + 1];
			if (x < rect.left)
				rect.left = x;
			if (x > rect.right)
				rect.right = x;
			if (y < rect.top)
				rect.top = y;
			if (y > rect.bottom)
				rect.bottom = y;
		}
		return rect;
	}
	
	public RectF getOriginRect() {
		RectF rect = new RectF(100000.0f, 100000.0f, -1000000.0f, -1000000.0f);
		for (int i = 0; i < size; i++) {
			float x = origins[i * 2];
			float y = origins[i * 2 + 1];
			if (x < rect.left)
				rect.left = x;
			if (x > rect.right)
				rect.right = x;
			if (y < rect.top)
				rect.top = y;
			if (y > rect.bottom)
				rect.bottom = y;
		}
		return rect;
	}	

	public RectF getRect() {
		if (rect == null) {
			rect = new RectF(1000000.0f, 1000000.0f, -10000000.0f, -10000000.0f);
			for (int i = 0; i < size; i++) {
				Coordinate c = getNode(i);
				if (c.x < rect.left)
					rect.left = c.x;
				if (c.x > rect.right)
					rect.right = c.x;
				if (c.y < rect.top)
					rect.top = c.y;
				if (c.y > rect.bottom)
					rect.bottom = c.y;
			}
		}
		return rect;
	}

	public Coordinate getGravityCenter() {
		if (gravityCenter == null) {
			gravityCenter = new Coordinate();
			float x_sum = 0.0f;
			float y_sum = 0.0f;
			for (int i = 0; i < size; i++) {
				x_sum += recents[i * 2];
				y_sum += recents[i * 2 + 1];
			}
			gravityCenter.set(x_sum / size, y_sum / size);
		}
		return gravityCenter;
	}

	public float getRadius() {
		if (radius == null) {
			float t = 0;
			for (int i = 0; i < size; i++) {
				float r = Coordinate.distance(getRect().centerX(), getRect()
						.centerY(), recents[i * 2], recents[i * 2 + 1]);
				t = Math.max(t, r);
			}
			radius = new Float(t);
		}
		return radius.floatValue();
	}

	public Coordinate getNode(int index) {
		if (nodes == null) {
			nodes = new Coordinate[size];
			for (int i = 0; i < size; i++) {
				nodes[i] = new Coordinate(recents[i * 2], recents[i * 2 + 1]);
			}
		}
		if (index < 0)
			index += size;
		return nodes[index % size];
	}

	public Line getEdge(int index) {
		if (edges == null) {
			edges = new Line[size];
			for (int i = 0; i < size; i++) {
				edges[i] = new Line(getNode(i), getNode(i + 1));
			}
		}
		if (index < 0)
			index += size;
		return edges[index % size];
	}

	public Line[] getEdge(Coordinate node) {
		for (int i = 0; i < size; i++) {
			Coordinate c = getNode(i);
			if (node.equals(c)) {
				return new Line[] { getEdge(i - 1), getEdge(i) };
			}
		}
		return null;
	}

	public Path getRecentPath() {
		if (recentPath == null) {
			recentPath = new Path();
			recentPath.moveTo(recents[0], recents[1]);
			for (int i = 1; i < size; i++) {
				recentPath.lineTo(recents[i * 2], recents[i * 2 + 1]);
			}
			recentPath.close();
		}
		return recentPath;
	}

	public Path getOriginPath() {
		if (originPath == null) {
			originPath = new Path();
			originPath.moveTo(origins[0], origins[1]);
			for (int i = 1; i < size; i++) {
				originPath.lineTo(origins[i * 2], origins[i * 2 + 1]);
			}
			originPath.close();
		}
		return originPath;
	}

	public int relation(Coordinate c) {
		/* P点在多边形的边上或角上的情况 */
		for (int i = 0; i < size; i++) {
			float t = getEdge(i).online(c);
			if (t == 0.5f) {
				return ONNODE;
			} else if (t == 1.0f) {
				return ONEDGE;
			}
		}
		/* 求出射线和多边形各边的相交个数(不包括与角相交) */
		int count = 0;
		Line l = new Line(c.x, c.y, c.x + 10000.0f, c.y);
		for (int i = 0; i < size; i++) {
			if (l.intersect(getEdge(i)) != null) {
				count++;
			}
		}
		/* 找出第一个与它的前后边都不共线的角 */
		int offset = 0;
		for (int i = 0; i < size; i++) {
			if (l.online(getNode(i)) > 0) {
				float t = l.direct(getEdge(i - 1)) * l.direct(getEdge(i));
				if (t == 0) {
					continue;
				}
			}
			offset = i;
			break;
		}
		/* 射线与角相交的各数 */
		// (1) 穿越相交，计为1
		// ```\
		// ----\------- 射线
		// ```` \
		// (2) 不穿越，计为0
		// ```\ `/
		// ____\/______ 射线
		//
		// (3) 共线穿越相交，计为1
		// `` \
		// ____\_______ 射线
		// ```````` \
		// ````````` \
		// (4) 共线不穿越，计为0
		// `` \ ``` /
		// ____\___/___ 射线
		//
		float last_direct = 0.0f;
		for (int i = offset; i < offset + size; i++) {
			if (l.online(getNode(i)) > 0) {
				float back_direct = l.direct(getEdge(i - 1));
				float front_direct = l.direct(getEdge(i));
				float t = back_direct * front_direct;
				if (t > 0) {
					count++;
				} else if (t == 0) {
					float current_direct = 0.0f;
					if (back_direct != 0.0f) {
						current_direct = back_direct;
					}
					if (front_direct != 0.0f) {
						current_direct = front_direct;
					}
					if (current_direct != 0.0f) {
						if (last_direct == 0.0f) {
							last_direct = current_direct;
						} else {
							if (last_direct * current_direct > 0) {
								count++;
							}
							last_direct = 0.0f;
						}
					}
				} else {
					// ignore this cross;
				}
			}
		}
		return (count % 2) != 0 ? INSIDE : OUTSIDE;
	}

	public int relation(Line l) {
		int expect = ONEDGE;
		int rls = relation(l.s);
		int rle = relation(l.e);
		if (rls == OUTSIDE || rle == OUTSIDE) {
			if (rls == INSIDE || rle == INSIDE) {
				return CROSS;
			}
			expect = OUTSIDE;
		} else if (rls == INSIDE || rle == INSIDE) {
			if (rls == OUTSIDE || rle == OUTSIDE) {
				return CROSS;
			}
			expect = INSIDE;
		} else {
			expect = ONEDGE;
		}

		TreeSet<Coordinate> ts = new TreeSet<Coordinate>();
		for (int i = 0; i < size; i++) {
			Line s = getEdge(i);
			if (l.intersect(s) != null) {
				return CROSS;
			} else {
				if (s.online(l.s) > 0) {
					ts.add(l.s);
				}
				if (s.online(l.e) > 0) {
					ts.add(l.e);
				}
				if (l.online(s.s) > 0) {
					ts.add(s.s);
				}
				if (l.online(s.e) > 0) {
					ts.add(s.e);
				}
			}
		}

		Iterator<Coordinate> it = ts.iterator();
		if (it.hasNext()) {
			Coordinate last = it.next();
			while (it.hasNext()) {
				Coordinate c = it.next();
				Coordinate m = new Coordinate((c.x + last.x) / 2,
						(c.y + last.y) / 2);
				switch (relation(m)) {
				case INSIDE:
					switch (expect) {
					case INSIDE:
						break;
					case ONEDGE:
						expect = INSIDE;
						break;
					case OUTSIDE:
						return CROSS;
					}
					break;
				case ONNODE:
				case ONEDGE:
					switch (expect) {
					case INSIDE:
						break;
					case ONEDGE:
						break;
					case OUTSIDE:
						break;
					}
					break;
				case OUTSIDE:
					switch (expect) {
					case INSIDE:
						return CROSS;
					case ONEDGE:
						expect = OUTSIDE;
						break;
					case OUTSIDE:
						break;
					}
					break;
				}
			}
		}

		return expect;
	}

	public int relation(Polygon p) {
		int expect = ONEDGE;
		for (int i = 0; i < p.getSize(); i++) {
			switch (relation(p.getEdge(i))) {
			case INSIDE:
				switch (expect) {
				case INSIDE:
					break;
				case OUTSIDE:
					return CROSS;
				case ONEDGE:
					expect = INSIDE;
					break;
				}
				break;
			case OUTSIDE:
				switch (expect) {
				case INSIDE:
					return CROSS;
				case OUTSIDE:
					break;
				case ONEDGE:
					expect = OUTSIDE;
					break;
				}
				break;
			case ONEDGE:
				break;
			case CROSS:
				return CROSS;
			}
		}
		if (expect == OUTSIDE) {
			for (int i = 0; i < getSize(); i++) {
				if (p.relation(getEdge(i)) == INSIDE) {
					return ENCLOSE;
				}
			}
		} else if (expect == ONEDGE) {
			expect = INSIDE;
		}
		return expect;
	}

	public boolean isInside(float x, float y) {
		int r = relation(new Coordinate(x, y));
		return (r == INSIDE || r == ONNODE || r == ONEDGE);
	}

	public boolean inShadow(float x, float y) {
		Coordinate p = new Coordinate(x, y);
		for (int i = 0; i < size; i++) {
			if (getEdge(i).mileage(p) < SHADOW_PIXEL)
				return true;
		}
		return false;
	}

	public Coordinate inCorner(float x, float y) {
		for (int i = 0; i < size; i++) {
			if (getEdge(i - 1).direct(getEdge(i)) >= 0) {
				Coordinate p = getNode(i);
				if (p.distance(x, y) < CORNER_PIXEL) {
					return p;
				}
			}
		}
		return null;
	}

	public Coordinate getAgainst(Coordinate node) {
		Coordinate k = null;
		float m = 0.0f;
		for (int j = 0; j < size; j++) {
			Coordinate c = getNode(j);
			float t = node.distance(c);
			if (t > m) {
				m = t;
				k = c;
			}
		}
		return k;
	}

	public String stateTuString(int state) {
		switch (state) {
		case INSIDE:
			return "INSIDE";
		case ONEDGE:
			return "ONEDGE";
		case ONNODE:
			return "ONNODE";
		case OUTSIDE:
			return "OUTSIDE";
		case CROSS:
			return "CROSS";
		case ENCLOSE:
			return "ENCLOSE";
		default:
			return "UNKNOWN";
		}
	}

	public String tuString(int level) {
		StringBuilder sb = new StringBuilder();
		if (level >= 0) {
			sb.append("size=" + size + "(");
			for (int i = 0; i < size; i++) {
				sb.append(String.format("%.2f,%.2f;", recents[i * 2],
						recents[i * 2 + 1]));
			}
			sb.append(")");
		}
		if (level >= 1) {
			sb.append(String.format("%s,%.2f", getGravityCenter().tuString(0),
					getRadius()));
		}
		return sb.toString();
	}

	public String toString() {
		return tuString(0);
	}
}
