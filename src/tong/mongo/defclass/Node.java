package tong.mongo.defclass;

//弧编号与弧中点坐标
public class Node{
	public long lineid,time;
	public Point po;
	public Node() {
		this.po=new Point();
		this.lineid =0;
		this.time=-1;
	}
	public Node(long lid,Point p) {
		this.lineid=lid;
		this.po=p;
		time=-1;
	}
	public Node(long lid,Point p,long t){
		this.lineid=lid;
		this.time=t;
		this.po=p;
	}
}