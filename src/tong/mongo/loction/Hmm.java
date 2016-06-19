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

//�û�վ��Ϣ����ƥ��
public class Hmm {
	double VARIANCE = 280.0;
	double PI = 3.141592653589793238462643383279502884;
	double INF = 2100000000.0;
	double LIMITDIS = 200.0;
	public static boolean USEGPS = false;// ��ʾ�����õ��ǻ�վ����
	public Graph Dij;
	public int MIDNUM = 15;
	public Vector<Vector<Point>> legalPoint;// �����л��Ķ�λ��

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

	public double GetP(double len) {// ״̬���� (��Ϊreturn 1)
		return 1.0;
	}

	public double GetTransP(Point u, long from, Point v, long to, MapLoc mymp) { // ת�Ƹ���
		double len = Algorithm.Distance(u, v);// ֱ�߾���
		len=0;
		if (((Double) u.x).equals(Double.NaN)
				|| ((Double) v.x).equals(Double.NaN)) {
			System.out.println("��λ�����");
		}
		double dt = Dij.GetDisAtoB(mymp, u, mymp.LineSet.get(from), v,
				mymp.LineSet.get(to), len,true);
		if (((Double) dt).equals(Double.NaN)) {
			System.out.println("���·����ʧ��");
		}
		if (dt == INF) {
			System.out.println("ת�Ƹ��ʼ���ʧ��");
			return 0.0;
		}

		double transp = Math.exp(-dt / VARIANCE); // ��ʽ1
		return transp;
	}

	public void Balance(Map<Long, Double> dp) {// ���滯
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

	public double getalpha(MapLoc mymp, long from, long to) {// ���شӻ�from����to�ķ���
		if (from == to || from == -1 || to == -1)
			return 0.0;
		Line L1 = mymp.getLine(from), L2 = mymp.getLine(to);
		Point P1 = L1.getmidpoint();
		Point P2 = L2.getmidpoint();
		double angle = Algorithm.getAngel(P1, P2);
		return angle;
	}

	public void GetPath(Car car, int st, int ed, MapLoc mymp, long preline,
			Vector<Long> path) {// ������ʽ����Ʒ���ƥ��·��(Viterbi�㷨)
		int n = ed - st + 1;
		@SuppressWarnings("unchecked")
		Map<Long, Double>[] dp = new Map[n];// ����Ʒ������
		@SuppressWarnings("unchecked")
		Map<Long, Long>[] pre = new Map[n];// ǰ��
		for (int i = 0; i < n; i++) {
			dp[i] = new HashMap<Long, Double>();
			pre[i] = new HashMap<Long, Long>();
		}
		int size = 0;
		// System.out.println(st+" "+ed+" "+car.legalline.size());
		// ��ʼ��dp��ǰ������
		for (int i = 0; i < n; i++) {
			size = car.legalline.get(i + st).size();
			for (int j = 0; j < size; j++) {
				long id = car.legalline.get(i + st).get(j);
				dp[i].put(id, 0.0);
				pre[i].put(id, (long) -1);
			}
		}
		size = car.legalline.get(st).size();
		boolean lastresult = false;// ��һ�ν���Ƿ�Ϊ��
		for (int i = 0; i < size; i++) {// �����һ�����״̬����
			long id = car.legalline.get(st).get(i);
			Point point = legalPoint.get(st).get(i);
			double initP = GetP(Algorithm.disptoseg(point, mymp.LineSet.get(id)));
			if (preline != -1) {// Ҫ������һ��ƥ������������ת��
				if (mymp.LineSet.containsKey(preline)) {// ����������
					Line lastline = mymp.getLine(preline);
					Point lastpoint = lastline.getmidpoint();
					initP *= GetTransP(lastpoint, preline, point, id, mymp);
				}
			}
			if (initP > 0.0)
				lastresult = true;
			dp[0].put(id, initP);
		}
		if (!lastresult) { // �����һ�ε�ƥ���������޷�ת�Ƶ���һ������κ�һ����ѡ���Σ���������һ��ƥ����
			for (int i = 0; i < size; i++) {// �����һ�����״̬����
				long id = car.legalline.get(st).get(i);
				Point point = legalPoint.get(st).get(i);
				double initP = GetP(Algorithm.disptoseg(point, mymp.LineSet.get(id)));
				dp[0].put(id, initP);
			}
		}

		Balance(dp[0]);
		for (int i = st + 1; i <= ed; i++) {
			int s1 = car.legalline.get(i - 1).size();
			for (int j = 0; j < s1; j++) {// ö�����
				long id1 = car.legalline.get(i - 1).get(j);// ��㻡��
				Point p1 = legalPoint.get(i - 1).get(j);
				if (dp[i - st - 1].get(id1) >= 0.0) {
					int s2 = car.legalline.get(i).size();
					for (int k = 0; k < s2; k++) {
						long id2 = car.legalline.get(i).get(k);// �յ㻡��
						Point p2 = legalPoint.get(i).get(k);
						double transp = GetTransP(p1, id1, p2, id2, mymp);// ת�Ƹ���
						if (i != st + 1) {// �ӵ������㿪ʼ
							long id0 = pre[i - st - 1].get(id1);
							double alpha = getalpha(mymp, id0, id1)
									- getalpha(mymp, id1, id2);
							if (alpha < 0)
								alpha = -alpha;
							if (alpha > 180.0)
								alpha = 360.0 - alpha;
							alpha = alpha * PI / 180.0;// ת��Ϊ������
							if (id1 == id2 || id0 == -1)
								alpha = 0;
							assert (alpha <= PI);
							double P = dp[i - st - 1].get(id1);
							transp *= (1.0 + P
									* (Algorithm.sigmoid(2*(PI/2  - alpha)) - 0.5));
						}
						double dis = Algorithm.disptoseg(p2, mymp.LineSet.get(id2));// ��p2����id2�ľ���

						double P = dp[i - st - 1].get(id1) * transp * GetP(dis);
						assert (P >= 0.0);
						if (P > dp[i - st].get(id2)) {
							dp[i - st].put(id2, P);
							pre[i - st].put(id2, id1);// ��id1ת�ƹ���
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
		// ���ﲻ��
		if (po == -1) {
			car.debug();
			System.out.println("ƥ��켣Ϊ��");
			return;
		}
		path.clear();
		for (int i = n - 1; i >= 0; i--) {
			path.add(po);
			po = pre[i].get(po);
		}
		for (int i = 0; i < n / 2; i++) {// ��ת·������
			long tmp = path.get(i);
			path.set(i, path.get(n - i - 1));
			path.set(n - i - 1, tmp);
		}
	}

	public void getLegalSet(MapLoc mymp, Car car) {// ��ú�ѡ��
		if (car.legalline.size() != 0)// ����������Ѿ����ں�ѡ�������˳�
			return;
		int pnum = car.PointNum;
		for (int i = 0; i < pnum; i++) {
			Vector<Long> lgline = new Vector<Long>();
			AnchorPoint po = car.getAnchorPoint(i);
			for (Long id : mymp.LineSet.keySet()) {
				if (Algorithm.islegalLine(mymp.getLine(id), po))
					lgline.add(id);// �����ѡ��
			}
			car.legalline.add(lgline);
		}
	}

	public Car getcar(Car car) {// ����ѡ��Ϊ0�ĳ��ĵ㶪��
		int n = car.legalline.size();
		Car ret = new Car();
		for (int i = 0; i < n; i++) {
			if (car.legalline.get(i).size() != 0) {
				ret.addPointAll(car.getAnchorPoint(i), car.getGpsPoint(i),
						car.getPci(i), car.getTime(i));
				ret.legalline.add(car.legalline.get(i));// �����ѡ��
			}
		}
		return ret;
	}

	public void getLegalPoint(MapLoc mymp, Car car) {// ��ó���ÿ����ѡ���ϵĶ�λ��
		legalPoint = new Vector<Vector<Point>>();
		int n = car.legalline.size();
		for (int i = 0; i < n; i++) {// ����ÿ�����ĺ�ѡ��
			Vector<Point> pset = new Vector<Point>();
			int m = car.legalline.get(i).size();
			Point nowPoint = car.getAnchorPoint(i).getPoint(0);// ��վ��
			double Ta = car.getTa(i);// ����
			for (int j = 0; j < m; j++) {
				Line nowLine = mymp.LineSet.get(car.legalline.get(i).get(j));
				Point pointnow = Algorithm.getLocationPoint(nowPoint, Ta, nowLine);
				if (((Double) pointnow.x).equals(Double.NaN)) {
					nowPoint.print();
					nowLine.p[0].print();
					nowLine.p[1].print();
					System.out.println("ȡ���϶�λ�����");
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
		getLegalSet(mymp, car);// ��ȡ��ѡ����
		car = getcar(car);// ȥ����ѡ��Ϊ0�ĵ�
		getLegalPoint(mymp, car);// ��ó���ÿ����ѡ���ϵĶ�λ��
		// car.debug();
		int ponum = car.PointNum;// ���������¶���㼯��
		if (ponum == 0)
			return;
		Vector<Long> tmporbit = new Vector<Long>();
		GetPath(car, 0, ponum - 1, mymp, preline, tmporbit);
		int size = tmporbit.size();
		assert (size == ponum);
		// ����Ҫ��һ�ι���
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
