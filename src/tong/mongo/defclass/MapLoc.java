package tong.mongo.defclass;

import java.util.HashMap;

/**
 * 地图类，包括点集合，弧集合
 * @author ddyyxx
 * 
 */
public class MapLoc {
	
	public MapLoc() {
		PointNum = LineNum = 0;
		PointSet = new HashMap<Long, Point>();
		LineSet = new HashMap<Long, Line>();
	}

	public MapLoc(int pnum, int lnum) {
		PointNum = pnum;
		LineNum = lnum;
		PointSet = new HashMap<Long, Point>();
		LineSet = new HashMap<Long, Line>();
	}

	public void addPoint(long id, double lat, double lng) {
		PointSet.put(id, new Point(lat, lng));
	}

	public void addLine(long id, long xid, long yid, double len, long stid) {
		LineSet.put(id, new Line(PointSet.get(xid), PointSet.get(yid), id, xid,
				yid, len, stid));
	}

	public Line getLine(long lid) {
		return LineSet.get(lid);
	}

	public Point getPoint(long pid) {
		return PointSet.get(pid);
	}

	public int getPointnum() {
		return PointNum;
	}

	public int getLinenum() {
		return LineNum;
	}

	public void print() {
		System.out.println(PointNum);

		for (Long id : PointSet.keySet()) {
			System.out.print(id + " ");
			PointSet.get(id).print();
		}

		System.out.println(LineNum);
		for (Long id : LineSet.keySet()) {
			LineSet.get(id).print();
		}
	}

	public HashMap<Long, Point> PointSet;
	public HashMap<Long, Line> LineSet;
	public int PointNum, LineNum;
}
