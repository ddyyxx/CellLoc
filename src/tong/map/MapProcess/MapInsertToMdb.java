package tong.map.MapProcess;
import java.io.IOException;
/**
 * @author ddyyxx
 * openStreetMap:  将从OpenStreetMap上下载下来的地图文件进行分析，获取需要的信息（点，弧，路）存储到本地文件
 * MapPre		：     将得到的（点，弧，路）信息进行进一步处理，得到点，弧，路的关系。
 * MdbInsert	:  将前两步处理后得到的点，弧，路插入到数据库中（mongodb）
 */
public class MapInsertToMdb {
	public static void main(String args[]) {
		try {
			OpenStreetMap.mapAnalysis();//原始地图数据分析
			MapPre.mapPrepare();	//进一步确定点，弧，路的关系，构造弧，点，路编号
			MdbInsert.mdbInsert();	//将地图数据插入数据库
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
}
