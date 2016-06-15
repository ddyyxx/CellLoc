package basestation;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import tong.mongo.defclass.Point;
import tong.mongo.loction.Algorithm;

public class BaseStation{
	Algorithm Alg;//算法类
	Vector<CarPosition> vec;//定位点
	public Random rand;
	int Num=30;//智能点数量
	double Tem;//温度
	double INF=210000000000.0;
	double PI=Math.PI;//pi
	double EPS=1e-4;//精度
	public Point[] poset;//智能点坐标
	public double[] Dis;//智能点估价函数
 	public BaseStation(){
		rand=new Random();
		poset=new Point[Num];
		Dis=new double[Num];
		Alg=new Algorithm();
		this.vec=new Vector<CarPosition>();
	}
 	void Init(){
 		Tem=0.1;//设定初始温度
 		rand=new Random();
		poset=new Point[Num];
		Dis=new double[Num];
		Alg=new Algorithm();
 		for(int i=0;i<Num;i++){
 			int id=rand.nextInt(vec.size());
 			poset[i]=new Point(vec.get(id).lat,vec.get(id).lng);
 			Dis[i]=foo(poset[i]);
 		}
 	}
	public double foo(Point po){//估价函数
		int size=vec.size();
		double sum=0.0;
		for(int i=0;i<size;i++){
			double lat=vec.get(i).lat;
			double lng=vec.get(i).lng;
			double dis=vec.get(i).dis;
			double x=Alg.Distance(po, new Point(lat,lng))-dis;
			sum+=Fabs(x);
		}
		return sum/size;
	}
	public CarPosition getPoint(Vector<CarPosition> PointSet,int l,int r){
		double lat=PointSet.get(l).lat,lng=PointSet.get(l).lng,dis=PointSet.get(l).dis;
		for(int i=l+1;i<=r;i++){
			if(PointSet.get(i).dis<dis)
					dis=PointSet.get(i).dis;
		}
		return new CarPosition(lat,lng,dis);
	}
	public double Rand(){
		return (double)(rand.nextInt(100))/100.0;
	}
	public boolean issame(CarPosition x,CarPosition y){
		if(Fabs(x.lat-y.lat)<=1e-5&&Fabs(x.lng-y.lng)<1e-5)
			return true;
		return false;
	}
	public Vector<CarPosition> pretreatment(Vector<CarPosition> PointSet){//预处理数据
		Vector<CarPosition> ret=new Vector<CarPosition>();
		Vector<CarPosition> tmp=new Vector<CarPosition>();
		//去除重复点
		int size=PointSet.size(),left=0,right=0;
		for(int i=1;i<size;i++){
			if(issame(PointSet.get(i-1),PointSet.get(i))){
				right=i;
			}
			else{
				ret.add(getPoint(PointSet,left,right));
				left=right=i;
			}
		}
		ret.add(getPoint(PointSet,left,right));
		//去除长度为负数的点
		size=ret.size();
		tmp.clear();
		for(int i=0;i<size;i++){
			if(ret.get(i).dis>0){
				tmp.add(ret.get(i));
			}
		}
		ret=tmp;
		return ret;
	}
	public double solve(Vector<CarPosition> PointSet,Point po){
		this.vec=new Vector<CarPosition>();
		this.vec=PointSet;
		Init();
		while(Tem>EPS){
			for(int i=0;i<Num;i++){
				for(int j=0;j<30;j++){//每个点走30步
					double Dx=Tem*Math.cos(Rand()*PI);
					double Dy=Tem*Math.cos(Rand()*PI);
					Point tp=new Point(Dx+poset[i].x,Dy+poset[i].y);
					double sub=foo(tp);
					if(sub<Dis[i]){
						Dis[i]=sub;
						poset[i]=tp;
					}
				}
			}
			Tem*=0.95;
		}
		Point ret=poset[0];
		double mi=Dis[0];
		for(int i=1;i<Num;i++){
			if(mi>Dis[i]){
				mi=Dis[i];
				ret=poset[i];
			}
		}
		po.x=ret.x;
		po.y=ret.y;
		return mi;
	}
	public int Max(int a,int b){
		return a>b?a:b;
	}
	public double Fabs(double x){
		return x>0?x:-x;
	}
	public Vector<CarPosition> InitPoint(Vector<CarPosition> PointSet,int num){
		
		Vector<CarPosition> ret=new Vector<CarPosition>();
		int size=PointSet.size();
		//随机取点
		Random rand=new Random();
		for(int i=0;i<num;i++){
			int id=rand.nextInt(size);
			ret.add(PointSet.get(id));
		}
		return ret;
		//间隔取点
		/*int Int=Max(1,size/num),id=0;
		while(num!=0){
			ret.add(PointSet.get(id));
			id+=Int;
			num--;
		}
		return ret;*/
	}
	public Vector<CarPosition> getPoints(Vector<CarPosition> PointSet){
		Vector<CarPosition> ret=new Vector<CarPosition>();
		int size=PointSet.size();
		ret.add(PointSet.get(0));
		for(int i=1;i<size;i++){
			if(Fabs(PointSet.get(i).dis-PointSet.get(i-1).dis)>50.0)
				ret.add(PointSet.get(i));
		}
		return ret;
	}
	public Vector<CarPosition> PointSort(Vector<CarPosition> pointSet){
		Collections.sort(pointSet);
		Vector<CarPosition> ret=new Vector<CarPosition>();
		int size=pointSet.size();
		if(size>15)
			size=15;
		for(int i=0;i<size;i++){
			if(pointSet.get(i).dis>750.0){
				break;
			}
			ret.add(pointSet.get(i));
		}
		return ret;
	}
	public double getrand(){
		//return rand.nextInt(20)-10;
		return 0;
	}
	public void Check(Vector<CarPosition> PointSet,Point result){//测试算法用
		int num=PointSet.size();
		Vector<CarPosition> vec=new Vector<CarPosition>();
		for(int i=0;i<num;i++){
			double lat=PointSet.get(i).lat;
			double lng=PointSet.get(i).lng;
			double dis=Alg.Distance(result, new Point(lat,lng))+getrand();
			vec.add(new CarPosition(lat,lng,dis));
		}
		Point res=new Point();
		double diff=solve(vec,res);
		System.out.println("diff= "+diff);
		result.print();
		res.print();
		System.out.println("distance="+Alg.Distance(res, result));
	}
	public Point getPosition(Vector<CarPosition> PointSet,StringBuffer diff){
		if(PointSet.size()==0){
			System.out.println("这是咋回事");
			diff.append(String.valueOf(100000000.0));
			return new Point();
		}
		Vector<CarPosition> vectmp=new Vector<CarPosition>();
		vectmp=pretreatment(PointSet);//预处理一遍点集合
		//vectmp=PointSort(PointSet);//取TA值小的点
		if(vectmp.size()<3){//点数太少
			diff.append(String.valueOf(100000000.0));
			return new Point(-1.0,-1.0);
		}
		//Vector<CarPosition> tmp=new Vector<CarPosition>();
		//tmp=getPoints(PointSet);
		Point ret=new Point();
		System.out.println(vectmp.size());
		double dif=solve(vectmp,ret);
		/*for(int i=0;i<PointSet.size();i++){
		Point pi=new Point(PointSet.get(i).lat,PointSet.get(i).lng);
			System.out.println("dis = "+Alg.Distance(pi, ret)+"Ta = "+PointSet.get(i).dis);
		}*/
		diff.append(String.valueOf(dif));
		//Check(PointSet,ret);
		return ret;
	}
}

