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

	public Algorithm Alg;//�㷨��
	public Random rand;
	public edge[] e;//��ʽǰ��������
	public HashMap<Long,Integer> box,vis,inc;
	public HashMap<Integer,Integer> incnum;//��ʾ��ͨ�����е�ĸ���
	public HashMap<Long,Double> dis;
	public HashMap<Long,Integer> changenum;
	public HashMap<Long,Long> prestid;//��¼һ����ǰһ��·�ı��
	public HashMap<Long,Path> px;//��¼���··��
	public HashMap<Long,Long> flag;//��ű�ǣ���Ҫ���ڱ���Ƶ����ʼ��
	public HashMap<fromto,Double> shortestPath;//��¼����֮������·
	public HashMap<fromto,Integer> Pathchange;//��¼����֮�����·��·����
	public HashMap<Long,Integer> Vis;//����Ͱ������
	public int cnt;//������ 
	public double INF=2100000000.0;
	public double cost=150.0;//��·�Ĵ���
	public Graph(){}
	public Graph(MapLoc mymp){//��ʼ��
		rand=new Random();
		int lnum=mymp.getLinenum()*2+10;
		e=new edge[lnum];
		for(int i=0;i<lnum;i++){
			e[i]=new edge();
		}
		incnum=new HashMap<Integer,Integer>();
		inc=new HashMap<Long,Integer>();//��ʾ��������ͨ�������
		box=new HashMap<Long,Integer>();//��ʽǰ����
		vis=new HashMap<Long,Integer>();//�洢�Ƿ���ʹ��ýڵ�
		dis=new HashMap<Long,Double>();//�������·��
		changenum=new HashMap<Long,Integer>();//��¼��·�л�����
		px=new HashMap<Long,Path>();
		prestid=new HashMap<Long,Long>();
		flag=new HashMap<Long,Long>();
		shortestPath=new HashMap<fromto,Double>();
		Pathchange=new HashMap<fromto,Integer>();
		cnt=0;
		Alg=new Algorithm();
		BuildMap(mymp);//��ͼ
	}
	public class edge{//����
		public long to,id,stid;//�ֱ��ʾ��һ�����ţ�������ţ���������·���
		public int next;
		public double len;//��������
	}
	public class Path{//·����
		public Path(){}
		public Path(long x,long y){
			this.preP=x;//ǰ���
			this.preL=y;//ǰ��
		}
		public long preP,preL;
	}
	public class fromto{//�����Ż�
		public long from, to;
		public fromto() {}
		public fromto(long a, long b) {
			this.from = a;
			this.to = b;
		}

		public int hashCode() {
			int ret = new Long(from).hashCode() ^ new Long(to).hashCode();
			// Ҳ�������������ַ�ʽ����hashCode
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
		for(Long id : mymp.PointSet.keySet()){//�����㼯���еĵ�����
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
	public void BuildMap(MapLoc mymp){//��ͼ ��ʽǰ����
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
	public int dfs(long now,int num){//����ͨ����
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
	public int connectNum(MapLoc mymp){//����ͨ��������
		Vis=new HashMap<Long,Integer>();
		int num=0;
		for(long id:mymp.PointSet.keySet()){
			Vis.put(id,0);
		}
		for(Long id:mymp.PointSet.keySet()){
			if(Vis.get(id)==0){
				int incn=dfs(id,++num);
				incnum.put(num, incn);//��ʾ���Ϊnum����ͨ���е������
			}
		}
		return num;
	}
	public void MapSimple(MapLoc oldmap,MapLoc newmap) {//��֤newmap����ͨ��
		connectNum(oldmap);
		//System.out.println("��ͨ�������� = "+num);
		int fin=0,maximum=0;
		for(Integer id:incnum.keySet()){//Ѱ��������ͨ��
			if(maximum<incnum.get(id)){
				maximum=incnum.get(id);
				fin=id;
			}
		}
		//System.out.println("ԭͼ������ = "+oldmap.PointNum+" ��ͼ������ = "+incnum.get(fin));//�Ƚ���ͼԭͼ������
		for(Long id:oldmap.PointSet.keySet()){
			if(inc.get(id)==fin) {//�����������ͨ���еĵ�,�������ͼ
				double lat=oldmap.PointSet.get(id).x;
				double lng=oldmap.PointSet.get(id).y;
				newmap.addPoint(id, lat, lng);
			}
		}
		newmap.PointNum=newmap.PointSet.size();//���õ������
		for(Long id:oldmap.LineSet.keySet()){//���뻡
			long id1=oldmap.LineSet.get(id).pid[0];
			long id2=oldmap.LineSet.get(id).pid[1];
			long stid=oldmap.LineSet.get(id).strid;
			if(inc.get(id1)==fin||inc.get(id2)==fin) {//ֻҪ��һ�����������ͨ���У���ʵ��ֻ�п��ܶ��ڻ򶼲��ڣ�,�ͽ��û���������
				double len=oldmap.LineSet.get(id).length;
				newmap.addLine(id, id1, id2, len,stid);
			}
		}
		newmap.LineNum=newmap.LineSet.size();//���û�������
	}
	
	public double Solve(MapLoc mymp,long S,long T) {//��S��T�����· �����ʼ�� (SPFA)
		if(S==T)
			return 0.0;
		if(inc.get(S)!=inc.get(T))//�������ͬһ����ͨ���У��򷵻������
			return INF;
		Queue<Long> pq =  new LinkedList<Long>();//����
		long tmp=Fabs(rand.nextLong());
		prestid.put(S, -1L);//S��ǰ��·Ϊ-1
		dis.put(S,0.0);//S��S�ľ���Ϊ0
		changenum.put(S, 0);
		flag.put(S,tmp);//����SΪ����
		vis.put(S, 1);
		pq.add(S);
		while(!pq.isEmpty()){
			long po=pq.poll();
			vis.put(po, 0);
			flag.put(po,tmp);//ò�ƿ��Բ�Ҫ��䣿
			for(int t=box.get(po);t!=-1;t=e[t].next){
				long v=e[t].to;
				long stid=e[t].stid;
				double len=e[t].len+dis.get(po);
				long prestreet=prestid.get(po);
				int chnum=changenum.get(po);
				if(prestreet!=-1&&prestreet!=stid){//��ʾ��·��
					chnum+=1;
				}
				if(flag.get(v)!=tmp||dis.get(v)>len){
					dis.put(v,len);
					prestid.put(v, stid);//��¼��һ��·
					px.put(v,new Path(po,e[t].id));
					changenum.put(v,chnum);//���»�·����
					if(flag.get(v)!=tmp||vis.get(v)==0){
						vis.put(v, 1);
						flag.put(v,tmp);
						pq.add(v);//�������
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
		if(flag.get(T)!=tmp) {//û�ѵ�T ͼ����ͨ
			System.out.println("��ô�����Ѳ���~~ͼ����ͨ������");
			return INF;
		}
		double Distance=dis.get(T);
		return Distance;
	}
	public double GetDisAtoB(MapLoc mymp,Point a,Line La,Point b,Line Lb,double len,boolean useTa){//�õ���

		Point x=Alg.ptoseg(a, La);//��La�ϵ���a����ĵ�
		Point y=Alg.ptoseg(b, Lb);//��Lb�ϵ���b����ĵ�
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
				double tmp=Alg.Distance(x, La.p[i]);//��x����La��һ���˵�
				if(shortestPath.get(new fromto(La.pid[i],Lb.pid[j]))==null)//�����ǰû�м����S��T�ľ��룬����㣬����ֱ�Ӵ�shortestpath�в�ѯ����
					tmp+=Solve(mymp,La.pid[i],Lb.pid[j]);
				else{
					tmp+=shortestPath.get(new fromto(La.pid[i],Lb.pid[j]));
				}
				tmp+=Alg.Distance(Lb.p[j],y);//�ӻ�Lb��һ���˵㵽y
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
		orbit.add(La.index);//���ȼ��ϵ�һ�����ı��
		Point x=Alg.ptoseg(a, La);
		Point y=Alg.ptoseg(b, Lb);
		if(La.index==Lb.index){
			return Alg.Distance(x, y);
		}
		double mi= INF;
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				double tmp=Alg.Distance(x, La.p[i]);//��x����La��һ���˵�
				tmp+=Solve(mymp,La.pid[i],Lb.pid[j]);
				tmp+=Alg.Distance(Lb.p[j],y);//�ӻ�Lb��һ���˵㵽y
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
	public void GetPobitAtoB(MapLoc mymp,Long S,Long T,Vector<Node> poset){//�õ���һ��������һ����֮���·���ĵ㼯��
		Line from=mymp.LineSet.get(S);
		Line to=mymp.LineSet.get(T);
		Vector<Long> orbit=new Vector<Long>();
		if(from==null||to==null)
			return;
		@SuppressWarnings("unused")
		double disbitStoT=GetOrbitAtoB(mymp,from.p[0],from,to.p[0],to,orbit);//�õ��ӻ�S����T�����·��
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
		//System.out.println("���·���� = "+disbitStoT);
	}
}