package tong.mongo.loction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.defcons.MyCons;

import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Car;
import tong.mongo.defclass.Point;
import tong.mongo.defclass.sector;


//生成点的角度
public class CarInformation {

	static int INTERVAL_NUM =  MdbFind.INTERVALNUM;	 //采样密度
	final static double COEFFICIENT = 78.0/16.0; //ta值乘的系数
	final static int DISTANCE = MdbFind.DISTANCE_DIFF ; //距离误差阈值
	final static String regEx ="[^0-9.\\+\\-\\sE]"; 
	final static Pattern p = Pattern.compile(regEx); 
	public Algorithm Alg;
	static String filename = MdbFind.filename; ////文件名////////
	
	public static void main(String[] args) throws IOException, SQLException, ParseException {
		
		CarInformation caa = new CarInformation() ;
		//存放所有的基站位置map（不含diff为1.0e8这种情况）
		Map<String , double[]> map_lteloc = new HashMap<String , double[]>();
		//获取基站位置
		caa.getUniqLTELoc(filename,map_lteloc);	
		//执行函数
		@SuppressWarnings("unused")
		Car mycar ;
		mycar = caa.readFileSolution(filename,map_lteloc,false);
	}

	//获取唯一的基站位置
	public void getUniqLTELoc(String filename,Map<String ,double[]> map ) throws NumberFormatException, IOException{
		//输入文件-基站位置文件
		String solufile = MyCons.CarfileDir+"solu//solu_"+filename;
		//初始化基站位置文件reader
		FileReader fr_solu = new FileReader(solufile);
		BufferedReader br_solu = new BufferedReader(fr_solu);
		String line_solu = null;
		String[] arrs ;
		
		while((line_solu = br_solu.readLine())!=null){
			Matcher m = p.matcher(line_solu);
        	arrs=m.replaceAll("").trim().split("\\s");
        	if(arrs[3].equals("1.0E8")){
        		//System.out.println(Double.parseDouble(arrs[3])+" "+arrs[0]);
        		continue;
        	}
        	if(map.get(arrs[0])==null){
        		double[] temp = new double[3];
        		temp[0] = Double.valueOf(arrs[1]);
        		temp[1] = Double.valueOf(arrs[2]);
        		temp[2] = Double.valueOf(arrs[3]);        		
        		map.put(arrs[0], temp);
        	}else{
        		double prediff = map.get(arrs[0])[2];
        		if(prediff>Double.parseDouble(arrs[3])){
        			double[] temp = new double[3];
            		temp[0] = Double.valueOf(arrs[1]);
            		temp[1] = Double.valueOf(arrs[2]);
            		temp[2] = Double.valueOf(arrs[3]);        		
            		map.put(arrs[0], temp);
        		} 
        	}
		}	
		br_solu.close();
		fr_solu.close();
	}
	
	public Car PointChoose(Car car,boolean isGPS){//适当选取定位点 （如基站切换点和Ta变换点）
		Car resultCar = new Car();
		int n=car.PointNum,nowid=0,num=0,total=0;
		assert(car.PointNum==car.PointSet.size());
		for(int id=0;id<n-1;id++){
			Point now=car.getAnchorPoint(id).getPoint(0);
			Point next=car.getAnchorPoint(id+1).getPoint(0);//下一个点
//			if(isGPS){//如果是选取GPS点，则不需要考虑基站切换或者TA变化
//				if(id%INTERVAL_NUM==0){
//					resultCar.addPointAll(car.getAnchorPoint(id), car.getGpsPoint(id), car.getPci(id),car.getTime(id));
//				}
//				continue;
//			}
			double Ta1 =car.getTa(id);
			double Ta2 =car.getTa(id+1);
			if(!Alg.compare(now, next)){//表示这是一个切换点(特殊点)
				//首先判断是否时间间隔太大
				if(car.getTime(id+1)-car.getTime(id)<=2){//时间间隔相差2s以内认为是切换点（有效）
					AnchorPoint Po = new AnchorPoint(1);//切换点（特殊）
					sector sec1 = car.getSector(id);
					sector sec2 = car.getSector(id+1); 
					Po.addPoint(now,Ta1,sec1);
					Po.addPoint(next,Ta2,sec2);
					resultCar.addPointAll(Po, car.getGpsPoint(id),car.getPci(id),car.getTime(id));
					//这里已经取了这个点，所以要将下面的工作
					nowid=0;
//					id++;
//					nowid=1;
//					continue;
				}
			}
//			if(Alg.Fabs(Ta1-Ta2)>50.0){//表示这是一个Ta变化点
//				AnchorPoint Po = new AnchorPoint();
//				double distance=Alg.Distance(car.getAnchorPoint(id+1).getPoint(0), car.getGpsPoint(id+1));
//				if(Ta1<Ta2){//Ta从小变大
//					Po.type = 2;
//					if(distance < Ta2)
//						num++;
//					else{
//						nowid++;
//						continue;
//					}	
//				}
//				else{//Ta从大变小
//					Po.type = 3;
//					if(distance > Ta2)
//						num++;
//					else{
//						nowid++;
//						continue;
//					}
//				}
//				//Po.addPoint(now, Ta1, car.getSector(id));
//				//resultCar.addPointAll(Po, car.getGpsPoint(id), car.getPci(id), car.getTime(id));
////				System.out.println("preTA = "+car.getTa(id)+" nextTa = "+car.getTa(id+1)
////						+" predis = "+Alg.Distance(car.getAnchorPoint(id).getPoint(0), car.getGpsPoint(id))
////						+" nextdis = "+Alg.Distance(car.getAnchorPoint(id+1).getPoint(0), car.getGpsPoint(id+1))+" time = "+car.getTime(id+1));
//				
//				total++;
//				//id++;
//				Po.addPoint(next, Ta2, car.getSector(id+1));//取Ta值变化的那个点
//				resultCar.addPointAll(Po, car.getGpsPoint(id+1), car.getPci(id+1), car.getTime(id+1));
//				nowid=0;
//			}
			else if(nowid%INTERVAL_NUM==0){//普通定位点
				resultCar.addPointAll(car.getAnchorPoint(id), car.getGpsPoint(id), car.getPci(id),car.TimeSet.get(id));
			}
			nowid++;
		}
		//System.out.println("满足Ta变化规律的点 = "+num+" out of"+total);
		return resultCar;
	}
	
	//得到一条完整的车的轨迹
	public Car readFileSolution(String filename,Map<String , double[]> map_lteloc,boolean trueTa) throws IOException, SQLException, ParseException{
		
		//输入文件-点的位置文件
		String pointfile = MyCons.CarfileDir+"MyJson//"+filename;	
		//初始化点的位置文件reader
		FileReader fr_poi = new FileReader(pointfile);
		BufferedReader br_poi = new BufferedReader(fr_poi);
		String line_poi = null;
		Alg =new Algorithm();
		String[] arrs_poi ;
		int totalnum = 0,carnum=0;
		Car mycar ;
		Vector<Vector<Long>> legalline = new Vector<Vector<Long>>();//候选集
		Vector<AnchorPoint> PointSet = new Vector<AnchorPoint>();//基站坐标
		Vector<Point> GpsSet = new Vector<Point>();//GPS 坐标
		Vector<Long> PciSet = new Vector<Long>();// 基站id
		Vector<Integer> TimeSet = new Vector<Integer>();//时间值		
		
		while((line_poi = br_poi.readLine())!=null){//得到车辆所有点
			//System.out.println(line_poi);
			Matcher m_poi = p.matcher(line_poi);
			arrs_poi= m_poi.replaceAll("").trim().split("\\s");
			if(arrs_poi.length<6)
				continue;
			String pci = arrs_poi[2];
			double[] currlteloc = map_lteloc.get(pci);  //获得基站坐标
			if(currlteloc==null) 
				continue;
			
			double lng_a = currlteloc[0];
			double lat_a = currlteloc[1];
			double lng_b = Double.valueOf(arrs_poi[0]);
			double lat_b = Double.valueOf(arrs_poi[1]);
			
			totalnum++;
			Algorithm Alg=new Algorithm();
			
			//如果真实距离和TA值算的距离差距太大，排除这个点
			if(Math.abs(Alg.Distance( lat_a,lng_a, lat_b, lng_b)-Double.valueOf(arrs_poi[5])*78.0/16) > DISTANCE){
				continue;
			}
			//去除无效基站坐标点（误差过大的基站坐标）
			double dddis = Alg.Distance( lat_a,lng_a, lat_b, lng_b); //真实距离
			/////////计算并输出输出角度//////////
			double currAzimuth = Alg.gps2d(lat_a,lng_a,lat_b,lng_b);
			//legalline.add(null);//候选集(未设定集体值，待定)
			
			double TimeofArrival=0.0;
			if(trueTa){//使用真实Ta值
				TimeofArrival = dddis;
			}
			else{//使用自己测量的Ta值
				TimeofArrival = Double.valueOf(arrs_poi[5])*COEFFICIENT;
			}				
			AnchorPoint nowpoint = new AnchorPoint(0);//普通点
			nowpoint.addPoint(new Point(currlteloc[1],currlteloc[0]), TimeofArrival, new sector((currAzimuth+300)%360,(currAzimuth+60)%360));//定位点坐标
			GpsSet.add(new Point(lat_b,lng_b));//GPS坐标
			PciSet.add(Long.parseLong(pci));//基站ID
			PointSet.add(nowpoint);
			TimeSet.add(Integer.valueOf(arrs_poi[6]));//时间值 		
			carnum ++ ;
		}
		//
		////////////////////////////得到car的所有定位点//////////////////////////////////
		//System.out.println("总共的数量："+totalnum);
		mycar = new Car(carnum,legalline,PointSet,GpsSet,PciSet,TimeSet);
		br_poi.close();
		fr_poi.close();
		return mycar;
	}
	//获取过滤后的车，并且选取适当的定位点
	public Car getCar(Car mycar,int interval,boolean isGPS){
		Car resultcar = mycar;
		Alg = new Algorithm();
		INTERVAL_NUM=interval;
		PointFilter Filter = new PointFilter();//过滤算法类
		if(!isGPS)
			System.out.println("过滤前点的数量"+mycar.PointNum);
		resultcar = Filter.ErrorFilter(mycar);//过滤误差点
		if(!isGPS)
			System.out.println("过滤后点的数量"+resultcar.PointNum);
		resultcar = PointChoose(resultcar,isGPS);//选取适当的定位点
		if(!isGPS)
			System.out.println("选取点后点的数量:"+resultcar.PointNum);
		return resultcar;
	}
}

