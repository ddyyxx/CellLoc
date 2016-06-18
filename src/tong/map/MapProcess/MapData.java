package tong.map.MapProcess;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import tong.mongo.defclass.Line;
import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Point;
import tong.mongo.loction.Algorithm;
import tong.mongo.loction.CellLoc;

import com.defcons.SystemSettings;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 
 * @author ddyyxx 从数据库中读取地图数据（点，弧，弧长度，地图）
 */
public class MapData {
	// 根据弧ID返回对应的弧
	public static Line GetLine(long Lineid, DB db) throws UnknownHostException {
		DBCollection dbcollArc = db.getCollection("mapArc");
		DBCursor dbcsorArc = dbcollArc.find(new BasicDBObject("_id", Lineid));
		DBObject arcobject = dbcsorArc.next();
		@SuppressWarnings("unchecked")
		Map<String, Long> m = (Map<String, Long>) arcobject.get("gis");
		long pointA = m.get("x");
		long pointB = m.get("y");
		Line retLine = new Line(GetPoint(pointA, db), GetPoint(pointB, db),
				Lineid, pointA, pointB, 0, 0);
		return retLine;
	}
	//根据点ID返回对应的点
	public static Point GetPoint(long pointid, DB db) {
		DBCollection dbcollArc = db.getCollection("mapPoint");
		DBCursor dbcsorPo = dbcollArc.find(new BasicDBObject("_id", pointid));
		DBObject poobject = dbcsorPo.next();
		@SuppressWarnings("unchecked")
		Map<String, Double> m = (Map<String, Double>) poobject.get("gis");
		Point retpo = new Point(m.get("lat"), m.get("lon"));
		return retpo;
	}
	//根据弧ID返回对应点的长度
	public static double GetLineLength(long Lineid, DB db)
			throws UnknownHostException {
		DBCollection dbcollArc = db.getCollection("mapArc");
		DBCursor dbcsorArc = dbcollArc.find(new BasicDBObject("_id", Lineid));
		DBObject arcobject = dbcsorArc.next();
		Double length = (Double) arcobject.get("length");
		return length;
	}

	// -------------------获取点集合周围的道路点和弧-------------------------//
	@SuppressWarnings("unchecked")
	public static MapLoc getMap(Vector<Point> pointSet, DB db, double radius) { // 调用的时候再加上一个db即可
		MapLoc mLoc = new MapLoc(); // 存放搜索的结果
		HashMap<Long, Point> pMap = new HashMap<Long, Point>(); // 存放点集合
		HashMap<Long, Line> lMap = new HashMap<Long, Line>(); // 存放弧集合
		List<Long> aList = new LinkedList<Long>();
		DBCollection coll = null;
		DBObject object = null; // 数据库的搜索条件
		boolean isFirstPoint = true;
		double prelat = 0, prelng = 0;// 前一个点的Gps坐标
		for (Point currpoi : pointSet) { // 如果只有一个点，就按照默认半径进行取点
			double latt = currpoi.x;
			double lngg = currpoi.y;
			if (!isFirstPoint) { // 如果不是第一个点, 取两相邻点的中点，以两相邻点距离的二分之一为半径
				latt = (latt + prelat) / 2;
				lngg = (lngg + prelng) / 2;
				radius = (Algorithm.Distance(latt, lngg, prelat, prelng) + 300.0) / 1000.0;
			}
			prelat = latt;
			prelng = lngg;
			isFirstPoint = false;
			coll = db.getCollection("mapPoint"); // 获取点集合
			object = new BasicDBObject("gis", new BasicDBObject("$within",
					new BasicDBObject("$center", Arrays.asList(
							Arrays.asList(latt, lngg), radius / 111.12))));
			DBCursor cursor = coll.find(object);
			// 找出一辆车行驶线路范围内的点
			while (cursor.hasNext()) {
				DBObject result = cursor.next();
				Long key = (Long) result.get("_id");

				if (!pMap.containsKey(key)) {
					Point p = new Point();
					Map<String, Double> m = (Map<String, Double>) result
							.get("gis");
					List<Long> list = (List<Long>) result.get("edge");
					Vector<Long> vtor = new Vector<Long>();
					vtor.addAll(list);
					p.id = (Long) result.get("_id");
					p.x = m.get("lat");
					p.y = m.get("lon");
					pMap.put(key, p);
					for (long v : vtor) { // 将包含当前点的弧的ID放进list
						if (!aList.contains(v))
							aList.add(v);
					}
				}
			}// END WHILE
				// 找出一辆车行驶线路范围内的线段
			coll = db.getCollection("mapArc");
			cursor = coll.find(new BasicDBObject("_id", new BasicDBObject(
					"$in", aList)));

			while (cursor.hasNext()) {
				DBObject result = cursor.next();
				Long key = (Long) result.get("_id");
				if (!lMap.containsKey(key)) {
					Line l = new Line();
					long id = (Long) result.get("_id");
					Map<String, Long> m = (Map<String, Long>) result.get("gis");
					double length = (Double) result.get("length");
					long strid = (Long) result.get("wayid");
					l.index = id;
					l.pid[0] = m.get("x");
					l.pid[1] = m.get("y");

					l.length = length;
					l.strid = strid;
					if (pMap.containsKey(l.pid[0])
							&& pMap.containsKey(l.pid[1])) {
						l.p[0] = pMap.get(l.pid[0]);
						l.p[1] = pMap.get(l.pid[1]);
						if (SystemSettings.PrintDriveOrbit)
							CellLoc.DriveMap.put(key, l);
						lMap.put(key, l);
					}
				}
			}// END WHILE
		}
		mLoc.PointNum = pMap.size();
		mLoc.LineNum = lMap.size();
		mLoc.PointSet = pMap;
		mLoc.LineSet = lMap;
		return mLoc;
	}
}
