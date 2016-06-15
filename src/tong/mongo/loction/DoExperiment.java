package tong.mongo.loction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import com.defcons.MyCons;



//做实验用函数
public class DoExperiment {
	
	public static void main(String[] args) throws IOException, SQLException, ParseException{
		//Algorithm Alg =new Algorithm();
		//System.out.println(Alg.Distance(38.8956683, 121.64691415, 38.896017900000004, 121.6476668));
		MdbFind.PreciseOut =new OutputFile();
		MdbFind.PreciseOut.init(MyCons.CarfileDir+"evaluation//Eval_Precise_empty.json");
		MdbFind.RecallOut = new OutputFile();
		MdbFind.RecallOut.init(MyCons.CarfileDir+"evaluation//Eval_Recall_empty.json");
		MdbFind.diserrorOut = new OutputFile();
		MdbFind.diserrorOut.init(MyCons.CarfileDir+"evaluation//disError_empty.json");
		int start=60,end=60;
		for(int i=start;i<=end;i++){
			MdbFind.StartWork(i);
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
	}
}
