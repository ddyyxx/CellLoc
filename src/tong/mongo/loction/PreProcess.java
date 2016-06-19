package tong.mongo.loction;

import tong.mongo.defclass.Car;
import tong.mongo.defclass.Point;

/*
 * 预处理，将输入数据中的噪声点去除
 */
public class PreProcess {
	
	private static int THRESHOLD = 2; //阈值 当两个相邻点的TA差值大于阈值，则认为出现噪声点
	
	public static void setThreshold(int Threshold){ //设置过滤阈值
		THRESHOLD = Threshold;
	}
	public static Car CarErrorFilter(Car car,int left,int right){//过滤相同基站的误差点
		Car ret=new Car();
		if(right-left<2)//如果点数太少直接去掉
			return ret;
		int[] stack= new int[right-left+1];
		int top=0;
		if(Algorithm.Fabs(car.getTa(left)-car.getTa(left+1))>50.0)//去掉基站切换第一个点（因为容易出现误差）
			left++;
		for(int i=left;i<=right;){
			if(top==0){//栈为空
				stack[top++]=i++;
			}
			else{
				int now=stack[top-1];
				double ta1=car.getTa(now);
				double ta2=car.getTa(i);
				double time = car.getTime(i)-car.getTime(now);//时间差
				if(Algorithm.Fabs(ta1-ta2)/time>=THRESHOLD*78.0){//出现异常点
					if(i==right||top>15){//若最后一个点出现异常或top比较大(也就是说集合中前面的结果比较准确)则认为第i个点有误差
						i++;
						continue;
					}
					int ed = i+5<=right?i+5:right;
					double meanTa=0.0;
					for(int j=i+1;j<=ed;j++){//向后看若干的点,求Ta的平均值
						meanTa+=car.getAnchorPoint(j).getTa(0);
					}
					meanTa/=(ed-i);//表示后面几个点的平均Ta
					if(Algorithm.Fabs(meanTa-ta2)<=THRESHOLD*78.0){//认为栈顶点出现错误
						top--;//将栈顶点弹出
					}
					else{//认为第i个点错误
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
	public static Car ErrorFilter(Car car){//将误差较大的点过滤掉
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
