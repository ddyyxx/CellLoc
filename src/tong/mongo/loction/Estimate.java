package tong.mongo.loction;
import java.util.Vector;

import tong.mongo.defclass.Car;
import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Node;

public class Estimate{
	public static void StreetEstimate(MapLoc mymp,Car car,Vector<Node> orbit,long preline,boolean USEGPS){//返回匹配道路 基站定位或GPS定位
	    Graph Dij=new Graph(mymp);
	    MapLoc newmap=new MapLoc();
	    Dij.MapSimple(mymp, newmap);
	    if(USEGPS==true){//使用Gps进行匹配
	    	HmmGps hmm=new HmmGps(Dij);
	    	hmm.solve(newmap, car, orbit);
	    }
	    else{ //使用TA值进行匹配
	    	Hmm hmm=new Hmm(Dij);
	    	hmm.solve(newmap,car,orbit,preline);//插入的是新的map(保证连通)
	    }
	}
	public static void StreetEstimate(MapLoc mymp,Car car,Vector<Node> TaStreet,Vector<Node> GpsStreet,long preline){
		Graph Dij=new Graph(mymp);
		MapLoc newmap = new MapLoc();
		Dij.MapSimple(mymp,newmap);
		Hmm hmm = new Hmm(Dij); //基站定位
		hmm.solve(newmap, car, TaStreet,preline);
		HmmGps hmmgps = new HmmGps(Dij); //GPS定位
		hmmgps.solve(newmap,car,GpsStreet);
	}
}

