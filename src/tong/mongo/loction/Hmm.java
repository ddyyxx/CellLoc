package tong.mongo.loction;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Car;
import tong.mongo.defclass.Line;
import tong.mongo.defclass.MapLoc;
import tong.mongo.defclass.Point;
import tong.mongo.defclass.Node;

//用基站信息进行匹配
public class Hmm {
	double VARIANCE = 280.0;
	double PI = 3.141592653589793238462643383279502884;
	double INF = 2100000000.0;
	double LIMITDIS = 200.0;
	public static boolean USEGPS = false;// 表示这里用的是基站坐标
	public Graph Dij;
	public int MIDNUM = 15;
	public Vector<Vector<Point>> legalPoint;// 到可行弧的定位点

	public Hmm() {}

	public Hmm(Graph dij) {
		this.Dij = dij;
	}

	public double Min(double a, double b) {
		return a < b ? a : b;
	}

	public double Fabs(double x) {
		return x < 0 ? -x : x;
	}

	public double GetP(double len) {// 状态概率 (改为return 1)
		return 1.0;
	}

	public double GetTransP(Point u, long from, Point v, long to, MapLoc mymp) { // 转移概率
		double len = Algorithm.Distance(u, v);// 直线距离
		len=0;
		if (((Double) u.x).equals(Double.NaN)
				|| ((Double) v.x).equals(Double.NaN)) {
			System.out.println("定位点出错");
		}
		double dt = Dij.GetDisAtoB(mymp, u, mymp.LineSet.get(from), v,
				mymp.LineSet.get(to), len,true);
		if (((Double) dt).equals(Double.NaN)) {
			System.out.println("最短路计算失败");
		}
		if (dt == INF) {
			System.out.println("转移概率计算失败");
			return 0.0;
		}

		double transp = Math.exp(-dt / VARIANCE); // 公式1
		return transp;
	}

	public void Balance(Map<Long, Double> dp) {// 正规化
		double sum = 0.0;
		for (long id : dp.keySet()) {
			sum += dp.get(id);
		}
		for (long id : dp.keySet()) {
			double tmp = dp.get(id);
			assert (sum > 0.0);
			dp.put(id, tmp / sum);
		}
	}

	public double getalpha(MapLoc mymp, long from, long to) {// 返回从弧from到弧to的方向
		if (from == to || from == -1 || to == -1)
			return 0.0;
		Line L1 = mymp.getLine(from), L2 = mymp.getLine(to);
		Point P1 = L1.getmidpoint();
		Point P2 = L2.getmidpoint();
		double angle = Algorithm.getAngel(P1, P2);
		return angle;
	}

	public void GetPath(Car car, int st, int ed, MapLoc mymp, long preline,
			Vector<Long> path) {// 利用隐式马尔科夫求匹配路径(Viterbi算法)
		int n = ed - st + 1;
		@SuppressWarnings("unchecked")
		Map<Long, Double>[] dp = new Map[n];// 马尔科夫概率链
		@SuppressWarnings("unchecked")
		Map<Long, Long>[] pre = new Map[n];// 前向弧
		for (int i = 0; i < n; i++) {
			dp[i] = new HashMap<Long, Double>();
			pre[i] = new HashMap<Long, Long>();
		}
		int size = 0;
		// System.out.println(st+" "+ed+" "+car.legalline.size());
		// 初始化dp和前向弧数组
		for (int i = 0; i < n; i++) {
			size = car.legalline.get(i + st).size();
			for (int j = 0; j < size; j++) {
				long id = car.legalline.get(i + st).get(j);
				dp[i].put(id, 0.0);
				pre[i].put(id, (long) -1);
			}
		}
		size = car.legalline.get(st).size();
		boolean lastresult = false;// 上一次结果是否为真
		for (int i = 0; i < size; i++) {// 处理第一个点的状态概率
			long id = car.legalline.get(st).get(i);
			Point point = legalPoint.get(st).get(i);
			double initP = GetP(Algorithm.disptoseg(point, mymp.LineSet.get(id)));
			if (preline != -1) {// 要根据上一次匹配的最后结果进行转移
				if (mymp.LineSet.containsKey(preline)) {// 存在这条弧
					Line lastline = mymp.getLine(preline);
					Point lastpoint = lastline.getmidpoint();
					initP *= GetTransP(lastpoint, preline, point, id, mymp);
				}
			}
			if (initP > 0.0)
				lastresult = true;
			dp[0].put(id, initP);
		}
		if (!lastresult) { // 如果上一次的匹配结果错误（无法转移到第一个点的任何一条候选弧段）则舍弃上一次匹配结果
			for (int i = 0; i < size; i++) {// 处理第一个点的状态概率
				long id = car.legalline.get(st).get(i);
				Point point = legalPoint.get(st).get(i);
				double initP = GetP(Algorithm.disptoseg(point, mymp.LineSet.get(id)));
				dp[0].put(id, initP);
			}
		}

		Balance(dp[0]);
		for (int i = st + 1; i <= ed; i++) {
			int s1 = car.legalline.get(i - 1).size();
			for (int j = 0; j < s1; j++) {// 枚举起点
				long id1 = car.legalline.get(i - 1).get(j);// 起点弧段
				Point p1 = legalPoint.get(i - 1).get(j);
				if (dp[i - st - 1].get(id1) >= 0.0) {
					int s2 = car.legalline.get(i).size();
					for (int k = 0; k < s2; k++) {
						long id2 = car.legalline.get(i).get(k);// 终点弧段
						Point p2 = legalPoint.get(i).get(k);
						double transp = GetTransP(p1, id1, p2, id2, mymp);// 转移概率
						if (i != st + 1) {// 从第三个点开始
							long id0 = pre[i - st - 1].get(id1);
							double alpha = getalpha(mymp, id0, id1)
									- getalpha(mymp, id1, id2);
							if (alpha < 0)
								alpha = -alpha;
							if (alpha > 180.0)
								alpha = 360.0 - alpha;
							alpha = alpha * PI / 180.0;// 转化为弧度制
							if (id1 == id2 || id0 == -1)
								alpha = 0;
							assert (alpha <= PI);
							double P = dp[i - st - 1].get(id1);
							transp *= (1.0 + P
									* (Algorithm.sigmoid(2*(PI/2  - alpha)) - 0.5));
						}
						double dis = Algorithm.disptoseg(p2, mymp.LineSet.get(id2));// 点p2到弧id2的距离

						double P = dp[i - st - 1].get(id1) * transp * GetP(dis);
						assert (P >= 0.0);
						if (P > dp[i - st].get(id2)) {
							dp[i - st].put(id2, P);
							pre[i - st].put(id2, id1);// 从id1转移过来
						}
					}
				}
			}
			Balance(dp[i - st]);
		}
		double ma = -INF;
		long po = -1;
		for (long id : dp[n - 1].keySet()) {
			double tmp = dp[n - 1].get(id);
			if (tmp > ma) {
				ma = tmp;
				po = id;
			}
		}
		// 这里不对
		if (po == -1) {
			car.debug();
			System.out.println("匹配轨迹为空");
			return;
		}
		path.clear();
		for (int i = n - 1; i >= 0; i--) {
			path.add(po);
			po = pre[i].get(po);
		}
		for (int i = 0; i < n / 2; i++) {// 翻转路径序列
			long tmp = path.get(i);
			path.set(i, path.get(n - i - 1));
			path.set(n - i - 1, tmp);
		}
	}

	public void getLegalSet(MapLoc mymp, Car car) {// 获得候选集
		if (car.legalline.size() != 0)// 如果车本身已经存在候选集，则退出
			return;
		int pnum = car.PointNum;
		for (int i = 0; i < pnum; i++) {
			Vector<Long> lgline = new Vector<Long>();
			AnchorPoint po = car.getAnchorPoint(i);
			for (Long id : mymp.LineSet.keySet()) {
				if (Algorithm.islegalLine(mymp.getLine(id), po))
					lgline.add(id);// 加入候选集
			}
			car.legalline.add(lgline);
		}
	}

	public Car getcar(Car car) {// 将候选集为0的车的点丢掉
		int n = car.legalline.size();
		Car ret = new Car();
		for (int i = 0; i < n; i++) {
			if (car.legalline.get(i).size() != 0) {
				ret.addPointAll(car.getAnchorPoint(i), car.getGpsPoint(i),
						car.getPci(i), car.getTime(i));
				ret.legalline.add(car.legalline.get(i));// 加入候选集
			}
		}
		return ret;
	}

	public void getLegalPoint(MapLoc mymp, Car car) {// 获得车在每条候选弧上的定位点
		legalPoint = new Vector<Vector<Point>>();
		int n = car.legalline.size();
		for (int i = 0; i < n; i++) {// 遍历每辆车的候选集
			Vector<Point> pset = new Vector<Point>();
			int m = car.legalline.get(i).size();
			Point nowPoint = car.getAnchorPoint(i).getPoint(0);// 基站点
			double Ta = car.getTa(i);// 距离
			for (int j = 0; j < m; j++) {
				Line nowLine = mymp.LineSet.get(car.legalline.get(i).get(j));
				Point pointnow = Algorithm.getLocationPoint(nowPoint, Ta, nowLine);
				if (((Double) pointnow.x).equals(Double.NaN)) {
					nowPoint.print();
					nowLine.p[0].print();
					nowLine.p[1].print();
					System.out.println("取弧上定位点出错");
				}
				pset.add(pointnow);
			}
			legalPoint.add(pset);
		}
	}

	public Point getMidPoint(MapLoc mymp, long Lid) {
		Line L = mymp.getLine(Lid);
		Point Midpoint = new Point((L.p[0].x + L.p[1].x) / 2,
				(L.p[0].y + L.p[1].y) / 2);
		return Midpoint;
	}

	public void solve(MapLoc mymp, Car car, Vector<Node> orbit, long preline) {
		getLegalSet(mymp, car);// 获取候选集合
		car = getcar(car);// 去除候选集为0的点
		getLegalPoint(mymp, car);// 获得车在每条候选弧上的定位点
		// car.debug();
		int ponum = car.PointNum;// 在这里重新定义点集合
		if (ponum == 0)
			return;
		Vector<Long> tmporbit = new Vector<Long>();
		GetPath(car, 0, ponum - 1, mymp, preline, tmporbit);
		int size = tmporbit.size();
		assert (size == ponum);
		// 这里要做一次过滤
		Vector<Node> LpSet = new Vector<Node>();
		for (int i = 0; i < size; i++) {
			long Lid = tmporbit.get(i);
			Point po = getMidPoint(mymp, Lid);
			long time = car.getTime(i);
			LpSet.add(new Node(Lid, po, time));
		}
		size = LpSet.size();
		for (int i = 0; i < size - 1; i++) {
			orbit.add(LpSet.get(i));
			long S = LpSet.get(i).lineid, T = LpSet.get(i + 1).lineid;
			Vector<Node> midArc = new Vector<Node>();
			Dij.GetPobitAtoB(mymp, S, T, midArc);
			orbit.addAll(midArc);
		}
		if (size > 0)
			orbit.add(LpSet.get(size - 1));
	}
}
