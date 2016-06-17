package tong.map.MapProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import tong.mongo.loction.Algorithm;

import com.defcons.MyCons;

public class MapPre {

	//�趨һ��UUID �����������ID �����IDΪ��ǰʱ�侫ȷ������+UUID�� UUIDΪ������
	static long UUID = 10000;
	final static long MODNUM = UUID*10;
	
	@SuppressWarnings("resource")
	public static void mapPrepare() throws IOException {
		File finArc = new File(MyCons.MongoDataDir+"MapPre\\Arc.txt");
		File finPoint = new File(MyCons.MongoDataDir+"MapPre\\Point.txt");
		File foutPoint = new File(MyCons.MongoDataDir+"MongoDB\\mapPoint.txt");
		File foutAre = new File(MyCons.MongoDataDir+"MongoDB\\mapArc.txt");
		File foutLength = new File(MyCons.MongoDataDir+"MongoDB\\mapArcLength.txt");
		InputStreamReader read = null;
		BufferedReader buf = null;
		Writer out = null;

		String value = null;
		StringTokenizer stk = null;
		String str = null;
		List<Long> highList = new ArrayList<Long>();
		Map<String, Long> pidMap = new LinkedHashMap<String, Long>();
		@SuppressWarnings("rawtypes")
		Map<String, List> point = new HashMap<String, List>();
		long highWay;
		String pointID;
		String lat = null;
		String lon = null;
		String flag = null;
		StringBuffer sbuf = new StringBuffer();
		String Key = null;
		String preKey = null;
		String Arcid = null;
		long preValue = -1;
		double lat1 = -1, lon1 = -1, lat2, lon2;

		// �ҳ�highway��ID��ӵ�highList��
		read = new InputStreamReader(new FileInputStream(finArc));
		buf = new BufferedReader(read);
		while ((value = buf.readLine()) != null) {
			stk = new StringTokenizer(value);
			highWay = Integer.parseInt(stk.nextToken());
			stk.nextToken();
			if ("highway".equals(stk.nextToken())) {
				highList.add(highWay);
			}
		}

		// �ҳ�highway��Ӧ���ID
		read = new InputStreamReader(new FileInputStream(finArc));
		buf = new BufferedReader(read);
		while ((value = buf.readLine()) != null) {
			stk = new StringTokenizer(value);
			highWay = Integer.parseInt(stk.nextToken()); //��ȡ��·ID
			if (highList.contains(highWay)) { //�����ǰ��·��highway
				stk.nextToken();
				if ((value = stk.nextToken()).matches("^\\d+$")) { //��ȡ���·�ϵĵ�ID
					pointID = value;
					if (pidMap.containsKey(pointID)) {
						flag = String.valueOf(System.currentTimeMillis());
						UUID= (UUID+1)%MODNUM;
						pointID += " " + flag + UUID ;
						pidMap.put(pointID, highWay);
					} else {
						pidMap.put(pointID, highWay);
					}
				}
			}
		}
		System.out.println("����highway��ϣ�");

		// �ҳ������Ϣ
		str = "";
		read = new InputStreamReader(new FileInputStream(finPoint));
		buf = new BufferedReader(read);
		out = new FileWriter(foutPoint);
		while ((value = buf.readLine()) != null) {
			stk = new StringTokenizer(value);
			pointID = stk.nextToken();
			if (pidMap.containsKey(pointID) && !point.containsKey(pointID)) {
				lat = stk.nextToken();
				lon = stk.nextToken();
				point.put(pointID, Arrays.asList(lat, lon));
				str = String.valueOf(pointID) + "	" + lat + "	" + lon + '\n';
				out.write(str);
			}
		}
		
		out.close();
		System.out.println("�������Ϣ��ϣ�");
		
		// �ҳ��߶ε���Ϣ
		str = "";
		out = new FileWriter(foutAre);
		Writer out2 = new FileWriter(foutLength);
		Iterator<Entry<String, Long>> iterator = pidMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Long> entry = iterator.next();
			String strKey[] = entry.getKey().split(" ");
			Key = strKey[0];
			@SuppressWarnings("rawtypes")
			List list = point.get(Key);
			lat2 = Double.parseDouble((String) list.get(0));
			lon2 = Double.parseDouble((String) list.get(1));
			if (entry.getValue() == preValue) {
				double length = Algorithm.Distance(lat1, lon1, lat2, lon2);
				flag = String.valueOf(System.currentTimeMillis());
				UUID= (UUID+1)%MODNUM;
				Arcid = flag + UUID;
				out2.write(String.valueOf(length)+'\n');
				str = Arcid + " " + preKey + " " + Key + " " + length +" "+entry.getValue()+'\n';
				out.write(str);
				sbuf.setLength(0);
			} else {
				preValue = entry.getValue();
			}
			preKey = Key;
			lat1 = lat2;
			lon1 = lon2;
		}
		System.out.println("���������Ϣ��ϣ�");
		out2.close();
		out.close();
		buf.close();
	}
}