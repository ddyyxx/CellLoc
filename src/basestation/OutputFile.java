package basestation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputFile {
	
	BufferedWriter writer ;
	public void init(String filename) throws IOException{
		File file = new File(filename);
		if(!file.exists()){
			file.createNewFile();
		}
		writer = new BufferedWriter(new FileWriter(file));
	}
	public String getStr(int currid , double lng, double lat, String diff) throws IOException{
		return "{\"pci\": "+currid
    			+",\"longitude\": "+lat
				+",\"latitude\": "+lng    				
  				+",\"diff\": "+diff.toString()+"}\n";
	}
	
	public void outputToFile(String str) throws IOException{
		writer.write(str);
		writer.flush();
	}
	public void closelink() throws IOException{
		writer.close();
	}
}
