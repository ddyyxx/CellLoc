package tong.map.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.defcons.MyCons;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;


/// 将地图数据插入到数据库当中
public class MdbInsert {

	static Mongo connection = null;
	static DB db = null;

	@SuppressWarnings("deprecation")
	public MdbInsert(String dataBaseName) throws UnknownHostException {
		connection = new Mongo("127.0.0.1:27017");
		db = connection.getDB(dataBaseName);
		db.dropDatabase();
	}

	public void createCollection(String name) {
		// 判断数据集合是否存在，不存在创建数据集合
		if (!db.collectionExists(name)) {
			DBObject dbs = new BasicDBObject();
			db.createCollection(name, dbs);
		}
	}

	// 单个数据插入
	public void insert(String collname, DBObject dbs) {
		DBCollection coll = db.getCollection(collname);
		coll.insert(dbs);
	}

	// 批量插入数据
	public void insertBatch(String collname, List<DBObject> dbses) {
		DBCollection coll = db.getCollection(collname);
		coll.insert(dbses);
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {

		MdbInsert mongo = new MdbInsert("MapLoc");
		mongo.createCollection("mapPoint");
		mongo.createCollection("mapArc");
		File fmap = new File(MyCons.MongoDataDir+"MongoDB\\mapPoint.txt");
		File farc = new File(MyCons.MongoDataDir+"MongoDB\\mapArc.txt");

		if (!fmap.exists()) {
			System.out.println("No mapPoint.txt");
			connection.close();
			System.exit(1);
		}
		if (!farc.exists()) {
			System.out.println("No mapArc.txt");
			connection.close();
			System.exit(1);
		}

		List<DBObject> dbObject = new ArrayList<DBObject>();
		InputStreamReader read = null;
		BufferedReader buf = null;
		String value = null;
		StringTokenizer stk = null;
		long pid, aid, x, y;
		double lat, lon, length;
		long wayid;
		Map<Long, List<Long>> pEdge = new HashMap<Long, List<Long>>();		
		// 向数据库插入边的信息
		dbObject.clear();
		read = new InputStreamReader(new FileInputStream(farc));
		buf = new BufferedReader(read);
		while ((value = buf.readLine()) != null) {
			stk = new StringTokenizer(value);
			aid = Long.parseLong(stk.nextToken());
			x = Long.parseLong(stk.nextToken());
			y = Long.parseLong(stk.nextToken());	
			length = Double.parseDouble(stk.nextToken());
			wayid = Long.parseLong(stk.nextToken());

			if (pEdge.containsKey(x)) {
				pEdge.get(x).add(aid);
			} else {
				List<Long> list = new ArrayList<Long>();
				list.add(aid);
				pEdge.put(x, list);
			}
			if (pEdge.containsKey(y)) {
				pEdge.get(y).add(aid);
			} else {
				List<Long> list = new ArrayList<Long>();
				list.add(aid);
				pEdge.put(y, list);
			}
			DBObject loc = new BasicDBObject();
			Map<String, Long> mapObject = new HashMap<String, Long>();	
			mapObject.put("x", x);
			mapObject.put("y", y);
			loc.put("_id", aid);
			loc.put("gis", mapObject);
			loc.put("length", length);
			loc.put("wayid",wayid);			//新增:道路ID

			dbObject.add(loc);
		}
		mongo.insertBatch("mapArc", dbObject);
		// 向数据库插入点的信息
				dbObject.clear();
				read = new InputStreamReader(new FileInputStream(fmap));
				buf = new BufferedReader(read);
				while ((value = buf.readLine()) != null) {
					stk = new StringTokenizer(value);
					pid = Long.parseLong(stk.nextToken());
					lat = Double.parseDouble(stk.nextToken());
					lon = Double.parseDouble(stk.nextToken());
					
			    	DBObject loc = new BasicDBObject();
					Map<String, Double> mapObject = new LinkedHashMap<String, Double>();
					mapObject.put("lat", lat);
					mapObject.put("lon", lon);
					List<Long> list = pEdge.get(pid);
					loc.put("_id", pid);
					loc.put("gis", mapObject);
					loc.put("edge", list);
					dbObject.add(loc);
				}
				mongo.insertBatch("mapPoint", dbObject);
		db.getCollection("mapPoint").ensureIndex(new BasicDBObject("gis","2d"));
		buf.close();
		connection.close();
		System.out.println("插入完毕");
	}
}
