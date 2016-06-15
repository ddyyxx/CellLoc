package tong.mongo.loction;


import java.util.HashSet;
import java.util.Vector;

import tong.mongo.defclass.Car;
import tong.mongo.defclass.Node;
import tong.mongo.defclass.Point;

//ʹ·��ƽ������ȥ�������·
public class PointFilter {
	
	public PointFilter(){
		Alg=new Algorithm();
	}
	public Algorithm Alg;//�㷨��
	public int postnum=10;//��ʾ��󿴵ĵ���
	static double COEFFICIENT = 78.0/16.0;
	public Vector<Node> eraseSamePoint(Vector<Node> LpSet){//ȥ���ظ���
		Vector<Node> ret=new Vector<Node>();
		int size=LpSet.size();
		ret.add(LpSet.get(0));
		for(int i=1;i<size;i++){
				if(LpSet.get(i).lineid!=LpSet.get(i-1).lineid)
					ret.add(LpSet.get(i));
		}
		return ret;
	}
	
	public Car CarErrorFilter(Car car,int left,int right){//������ͬ��վ������
		
		Car ret=new Car();
		if(right-left<2)//�������̫��ֱ��ȥ��
			return ret;
		int[] stack= new int[right-left+1];
		int top=0;
		if(Alg.Fabs(car.getTa(left)-car.getTa(left+1))>50.0)//ȥ����վ�л���һ���㣨��Ϊ���׳�����
			left++;
		for(int i=left;i<=right;){
			if(top==0){//ջΪ��
				stack[top++]=i++;
			}
			else{
				int now=stack[top-1];
				double ta1=car.getTa(now)/COEFFICIENT;
				double ta2=car.getTa(i)/COEFFICIENT;
				double time = car.getTime(i)-car.getTime(now);//ʱ���
				if(Alg.Fabs(ta1-ta2)/time>32.0){//�����쳣��
					//System.out.println(ta1+" "+ta2);
					if(i==right||top>15){//�����һ��������쳣��top�Ƚϴ�(Ҳ����˵������ǰ��Ľ���Ƚ�׼ȷ)����Ϊ��i���������
						i++;
						continue;
					}
					int ed = i+5<=right?i+5:right;
					double meanTa=0.0;
					for(int j=i+1;j<=ed;j++){//������ɵĵ�,��Ta��ƽ��ֵ
						meanTa+=car.getAnchorPoint(j).getTa(0)/COEFFICIENT;
					}
					meanTa/=(ed-i);//��ʾ���漸�����ƽ��Ta
					if(Alg.Fabs(meanTa-ta2)<48.0){//��Ϊջ������ִ���
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
	public Car ErrorFilter(Car car){//�����ϴ�ĵ���˵�
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
	/////////////////////////////////������Ԥ����//////////////////////////////////////
	/////////////////////////////////�����Ǻ���/////////////////////////////////////
	public double getAnglebtPoint(Point A,Point B,Point C){//���ؽ�ABC
		double AngleBA=Alg.getAngel(B, A);
		double AngleBC=Alg.getAngel(B, C);
		double Angle=Math.abs(AngleBA-AngleBC);
		if(Angle>=180.0)
			Angle=360.0-Angle;
		assert(Angle>=0.0);
		return Angle;
	}
	
	public Vector<Node> FilterByAngle(Vector<Node> LpSet){//�����ݽǶȹ���ƥ���
		Vector<Node> ret=new Vector<Node>();
		if(LpSet.size()==0)
			return ret;
		LpSet=eraseSamePoint(LpSet);//ȥ���ظ���
		//���ҵ���һ����ȷ����ĳ�ʼ��
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
		if(now>0){//˵���ҵ��˵�һ����
			Point prepoint=LpSet.get(now-1).po;
			for(int i=now+1;i+postnum<n;i++){
				Point A=LpSet.get(now).po;
				Point B=LpSet.get(i).po;
				Point C=LpSet.get(i+postnum).po;
				if(getAnglebtPoint(B,A,prepoint)<15.0){
					continue;
				}
				if(getAnglebtPoint(B,A,C)>90.0){//�������Ҫ��
					continue;
				}
				if(LpSet.get(now).lineid==LpSet.get(i).lineid)//A��B���
					continue;
				prepoint=A;
				now=i;
				ret.add(LpSet.get(i));
			}
		}
		if(now==0){//��û���ҵ���һ����(���ָ���Ӧ�ò����) һ�ֿ����ǵ�̫��
			System.out.println("�е���� ���� = "+n);
			ret.add(LpSet.get(0));
			ret.add(LpSet.get(1));
			now=1;
		}
		//��ʣ�µĵ����ɸѡ
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
	
	public Vector<Node> FilterByArc(Vector<Node> LpSet){ //�����ݻ���ID���й���
		
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
	
	public Vector<Node> Filter(Vector<Node> LpSet){
		//System.out.println("��ʼ����");
		return FilterByArc(LpSet);//Ŀǰֻʹ�ø��ݻ���ID���й���
	}
}

