package com.lastbear;

import android.graphics.Matrix;

public class Line {
	public static final float Angle_EP = 0.2f;
	public static final float Zero_EP = 0.0001f;
	public static final float Cross_EP = 0.0001f;

	public Coordinate s = new Coordinate(0, 0);
	public Coordinate e = new Coordinate(0, 0);
	private float[] v = new float[4];

	/* 直线的解析方程 a*x + b*y + c = 0 */
	private float a = 0, b = 0, c = 0;

	public Line() {
		super();
	}

	public Line(Coordinate sp, Coordinate ep) {
		super();
		set(sp.x, sp.y, ep.x, ep.y);
	}

	public Line(float sx, float sy, float ex, float ey) {
		super();
		set(sx, sy, ex, ey);
	}

	public void set(Coordinate sp, Coordinate ep) {
		set(sp.x, sp.y, ep.x, ep.y);
	}

	public void set(float sx, float sy, float ex, float ey) {
		this.s.set(sx, sy);
		this.e.set(ex, ey);
		equation();
	}

	public void inverse() {
		set(e.x, e.y, s.x, s.y);
	}

	private void equation() {
		int sign = 1;
		a = e.y - s.y;
		if (a < 0) {
			sign = -1;
			a = sign * a;
		}
		b = sign * (s.x - e.x);
		c = sign * (s.y * e.x - s.x * e.y);
	}

	public float square() {
		float dx = e.x - s.x;
		float dy = e.y - s.y;
		return dx * dx + dy * dy;
	}

	/* 线段长度 */
	public float length() {
		return s.distance(e);
	}

	public void transform(Matrix matrix, Line dst) {
		v[0] = s.x;
		v[1] = s.y;
		v[2] = e.x;
		v[3] = e.y;
		matrix.mapPoints(v);
		dst.set(v[0], v[1], v[2], v[3]);
	}

	public void transform(Matrix matrix) {
		v[0] = s.x;
		v[1] = s.y;
		v[2] = e.x;
		v[3] = e.y;
		matrix.mapPoints(v);
		set(v[0], v[1], v[2], v[3]);
	}

	static public float dot_product(float abx, float aby, float acx, float acy) {
		return (abx * acx + aby * acy); /* 向量內积点积定理 */
	}

	static public float cross_product(float abx, float aby, float acx, float acy) {
		return (abx * acy - aby * acx); /* 向量外积叉积定理 */
	}

	static public float dot_product(Line l, Line r) {
		float ABx = l.e.x - l.s.x;
		float ABy = l.e.y - l.s.y;
		float ACx = r.e.x - r.s.x;
		float ACy = r.e.y - r.s.y;
		return (ABx * ACx + ABy * ACy); /* 向量內积点积定理 */
	}

	static public float cross_product(Line l, Line r) {
		float ABx = l.e.x - l.s.x;
		float ABy = l.e.y - l.s.y;
		float ACx = r.e.x - r.s.x;
		float ACy = r.e.y - r.s.y;
		return (ABx * ACy - ABy * ACx); /* 向量外积叉积定理 */
	}

	public float cos(Line with) {
		float abx = this.e.x - this.s.x;
		float aby = this.e.y - this.s.y;
		float acx = with.e.x - with.s.x;
		float acy = with.e.y - with.s.y;
		float abl = (float) Math.sqrt(abx * abx + aby * aby);
		float acl = (float) Math.sqrt(acx * acx + acy * acy);
		float t = abl * acl;
		if (t < Zero_EP) {
			return 1.0f;
		}
		return dot_product(abx, aby, acx, acy) / t;
	}

	/**
	 * 求二线段的夹角度
	 */
	public float angle(Line with) {
		float c = cos(with);
		if (c > 1.0f) {
			c = 1.0f;
		} else if (c < -1.0f) {
			c = -1.0f;
		}
		return (float) Math.toDegrees(Math.acos(c));
	}

	/**
	 * 求二线段的夹角度 以及方向
	 */
	public float anglev(Line with) {
		float abx = this.e.x - this.s.x;
		float aby = this.e.y - this.s.y;
		float acx = with.e.x - with.s.x;
		float acy = with.e.y - with.s.y;

		float c = cos(with);
		if (c > 1.0f) {
			c = 1.0f;
		} else if (c < -1.0f) {
			c = -1.0f;
		}
		float a = (float) Math.toDegrees(Math.acos(c));
		float z = cross_product(abx, aby, acx, acy);
		if (z < 0) {
			a = -a;
		}
		return a;
	}

	/**
	 * 求线段在线段的方向,采用叉积算法
	 */
	public float direct(Line with) {
		float abx = this.e.x - this.s.x;
		float aby = this.e.y - this.s.y;
		float acx = with.e.x - with.s.x;
		float acy = with.e.y - with.s.y;

		float z = cross_product(abx, aby, acx, acy);
		float a = angle(with);
		if (a > 90.0f) {
			a = 180.0f - a;
		}
		if (a < Angle_EP) {
			return 0.0f;
		} else {
			if (z > 0.0f) {
				return 1.0f;
			} else if (z == 0.0f) {
				return 0.0f;
			} else {
				return -1.0f;
			}
		}
	}

	/**
	 * 求点在线段的方向,采用叉积算法
	 */
	public float direct(Coordinate p) {
		return direct(new Line(s, p));
	}

	/**
	 * 求点是求在线段上
	 * 
	 * @param P
	 * @return =0: P is out of LINE
	 * @return =0.5: P is on point of line
	 * @return =1: P is on inside of LINE
	 */
	public float online(Coordinate p) {
		if (s.equals(p.x, p.y) || e.equals(p.x, p.y)) {
			return 0.5f;
		}
		if (Math.abs(direct(p)) == 0.0f) {
			float left = Math.min(s.x, e.x) - Coordinate.Axis_EP;
			float right = Math.max(s.x, e.x) + Coordinate.Axis_EP;
			float top = Math.min(s.y, e.y) - Coordinate.Axis_EP;
			float bottom = Math.max(s.y, e.y) + Coordinate.Axis_EP;

			if ((left <= p.x) && (p.x <= right) && (top <= p.y)
					&& (p.y <= bottom)) {
				return 1.0f;
			}
		}
		return 0.0f;
	}

	/**
	 * 二线段是否交于某端点
	 */
	public Coordinate stickup(Line with) {
		if (with.online(s) > 0) {
			return s;
		}
		if (with.online(e) > 0) {
			return e;
		}
		if (online(with.s) > 0) {
			return with.s;
		}
		if (online(with.e) > 0) {
			return with.e;
		}
		return null;
	}

	/**
	 * 求二线段的交点, 交与端点返回null, Powered by Box2D
	 */
	public Coordinate intersect(Line with) {
		float x1 = this.s.x;
		float y1 = this.s.y;
		float x2 = this.e.x;
		float y2 = this.e.y;
		float x3 = with.s.x;
		float y3 = with.s.y;
		float x4 = with.e.x;
		float y4 = with.e.y;

		if (stickup(with) != null)
			return null;

		float left = Math.min(x1, x2) - Coordinate.Axis_EP;
		float right = Math.max(x1, x2) + Coordinate.Axis_EP;
		float top = Math.min(y1, y2) - Coordinate.Axis_EP;
		float bottom = Math.max(y1, y2) + Coordinate.Axis_EP;

		if (Math.max(x3, x4) < left || right < Math.min(x3, x4))
			return null;
		if (Math.max(y3, y4) < top || bottom < Math.min(y3, y4))
			return null;

		float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3));
		float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3));
		float denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (Math.abs(denom) < 0.000001f)
			return null;

		ua /= denom;
		ub /= denom;

		if ((0 < ua) && (ua < 1) && (0 < ub) && (ub < 1)) {
			Coordinate t = new Coordinate((x1 + ua * (x2 - x1)), (y1 + ua
					* (y2 - y1)));
			return t;
		}

		return null;
	}

	/**
	 * 求垂足与线段的关系(Z为P点在线段上的垂足)
	 * 
	 * @param p
	 * @return =0: Z is on S(start point of line)
	 * @return =1: Z is on E(end point of line)
	 * @return <0: Z is on the backward extension of LINE
	 * @return >1: Z is on the forward extension of LINE
	 * @return 0<&<1: Z is interior to LINE
	 */
	public float project(Coordinate p) {
		float abx = e.x - s.x;
		float aby = e.y - s.y;
		float acx = p.x - s.x;
		float acy = p.y - s.y;

		float r = dot_product(abx, aby, acx, acy) / square();
		return r;
	}

	/**
	 * 求垂足
	 */
	public Coordinate perpend(Coordinate p) {
		float r = project(p);
		return new Coordinate(s.x + r * (e.x - s.x), s.y + r * (e.y - s.y));
	}

	/**
	 * 求垂直距离
	 */
	public float distance(Coordinate p) {
		float abx = e.x - s.x;
		float aby = e.y - s.y;
		float acx = p.x - s.x;
		float acy = p.y - s.y;

		return Math.abs(cross_product(abx, aby, acx, acy)) / length();
	}

	/**
	 * 求点到线段的最近距离(不一定是垂足)
	 */
	public float mileage(Coordinate p) {
		float r = project(p);
		if (r < 0) {
			return p.distance(s);
		} else if (r > 1) {
			return p.distance(e);
		} else {
			return distance(p);
		}
	}

	public String tuString(int level) {
		String sss = "";
		if (level >= 0) {
			sss += String.format("(%.2f,%.2f:%.2f,%.2f)", s.x, s.y, e.x, e.y);
		}
		if (level >= 1) {
			sss += String.format("(%.2fx%s%.2fy%s%.2f=0)", a, b < 0 ? "" : "+",
					b, c < 0 ? "" : "+", c);
		}
		return sss;
	}

	public String toString() {
		return tuString(0);
	}
}
