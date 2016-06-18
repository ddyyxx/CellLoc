package tong.mongo.defclass;

import java.util.Vector;

//定位点类
public class AnchorPoint {
	public int type;// 定位点种类  0表示普通点 1表示切换点 2表示TA改变点(从小变大) 3TA改变点(从大变小)
	private Vector<Point> PointSet;		//定位点基站坐标
	private Vector<Double> TaSet;		//定位点TA值
	private Vector<sector> SectorSet;	//定位点扇形范围
	
	public AnchorPoint(){
		type=0;
		PointSet = new Vector<Point>();
		TaSet = new Vector<Double>();
		SectorSet = new Vector<sector>();
	}
	
	public AnchorPoint(int typ){
		this.type=typ;
		PointSet = new Vector<Point>();
		TaSet = new Vector<Double>();
		SectorSet = new Vector<sector>();
	}
	
	public void addPoint(Point p,double Ta,sector sec){
		PointSet.add(p);
		TaSet.add(Ta);
		SectorSet.add(sec);
	}
	
	public Point getPoint(int id){
		return PointSet.get(id);
	}
	
	public double getTa(int id){
		return TaSet.get(id);
	}
	
	public sector getSector(int id){
		return SectorSet.get(id);
	}
}
