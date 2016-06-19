package tong.mongo.defclass;

import java.util.Vector;

//��λ����
public class AnchorPoint {
	public int type;// ��λ������  0��ʾ��ͨ�� 1��ʾ�л��� 2��ʾTA�ı��(��С���) 3TA�ı��(�Ӵ��С)
	private Vector<Point> PointSet;		//��λ���վ����
	private Vector<Double> TaSet;		//��λ��TAֵ
	private Vector<sector> SectorSet;	//��λ�����η�Χ
	
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
