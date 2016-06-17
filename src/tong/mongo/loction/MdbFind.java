package tong.mongo.loction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import tong.map.MapProcess.MapData;
import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Car;
import tong.mongo.defclass.Line;
import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Node;
import tong.mongo.defclass.Output;
import tong.mongo.defclass.Point;

import com.defcons.MyCons;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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
	public static boolean PrintDriveOrbit= false;
	public static double Talength=0.0;
	public static double Gpslength=0.0;
	public static double LCSlength=0.0;
	public static Output PreciseOut; //输出precise到文件
	public static Output RecallOut;
	public static Output diserrorOut;
	public static HashMap<Long,Line> DriveMap;//用于统计测试数据行驶的弧段
	final static int ONCE_NUM = 40; //一次给点的数量	
	static String outfilename  = 
			MyCons.CarfileDir+"road_poi//JSON_roadinfo_"+filename;
	//---------------------------------------------------------------//	
	static Vector<Node> allstreetSet = new Vector<Node>();	//道路id集合
	static Vector<Node> GpsstreetSet = new Vector<Node>();  //真实道路id集合
	static Mongo connection = null;
	static DB db = null;
	static DBCollection dbcoll = null;
	static DBCollection coll = null;	

	public static void Initialize(int index){//初始化
		INDEX = index;
		filename = MyCons.arr_filename[INDEX];
		outfilename = MyCons.CarfileDir+"road_poi//JSON_roadinfo_"+filename;
		allstreetSet = new Vector<Node>();	//道路id集合
		GpsstreetSet = new Vector<Node>();  //真实道路集合
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
		Output outer = new Output();
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
		if(PrintDriveOrbit){
			MapData.getMap(mycar.GpsSet,db,0.5);
			return;
		}
		CarLocate(mycar);
		//输出过滤之后的street中点信息
		
		allstreetSet=outLineWithFilter(outer,allstreetSet,!GPSOUTPUT);//后处理过后的集合 
		GpsstreetSet=outLineWithFilter(outer,GpsstreetSet,GPSOUTPUT);//对Gps匹配结果进行后处理(认为是准确的轨迹)
		//点过滤之后再进行误差估计
		//ErrorEstimate.DisError(GpsstreetSet, GpsstreetSet);
		ErrorEstimate.DisError(allstreetSet,GpsstreetSet,mycar,db);//TA匹配结果与Gps匹配结果的相同时间点的距离误差
		ErrorEstimate.TaPrecise(allstreetSet,GpsstreetSet,db);//进行误差估计

		connection.close();
		outer.closelink();
		OutputToFile.init();
		OutputToFile.outToJSFileRun();//将道路结果输出并转换格式用于描点
		OutputToFile.outputGpsFile(filename); //将准确GPS路径输出到前端文件
		
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
				MapLoc mLoc = MapData.getMap(currcar.GpsSet,db,0.3);//获取地图
				Vector<Node> street = new Vector<Node>();//TA匹配结果	
				Estimate.StreetEstimate(mLoc, currcar, street, preline,isGPS); //调用匹配算法
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
				MapLoc mLoc = MapData.getMap(currcar.GpsSet,db,0.3);//获取地图
				Vector<Node> street = new Vector<Node>();//TA匹配结果
				Vector<Node> GpsStreet = new Vector<Node>();//Gps匹配结果
				if(ERRORESTIMATE)
					Estimate.StreetEstimate(mLoc,currcar, street,GpsStreet,preline);
				else
					Estimate.StreetEstimate(mLoc, currcar, street, preline,USEGPS); //调用匹配算法
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
	public static Vector<Node> outLineWithFilter(Output outer,Vector<Node> Tastreet,boolean UseOut) throws IOException{
		Vector<Node> LpSet=new Vector<Node>();		
		PostProcess postprocess=new PostProcess();
		LpSet=Tastreet;
		LpSet=postprocess.removeLoops(Tastreet);//进行一次过滤
		LpSet=postprocess.Interpolation(LpSet);//补点
		LpSet=postprocess.removeLoops(LpSet);//再进行一次过滤
		//输出结果到文件中
		if(UseOut){
			int size=LpSet.size();
			for(int i=0;i<size;i++){
				String currstr = outer.getStrMid(LpSet.get(i).po.x, LpSet.get(i).po.y,LpSet.get(i).lineid);
				outer.outputToFile(currstr); //输出经纬度
			}
		}
		return LpSet;//返回最后的匹配结果
	}
}
