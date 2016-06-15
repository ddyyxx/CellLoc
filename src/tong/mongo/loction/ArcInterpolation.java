package tong.mongo.loction;

import java.net.UnknownHostException;
import java.util.Vector;

import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Node;
import tong.mongo.defclass.Point;

import com.mongodb.DB;
import com.mongodb.Mongo;

//���仡��ʹ����������������
public class ArcInterpolation {
	
	public Mongo connection = null;
	public DB db = null;
	static double disMax = 100.0;
	public Algorithm Alg;
	public ArcInterpolation(){
		Alg = new Algorithm();
		connection = null;
		db = null;
	}
	@SuppressWarnings("deprecation")
	public Vector<Node> Interpolation(Vector<Node> LpSet) throws UnknownHostException{//����
		//return LpSet;
		//System.out.println("��ʼ���˳���");
		//return LpSet;
		connection = new Mongo("127.0.0.1:27017");
		db = connection.getDB("MapLoc");
		Vector<Node> ret=new Vector<Node>();//����ֵ
		int size=LpSet.size();
		if(size==0){
			return ret;
		}
		for(int i=0;i<size-1;i++){//���ӵ�ʹ��·����ͨ
			ret.add(LpSet.get(i));
			double dis=Alg.Distance(LpSet.get(i).po, LpSet.get(i+1).po);
			if(dis>disMax){//��Ϊi�ͣ�i+1���� �Ͽ���
				Vector<Point> poset=new Vector<Point>();
				Point from=LpSet.get(i).po;
				Point to=LpSet.get(i+1).po;
				double lat=(to.x+from.x)/2;
				double lng=(to.y+from.y)/2;
				poset.add(new Point(lat,lng));//��i����͵�i+1������е�
				double  radius = Alg.Distance(from, to)+200.0;//�뾶
				MapLoc mymp=MdbFind.getMap(poset,db, radius/1000.0);//�õ�����ĵ���صĵ�ͼ
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
		//System.out.println("��������");
		return ret;
	}
}
