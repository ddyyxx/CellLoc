package tong.map.MapProcess;
import java.io.IOException;
/**
 * @author ddyyxx
 * openStreetMap:  ����OpenStreetMap�����������ĵ�ͼ�ļ����з�������ȡ��Ҫ����Ϣ���㣬����·���洢�������ļ�
 * MapPre		��     ���õ��ģ��㣬����·����Ϣ���н�һ�������õ��㣬����·�Ĺ�ϵ��
 * MdbInsert	:  ��ǰ���������õ��ĵ㣬����·���뵽���ݿ��У�mongodb��
 */
public class MapInsertToMdb {
	public static void main(String args[]) {
		try {
			OpenStreetMap.mapAnalysis();//ԭʼ��ͼ���ݷ���
			MapPre.mapPrepare();	//��һ��ȷ���㣬����·�Ĺ�ϵ�����컡���㣬·���
			MdbInsert.mdbInsert();	//����ͼ���ݲ������ݿ�
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
	}
}
