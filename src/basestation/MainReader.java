package basestation;

import java.io.IOException;

import com.defcons.SystemSettings;

public class MainReader {

	public static void main(String[] args) {

		for (String filename : SystemSettings.arr_filename) {
			String inputname = SystemSettings.CarfileDir+"BaseStation//"+ filename;
			String outputname = SystemSettings.CarfileDir+"solu//solu_"+ filename;
			try {
				ReadJSON.readFileJSON(inputname, outputname);// ∂¡»°–≈œ¢
			} catch (IOException e1) {
				System.out.println(e1);
			}
		}
	}
}
