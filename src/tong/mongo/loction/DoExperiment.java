package tong.mongo.loction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;

import tong.mongo.defclass.Line;
import tong.mongo.defclass.Output;

import com.defcons.SystemSettings;



//做实验用函数
public class DoExperiment {
	
	public static void main(String[] args) throws IOException, SQLException, ParseException{
		MdbFind.PreciseOut =new Output();
		MdbFind.PreciseOut.init(SystemSettings.CarfileDir+"evaluation//Eval_Precise_empty.json");
		MdbFind.RecallOut = new Output();
		MdbFind.RecallOut.init(SystemSettings.CarfileDir+"evaluation//Eval_Recall_empty.json");
		MdbFind.diserrorOut = new Output();
		MdbFind.diserrorOut.init(SystemSettings.CarfileDir+"evaluation//disError_empty.json");
		MdbFind.DriveMap = new HashMap<Long,Line>();
		int start=0,end=64;
		for(int i=start;i<=end;i++){
			MdbFind.StartWork(i);
		}
		if(SystemSettings.PrintDriveOrbit){
			Output output = new Output();
			output.init(SystemSettings.CarfileDir+"evaluation//mapdata.json");
			for(Long id:MdbFind.DriveMap.keySet()){
				Line now = MdbFind.DriveMap.get(id);
				String str = "{\"lat1\": "+String.valueOf(now.p[0].x)
						+",\"lon1\": "+String.valueOf(now.p[0].y)
						+",\"lat2\": "+String.valueOf(now.p[1].x)
						+",\"lon2\": "+String.valueOf(now.p[1].y)
						+"},\n";
				output.outputToFile(str);
			}
			output.closelink();
		}
		//计算总误差
		double Precision = MdbFind.LCSlength/MdbFind.Talength;
		double Recall = MdbFind.LCSlength/MdbFind.Gpslength;
		double F_Measure = 2 * Precision * Recall /(Precision + Recall);
		System.out.println("");
		System.out.println("precision = "+Precision*100+"%");
		System.out.println("Recall = "+Recall*100+"%");
		System.out.println("F_Measure = "+F_Measure*100+"%");
		MdbFind.PreciseOut.closelink();
		MdbFind.RecallOut.closelink();
		MdbFind.diserrorOut.closelink();
	}
}
