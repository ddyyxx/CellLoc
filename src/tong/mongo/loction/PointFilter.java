package tong.mongo.loction;


import java.util.HashSet;
import java.util.Vector;

import tong.mongo.defclass.Car;
import tong.mongo.defclass.Node;
import tong.mongo.defclass.Point;

//使路径平滑化，去除反向回路
public class PointFilter {
	
	public PointFilter(){
		Alg=new Algorithm();
	}
	public Algorithm Alg;//算法类
	public int postnum=10;//表示向后看的点数
	static double COEFFICIENT = 78.0/16.0;
	public Vector<Node> eraseSamePoint(Vector<Node> LpSet){//去除重复点
		Vector<Node> ret=new Vector<Node>();
		int size=LpSet.size();
		ret.add(LpSet.get(0));
		for(int i=1;i<size;i++){
				if(LpSet.get(i).lineid!=LpSet.get(i-1).lineid)
					ret.add(LpSet.get(i));
		}
		return ret;
	}
	
	public Car CarErrorFilter(Car car,int left,int right){//过滤相同基站的误差点
		
		Car ret=new Car();
		if(right-left<2)//如果点数太少直接去掉
			return ret;
		int[] stack= new int[right-left+1];
		int top=0;
		if(Alg.Fabs(car.getTa(left)-car.getTa(left+1))>50.0)//去掉基站切换第一个点（因为容易出现误差）
			left++;
		for(int i=left;i<=right;){
			if(top==0){//栈为空
				stack[top++]=i++;
			}
			else{
				int now=stack[top-1];
				double ta1=car.getTa(now)/COEFFICIENT;
				double ta2=car.getTa(i)/COEFFICIENT;
				double time = car.getTime(i)-car.getTime(now);//时间差
				if(Alg.Fabs(ta1-ta2)/time>32.0){//出现异常点
					//System.out.println(ta1+" "+ta2);
					if(i==right||top>15){//若最后一个点出现异常或top比较大(也就是说集合中前面的结果比较准确)则认为第i个点有误差
						i++;
						continue;
					}
					int ed = i+5<=right?i+5:right;
					double meanTa=0.0;
					for(int j=i+1;j<=ed;j++){//向后看若干的点,求Ta的平均值
						meanTa+=car.getAnchorPoint(j).getTa(0)/COEFFICIENT;
					}
					meanTa/=(ed-i);//表示后面几个点的平均Ta
					if(Alg.Fabs(meanTa-ta2)<48.0){//认为栈顶点出现错误
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
	public Car ErrorFilter(Car car){//将误差较大的点过滤掉
		int n = car.PointNum;
		Car resultCar = new Car();
		int left=0,right=0;
		Point pre = car.getAnchorPoint(0).getPoint(0);
		for(int i=0;i<n;i++){
			Point now = car.getAnchorPoint(i).getPoint(0);
			if(Alg.compare(pre, now))
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
	/////////////////////////////////以上是预处理//////////////////////////////////////
	/////////////////////////////////以下是后处理/////////////////////////////////////
	public double getAnglebtPoint(Point A,Point B,Point C){//返回角ABC
		double AngleBA=Alg.getAngel(B, A);
		double AngleBC=Alg.getAngel(B, C);
		double Angle=Math.abs(AngleBA-AngleBC);
		if(Angle>=180.0)
			Angle=360.0-Angle;
		assert(Angle>=0.0);
		return Angle;
	}
	
	public Vector<Node> FilterByAngle(Vector<Node> LpSet){//仅根据角度过滤匹配点
		Vector<Node> ret=new Vector<Node>();
		if(LpSet.size()==0)
			return ret;
		LpSet=eraseSamePoint(LpSet);//去除重复点
		//先找到第一个正确方向的初始点
		int now=0,n=LpSet.size();
		for(int i=0;i+postnum<n;i++){
			Point A=LpSet.get(i).po;
			Point B=LpSet.get(i+1).po;
			Point C=LpSet.get(i+postnum).po;
			if(getAnglebtPoint(B,A,C)<90.0){
				ret.add(LpSet.get(i));
				ret.add(LpSet.get(i+1));
				now=i+1;
				break;
			}
		}
		if(now>0){//说明找到了第一个点
			Point prepoint=LpSet.get(now-1).po;
			for(int i=now+1;i+postnum<n;i++){
				Point A=LpSet.get(now).po;
				Point B=LpSet.get(i).po;
				Point C=LpSet.get(i+postnum).po;
				if(getAnglebtPoint(B,A,prepoint)<15.0){
					continue;
				}
				if(getAnglebtPoint(B,A,C)>90.0){//这里可能要改
					continue;
				}
				if(LpSet.get(now).lineid==LpSet.get(i).lineid)//A和B相等
					continue;
				prepoint=A;
				now=i;
				ret.add(LpSet.get(i));
			}
		}
		if(now==0){//并没有找到第一个点(这种概率应该不大吧) 一种可能是点太少
			System.out.println("有点奇怪 点数 = "+n);
			ret.add(LpSet.get(0));
			ret.add(LpSet.get(1));
			now=1;
		}
		//对剩下的点进行筛选
		Point prepoint=LpSet.get(now-1).po;
		for(int i=now+1;i<n;i++){
			Point A=LpSet.get(now).po;
			Point B=LpSet.get(i).po;
			if(getAnglebtPoint(B,A,prepoint)<15.0){
				continue;
			}
			
			prepoint=A;
			now=i;
			ret.add(LpSet.get(i));
		}
		return ret;
	}
	
	public Vector<Node> FilterByArc(Vector<Node> LpSet){ //仅根据弧段ID进行过滤
		
		Vector<Node> ret =new Vector<Node>();
		if(LpSet.size()==0)//如果为空则直接返回
			return ret;
		HashSet<Long> ArcSet=new HashSet<Long>();
		int n=LpSet.size(),top=0;
		Node[] stack =new Node[n+1];//栈
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
	
	public Vector<Node> Filter(Vector<Node> LpSet){
		//System.out.println("开始后处理");
		return FilterByArc(LpSet);//目前只使用根据弧段ID进行过滤
	}
}

