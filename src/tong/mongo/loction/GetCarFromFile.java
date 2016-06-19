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
 * �ӳ��������ļ��ж��복������
 * 
 * @author ddyyxx
 */
public class GetCarFromFile {
	static int INTERVAL_NUM = SystemSettings.INTERVALNUM; // �����ܶ�
	final static double COEFFICIENT = 78.0 / 16.0; // taֵ�˵�ϵ��
	final static int DISTANCE = SystemSettings.DISTANCE_DIFF; // ���������ֵ
	final static String regEx = "[^0-9.\\+\\-\\sE]";
	final static Pattern p = Pattern.compile(regEx);
	static String filename = SystemSettings.filename; // //�ļ���////////

	// ��ȡΨһ�Ļ�վλ��
	public static void getUniqLTELoc(String filename, Map<String, double[]> map)
			throws NumberFormatException, IOException {
		// �����ļ�-��վλ���ļ�
		String solufile = SystemSettings.CarfileDir + "solu//solu_" + filename;
		// ��ʼ����վλ���ļ�reader
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

	// �õ�һ�������ĳ��Ĺ켣
	public static Car readFileSolution(String filename,
			Map<String, double[]> map_lteloc, boolean trueTa)
			throws IOException, SQLException, ParseException {

		// �����ļ�-���λ���ļ�
		String pointfile = SystemSettings.CarfileDir + "MyJson//" + filename;
		// ��ʼ�����λ���ļ�reader
		FileReader fr_poi = new FileReader(pointfile);
		BufferedReader br_poi = new BufferedReader(fr_poi);
		String line_poi = null;
		String[] arrs_poi;
		Car mycar = new Car();
		while ((line_poi = br_poi.readLine()) != null) {// �õ��������е�
			// System.out.println(line_poi);
			Matcher m_poi = p.matcher(line_poi);
			arrs_poi = m_poi.replaceAll("").trim().split("\\s");
			if (arrs_poi.length < 6)
				continue;
			String pci = arrs_poi[2];
			double[] currlteloc = map_lteloc.get(pci); // ��û�վ����
			if (currlteloc == null)
				continue;

			double lng_a = currlteloc[0];
			double lat_a = currlteloc[1];
			double lng_b = Double.valueOf(arrs_poi[0]);
			double lat_b = Double.valueOf(arrs_poi[1]);

			// �����ʵ�����TAֵ��ľ�����̫���ų������
			if (Math.abs(Algorithm.Distance(lat_a, lng_a, lat_b, lng_b)
					- Double.valueOf(arrs_poi[5]) * 78.0 / 16) > DISTANCE) {
				continue;
			}
			// ȥ����Ч��վ����㣨������Ļ�վ���꣩
			double dddis = Algorithm.Distance(lat_a, lng_a, lat_b, lng_b); // ��ʵ����
			// ///////���㲢�������Ƕ�//////////
			double currAzimuth = Algorithm.gps2d(lat_a, lng_a, lat_b, lng_b);
			// legalline.add(null);//��ѡ��(δ�趨����ֵ������)

			double TimeofArrival = 0.0;
			if (trueTa) {// ʹ����ʵTaֵ
				TimeofArrival = dddis;
			} else {// ʹ���Լ�������Taֵ
				TimeofArrival = Double.valueOf(arrs_poi[5]) * COEFFICIENT;
			}
			AnchorPoint nowpoint = new AnchorPoint(0);// ��ͨ��
			nowpoint.addPoint(new Point(currlteloc[1], currlteloc[0]),
					TimeofArrival, new sector((currAzimuth + 300) % 360,
							(currAzimuth + 60) % 360));// ��λ������
			mycar.addPointAll(nowpoint, new Point(lat_b,lng_b), Long.parseLong(pci), Integer.valueOf(arrs_poi[6]));
		}
		br_poi.close();
		fr_poi.close();
		return mycar;
	}
}
