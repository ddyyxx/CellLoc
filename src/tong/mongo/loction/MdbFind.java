package tong.mongo.loction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Car;
import tong.mongo.defclass.Line;
import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Node;
import tong.mongo.defclass.Point;

import com.defcons.MyCons;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

//main函数所在程序
/**
 * 1.读取数据库，获取车辆信息（id 基站经纬度 时间）
 * 2.计算
 * 3.输出结果到显示文件
*/
public class MdbFind {
	//---------------------------参数设置-----------------------------//	
	public static int INDEX = 0; //指定当前选定的文件名数组的下标(每次修改这里即可)
	public static String filename = MyCons.arr_filename[INDEX]; //获取文件名
	public static boolean TRUETA=false; //true: 用真实TA进行数据匹配  false：用测量TA值进行匹配
	public static boolean USEGPS=false;//true： 用GPS数据进行匹配 false：用TA值进行匹配
	public static boolean ERRORESTIMATE = true;// true: 进行误差分析，求GPS轨迹和TA轨迹进行对比 false: 仅求GPS或TA轨迹 
	public final static int INTERVALNUM = 5;	 //采样密度(每几个点取1个点)
	public static boolean DEBUG = false; //是否显示调试信息
	public final static int DISTANCE_DIFF = 100000;//Integer.MAX_VALUE; //误差
	public static int startpoint = 0;//调试起点
	public static int endpoint = 100; //调试终点
	public static boolean GPSOUTPUT = false; // true： 显示GPS结果 false 显示Ta匹配结果
	private static Algorithm Alg;
	public static double Talength=0.0;
	public static double Gpslength=0.0;
	public static double LCSlength=0.0;
	public static OutputFile PreciseOut;
	public static OutputFile RecallOut;
	public static OutputFile diserrorOut;
	final static int ONCE_NUM = 100; //一次给点的数量	
	static String outfilename  = 
			MyCons.CarfileDir+"road_poi//JSON_roadinfo_"+filename;
	//---------------------------------------------------------------//	
	static Vector<Node> allstreetSet = new Vector<Node>();	//道路id集合
	static Vector<Node> GpsstreetSet = new Vector<Node>();  //真实道路id集合
	static Mongo connection = null;
	static DB db = null;
	static DBCollection dbcoll = null;
	static DBCollection coll = null;	
	
	public static void main(String[] args) throws IOException, SQLException, ParseException {
		//什么也不做
	}
	public static void Initialize(int index){//初始化
		INDEX = index;
		filename = MyCons.arr_filename[INDEX];
		outfilename = MyCons.CarfileDir+"road_poi//JSON_roadinfo_"+filename;
		allstreetSet = new Vector<Node>();	//道路id集合
		GpsstreetSet = new Vector<Node>();  //真实道路集合
		Alg = new Algorithm();
		connection = null;
		db = null;
		dbcoll = null;
		coll = null;
	}
	@SuppressWarnings({ "deprecation" })
	public static void StartWork(int index)throws IOException, SQLException, ParseException {//进行一次匹配
		
		Initialize(index);//初始化数据
		System.out.println("\n地图数据 "+index);
		//初始化文件输出对象
		OutputFile outer = new OutputFile();
		outer.init(outfilename);
		//数据库连接
		connection = new Mongo("127.0.0.1:27017");
		db=connection.getDB("MapLoc");
		long start = System.currentTimeMillis();//得到开始时间(用于时间消耗统计)
		
		//////////////存放所有的基站位置map（不含diff为1.0e8这种情况）////////////
		Map<String , double[]> map_lteloc = new HashMap<String , double[]>();
		//获取基站位置
		//filename的形式为"Zhengye_DriveTesting_08-24.17-58";
		CarInformation carAzi = new CarInformation();
		carAzi.getUniqLTELoc(filename,map_lteloc);

		//得到车的轨迹
		Car mycar ;
		mycar = carAzi.readFileSolution(filename,map_lteloc,TRUETA);//得到完整的车的轨迹
		CarLocate(mycar);
		//输出过滤之后的street中点信息
		
		allstreetSet=outLineWithFilter(outer,allstreetSet,!GPSOUTPUT);//后处理过后的集合 
		GpsstreetSet=outLineWithFilter(outer,GpsstreetSet,GPSOUTPUT);//对Gps匹配结果进行后处理(认为是准确的轨迹)
		//点过滤之后再进行误差估计
		//ErrorEstimate.DisError(GpsstreetSet, GpsstreetSet);//TA匹配结果与Gps匹配结果的相同时间点的距离误差
		ErrorEstimate.DisError(allstreetSet,mycar);
		ErrorEstimate.TaPrecise(allstreetSet,GpsstreetSet,db);//进行误差估计

		connection.close();
		outer.closelink();
		OutGps.init();
		OutGps.outToJSFileRun();//将道路结果输出并转换格式用于描点
		OutGps.outputGpsFile(filename); //将准确GPS路径输出到前端文件
		
		//计算耗时
		long end = System.currentTimeMillis();
		System.out.println("用时："+(end-start));
	}
	
	static void getpath(Car mycar,Vector<Node> Result,boolean isGPS){
		// 对一辆车进行运算 TA
		int experiment=0;//试验用
		long preline=-1;//上一次匹配的结果弧段
		///////////////给一组car赋值(一组为30个)///////////////////////
		System.out.println("pointnum = "+mycar.PointNum);
		for(int count = 0 ; count < mycar.PointNum ; count+=ONCE_NUM){
			experiment++;
			if(experiment>=startpoint&&experiment<=endpoint){//如果在调试区间内，则进行计算
				int mycurrnum = 0;
				Vector<Vector<Long>> legalline = new Vector<Vector<Long>>();//候选集
				Vector<AnchorPoint> PointSet = new Vector<AnchorPoint>();//基站坐标
				Vector<Point> GpsSet = new Vector<Point>();//GPS坐标
				Vector<Long> PciSet = new Vector<Long>();
				Vector<Integer> TimeSet = new Vector<Integer>();//时间值		
				for(int i = count ; i< count+ONCE_NUM && i< mycar.PointNum; i++){
					PointSet.add(mycar.getAnchorPoint(i));//得到基站坐标
					PciSet.add(mycar.getPci(i));//加入基站ID
					GpsSet.add(mycar.getGpsPoint(i));//得到GPS坐标
					TimeSet.add(mycar.getTime(i));//加入时间
					mycurrnum ++;
				}	
				Car currcar = new Car(mycurrnum,legalline,PointSet,GpsSet,PciSet,TimeSet); //30个点车
				///////////////////////////////////////////////////////////////
				//以下取对应轨迹的地图数据
				MapLoc mLoc = getMap(currcar.GpsSet,db,0.3);//获取地图
				Estimate est = new Estimate();//匹配函数
				Vector<Node> street = new Vector<Node>();//TA匹配结果	
				est.StreetEstimate(mLoc, currcar, street, preline,isGPS); //调用匹配算法
				////////////////将上一次匹配的最后一条弧段当做下一次匹配的第一个匹配弧/////////////////////////
				if(street.size()>0){
					preline = street.get(street.size()-1).lineid;
				}
				else
					preline = -1;
				if(DEBUG){
					currcar.debug();
					System.out.println("preline = "+preline);
				}
				Result.addAll(street); //加入street
				if(street.size()==0&&!isGPS){ //如果当前没有匹配到结果，则还需要进一步处理
					System.out.println("没有匹配到street!!!!!!!!!!!"+" experiment = "+experiment);
					currcar.debug();
				}
			}
		}
	}
	public static void CarLocate(Car mycar){ // 返回车的定位轨迹坐标
		CarInformation carAzi = new CarInformation();
//		if(ERRORESTIMATE){//如果需要进行误差评价
//			Car GpsCar = carAzi.getCar(mycar,5,true);//这里取得是GPS定位的点（认为这个匹配轨迹是车真实的轨迹，试验用）
//			getpath(GpsCar,GpsstreetSet,true);//计算真实路径
//		}
//		mycar = carAzi.getCar(mycar,INTERVALNUM,USEGPS); //误差过滤后并且根据具体情况选取点（TA和GPS各不相同）
//		getpath(mycar,allstreetSet,USEGPS);
		
		mycar = carAzi.getCar(mycar,INTERVALNUM,USEGPS); //误差过滤后并且根据具体情况选取点（TA和GPS各不相同）
		int experiment=0;//试验用
		long preline=-1;//上一次匹配的结果弧段
		///////////////给一组car赋值(一组为30个)///////////////////////
		System.out.println("pointnum = "+mycar.PointNum);
		for(int count = 0 ; count < mycar.PointNum ; count+=ONCE_NUM){
			experiment++;
			if(experiment>=startpoint&&experiment<=endpoint){//如果在调试区间内，则进行计算
				int mycurrnum = 0;
				Vector<Vector<Long>> legalline = new Vector<Vector<Long>>();//候选集
				Vector<AnchorPoint> PointSet = new Vector<AnchorPoint>();//基站坐标
				Vector<Point> GpsSet = new Vector<Point>();//GPS坐标
				Vector<Long> PciSet = new Vector<Long>();
				Vector<Integer> TimeSet = new Vector<Integer>();//时间值		
				for(int i = count ; i< count+ONCE_NUM && i< mycar.PointNum; i++){
					PointSet.add(mycar.getAnchorPoint(i));//得到基站坐标
					PciSet.add(mycar.getPci(i));//加入基站ID
					GpsSet.add(mycar.getGpsPoint(i));//得到GPS坐标
					TimeSet.add(mycar.getTime(i));//加入时间
					mycurrnum ++;
				}	
				Car currcar = new Car(mycurrnum,legalline,PointSet,GpsSet,PciSet,TimeSet); //30个点车
				///////////////////////////////////////////////////////////////
				//以下取对应轨迹的地图数据
				MapLoc mLoc = getMap(currcar.GpsSet,db,0.3);//获取地图
				Estimate est = new Estimate();//匹配函数
				Vector<Node> street = new Vector<Node>();//TA匹配结果
				Vector<Node> GpsStreet = new Vector<Node>();//Gps匹配结果
				if(ERRORESTIMATE)
					est.StreetEstimate(mLoc,currcar, street,GpsStreet,preline);
				else
					est.StreetEstimate(mLoc, currcar, street, preline,USEGPS); //调用匹配算法
				////////////////将上一次匹配的最后一条弧段当做下一次匹配的第一个匹配弧/////////////////////////
				if(street.size()>0){
					preline = street.get(street.size()-1).lineid;
				}
				else
					preline = -1;
				if(DEBUG){
					currcar.debug();
					System.out.println("preline = "+preline);
				}
				allstreetSet.addAll(street); //加入street
				GpsstreetSet.addAll(GpsStreet);
				if(street.size()==0){ //如果当前没有匹配到结果，则还需要进一步处理
					//System.out.println("没有匹配到street!!!!!!!!!!!"+" experiment = "+experiment);
					//currcar.debug();
				}
			}
		}
	}
	
	//-------------加上过滤器,输出过滤之后的道路中点--------------//
	public static Vector<Node> outLineWithFilter(OutputFile outer,Vector<Node> Tastreet,boolean UseOut) throws IOException{
		Vector<Node> LpSet=new Vector<Node>();		
		PointFilter filter=new PointFilter();
		ArcInterpolation interpolation =new ArcInterpolation();
		LpSet=Tastreet;
		LpSet=filter.Filter(Tastreet);//进行一次过滤
		LpSet=interpolation.Interpolation(LpSet);//补点
		LpSet=filter.Filter(LpSet);//再进行一次过滤
		//输出结果到文件中
		if(UseOut){
			int size=LpSet.size();
			for(int i=0;i<size;i++){
//				if(LpSet.get(i).time==-1)
//					continue;
				String currstr = outer.getStrMid(LpSet.get(i).po.x, LpSet.get(i).po.y,LpSet.get(i).lineid);
				outer.outputToFile(currstr); //输出经纬度
			}
		}
		return LpSet;//返回最后的匹配结果
	}
	
	//-------------------获取点集合周围的道路点和弧-------------------------//
	@SuppressWarnings("unchecked")
	public static MapLoc getMap(Vector<Point> pointSet, DB db ,double radius){ //调用的时候再加上一个db即可
		MapLoc mLoc = new MapLoc(); //存放搜索的结果
		HashMap<Long, Point> pMap = new HashMap<Long, Point>(); //存放点集合
		HashMap<Long, Line> lMap = new HashMap<Long, Line>();	//存放弧集合
		List<Long> aList = new LinkedList<Long>();
		DBCollection coll = null;
		DBObject object = null; //数据库的搜索条件
		boolean isFirstPoint = true;
		double prelat=0,prelng=0;//前一个点的Gps坐标
		for(Point currpoi : pointSet){ //如果只有一个点，就按照默认半径进行取点
			double latt = currpoi.x;
			double lngg = currpoi.y;
			if(!isFirstPoint){ //如果不是第一个点, 取两相邻点的中点，以两相邻点距离的二分之一为半径
				latt=(latt+prelat)/2;
				lngg=(lngg+prelng)/2;
				radius = (Alg.Distance(latt, lngg, prelat, prelng)+300.0)/1000.0;
			}
			prelat=latt;
			prelng=lngg;
			isFirstPoint = false;
			coll = db.getCollection("mapPoint"); //获取点集合
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
					Map<String, Double> m = (Map<String, Double>) result.get("gis");
					List<Long> list = (List<Long>) result.get("edge");
					Vector<Long> vtor = new Vector<Long>();
					vtor.addAll(list);
					p.id = (Long) result.get("_id");
					p.x = m.get("lat");
					p.y = m.get("lon");
					pMap.put(key, p);
	
					for (long v : vtor) { //将包含当前点的弧的ID放进list
						if (!aList.contains(v))
							aList.add(v);
					}
				}
			}//END WHILE
			// 找出一辆车行驶线路范围内的线段
			coll = db.getCollection("mapArc");
			cursor = coll.find(new BasicDBObject("_id", new BasicDBObject("$in", aList)));
	
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
					if (pMap.containsKey(l.pid[0])&& pMap.containsKey(l.pid[1])) {
						l.p[0] = pMap.get(l.pid[0]);
						l.p[1] = pMap.get(l.pid[1]);
						lMap.put(key, l);
					}
				}
			}//END WHILE
		}
		mLoc.PointNum = pMap.size();
		mLoc.LineNum = lMap.size();
		mLoc.PointSet = pMap;
		mLoc.LineSet = lMap;
		return mLoc;		
	}
}
