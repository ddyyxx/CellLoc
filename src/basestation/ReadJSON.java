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
		
		//输入文件初始化
		FileReader fr=new FileReader(inputname);
        BufferedReader br=new BufferedReader(fr);
        String line="";
        String[] arrs=null;       
        br.readLine();//跳过第一行
        br.readLine();//跳过第一行
        System.out.println("hello");
        //输出文件初始化
        OutputFile outer = new OutputFile();
        outer.init(outputname);
        
        Vector<CarPosition> pointSet = new Vector<CarPosition>();        
        BaseStation stat = new BaseStation(); //调用算法的对象        
  	
        int currid = getFirstID(inputname);  //获取第一个ID
        while ((line=br.readLine())!=null) {
        	Matcher m = p.matcher(line);
        	arrs=m.replaceAll("").trim().split("\\s");
        	
        	if(arrs.length>=6){
        		if(currid != Integer.parseInt(arrs[2])){ //若当前id与下一id不同执行算法
        			StringBuffer diff = new StringBuffer();
        			Point solupoi =stat.getPosition(pointSet,diff); //执行算法
        			
        			
        			String solu_str = outer.getStr(currid,solupoi.x, solupoi.y, diff.toString());
        			//System.out.println("=============================");
        			//System.out.println("当前集合大小:"+pointSet.size());
        			System.out.println(solu_str);
        			outer.outputToFile(solu_str);

        			currid = Integer.parseInt(arrs[2]); //更新id
        			pointSet = new Vector<CarPosition>();        
        		}
        		
        		//System.out.println(arrs[0]+" "+arrs[1]+" "+arrs[2]+" "+arrs[6]);        		
        		CarPosition poi = new CarPosition(Double.parseDouble(arrs[1]),
        										  Double.parseDouble(arrs[0]),
        										  Double.parseDouble(arrs[5])*78.0/16);//经纬度读入数据中是反的
        		//System.out.println("TA="+poi.dis);
        		pointSet.add(poi);
        		
        	}        	
        }
        if(line==null){ //还剩最后一个没有执行，这里完成
        	StringBuffer diff = new StringBuffer();
			Point solupoi =stat.getPosition(pointSet,diff); //执行算法
			
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
//		+ "|\\}");//以1个或多个空格进行分割

