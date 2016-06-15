package basestation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tong.mongo.defclass.Point;

public class ReadJSON {

	final static String regEx ="[^0-9.\\+\\-\\s]"; 
	final static Pattern p = Pattern.compile(regEx); 
	public static void readFileJSON(String inputname,String outputname) throws IOException {
		
		//�����ļ���ʼ��
		FileReader fr=new FileReader(inputname);
        BufferedReader br=new BufferedReader(fr);
        String line="";
        String[] arrs=null;       
        br.readLine();//������һ��
        br.readLine();//������һ��
        System.out.println("hello");
        //����ļ���ʼ��
        OutputFile outer = new OutputFile();
        outer.init(outputname);
        
        Vector<CarPosition> pointSet = new Vector<CarPosition>();        
        BaseStation stat = new BaseStation(); //�����㷨�Ķ���        
  	
        int currid = getFirstID(inputname);  //��ȡ��һ��ID
        while ((line=br.readLine())!=null) {
        	Matcher m = p.matcher(line);
        	arrs=m.replaceAll("").trim().split("\\s");
        	
        	if(arrs.length>=6){
        		if(currid != Integer.parseInt(arrs[2])){ //����ǰid����һid��ִͬ���㷨
        			StringBuffer diff = new StringBuffer();
        			Point solupoi =stat.getPosition(pointSet,diff); //ִ���㷨
        			
        			
        			String solu_str = outer.getStr(currid,solupoi.x, solupoi.y, diff.toString());
        			//System.out.println("=============================");
        			//System.out.println("��ǰ���ϴ�С:"+pointSet.size());
        			System.out.println(solu_str);
        			outer.outputToFile(solu_str);

        			currid = Integer.parseInt(arrs[2]); //����id
        			pointSet = new Vector<CarPosition>();        
        		}
        		
        		//System.out.println(arrs[0]+" "+arrs[1]+" "+arrs[2]+" "+arrs[6]);        		
        		CarPosition poi = new CarPosition(Double.parseDouble(arrs[1]),
        										  Double.parseDouble(arrs[0]),
        										  Double.parseDouble(arrs[5])*78.0/16);//��γ�ȶ����������Ƿ���
        		//System.out.println("TA="+poi.dis);
        		pointSet.add(poi);
        		
        	}        	
        }
        if(line==null){ //��ʣ���һ��û��ִ�У��������
        	StringBuffer diff = new StringBuffer();
			Point solupoi =stat.getPosition(pointSet,diff); //ִ���㷨
			
			String solu_str = outer.getStr(currid,solupoi.x, solupoi.y, diff.toString());
			System.out.println(solu_str);
			outer.outputToFile(solu_str);
        }
        
        outer.closelink();
        br.close();
        fr.close();
	}
	
	@SuppressWarnings("resource")
	public static int getFirstID(String filename) throws IOException{
		FileReader fr_currid=new FileReader(filename);
        BufferedReader br_currid=new BufferedReader(fr_currid);
        br_currid.readLine();
        String line = br_currid.readLine();
        Matcher m_currid = p.matcher(line);
    	String[] arrs=m_currid.replaceAll("").trim().split("\\s");    	
        return  Integer.parseInt(arrs[2]);
	}
}



//
//arrs = line.split("\\{\"longitude\": "
//		+ "|,\"latitude\": "
//		+ "|,\"pci\": "
//		+ "|,\"rsrp\": "
//		+ "| dBm,\"rsrq\": "
//		+ "| dB,\"ta\":"
//		+ "|\\}");//��1�������ո���зָ�

