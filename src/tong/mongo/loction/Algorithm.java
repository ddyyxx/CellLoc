package tong.mongo.loction;

import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Line;
import tong.mongo.defclass.Point;
import tong.mongo.defclass.sector;
//�㷨�࣬�ṩ�����㷨��ؽӿ�
public class Algorithm{
	//��������
	private static double PI  = 3.141592653589793238462643383279502884;
	private static double EPS = 1e-8;
	
	public static double Rad(double d) {
	    return d * PI / 180.0;
	}
	
	public static double Distance(double lat1, double lng1, double lat2,double lng2) { //��������֮��ľ���
		double radLat1 = Rad(lat1);
		double radLat2 = Rad(lat2);
		double a = radLat1 - radLat2;
		double b = Rad(lng1) - Rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * 6378137.0;
		return s;
	}
	
	public static double Distance(Point p1,Point p2){//����֮�����
	    double lat1=p1.x, lng1=p1.y,lat2=p2.x,lng2=p2.y;
	    return Distance(lat1,lng1,lat2,lng2);
	}
	
	public static double Fabs(double x){//����ֵ
		return x>0?x:-x;
	}
	
	public static boolean compare(Point a,Point b){//�ж������Ƿ���ͬһ��
	    if(Fabs(a.x-b.x)<EPS && Fabs(a.y-b.y)<EPS)
	    	return true;
	    return false;
	}

	public static double xmult(Point p1, Point p2,Point p0){//����
	    double t1=p1.x-p0.x;
	    double t2=p2.y-p0.y;
	    double t3=p2.x-p0.x;
	    double t4=p1.y-p0.y;
	    return t1*t2-t3*t4;
	}

	public static Point intersection(Line u,Line v){//������ֱ�߽��� ע����ֱ�ߣ���Ҫ�����߶��ཻ�����ж��߶��Ƿ��ཻ�� ��֮ǰҪ�ж��Ƿ��н��㣩
		Point ret=new Point(u.p[0]);
	    double x=((u.p[0].x-v.p[0].x)*(v.p[0].y-v.p[1].y)-(u.p[0].y-v.p[0].y)*(v.p[0].x-v.p[1].x));
	    double y=((u.p[0].x-u.p[1].x)*(v.p[0].y-v.p[1].y)-(u.p[0].y-u.p[1].y)*(v.p[0].x-v.p[1].x));
	    double t=x/y;
	    
	    ret.x+=(u.p[1].x-u.p[0].x)*t;
	    ret.y+=(u.p[1].y-u.p[0].y)*t;
	    return ret;
	}
	
	public static Point ptoseg(Point p,Line l){//�㵽�߶�������ĵ�
		
	    if(compare(l.p[0],l.p[1]))//�����������ͬһ��
	    	return new Point(l.p[0]);
	    Point t=new Point(p);
	    if(((Double)t.x).equals(Double.NaN)){
	    	p.print();
	    	l.p[0].print();
	    	l.p[1].print();
	    	System.out.println("������쳣");
	    }
	    t.x+=l.p[0].y-l.p[1].y;
	    t.y+=l.p[1].x-l.p[0].x;
	    if(xmult(l.p[0],t,p)*xmult(l.p[1],t,p)>EPS)
	    	return Distance(p,l.p[0])<Distance(p,l.p[1])?new Point(l.p[0]):new Point(l.p[1]);
	    Line tmp=new Line();
	    tmp.p[0]=new Point(t);
	    tmp.p[1]=new Point(p);
	    return intersection(tmp,l);
	}
	
	public static double disptoseg(Point p,Line L){//��㵽�߶εľ���
	    double a=Distance(L.p[0],L.p[1]);
	    double b=Distance(p,L.p[0]);
	    double c=Distance(p,L.p[1]);
	    if(c*c>=b*b+a*a)
	    return b;
	    else if(b*b>c*c+a*a)
	    return c;
	    else
	    return Distance(p,ptoseg(p,L));
	}
	
	public static double gps2d(double lat_a, double lng_a, double lat_b, double lng_b) { //���ص�b�����a�ĽǶ�
		double d = 0;
		lat_a = lat_a * Math.PI / 180;
		lng_a = lng_a * Math.PI / 180;
		lat_b = lat_b * Math.PI / 180;
		lng_b = lng_b * Math.PI / 180;
		d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a)
				* Math.cos(lat_b) * Math.cos(lng_b - lng_a);
		assert(d>0.0||d<0.0);
		d = Math.sqrt(1 - d * d);
		d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;//������ܻᶪʧ����
		//����Ҫ���е���,��֤d��ֵ��[-1.0,1.0]��Χ��,��ʱ�������ȣ���ʱ���Լ�дһ����
		if(d>1.0)
			d=1.0;
		if(d<-1.0)
			d=-1.0;
		d = Math.asin(d) * 180 / Math.PI;
		if(lat_b-lat_a<0)
			d = 180-d;
		if(d<0) d = 360+d; 	
		// �任 ��ԭ���ĳ�ʼλ��Ϊ������Ϊ��ʼλ��Ϊ������������ʱ���Ϊ������
		d = (90-d+360)%360;
		return d;
	}
	
	public static double getAngel(Point p1,Point p2){//����p2�����p1�ĽǶ�
		return gps2d(p1.x,p1.y,p2.x,p2.y);
	}
	
	public static Point MinDistPtoLine(Point p,Line L){//�㵽�߶�������ĵ� ��ȷֵ(���ַ�)
	    if(compare(L.p[0],L.p[1]))//�����������ͬһ��
	    	return L.p[0];
	    Point res = new Point();
	    Point left =L.p[0],right = L.p[1];
	    for(int i=0;i<20;i++){
	    	Point lmid = new Point((left.x+right.x)*1.0/3,(left.y+right.y)*1.0/3);
	    	Point rmid = new Point((left.x+right.x)*2.0/3,(left.y+right.y)*2.0/3);
	    	if(Distance(lmid,p)<Distance(rmid,p)){
	    		res=lmid;
	    		right=rmid;
	    	}
	    	else{
	    		res=rmid;
	    		left=lmid;
	    	}
	    }
	    return res;
	}
	
	public static boolean SectorIntersectLine(Line L,Point p,double Ta,sector sec,double downerror,double uperror){//�жϻ��Ƿ�������ཻ
		double angle1=getAngel(p,L.p[0]),angle2=getAngel(p,L.p[1]); //�������˵���Ի�վ�ĽǶ�
		if(angle1>angle2){
			double tmp=angle1;
			angle1=angle2;
			angle2=tmp;
		}
		if(angle2-angle1>180.0){//˵���Ǵ�angle2��angle1
			double tmp=angle1;
			angle1=angle2;
			angle2=tmp;
		}
		//������ ��֤������ڵ�ĽǶȷ�Χ�� angle1��angle2
		// �ж����������Ƿ��н���
		if(sec.st<sec.ed){ 
			if(angle1<angle2){
				if(angle1>sec.ed||angle2<sec.st)
					return false;
			}
			else{
				if(sec.st>angle2&&sec.ed<angle1)
					return false;
			}
		}
		else{
			if(angle1<angle2){
				if(angle1>sec.ed&&angle2<sec.st)
					return false;
			}
		}
		double mindist = Distance(p,MinDistPtoLine(p,L));//������վ����С����
		double maxdist = Math.max(Distance(p,L.p[0]), Distance(p,L.p[1]));//������վ��������
		if(maxdist<Ta-downerror||mindist>Ta+uperror)//������������С�򷵻�no
			return false;
		return true;
	}
	
	public static boolean islegalLine(Line L,AnchorPoint p){//�жϻ�L�Ƿ����ڶ�λ��P�ĺ�ѡ����
		if(p.type==0){//��ͨ�� ��֤���������ཻ��������Χ�ϴ�80�ף�
			return SectorIntersectLine(L,p.getPoint(0),p.getTa(0),p.getSector(0),80.0,80.0);
		}
		else if(p.type == 1){//�л���
			//System.out.println("�л���");
			for(int id=0;id<2;id++){//��Ҫ��֤�������������εĽ�����
				if(!SectorIntersectLine(L,p.getPoint(id),p.getTa(id),p.getSector(id),80.0,80.0))
					return false;
			}
			return true;
		}
		else if(p.type==2){//TA�仯�� (��С���)
			return SectorIntersectLine(L,p.getPoint(0),p.getTa(0),p.getSector(0),80.0,0.0);
		}
		else if(p.type==3){//TA�仯�㣨�Ӵ��С��
			return SectorIntersectLine(L,p.getPoint(0),p.getTa(0),p.getSector(0),0.0,80.0);
		}
		else{
			assert(false);
			System.out.println("��λ������˵�5�п��ܣ�һ������������!!");
			return false;
		}
	}
	
	public static Point getLocationPoint(Point p,double TA,Line L){//�����ڻ��ϵĶ�λ��(ֱ�ӷ����е㾹ȻЧ�����á�������)
		//return MinDistPtoLine(p,L);//���ػ������p����ĵ�
		return L.getmidpoint();  //���ػ����е�
	}
	
	public static double sigmoid(double x){
		return 1.0/(Math.exp(-x)+1.0);
	}
}
