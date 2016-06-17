package tong.mongo.loction;

import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Line;
import tong.mongo.defclass.Point;
import tong.mongo.defclass.sector;
//算法类，提供各种算法相关接口
public class Algorithm{
	//函数定义
	private static double PI  = 3.141592653589793238462643383279502884;
	private static double EPS = 1e-8;
	
	public static double Rad(double d) {
	    return d * PI / 180.0;
	}
	
	public static double Distance(double lat1, double lng1, double lat2,double lng2) { //求两个点之间的距离
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
	
	public static double Distance(Point p1,Point p2){//两点之间距离
	    double lat1=p1.x, lng1=p1.y,lat2=p2.x,lng2=p2.y;
	    return Distance(lat1,lng1,lat2,lng2);
	}
	
	public static double Fabs(double x){//绝对值
		return x>0?x:-x;
	}
	
	public static boolean compare(Point a,Point b){//判断两点是否在同一点
	    if(Fabs(a.x-b.x)<EPS && Fabs(a.y-b.y)<EPS)
	    	return true;
	    return false;
	}

	public static double xmult(Point p1, Point p2,Point p0){//求叉积
	    double t1=p1.x-p0.x;
	    double t2=p2.y-p0.y;
	    double t3=p2.x-p0.x;
	    double t4=p1.y-p0.y;
	    return t1*t2-t3*t4;
	}

	public static Point intersection(Line u,Line v){//计算两直线交点 注意是直线（若要计算线段相交得先判断线段是否相交） （之前要判断是否有交点）
		Point ret=new Point(u.p[0]);
	    double x=((u.p[0].x-v.p[0].x)*(v.p[0].y-v.p[1].y)-(u.p[0].y-v.p[0].y)*(v.p[0].x-v.p[1].x));
	    double y=((u.p[0].x-u.p[1].x)*(v.p[0].y-v.p[1].y)-(u.p[0].y-u.p[1].y)*(v.p[0].x-v.p[1].x));
	    double t=x/y;
	    
	    ret.x+=(u.p[1].x-u.p[0].x)*t;
	    ret.y+=(u.p[1].y-u.p[0].y)*t;
	    return ret;
	}
	
	public static Point ptoseg(Point p,Line l){//点到线段上最近的点
		
	    if(compare(l.p[0],l.p[1]))//如果两个点是同一点
	    	return new Point(l.p[0]);
	    Point t=new Point(p);
	    if(((Double)t.x).equals(Double.NaN)){
	    	p.print();
	    	l.p[0].print();
	    	l.p[1].print();
	    	System.out.println("输入点异常");
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
	
	public static double disptoseg(Point p,Line L){//求点到线段的距离
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
	
	public static double gps2d(double lat_a, double lng_a, double lat_b, double lng_b) { //返回点b相对于a的角度
		double d = 0;
		lat_a = lat_a * Math.PI / 180;
		lng_a = lng_a * Math.PI / 180;
		lat_b = lat_b * Math.PI / 180;
		lng_b = lng_b * Math.PI / 180;
		d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a)
				* Math.cos(lat_b) * Math.cos(lng_b - lng_a);
		assert(d>0.0||d<0.0);
		d = Math.sqrt(1 - d * d);
		d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;//这里可能会丢失精度
		//下面要进行调整,保证d的值在[-1.0,1.0]范围内,有时候会掉精度，有时间自己写一个吧
		if(d>1.0)
			d=1.0;
		if(d<-1.0)
			d=-1.0;
		d = Math.asin(d) * 180 / Math.PI;
		if(lat_b-lat_a<0)
			d = 180-d;
		if(d<0) d = 360+d; 	
		// 变换 由原来的初始位置为北方改为初始位置为东方，并且逆时针改为正方向
		d = (90-d+360)%360;
		return d;
	}
	
	public static double getAngel(Point p1,Point p2){//返回p2相对于p1的角度
		return gps2d(p1.x,p1.y,p2.x,p2.y);
	}
	
	public static Point MinDistPtoLine(Point p,Line L){//点到线段上最近的点 精确值(三分法)
	    if(compare(L.p[0],L.p[1]))//如果两个点是同一点
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
	
	public static boolean SectorIntersectLine(Line L,Point p,double Ta,sector sec,double downerror,double uperror){//判断弧是否和扇形相交
		double angle1=getAngel(p,L.p[0]),angle2=getAngel(p,L.p[1]); //弧的两端点相对基站的角度
		if(angle1>angle2){
			double tmp=angle1;
			angle1=angle2;
			angle2=tmp;
		}
		if(angle2-angle1>180.0){//说明是从angle2到angle1
			double tmp=angle1;
			angle1=angle2;
			angle2=tmp;
		}
		//到这里 保证弧相对于点的角度范围是 angle1到angle2
		// 判断两个扇形是否有交集
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
		double mindist = Distance(p,MinDistPtoLine(p,L));//弧到基站的最小距离
		double maxdist = Math.max(Distance(p,L.p[0]), Distance(p,L.p[1]));//弧到基站的最大距离
		if(maxdist<Ta-downerror||mindist>Ta+uperror)//如果距离过大或过小则返回no
			return false;
		return true;
	}
	
	public static boolean islegalLine(Line L,AnchorPoint p){//判断弧L是否属于定位点P的候选弧集
		if(p.type==0){//普通点 保证弧与扇形相交，扇形误差范围较大（80米）
			return SectorIntersectLine(L,p.getPoint(0),p.getTa(0),p.getSector(0),80.0,80.0);
		}
		else if(p.type == 1){//切换点
			//System.out.println("切换点");
			for(int id=0;id<2;id++){//需要保证这条弧在两扇形的交集中
				if(!SectorIntersectLine(L,p.getPoint(id),p.getTa(id),p.getSector(id),80.0,80.0))
					return false;
			}
			return true;
		}
		else if(p.type==2){//TA变化点 (从小变大)
			return SectorIntersectLine(L,p.getPoint(0),p.getTa(0),p.getSector(0),80.0,0.0);
		}
		else if(p.type==3){//TA变化点（从大变小）
			return SectorIntersectLine(L,p.getPoint(0),p.getTa(0),p.getSector(0),0.0,80.0);
		}
		else{
			assert(false);
			System.out.println("定位点出现了第5中可能，一定是哪里搞错了!!");
			return false;
		}
	}
	
	public static Point getLocationPoint(Point p,double TA,Line L){//返回在弧上的定位点(直接返回中点竟然效果更好。。。。)
		//return MinDistPtoLine(p,L);//返回弧上离点p最近的点
		return L.getmidpoint();  //返回弧的中点
	}
	
	public static double sigmoid(double x){
		return 1.0/(Math.exp(-x)+1.0);
	}
}
