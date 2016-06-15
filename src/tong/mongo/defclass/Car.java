package tong.mongo.defclass;

import java.util.Vector;

import tong.mongo.loction.Algorithm;

//车辆信息
public class Car{
	public Car(){
		PointNum=0;
		PointSet=new Vector<AnchorPoint>();
		GpsSet=new Vector<Point>();
		TimeSet=new Vector<Integer>();
		PciSet = new Vector<Long>();
		legalline=new Vector<Vector<Long>>();
	}
	
	public void addPointAll(AnchorPoint Tap,Point Gpsp,long pciid,int time){
		PointSet.add(Tap);
		GpsSet.add(Gpsp);
		PciSet.add(pciid);
		TimeSet.add(time);
		PointNum++;
	}
	
	public void addCar(Car car){//合并两辆车轨迹序列
		PointNum+=car.PointNum;
		PointSet.addAll(car.PointSet);
		GpsSet.addAll(car.GpsSet);
		PciSet.addAll(car.PciSet);
		TimeSet.addAll(car.TimeSet);
		legalline.addAll(car.legalline);
	}
	
	public void addGpsPoint(Point p){
		this.GpsSet.add(p);
		PointNum++;
	}
	
	public void addTime(int time){
		this.TimeSet.add(time);
	}
	
	public double Rad(double d) {
		double PI =3.141592653589793238462643383279502884;
	    return d * PI / 180.0;
	}
	
	public double Distance(double lat1, double lng1, double lat2,double lng2) {
		double radLat1 = Rad(lat1);
		double radLat2 = Rad(lat2);
		double a = radLat1 - radLat2;
		double b = Rad(lng1) - Rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * 6378137.0;
		//s = Math.round(s * 10000) / 10000;
		return s;
	}
	
	public double Distance(Point p1,Point p2){
	    double lat1=p1.x, lng1=p1.y,lat2=p2.x,lng2=p2.y;
	    return Distance(lat1,lng1,lat2,lng2);
	}
	public double gps2d(double lat_a, double lng_a, double lat_b, double lng_b) {
		double d = 0;
		lat_a = lat_a * Math.PI / 180;
		lng_a = lng_a * Math.PI / 180;
		lat_b = lat_b * Math.PI / 180;
		lng_b = lng_b * Math.PI / 180;
		d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a)
				* Math.cos(lat_b) * Math.cos(lng_b - lng_a);
		d = Math.sqrt(1 - d * d);
		d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;
		d = Math.asin(d) * 180 / Math.PI;
		// d = Math.round(d*10000);
		if(lat_b-lat_a<0)
			d = 180-d;
		if(d<0) d = 360+d; 	
		// 变换 由原来的初始位置为北方改为初始位置为东方，并且逆时针改为正方向
		d = (90-d+360)%360;
		return d;
	}
	
	public double getAngel(Point p1,Point p2){//返回p2相对于p1的角度
		return gps2d(p1.x,p1.y,p2.x,p2.y);
	}
	public void debug(){
		System.out.println("PointNum = "+PointNum);
		for(int i=0;i<PointNum;i++){
			//sector sec = this.getSector(i);
			System.out.println("PCI = "+this.getPci(i)+" TA = "+this.getTa(i)+" "+
					" dis = "+Distance(this.getGpsPoint(i),this.getAnchorPoint(i).getPoint(0))+" trueAngle = "+
					getAngel(this.getAnchorPoint(i).getPoint(0),this.getGpsPoint(i))+" Time = "+this.getTime(i));
		}
	}
	
	public void printlegalline(MapLoc mymp,int id){ // 输出一个车定位点的候选弧集合
		Vector<Long> vec = legalline.get(id);
		int n = vec.size();
		for(int i=0;i<n;i++){
			long lid = vec.get(i);
			Line nowline=mymp.getLine(lid);
			Algorithm Alg = new Algorithm();
			Point po = getAnchorPoint(id).getPoint(0);
			System.out.print(i+" lineid = "+vec.get(i));
			System.out.print(" st = "+Alg.getAngel(po, nowline.p[0])+" ed ="+Alg.getAngel(po, nowline.p[1]));
			System.out.println(" dist = "+Distance(po,Alg.MinDistPtoLine(po,nowline)));
		}
	}
	
	public Point getGpsPoint(int id){//返回第i个点(GPS)
		return GpsSet.get(id);
	}
	
	public AnchorPoint getAnchorPoint(int id){//返回第i个基站定位点
		return PointSet.get(id);
	}
	
	public int getTime(int id){//返回时间
		return TimeSet.get(id);
	}
	
	public double getTa(int id){ //返回Ta值
		return PointSet.get(id).getTa(0);
	}
	
	public sector getSector(int id){
		return PointSet.get(id).getSector(0);
	}
	
	public long getPci(int id){
		return PciSet.get(id);
	}
	
	//============改了！！！新的构造函数
	public Car(int pointNum, Vector<Vector<Long>> legalline,
			Vector<AnchorPoint> pointSet, Vector<Point> gpsSet, Vector<Long> pciset,Vector<Integer> timeSet) {
		this.PointNum = pointNum;
		this.legalline = legalline;
		this.PointSet = pointSet;
		this.GpsSet = gpsSet;
		this.PciSet = pciset;
		this.TimeSet = timeSet;
	}
	public int PointNum;
	public Vector<Vector<Long>> legalline;//候选集
	public Vector<Long> PciSet;// 基站ID集
	public Vector<AnchorPoint> PointSet;//定位点坐标
	public Vector<Point> GpsSet;//GPS 坐标
	public Vector<Integer> TimeSet;//时间值
}



