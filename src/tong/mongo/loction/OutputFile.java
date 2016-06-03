package tong.mongo.loction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


// -----规定输出格式
public class OutputFile {
	
	BufferedWriter writer ;
	public void init(String filename) throws IOException{
		File file = new File(filename);
		if(!file.exists()){
			file.createNewFile();
		}
		writer = new BufferedWriter(new FileWriter(file));
	}
	

	//-----------返回一组经纬度-------------
	public String getStrMid(double mid_lat , double mid_lon, long lineid){ 
		return "{\"lat\": "+mid_lat
				+",\"lon\": "+mid_lon
				+",\"lineid\": "+lineid
				+"}\n";
	}
	
	public void outputToFile(String str) throws IOException{
		writer.write(str);
		writer.flush();
	}
	public void closelink() throws IOException{
		writer.close();
	}
}
