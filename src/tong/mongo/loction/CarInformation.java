package tong.mongo.loction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.defcons.MyCons;

import tong.mongo.defclass.AnchorPoint;
import tong.mongo.defclass.Car;
import tong.mongo.defclass.Point;
import tong.mongo.defclass.sector;


//���ɵ�ĽǶ�
public class CarInformation {

	static int INTERVAL_NUM =  MdbFind.INTERVALNUM;	 //�����ܶ�
	final static double COEFFICIENT = 78.0/16.0; //taֵ�˵�ϵ��
	final static int DISTANCE = MdbFind.DISTANCE_DIFF ; //���������ֵ
	final static String regEx ="[^0-9.\\+\\-\\sE]"; 
	final static Pattern p = Pattern.compile(regEx); 
	public Algorithm Alg;
	static String filename = MdbFind.filename; ////�ļ���////////
	
	public static void main(String[] args) throws IOException, SQLException, ParseException {
		
		CarInformation caa = new CarInformation() ;
		//������еĻ�վλ��map������diffΪ1.0e8���������
		Map<String , double[]> map_lteloc = new HashMap<String , double[]>();
		//��ȡ��վλ��
		caa.getUniqLTELoc(filename,map_lteloc);	
		//ִ�к���
		@SuppressWarnings("unused")
		Car mycar ;
		mycar = caa.readFileSolution(filename,map_lteloc,false);
	}

	//��ȡΨһ�Ļ�վλ��
	public void getUniqLTELoc(String filename,Map<String ,double[]> map ) throws NumberFormatException, IOException{
		//�����ļ�-��վλ���ļ�
		String solufile = MyCons.CarfileDir+"solu//solu_"+filename;
		//��ʼ����վλ���ļ�reader
		FileReader fr_solu = new FileReader(solufile);
		BufferedReader br_solu = new BufferedReader(fr_solu);
		String line_solu = null;
		String[] arrs ;
		
		while((line_solu = br_solu.readLine())!=null){
			Matcher m = p.matcher(line_solu);
        	arrs=m.replaceAll("").trim().split("\\s");
        	if(arrs[3].equals("1.0E8")){
        		//System.out.println(Double.parseDouble(arrs[3])+" "+arrs[0]);
        		continue;
        	}
        	if(map.get(arrs[0])==null){
        		double[] temp = new double[3];
        		temp[0] = Double.valueOf(arrs[1]);
        		temp[1] = Double.valueOf(arrs[2]);
        		temp[2] = Double.valueOf(arrs[3]);        		
        		map.put(arrs[0], temp);
        	}else{
        		double prediff = map.get(arrs[0])[2];
        		if(prediff>Double.parseDouble(arrs[3])){
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
	
	public Car PointChoose(Car car,boolean isGPS){//�ʵ�ѡȡ��λ�� �����վ�л����Ta�任�㣩
		Car resultCar = new Car();
		int n=car.PointNum,nowid=0,num=0,total=0;
		assert(car.PointNum==car.PointSet.size());
		for(int id=0;id<n-1;id++){
			Point now=car.getAnchorPoint(id).getPoint(0);
			Point next=car.getAnchorPoint(id+1).getPoint(0);//��һ����
//			if(isGPS){//�����ѡȡGPS�㣬����Ҫ���ǻ�վ�л�����TA�仯
//				if(id%INTERVAL_NUM==0){
//					resultCar.addPointAll(car.getAnchorPoint(id), car.getGpsPoint(id), car.getPci(id),car.getTime(id));
//				}
//				continue;
//			}
			double Ta1 =car.getTa(id);
			double Ta2 =car.getTa(id+1);
			if(!Alg.compare(now, next)){//��ʾ����һ���л���(�����)
				//�����ж��Ƿ�ʱ����̫��
				if(car.getTime(id+1)-car.getTime(id)<=2){//ʱ�������2s������Ϊ���л��㣨��Ч��
					AnchorPoint Po = new AnchorPoint(1);//�л��㣨���⣩
					sector sec1 = car.getSector(id);
					sector sec2 = car.getSector(id+1); 
					Po.addPoint(now,Ta1,sec1);
					Po.addPoint(next,Ta2,sec2);
					resultCar.addPointAll(Po, car.getGpsPoint(id),car.getPci(id),car.getTime(id));
					//�����Ѿ�ȡ������㣬����Ҫ������Ĺ���
					nowid=0;
//					id++;
//					nowid=1;
//					continue;
				}
			}
//			if(Alg.Fabs(Ta1-Ta2)>50.0){//��ʾ����һ��Ta�仯��
//				AnchorPoint Po = new AnchorPoint();
//				double distance=Alg.Distance(car.getAnchorPoint(id+1).getPoint(0), car.getGpsPoint(id+1));
//				if(Ta1<Ta2){//Ta��С���
//					Po.type = 2;
//					if(distance < Ta2)
//						num++;
//					else{
//						nowid++;
//						continue;
//					}	
//				}
//				else{//Ta�Ӵ��С
//					Po.type = 3;
//					if(distance > Ta2)
//						num++;
//					else{
//						nowid++;
//						continue;
//					}
//				}
//				//Po.addPoint(now, Ta1, car.getSector(id));
//				//resultCar.addPointAll(Po, car.getGpsPoint(id), car.getPci(id), car.getTime(id));
////				System.out.println("preTA = "+car.getTa(id)+" nextTa = "+car.getTa(id+1)
////						+" predis = "+Alg.Distance(car.getAnchorPoint(id).getPoint(0), car.getGpsPoint(id))
////						+" nextdis = "+Alg.Distance(car.getAnchorPoint(id+1).getPoint(0), car.getGpsPoint(id+1))+" time = "+car.getTime(id+1));
//				
//				total++;
//				//id++;
//				Po.addPoint(next, Ta2, car.getSector(id+1));//ȡTaֵ�仯���Ǹ���
//				resultCar.addPointAll(Po, car.getGpsPoint(id+1), car.getPci(id+1), car.getTime(id+1));
//				nowid=0;
//			}
			else if(nowid%INTERVAL_NUM==0){//��ͨ��λ��
				resultCar.addPointAll(car.getAnchorPoint(id), car.getGpsPoint(id), car.getPci(id),car.TimeSet.get(id));
			}
			nowid++;
		}
		//System.out.println("����Ta�仯���ɵĵ� = "+num+" out of"+total);
		return resultCar;
	}
	
	//�õ�һ�������ĳ��Ĺ켣
	public Car readFileSolution(String filename,Map<String , double[]> map_lteloc,boolean trueTa) throws IOException, SQLException, ParseException{
		
		//�����ļ�-���λ���ļ�
		String pointfile = MyCons.CarfileDir+"MyJson//"+filename;	
		//��ʼ�����λ���ļ�reader
		FileReader fr_poi = new FileReader(pointfile);
		BufferedReader br_poi = new BufferedReader(fr_poi);
		String line_poi = null;
		Alg =new Algorithm();
		String[] arrs_poi ;
		int totalnum = 0,carnum=0;
		Car mycar ;
		Vector<Vector<Long>> legalline = new Vector<Vector<Long>>();//��ѡ��
		Vector<AnchorPoint> PointSet = new Vector<AnchorPoint>();//��վ����
		Vector<Point> GpsSet = new Vector<Point>();//GPS ����
		Vector<Long> PciSet = new Vector<Long>();// ��վid
		Vector<Integer> TimeSet = new Vector<Integer>();//ʱ��ֵ		
		
		while((line_poi = br_poi.readLine())!=null){//�õ��������е�
			//System.out.println(line_poi);
			Matcher m_poi = p.matcher(line_poi);
			arrs_poi= m_poi.replaceAll("").trim().split("\\s");
			if(arrs_poi.length<6)
				continue;
			String pci = arrs_poi[2];
			double[] currlteloc = map_lteloc.get(pci);  //��û�վ����
			if(currlteloc==null) 
				continue;
			
			double lng_a = currlteloc[0];
			double lat_a = currlteloc[1];
			double lng_b = Double.valueOf(arrs_poi[0]);
			double lat_b = Double.valueOf(arrs_poi[1]);
			
			totalnum++;
			Algorithm Alg=new Algorithm();
			
			//�����ʵ�����TAֵ��ľ�����̫���ų������
			if(Math.abs(Alg.Distance( lat_a,lng_a, lat_b, lng_b)-Double.valueOf(arrs_poi[5])*78.0/16) > DISTANCE){
				continue;
			}
			//ȥ����Ч��վ����㣨������Ļ�վ���꣩
			double dddis = Alg.Distance( lat_a,lng_a, lat_b, lng_b); //��ʵ����
			/////////���㲢�������Ƕ�//////////
			double currAzimuth = Alg.gps2d(lat_a,lng_a,lat_b,lng_b);
			//legalline.add(null);//��ѡ��(δ�趨����ֵ������)
			
			double TimeofArrival=0.0;
			if(trueTa){//ʹ����ʵTaֵ
				TimeofArrival = dddis;
			}
			else{//ʹ���Լ�������Taֵ
				TimeofArrival = Double.valueOf(arrs_poi[5])*COEFFICIENT;
			}				
			AnchorPoint nowpoint = new AnchorPoint(0);//��ͨ��
			nowpoint.addPoint(new Point(currlteloc[1],currlteloc[0]), TimeofArrival, new sector((currAzimuth+300)%360,(currAzimuth+60)%360));//��λ������
			GpsSet.add(new Point(lat_b,lng_b));//GPS����
			PciSet.add(Long.parseLong(pci));//��վID
			PointSet.add(nowpoint);
			TimeSet.add(Integer.valueOf(arrs_poi[6]));//ʱ��ֵ 		
			carnum ++ ;
		}
		//
		////////////////////////////�õ�car�����ж�λ��//////////////////////////////////
		//System.out.println("�ܹ���������"+totalnum);
		mycar = new Car(carnum,legalline,PointSet,GpsSet,PciSet,TimeSet);
		br_poi.close();
		fr_poi.close();
		return mycar;
	}
	//��ȡ���˺�ĳ�������ѡȡ�ʵ��Ķ�λ��
	public Car getCar(Car mycar,int interval,boolean isGPS){
		Car resultcar = mycar;
		Alg = new Algorithm();
		INTERVAL_NUM=interval;
		PointFilter Filter = new PointFilter();//�����㷨��
		if(!isGPS)
			System.out.println("����ǰ�������"+mycar.PointNum);
		resultcar = Filter.ErrorFilter(mycar);//��������
		if(!isGPS)
			System.out.println("���˺�������"+resultcar.PointNum);
		resultcar = PointChoose(resultcar,isGPS);//ѡȡ�ʵ��Ķ�λ��
		if(!isGPS)
			System.out.println("ѡȡ���������:"+resultcar.PointNum);
		return resultcar;
	}
}

