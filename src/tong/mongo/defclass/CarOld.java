package tong.mongo.defclass;

import java.util.Vector;

public class CarOld {
	public CarOld(){}
	public CarOld(int num){
		PointNum=num;
		PointSet=new Vector<Point>();
		TimeSet=new Vector<Double>();
	}
	public void addPoint(double lat,double lng,double time){
		PointSet.add(new Point(lat,lng));
		TimeSet.add(time);
	}
	public void print(){
		System.out.println(PointNum);
		for(int i=0;i<PointNum;i++){
			PointSet.get(i).print();
		}
		for(int i=0;i<PointNum;i++){
			
			System.out.println(TimeSet.get(i));
		}
	}
	public long cid;
	public int PointNum;
	public Vector<Point> PointSet;
	public Vector<Double> TimeSet;
}