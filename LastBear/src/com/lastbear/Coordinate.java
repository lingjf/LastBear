package com.lastbear;

import java.util.Comparator;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;

public class Coordinate implements Comparable<Coordinate> {

	public static final float Axis_EP = 0.5f;

	public float x;
	public float y;

	public Coordinate() {
		super();
	}

	public Coordinate(float x, float y) {
		set(x, y);
	}

	public Coordinate(Coordinate c) {
		set(c.x, c.y);
	}

	public Coordinate(PointF c) {
		set(c.x, c.y);
	}

	public Coordinate(Point c) {
		set((float) c.x, (float) c.y);
	}

	public void set(Coordinate c) {
		set(c.x, c.y);
	}

	public void set(PointF c) {
		set(c.x, c.y);
	}

	public void set(Point c) {
		set((float) c.x, (float) c.y);
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public boolean equals(Coordinate c) {
		return equals(c.x, c.y);
	}

	public boolean equals(PointF c) {
		return equals(c.x, c.y);
	}

	public boolean equals(Point c) {
		return equals((float) c.x, (float) c.y);
	}

	public boolean equals(float x, float y) {
		return Math.abs(this.x - x) < Axis_EP && Math.abs(this.y - y) < Axis_EP;
	}

	public int compareTo(Coordinate c) {
		if (equals(c)) {
			return 0;
		}
		if (x > c.x) {
			return 1;
		} else if (x < c.x) {
			return -1;
		} else {
			if (y > c.y) {
				return 1;
			} else if (y < c.y) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	public class Compare implements Comparator<Coordinate> {
		public int compare(Coordinate a, Coordinate b) {
			if (a.x > b.x) {
				return 1;
			} else if (a.x < b.x) {
				return -1;
			} else {
				if (a.y > b.y) {
					return 1;
				} else if (a.y < b.y) {
					return -1;
				} else {
					return 0;
				}
			}
		}

	}

	static public float distance(float x, float y, float a, float b) {
		return PointF.length(a - x, b - y);
	}

	static public float distance(Coordinate c, Coordinate d) {
		return PointF.length(d.x - c.x, d.y - c.y);
	}

	public float distance(Coordinate c) {
		return PointF.length(c.x - x, c.y - y);
	}

	public float distance(PointF c) {
		return PointF.length(c.x - x, c.y - y);
	}

	public float distance(Point c) {
		return PointF.length((float) c.x - x, (float) c.y - y);
	}

	public float distance(float cx, float cy) {
		return PointF.length(cx - x, cy - y);
	}

	public float distance() {
		return PointF.length(x, y);
	}

	public void rotate(float degrees) {
		rotate(degrees, 0.0f, 0.0f);
	}

	public void rotate(float degrees, float ox, float oy) {
		Matrix matrix = new Matrix();
		matrix.setRotate(degrees, ox, oy);
		transform(matrix);
	}

	public void transform(Matrix matrix, Coordinate dst) {
		float[] t = new float[] { x, y };
		matrix.mapPoints(t);
		dst.set(t[0], t[1]);
	}

	public void transform(Matrix matrix) {
		float[] t = new float[] { x, y };
		matrix.mapPoints(t);
		set(t[0], t[1]);
	}

	public String tuString(int level) {
		return String.format("(%.2f,%.2f)", x, y);
	}

	public String toString() {
		return tuString(0);
	}
}
