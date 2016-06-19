package tong.mongo.loction;
import java.util.Vector;

import tong.mongo.defclass.Car;
import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Node;

public class Estimate{
	public static void StreetEstimate(MapLoc mymp,Car car,Vector<Node> orbit,long preline,boolean USEGPS){//����ƥ���· ��վ��λ��GPS��λ
	    Graph Dij=new Graph(mymp);
	    MapLoc newmap=new MapLoc();
	    Dij.MapSimple(mymp, newmap);
	    if(USEGPS==true){//ʹ��Gps����ƥ��
	    	HmmGps hmm=new HmmGps(Dij);
	    	hmm.solve(newmap, car, orbit);
	    }
	    else{ //ʹ��TAֵ����ƥ��
	    	Hmm hmm=new Hmm(Dij);
	    	hmm.solve(newmap,car,orbit,preline);//��������µ�map(��֤��ͨ)
	    }
	}
	public static void StreetEstimate(MapLoc mymp,Car car,Vector<Node> TaStreet,Vector<Node> GpsStreet,long preline){
		Graph Dij=new Graph(mymp);
		MapLoc newmap = new MapLoc();
		Dij.MapSimple(mymp,newmap);
		Hmm hmm = new Hmm(Dij); //��վ��λ
		hmm.solve(newmap, car, TaStreet,preline);
		HmmGps hmmgps = new HmmGps(Dij); //GPS��λ
		hmmgps.solve(newmap,car,GpsStreet);
	}
}

