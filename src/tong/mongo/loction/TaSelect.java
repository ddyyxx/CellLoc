package tong.mongo.loction;

import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Car;
import tong.mongo.defclass.Point;
import tong.mongo.defclass.sector;


//���ɵ�ĽǶ�
public class TaSelect {

	private static int INTERVAL_NUM = 1;
	
	public static Car PointChoose(Car car,boolean isGPS){//�ʵ�ѡȡ��λ�� �����վ�л����Ta�任�㣩
		Car resultCar = new Car();
		int n=car.PointNum,nowid=0;
		assert(car.PointNum==car.PointSet.size());
		for(int id=0;id<n-1;id++){
			Point now=car.getAnchorPoint(id).getPoint(0);
			Point next=car.getAnchorPoint(id+1).getPoint(0);//��һ����
//			if(isGPS){//�����ѡȡGPS�㣬����Ҫ���ǻ�վ�л�����TA�仯
//				if(id%INTERVAL_NUM==0){
//					resultCar.addPointAll(car.getAnchorPoint(id), car.getGpsPoint(id), car.getPci(id),car.getTime(id));
//				}
//				continue;
//			}
			double Ta1 =car.getTa(id);
			double Ta2 =car.getTa(id+1);
			if(!Algorithm.compare(now, next)){//��ʾ����һ���л���(�����)
				//�����ж��Ƿ�ʱ����̫��
				if(car.getTime(id+1)-car.getTime(id)<=2){//ʱ�������2s������Ϊ���л��㣨��Ч��
					AnchorPoint Po = new AnchorPoint(1);//�л��㣨���⣩
					sector sec1 = car.getSector(id);
					sector sec2 = car.getSector(id+1); 
					Po.addPoint(now,Ta1,sec1);
					Po.addPoint(next,Ta2,sec2);
					resultCar.addPointAll(Po, car.getGpsPoint(id),car.getPci(id),car.getTime(id));
					//�����Ѿ�ȡ������㣬����Ҫ������Ĺ���
					nowid=0;
				}
			}
			else if(Algorithm.Fabs(Ta1-Ta2)>50.0){//��ʾ����һ��Ta�仯��
				AnchorPoint Po = new AnchorPoint();
				//double distance=Algorithm.Distance(car.getAnchorPoint(id+1).getPoint(0), car.getGpsPoint(id+1));
				if(Ta1<Ta2){//Ta��С���
					Po.type = 2;	
				}
				else{//Ta�Ӵ��С
					Po.type = 3;
				}
				Po.addPoint(next, Ta2, car.getSector(id+1));//ȡTaֵ�仯���Ǹ���
				resultCar.addPointAll(Po, car.getGpsPoint(id+1), car.getPci(id+1), car.getTime(id+1));
				nowid=0;
			}
			else if(nowid%INTERVAL_NUM==0){//��ͨ��λ��
				resultCar.addPointAll(car.getAnchorPoint(id), car.getGpsPoint(id), car.getPci(id),car.TimeSet.get(id));
			}
			nowid++;
		}
		return resultCar;
	}
	//��ȡ���˺�ĳ�������ѡȡ�ʵ��Ķ�λ��
	public static Car getCar(Car mycar,int interval,boolean isGPS){
		Car resultcar = mycar;
		INTERVAL_NUM=interval;
		if(!isGPS)
			System.out.println("����ǰ�������"+mycar.PointNum);
		resultcar = PreProcess.ErrorFilter(mycar);//��������
		if(!isGPS)
			System.out.println("���˺�������"+resultcar.PointNum);
		resultcar = PointChoose(resultcar,isGPS);//ѡȡ�ʵ��Ķ�λ��
		if(!isGPS)
			System.out.println("ѡȡ���������:"+resultcar.PointNum);
		return resultcar;
	}
}

