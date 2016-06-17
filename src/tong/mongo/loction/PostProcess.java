package tong.mongo.loction;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Vector;

import tong.map.MapProcess.MapData;
import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Node;
import tong.mongo.defclass.Point;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class PostProcess {
	private static Mongo connection = null;
	private static DB db = null;
	private static double disMax = 100.0;
	public PostProcess(){
		connection = null;
		db = null;
	}
	
	public static Vector<Node> filterByArcID(Vector<Node> LpSet){ //���ݻ���ID���й��ˣ�ȥ����ͬ��ID֮��Ļ��Σ���ȥ����·
		Vector<Node> ret =new Vector<Node>();
		if(LpSet.size()==0)//���Ϊ����ֱ�ӷ���
			return ret;
		HashSet<Long> ArcSet=new HashSet<Long>();
		int n=LpSet.size(),top=0;
		Node[] stack =new Node[n+1];//ջ
		for(int i=0;i<n;i++){
			long Arcid = LpSet.get(i).lineid;
			if(ArcSet.contains(Arcid)==true){
				while(top>0&&stack[top-1].lineid!=Arcid){
					ArcSet.remove(stack[top-1].lineid);
					top--;
				}
			}
			else{
				ArcSet.add(Arcid);
				stack[top++]=LpSet.get(i);
			}
		}
		for(int i=0;i<top;i++){
			ret.add(stack[i]);
		}
		return ret;
	}
	
	public static Vector<Node> removeLoops(Vector<Node> LpSet){
		return filterByArcID(LpSet);//Ŀǰֻʹ�ø��ݻ���ID���й���()
	}
	
	@SuppressWarnings("deprecation")
	public static Vector<Node> Interpolation(Vector<Node> LpSet) throws UnknownHostException{//����
		connection = new Mongo("127.0.0.1:27017");
		db = connection.getDB("MapLoc");
		Vector<Node> ret=new Vector<Node>();//����ֵ
		int size=LpSet.size();
		if(size==0){
			return ret;
		}
		for(int i=0;i<size-1;i++){//���ӵ�ʹ��·����ͨ
			ret.add(LpSet.get(i));
			double dis=Algorithm.Distance(LpSet.get(i).po, LpSet.get(i+1).po);
			if(dis>disMax){//��Ϊi�ͣ�i+1���� �Ͽ���
				Vector<Point> poset=new Vector<Point>();
				Point from=LpSet.get(i).po;
				Point to=LpSet.get(i+1).po;
				double lat=(to.x+from.x)/2;
				double lng=(to.y+from.y)/2;
				poset.add(new Point(lat,lng));//��i����͵�i+1������е�
				double  radius = Algorithm.Distance(from, to)+200.0;//�뾶
				MapLoc mymp=MapData.getMap(poset,db, radius/1000.0);//�õ�����ĵ���صĵ�ͼ
				Graph Dij=new Graph(mymp);
				MapLoc newmap = new MapLoc();
				Dij.MapSimple(mymp,newmap);//�򻯵�ͼ����֤��ͼ��ͨ
				Vector<Node> midNode = new Vector<Node>();
				Dij.GetPobitAtoB(newmap,LpSet.get(i).lineid,LpSet.get(i+1).lineid,midNode);
				ret.addAll(midNode);
			}
		}
		connection.close();
		ret.add(LpSet.get(size-1));
		return ret;
	}
	
//	public static Vector<Node> doPostProcess(Vector<Node> LpSet) {
//		LpSet = remove
//	}
}
