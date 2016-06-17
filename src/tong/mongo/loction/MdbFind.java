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

//main�������ڳ���
/**
 * 1.��ȡ���ݿ⣬��ȡ������Ϣ��id ��վ��γ�� ʱ�䣩
 * 2.����
 * 3.����������ʾ�ļ�
*/
public class MdbFind {
	//---------------------------��������-----------------------------//	
	public static int INDEX = 0; //ָ����ǰѡ�����ļ���������±�(ÿ���޸����Ｔ��)
	public static String filename = MyCons.arr_filename[INDEX]; //��ȡ�ļ���
	public static boolean TRUETA=false; //true: ����ʵTA��������ƥ��  false���ò���TAֵ����ƥ��
	public static boolean USEGPS=false;//true�� ��GPS���ݽ���ƥ�� false����TAֵ����ƥ��
	public static boolean ERRORESTIMATE = true;// true: ��������������GPS�켣��TA�켣���жԱ� false: ����GPS��TA�켣 
	public final static int INTERVALNUM = 5;	 //�����ܶ�(ÿ������ȡ1����)
	public static boolean DEBUG = false; //�Ƿ���ʾ������Ϣ
	public final static int DISTANCE_DIFF = 100000;//Integer.MAX_VALUE; //���
	public static int startpoint = 0;//�������
	public static int endpoint = 100; //�����յ�
	public static boolean GPSOUTPUT = false; // true�� ��ʾGPS��� false ��ʾTaƥ����
	public static boolean PrintDriveOrbit= false;
	public static double Talength=0.0;
	public static double Gpslength=0.0;
	public static double LCSlength=0.0;
	public static Output PreciseOut; //���precise���ļ�
	public static Output RecallOut;
	public static Output diserrorOut;
	public static HashMap<Long,Line> DriveMap;//����ͳ�Ʋ���������ʻ�Ļ���
	final static int ONCE_NUM = 40; //һ�θ��������	
	static String outfilename  = 
			MyCons.CarfileDir+"road_poi//JSON_roadinfo_"+filename;
	//---------------------------------------------------------------//	
	static Vector<Node> allstreetSet = new Vector<Node>();	//��·id����
	static Vector<Node> GpsstreetSet = new Vector<Node>();  //��ʵ��·id����
	static Mongo connection = null;
	static DB db = null;
	static DBCollection dbcoll = null;
	static DBCollection coll = null;	

	public static void Initialize(int index){//��ʼ��
		INDEX = index;
		filename = MyCons.arr_filename[INDEX];
		outfilename = MyCons.CarfileDir+"road_poi//JSON_roadinfo_"+filename;
		allstreetSet = new Vector<Node>();	//��·id����
		GpsstreetSet = new Vector<Node>();  //��ʵ��·����
		connection = null;
		db = null;
		dbcoll = null;
		coll = null;
	}
	@SuppressWarnings({ "deprecation" })
	public static void StartWork(int index)throws IOException, SQLException, ParseException {//����һ��ƥ��
		
		Initialize(index);//��ʼ������
		System.out.println("\n��ͼ���� "+index);
		//��ʼ���ļ��������
		Output outer = new Output();
		outer.init(outfilename);
		//���ݿ�����
		connection = new Mongo("127.0.0.1:27017");
		db=connection.getDB("MapLoc");
		long start = System.currentTimeMillis();//�õ���ʼʱ��(����ʱ������ͳ��)
		
		//////////////������еĻ�վλ��map������diffΪ1.0e8���������////////////
		Map<String , double[]> map_lteloc = new HashMap<String , double[]>();
		//��ȡ��վλ��
		//filename����ʽΪ"Zhengye_DriveTesting_08-24.17-58";
		CarInformation carAzi = new CarInformation();
		carAzi.getUniqLTELoc(filename,map_lteloc);

		//�õ����Ĺ켣
		Car mycar ;
		mycar = carAzi.readFileSolution(filename,map_lteloc,TRUETA);//�õ������ĳ��Ĺ켣
		if(PrintDriveOrbit){
			MapData.getMap(mycar.GpsSet,db,0.5);
			return;
		}
		CarLocate(mycar);
		//�������֮���street�е���Ϣ
		
		allstreetSet=outLineWithFilter(outer,allstreetSet,!GPSOUTPUT);//�������ļ��� 
		GpsstreetSet=outLineWithFilter(outer,GpsstreetSet,GPSOUTPUT);//��Gpsƥ�������к���(��Ϊ��׼ȷ�Ĺ켣)
		//�����֮���ٽ���������
		//ErrorEstimate.DisError(GpsstreetSet, GpsstreetSet);
		ErrorEstimate.DisError(allstreetSet,GpsstreetSet,mycar,db);//TAƥ������Gpsƥ��������ͬʱ���ľ������
		ErrorEstimate.TaPrecise(allstreetSet,GpsstreetSet,db);//����������

		connection.close();
		outer.closelink();
		OutputToFile.init();
		OutputToFile.outToJSFileRun();//����·��������ת����ʽ�������
		OutputToFile.outputGpsFile(filename); //��׼ȷGPS·�������ǰ���ļ�
		
		//�����ʱ
		long end = System.currentTimeMillis();
		System.out.println("��ʱ��"+(end-start));
	}
	
	static void getpath(Car mycar,Vector<Node> Result,boolean isGPS){
		// ��һ������������ TA
		int experiment=0;//������
		long preline=-1;//��һ��ƥ��Ľ������
		///////////////��һ��car��ֵ(һ��Ϊ30��)///////////////////////
		System.out.println("pointnum = "+mycar.PointNum);
		for(int count = 0 ; count < mycar.PointNum ; count+=ONCE_NUM){
			experiment++;
			if(experiment>=startpoint&&experiment<=endpoint){//����ڵ��������ڣ�����м���
				int mycurrnum = 0;
				Vector<Vector<Long>> legalline = new Vector<Vector<Long>>();//��ѡ��
				Vector<AnchorPoint> PointSet = new Vector<AnchorPoint>();//��վ����
				Vector<Point> GpsSet = new Vector<Point>();//GPS����
				Vector<Long> PciSet = new Vector<Long>();
				Vector<Integer> TimeSet = new Vector<Integer>();//ʱ��ֵ		
				for(int i = count ; i< count+ONCE_NUM && i< mycar.PointNum; i++){
					PointSet.add(mycar.getAnchorPoint(i));//�õ���վ����
					PciSet.add(mycar.getPci(i));//�����վID
					GpsSet.add(mycar.getGpsPoint(i));//�õ�GPS����
					TimeSet.add(mycar.getTime(i));//����ʱ��
					mycurrnum ++;
				}	
				Car currcar = new Car(mycurrnum,legalline,PointSet,GpsSet,PciSet,TimeSet); //30���㳵
				///////////////////////////////////////////////////////////////
				//����ȡ��Ӧ�켣�ĵ�ͼ����
				MapLoc mLoc = MapData.getMap(currcar.GpsSet,db,0.3);//��ȡ��ͼ
				Vector<Node> street = new Vector<Node>();//TAƥ����	
				Estimate.StreetEstimate(mLoc, currcar, street, preline,isGPS); //����ƥ���㷨
				////////////////����һ��ƥ������һ�����ε�����һ��ƥ��ĵ�һ��ƥ�仡/////////////////////////
				if(street.size()>0){
					preline = street.get(street.size()-1).lineid;
				}
				else
					preline = -1;
				if(DEBUG){
					currcar.debug();
					System.out.println("preline = "+preline);
				}
				Result.addAll(street); //����street
				if(street.size()==0&&!isGPS){ //�����ǰû��ƥ�䵽���������Ҫ��һ������
					System.out.println("û��ƥ�䵽street!!!!!!!!!!!"+" experiment = "+experiment);
					currcar.debug();
				}
			}
		}
	}
	public static void CarLocate(Car mycar){ // ���س��Ķ�λ�켣����
		CarInformation carAzi = new CarInformation();
//		if(ERRORESTIMATE){//�����Ҫ�����������
//			Car GpsCar = carAzi.getCar(mycar,5,true);//����ȡ����GPS��λ�ĵ㣨��Ϊ���ƥ��켣�ǳ���ʵ�Ĺ켣�������ã�
//			getpath(GpsCar,GpsstreetSet,true);//������ʵ·��
//		}
//		mycar = carAzi.getCar(mycar,INTERVALNUM,USEGPS); //�����˺��Ҹ��ݾ������ѡȡ�㣨TA��GPS������ͬ��
//		getpath(mycar,allstreetSet,USEGPS);
		
		mycar = carAzi.getCar(mycar,INTERVALNUM,USEGPS); //�����˺��Ҹ��ݾ������ѡȡ�㣨TA��GPS������ͬ��
		int experiment=0;//������
		long preline=-1;//��һ��ƥ��Ľ������
		///////////////��һ��car��ֵ(һ��Ϊ30��)///////////////////////
		System.out.println("pointnum = "+mycar.PointNum);
		for(int count = 0 ; count < mycar.PointNum ; count+=ONCE_NUM){
			experiment++;
			if(experiment>=startpoint&&experiment<=endpoint){//����ڵ��������ڣ�����м���
				int mycurrnum = 0;
				Vector<Vector<Long>> legalline = new Vector<Vector<Long>>();//��ѡ��
				Vector<AnchorPoint> PointSet = new Vector<AnchorPoint>();//��վ����
				Vector<Point> GpsSet = new Vector<Point>();//GPS����
				Vector<Long> PciSet = new Vector<Long>();
				Vector<Integer> TimeSet = new Vector<Integer>();//ʱ��ֵ		
				for(int i = count ; i< count+ONCE_NUM && i< mycar.PointNum; i++){
					PointSet.add(mycar.getAnchorPoint(i));//�õ���վ����
					PciSet.add(mycar.getPci(i));//�����վID
					GpsSet.add(mycar.getGpsPoint(i));//�õ�GPS����
					TimeSet.add(mycar.getTime(i));//����ʱ��
					mycurrnum ++;
				}	
				Car currcar = new Car(mycurrnum,legalline,PointSet,GpsSet,PciSet,TimeSet); //30���㳵
				///////////////////////////////////////////////////////////////
				//����ȡ��Ӧ�켣�ĵ�ͼ����
				MapLoc mLoc = MapData.getMap(currcar.GpsSet,db,0.3);//��ȡ��ͼ
				Vector<Node> street = new Vector<Node>();//TAƥ����
				Vector<Node> GpsStreet = new Vector<Node>();//Gpsƥ����
				if(ERRORESTIMATE)
					Estimate.StreetEstimate(mLoc,currcar, street,GpsStreet,preline);
				else
					Estimate.StreetEstimate(mLoc, currcar, street, preline,USEGPS); //����ƥ���㷨
				////////////////����һ��ƥ������һ�����ε�����һ��ƥ��ĵ�һ��ƥ�仡/////////////////////////
				if(street.size()>0){
					preline = street.get(street.size()-1).lineid;
				}
				else
					preline = -1;
				if(DEBUG){
					currcar.debug();
					System.out.println("preline = "+preline);
				}
				allstreetSet.addAll(street); //����street
				GpsstreetSet.addAll(GpsStreet);
				if(street.size()==0){ //�����ǰû��ƥ�䵽���������Ҫ��һ������
					//System.out.println("û��ƥ�䵽street!!!!!!!!!!!"+" experiment = "+experiment);
					//currcar.debug();
				}
			}
		}
	}
	
	//-------------���Ϲ�����,�������֮��ĵ�·�е�--------------//
	public static Vector<Node> outLineWithFilter(Output outer,Vector<Node> Tastreet,boolean UseOut) throws IOException{
		Vector<Node> LpSet=new Vector<Node>();		
		PostProcess postprocess=new PostProcess();
		LpSet=Tastreet;
		LpSet=postprocess.removeLoops(Tastreet);//����һ�ι���
		LpSet=postprocess.Interpolation(LpSet);//����
		LpSet=postprocess.removeLoops(LpSet);//�ٽ���һ�ι���
		//���������ļ���
		if(UseOut){
			int size=LpSet.size();
			for(int i=0;i<size;i++){
				String currstr = outer.getStrMid(LpSet.get(i).po.x, LpSet.get(i).po.y,LpSet.get(i).lineid);
				outer.outputToFile(currstr); //�����γ��
			}
		}
		return LpSet;//��������ƥ����
	}
}
