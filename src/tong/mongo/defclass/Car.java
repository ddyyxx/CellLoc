package tong.mongo.defclass;

import java.util.Vector;

import tong.mongo.loction.Algorithm;

//������Ϣ
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
	
	public void addCar(Car car){//�ϲ��������켣����
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
	
	
	public void debug(){
		System.out.println("PointNum = "+PointNum);
		for(int i=0;i<PointNum;i++){
			//sector sec = this.getSector(i);
			System.out.println("PCI = "+this.getPci(i)+" TA = "+this.getTa(i)+" "+
					" dis = "+Algorithm.Distance(this.getGpsPoint(i),this.getAnchorPoint(i).getPoint(0))+" trueAngle = "+
					Algorithm.getAngel(this.getAnchorPoint(i).getPoint(0),this.getGpsPoint(i))+" Time = "+this.getTime(i));
		}
	}
	
	public void printlegalline(MapLoc mymp,int id){ // ���һ������λ��ĺ�ѡ������
		Vector<Long> vec = legalline.get(id);
		int n = vec.size();
		for(int i=0;i<n;i++){
			long lid = vec.get(i);
			Line nowline=mymp.getLine(lid);
			Point po = getAnchorPoint(id).getPoint(0);
			System.out.print(i+" lineid = "+vec.get(i));
			System.out.print(" st = "+Algorithm.getAngel(po, nowline.p[0])+" ed ="+Algorithm.getAngel(po, nowline.p[1]));
			System.out.println(" dist = "+Algorithm.Distance(po,Algorithm.MinDistPtoLine(po,nowline)));
		}
	}
	
	public Point getGpsPoint(int id){//���ص�i����(GPS)
		return GpsSet.get(id);
	}
	
	public AnchorPoint getAnchorPoint(int id){//���ص�i����վ��λ��
		return PointSet.get(id);
	}
	
	public int getTime(int id){//����ʱ��
		return TimeSet.get(id);
	}
	
	public double getTa(int id){ //����Taֵ
		return PointSet.get(id).getTa(0);
	}
	
	public sector getSector(int id){
		return PointSet.get(id).getSector(0);
	}
	
	public long getPci(int id){
		return PciSet.get(id);
	}
	
	//============���ˣ������µĹ��캯��
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
	public Vector<Vector<Long>> legalline;//��ѡ��
	public Vector<Long> PciSet;// ��վID��
	public Vector<AnchorPoint> PointSet;//��λ������
	public Vector<Point> GpsSet;//GPS ����
	public Vector<Integer> TimeSet;//ʱ��ֵ
}



