package tong.mongo.loction;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import tong.mongo.defclass.Car;
import tong.mongo.defclass.Line;
import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Node;
import tong.mongo.defclass.Point;

public class HmmGps{
	double VARIANCE=20.0;
	double PI=3.141592653589793238462643383279502884;
	double INF=2100000000.0;
	double LIMITDIS=200.0;
	public static boolean USEGPS=true;//表示用的是GPS坐标
	double MAXDIS=15.0;//候选集半径
	public Graph Dij;
	public int MIDNUM=15;
	public Algorithm Alg;
	public HmmGps(){
		Alg=new Algorithm();
	}
	public HmmGps(Graph dij){
		Alg=new Algorithm();
		this.Dij=dij;
	}
	public double Min(double a,double b){
		return a<b?a:b;
	}
	public double Fabs(double x){
		return x<0?-x:x;
	}
	public double GetP(double len){//状态概率 (改为return 1)
		//return 1.0;
		double x=1/(VARIANCE*Math.sqrt(PI*2));
		double y=Math.exp(-(len/VARIANCE)*(len/VARIANCE)/2.0);
		double P=Min(x*y,1.0);
		return  P;
	}
	public double GetTransP(Point u,long from,Point v,long to,MapLoc mymp){ //转移概率
		double len=Alg.Distance(u,v);//直线距离
		double dt=Dij.GetDisAtoB(mymp, u, mymp.getLine(from), v, mymp.getLine(to),len);
		//double dt=Dij.GetDisAtoB(mymp, u, mymp.LineSet.get(from), v, mymp.LineSet.get(to));//最短路距离
		if(dt==INF)//如果不连通，则返回0
			return 0.0;
		double transp=Math.exp(-dt/VARIANCE);	//公式1
		return transp;
	}
	public void Balance(Map<Long,Double> dp){//正规化
		double sum=0.0;
		for(long id:dp.keySet()){
			sum+=dp.get(id);
		}
		for(long id:dp.keySet()){
			double tmp=dp.get(id);
			dp.put(id, tmp/sum);
		}
	}
	public void GetPath(Car car,int st,int ed,MapLoc mymp,long preline,Vector<Long> path){//利用隐式马尔科夫求匹配路径
		int n=ed-st+1;
		@SuppressWarnings("unchecked")
		Map<Long,Double>[] dp=new Map[n];//马尔科夫概率链
		@SuppressWarnings("unchecked")
		Map<Long,Long>[] pre=new Map[n];//前向弧
		for(int i=0;i<n;i++){
			dp[i]=new HashMap<Long,Double>();
			pre[i]=new HashMap<Long,Long>();
		}
		int size=0;
		for(int i=0;i<n;i++){//初始化
			size=car.legalline.get(i+st).size();
			for(int j=0;j<size;j++){
				long id=car.legalline.get(i+st).get(j);
				dp[i].put(id,0.0);
				pre[i].put(id, (long)-1);
			}
		}
		size=car.legalline.get(st).size();
		for(int i=0;i<size;i++){//处理第一个点的状态概率
			long id=car.legalline.get(st).get(i);
			Point point=car.getGpsPoint(st);
			double len=Alg.disptoseg(point, mymp.getLine(id));
			dp[0].put(id, GetP(len));
		}
		Balance(dp[0]);
		for(int i=st+1;i<=ed;i++){
			int s1=car.legalline.get(i-1).size();
			for(int j=0;j<s1;j++){//枚举起点
				long id1=car.legalline.get(i-1).get(j);//起点弧段
				Point p1=car.getGpsPoint(i-1);
				if(dp[i-st-1].get(id1)>=0.0){
					int s2=car.legalline.get(i).size();
					for(int k=0;k<s2;k++){
						long id2=car.legalline.get(i).get(k);//终点弧段
						Point p2=car.getGpsPoint(i);
						double transp=GetTransP(p1,id1,p2,id2,mymp);//转移概率
						double dis=Alg.disptoseg(p2, mymp.getLine(id2));//点p2到弧id2的距离
						double P=dp[i-st-1].get(id1)*transp*GetP(dis);
						assert(P>=0.0);
						if(P>dp[i-st].get(id2)){
							dp[i-st].put(id2, P);
							pre[i-st].put(id2, id1);//从id1转移过来
						}
					}
				}
			}
			Balance(dp[i-st]);
		}
		double ma=-INF;
		long po=-1;
		for(long id:dp[n-1].keySet()){
			double tmp=dp[n-1].get(id);
			if(tmp>ma){
				ma=tmp;
				po=id;
			}
		}
		if(po==-1){
			System.out.println("匹配轨迹为空");
			return;
		}
		path.clear();
		for(int i=n-1;i>=0;i--){
			path.add(po);
			po=pre[i].get(po);
		}
		for(int i=0;i<n/2;i++){//翻转路径序列
			long tmp=path.get(i);
			path.set(i, path.get(n-i-1));
			path.set(n-i-1,tmp);
		}
	}
	public void getLegalSet(MapLoc mymp,Car car){//获得候选集
		if(car.legalline.size()!=0)//如果车本身已经存在候选集，则退出
			return;
		int pnum=car.PointNum;
		for(int i=0;i<pnum;i++){
			Vector<Long> lgline=new Vector<Long>();
			Point p1=car.getGpsPoint(i);//Gps坐标点
			for(Long id:mymp.LineSet.keySet()){
				//x,y分别是弧的两个点
				if(Alg.disptoseg(p1, mymp.getLine(id))<MAXDIS){//弧到点的距离小于20米则加入候选集
					lgline.add(id);//加入候选集
				}
			}
			car.legalline.add(lgline);
		}
	}
	public Car getcar(Car car){//将候选集为0的车的点丢掉
		int n=car.legalline.size();
		Car ret=new Car();
		for(int i=0;i<n;i++){
			if(car.legalline.get(i).size()!=0){
				ret.addGpsPoint(car.getGpsPoint(i));//加入点
				ret.legalline.add(car.legalline.get(i));//加入候选集
				ret.TimeSet.add(car.getTime(i));//加入时间
				//时间暂时不需要，需要时候再加入
			}
		}
		return ret;
	}
	
	public Point getMidPoint(MapLoc mymp,long Lid){
		Line L=mymp.getLine(Lid);
		Point Midpoint = new Point((L.p[0].x+L.p[1].x)/2,(L.p[0].y+L.p[1].y)/2);
		return Midpoint;
	}
	public void solve(MapLoc mymp,Car car,Vector<Node> orbit){
		getLegalSet(mymp,car);//获取候选集合
		car=getcar(car);//去除候选集为0的点
		int ponum=car.PointNum;//在这里重新定义点集合
		if(ponum==0)
			return;
		Vector<Long> tmporbit=new Vector<Long>();
		GetPath(car,0,ponum-1,mymp,-1,tmporbit);
		int size=tmporbit.size();
		//这里要做一次过滤
		Vector<Node> LpSet = new Vector<Node>();
		for(int i=0;i<size;i++){
			long Lid=tmporbit.get(i);
			Point po = getMidPoint(mymp,Lid);
			long time=car.getTime(i);
			LpSet.add(new Node(Lid,po,time));
		}
		size=LpSet.size();
		for(int i=0;i<size-1;i++){
			orbit.add(LpSet.get(i));
			long S=LpSet.get(i).lineid,T=LpSet.get(i+1).lineid;
			Vector<Node> midArc=new Vector<Node>();
			Dij.GetPobitAtoB(mymp, S, T, midArc);
			orbit.addAll(midArc);
		}
		if(size>0)
			orbit.add(LpSet.get(size-1));
	}
}
