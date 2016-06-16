package tong.mongo.loction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import tong.mongo.defclass.Line;
import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Node;
import tong.mongo.defclass.Point;

public class Graph{//

	public Algorithm Alg;//算法类
	public Random rand;
	public edge[] e;//链式前向星数组
	public HashMap<Long,Integer> box,vis,inc;
	public HashMap<Integer,Integer> incnum;//表示联通分量中点的个数
	public HashMap<Long,Double> dis;
	public HashMap<Long,Integer> changenum;
	public HashMap<Long,Long> prestid;//记录一个点前一条路的编号
	public HashMap<Long,Path> px;//记录最短路路径
	public HashMap<Long,Long> flag;//编号标记，主要用于避免频繁初始化
	public HashMap<fromto,Double> shortestPath;//记录两点之间的最短路
	public HashMap<fromto,Integer> Pathchange;//记录两点之间最短路换路次数
	public HashMap<Long,Integer> Vis;//求两桶分量用
	public int cnt;//边数量 
	public double INF=2100000000.0;
	public double cost=150.0;//换路的代价
	public Graph(){}
	public Graph(MapLoc mymp){//初始化
		rand=new Random();
		int lnum=mymp.getLinenum()*2+10;
		e=new edge[lnum];
		for(int i=0;i<lnum;i++){
			e[i]=new edge();
		}
		incnum=new HashMap<Integer,Integer>();
		inc=new HashMap<Long,Integer>();//表示点所在连通分量编号
		box=new HashMap<Long,Integer>();//链式前向星
		vis=new HashMap<Long,Integer>();//存储是否访问过该节点
		dis=new HashMap<Long,Double>();//计算最短路用
		changenum=new HashMap<Long,Integer>();//记录道路切换次数
		px=new HashMap<Long,Path>();
		prestid=new HashMap<Long,Long>();
		flag=new HashMap<Long,Long>();
		shortestPath=new HashMap<fromto,Double>();
		Pathchange=new HashMap<fromto,Integer>();
		cnt=0;
		Alg=new Algorithm();
		BuildMap(mymp);//建图
	}
	public class edge{//边类
		public long to,id,stid;//分别表示下一个点标号，本弧编号，本弧所在路编号
		public int next;
		public double len;//本弧长度
	}
	public class Path{//路径类
		public Path(){}
		public Path(long x,long y){
			this.preP=x;//前向点
			this.preL=y;//前向弧
		}
		public long preP,preL;
	}
	public class fromto{//用于优化
		public long from, to;
		public fromto() {}
		public fromto(long a, long b) {
			this.from = a;
			this.to = b;
		}

		public int hashCode() {
			int ret = new Long(from).hashCode() ^ new Long(to).hashCode();
			// 也可以用下面这种方式计算hashCode
			// int ret = String.valueOf(id).hashCode() ^
			// String.valueOf(type).hashCode();
			//System.out.println(ret);
			return ret;
		}
		public boolean equals(Object obj) {
			if (null == obj) {
				return false;
			}
			if (!(obj instanceof fromto)) {
				return false;
			}
			fromto tmpObj = (fromto) obj;
			return tmpObj.from == from && tmpObj.to == to;
		}
	}
	public void Init(MapLoc mymp){
		for(Long id : mymp.PointSet.keySet()){//遍历点集合中的点数据
			box.put(id,-1);
			vis.put(id,0);
			dis.put(id,INF);
			changenum.put(id, 0);
			prestid.put(id,(long)-1);
			px.put(id,new Path(-1,-1));
			flag.put(id,(long)-1);
		}
		cnt=0;
	}
	public void Add(long from,long to,double len,long id,long stid){
		e[cnt].to=to;
		e[cnt].id=id;
		e[cnt].stid=stid;
		e[cnt].len=len;
		e[cnt].id=id;
		e[cnt].next=box.get(from);
		box.put(from, cnt);
		cnt++;
	}
	public void BuildMap(MapLoc mymp){//建图 链式前向星
		Init(mymp);
		for(Long id:mymp.LineSet.keySet()){
			Line line=mymp.LineSet.get(id);
			long from=line.pid[0];
			long to=line.pid[1];
			double len=line.length;
			long stid=line.strid;
			Add(from,to,len,id,stid);
			Add(to,from,len,id,stid);
		}
	}
	public double Min(double a,double b){
		return a<b?a:b;
	}
	public long Fabs(long x){
		return x>0?x:-x;
	}
	public int dfs(long now,int num){//求连通分量
		Vis.put(now,1);
		int sum=1;
		inc.put(now, num);
		for(int t=box.get(now);t!=-1;t=e[t].next){
			long v=e[t].to;
			if(Vis.get(v)==0){
				sum+=dfs(v,num);
			}
		}
		return sum;
	}
	public int connectNum(MapLoc mymp){//求连通分量个数
		Vis=new HashMap<Long,Integer>();
		int num=0;
		for(long id:mymp.PointSet.keySet()){
			Vis.put(id,0);
		}
		for(Long id:mymp.PointSet.keySet()){
			if(Vis.get(id)==0){
				int incn=dfs(id,++num);
				incnum.put(num, incn);//表示编号为num的连通块中点的数量
			}
		}
		return num;
	}
	public void MapSimple(MapLoc oldmap,MapLoc newmap) {//保证newmap是连通的
		connectNum(oldmap);
		//System.out.println("连通分量个数 = "+num);
		int fin=0,maximum=0;
		for(Integer id:incnum.keySet()){//寻找最大的连通块
			if(maximum<incnum.get(id)){
				maximum=incnum.get(id);
				fin=id;
			}
		}
		//System.out.println("原图点数量 = "+oldmap.PointNum+" 新图点数量 = "+incnum.get(fin));//比较新图原图点数量
		for(Long id:oldmap.PointSet.keySet()){
			if(inc.get(id)==fin) {//如果是最大的连通块中的点,则加入新图
				double lat=oldmap.PointSet.get(id).x;
				double lng=oldmap.PointSet.get(id).y;
				newmap.addPoint(id, lat, lng);
			}
		}
		newmap.PointNum=newmap.PointSet.size();//设置点的数量
		for(Long id:oldmap.LineSet.keySet()){//加入弧
			long id1=oldmap.LineSet.get(id).pid[0];
			long id2=oldmap.LineSet.get(id).pid[1];
			long stid=oldmap.LineSet.get(id).strid;
			if(inc.get(id1)==fin||inc.get(id2)==fin) {//只要有一个点在最大连通块中（事实上只有可能都在或都不在）,就将该弧加入其中
				double len=oldmap.LineSet.get(id).length;
				newmap.addLine(id, id1, id2, len,stid);
			}
		}
		newmap.LineNum=newmap.LineSet.size();//设置弧的数量
	}
	
	public double Solve(MapLoc mymp,long S,long T) {//求S到T的最短路 避免初始化 (SPFA)
		if(S==T)
			return 0.0;
		if(inc.get(S)!=inc.get(T))//如果不在同一个连通块中，则返回无穷大
			return INF;
		Queue<Long> pq =  new LinkedList<Long>();//队列
		long tmp=Fabs(rand.nextLong());
		prestid.put(S, -1L);//S的前向路为-1
		dis.put(S,0.0);//S到S的距离为0
		changenum.put(S, 0);
		flag.put(S,tmp);//设置S为本次
		vis.put(S, 1);
		pq.add(S);
		while(!pq.isEmpty()){
			long po=pq.poll();
			vis.put(po, 0);
			flag.put(po,tmp);//貌似可以不要这句？
			for(int t=box.get(po);t!=-1;t=e[t].next){
				long v=e[t].to;
				long stid=e[t].stid;
				double len=e[t].len+dis.get(po);
				long prestreet=prestid.get(po);
				int chnum=changenum.get(po);
				if(prestreet!=-1&&prestreet!=stid){//表示换路了
					chnum+=1;
				}
				if(flag.get(v)!=tmp||dis.get(v)>len){
					dis.put(v,len);
					prestid.put(v, stid);//记录上一条路
					px.put(v,new Path(po,e[t].id));
					changenum.put(v,chnum);//更新换路次数
					if(flag.get(v)!=tmp||vis.get(v)==0){
						vis.put(v, 1);
						flag.put(v,tmp);
						pq.add(v);//加入队列
					}
				}
			}
		}
		for(long id:mymp.PointSet.keySet()){
			if(flag.get(id)==tmp&&dis.get(id)!=INF){
				shortestPath.put(new fromto(S,id),dis.get(id));
				shortestPath.put(new fromto(id,S),dis.get(id));
				Pathchange.put(new fromto(S,id),changenum.get(id));
				Pathchange.put(new fromto(id,S),changenum.get(id));
			}
		}
		if(flag.get(T)!=tmp) {//没搜到T 图不连通
			System.out.println("怎么可能搜不到~~图不连通！！！");
			return INF;
		}
		double Distance=dis.get(T);
		return Distance;
	}
	public double GetDisAtoB(MapLoc mymp,Point a,Line La,Point b,Line Lb,double len,boolean useTa){//得到从

		Point x=Alg.ptoseg(a, La);//弧La上到点a最近的点
		Point y=Alg.ptoseg(b, Lb);//弧Lb上到点b最近的点
		if(La.index==Lb.index){
			return Alg.Distance(x, y);
		}
		double mi= INF;
		int add=0,chnum=0;
		if (La.strid!=Lb.strid){
			add=1;
		}
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				double tmp=Alg.Distance(x, La.p[i]);//从x到弧La的一个端点
				if(shortestPath.get(new fromto(La.pid[i],Lb.pid[j]))==null)//如果以前没有计算过S到T的距离，则计算，否则直接从shortestpath中查询即可
					tmp+=Solve(mymp,La.pid[i],Lb.pid[j]);
				else{
					tmp+=shortestPath.get(new fromto(La.pid[i],Lb.pid[j]));
				}
				tmp+=Alg.Distance(Lb.p[j],y);//从弧Lb的一个端点到y
				if(tmp<mi){
					mi=tmp;
					int Num=0;
					if(Pathchange.get(new fromto(La.pid[i],Lb.pid[j]))==null)
						Num=0;
					else
						Num=Pathchange.get(new fromto(La.pid[i],Lb.pid[j]));
					chnum=add+Num;
				}
				mi=Min(mi,tmp);
			}
		}
		if(mi==INF)
			return INF;
		return Math.abs(mi-len)+cost*chnum;
	}
	public void GetOrbit(long S,long T,Vector<Long> orbit){
		orbit.clear();
		long now=T;
		while(now!=S){
			orbit.add(px.get(now).preL);
			now=px.get(now).preP;
		}
		int size=orbit.size();
		int n=size/2;
		for(int i=0;i<n;i++){
			long tmp=orbit.get(i);
			orbit.set(i, orbit.get(size-i-1));
			orbit.set(size-i-1, tmp);
		}
	}
	public double GetOrbitAtoB(MapLoc mymp,Point a,Line La,Point b,Line Lb,Vector<Long> orbit){
		Vector<Long> tmpOrbit=new Vector<Long>();
		orbit.clear();
		orbit.add(La.index);//首先加上第一条弧的标号
		Point x=Alg.ptoseg(a, La);
		Point y=Alg.ptoseg(b, Lb);
		if(La.index==Lb.index){
			return Alg.Distance(x, y);
		}
		double mi= INF;
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				double tmp=Alg.Distance(x, La.p[i]);//从x到弧La的一个端点
				tmp+=Solve(mymp,La.pid[i],Lb.pid[j]);
				tmp+=Alg.Distance(Lb.p[j],y);//从弧Lb的一个端点到y
				if(tmp<mi){
					mi=tmp;
					GetOrbit(La.pid[i],Lb.pid[j],tmpOrbit);
				}
			}
		}
		int size=tmpOrbit.size();
		for(int i=0;i<size;i++){
			long now=tmpOrbit.get(i);
			if(now!=La.index&&now!=Lb.index)
				orbit.add(now);
		}
		orbit.add(Lb.index);
		return mi;
	}
	public void GetPobitAtoB(MapLoc mymp,Long S,Long T,Vector<Node> poset){//得到从一条弧到另一条弧之间的路径的点集合
		Line from=mymp.LineSet.get(S);
		Line to=mymp.LineSet.get(T);
		Vector<Long> orbit=new Vector<Long>();
		if(from==null||to==null)
			return;
		@SuppressWarnings("unused")
		double disbitStoT=GetOrbitAtoB(mymp,from.p[0],from,to.p[0],to,orbit);//得到从弧S到弧T的最短路径
		int size=orbit.size();
		for(int i=0;i<size;i++){
			long nowline=orbit.get(i);
			if(nowline != S && nowline != T){
				Line now=mymp.LineSet.get(nowline);
				double lat=(now.p[0].x+now.p[1].x)/2;
				double lng=(now.p[0].y+now.p[1].y)/2;
				poset.add(new Node(nowline,new Point(lat,lng)));
			}
		}
		//System.out.println("最短路长度 = "+disbitStoT);
	}
}