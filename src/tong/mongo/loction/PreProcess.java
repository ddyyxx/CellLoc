package tong.mongo.loction;

import tong.mongo.defclass.Car;
import tong.mongo.defclass.Point;

/*
 * Ԥ���������������е�������ȥ��
 */
public class PreProcess {
	
	private static int THRESHOLD = 2; //��ֵ ���������ڵ��TA��ֵ������ֵ������Ϊ����������
	
	public static void setThreshold(int Threshold){ //���ù�����ֵ
		THRESHOLD = Threshold;
	}
	public static Car CarErrorFilter(Car car,int left,int right){//������ͬ��վ������
		Car ret=new Car();
		if(right-left<2)//�������̫��ֱ��ȥ��
			return ret;
		int[] stack= new int[right-left+1];
		int top=0;
		if(Algorithm.Fabs(car.getTa(left)-car.getTa(left+1))>50.0)//ȥ����վ�л���һ���㣨��Ϊ���׳�����
			left++;
		for(int i=left;i<=right;){
			if(top==0){//ջΪ��
				stack[top++]=i++;
			}
			else{
				int now=stack[top-1];
				double ta1=car.getTa(now);
				double ta2=car.getTa(i);
				double time = car.getTime(i)-car.getTime(now);//ʱ���
				if(Algorithm.Fabs(ta1-ta2)/time>=THRESHOLD*78.0){//�����쳣��
					if(i==right||top>15){//�����һ��������쳣��top�Ƚϴ�(Ҳ����˵������ǰ��Ľ���Ƚ�׼ȷ)����Ϊ��i���������
						i++;
						continue;
					}
					int ed = i+5<=right?i+5:right;
					double meanTa=0.0;
					for(int j=i+1;j<=ed;j++){//������ɵĵ�,��Ta��ƽ��ֵ
						meanTa+=car.getAnchorPoint(j).getTa(0);
					}
					meanTa/=(ed-i);//��ʾ���漸�����ƽ��Ta
					if(Algorithm.Fabs(meanTa-ta2)<=THRESHOLD*78.0){//��Ϊջ������ִ���
						top--;//��ջ���㵯��
					}
					else{//��Ϊ��i�������
						i++;
					}
				}
				else
					stack[top++]=i++;
			}
		}
		for(int i=0;i<top;i++){
			int id=stack[i];
			ret.addPointAll(car.getAnchorPoint(id), car.getGpsPoint(id),car.getPci(id),car.TimeSet.get(id));
		}
		return ret;
	}
	public static Car ErrorFilter(Car car){//�����ϴ�ĵ���˵�
		int n = car.PointNum;
		Car resultCar = new Car();
		int left=0,right=0;
		Point pre = car.getAnchorPoint(0).getPoint(0);
		for(int i=0;i<n;i++){
			Point now = car.getAnchorPoint(i).getPoint(0);
			if(Algorithm.compare(pre, now))
				right=i;
			else{
				resultCar.addCar(CarErrorFilter(car,left,right));
				left=right=i;
				pre=now;
			}
		}
		resultCar.addCar(CarErrorFilter(car,left,right));
		return resultCar;
	}
}
