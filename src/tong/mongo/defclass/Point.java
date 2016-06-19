package tong.mongo.defclass;

import java.util.Vector;

public class Point {
	public long id;
	public double x;// γ��
	public double y;// ����

	public Point() {
		x=y=0;
	}

	public Point(double lat, double lng) {
		this.x = lat;
		this.y = lng;
	}

	public Point(Point other) {
		this.x = other.x;
		this.y = other.y;
	}

	public void print() {
		System.out.println(x + " " + y);
	}

	public Vector<Long> line_set;// ��ʾ�õ������Ļ����ϣ������ж���
}
