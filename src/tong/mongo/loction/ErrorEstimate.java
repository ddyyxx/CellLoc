package tong.mongo.loction;

import java.io.IOException;
import java.util.Vector;

import tong.map.MapProcess.MapData;
import tong.mongo.defclass.Car;
import tong.mongo.defclass.Line;
import tong.mongo.defclass.Node;
import tong.mongo.defclass.Point;

import com.mongodb.DB;

/**
 * 
 * @author ddyyxx 
 * TaPrecise:
 *         	比较TA匹配结果与GPS匹配结果（认为是真实路径），计算TA匹配的precision，recall和F-Measure
 * DisError:
 * 		   	计算TA匹配结果与GPS匹配结果的地理距离误差（在论文中没有体现，没有进行优化，这部分可能需要改进）
 */
public class ErrorEstimate {
	// --------将TA匹配轨迹与GPS匹配轨迹进行对比。。匹配精确度----------//

	public static void TaPrecise(Vector<Node> TaOrbit, Vector<Node> GpsOrbit,
			DB db) throws IOException { // 对比Ta匹配结果和GPS匹配结果（ 两条匹配轨迹的LCS所占比例）
		long pre = 0;
		Vector<Long> Ta = new Vector<Long>();
		Vector<Long> Gps = new Vector<Long>();
		int size = TaOrbit.size();
		for (int i = 0; i < size; i++) {// 去除重复弧段
			if (pre != TaOrbit.get(i).lineid)
				Ta.add(TaOrbit.get(i).lineid);
			pre = TaOrbit.get(i).lineid;
		}
		size = GpsOrbit.size();
		pre = 0;
		for (int i = 0; i < size; i++) {// 去除重复弧段
			if (pre != GpsOrbit.get(i).lineid)
				Gps.add(GpsOrbit.get(i).lineid);
			pre = GpsOrbit.get(i).lineid;
		}
		double TaLength = 0.0, GpsLength = 0.0, LCSLength = 0.0;
		int n = Ta.size(), m = Gps.size();
		int[][] dp = new int[n + 1][m + 1];
		int[][] status = new int[n + 1][m + 1];
		// 动态规划求LCS
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				long x = Ta.get(i - 1), y = Gps.get(j - 1);
				if (x == y) {
					dp[i][j] = dp[i - 1][j - 1] + 1;
					status[i][j] = 0;
					if (dp[i - 1][j] > dp[i][j]) {
						dp[i][j] = dp[i - 1][j];
						status[i][j] = 1;
					}
					if (dp[i][j - 1] > dp[i][j]) {
						dp[i][j] = dp[i][j - 1];
						status[i][j] = -1;
					}
				} else if (dp[i][j - 1] >= dp[i - 1][j]) {
					dp[i][j] = dp[i][j - 1];
					status[i][j] = -1;
				} else {
					dp[i][j] = dp[i - 1][j];
					status[i][j] = 1;
				}
			}
		}
		// 求出匹配准确弧段
		Vector<Long> LCS = new Vector<Long>();
		int tn = n, tm = m;
		//
		while (tn != 0 && tm != 0) {
			if (status[tn][tm] == 0) {
				LCS.add(Ta.get(tn - 1));
				tn--;
				tm--;
			} else if (status[tn][tm] == -1) {
				tm--;
			} else {
				tn--;
			}
		}
		int k = LCS.size();
		// 求出TA轨迹长度
		for (int i = 0; i < n; i++) {
			TaLength += MapData.GetLineLength(Ta.get(i), db);
		}

		for (int i = 0; i < m; i++) {
			GpsLength += MapData.GetLineLength(Gps.get(i), db);
		}
		for (int i = 0; i < k; i++)
			LCSLength += MapData.GetLineLength(LCS.get(i), db);
		MdbFind.Talength += TaLength;
		MdbFind.Gpslength += GpsLength;
		MdbFind.LCSlength += LCSLength;
		double Precision = LCSLength / TaLength;
		double Recall = LCSLength / GpsLength;
		double F_Measure = 2 * Precision * Recall / (Precision + Recall);
		
		MdbFind.PreciseOut.outputToFile(String.valueOf(Precision) + '\n');// 输出precise到文件
		MdbFind.RecallOut.outputToFile(String.valueOf(Recall) + '\n');// 输出recall到文件
		System.out.println("precision = " + Precision * 100 + "%");
		System.out.println("Recall = " + Recall * 100 + "%");
		System.out.println("F_Measure = " + F_Measure * 100 + "%");
		System.out.println("n =" + n + " m =" + m + " LCS = " + dp[n][m]);
	}

	public static void DisError(Vector<Node> TaOrbit, Vector<Node> GpsOrbit,
			Car Gpscar, DB db) throws IOException {// 对比Ta匹配结果与测量GPS坐标，标准（欧式距离）
		int n = TaOrbit.size(), m = GpsOrbit.size(), l = 0, r = 0, po = 0, num = 0;
		// OutputFile output = new OutputFile();
		// output.init(MyCons.CarfileDir+"DisError//Error_"+MdbFind.filename);
		double Error = 0.0;
		while (l < n && r < m) {
			Node Ta = TaOrbit.get(l);
			Node Gps = GpsOrbit.get(r);
			if (Ta.time == -1)
				l++;
			else if (Gps.time == -1)
				r++;
			else {
				if (Ta.time < Gps.time) {
					l++;
				} else if (Ta.time > Gps.time) {
					r++;
				} else {
					num++;
					while (po < Gpscar.PointNum && Gpscar.getTime(po) < Ta.time) {
						po++;
					}
					Point nowp = Gpscar.getGpsPoint(po);
					Line trueLine = MapData.GetLine(Gps.lineid, db);
					Point Gpspoint = Algorithm.MinDistPtoLine(nowp, trueLine);
					double dist = Algorithm.Distance(Ta.po, Gpspoint);
					Error += dist;
					if (Ta.lineid == Gps.lineid)
						MdbFind.diserrorOut
								.outputToFile(String.valueOf(dist) + '\n');
					// System.out.println("dis = "+dist+" time = "+Ta.time+" TaLine ="+Ta.lineid+
					// " GpsLine ="+Gps.lineid);
					l++;
					r++;
				}
			}
		}
		// output.closelink();
		System.out.println("num = " + num + " meandistError = " + Error / num);
	}

}
