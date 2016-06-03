package tong.mongo.loction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.defcons.MyCons;


//�������ʾ�ļ��е���


//����JSON_INFO
public class OutGps {
	//����ļ���
	static String outputname_gps = MyCons.Outputdir+"JSON_JSON_Yanming_DriveTesting_09-05.json";
	static String outputname_lte = MyCons.Outputdir+"lteloc//JSON_LTE_JSON_Yanming_DriveTesting_09-05.json";
	
	static BufferedReader br;
	final static String regEx ="[^0-9.\\+\\-\\sE]"; 
	final static Pattern p = Pattern.compile(regEx);
	static String filename = MdbFind.filename; //�����ļ��� ��MdbFind������ļ���һ��
	static String inputname = MyCons.CarfileDir+"road_poi//JSON_roadinfo_"+filename;
	static String inputname_lte = MyCons.CarfileDir+"solu//solu_"+filename;
	public static void main(String args[]) throws IOException{	
			//do nothing
	}
	
	//--�������ǰ����ʾ
	public static  String getStr(String x_lat, String x_lon, String x_lineid) throws IOException{
		return "{\"longitude\": "+x_lon
				+ ",\"latitude\": "+x_lat
				+ ",\"lineid\": "+"\""+x_lineid+"\""
				+"},";
	}
	
	public static String getXMLStr(String x_lat,String x_lon){
		return "<trkpt lat=\""
				+ x_lat
				+ "\" lon=\""
				+ x_lon
				+ "\"><ele>61.3</ele><time>2015-09-04T14:12:20Z</time></trkpt>";
	}
	public static void init(){
		outputname_gps = MyCons.Outputdir+"JSON_JSON_Yanming_DriveTesting_09-05.json";
		outputname_lte = MyCons.Outputdir+"lteloc//JSON_LTE_JSON_Yanming_DriveTesting_09-05.json";
		filename = MdbFind.filename; //�����ļ��� ��MdbFind������ļ���һ��
		inputname = MyCons.CarfileDir+"road_poi//JSON_roadinfo_"+filename;
		inputname_lte = MyCons.CarfileDir+"solu//solu_"+filename;
	}
	//ִ��outToJSFile�����ĺ���
	public static void outToJSFileRun() throws IOException{
		FileReader fr=new FileReader(inputname);
        br=new BufferedReader(fr);
        
        outToJSFile(); //��GPS��Ϣ������ļ�

        br.close();
        fr.close();
        
        outLTEToJSFile(); //����վ��Ϣ������ļ�
        copyAndWrite(); //������ļ�����һ�ݣ��Թ涨��ʽ���ļ�������
	}
	
	//��GPS��Ϣ�����ļ������ڶ�̬���
	public static void outToJSFile() throws IOException{
		String line = "";
		String[] arrs = null;
		BufferedWriter writer ;
		File file = new File(outputname_gps);
		if(!file.exists()){
			file.createNewFile();
		}
		writer = new BufferedWriter(new FileWriter(file));
		writer.write("[ \n");

		while((line = br.readLine())!=null){
			Matcher m = p.matcher(line);
        	arrs=m.replaceAll("").trim().split("\\s");

        	String str1 = getStr(arrs[0],arrs[1],arrs[2]);
        	writer.write(str1+"\n");
		}
		writer.close();
		RandomAccessFile ranf=new RandomAccessFile(outputname_gps,"rw");
		long pos=ranf.length()-2;
		ranf.seek(pos);
		ranf.setLength(ranf.getFilePointer());
		ranf.write('\n');
		ranf.write(']');
		ranf.close();
	}	
	///////////////////////////////////////
	
	//----------���������׼json��ʽ�Ļ�վλ��
	public static void outLTEToJSFile() throws IOException{
		CarInformation carAzi = new CarInformation();
		Map<String , double[]> map_lteloc = new HashMap<String , double[]>();
		carAzi.getUniqLTELoc(filename,map_lteloc); //��ȡΨһ��վλ��
		

		BufferedWriter writer ;
		File file = new File(outputname_lte);
		if(!file.exists()){
			file.createNewFile();
		}
		writer = new BufferedWriter(new FileWriter(file));
		writer.write("[ \n");
		
		for (Entry<String, double[]> entry : map_lteloc.entrySet()) {
			String str1 = getLtelocStr(entry.getKey(),entry.getValue()[0],entry.getValue()[1],entry.getValue()[2]);   
			writer.write(str1+"\n");
			   
		}
		writer.close();
		RandomAccessFile ranf=new RandomAccessFile(outputname_lte,"rw");
		long pos=ranf.length()-2;
		ranf.seek(pos);
		ranf.setLength(ranf.getFilePointer());
		ranf.write('\n');
		ranf.write(']');
		ranf.close();

	}
	
	public static String getLtelocStr(String pci , double d ,double e ,double f){
		return "{\"pci\": "
				+ pci
				+ ",\"longitude\": "+d
				+ ",\"latitude\": "+e
				+",\"diff\": "+f
				+"},";
	}
	
	///////////////////////////////////////////////
	//-------------����Ĭ���ļ����������----------(�ļ�����)
	public static void copyAndWrite() throws IOException{
		String cp_inputname = MyCons.Outputdir+"JSON_JSON_Yanming_DriveTesting_09-05.json";
		String cp_inputname_lte = MyCons.Outputdir+"lteloc\\JSON_LTE_JSON_Yanming_DriveTesting_09-05.json";
		String cp_outputname = MyCons.Outputdir+"JSON_"+MdbFind.filename;
		String cp_outputname_lte = MyCons.Outputdir+"lteloc\\JSON_LTE_"+MdbFind.filename;
		
		File file_cp_input = new File(cp_inputname);
		File file_cp_output = new File(cp_outputname);
		
		File file_cp_input_lte = new File(cp_inputname_lte);		
		File file_cp_output_lte = new File(cp_outputname_lte);

		fileChannelCopy(file_cp_input, file_cp_output);
		fileChannelCopy(file_cp_input_lte, file_cp_output_lte);

	}

	//�����ļ�
	public static void fileChannelCopy(File s, File t) throws IOException {
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			fi = new FileInputStream(s);
			fo = new FileOutputStream(t);
			in = fi.getChannel();// �õ���Ӧ���ļ�ͨ��
			out = fo.getChannel();// �õ���Ӧ���ļ�ͨ��
			in.transferTo(0, in.size(), out);// ��������ͨ�������Ҵ�inͨ����ȡ��Ȼ��д��outͨ��

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fi.close();
			in.close();
			fo.close();
			out.close();
		}
	}
	
	//------����ʼ��gps�ļ���ʽ��------
	public static void outputGpsFile(String filename) throws IOException{
		String inputname_gps = MyCons.CarfileDir+"MyJson\\"+filename;
		String outputname_gps = MyCons.Outputdir+"gps\\JSON_Yanming_DriveTesting_09-05.json";
		String cp_outputname_gps = MyCons.Outputdir+"gps\\"+filename;
		
		
		FileReader fr_poi = new FileReader(inputname_gps);
		BufferedReader br_poi = new BufferedReader(fr_poi);
		String line_poi = null;
		String[] arrs_poi ;

		BufferedWriter writer ;
		File file = new File(outputname_gps);
		if(!file.exists()){
			file.createNewFile();
		}
		writer = new BufferedWriter(new FileWriter(file));
		writer.write("[ \n");
		
		
		while((line_poi = br_poi.readLine())!=null){
			Matcher m_poi = p.matcher(line_poi);
			arrs_poi= m_poi.replaceAll("").trim().split("\\s");
			if(arrs_poi.length<6)
				continue;
			
			Double lng = Double.valueOf(arrs_poi[0]);
			Double lat = Double.valueOf(arrs_poi[1]);
			
			String pci = arrs_poi[2];
			
			String str = "{\"longitude\": "+ lng
					+ ",\"latitude\": "+lat
					+ ",\"pci\": "+pci
					+ ",\"rsrp\": "+arrs_poi[3]
					+ ",\"rsrq\": "+arrs_poi[4]
					+ ",\"ta\": "+arrs_poi[5]
					+"},\n";
			writer.write(str);
		}
		writer.close();
		br_poi.close();
		
		RandomAccessFile ranf=new RandomAccessFile(outputname_gps,"rw");
		long pos=ranf.length()-2;
		ranf.seek(pos);
		ranf.setLength(ranf.getFilePointer());
		ranf.write('\n');
		ranf.write(']');
		ranf.close();
		
		fileChannelCopy(new File(MyCons.Outputdir+"gps\\JSON_Yanming_DriveTesting_09-05.json"),new File(cp_outputname_gps));
	}

}
