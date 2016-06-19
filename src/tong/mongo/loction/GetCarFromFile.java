package tong.mongo.loction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Car;
import tong.mongo.defclass.Point;
import tong.mongo.defclass.sector;

import com.defcons.SystemSettings;

/**
 * 从车辆数据文件中读入车辆数据
 * 
 * @author ddyyxx
 */
public class GetCarFromFile {
	static int INTERVAL_NUM = SystemSettings.INTERVALNUM; // 采样密度
	final static double COEFFICIENT = 78.0 / 16.0; // ta值乘的系数
	final static int DISTANCE = SystemSettings.DISTANCE_DIFF; // 距离误差阈值
	final static String regEx = "[^0-9.\\+\\-\\sE]";
	final static Pattern p = Pattern.compile(regEx);
	static String filename = SystemSettings.filename; // //文件名////////

	// 获取唯一的基站位置
	public static void getUniqLTELoc(String filename, Map<String, double[]> map)
			throws NumberFormatException, IOException {
		// 输入文件-基站位置文件
		String solufile = SystemSettings.CarfileDir + "solu//solu_" + filename;
		// 初始化基站位置文件reader
		FileReader fr_solu = new FileReader(solufile);
		BufferedReader br_solu = new BufferedReader(fr_solu);
		String line_solu = null;
		String[] arrs;

		while ((line_solu = br_solu.readLine()) != null) {
			Matcher m = p.matcher(line_solu);
			arrs = m.replaceAll("").trim().split("\\s");
			if (arrs[3].equals("1.0E8")) {
				continue;
			}
			if (map.get(arrs[0]) == null) {
				double[] temp = new double[3];
				temp[0] = Double.valueOf(arrs[1]);
				temp[1] = Double.valueOf(arrs[2]);
				temp[2] = Double.valueOf(arrs[3]);
				map.put(arrs[0], temp);
			} else {
				double prediff = map.get(arrs[0])[2];
				if (prediff > Double.parseDouble(arrs[3])) {
					double[] temp = new double[3];
					temp[0] = Double.valueOf(arrs[1]);
					temp[1] = Double.valueOf(arrs[2]);
					temp[2] = Double.valueOf(arrs[3]);
					map.put(arrs[0], temp);
				}
			}
		}
		br_solu.close();
		fr_solu.close();
	}

	// 得到一条完整的车的轨迹
	public static Car readFileSolution(String filename,
			Map<String, double[]> map_lteloc, boolean trueTa)
			throws IOException, SQLException, ParseException {

		// 输入文件-点的位置文件
		String pointfile = SystemSettings.CarfileDir + "MyJson//" + filename;
		// 初始化点的位置文件reader
		FileReader fr_poi = new FileReader(pointfile);
		BufferedReader br_poi = new BufferedReader(fr_poi);
		String line_poi = null;
		String[] arrs_poi;
		Car mycar = new Car();
		while ((line_poi = br_poi.readLine()) != null) {// 得到车辆所有点
			// System.out.println(line_poi);
			Matcher m_poi = p.matcher(line_poi);
			arrs_poi = m_poi.replaceAll("").trim().split("\\s");
			if (arrs_poi.length < 6)
				continue;
			String pci = arrs_poi[2];
			double[] currlteloc = map_lteloc.get(pci); // 获得基站坐标
			if (currlteloc == null)
				continue;

			double lng_a = currlteloc[0];
			double lat_a = currlteloc[1];
			double lng_b = Double.valueOf(arrs_poi[0]);
			double lat_b = Double.valueOf(arrs_poi[1]);

			// 如果真实距离和TA值算的距离差距太大，排除这个点
			if (Math.abs(Algorithm.Distance(lat_a, lng_a, lat_b, lng_b)
					- Double.valueOf(arrs_poi[5]) * 78.0 / 16) > DISTANCE) {
				continue;
			}
			// 去除无效基站坐标点（误差过大的基站坐标）
			double dddis = Algorithm.Distance(lat_a, lng_a, lat_b, lng_b); // 真实距离
			// ///////计算并输出输出角度//////////
			double currAzimuth = Algorithm.gps2d(lat_a, lng_a, lat_b, lng_b);
			// legalline.add(null);//候选集(未设定集体值，待定)

			double TimeofArrival = 0.0;
			if (trueTa) {// 使用真实Ta值
				TimeofArrival = dddis;
			} else {// 使用自己测量的Ta值
				TimeofArrival = Double.valueOf(arrs_poi[5]) * COEFFICIENT;
			}
			AnchorPoint nowpoint = new AnchorPoint(0);// 普通点
			nowpoint.addPoint(new Point(currlteloc[1], currlteloc[0]),
					TimeofArrival, new sector((currAzimuth + 300) % 360,
							(currAzimuth + 60) % 360));// 定位点坐标
			mycar.addPointAll(nowpoint, new Point(lat_b,lng_b), Long.parseLong(pci), Integer.valueOf(arrs_poi[6]));
		}
		br_poi.close();
		fr_poi.close();
		return mycar;
	}
}
