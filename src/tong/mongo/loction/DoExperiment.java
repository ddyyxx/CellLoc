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
		CellLoc.PreciseOut =new Output();
		CellLoc.PreciseOut.init(SystemSettings.CarfileDir+"evaluation//Eval_Precise_empty.json");
		CellLoc.RecallOut = new Output();
		CellLoc.RecallOut.init(SystemSettings.CarfileDir+"evaluation//Eval_Recall_empty.json");
		CellLoc.diserrorOut = new Output();
		CellLoc.diserrorOut.init(SystemSettings.CarfileDir+"evaluation//disError_empty.json");
		CellLoc.DriveMap = new HashMap<Long,Line>();
		int start=0,end=64;
		for(int i=start;i<=end;i++){
			CellLoc.StartWork(i);
		}
		if(SystemSettings.PrintDriveOrbit){
			Output output = new Output();
			output.init(SystemSettings.CarfileDir+"evaluation//mapdata.json");
			for(Long id:CellLoc.DriveMap.keySet()){
				Line now = CellLoc.DriveMap.get(id);
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
		double Precision = CellLoc.LCSlength/CellLoc.Talength;
		double Recall = CellLoc.LCSlength/CellLoc.Gpslength;
		double F_Measure = 2 * Precision * Recall /(Precision + Recall);
		System.out.println("");
		System.out.println("precision = "+Precision*100+"%");
		System.out.println("Recall = "+Recall*100+"%");
		System.out.println("F_Measure = "+F_Measure*100+"%");
		CellLoc.PreciseOut.closelink();
		CellLoc.RecallOut.closelink();
		CellLoc.diserrorOut.closelink();
	}
}
