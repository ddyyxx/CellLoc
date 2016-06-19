package tong.mongo.loction;

import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Car;
import tong.mongo.defclass.Point;
import tong.mongo.defclass.sector;


//生成点的角度
public class TaSelect {

	private static int INTERVAL_NUM = 1;
	
	public static Car PointChoose(Car car,boolean isGPS){//适当选取定位点 （如基站切换点和Ta变换点）
		Car resultCar = new Car();
		int n=car.PointNum,nowid=0;
		assert(car.PointNum==car.PointSet.size());
		for(int id=0;id<n-1;id++){
			Point now=car.getAnchorPoint(id).getPoint(0);
			Point next=car.getAnchorPoint(id+1).getPoint(0);//下一个点
//			if(isGPS){//如果是选取GPS点，则不需要考虑基站切换或者TA变化
//				if(id%INTERVAL_NUM==0){
//					resultCar.addPointAll(car.getAnchorPoint(id), car.getGpsPoint(id), car.getPci(id),car.getTime(id));
//				}
//				continue;
//			}
			double Ta1 =car.getTa(id);
			double Ta2 =car.getTa(id+1);
			if(!Algorithm.compare(now, next)){//表示这是一个切换点(特殊点)
				//首先判断是否时间间隔太大
				if(car.getTime(id+1)-car.getTime(id)<=2){//时间间隔相差2s以内认为是切换点（有效）
					AnchorPoint Po = new AnchorPoint(1);//切换点（特殊）
					sector sec1 = car.getSector(id);
					sector sec2 = car.getSector(id+1); 
					Po.addPoint(now,Ta1,sec1);
					Po.addPoint(next,Ta2,sec2);
					resultCar.addPointAll(Po, car.getGpsPoint(id),car.getPci(id),car.getTime(id));
					//这里已经取了这个点，所以要将下面的工作
					nowid=0;
				}
			}
			else if(Algorithm.Fabs(Ta1-Ta2)>50.0){//表示这是一个Ta变化点
				AnchorPoint Po = new AnchorPoint();
				//double distance=Algorithm.Distance(car.getAnchorPoint(id+1).getPoint(0), car.getGpsPoint(id+1));
				if(Ta1<Ta2){//Ta从小变大
					Po.type = 2;	
				}
				else{//Ta从大变小
					Po.type = 3;
				}
				Po.addPoint(next, Ta2, car.getSector(id+1));//取Ta值变化的那个点
				resultCar.addPointAll(Po, car.getGpsPoint(id+1), car.getPci(id+1), car.getTime(id+1));
				nowid=0;
			}
			else if(nowid%INTERVAL_NUM==0){//普通定位点
				resultCar.addPointAll(car.getAnchorPoint(id), car.getGpsPoint(id), car.getPci(id),car.TimeSet.get(id));
			}
			nowid++;
		}
		return resultCar;
	}
	//获取过滤后的车，并且选取适当的定位点
	public static Car getCar(Car mycar,int interval,boolean isGPS){
		Car resultcar = mycar;
		INTERVAL_NUM=interval;
		if(!isGPS)
			System.out.println("过滤前点的数量"+mycar.PointNum);
		resultcar = PreProcess.ErrorFilter(mycar);//过滤误差点
		if(!isGPS)
			System.out.println("过滤后点的数量"+resultcar.PointNum);
		resultcar = PointChoose(resultcar,isGPS);//选取适当的定位点
		if(!isGPS)
			System.out.println("选取点后点的数量:"+resultcar.PointNum);
		return resultcar;
	}
}

