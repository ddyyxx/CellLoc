package tong.mongo.defclass;

public class Line{
	public Point[] p;//两定点
    public long[] pid;//两定点标号
    public double length;//弧的长度
    public long index;//弧的标号
    public long strid;//弧所在路标号
    public Line(){
    	this.p=new Point[2];
    	this.pid=new long[2];
    }
    public Line(Line other){
    	this.p=new Point[2];
    	this.p[0]=new Point(other.p[0]);
    	this.p[1]=new Point(other.p[1]);
    	this.pid = other.pid;
    	this.length = other.length;
    	this.strid = other.strid;
    	this.index = other.index;
    }
    public Line(Point p1,Point p2, long id,long id1, long id2,double len,long stid){
        this.p=new Point[2];
        this.p[0]=p1;
        this.p[1]=p2;
        this.pid=new long[2];
        this.pid[0]=id1;
        this.pid[1]=id2;
        this.index=id;
        this.length=len;
        this.strid=stid;
    }
    public Point getmidpoint(){
    	return new Point((p[0].x+p[1].x)/2,(p[0].y+p[1].y)/2);
    }
    public void print(){
    	System.out.println(index+" "+pid[0]+" "+pid[1]+" "+length);
    }
}
